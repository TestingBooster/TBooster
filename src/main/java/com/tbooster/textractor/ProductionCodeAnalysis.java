package com.tbooster.textractor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.tbooster.models.*;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;
import com.tbooster.utils.JavaParserUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/5/28 12:01 PM
  */
public class ProductionCodeAnalysis extends SourceCodeAnalysis{

    /**
      * Extract all method blocks from production codes and save to 'productionMethodClassDirectoryPath';
      * @return void
      * @date 2020/6/18 10:57 AM
      * @author xxx
      */
    public void extractAllMethodBlocksFromProductionCode() {
        String singleMethodInProductionCodeDirectoryPath = this.getSingleMethodInProductionCodeDirectoryPath();
        Map<String, String> allExtendedSignatureMD5Map = new HashMap<>();

        File[] productionFileArray = this.getProductionFileArray();
        for (File productionFile : productionFileArray) {
            String fileName = productionFile.getName();
//            if (!"JdnssArgs.java".equals(fileName)) {
//                continue;
//            }
//            System.out.println("----------------------" + fileName + "----------------------");
            String basicClassOrInterfaceName = fileName.replace(".java", "");
            SourceCodeModel sourceCodeModel = parseCodeFile(productionFile, this.getSrcDirectory());
            if (sourceCodeModel == null) {
                continue;
            }
            int classOrInterfaceOrEnum = sourceCodeModel.getClassOrInterfaceOrEnum();
            if (classOrInterfaceOrEnum == -1) {
                // empty file
                continue;
            }

            PackageDeclaration packageDeclaration = sourceCodeModel.getPackageDeclaration();
            ClassOrInterfaceDeclaration basicClassOrInterfaceDeclaration = sourceCodeModel.getClassOrInterfaceDeclaration();
            List<ImportDeclaration> importDeclarationList = sourceCodeModel.getImportDeclarationList();

            List<InitializerDeclaration> initializerDeclarationListInBasicTypeDeclaration = null;
            List<FieldDeclaration> fieldDeclarationListInBasicTypeDeclaration = null;
            if (classOrInterfaceOrEnum == 0 || classOrInterfaceOrEnum == 1) {
                // class or interface
                if (basicClassOrInterfaceDeclaration != null) {
                    initializerDeclarationListInBasicTypeDeclaration = basicClassOrInterfaceDeclaration.findAll(InitializerDeclaration.class);
                    fieldDeclarationListInBasicTypeDeclaration = basicClassOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(basicClassOrInterfaceDeclaration
                            , packageDeclaration, importDeclarationList, null
                            , initializerDeclarationListInBasicTypeDeclaration
                            , 1);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , singleMethodInProductionCodeDirectoryPath, basicClassOrInterfaceName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                        }
                    }
                    basicClassOrInterfaceDeclaration = null;
                }
            }
            if (classOrInterfaceOrEnum == 2) {
                // enum
                EnumDeclaration enumDeclaration = sourceCodeModel.getInnerEnumDeclarationList().get(0);
                initializerDeclarationListInBasicTypeDeclaration = enumDeclaration.findAll(InitializerDeclaration.class);
                fieldDeclarationListInBasicTypeDeclaration = enumDeclaration.findAll(FieldDeclaration.class);
                List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(enumDeclaration
                        , packageDeclaration, importDeclarationList, null
                        , initializerDeclarationListInBasicTypeDeclaration
                        , 1);
                if (methodFragmentList != null) {
                    Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                            , singleMethodInProductionCodeDirectoryPath, basicClassOrInterfaceName);
                    if (extendedSignatureMD5Map != null) {
                        allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                        extendedSignatureMD5Map = null;
                    }
                }
            }
            List<ClassOrInterfaceDeclaration> innerClassDeclarationList = sourceCodeModel.getInnerClassList();
            if (innerClassDeclarationList != null) {
                for (ClassOrInterfaceDeclaration innerClassDeclaration : innerClassDeclarationList) {
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(innerClassDeclaration
                            , packageDeclaration, importDeclarationList, fieldDeclarationListInBasicTypeDeclaration
                            , initializerDeclarationListInBasicTypeDeclaration
                            , 1);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , singleMethodInProductionCodeDirectoryPath, basicClassOrInterfaceName);
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
                            , initializerDeclarationListInBasicTypeDeclaration
                            , 1);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , singleMethodInProductionCodeDirectoryPath, basicClassOrInterfaceName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
            }
            List<EnumDeclaration> innerEnumDeclarationList = sourceCodeModel.getInnerEnumDeclarationList();
            if (innerEnumDeclarationList != null) {
                for (EnumDeclaration innerEnumDeclaration : innerEnumDeclarationList) {
                    List<CompilationUnit> methodFragmentList = extractMethodClassFromClassOrInterfaceOrEnumDeclaration(innerEnumDeclaration
                            , packageDeclaration, importDeclarationList, fieldDeclarationListInBasicTypeDeclaration
                            , initializerDeclarationListInBasicTypeDeclaration
                            , 1);
                    if (methodFragmentList != null) {
                        Map<String, String> extendedSignatureMD5Map = saveMethodClassListToTargetPath(methodFragmentList
                                , singleMethodInProductionCodeDirectoryPath, basicClassOrInterfaceName);
                        if (extendedSignatureMD5Map != null) {
                            allExtendedSignatureMD5Map.putAll(extendedSignatureMD5Map);
                            extendedSignatureMD5Map = null;
                        }
                        methodFragmentList = null;
                    }
                }
            }

            importDeclarationList = null;
            packageDeclaration = null;
            sourceCodeModel = null;
            productionFile = null;
        }
        productionFileArray = null;

        if (!allExtendedSignatureMD5Map.isEmpty()) {
            String jsonString = (new JacksonUtil()).bean2Json(allExtendedSignatureMD5Map);
            String targetFilePath = singleMethodInProductionCodeDirectoryPath
                    + File.separator
                    + this.getMd5ExtendedSignatureMapFileName();
            FileUtil.writeStringToTargetFile(jsonString, targetFilePath);
            targetFilePath = null;
            jsonString = null;
        }
        allExtendedSignatureMD5Map = null;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2020/7/3 9:05 PM
      * @author xxx
      */
    private String generateExtendedMethodSignatureForMethodCall(MethodCallModel methodCallModel) {
        ScopeModel scopeModel = methodCallModel.getScopeModel();
        StringBuffer stringBuffer = new StringBuffer();
        if (scopeModel != null) {
            if (!scopeModel.isResolved()) {
                return "unresolved";
            }
            String scopeType = scopeModel.getScopeType();
            stringBuffer.append(scopeType + "+");
        }

        String methodName = methodCallModel.getMethodName();
        stringBuffer.append(methodName + "+");

        List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();
        if (argumentModelList == null) {
            stringBuffer.append("()");
            return stringBuffer.toString();
        }
        stringBuffer.append("(");
        int index = 0;
        for (ArgumentModel argumentModel: argumentModelList) {
            if (argumentModel.getArgumentOrder() != index) {
                continue;
            }
            if (!argumentModel.isResolved()) {
                break;
            }
            String argumentType = argumentModel.getArgumentType();
            if (index == argumentModelList.size() - 1) {
                stringBuffer.append(argumentType);
            } else {
                stringBuffer.append(argumentType + ",");
            }
            index++;
        }
        stringBuffer.append(")");
        if (index != argumentModelList.size()) {
            return "unresolved";
        }
        return stringBuffer.toString();
    }


    /**
     * Analyze the method class and extract important information.
     * @return
     * @throws
     * @date 2020/7/17 2:49 PM
     * @author xxx
     */
    public void extractProductionMethodFragment() {
        String methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = this.getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath();

        String md5ExtendedSignatureMapFilePath = methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File md5ExtendedSignatureMapFile = new File(md5ExtendedSignatureMapFilePath);
        if (!md5ExtendedSignatureMapFile.exists()) {
            md5ExtendedSignatureMapFile = null;
            md5ExtendedSignatureMapFilePath = null;
            methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = null;
            return;
        }

        String md5ExtendedSignatureFileContent = FileUtil.readFileContentToString(md5ExtendedSignatureMapFile);
        Map<String, String> md5ExtendedSignatureMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContent, Map.class);

        String fragmentOfSingleMethodInProductionCodeDirectoryPath = this.getFragmentOfSingleMethodInProductionCodeDirectoryPath();

        File singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory = new File(methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath);
        File[] productionMethodClassFileArray = singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory.listFiles();
        CompilationUnit currentMethodClassCU;
        for (File productionMethodClassFile : productionMethodClassFileArray) {
            String fileName = productionMethodClassFile.getName();
            if (!fileName.endsWith(".java")) {
                continue;
            }
//            if (!"xxx.java".equals(fileName)) {
//                continue;
//            }
//            System.out.println("----------------------" + fileName + "----------------------");
            String md5String = fileName.replace(".java", "");
            String extendedSignatureOfCurrentMethod = md5ExtendedSignatureMap.get(md5String);
            currentMethodClassCU = JavaParserUtil.constructCompilationUnit(null
                    , productionMethodClassFile, this.getSrcDirectory());
            extractMethodFragmentFromMethodClass(currentMethodClassCU
                    , extendedSignatureOfCurrentMethod
                    , fragmentOfSingleMethodInProductionCodeDirectoryPath);
            currentMethodClassCU = null;
        }

        String targetMD5ExtendedSignatureMapFilePath = fragmentOfSingleMethodInProductionCodeDirectoryPath
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
        targetMD5ExtendedSignatureMapFilePath = null;
        targetMD5ExtendedSignatureMapFile = null;
        singleMethodInProductionCodeWithoutExternalMethodDependencyDirectory = null;

        productionMethodClassFileArray = null;
        md5ExtendedSignatureMap = null;
        md5ExtendedSignatureMapFile = null;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/18 5:20 PM
      * @author xxx
      */
    public void productionCodeAnalysis() {

        // step1: analyze production file and extract all method classes.
        extractAllMethodBlocksFromProductionCode();

        // step2: process the import dependency and remove all imports that belongs to the project itself.
        File singleMethodInProductionCodeDirectory = new File(this.getSingleMethodInProductionCodeDirectoryPath());
        String singleMethodInProductionCodeWithoutExternalImportDirectoryPath = this.getSingleMethodInProductionCodeWithoutExternalImportDirectoryPath();
        processImportDependency(singleMethodInProductionCodeDirectory
                , null
                , singleMethodInProductionCodeWithoutExternalImportDirectoryPath);

        // step3: process the method dependency.
        processMethodCallInProductionMethodClass();

        // step4: analyze the method class and extract the method fragment.
        extractProductionMethodFragment();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/25 10:51 PM
     * @author xxx
     */
    public void processMethodCallInProductionMethodClass() {
        String methodInProductionCodeWithoutExternalImportDirectoryPath = this.getSingleMethodInProductionCodeWithoutExternalImportDirectoryPath();

        // 2020/8/8 12:38
        String md5ExtendedSignatureMapFilePathInProductionCode = methodInProductionCodeWithoutExternalImportDirectoryPath
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File md5ExtendedSignatureMapFile = new File(md5ExtendedSignatureMapFilePathInProductionCode);

        if (!md5ExtendedSignatureMapFile.exists()) {
            md5ExtendedSignatureMapFile = null;
            md5ExtendedSignatureMapFilePathInProductionCode = null;
            methodInProductionCodeWithoutExternalImportDirectoryPath = null;
            return;
        }

        Set<String> productionMethodExtendedSignatureSet = new HashSet<>();
        String md5ExtendedSignatureFileContentInProductionCode = FileUtil.readFileContentToString(md5ExtendedSignatureMapFile);
        Map<String, String> md5ExtendedSignatureMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContentInProductionCode, Map.class);
        Collection<String> extendedSignatureCollection = md5ExtendedSignatureMap.values();
        productionMethodExtendedSignatureSet.addAll(extendedSignatureCollection);


        String methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = this.getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath();
        StringBuffer targetFilePathStringBuffer = new StringBuffer(methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath
                + File.separator);
        File singleMethodInProductionCodeWithoutExternalImportDirectory = new File(methodInProductionCodeWithoutExternalImportDirectoryPath);
        CompilationUnit currentMethodClassCU;
        File[] productionMethodClassFileArray = singleMethodInProductionCodeWithoutExternalImportDirectory.listFiles();
        for (File productionMethodClassFile : productionMethodClassFileArray) {
            String testMethodClassFileName = productionMethodClassFile.getName();
            if (!testMethodClassFileName.endsWith(".java")) {
                continue;
            }
//            if (!"ea34a8ed13c51f71335fd1036c9ccbe6.java".equals(testMethodClassFileName)) {
//                continue;
//            }
//            System.out.println("----------------------" + testMethodClassFileName + "----------------------");

            targetFilePathStringBuffer.append(testMethodClassFileName);

            String md5String = testMethodClassFileName.replace(".java", "");
            String extendedSignatureOfCurrentMethod = md5ExtendedSignatureMap.get(md5String);

            currentMethodClassCU = JavaParserUtil.constructCompilationUnit(null, productionMethodClassFile, this.getSrcDirectory());

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
            /*
            CallableDeclaration 和 MethodDeclaration 是有区别的
            CallableDeclaration 兼容 MethodDeclaration 和 ConstructorDeclaration
            CallableDeclaration 无法 getType()，即无法获取方法的返回类型，只能通过如下方式获取返回类型
            callableDeclaration.asMethodDeclaration().getTypeAsString();
             */
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
                basicInfoModel.setExtendedTypes(extendedTypes);
                if (!extendedTypes.isEmpty()) {
                    extendClassNameList = new ArrayList<>();
                    for (ClassOrInterfaceType extendedType : extendedTypes) {
                        extendClassNameList.add(extendedType.getNameAsString());
                    }
                }
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

            List<CallableDeclaration> externalMethodCallDependencyList = new ArrayList<>();
            for (MethodDeclaration methodDeclaration : methodDeclarationList) {
                if (methodDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                    externalMethodCallDependencyList.add(methodDeclaration);
                    continue;
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


            if (callableDeclaration == null) {
                /*
                枚举中静态方法提取不出来
                示例
                2018 Feb 121236494+apm-agent-java+apm-agent-plugins+apm-jdbc-plugin
                ea34a8ed13c51f71335fd1036c9ccbe6.java
                2020/8/12 11:55
                 */
                int start = targetFilePathStringBuffer.indexOf(testMethodClassFileName);
                targetFilePathStringBuffer.replace(start, start + testMethodClassFileName.length(), "");
                continue;
            }

//            if (callableDeclaration == null) {
//                /*
//                  已解决 2020/08/08 13:16
//                2020/8/7 13:23
//                如下同一类中如下两个函数的 signature 分别为 Digest(byte[]) 和 digest(byte[])
//                然而，mac 文件命名时是不区分大小写的，会导致文件命名被占用，文件内容被覆盖的情况
//                    public static synchronized String Digest(byte[] dataToHash)
//                    {
//                        Md5_.update(dataToHash, 0, dataToHash.length);
//                        return HexStringFromBytes( Md5_.digest() );
//                    }
//
//                    public String digest(byte[] dataToHash)
//                    {
//                        md5_.update(dataToHash, 0, dataToHash.length);
//                        return HexStringFromBytes( md5_.digest() );
//                    }
//                 */
//                int start = targetFilePathStringBuffer.indexOf(testMethodClassFileName);
//                targetFilePathStringBuffer.replace(start, start + testMethodClassFileName.length(), "");
//                continue;
//            }

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
            }

            List<MethodCallModel> methodDependencyList = new ArrayList<>();
//            System.out.println("+++ Analyze method or constructor dependencies in Method Declaration +++");
            // analyze method calls in the main method.
            List<MethodCallModel> methodDependencyInMainMethodList = analysisMethodDependencyForCallableDeclaration(callableDeclaration
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , globalVariableNameTypedMap
                    , productionMethodExtendedSignatureSet, null
                    , externalMethodCallDependencyList);
            if (!methodDependencyInMainMethodList.isEmpty()) {
                for (MethodCallModel methodCallModel : methodDependencyInMainMethodList) {
                    methodCallModel.setWhereAppears("InMethod");
                }
                methodDependencyList.addAll(methodDependencyInMainMethodList);
            }
            callableDeclaration.remove();

//            /*
//            analyze method calls that are not belongs to any methods.
//            for example: 'final Stats stats = new Stats(cache)' which is a Field Declaration.
//             */
////            System.out.println("+++ Analyze method or constructor dependencies NOT in Method Declaration +++");
//            /*
//            collect local variables in methods that belong to the anonymous class.
//            for example:
//                private static final RequestHandler ERRORING_HANDLER = new RequestHandler() {
//                    @Override
//                    public void load(@NonNull Picasso picasso, @NonNull Request request, @NonNull Callback callback) {
//                        callback.onError(new IllegalStateException("Unrecognized type of request: " + request));
//                    }
//                };
//             */

            List<MethodDeclaration> methodDeclarationListInAnonymousClass = currentMethodClassCU.findAll(MethodDeclaration.class);
            Map<String, String> localVariableNameTypeMap = null;
            if (!methodDeclarationListInAnonymousClass.isEmpty()) {
                localVariableNameTypeMap = new HashMap<>();
                for (MethodDeclaration methodDeclaration : methodDeclarationListInAnonymousClass) {
                    List<VariableDeclarator> localVariableDeclaratorList = methodDeclaration.findAll(VariableDeclarator.class);
                    if (!localVariableDeclaratorList.isEmpty()) {
                        for (VariableDeclarator localVariableDeclarator : localVariableDeclaratorList) {
                            localVariableNameTypeMap.put(localVariableDeclarator.getNameAsString()
                                    , localVariableDeclarator.getTypeAsString());
                        }
                    }
                    // parameters belongs to methods that appear at the anonymous class
                    List<Parameter> innerMethodParameterList =  methodDeclaration.getParameters();
                    if (innerMethodParameterList != null && innerMethodParameterList.size() > 0) {
                        for (Parameter parameter : innerMethodParameterList) {
                            localVariableNameTypeMap.put(parameter.getNameAsString(), parameter.getTypeAsString());
                        }
                    }
                }
            }

            List<MethodCallExpr> methodCallExprList = currentMethodClassCU.findAll(MethodCallExpr.class);
            List<MethodCallModel> methodDependencyNotInAnyMethods = analyzeMethodDependency(methodCallExprList
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , localVariableNameTypeMap
                    , null
                    , globalVariableNameTypedMap
                    , productionMethodExtendedSignatureSet
                    , null
                    , externalMethodCallDependencyList);
            if (!methodDependencyNotInAnyMethods.isEmpty()) {
                for (MethodCallModel methodCallModel : methodDependencyNotInAnyMethods) {
                    methodCallModel.setWhereAppears("NotInMethod");
                }
                methodDependencyList.addAll(methodDependencyNotInAnyMethods);
            }
            List<ObjectCreationExpr> objectCreationExprList = currentMethodClassCU.findAll(ObjectCreationExpr.class);
            List<MethodCallModel> constructorDependencyNotInAnyMethods = analyzeMethodDependency(objectCreationExprList
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , localVariableNameTypeMap
                    , null
                    , globalVariableNameTypedMap
                    , productionMethodExtendedSignatureSet
                    , null
                    , externalMethodCallDependencyList);
            if (!constructorDependencyNotInAnyMethods.isEmpty()) {
                for (MethodCallModel methodCallModel : constructorDependencyNotInAnyMethods) {
                    methodCallModel.setWhereAppears("NotInMethod");
                }
                methodDependencyList.addAll(constructorDependencyNotInAnyMethods);
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
                    , null
                    , methodDependencyList);
            String codeString = newCompilationUnit.toString();
            String targetFilePath = targetFilePathStringBuffer.toString();
            FileUtil.writeStringToTargetFile(codeString, targetFilePath);
            int start = targetFilePathStringBuffer.indexOf(testMethodClassFileName);
            targetFilePathStringBuffer.replace(start, start + targetFilePathStringBuffer.length(), "");

            newCompilationUnit = null;
            basicInfoModel = null;
            globalFieldMap = null;
            enumConstantDeclarationList = null;
            initializerDeclarationList = null;
            fieldDeclarationList = null;
            callableDeclarationList = null;
            methodDependencyList = null;
            importDeclarationList = null;

            localVariableNameTypeMap = null;
            externalMethodCallDependencyList = null;
            currentMethodClassCU = null;
        }


        // copy the md5_extended_signature_map.json file.
        String targetMD5ExtendedSignatureMapFilePath = methodInProductionCodeWithoutExternalMethodDependencyDirectoryPath
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

        productionMethodExtendedSignatureSet = null;
        extendedSignatureCollection = null;
        productionMethodClassFileArray = null;
        md5ExtendedSignatureMap = null;
    }
}
