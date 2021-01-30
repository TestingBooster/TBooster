package com.tbooster.textractor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.*;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;
import com.tbooster.utils.JavaParserUtil;
import com.tbooster.utils.MD5Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/7/18 5:19 PM
  */
public class TestCodeAnalysis extends SourceCodeAnalysis{

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/31 9:06 AM
      * @author xxx
      */
    public int testCodeAnalysis() {

        // Step1: extract all methods from test files and save as method classes.
        extractAllMethodBlocksFromTestCode();

        String methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = this.getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath();
        File singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory = new File(methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath);
        String singleMethodInTestCodeWithoutExternalImportDirectoryPath = this.getSingleMethodInTestCodeWithoutExternalImportDirectoryPath();

        // step2: process the import dependency and remove all imports that belongs to the project itself.
        File singleMethodInTestCodeDirectory = new File(this.getSingleMethodInTestCodeDirectoryPath());
        processImportDependency(singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory
                , singleMethodInTestCodeDirectory
                , singleMethodInTestCodeWithoutExternalImportDirectoryPath);
        singleMethodInTestCodeWithoutExternalImportDirectoryPath = null;
        singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory = null;

        // step3: analyze method dependency.
        // step3-1: load all methods under test (extended signatures).
        String md5ExtendedSignatureMapFileInProductionCodePath = methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File md5ExtendedSignatureMapFileInProductionCode = new File(md5ExtendedSignatureMapFileInProductionCodePath);
        if (!md5ExtendedSignatureMapFileInProductionCode.exists()) {
            return 0;
        }

        Set<String> testTargetSet = new HashSet<>();
        String md5ExtendedSignatureFileContentInProductionCode = FileUtil.readFileContentToString(md5ExtendedSignatureMapFileInProductionCode);
        Map<String, String> md5ExtendedSignatureMapInProductionCode = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContentInProductionCode, Map.class);
        Collection<String> extendedSignatureCollectionInProductionCode = md5ExtendedSignatureMapInProductionCode.values();
        testTargetSet.addAll(extendedSignatureCollectionInProductionCode);
        extendedSignatureCollectionInProductionCode = null;
        md5ExtendedSignatureMapInProductionCode = null;
        md5ExtendedSignatureMapFileInProductionCode = null;
        md5ExtendedSignatureMapFileInProductionCodePath = null;
        methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = null;

        // step3-2: process method calls.
        processMethodCallInTestMethodClass(testTargetSet);

        // step4: analyze test target.
        analyzeTestTarget(testTargetSet);
        testTargetSet = null;

        // step5: extract test case
        int testCaseNumber = generateTestCaseFromTestMethodWithTestTarget();

        return testCaseNumber;
//        return 0;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/4 3:29 PM
      * @author xxx
      */
    private int generateTestCaseFromTestMethodWithTestTarget() {
        String testMethodWithTestTargetDirectoryPath = this.getTestMethodWithTestTargetDirectoryPath();
        String md5ExtendedSignatureMapFileName = this.getMd5ExtendedSignatureMapFileName();
        String md5ExtendedSignatureMapFilePath = testMethodWithTestTargetDirectoryPath
                + File.separator + md5ExtendedSignatureMapFileName;
        File md5ExtendedSignatureMapFile = new File(md5ExtendedSignatureMapFilePath);
        if (!md5ExtendedSignatureMapFile.exists()) {
            // no test methods with test targets.
            md5ExtendedSignatureMapFile = null;
            md5ExtendedSignatureMapFilePath = null;
            md5ExtendedSignatureMapFileName = null;
            testMethodWithTestTargetDirectoryPath = null;
            return 0;
        }
        String md5ExtendedSignatureFileContent = FileUtil.readFileContentToString(md5ExtendedSignatureMapFile);
        Map<String, String> md5ExtendedSignatureMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContent, Map.class);

        File testMethodWithTestTargetDirectory = new File (testMethodWithTestTargetDirectoryPath);
        File[] testMethodWithTestTargetFileArray = testMethodWithTestTargetDirectory.listFiles();
        CompilationUnit currentMethodClassCU;
        int testCaseNumber = 0;
        for (File testMethodWithTestTargetFile : testMethodWithTestTargetFileArray) {
            String fileName = testMethodWithTestTargetFile.getName();
            if (!fileName.endsWith(".java")) {
                continue;
            }
            testCaseNumber++;
//            System.out.println("----------------------" + fileName + "----------------------");
            String md5String = fileName.replace(".java", "");
            String extendedSignatureOfCurrentMethod = md5ExtendedSignatureMap.get(md5String);
            currentMethodClassCU = JavaParserUtil.constructCompilationUnit(null
                    , testMethodWithTestTargetFile, this.getSrcDirectory());
            extractTestCaseFragmentFromMethodClass(currentMethodClassCU, extendedSignatureOfCurrentMethod);
            currentMethodClassCU = null;
        }
        testMethodWithTestTargetFileArray = null;
        testMethodWithTestTargetDirectory = null;


        String fragmentOfTestMethodWithTestTargetDirectoryPath = this.getFragmentOfTestMethodWithTestTargetDirectoryPath();
        String targetFilePath = fragmentOfTestMethodWithTestTargetDirectoryPath
                + File.separator + md5ExtendedSignatureMapFileName;
        File targetFile = new File(targetFilePath);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileUtil.copyFileUsingFileChannels(md5ExtendedSignatureMapFile, targetFile);
        targetFile = null;
        md5ExtendedSignatureMapFile = null;
        targetFilePath = null;
        fragmentOfTestMethodWithTestTargetDirectoryPath = null;
        return testCaseNumber;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/4 3:34 PM
      * @author xxx
      */
    private void extractTestCaseFragmentFromMethodClass(CompilationUnit currentTestMethodClassCU
            , String extendedSignatureOfCurrentMethod) {
        List<ImportDeclaration> importDeclarationList = currentTestMethodClassCU.findAll(ImportDeclaration.class);
        Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationOptional = currentTestMethodClassCU.findFirst(ClassOrInterfaceDeclaration.class);
        Optional<EnumDeclaration> enumDeclarationOptional = currentTestMethodClassCU.findFirst(EnumDeclaration.class);

        List<MethodDeclaration> methodDeclarationList = null;
        List<FieldDeclaration> fieldDeclarationList = null;
        List<InitializerDeclaration> initializerDeclarationList = null;
        List<EnumConstantDeclaration> enumConstantDeclarationList = null;
        List<String> classAnnotationList = null;
        List<String> extendClassNameList = null;
        List<String> implementInterfaceNameList = null;
        if (classOrInterfaceDeclarationOptional.isPresent()) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = classOrInterfaceDeclarationOptional.get();
            NodeList<AnnotationExpr> annotationExprNodeList = classOrInterfaceDeclaration.getAnnotations();
            if (annotationExprNodeList.isEmpty()) {
                classAnnotationList = new ArrayList<>();
                for (AnnotationExpr annotationExpr: annotationExprNodeList) {
                    String annotationName = annotationExpr.getNameAsString();
                    classAnnotationList.add(annotationName);
                }
            }
            NodeList<ClassOrInterfaceType> extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
            if (!extendedTypes.isEmpty()) {
                extendClassNameList = new ArrayList<>();
                for (ClassOrInterfaceType extendedType : extendedTypes) {
                    extendClassNameList.add(extendedType.getNameAsString());
                }
            }
            NodeList<ClassOrInterfaceType> implementedTypes = classOrInterfaceDeclaration.getImplementedTypes();
            if (!implementedTypes.isEmpty()) {
                implementInterfaceNameList = new ArrayList<>();
                for (ClassOrInterfaceType implementedType : implementedTypes) {
                    implementInterfaceNameList.add(implementedType.getNameAsString());
                }
            }
            methodDeclarationList = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
            fieldDeclarationList = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
            initializerDeclarationList = classOrInterfaceDeclaration.findAll(InitializerDeclaration.class);
        } else {
            EnumDeclaration enumDeclaration = enumDeclarationOptional.get();
            NodeList<AnnotationExpr> annotationExprNodeList = enumDeclaration.getAnnotations();
            if (annotationExprNodeList.isEmpty()) {
                classAnnotationList = new ArrayList<>();
                for (AnnotationExpr annotationExpr: annotationExprNodeList) {
                    String annotationName = annotationExpr.getNameAsString();
                    classAnnotationList.add(annotationName);
                }
            }
            NodeList<ClassOrInterfaceType> implementedTypes = enumDeclaration.getImplementedTypes();
            if (!implementedTypes.isEmpty()) {
                implementInterfaceNameList = new ArrayList<>();
                for (ClassOrInterfaceType implementedType : implementedTypes) {
                    implementInterfaceNameList.add(implementedType.getNameAsString());
                }
            }
            methodDeclarationList = enumDeclaration.findAll(MethodDeclaration.class);
            fieldDeclarationList = enumDeclaration.findAll(FieldDeclaration.class);
            initializerDeclarationList = enumDeclaration.findAll(InitializerDeclaration.class);
            enumConstantDeclarationList = enumDeclaration.getEntries();
        }

        List<MethodDeclaration> externalMethodCallDependencyList = new ArrayList<>();
        String beforeClassMethodString = null;
        String beforeMethodString = null;
        String afterMethodString = null;
        String afterClassMethodString = null;
        List<MethodDeclaration> teatTargetDeclarationList = new ArrayList<>();
        MethodDeclaration testMethodDeclaration = null;
        for (MethodDeclaration methodDeclaration : methodDeclarationList) {
            if (methodDeclaration.getAnnotationByName("BeforeClass").isPresent()
                    || methodDeclaration.getAnnotationByName("BeforeAll").isPresent()) {
                beforeClassMethodString = methodDeclaration.toString();
                continue;
            }
            if (methodDeclaration.getAnnotationByName("Before").isPresent()
                    || methodDeclaration.getAnnotationByName("BeforeEach").isPresent()) {
                beforeMethodString = methodDeclaration.toString();
                continue;
            }
            if (methodDeclaration.getAnnotationByName("After").isPresent()
                    || methodDeclaration.getAnnotationByName("AfterEach").isPresent()) {
                afterMethodString = methodDeclaration.toString();
                continue;
            }
            if (methodDeclaration.getAnnotationByName("AfterClass").isPresent()
                    || methodDeclaration.getAnnotationByName("AfterAll").isPresent()) {
                afterClassMethodString = methodDeclaration.toString();
                continue;
            }
            if (methodDeclaration.getAnnotationByName("TBooster_TestTarget").isPresent()) {
                teatTargetDeclarationList.add(methodDeclaration);
                continue;
            }
            if (methodDeclaration.getAnnotationByName("Test").isPresent()) {
                testMethodDeclaration = methodDeclaration;
                continue;
            }
            if (!methodDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                continue;
            }
            externalMethodCallDependencyList.add(methodDeclaration);
        }

        ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(extendedSignatureOfCurrentMethod);
        String className = extendedSignatureComponentModel.getClassOrInterfaceOrEnumName();
        String testMethodName = extendedSignatureComponentModel.getMethodName();

        NewTestCaseModel newTestCaseModel = new NewTestCaseModel();
        newTestCaseModel.setPackageName(extendedSignatureComponentModel.getPackageName());
        newTestCaseModel.setClassName(className);
        if (classAnnotationList != null) {
            newTestCaseModel.setClassAnnotationList(classAnnotationList);
            classAnnotationList = null;
        }
        if (extendClassNameList != null) {
            newTestCaseModel.setExtendedTypeList(extendClassNameList);
            extendClassNameList = null;
        }
        if (implementInterfaceNameList != null) {
            newTestCaseModel.setImplementedTypeList(implementInterfaceNameList);
            implementInterfaceNameList = null;
        }

        newTestCaseModel.setTestMethodName(testMethodName);
        String methodComment = testMethodDeclaration.getJavadoc().toString();
        if (!"Optional.empty".equals(methodComment)) {
            newTestCaseModel.setTestMethodComment(methodComment);
        }
        newTestCaseModel.setExtendedSignature(extendedSignatureOfCurrentMethod);
        newTestCaseModel.setTestMethodCode(testMethodDeclaration.toString());

        newTestCaseModel.setBeforeClassMethod(beforeClassMethodString);
        newTestCaseModel.setBeforeMethod(beforeMethodString);
        newTestCaseModel.setAfterMethod(afterMethodString);
        newTestCaseModel.setAfterClassMethod(afterClassMethodString);

        List<String> testTargetList = new ArrayList<>();
        for (MethodDeclaration teatTargetDeclaration : teatTargetDeclarationList) {
            String extendedSignature = extractExtendedSignatureFromTestTargetDeclaration(teatTargetDeclaration);
            testTargetList.add(extendedSignature);
        }
        newTestCaseModel.setTestTargetList(testTargetList);

        if (enumConstantDeclarationList != null) {
            List<String> enumEntryDependencyList = new ArrayList<>();
            for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarationList) {
                String enumEntryDependency = enumConstantDeclaration.toString();
                enumEntryDependencyList.add(enumEntryDependency);
            }
            newTestCaseModel.setEnumEntryDependencyList(enumEntryDependencyList);
        }

        List<String> globalVariableDependencyList = new ArrayList<>();
        if (!fieldDeclarationList.isEmpty()) {
            for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
                String fieldDeclarationString = fieldDeclaration.toString();
                globalVariableDependencyList.add(fieldDeclarationString);
            }
            if (!globalVariableDependencyList.isEmpty()) {
                newTestCaseModel.setVariableDependencyList(globalVariableDependencyList);
                globalVariableDependencyList = null;
            }
        }

        List<String> initializerDependencyList = new ArrayList<>();
        if (!initializerDeclarationList.isEmpty()) {
            for (InitializerDeclaration initializerDeclaration : initializerDeclarationList) {
                String initializerDependency = initializerDeclaration.toString();
                initializerDependencyList.add(initializerDependency);
            }
            if (!initializerDependencyList.isEmpty()) {
                newTestCaseModel.setInitializerDependencyList(initializerDependencyList);
                initializerDependencyList = null;
            }
        }

        int testFramework = 0;
        int junitVersion = 0;
        int assertFramework = 0;

        List<String> importDependencyList = new ArrayList<>();
        if (!importDeclarationList.isEmpty()) {
            for (ImportDeclaration importDeclaration : importDeclarationList) {
                String importString = importDeclaration.toString();
                if (importString.contains("junit")) {
                    testFramework = 1;
                } else if (importString.contains("org.testng")) {
                    testFramework = 2;
                } else {
                    // others
                }
                if (importString.contains("org.junit.jupiter")) {
                    junitVersion = 5;
                } else if (importString.contains("org.junit.Test")) {
                    junitVersion = 4;
                    assertFramework = 1;
                } else if (importString.contains("junit.framework.TestCase")){
                    junitVersion = 3;
                }
                if (importString.contains("org.assertj.core.api.Assertions")) {
                    assertFramework = 2;
                } else if (importString.contains("com.google.common.truth.Truth")) {
                    assertFramework = 3;
                } else {
                    // others
                }
                if (importString.indexOf(System.lineSeparator()) != -1) {
                    importString = importString.replace(System.lineSeparator(), "");
                }
                importDependencyList.add(importString);
            }
            newTestCaseModel.setImportDependencyList(importDependencyList);
        }

        if (!externalMethodCallDependencyList.isEmpty()) {
            List<String> methodDependencyList = new ArrayList<>();
            for (MethodDeclaration methodDeclaration : externalMethodCallDependencyList) {
                String extendedSignature = extractExtendedSignatureFromTestTargetDeclaration(methodDeclaration);
                methodDependencyList.add(extendedSignature);
            }
            newTestCaseModel.setMethodDependencyList(methodDependencyList);
        }

        newTestCaseModel.setTestFramework(testFramework);
        if (testFramework == 1) {
            newTestCaseModel.setJunitVersion(junitVersion);
        }
        newTestCaseModel.setAssertFramework(assertFramework);
        saveTestCaseFragmentToTargetPath(newTestCaseModel);
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/4 10:09 PM
      * @author xxx
      */
    private void saveTestCaseFragmentToTargetPath(NewTestCaseModel newTestCaseModel) {
        StringBuffer targetFilePathBuffer = new StringBuffer(this.getFragmentOfTestMethodWithTestTargetDirectoryPath()
                + File.separator);
        String extendedSignature = newTestCaseModel.getExtendedSignature();
        String md5String = MD5Util.getMD5(extendedSignature);
        targetFilePathBuffer.append(md5String + ".json");
        String jsonString = (new JacksonUtil()).bean2Json(newTestCaseModel);
        String targetFilePath = targetFilePathBuffer.toString();
        FileUtil.writeStringToTargetFile(jsonString, targetFilePath);
        targetFilePath = null;
        targetFilePathBuffer = null;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/4 3:57 PM
      * @author xxx
      */
    private String extractExtendedSignatureFromTestTargetDeclaration(MethodDeclaration teatTargetDeclaration) {
        StringBuffer extendedSignatureStringBuffer = new StringBuffer();
        NodeList<AnnotationExpr> annotationExprNodeList = teatTargetDeclaration.getAnnotations();
        for (AnnotationExpr annotationExpr : annotationExprNodeList) {
            String annotationName = annotationExpr.getNameAsString();
            int underlineIndex = annotationName.indexOf("_");
            String prefix = annotationName.substring(0, underlineIndex);
            if ("TBooster".equals(prefix)) {
                continue;
            }
            if ("PN".equals(prefix)) {
                String packageName = annotationName.substring(underlineIndex + 1);
                extendedSignatureStringBuffer.append(packageName + "+");
                continue;
            }
            if ("CN".equals(prefix)) {
                String className = annotationName.substring(underlineIndex + 1);
                extendedSignatureStringBuffer.append(className + "+");
            }
        }
        String methodSignature = teatTargetDeclaration.getSignature().asString();
        extendedSignatureStringBuffer.append(methodSignature + "+");
        String returnType = teatTargetDeclaration.getTypeAsString();
        extendedSignatureStringBuffer.append(returnType);
        return extendedSignatureStringBuffer.toString();
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/31 10:13 AM
      * @author xxx
      */
    private void analyzeTestTarget(Set<String> testTargetSet) {
        String md5ExtendedSignatureMapFileName = this.getMd5ExtendedSignatureMapFileName();
        String methodInTestCodeWithoutExternalMethodDependencyDirectoryPath =  this.getSingleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath();
        String md5ExtendedSignatureMapFilePathInTestCode = methodInTestCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator + md5ExtendedSignatureMapFileName;
        File md5ExtendedSignatureMapFile = new File(md5ExtendedSignatureMapFilePathInTestCode);
        if (!md5ExtendedSignatureMapFile.exists()) {
            md5ExtendedSignatureMapFile = null;
            md5ExtendedSignatureMapFilePathInTestCode = null;
            methodInTestCodeWithoutExternalMethodDependencyDirectoryPath = null;
            md5ExtendedSignatureMapFileName = null;
            return;
        }
        String md5ExtendedSignatureFileContentInTestCode = FileUtil.readFileContentToString(md5ExtendedSignatureMapFile);
        Map<String, String> md5ExtendedSignatureMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContentInTestCode, Map.class);

        String testMethodDirectoryPath = this.getTestMethodInTestCodeDirectoryPath();
        String fragmentOfSingleMethodInTestCodeDirectoryPath = this.getFragmentOfSingleMethodInTestCodeDirectoryPath();
        String testMethodWithTestTargetPath = this.getTestMethodWithTestTargetDirectoryPath();

        Map<String, String> md5ExtendedSignatureMapOfMethod = new HashMap<>();
        Map<String, String> md5ExtendedSignatureMapOfTestMethod = new HashMap<>();
        Map<String, String> md5ExtendedSignatureMapOfTestCase = new HashMap<>();

        File singleMethodInTestCodeWithoutExternalMethodDependencyDirectory = new File(methodInTestCodeWithoutExternalMethodDependencyDirectoryPath);
        File[] singleMethodFileArray = singleMethodInTestCodeWithoutExternalMethodDependencyDirectory.listFiles();
        StringBuffer targetFilePathStringBuffer = new StringBuffer(testMethodWithTestTargetPath + File.separator);
        for (File singleMethodFile : singleMethodFileArray) {
            String testMethodClassFileName = singleMethodFile.getName();
            if (!testMethodClassFileName.endsWith(".java")) {
                continue;
            }
//            if (!"xxx.java".equals(testMethodClassFileName)) {
//                continue;
//            }
//            System.out.println("----------------------" + testMethodClassFileName + "----------------------");

            String md5String = testMethodClassFileName.replace(".java", "");
            String extendedSignatureOfCurrentMethod = md5ExtendedSignatureMap.get(md5String);

            CompilationUnit compilationUnit = JavaParserUtil.constructCompilationUnit(null
                    , singleMethodFile, this.getSrcDirectory());
            List<MethodDeclaration> methodDeclarationList = compilationUnit.findAll(MethodDeclaration.class);
            MethodDeclaration testMethodDeclaration = null;
            List<MethodDeclaration> externalMethodDependencyList = new ArrayList<>();
            for (MethodDeclaration methodDeclaration: methodDeclarationList) {
                if (methodDeclaration.getAnnotationByName("Test").isPresent()) {
                    testMethodDeclaration = methodDeclaration;
                    continue;
                }
                if (methodDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                    externalMethodDependencyList.add(methodDeclaration);
                    continue;
                }
            }
            if (testMethodDeclaration == null) {
                // testMethodDeclaration == null 说明没有标注 @Test 的方法，也就是没有测试方法；
                extractMethodFragmentFromMethodClass(compilationUnit
                        , extendedSignatureOfCurrentMethod
                        , fragmentOfSingleMethodInTestCodeDirectoryPath);
                md5ExtendedSignatureMapOfMethod.put(md5String, extendedSignatureOfCurrentMethod);
                continue;
            }
            List<MethodCallExpr> methodCallExprList = testMethodDeclaration.findAll(MethodCallExpr.class);
            if (methodCallExprList.isEmpty()) {
                // methodCallExprList.isEmpty() 说明测试方法中没有方法调用，也就不存在 assertXXX() 方法的调用
                copySingleMethodFileToTestMethodDirectory(singleMethodFile, testMethodDirectoryPath);
                md5ExtendedSignatureMapOfTestMethod.put(md5String, extendedSignatureOfCurrentMethod);
                continue;
            }
            if (externalMethodDependencyList.isEmpty()) {
                // externalMethodDependencyList.isEmpty() 说明测试方法中没有对本项目的外部方法进行调用。
                copySingleMethodFileToTestMethodDirectory(singleMethodFile, testMethodDirectoryPath);
                md5ExtendedSignatureMapOfTestMethod.put(md5String, extendedSignatureOfCurrentMethod);
                externalMethodDependencyList = null;
                continue;
            }
            Set<String> testTargetSetInCurrentTestMethod = analyzeTestTargetInExternalMethodDependencySet(testTargetSet
                    , externalMethodDependencyList);
            externalMethodDependencyList = null;
            if (testTargetSetInCurrentTestMethod == null) {
                // 说明尽管测试方法中有外部方法调用，但是调用的这些方法不属于 production code.
                copySingleMethodFileToTestMethodDirectory(singleMethodFile, testMethodDirectoryPath);
                md5ExtendedSignatureMapOfTestMethod.put(md5String, extendedSignatureOfCurrentMethod);
                continue;
            }
            List<MethodCallExpr> assertMethodCallList = new ArrayList<>();
            for (MethodCallExpr methodCallExpr : methodCallExprList) {
                if (methodCallExpr.getScope().isPresent()) {
                    String scope = methodCallExpr.getScope().toString();
                    if (!"Assert".equals(scope)) {
                        continue;
                    }
                }
                String methodName = methodCallExpr.getNameAsString();
                if (!methodName.startsWith("assert")) {
                    continue;
                }
                assertMethodCallList.add(methodCallExpr);
            }
            if (assertMethodCallList.isEmpty()) {
                // 说明测试方法中没有 assert 方法调用
                copySingleMethodFileToTestMethodDirectory(singleMethodFile, testMethodDirectoryPath);
                md5ExtendedSignatureMapOfTestMethod.put(md5String, extendedSignatureOfCurrentMethod);
                assertMethodCallList = null;
                continue;
            }
            targetFilePathStringBuffer.append(testMethodClassFileName);
            String targetFilePath = targetFilePathStringBuffer.toString();
            String codeString = compilationUnit.toString();
            FileUtil.writeStringToTargetFile(codeString, targetFilePath);
            md5ExtendedSignatureMapOfTestCase.put(md5String, extendedSignatureOfCurrentMethod);
            targetFilePathStringBuffer.setLength(0);
            targetFilePathStringBuffer.append(testMethodWithTestTargetPath + File.separator);
            compilationUnit = null;
        }

        targetFilePathStringBuffer = null;
        singleMethodFileArray = null;
        md5ExtendedSignatureMap = null;
        md5ExtendedSignatureMapFile = null;

        if (!md5ExtendedSignatureMapOfMethod.isEmpty()) {
            String json = (new JacksonUtil()).bean2Json(md5ExtendedSignatureMapOfMethod);
            String targetFilePath = fragmentOfSingleMethodInTestCodeDirectoryPath
                    + File.separator + md5ExtendedSignatureMapFileName;
            FileUtil.writeStringToTargetFile(json, targetFilePath);
        }
        md5ExtendedSignatureMapOfMethod = null;

        if (!md5ExtendedSignatureMapOfTestMethod.isEmpty()) {
            String json = (new JacksonUtil()).bean2Json(md5ExtendedSignatureMapOfTestMethod);
            String targetFilePath = testMethodDirectoryPath
                    + File.separator + md5ExtendedSignatureMapFileName;
            FileUtil.writeStringToTargetFile(json, targetFilePath);
        }
        md5ExtendedSignatureMapOfTestMethod = null;

        if (!md5ExtendedSignatureMapOfTestCase.isEmpty()) {
            String json = (new JacksonUtil()).bean2Json(md5ExtendedSignatureMapOfTestCase);
            String targetFilePath = testMethodWithTestTargetPath
                    + File.separator + md5ExtendedSignatureMapFileName;
            FileUtil.writeStringToTargetFile(json, targetFilePath);
        }
        md5ExtendedSignatureMapOfTestCase = null;
        testMethodWithTestTargetPath = null;

    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/3 9:52 PM
      * @author xxx
      */
    private void copySingleMethodFileToTestMethodDirectory(File singleMethodFile
            , String testMethodDirectoryPath) {
        String singleMethodFileName = singleMethodFile.getName();
        String targetFilePath = testMethodDirectoryPath + File.separator + singleMethodFileName;
        File targetFile = new File(targetFilePath);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileUtil.copyFileUsingFileChannels(singleMethodFile, targetFile);
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/31 10:10 PM
      * @author xxx
      */
    private List<CompilationUnit> buildTestCaseClassForTestTarget(Set<String> testTargetSetInCurrentTestMethod, CompilationUnit compilationUnit) {
        List<CompilationUnit> testCaseCompilationUnitList = new ArrayList<>(testTargetSetInCurrentTestMethod.size());
        for (String testTargetExtendedSignature : testTargetSetInCurrentTestMethod) {
            ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(testTargetExtendedSignature);
            String testTargetName = extendedSignatureComponentModel.getMethodName();
            testTargetName = testTargetName.substring(0,1).toUpperCase() + testTargetName.substring(1);
            CompilationUnit testCaseCompilationUnit = compilationUnit.clone();
            List<MethodDeclaration> methodDeclarationList = testCaseCompilationUnit.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration :methodDeclarationList) {
                if (!methodDeclaration.getAnnotationByName("Test").isPresent()) {
                    continue;
                }
                methodDeclaration.setName("test" + testTargetName);
                break;
            }
            testCaseCompilationUnitList.add(testCaseCompilationUnit);
        }
        return testCaseCompilationUnitList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/31 2:01 PM
      * @author xxx
      */
    private Set<String> searchTestTargetInExternalMethodDependencyByMethodName(String methodName
            , Set<String> testTargetSetInCurrentTestMethod) {
        Set<String> testTargetWithSameMethodNameSet = new HashSet<>();
        for (String testTarget :testTargetSetInCurrentTestMethod) {
            String[] elementArray = testTarget.split("\\+");
            String methodSignature = elementArray[2];
            int end = methodSignature.indexOf("(");
            String tempMethodName = methodSignature.substring(0, end).trim();
            if (!tempMethodName.equals(methodName)) {
                continue;
            }
            testTargetWithSameMethodNameSet.add(testTarget);
        }
        if (testTargetWithSameMethodNameSet.isEmpty()) {
            testTargetWithSameMethodNameSet = null;
        }
        return testTargetWithSameMethodNameSet;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/31 10:33 AM
      * @author xxx
      */
    private Set<String> analyzeTestTargetInExternalMethodDependencySet(Set<String> testTargetSet
            , List<MethodDeclaration> externalMethodDependencyList) {
        Set<String> testTargetSetInCurrentTestMethod = new HashSet<>();
        StringBuffer extendedSignatureStringBuffer = new StringBuffer();
        for (MethodDeclaration methodDeclaration : externalMethodDependencyList) {
            if (!methodDeclaration.getAnnotationByName("WA_InTestMethod").isPresent()) {
                continue;
            }
            NodeList<AnnotationExpr> annotationExprNodeList = methodDeclaration.getAnnotations();
            for (AnnotationExpr annotationExpr : annotationExprNodeList) {
                String annotationName = annotationExpr.getNameAsString();
                int underlineIndex = annotationName.indexOf("_");
                String prefix = annotationName.substring(0, underlineIndex);
                if ("TBooster".equals(prefix)) {
                    continue;
                }
                if ("PN".equals(prefix)) {
                    String packageName = annotationName.substring(underlineIndex + 1);
                    extendedSignatureStringBuffer.append(packageName + "+");
                    continue;
                }
                if ("CN".equals(prefix)) {
                    String className = annotationName.substring(underlineIndex + 1);
                    extendedSignatureStringBuffer.append(className + "+");
                }
            }
            String methodSignature = methodDeclaration.getSignature().asString();
            extendedSignatureStringBuffer.append(methodSignature + "+");
            String returnType = methodDeclaration.asMethodDeclaration().getTypeAsString();
            extendedSignatureStringBuffer.append(returnType);
            String extendedSignature = extendedSignatureStringBuffer.toString();
            extendedSignatureStringBuffer.setLength(0);
            if (!testTargetSet.contains(extendedSignature)) {
                continue;
            }

            methodDeclaration.addMarkerAnnotation("TBooster_TestTarget");
            testTargetSetInCurrentTestMethod.add(extendedSignature);
        }
        extendedSignatureStringBuffer = null;
        if (testTargetSetInCurrentTestMethod.isEmpty()) {
            testTargetSetInCurrentTestMethod = null;
        }
        return testTargetSetInCurrentTestMethod;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/19 9:44 AM
      * @author xxx
      */
    public void extractAllMethodBlocksFromTestCode() {
        Map<String, String> allExtendedSignatureMD5Map = new HashMap<>();
        String methodInTestCodeDirectoryPath = this.getSingleMethodInTestCodeDirectoryPath();
        File[] testFileArray = this.getTestFileArray();
        for (File testFile : testFileArray) {
            String testFileName = testFile.getName();
//            if (!"Meaty.java".equals(testFileName)) {
//                continue;
//            }
//            Systemm.out.println("----------------------" + testFileName + "----------------------");
            String basicClassOrInterfaceOrEnumName = testFileName.replace(".java", "");
            SourceCodeModel sourceCodeModel = parseCodeFile(testFile, this.getSrcDirectory());
            if (sourceCodeModel == null) {
                continue;
            }
            List<ImportDeclaration> importDeclarationList = sourceCodeModel.getImportDeclarationList();
            int classOrInterfaceOrEnum = sourceCodeModel.getClassOrInterfaceOrEnum();
            PackageDeclaration packageDeclaration = sourceCodeModel.getPackageDeclaration();

            ClassOrInterfaceDeclaration basicClassOrInterfaceDeclaration = sourceCodeModel.getClassOrInterfaceDeclaration();
            if (classOrInterfaceOrEnum == -1) {
                // empty file
                continue;
            }

            List<InitializerDeclaration> initializerDeclarationListInBasicTypeDeclaration = null;
            List<FieldDeclaration> fieldDeclarationListInBasicTypeDeclaration = null;
            if (classOrInterfaceOrEnum == 0 || classOrInterfaceOrEnum == 1) {
                // class or interface
                if (basicClassOrInterfaceDeclaration != null) {
                    initializerDeclarationListInBasicTypeDeclaration = basicClassOrInterfaceDeclaration.findAll(InitializerDeclaration.class);
                    fieldDeclarationListInBasicTypeDeclaration = basicClassOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(basicClassOrInterfaceDeclaration
                            , packageDeclaration, importDeclarationList, null
                            , initializerDeclarationListInBasicTypeDeclaration, 2);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , methodInTestCodeDirectoryPath, basicClassOrInterfaceOrEnumName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
            }
            if (classOrInterfaceOrEnum == 2) {
                // enum
                EnumDeclaration enumDeclaration = sourceCodeModel.getInnerEnumDeclarationList().get(0);
                initializerDeclarationListInBasicTypeDeclaration = enumDeclaration.findAll(InitializerDeclaration.class);
                fieldDeclarationListInBasicTypeDeclaration = enumDeclaration.findAll(FieldDeclaration.class);
                List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(enumDeclaration
                        , packageDeclaration, importDeclarationList, null
                        , initializerDeclarationListInBasicTypeDeclaration, 2);
                if (methodFragmentList != null) {
                    Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                            , methodInTestCodeDirectoryPath, basicClassOrInterfaceOrEnumName);
                    if (extendedSignatureMD5Map != null) {
                        allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                        extendedSignatureMD5Map = null;
                    }
                    methodFragmentList = null;
                }
            }
            List<ClassOrInterfaceDeclaration> innerClassDeclarationList = sourceCodeModel.getInnerClassList();
            if (innerClassDeclarationList != null) {
                for (ClassOrInterfaceDeclaration innerClassDeclaration : innerClassDeclarationList) {
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(innerClassDeclaration
                            , packageDeclaration, importDeclarationList, fieldDeclarationListInBasicTypeDeclaration
                            , initializerDeclarationListInBasicTypeDeclaration, 2);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , methodInTestCodeDirectoryPath, basicClassOrInterfaceOrEnumName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
                innerClassDeclarationList = null;
            }
            List<ClassOrInterfaceDeclaration> innerInterfaceDeclarationList = sourceCodeModel.getInnerInterfaceList();
            if (innerInterfaceDeclarationList != null) {
                for (ClassOrInterfaceDeclaration innerInterfaceDeclaration : innerInterfaceDeclarationList) {
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(innerInterfaceDeclaration
                            , packageDeclaration, importDeclarationList, fieldDeclarationListInBasicTypeDeclaration
                            , initializerDeclarationListInBasicTypeDeclaration, 2);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , methodInTestCodeDirectoryPath, basicClassOrInterfaceOrEnumName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
                innerInterfaceDeclarationList = null;
            }
            List<EnumDeclaration> innerEnumDeclarationList = sourceCodeModel.getInnerEnumDeclarationList();
            if (innerEnumDeclarationList != null) {
                for (EnumDeclaration innerEnumDeclaration : innerEnumDeclarationList) {
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(innerEnumDeclaration
                            , packageDeclaration, importDeclarationList, fieldDeclarationListInBasicTypeDeclaration
                            , initializerDeclarationListInBasicTypeDeclaration, 2);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , methodInTestCodeDirectoryPath, basicClassOrInterfaceOrEnumName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
                innerEnumDeclarationList = null;
            }
        }

        if (!allExtendedSignatureMD5Map.isEmpty()) {
            String jsonString = (new JacksonUtil()).bean2Json(allExtendedSignatureMD5Map);
            String targetFilePath = methodInTestCodeDirectoryPath
                    + File.separator
                    + this.getMd5ExtendedSignatureMapFileName();
            FileUtil.writeStringToTargetFile(jsonString, targetFilePath);
            targetFilePath = null;
            jsonString = null;
        }
        allExtendedSignatureMD5Map = null;
        testFileArray = null;
    }


    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/25 10:51 PM
      * @author xxx
      */
    public void processMethodCallInTestMethodClass(Set<String> productionMethodSignatureSetInProject) {
        String methodInTestCodeWithoutExternalImportDirectoryPath = this.getSingleMethodInTestCodeWithoutExternalImportDirectoryPath();
        String md5ExtendedSignatureMapFilePath = methodInTestCodeWithoutExternalImportDirectoryPath
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File md5ExtendedSignatureMapFile = new File(md5ExtendedSignatureMapFilePath);
        if (!md5ExtendedSignatureMapFile.exists()) {
            md5ExtendedSignatureMapFile = null;
            md5ExtendedSignatureMapFilePath = null;
            methodInTestCodeWithoutExternalImportDirectoryPath = null;
            return;
        }

        Set<String> testMethodSignatureSetInProject = new HashSet<>();
        String md5ExtendedSignatureFileContent = FileUtil.readFileContentToString(md5ExtendedSignatureMapFile);
        Map<String, String> md5ExtendedSignatureMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContent, Map.class);
        Collection<String> extendedSignatureCollection = md5ExtendedSignatureMap.values();
        testMethodSignatureSetInProject.addAll(extendedSignatureCollection);
        extendedSignatureCollection = null;
        md5ExtendedSignatureFileContent = null;

        File methodInTestCodeWithoutExternalImportDirectory = new File(methodInTestCodeWithoutExternalImportDirectoryPath);
        File[] testMethodClassFileArray = methodInTestCodeWithoutExternalImportDirectory.listFiles();
        String methodInTestCodeWithoutExternalMethodDependencyDirectoryPath = this.getSingleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath();
        StringBuffer targetFilePathStringBuffer = new StringBuffer(methodInTestCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator);
        CompilationUnit currentMethodClassCU;
        for (File testMethodClassFile : testMethodClassFileArray) {
            String testMethodClassFileName = testMethodClassFile.getName();
            if (!testMethodClassFileName.endsWith(".java")) {
                continue;
            }
//            if (!"com.squareup.picasso3+RequestCreatorTest+getReturnsNullIfNullUriAndResourceId()+void.java".equals(testMethodClassFileName)) {
//                continue;
//            }
            //            System.out.println("----------------------" + testMethodClassFileName + "----------------------");
            targetFilePathStringBuffer.append(testMethodClassFileName);

            String md5String = testMethodClassFileName.replace(".java", "");
            String extendedSignatureOfCurrentMethod = md5ExtendedSignatureMap.get(md5String);

            currentMethodClassCU = JavaParserUtil.constructCompilationUnit(null
                    , testMethodClassFile, this.getSrcDirectory());

            int classOrInterfaceOrEnum = 0;

            List<ImportDeclaration> importDeclarationList = currentMethodClassCU.findAll(ImportDeclaration.class);
            Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationOptional = currentMethodClassCU.findFirst(ClassOrInterfaceDeclaration.class);
            Optional<EnumDeclaration> enumDeclarationOptional = currentMethodClassCU.findFirst(EnumDeclaration.class);

            Optional<PackageDeclaration> packageDeclarationOptional = currentMethodClassCU.findFirst(PackageDeclaration.class);
            String packageName = null;
            if (packageDeclarationOptional.isPresent()) {
                packageName = packageDeclarationOptional.get().getNameAsString();
            }

            String className;
            String enumName;
            NodeList<Modifier> modifiers;
            NodeList<ClassOrInterfaceType> extendedTypes;
            NodeList<ClassOrInterfaceType> implementedTypes;
            List<String> extendClassNameList = null;

            BasicInfoModel basicInfoModel = new BasicInfoModel();
            List<MethodDeclaration> methodDeclarationList = null;
            List<CallableDeclaration> callableDeclarationList = null;
            List<FieldDeclaration> fieldDeclarationList = null;
            List<InitializerDeclaration> initializerDeclarationList = null;
            NodeList<EnumConstantDeclaration> enumConstantDeclarationList = null;
            if (classOrInterfaceDeclarationOptional.isPresent()) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = classOrInterfaceDeclarationOptional.get();
                methodDeclarationList = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
                callableDeclarationList = classOrInterfaceDeclaration.findAll(CallableDeclaration.class);
                fieldDeclarationList = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                initializerDeclarationList = classOrInterfaceDeclaration.findAll(InitializerDeclaration.class);

                basicInfoModel.setAnnotationExprs(classOrInterfaceDeclaration.getAnnotations());
                modifiers = classOrInterfaceDeclaration.getModifiers();
                basicInfoModel.setModifiers(modifiers);
                className = classOrInterfaceDeclaration.getName().getIdentifier();
                basicInfoModel.setName(className);
                extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
                if (!extendedTypes.isEmpty()) {
                    extendClassNameList = new ArrayList<>();
                    for (ClassOrInterfaceType extendedType : extendedTypes) {
                        extendClassNameList.add(extendedType.getNameAsString());
                    }
                }
                basicInfoModel.setExtendedTypes(extendedTypes);
                implementedTypes = classOrInterfaceDeclaration.getImplementedTypes();
                basicInfoModel.setImplementedTypes(implementedTypes);
                if (classOrInterfaceDeclaration.isInterface()) {
                    classOrInterfaceOrEnum = 1;
                }
            } else {
                EnumDeclaration enumDeclaration = enumDeclarationOptional.get();
                methodDeclarationList = enumDeclaration.findAll(MethodDeclaration.class);
                callableDeclarationList = enumDeclaration.findAll(CallableDeclaration.class);
                fieldDeclarationList = enumDeclaration.findAll(FieldDeclaration.class);
                initializerDeclarationList = enumDeclaration.findAll(InitializerDeclaration.class);
                enumConstantDeclarationList = enumDeclaration.getEntries();

                basicInfoModel.setAnnotationExprs(enumDeclaration.getAnnotations());
                modifiers = enumDeclaration.getModifiers();
                basicInfoModel.setModifiers(modifiers);
                enumName = enumDeclaration.getName().getIdentifier();
                basicInfoModel.setName(enumName);
                implementedTypes = enumDeclaration.getImplementedTypes();
                basicInfoModel.setImplementedTypes(implementedTypes);
                classOrInterfaceOrEnum = 2;
            }

            ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(extendedSignatureOfCurrentMethod);
            String classOrInterfaceOrEnumName = extendedSignatureComponentModel.getClassOrInterfaceOrEnumName();
            String methodName = extendedSignatureComponentModel.getMethodName();

            List<MethodDeclaration> beforeAndAfterMethodDeclarationList = new ArrayList<>();

            List<CallableDeclaration> externalMethodCallDependencyList = new ArrayList<>();
            for (MethodDeclaration methodDeclaration : methodDeclarationList) {
                if (methodDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                    externalMethodCallDependencyList.add(methodDeclaration);
                    continue;
                }
                if (methodDeclaration.getAnnotationByName("BeforeClass").isPresent()
                        || methodDeclaration.getAnnotationByName("BeforeAll").isPresent()
                        || methodDeclaration.getAnnotationByName("Before").isPresent()
                        || methodDeclaration.getAnnotationByName("BeforeEach").isPresent()
                        || methodDeclaration.getAnnotationByName("After").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterEach").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterClass").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterAll").isPresent()) {
                    beforeAndAfterMethodDeclarationList.add(methodDeclaration);
                }
            }

            CallableDeclaration callableDeclaration = null;
            for (CallableDeclaration tempCallableDeclaration : callableDeclarationList) {
                String methodDeclarationName = tempCallableDeclaration.getNameAsString();
                if (tempCallableDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                    continue;
                }
                if (!methodDeclarationName.equals(methodName)) {
                    continue;
                }
                callableDeclaration = tempCallableDeclaration;
            }

            // collect global variables and the corresponding types.
            Map<String, FieldDeclaration> globalFieldMap = null;
            Map<String, String> globalVariableNameTypedMap = new HashMap<>();
            if (!fieldDeclarationList.isEmpty()) {
                globalFieldMap = extractGlobalFieldDeclarationAndSavedIntoMap(fieldDeclarationList);
                for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
                    NodeList<VariableDeclarator> variableNodeList = fieldDeclaration.getVariables();
                    for (VariableDeclarator globalVariableDeclarator : variableNodeList) {
                        String globalVariableName = globalVariableDeclarator.getName().getIdentifier();
                        String globalVariableType = globalVariableDeclarator.getType().asString();
                        globalVariableNameTypedMap.put(globalVariableName, globalVariableType);
                    }
                }
                fieldDeclarationList = null;
            }

            List<MethodCallModel> methodDependencyList = new ArrayList<>();
//            System.out.println("+++ Analyze method or constructor dependencies in Method Declaration +++");
            // analyze method calls in the main method.
            List<MethodCallModel> methodDependencyInMainMethodList = analysisMethodDependencyForCallableDeclaration(callableDeclaration
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , globalVariableNameTypedMap
                    , productionMethodSignatureSetInProject, testMethodSignatureSetInProject
                    , externalMethodCallDependencyList);
            if (!methodDependencyInMainMethodList.isEmpty()) {
                for (MethodCallModel methodCallModel : methodDependencyInMainMethodList) {
                    methodCallModel.setWhereAppears("InTestMethod");
                }
                methodDependencyList.addAll(methodDependencyInMainMethodList);
                methodDependencyInMainMethodList = null;
            }
            callableDeclaration.remove();

            // analyze method calls in the 'Before(xxx)' and 'After(xxx)' methods.
            if (beforeAndAfterMethodDeclarationList.size() > 0) {
                for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                    List<MethodCallModel> methodDependencyListInBeforeAndAfterMethodDeclaration = analysisMethodDependencyForCallableDeclaration(beforeAndAfterMethodDeclaration
                            , classOrInterfaceOrEnumName
                            , extendClassNameList
                            , globalVariableNameTypedMap
                            , productionMethodSignatureSetInProject, testMethodSignatureSetInProject
                            , externalMethodCallDependencyList);
                    if (!methodDependencyListInBeforeAndAfterMethodDeclaration.isEmpty()) {
                        for (MethodCallModel methodCallModel : methodDependencyListInBeforeAndAfterMethodDeclaration) {
                            methodCallModel.setWhereAppears("InBeforeOrAfterMethod");
                        }
                        methodDependencyList.addAll(methodDependencyListInBeforeAndAfterMethodDeclaration);
                        methodDependencyListInBeforeAndAfterMethodDeclaration = null;
                    }
                    beforeAndAfterMethodDeclaration.remove();
                }
            }

            /*
            analyze method calls that are not belongs to any methods.
            for example: 'final Stats stats = new Stats(cache)' which is a Field Declaration.
             */
//            System.out.println("+++ Analyze method or constructor dependencies NOT in Method Declaration +++");
            List<MethodCallExpr> methodCallExprList = currentMethodClassCU.findAll(MethodCallExpr.class);
            if (!methodCallExprList.isEmpty()) {
                List<MethodCallModel> methodDependencyNotInAnyMethods = analyzeMethodDependency(methodCallExprList
                        , classOrInterfaceOrEnumName
                        , extendClassNameList
                        , null
                        , null
                        , globalVariableNameTypedMap
                        , productionMethodSignatureSetInProject
                        , testMethodSignatureSetInProject
                        , externalMethodCallDependencyList);
                if (!methodDependencyNotInAnyMethods.isEmpty()) {
                    for (MethodCallModel methodCallModel : methodDependencyNotInAnyMethods) {
                        methodCallModel.setWhereAppears("NotInMethod");
                    }
                    methodDependencyList.addAll(methodDependencyNotInAnyMethods);
                    methodDependencyNotInAnyMethods = null;
                }
                methodCallExprList = null;
            }


            List<ObjectCreationExpr> objectCreationExprList = currentMethodClassCU.findAll(ObjectCreationExpr.class);
            if (!objectCreationExprList.isEmpty()) {
                List<MethodCallModel> constructorDependencyNotInAnyMethods = analyzeMethodDependency(objectCreationExprList
                        , classOrInterfaceOrEnumName
                        , extendClassNameList
                        , null
                        , null
                        , globalVariableNameTypedMap
                        , productionMethodSignatureSetInProject
                        , testMethodSignatureSetInProject
                        , externalMethodCallDependencyList);
                if (!constructorDependencyNotInAnyMethods.isEmpty()) {
                    for (MethodCallModel methodCallModel : constructorDependencyNotInAnyMethods) {
                        methodCallModel.setWhereAppears("NotInMethod");
                    }
                    methodDependencyList.addAll(constructorDependencyNotInAnyMethods);
                    constructorDependencyNotInAnyMethods = null;
                }
                objectCreationExprList = null;
            }


//            System.out.println("Results of method/constructor call dependencies: ");
            Iterator<MethodCallModel> methodCallModelIterator = methodDependencyList.iterator();
            while (methodCallModelIterator.hasNext()) {
                MethodCallModel methodCallModel = methodCallModelIterator.next();
                if (!methodCallModel.isResolved()) {
//                    System.out.println("\t Unresolved: " + methodCallModel.toString());
                    methodCallModelIterator.remove();
                    continue;
                }
//                System.out.println("\t Resolved: " + methodCallModel.toString());
            }

            CompilationUnit newCompilationUnit = buildMethodClassCompilationUnit(classOrInterfaceOrEnum
                    , packageName
                    , importDeclarationList
                    , basicInfoModel
                    , globalFieldMap
                    , callableDeclaration
                    , initializerDeclarationList
                    , enumConstantDeclarationList
                    , beforeAndAfterMethodDeclarationList
                    , methodDependencyList);
            String codeString = newCompilationUnit.toString();
            String targetFilePath = targetFilePathStringBuffer.toString();
            FileUtil.writeStringToTargetFile(codeString, targetFilePath);
            newCompilationUnit = null;
            int start = targetFilePathStringBuffer.indexOf(testMethodClassFileName);
            targetFilePathStringBuffer.replace(start, targetFilePathStringBuffer.length(), "");

            callableDeclarationList = null;
            importDeclarationList = null;
            basicInfoModel = null;
            globalFieldMap = null;
            callableDeclaration = null;
            globalVariableNameTypedMap = null;
            initializerDeclarationList = null;
            enumConstantDeclarationList = null;
            beforeAndAfterMethodDeclarationList = null;
            methodDependencyList = null;
            newCompilationUnit = null;
            currentMethodClassCU = null;
        }
        testMethodClassFileArray = null;

        // copy the md5_extended_signature_map.json file.
        String targetMD5ExtendedSignatureMapFilePath = methodInTestCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File targetMD5ExtendedSignatureMapFile = new File(targetMD5ExtendedSignatureMapFilePath);
        if (!targetMD5ExtendedSignatureMapFile.exists()) {
            try {
                targetMD5ExtendedSignatureMapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileUtil.copyFileUsingFileChannels(md5ExtendedSignatureMapFile, targetMD5ExtendedSignatureMapFile);
        targetMD5ExtendedSignatureMapFile = null;
        md5ExtendedSignatureMapFile = null;

        targetFilePathStringBuffer = null;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2020/8/3 6:49 PM
      * @author xxx
      */
    public void saveSingleMethodAndTestCaseToDatabase(ProjectInfoTableModel projectInfoTableModel) {
        TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
        String fragmentOfSingleMethodInTestCodeDirectoryPath = this.getFragmentOfSingleMethodInTestCodeDirectoryPath();
        saveSingleMethodToDatabase(projectInfoTableModel
                , fragmentOfSingleMethodInTestCodeDirectoryPath, 1);
        File fragmentOfTestMethodWithTestTargetDirectory = new File(this.getFragmentOfTestMethodWithTestTargetDirectoryPath());
        File[] fragmentOfTestMethodWithTestTargetFileArray = fragmentOfTestMethodWithTestTargetDirectory.listFiles();
        for (File fragmentOfTestMethodWithTestTargetFile : fragmentOfTestMethodWithTestTargetFileArray) {
            String fragmentOfTestMethodWithTestTargetFileName = fragmentOfTestMethodWithTestTargetFile.getName();
            if (!fragmentOfTestMethodWithTestTargetFileName.endsWith(".json")) {
                continue;
            }
            if ("md5_extended_signature_map.json".equals(fragmentOfTestMethodWithTestTargetFileName)) {
                continue;
            }
//            if (!"xxx.json".equals(fragmentOfTestMethodWithTestTargetFileName)) {
//                continue;
//            }
            String fileContentString = FileUtil.readFileContentToString(fragmentOfTestMethodWithTestTargetFile);
            NewTestCaseModel newTestCaseModel = (new JacksonUtil()).json2Bean(fileContentString, NewTestCaseModel.class);
            List<String> importDependencyList = newTestCaseModel.getImportDependencyList();
            Set<String> importDependencySet = null;
            if (importDependencyList != null) {
                importDependencySet = saveImportDependencyListToDatabase(importDependencyList);
            }
            TestInfoTableModel testInfoTableModel = prepareTestInfoTableModel(projectInfoTableModel
                    , importDependencySet, newTestCaseModel);
            testInfoTableDao.saveTestInfoToDatabase(testInfoTableModel);

            testInfoTableModel = null;
            newTestCaseModel = null;
            fragmentOfTestMethodWithTestTargetFile = null;
        }
        fragmentOfTestMethodWithTestTargetFileArray = null;
        fragmentOfTestMethodWithTestTargetDirectory = null;
        testInfoTableDao = null;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/4 10:36 PM
      * @author xxx
      */
    private TestInfoTableModel prepareTestInfoTableModel(ProjectInfoTableModel projectInfoTableModel
            , Set<String> importDependencySet, NewTestCaseModel newTestCaseModel) {
        String repositoryId = projectInfoTableModel.getRepositoryId();
        String repositoryName = projectInfoTableModel.getRepositoryName();
        String projectName = projectInfoTableModel.getProjectName();

        TestInfoTableModel testInfoTableModel = new TestInfoTableModel();
        String extendedSignature = newTestCaseModel.getExtendedSignature();
        testInfoTableModel.setExtendedSignature(extendedSignature);
        testInfoTableModel.setPackageName(newTestCaseModel.getPackageName());
        testInfoTableModel.setClassName(newTestCaseModel.getClassName());
        List<String> classAnnotationList = newTestCaseModel.getClassAnnotationList();
        if (classAnnotationList != null) {
            testInfoTableModel.setClassAnnotations(classAnnotationList.toString());
            classAnnotationList = null;
        }
        List<String> extendedTypeList = newTestCaseModel.getExtendedTypeList();
        if (extendedTypeList != null) {
            testInfoTableModel.setExtendsClasses(extendedTypeList.toString());
            extendedTypeList = null;
        }
        List<String> implementedTypeList = newTestCaseModel.getImplementedTypeList();
        if (implementedTypeList != null) {
            testInfoTableModel.setImplementsInterfaces(implementedTypeList.toString());
            implementedTypeList = null;
        }
        testInfoTableModel.setTestMethodName(newTestCaseModel.getTestMethodName());
        testInfoTableModel.setTestMethodCode(newTestCaseModel.getTestMethodCode());
        List<String> testTargetExtendedSignatureList = newTestCaseModel.getTestTargetList();
        if (testTargetExtendedSignatureList != null) {
            Set<String> testTargetSet = new HashSet<>();
            for (String testTargetExtendedSignature : testTargetExtendedSignatureList) {
                String testTargetIdString = repositoryId + "+" + repositoryName + "+" + projectName + "+"
                        + testTargetExtendedSignature;
                String testTargetId = MD5Util.getMD5(testTargetIdString);
                testTargetSet.add(testTargetId);
            }
            testInfoTableModel.setTestTargets(testTargetSet.toString());
            testTargetExtendedSignatureList = null;
        }
        testInfoTableModel.setBeforeClassMethod(newTestCaseModel.getBeforeClassMethod());
        testInfoTableModel.setBeforeMethod(newTestCaseModel.getBeforeMethod());
        testInfoTableModel.setAfterMethod(newTestCaseModel.getAfterMethod());
        testInfoTableModel.setAfterClassMethod(newTestCaseModel.getAfterClassMethod());

        if (importDependencySet != null) {
            testInfoTableModel.setImportDependencies(importDependencySet.toString());
            importDependencySet = null;
        }
        List<String> globalVariableDependencyList = newTestCaseModel.getVariableDependencyList();
        if (globalVariableDependencyList!= null) {
            testInfoTableModel.setVariableDependencies(globalVariableDependencyList.toString());
            globalVariableDependencyList = null;
        }
        List<String> initializerDependencyList = newTestCaseModel.getInitializerDependencyList();
        if (initializerDependencyList != null) {
            testInfoTableModel.setInitializerDependencies(initializerDependencyList.toString());
            initializerDependencyList = null;
        }
        List<String> enumEntryDependencyList = newTestCaseModel.getEnumEntryDependencyList();
        if (enumEntryDependencyList != null) {
            testInfoTableModel.setEnumDependencies(enumEntryDependencyList.toString());
            enumEntryDependencyList = null;
        }
        List<String> methodDependencyExtendedSignatureList = newTestCaseModel.getMethodDependencyList();
        if (methodDependencyExtendedSignatureList != null) {
            Set<String> methodDependencySet = new HashSet<>();
            for (String methodDependencyExtendedSignature : methodDependencyExtendedSignatureList) {
                String methodIdString = repositoryId + "+" + repositoryName + "+" + projectName + "+"
                        + methodDependencyExtendedSignature;
                String methodId = MD5Util.getMD5(methodIdString);
                methodDependencySet.add(methodId);
            }
            testInfoTableModel.setMethodDependencies(methodDependencySet.toString());
            methodDependencyExtendedSignatureList = null;
        }
        testInfoTableModel.setTestFramework(newTestCaseModel.getTestFramework());
        testInfoTableModel.setJunitVersion(newTestCaseModel.getJunitVersion());
        testInfoTableModel.setAssertFramework(newTestCaseModel.getAssertFramework());
        testInfoTableModel.setProjectId(projectInfoTableModel.getProjectId());
        String testCaseIdString = repositoryId + "+" + repositoryName
                + "+" + projectName + "+" + extendedSignature;
        String testCaseId = MD5Util.getMD5(testCaseIdString);
        testInfoTableModel.setTestCaseId(testCaseId);
        return testInfoTableModel;
    }
}
