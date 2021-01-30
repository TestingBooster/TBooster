package com.tbooster.textractor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.tbooster.dao.ImportInfoTableDao;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.models.*;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;
import com.tbooster.utils.JavaParserUtil;
import com.tbooster.utils.MD5Util;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * @Author xxx
  * @Date 2020/7/19 9:50 AM
  */
public class SourceCodeAnalysis {

    private String srcDirectoryPath;
    private File srcDirectory;
    
    private int productionFileNumber;
    private int testFileNumber;
    
    private File[] productionFileArray;
    private File[] testFileArray;

    private String tboosterDirectoryPath;

    private String md5ExtendedSignatureMapFileName;

    private String methodInProductionCodeDirectoryPath;
    private String singleMethodInProductionCodeDirectoryPath;
    private String singleMethodInProductionCodeWithoutExternalImportDirectoryPath;
    private String singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath;
    private String fragmentOfSingleMethodInProductionCodeDirectoryPath;

    private String methodInTestCodeDirectoryPath;
    private String singleMethodInTestCodeDirectoryPath;
    private String singleMethodInTestCodeWithoutExternalImportDirectoryPath;
    private String singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath;
    private String fragmentOfSingleMethodInTestCodeDirectoryPath;
    private String testMethodInTestCodeDirectoryPath;
    private String testMethodWithTestTargetDirectoryPath;
    private String fragmentOfTestMethodWithTestTargetDirectoryPath;

    private Map<String, PackageDeclaration> packageDeclarationInAllProductionFilesMap = new HashMap<>();
    private Map<String, PackageDeclaration> packageDeclarationInAllTestFilesMap = new HashMap<>();

    private Map<String, List<FieldDeclaration>> fieldDeclarationInAllProductionFilesMap = new HashMap<>();
    private Map<String, List<FieldDeclaration>> fieldDeclarationInAllTestFilesMap = new HashMap<>();

    public void setSrcDirectoryPath(String srcDirectoryPath) {
        this.srcDirectoryPath = srcDirectoryPath;
    }

    public void setSrcDirectory(File srcDirectory) {
        this.srcDirectory = srcDirectory;
    }

    public void setProductionFileNumber(int productionFileNumber) {
        this.productionFileNumber = productionFileNumber;
    }

    public void setTestFileNumber(int testFileNumber) {
        this.testFileNumber = testFileNumber;
    }

    public void setProductionFileArray(File[] productionFileArray) {
        this.productionFileArray = productionFileArray;
    }

    public void setTestFileArray(File[] testFileArray) {
        this.testFileArray = testFileArray;
    }

    public void setPackageDeclarationInAllProductionFilesMap(Map<String, PackageDeclaration> packageDeclarationInAllProductionFilesMap) {
        this.packageDeclarationInAllProductionFilesMap = packageDeclarationInAllProductionFilesMap;
    }

    public void setPackageDeclarationInAllTestFilesMap(Map<String, PackageDeclaration> packageDeclarationInAllTestFilesMap) {
        this.packageDeclarationInAllTestFilesMap = packageDeclarationInAllTestFilesMap;
    }

    public void setFieldDeclarationInAllProductionFilesMap(Map<String, List<FieldDeclaration>> fieldDeclarationInAllProductionFilesMap) {
        this.fieldDeclarationInAllProductionFilesMap = fieldDeclarationInAllProductionFilesMap;
    }

    public void setFieldDeclarationInAllTestFilesMap(Map<String, List<FieldDeclaration>> fieldDeclarationInAllTestFilesMap) {
        this.fieldDeclarationInAllTestFilesMap = fieldDeclarationInAllTestFilesMap;
    }

    public String getSrcDirectoryPath() {
        return srcDirectoryPath;
    }

    public File getSrcDirectory() {
        return srcDirectory;
    }

    public String getMd5ExtendedSignatureMapFileName() {
        return md5ExtendedSignatureMapFileName;
    }

    public void setMd5ExtendedSignatureMapFileName(String md5ExtendedSignatureMapFileName) {
        this.md5ExtendedSignatureMapFileName = md5ExtendedSignatureMapFileName;
    }

    public int getProductionFileNumber() {
        return productionFileNumber;
    }

    public int getTestFileNumber() {
        return testFileNumber;
    }

    public File[] getProductionFileArray() {
        return productionFileArray;
    }

    public File[] getTestFileArray() {
        return testFileArray;
    }

    public Map<String, PackageDeclaration> getPackageDeclarationInAllProductionFilesMap() {
        return packageDeclarationInAllProductionFilesMap;
    }

    public Map<String, PackageDeclaration> getPackageDeclarationInAllTestFilesMap() {
        return packageDeclarationInAllTestFilesMap;
    }

    public Map<String, List<FieldDeclaration>> getFieldDeclarationInAllProductionFilesMap() {
        return fieldDeclarationInAllProductionFilesMap;
    }

    public Map<String, List<FieldDeclaration>> getFieldDeclarationInAllTestFilesMap() {
        return fieldDeclarationInAllTestFilesMap;
    }

    public String getTboosterDirectoryPath() {
        return tboosterDirectoryPath;
    }

    public void setTboosterDirectoryPath(String tboosterDirectoryPath) {
        this.tboosterDirectoryPath = tboosterDirectoryPath;
    }

    public String getMethodInProductionCodeDirectoryPath() {
        return methodInProductionCodeDirectoryPath;
    }

    public void setMethodInProductionCodeDirectoryPath(String methodInProductionCodeDirectoryPath) {
        this.methodInProductionCodeDirectoryPath = methodInProductionCodeDirectoryPath;
    }

    public String getMethodInTestCodeDirectoryPath() {
        return methodInTestCodeDirectoryPath;
    }

    public void setMethodInTestCodeDirectoryPath(String methodInTestCodeDirectoryPath) {
        this.methodInTestCodeDirectoryPath = methodInTestCodeDirectoryPath;
    }

    public String getSingleMethodInProductionCodeDirectoryPath() {
        return singleMethodInProductionCodeDirectoryPath;
    }

    public void setSingleMethodInProductionCodeDirectoryPath(String singleMethodInProductionCodeDirectoryPath) {
        this.singleMethodInProductionCodeDirectoryPath = singleMethodInProductionCodeDirectoryPath;
    }

    public String getSingleMethodInTestCodeDirectoryPath() {
        return singleMethodInTestCodeDirectoryPath;
    }

    public void setSingleMethodInTestCodeDirectoryPath(String singleMethodInTestCodeDirectoryPath) {
        this.singleMethodInTestCodeDirectoryPath = singleMethodInTestCodeDirectoryPath;
    }

    public String getSingleMethodInProductionCodeWithoutExternalImportDirectoryPath() {
        return singleMethodInProductionCodeWithoutExternalImportDirectoryPath;
    }

    public void setSingleMethodInProductionCodeWithoutExternalImportDirectoryPath(String singleMethodInProductionCodeWithoutExternalImportDirectoryPath) {
        this.singleMethodInProductionCodeWithoutExternalImportDirectoryPath = singleMethodInProductionCodeWithoutExternalImportDirectoryPath;
    }

    public String getSingleMethodInTestCodeWithoutExternalImportDirectoryPath() {
        return singleMethodInTestCodeWithoutExternalImportDirectoryPath;
    }

    public void setSingleMethodInTestCodeWithoutExternalImportDirectoryPath(String singleMethodInTestCodeWithoutExternalImportDirectoryPath) {
        this.singleMethodInTestCodeWithoutExternalImportDirectoryPath = singleMethodInTestCodeWithoutExternalImportDirectoryPath;
    }

    public String getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath() {
        return singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath;
    }

    public void setSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath(String singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath) {
        this.singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath;
    }

    public String getSingleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath() {
        return singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath;
    }

    public void setSingleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath(String singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath) {
        this.singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath = singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath;
    }

    public String getFragmentOfSingleMethodInProductionCodeDirectoryPath() {
        return fragmentOfSingleMethodInProductionCodeDirectoryPath;
    }

    public void setFragmentOfSingleMethodInProductionCodeDirectoryPath(String fragmentOfSingleMethodInProductionCodeDirectoryPath) {
        this.fragmentOfSingleMethodInProductionCodeDirectoryPath = fragmentOfSingleMethodInProductionCodeDirectoryPath;
    }

    public String getTestMethodWithTestTargetDirectoryPath() {
        return testMethodWithTestTargetDirectoryPath;
    }

    public void setTestMethodWithTestTargetDirectoryPath(String testMethodWithTestTargetDirectoryPath) {
        this.testMethodWithTestTargetDirectoryPath = testMethodWithTestTargetDirectoryPath;
    }

    public String getFragmentOfTestMethodWithTestTargetDirectoryPath() {
        return fragmentOfTestMethodWithTestTargetDirectoryPath;
    }

    public void setFragmentOfTestMethodWithTestTargetDirectoryPath(String fragmentOfTestMethodWithTestTargetDirectoryPath) {
        this.fragmentOfTestMethodWithTestTargetDirectoryPath = fragmentOfTestMethodWithTestTargetDirectoryPath;
    }

    public String getFragmentOfSingleMethodInTestCodeDirectoryPath() {
        return fragmentOfSingleMethodInTestCodeDirectoryPath;
    }

    public void setFragmentOfSingleMethodInTestCodeDirectoryPath(String fragmentOfSingleMethodInTestCodeDirectoryPath) {
        this.fragmentOfSingleMethodInTestCodeDirectoryPath = fragmentOfSingleMethodInTestCodeDirectoryPath;
    }

    public String getTestMethodInTestCodeDirectoryPath() {
        return testMethodInTestCodeDirectoryPath;
    }

    public void setTestMethodInTestCodeDirectoryPath(String testMethodInTestCodeDirectoryPath) {
        this.testMethodInTestCodeDirectoryPath = testMethodInTestCodeDirectoryPath;
    }

    public SourceCodeAnalysis() {
    }

    public SourceCodeAnalysis(String srcDirectoryPath) {
        this.srcDirectoryPath = srcDirectoryPath;
        this.md5ExtendedSignatureMapFileName = "md5_extended_signature_map.json";
        init();
    }

    /**
      * 
      * @param
      * @return
      * @throws
      * @date 2020/7/27 12:00 PM
      * @author xxx
      */
    public void init() {
        File srcDirectory = new File(this.srcDirectoryPath);
        this.srcDirectory = srcDirectory;
        File[] fileArray = srcDirectory.listFiles();
        for (File file : fileArray) {
            if (file.isFile()) {
                continue;
            }
            String fileName = file.getName();
            if (!fileName.equals("main") && !fileName.equals("test")) {
                continue;
            }
            String javaDirectoryPath = file.getAbsolutePath() + File.separator + "java";
            File javaDirectory = new File(javaDirectoryPath);
            if (fileName.equals("main") && javaDirectory.exists()) {
                File[] tempFileArray = FileUtil.findFilesWithSpecifiedSuffixInTargetDirectory(javaDirectory, ".java");
                if (tempFileArray != null) {
                    this.productionFileArray = tempFileArray;
                    this.productionFileNumber = tempFileArray.length;
                }
            }
            if (fileName.equals("test") && javaDirectory.exists()) {
                File[] tempFileArray = FileUtil.findFilesWithSpecifiedSuffixInTargetDirectory(javaDirectory, ".java");
                if (tempFileArray != null) {
                    this.testFileArray = tempFileArray;
                    this.testFileNumber = tempFileArray.length;
                }
            }
            javaDirectory = null;
            javaDirectoryPath = null;
        }
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/10 15:59
      * @author xxx
      */
    public void deleteTBoosterDirectory() {
        String parentPathOfSrcDirectory = this.getSrcDirectory().getParent();
        this.tboosterDirectoryPath = parentPathOfSrcDirectory + File.separator + "tbooster";
        File tboosterDirectory = new File(this.tboosterDirectoryPath);
        if (tboosterDirectory.exists()) {
            FileUtil.deleteDirectory(tboosterDirectory);
        }
    }

    /**
     *
     * @param
     * @return void
     * @throws
     * @date 2020/7/31 10:54 PM
     * @author xxx
     */
    public void initTBoosterAnalysisDirectories() {
        String parentPathOfSrcDirectory = this.getSrcDirectory().getParent();
        this.tboosterDirectoryPath = parentPathOfSrcDirectory + File.separator + "tbooster";
        File tboosterDirectory = new File(this.tboosterDirectoryPath);

        if (!tboosterDirectory.exists()) {
            tboosterDirectory.mkdir();
        }
        tboosterDirectory = null;

        this.methodInProductionCodeDirectoryPath = this.tboosterDirectoryPath + File.separator + "method_in_production_code";
        File productionMethodDirectory = new File(this.methodInProductionCodeDirectoryPath);
        if (!productionMethodDirectory.exists()) {
            productionMethodDirectory.mkdir();
        }
        productionMethodDirectory = null;

        this.singleMethodInProductionCodeDirectoryPath = this.methodInProductionCodeDirectoryPath
                + File.separator + "method";
        File productionMethodClassDirectory = new File(this.singleMethodInProductionCodeDirectoryPath);
        if (!productionMethodClassDirectory.exists()) {
            productionMethodClassDirectory.mkdir();
        }
        productionMethodClassDirectory = null;

        this.singleMethodInProductionCodeWithoutExternalImportDirectoryPath = this.methodInProductionCodeDirectoryPath
                + File.separator + "method_no_external_import";
        File productionMethodClassWithoutExternalImportDirectory = new File(this.singleMethodInProductionCodeWithoutExternalImportDirectoryPath);
        if (!productionMethodClassWithoutExternalImportDirectory.exists()) {
            productionMethodClassWithoutExternalImportDirectory.mkdirs();
        }
        productionMethodClassWithoutExternalImportDirectory = null;

        this.singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath = this.methodInProductionCodeDirectoryPath
                + File.separator + "method_no_external_method_dependency";
        File productionMethodClassWithoutExternalMethodDependencyDirectory = new File(this.singleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath);
        if (!productionMethodClassWithoutExternalMethodDependencyDirectory.exists()) {
            productionMethodClassWithoutExternalMethodDependencyDirectory.mkdirs();
        }
        productionMethodClassWithoutExternalMethodDependencyDirectory = null;

        this.fragmentOfSingleMethodInProductionCodeDirectoryPath = this.methodInProductionCodeDirectoryPath
                + File.separator + "fragment_of_single_method";
        File productionMethodFragmentDirectory = new File(this.fragmentOfSingleMethodInProductionCodeDirectoryPath);
        if (!productionMethodFragmentDirectory.exists()) {
            productionMethodFragmentDirectory.mkdir();
        }
        productionMethodFragmentDirectory = null;

        this.methodInTestCodeDirectoryPath = this.tboosterDirectoryPath + File.separator + "method_in_test_code";
        File testMethodDirectory = new File(this.methodInTestCodeDirectoryPath);
        if (!testMethodDirectory.exists()) {
            testMethodDirectory.mkdir();
        }
        testMethodDirectory = null;

        this.singleMethodInTestCodeDirectoryPath = this.methodInTestCodeDirectoryPath + File.separator + "method";
        File testMethodClassDirectory = new File(this.singleMethodInTestCodeDirectoryPath);
        if (!testMethodClassDirectory.exists()) {
            testMethodClassDirectory.mkdir();
        }
        testMethodClassDirectory = null;

        this.singleMethodInTestCodeWithoutExternalImportDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "method_no_external_import";
        File testMethodClassWithoutExternalImportDirectory = new File(this.singleMethodInTestCodeWithoutExternalImportDirectoryPath);
        if (!testMethodClassWithoutExternalImportDirectory.exists()) {
            testMethodClassWithoutExternalImportDirectory.mkdir();
        }
        testMethodClassWithoutExternalImportDirectory = null;

        this.singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "method_no_external_method_dependency";
        File testMethodClassWithoutExternalMethodDependencyDirectory = new File(this.singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath);
        if (!testMethodClassWithoutExternalMethodDependencyDirectory.exists()) {
            testMethodClassWithoutExternalMethodDependencyDirectory.mkdir();
        }
        testMethodClassWithoutExternalMethodDependencyDirectory = null;

        this.fragmentOfSingleMethodInTestCodeDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "fragment_single_method";
        File fragmentOfSingleMethodInTestCodeDirectory = new File(this.fragmentOfSingleMethodInTestCodeDirectoryPath);
        if (!fragmentOfSingleMethodInTestCodeDirectory.exists()) {
            fragmentOfSingleMethodInTestCodeDirectory.mkdir();
        }
        fragmentOfSingleMethodInTestCodeDirectory = null;

        this.testMethodInTestCodeDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "test_method";
        File testMethodInTestCodeDirectory = new File(this.testMethodInTestCodeDirectoryPath);
        if (!testMethodInTestCodeDirectory.exists()) {
            testMethodInTestCodeDirectory.mkdir();
        }
        testMethodInTestCodeDirectory = null;

        this.testMethodWithTestTargetDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "test_method_with_test_target";
        File testMethodClassWithClearTestTargetDirectory = new File(this.testMethodWithTestTargetDirectoryPath);
        if (!testMethodClassWithClearTestTargetDirectory.exists()) {
            testMethodClassWithClearTestTargetDirectory.mkdir();
        }
        testMethodClassWithClearTestTargetDirectory = null;

        this.fragmentOfTestMethodWithTestTargetDirectoryPath = this.methodInTestCodeDirectoryPath
                + File.separator + "fragment_of_test_method_with_test_target";
        File fragmentOfTestMethodWithTestTargetDirectory = new File(this.fragmentOfTestMethodWithTestTargetDirectoryPath);
        if (!fragmentOfTestMethodWithTestTargetDirectory.exists()) {
            fragmentOfTestMethodWithTestTargetDirectory.mkdir();
        }
        fragmentOfTestMethodWithTestTargetDirectory = null;
    }

    /**
      * Initialize packageDeclarationInAllXXXFilesMap and fieldDeclarationInAllXXXFilesMap
      * @date 2020/7/27 3:19 PM
      * @author xxx
      */
    public void initPackageAndFieldDeclarationMaps() {
        for (File productionFile : this.productionFileArray) {
            extractPackageOrFieldDeclarations(productionFile, 0);
            productionFile = null;
        }
        for (File testFile : this.testFileArray) {
            extractPackageOrFieldDeclarations(testFile, 1);
            testFile = null;
        }
    }

    /**
      * Extract package or field declarations from production and test files.
      * @param productionOrTestFile
      * @param productionOrTestFileFlag 0 : production file; 1 : test file.
      * @return void
      * @date 2020/7/27 3:16 PM
      * @author xxx
      */
    private void extractPackageOrFieldDeclarations(File productionOrTestFile, int productionOrTestFileFlag) {
        CompilationUnit compilationUnit = JavaParserUtil.constructCompilationUnit(null, productionOrTestFile, this.srcDirectory);
        if (compilationUnit == null) {
            return;
        }
        Optional<PackageDeclaration> optionalPackageDeclaration = compilationUnit.findFirst(PackageDeclaration.class);
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        String packageName = "src";
        if (optionalPackageDeclaration.isPresent()) {
            PackageDeclaration packageDeclaration = optionalPackageDeclaration.get();
            packageName = packageDeclaration.getNameAsString();
            if (productionOrTestFileFlag == 0) {
                this.packageDeclarationInAllProductionFilesMap.put(packageName, packageDeclaration);
            } else {
                this.packageDeclarationInAllTestFilesMap.put(packageName, packageDeclaration);
            }
        }
        if (!classOrInterfaceDeclarationList.isEmpty()) {
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                List<FieldDeclaration> fieldDeclarationList = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                if (fieldDeclarationList.isEmpty()) {
                    continue;
                }
                String classOrInterfaceName = classOrInterfaceDeclaration.getNameAsString();
                String classOrInterfaceIdentifier = packageName + "+" + classOrInterfaceName;
                if (productionOrTestFileFlag == 0) {
                    this.fieldDeclarationInAllProductionFilesMap.put(classOrInterfaceIdentifier, fieldDeclarationList);
                } else {
                    this.fieldDeclarationInAllTestFilesMap.put(classOrInterfaceIdentifier, fieldDeclarationList);
                }
                fieldDeclarationList = null;
            }
        }
        classOrInterfaceDeclarationList = null;
        compilationUnit = null;
    }


    /**
      *
      * @return TestCodeAnalysis
      * @date 2020/7/27 7:47 PM
      * @author xxx
      */
    public TestCodeAnalysis initInstanceOfTestCodeAnalysis() {
        TestCodeAnalysis tca = new TestCodeAnalysis();
        tca.setSrcDirectory(this.getSrcDirectory());
        tca.setSrcDirectoryPath(this.getSrcDirectoryPath());
        tca.setMd5ExtendedSignatureMapFileName(this.getMd5ExtendedSignatureMapFileName());
        tca.setProductionFileArray(this.getProductionFileArray());
        tca.setTestFileArray(this.getTestFileArray());
        tca.setProductionFileNumber(this.getProductionFileNumber());
        tca.setTestFileNumber(this.getTestFileNumber());

        tca.setSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath(this.getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath());

        tca.setMethodInTestCodeDirectoryPath(this.getMethodInTestCodeDirectoryPath());
        tca.setSingleMethodInTestCodeDirectoryPath(this.getSingleMethodInTestCodeDirectoryPath());
        tca.setSingleMethodInTestCodeWithoutExternalImportDirectoryPath(this.singleMethodInTestCodeWithoutExternalImportDirectoryPath);
        tca.setSingleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath(this.singleMethodInTestCodeWithoutExternalMethodDependencyDirectoryPath);
        tca.setFragmentOfSingleMethodInTestCodeDirectoryPath(this.fragmentOfSingleMethodInTestCodeDirectoryPath);
        tca.setTestMethodInTestCodeDirectoryPath(this.testMethodInTestCodeDirectoryPath);
        tca.setTestMethodWithTestTargetDirectoryPath(this.testMethodWithTestTargetDirectoryPath);
        tca.setFragmentOfTestMethodWithTestTargetDirectoryPath(this.fragmentOfTestMethodWithTestTargetDirectoryPath);

        tca.setPackageDeclarationInAllProductionFilesMap(this.getPackageDeclarationInAllProductionFilesMap());
        tca.setPackageDeclarationInAllTestFilesMap(this.getPackageDeclarationInAllTestFilesMap());
        tca.setFieldDeclarationInAllProductionFilesMap(this.getFieldDeclarationInAllProductionFilesMap());
        tca.setFieldDeclarationInAllTestFilesMap(this.getFieldDeclarationInAllTestFilesMap());
        return tca;
    }

    /**
      *
      * @return ProductionCodeAnalysis
      * @date 2020/7/27 7:50 PM
      * @author xxx
      */
    public ProductionCodeAnalysis initInstanceOfProductionCodeAnalysis() {
        ProductionCodeAnalysis pca = new ProductionCodeAnalysis();
        pca.setSrcDirectory(this.getSrcDirectory());
        pca.setSrcDirectoryPath(this.getSrcDirectoryPath());
        pca.setMd5ExtendedSignatureMapFileName(this.getMd5ExtendedSignatureMapFileName());
        pca.setProductionFileArray(this.getProductionFileArray());
        pca.setTestFileArray(this.getTestFileArray());
        pca.setProductionFileNumber(this.getProductionFileNumber());
        pca.setTestFileNumber(this.getTestFileNumber());

        pca.setMethodInProductionCodeDirectoryPath(this.getMethodInProductionCodeDirectoryPath());
        pca.setSingleMethodInProductionCodeDirectoryPath(this.getSingleMethodInProductionCodeDirectoryPath());
        pca.setSingleMethodInProductionCodeWithoutExternalImportDirectoryPath(this.getSingleMethodInProductionCodeWithoutExternalImportDirectoryPath());
        pca.setSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath(this.getSingleMethodInProductionCodeWithoutExternalMethodDependencyDirectoryPath());
        pca.setFragmentOfSingleMethodInProductionCodeDirectoryPath(this.getFragmentOfSingleMethodInProductionCodeDirectoryPath());

        pca.setPackageDeclarationInAllProductionFilesMap(this.getPackageDeclarationInAllProductionFilesMap());
        pca.setPackageDeclarationInAllTestFilesMap(this.getPackageDeclarationInAllTestFilesMap());
        pca.setFieldDeclarationInAllProductionFilesMap(this.getFieldDeclarationInAllProductionFilesMap());
        pca.setFieldDeclarationInAllTestFilesMap(this.getFieldDeclarationInAllTestFilesMap());
        return pca;
    }

    /**
     * Parse the code file and save as a SourceCodeModel.
     * @param codeFile
     * @param srcDirectory
     * @return SourceCodeModel
     * @date 2020/6/18 10:59 AM
     * @author xxx
     */
    public SourceCodeModel parseCodeFile(File codeFile, File srcDirectory) {
        String codeFileName = codeFile.getName();
        codeFileName = codeFileName.replace(".java", "").trim();
        SourceCodeModel sourceCodeModel = new SourceCodeModel();
        CompilationUnit cu = JavaParserUtil.constructCompilationUnit(null, codeFile, srcDirectory);
        if (cu == null) {
            return null;
        }

        // parse package
        Optional<PackageDeclaration> packageDeclaration = cu.findFirst(PackageDeclaration.class);
        if (packageDeclaration.isPresent()) {
            sourceCodeModel.setPackageDeclaration(packageDeclaration.get());
        }

        // parse imports
        List<ImportDeclaration> importDeclarationList = cu.findAll(ImportDeclaration.class);
        if (!importDeclarationList.isEmpty()) {
            sourceCodeModel.setImportDeclarationList(importDeclarationList);
        }

        // parse class or interface or enum
        List<ClassOrInterfaceDeclaration> innerClassDeclarationList = null;
        List<ClassOrInterfaceDeclaration> innerInterfaceDeclarationList = null;
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = cu.findAll(ClassOrInterfaceDeclaration.class);
        List<EnumDeclaration> enumDeclarationList = cu.findAll(EnumDeclaration.class);
        if (classOrInterfaceDeclarationList.isEmpty()) {
            if (enumDeclarationList.isEmpty()) {
                sourceCodeModel.setClassOrInterfaceOrEnum(-1);
            } else {
                sourceCodeModel.setClassOrInterfaceOrEnum(2);
                sourceCodeModel.setEnumDeclaration(enumDeclarationList.get(0));
            }
        } else {
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                String identifier = classOrInterfaceDeclaration.getName().getIdentifier();
                if (identifier.equals(codeFileName)) {
                    if (classOrInterfaceDeclaration.isInterface()) {
                        sourceCodeModel.setClassOrInterfaceOrEnum(1);
                        sourceCodeModel.setInterfaceName(identifier);
                    } else {
                        sourceCodeModel.setClassOrInterfaceOrEnum(0);
                        sourceCodeModel.setClassName(identifier);
                    }
                    sourceCodeModel.setClassOrInterfaceDeclaration(classOrInterfaceDeclaration);
                } else {
                    if (classOrInterfaceDeclaration.isInterface()) {
                        if (innerInterfaceDeclarationList == null) {
                            innerInterfaceDeclarationList = new ArrayList<>();
                        }
                        innerInterfaceDeclarationList.add(classOrInterfaceDeclaration);
                    } else {
                        if (innerClassDeclarationList == null) {
                            innerClassDeclarationList = new ArrayList<>();
                        }
                        innerClassDeclarationList.add(classOrInterfaceDeclaration);
                    }
                }
            }
        }
        if (innerClassDeclarationList != null) {
            sourceCodeModel.setInnerClassList(innerClassDeclarationList);
        }
        if (innerInterfaceDeclarationList != null) {
            sourceCodeModel.setInnerInterfaceList(innerInterfaceDeclarationList);
        }
        if (enumDeclarationList != null) {
            sourceCodeModel.setInnerEnumDeclarationList(enumDeclarationList);
        }
        return sourceCodeModel;
    }


    /**
     *
     * @param typeDeclaration ClassOrInterfaceOrEnumDeclaration
     * @param packageDeclaration
     * @param importDeclarationList
     * @param fieldDeclarationListInBasicClass
     * @param initializerDeclarationList
     * @param productionOrTest 1: production class; 2: test class;
     * @return List<CompilationUnit>
     * @date 2020/6/11 11:03 PM
     * @changedate 2020/7/19 10:11 AM
     * @author xxx
     */
    public List<CompilationUnit> extractMethodClassFromClassOrInterfaceOrEnumDeclaration(TypeDeclaration typeDeclaration
            , PackageDeclaration packageDeclaration
            , List<ImportDeclaration> importDeclarationList
            , List<FieldDeclaration> fieldDeclarationListInBasicClass
            , List<InitializerDeclaration> initializerDeclarationList
            , int productionOrTest) {

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        EnumDeclaration enumDeclaration = null;

        int classOrInterfaceOrEnum = 0;

        String className;
        String enumName;
        NodeList<Modifier> modifiers = typeDeclaration.getModifiers();
        NodeList<ClassOrInterfaceType> extendedTypes;
        NodeList<ClassOrInterfaceType> implementedTypes;
        NodeList<EnumConstantDeclaration> enumConstantDeclarations = null;

        List<MethodDeclaration> beforeAndAfterMethodDeclarationList = null;
        if (productionOrTest == 2) {
            // the methods annotated with 'Before' or 'BeforeClass' or 'After' or 'AfterClass'.
            beforeAndAfterMethodDeclarationList = new ArrayList<>();
            List<MethodDeclaration> methodDeclarationList = typeDeclaration.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration : methodDeclarationList) {
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
            if (beforeAndAfterMethodDeclarationList.isEmpty()) {
                beforeAndAfterMethodDeclarationList = null;
            }
        }

        BasicInfoModel basicInfoModel = new BasicInfoModel(modifiers);
        if (typeDeclaration.isClassOrInterfaceDeclaration()) {
            classOrInterfaceDeclaration = typeDeclaration.asClassOrInterfaceDeclaration();
            List<ClassOrInterfaceDeclaration> innerClassOrInterfaceDeclarationList = classOrInterfaceDeclaration.findAll(ClassOrInterfaceDeclaration.class);
            if (!innerClassOrInterfaceDeclarationList.isEmpty()) {
                for (ClassOrInterfaceDeclaration innerClassOrInterfaceDeclaration : innerClassOrInterfaceDeclarationList) {
                    classOrInterfaceDeclaration.remove(innerClassOrInterfaceDeclaration);
                }
            }

            List<EnumDeclaration> innerEnumDeclarationList = classOrInterfaceDeclaration.findAll(EnumDeclaration.class);
            if (!innerEnumDeclarationList.isEmpty()) {
                for (EnumDeclaration innerEnumDeclaration : innerEnumDeclarationList) {
                    classOrInterfaceDeclaration.remove(innerEnumDeclaration);
                }
            }
            className = classOrInterfaceDeclaration.getName().getIdentifier();
            extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
            implementedTypes = classOrInterfaceDeclaration.getImplementedTypes();
            basicInfoModel.setAnnotationExprs(classOrInterfaceDeclaration.getAnnotations());
            basicInfoModel.setName(className);
            basicInfoModel.setExtendedTypes(extendedTypes);
            basicInfoModel.setImplementedTypes(implementedTypes);
            if (classOrInterfaceDeclaration.isInterface()) {
                classOrInterfaceOrEnum = 1;
            }
        }
        if (typeDeclaration.isEnumDeclaration()) {
            enumDeclaration = typeDeclaration.asEnumDeclaration();
            enumName = enumDeclaration.getName().getIdentifier();
            implementedTypes = enumDeclaration.getImplementedTypes();
            basicInfoModel.setAnnotationExprs(enumDeclaration.getAnnotations());
            basicInfoModel.setName(enumName);
            basicInfoModel.setImplementedTypes(implementedTypes);
            classOrInterfaceOrEnum = 2;
            enumConstantDeclarations = enumDeclaration.getEntries();
        }

        String packageName = null;
        if (packageDeclaration != null) {
            packageName = packageDeclaration.getName().asString();
        }

        List<FieldDeclaration> fieldDeclarationList = new ArrayList<>();
        if (fieldDeclarationListInBasicClass != null) {
            fieldDeclarationList.addAll(fieldDeclarationListInBasicClass);
        }
        List<FieldDeclaration> fieldDeclarationListInCurrentTypeDeclaration;
        if (classOrInterfaceDeclaration != null) {
            fieldDeclarationListInCurrentTypeDeclaration = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
        } else {
            fieldDeclarationListInCurrentTypeDeclaration = enumDeclaration.findAll(FieldDeclaration.class);
        }

        if (!fieldDeclarationListInCurrentTypeDeclaration.isEmpty()) {
            fieldDeclarationList.addAll(fieldDeclarationListInCurrentTypeDeclaration);
        }

        Map<String, FieldDeclaration> globalFieldMap = null;
        if (!fieldDeclarationList.isEmpty()) {
            globalFieldMap = extractGlobalFieldDeclarationAndSavedIntoMap(fieldDeclarationList);
        }

        List<CompilationUnit> methodClassCompilationUnitList = new ArrayList<>();

        List<ConstructorDeclaration> constructorDeclarationList;
        List<MethodDeclaration> methodDeclarationList;
        if (classOrInterfaceDeclaration != null) {
            constructorDeclarationList = classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class);
            methodDeclarationList = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
        } else {
            constructorDeclarationList = enumDeclaration.findAll(ConstructorDeclaration.class);
            methodDeclarationList = enumDeclaration.findAll(MethodDeclaration.class);
        }

        if (!constructorDeclarationList.isEmpty()) {
            for (ConstructorDeclaration constructorDeclaration : constructorDeclarationList) {
                CompilationUnit methodClassOrInterfaceCompilationUnit = buildMethodClassCompilationUnit(classOrInterfaceOrEnum
                        , packageName, importDeclarationList, basicInfoModel, globalFieldMap
                        , constructorDeclaration, initializerDeclarationList, enumConstantDeclarations
                        , beforeAndAfterMethodDeclarationList, null);
                methodClassCompilationUnitList.add(methodClassOrInterfaceCompilationUnit);
            }
        }


        if (!methodDeclarationList.isEmpty()) {
            for (MethodDeclaration methodDeclaration : methodDeclarationList) {
                if (methodDeclaration.getAnnotationByName("BeforeClass").isPresent()
                        || methodDeclaration.getAnnotationByName("BeforeAll").isPresent()
                        || methodDeclaration.getAnnotationByName("Before").isPresent()
                        || methodDeclaration.getAnnotationByName("BeforeEach").isPresent()
                        || methodDeclaration.getAnnotationByName("After").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterEach").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterClass").isPresent()
                        || methodDeclaration.getAnnotationByName("AfterAll").isPresent()) {
                    continue;
                }

                if (methodDeclaration.getParentNode().isPresent()) {
                    String parentNodeString = methodDeclaration.getParentNode().get().toString();
                    if (parentNodeString.matches("\\s*new\\s[\\d\\D]+?\\{[\\d\\D]+?\\}")) {
                        /*
                        filter the method in the anonymous class.
                        for example:
                        requestHandler.load(picasso, data, new RequestHandler.Callback() {
                            @Override public void onSuccess(@Nullable Result result) {
                              resultReference.set(result);
                              latch.countDown();
                            }
                         });
                         */
                        continue;
                    }
                }
                CompilationUnit methodClassOrInterfaceCompilationUnit = buildMethodClassCompilationUnit(classOrInterfaceOrEnum
                        , packageName, importDeclarationList, basicInfoModel, globalFieldMap
                        , methodDeclaration, initializerDeclarationList, enumConstantDeclarations
                        , beforeAndAfterMethodDeclarationList, null);
                methodClassCompilationUnitList.add(methodClassOrInterfaceCompilationUnit);
            }
        }

        if (methodClassCompilationUnitList.isEmpty()) {
            methodClassCompilationUnitList = null;
        }
        basicInfoModel = null;
        fieldDeclarationList = null;
        return methodClassCompilationUnitList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/28 11:09 AM
      * @author xxx
      */
    public Map<String,FieldDeclaration> extractGlobalFieldDeclarationAndSavedIntoMap(List<FieldDeclaration> fieldDeclarationList) {
        Map<String, FieldDeclaration> globalFieldMap = new HashMap<>();
        for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
            List<VariableDeclarator> variableDeclaratorList = fieldDeclaration.getVariables();
            if (variableDeclaratorList.size() == 1) {
                String globalIdentifier = variableDeclaratorList.get(0).getName().getIdentifier();
                globalFieldMap.put(globalIdentifier, fieldDeclaration);
            } else {
                /**
                 *
                 *  当出现在一个语句中定义多个全局字段时，
                 *  例如：private int a, b;
                 *  使用下面注释掉的解决方案会抛出异常 IllegalStateException。
                 */
//                    for (VariableDeclarator variableDeclarator : variableDeclaratorList) {
//                        String globalIdentifier = variableDeclarator.getName().getIdentifier();
//                        FieldDeclaration tempFieldDeclaration = new FieldDeclaration();
//                        tempFieldDeclaration.setModifiers(fieldDeclaration.getModifiers());
//                        NodeList<VariableDeclarator> variableDeclaratorNodeList = new NodeList<>();
//                        variableDeclaratorNodeList.add(variableDeclarator);
//                        tempFieldDeclaration.setVariables(variableDeclaratorNodeList);
//                        globalVariableMap.put(globalIdentifier, tempFieldDeclaration);
//                    }

                for (VariableDeclarator variableDeclarator : variableDeclaratorList) {
                    String globalIdentifier = variableDeclarator.getName().getIdentifier();
                    globalFieldMap.put(globalIdentifier, fieldDeclaration);
                }
            }
        }
        return globalFieldMap;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/7 12:10 AM
     * @changedate 2020/7/19 11:06 AM
     * @author xxx
     */
    public CompilationUnit buildMethodClassCompilationUnit(int classOrInterfaceOrEnum, String packageName
            , List<ImportDeclaration> importDeclarationList
            , BasicInfoModel basicInfoModel
            , Map<String, FieldDeclaration> globalFieldMap
            , CallableDeclaration constructorOrMethodDeclaration
            , List<InitializerDeclaration> initializerDeclarationList
            , NodeList<EnumConstantDeclaration> enumConstantDeclarations
            , List<MethodDeclaration> beforeAndAfterMethodDeclarationList
            , List<MethodCallModel> methodDependencyList) {
        CompilationUnit methodClassCompilationUnit;
        if (packageName == null) {
            methodClassCompilationUnit = new CompilationUnit();
        } else {
            methodClassCompilationUnit = new CompilationUnit(packageName);
        }

        String classOrInterfaceOrEnumName = basicInfoModel.getName();
        NodeList<Modifier> modifiers = basicInfoModel.getModifiers();
        NodeList<ClassOrInterfaceType> extendedTypes = basicInfoModel.getExtendedTypes();
        NodeList<ClassOrInterfaceType> implementedTypes = basicInfoModel.getImplementedTypes();
        NodeList<AnnotationExpr> annotationExprs = basicInfoModel.getAnnotationExprs();
        ClassOrInterfaceDeclaration methodClassOrInterfaceDeclaration = null;
        EnumDeclaration methodEnumDeclaration = null;
        if (classOrInterfaceOrEnum == 0) {
            methodClassOrInterfaceDeclaration = methodClassCompilationUnit.addClass(classOrInterfaceOrEnumName);
        } else if (classOrInterfaceOrEnum == 1) {
            methodClassOrInterfaceDeclaration = methodClassCompilationUnit.addInterface(classOrInterfaceOrEnumName);
        } else {
            methodEnumDeclaration = methodClassCompilationUnit.addEnum(classOrInterfaceOrEnumName);
        }
        if (methodClassOrInterfaceDeclaration != null) {
            methodClassOrInterfaceDeclaration.setAnnotations(annotationExprs);
            methodClassOrInterfaceDeclaration.setModifiers(modifiers);
            methodClassOrInterfaceDeclaration.setExtendedTypes(extendedTypes);
            methodClassOrInterfaceDeclaration.setImplementedTypes(implementedTypes);
            /*
            有些内部类是静态的，例如: static class NetworkBroadcastReceiver extends BroadcastReceiver
            当把改类代码置于 basic 类 的位置时，用 javaparser 解析时会如下错误：
            com.github.javaparser.ParseProblemException: (line 15,col 1) 'static' is not allowed here.

            外部类只能是 public 和 default 不能是 private 和 protected
            com.github.javaparser.ParseProblemException: (line 6,col 1) 'private' is not allowed here.
             */
            methodClassOrInterfaceDeclaration.setStatic(false);
            methodClassOrInterfaceDeclaration.setPrivate(false);
            methodClassOrInterfaceDeclaration.setProtected(false);
        } else {
            methodEnumDeclaration.setAnnotations(annotationExprs);
            methodEnumDeclaration.setModifiers(modifiers);
            methodEnumDeclaration.setImplementedTypes(implementedTypes);
            methodEnumDeclaration.setStatic(false);
            methodEnumDeclaration.setPrivate(false);
            methodEnumDeclaration.setProtected(false);
        }


        StringBuffer typeStringBuffer = new StringBuffer();
        if (annotationExprs != null && !annotationExprs.isEmpty()) {
            typeStringBuffer.append(annotationExprs.toString() + "+");
        }
        if (extendedTypes != null && !extendedTypes.isEmpty()) {
            typeStringBuffer.append(extendedTypes.toString() + "+");
        }
        if (implementedTypes != null && !implementedTypes.isEmpty()) {
            typeStringBuffer.append(implementedTypes.toString() + "+");
        }
        NodeList<Parameter> parameterNodeList = constructorOrMethodDeclaration.getParameters();
        if (parameterNodeList != null && !parameterNodeList.isEmpty()) {
            typeStringBuffer.append(parameterNodeList.toString() + "+");
        }
        NodeList<ReferenceType> referenceTypeNodeList = constructorOrMethodDeclaration.getThrownExceptions();
        if (referenceTypeNodeList != null && !referenceTypeNodeList.isEmpty()) {
            /*
            for example:
            Bitmap decodeImageSource(ImageDecoder.Source imageSource, final Request request) throws IOException
             */
            typeStringBuffer.append(referenceTypeNodeList.toString() + "+");
        }
        NodeList<AnnotationExpr> annotationExprNodeList = constructorOrMethodDeclaration.getAnnotations();
        if (annotationExprNodeList != null && !annotationExprNodeList.isEmpty()) {
            typeStringBuffer.append(annotationExprNodeList.toString() + "+");
        }

        String returnType = null;
        BlockStmt constructorOrMethodBody;
        if (constructorOrMethodDeclaration.isConstructorDeclaration()) {
            constructorOrMethodBody = constructorOrMethodDeclaration.asConstructorDeclaration().getBody();
        } else {
            MethodDeclaration md = constructorOrMethodDeclaration.asMethodDeclaration();
            if (md.getBody().isPresent()) {
                constructorOrMethodBody = constructorOrMethodDeclaration.asMethodDeclaration().getBody().get();
            } else {
                /*
                abstract method declaration
                for example:
                abstract void error(Exception e);
                 */
                constructorOrMethodBody = null;
            }
            returnType = constructorOrMethodDeclaration.asMethodDeclaration().getTypeAsString();
        }
        if (returnType != null) {
            typeStringBuffer.append(returnType);
        }

        String methodName = constructorOrMethodDeclaration.getNameAsString();
        String typeString = typeStringBuffer.toString();

        boolean isTestMethod = constructorOrMethodDeclaration.getAnnotationByName("Test").isPresent();

        if (constructorOrMethodBody == null || constructorOrMethodBody.getStatements().isEmpty()) {
            if (importDeclarationList != null) {
                for (ImportDeclaration importDeclaration : importDeclarationList) {
                    String importIdentifier = importDeclaration.getName().getIdentifier();
                    if (findKeywordInString(importIdentifier, typeString) != -1) {
                        methodClassCompilationUnit.addImport(importDeclaration);
                    }
                }
            }
            if (methodClassOrInterfaceDeclaration == null) {
                if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
                    for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                        methodEnumDeclaration.addMember(beforeAndAfterMethodDeclaration);
                    }
                }
                methodEnumDeclaration.addMember(constructorOrMethodDeclaration);
            } else {
                if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
                    for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                        methodClassOrInterfaceDeclaration.addMember(beforeAndAfterMethodDeclaration);
                    }
                }
                methodClassOrInterfaceDeclaration.addMember(constructorOrMethodDeclaration);
            }
            return methodClassCompilationUnit;
        }
        if (enumConstantDeclarations != null) {
//            boolean hasFound = false;
//            for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarations) {
//                String enumIdentifier = enumConstantDeclaration.getName().getIdentifier();
//                for (Statement statement : statementList) {
//                    String statementString = statement.toString().trim();
//                    int index = statementString.indexOf(enumIdentifier);
//                    if (index == -1) {
//                        continue;
//                    }
//                    hasFound = findKeywordInString(enumIdentifier, statementString);
//                    if (hasFound) {
//                        break;
//                    }
//                }
//                if (hasFound) {
//                   break;
//                }
//            }
//            if (hasFound) {
//                methodEnumDeclaration.setEntries(enumConstantDeclarations);
//            }
            methodEnumDeclaration.setEntries(enumConstantDeclarations);
        }

        List<Statement> statementList = new ArrayList<>();
        if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
            for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {

                if (beforeAndAfterMethodDeclaration.getAnnotationByName("BeforeClass").isPresent()
                        || beforeAndAfterMethodDeclaration.getAnnotationByName("BeforeAll").isPresent()
                        || beforeAndAfterMethodDeclaration.getAnnotationByName("Before").isPresent()
                        || beforeAndAfterMethodDeclaration.getAnnotationByName("BeforeEach").isPresent()) {
                    // Before, BeforeClass, BeforeEach, BeforeAll
                    if (!beforeAndAfterMethodDeclaration.getBody().isPresent()) {
                        continue;
                    }
                    List<Statement> statementListInBeforeMethodDeclaration = beforeAndAfterMethodDeclaration.getBody().get().getStatements();
                    if (!statementListInBeforeMethodDeclaration.isEmpty()) {
                        statementList.addAll(statementListInBeforeMethodDeclaration);
                    }
                }
            }
        }

        statementList.addAll(constructorOrMethodBody.getStatements());

        List<FieldDeclaration> relatedFieldDeclarationList = new ArrayList<>();
        NodeList<InitializerDeclaration> relatedInitializerDeclarationList = new NodeList<>();
        if (globalFieldMap != null) {
            int hasFound;
            Iterator<Map.Entry<String, FieldDeclaration>> iterator = globalFieldMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, FieldDeclaration> entry = iterator.next();
                String globalIdentifier = entry.getKey();
                for (Statement statement : statementList) {
                    String statementString = statement.toString().trim();
                    int index = statementString.indexOf(globalIdentifier);
                    if (index == -1) {
                        continue;
                    }
                    hasFound = findKeywordInString(globalIdentifier, statementString);
                    if (hasFound != -1) {
                        relatedFieldDeclarationList.add(entry.getValue());
                        break;
                    }
                }
            }
            iterator = null;
            hasFound = -1;

            if (relatedFieldDeclarationList.size() != 0) {
                // analysis all initializer declarations.
                if (initializerDeclarationList != null && !initializerDeclarationList.isEmpty()) {
                    NodeList<Statement> relatedStatementNodeList;
                    Set<String> statementSet;
                    for (InitializerDeclaration initializerDeclaration : initializerDeclarationList) {
                        NodeList<Statement> statementNodeList = initializerDeclaration.getBody().getStatements();
                        if (!statementNodeList.isEmpty()) {
                            relatedStatementNodeList = new NodeList<>();
                            statementSet = new HashSet<>();
                            Set<String> variableIdentifierSet = extractAllVariableIdentifierFromFieldDeclaration(relatedFieldDeclarationList);
                            while (true) {
                                for (Statement statement : statementNodeList) {
                                    String statementString = statement.toString();
                                    if (statementSet.contains(statementString)) {
                                        continue;
                                    }
                                    for (String variableIdentifier : variableIdentifierSet) {
                                        if (findKeywordInString(variableIdentifier, statementString) == -1) {
                                            continue;
                                        }
                                        relatedStatementNodeList.add(statement);
                                        statementSet.add(statementString);
                                        break;
                                    }
                                }
                                if (relatedStatementNodeList.size() == 0) {
                                    break;
                                }
                                List<FieldDeclaration> fieldListDependedByInitializer = analyzeFieldDependedByInitializer(relatedStatementNodeList
                                        , relatedFieldDeclarationList, globalFieldMap);
                                if (fieldListDependedByInitializer == null) {
                                    break;
                                }
                                boolean exist = false;
                                for (FieldDeclaration fieldDeclaration : fieldListDependedByInitializer) {
                                    NodeList<VariableDeclarator> variableDeclaratorNodeList = fieldDeclaration.getVariables();
                                    for (VariableDeclarator variableDeclarator: fieldDeclaration.getVariables()) {
                                        String variableIdentifier = variableDeclarator.getName().getIdentifier();
                                        if (variableIdentifierSet.contains(variableIdentifier)) {
                                            exist = true;
                                            break;
                                        }
                                    }
                                    if (exist) {
                                        continue;
                                    }
                                    for (VariableDeclarator variableDeclarator :variableDeclaratorNodeList) {
                                        variableIdentifierSet.add(variableDeclarator.getName().getIdentifier());
                                    }
                                    relatedFieldDeclarationList.add(fieldDeclaration);
                                    exist = false;
                                }
                            }

                            if (relatedStatementNodeList.size() > 0) {
                                Collections.sort(relatedStatementNodeList, new Comparator<Statement> (){
                                    @Override
                                    public int compare(Statement s1, Statement s2) {
                                        if (s1.getBegin().get().line > s2.getBegin().get().line) {
                                            return 1;
                                        }
                                        if (s1.getBegin().get().line < s2.getBegin().get().line) {
                                            return -1;
                                        }
                                        return 0;
                                    }
                                });
                                InitializerDeclaration tempInitializerDeclaration = new InitializerDeclaration();
                                if (initializerDeclaration.isStatic()) {
                                    tempInitializerDeclaration.setStatic(true);

                                }
                                tempInitializerDeclaration.setBody(new BlockStmt(relatedStatementNodeList));
                                relatedInitializerDeclarationList.add(tempInitializerDeclaration);
                            }
                            relatedStatementNodeList = null;
                            statementSet = null;
                        }
                    }
                }


                /*
                analyze dependencies among global fields
                for example:
                Field1: private static final String ANDROID_ASSET = "android_asset";
                Field2: private static final int ASSET_PREFIX_LENGTH = (SCHEME_FILE + ":///" + ANDROID_ASSET + "/").length();
                */
                while (true) {
                    List<FieldDeclaration> tempFieldDeclaration = analysisDependencyAmongFields(relatedFieldDeclarationList
                            , globalFieldMap);
                    if (tempFieldDeclaration == null) {
                        break;
                    }
                    relatedFieldDeclarationList.addAll(tempFieldDeclaration);
                }

                // sort all related field declarations.
                Collections.sort(relatedFieldDeclarationList, new Comparator<FieldDeclaration> (){
                    /*
                     * int compare(Type t1, Type t2) 返回一个基本类型的整型，
                     * 返回负数表示：t1 小于 t2，
                     * 返回 0 表示：t1 和 t2 相等，
                     * 返回正数表示：t1 大于 t2
                     */
                    @Override
                    public int compare(FieldDeclaration f1, FieldDeclaration f2) {
                        if (f1.getBegin().get().line > f2.getBegin().get().line) {
                            return 1;
                        }
                        if (f1.getBegin().get().line < f2.getBegin().get().line) {
                            return -1;
                        }
                        return 0;
                    }
                });

                for (FieldDeclaration fieldDeclaration : relatedFieldDeclarationList) {
                    if (methodClassOrInterfaceDeclaration == null) {
                        methodEnumDeclaration.addMember(fieldDeclaration);
                    } else {
                        methodClassOrInterfaceDeclaration.addMember(fieldDeclaration);
                    }
                }

                if (!relatedInitializerDeclarationList.isEmpty()) {
                    for (InitializerDeclaration initializerDeclaration : relatedInitializerDeclarationList) {
                        if (methodClassOrInterfaceDeclaration == null) {
                            methodEnumDeclaration.addMember(initializerDeclaration);
                        } else {
                            methodClassOrInterfaceDeclaration.addMember(initializerDeclaration);
                        }
                    }
                }
            }
            hasFound = -1;
        }

        if (importDeclarationList != null) {
            List<ImportDeclaration> relatedImportDeclarationList = new ArrayList<>();
            for (ImportDeclaration importDeclaration : importDeclarationList) {
                String importString = importDeclaration.toString();
                if (importString.contains("*")) {
                    relatedImportDeclarationList.add(importDeclaration);
                    continue;
                }
                String importIdentifier = importDeclaration.getName().getIdentifier();

                int index;
                int hasFound = -1;
                // check the imports involved in field declarations.
                for (FieldDeclaration fieldDeclaration : relatedFieldDeclarationList) {
                    String fieldDeclarationString = fieldDeclaration.toString();
                    index = fieldDeclarationString.indexOf(importIdentifier);
                    if (index == -1) {
                        continue;
                    }
                    hasFound = findKeywordInString(importIdentifier, fieldDeclarationString);
                    if (hasFound != -1) {
                        break;
                    }
                }
                if (hasFound != -1) {
                    relatedImportDeclarationList.add(importDeclaration);
                    continue;
                }

                // check the imports involved in the initializer declarations.
                if (!relatedInitializerDeclarationList.isEmpty()) {
                    for (InitializerDeclaration initializerDeclaration: relatedInitializerDeclarationList) {
                        NodeList<Statement> statementListInInitializer = initializerDeclaration.getBody().getStatements();
                        if (statementListInInitializer.isEmpty()) {
                            continue;
                        }
                        for (Statement statementInInitializer : statementListInInitializer) {
                            String statementString = statementInInitializer.toString();
                            hasFound = findKeywordInString(importIdentifier, statementString);
                            if (hasFound != -1) {
                                break;
                            }
                        }
                        if (hasFound != -1) {
                            break;
                        }
                    }
                }
                if (hasFound != -1) {
                    relatedImportDeclarationList.add(importDeclaration);
                    continue;
                }

                // check the imports involved in the enum entries.
                if (enumConstantDeclarations != null) {
                    for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarations) {
                        String enumString = enumConstantDeclaration.toString();
                        if (findKeywordInString(importIdentifier, enumString) == -1) {
                            continue;
                        }
                        hasFound = 1;
                        break;
                    }
                }
                if (hasFound != -1) {
                    relatedImportDeclarationList.add(importDeclaration);
                    continue;
                }

                // check the imports involved in the 'Before' and 'After' method declaration.
                if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
                    for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                        NodeList<AnnotationExpr> tempAnnotationExprs = beforeAndAfterMethodDeclaration.getAnnotations();
                        String annotationString = tempAnnotationExprs.toString();
                        hasFound = findKeywordInString(importIdentifier, annotationString);
                        if (hasFound != -1) {
                            break;
                        }
                        String tempSignature = beforeAndAfterMethodDeclaration.getSignature().asString();
                        hasFound = findKeywordInString(importIdentifier, tempSignature);
                        if (hasFound != -1) {
                            break;
                        }
                        String tempReturnType = beforeAndAfterMethodDeclaration.getTypeAsString();
                        hasFound = findKeywordInString(importIdentifier, tempReturnType);
                        if (hasFound != -1) {
                            break;
                        }
                        List<Statement> tempStatementList = null;
                        if (beforeAndAfterMethodDeclaration.getBody().isPresent()) {
                            tempStatementList = beforeAndAfterMethodDeclaration.getBody().get().getStatements();
                            if (tempStatementList.isEmpty()) {
                                continue;
                            }
                        }
                        if (tempStatementList == null) {
                            continue;
                        }
                        for (Statement tempStatement : tempStatementList) {
                            String statementString = tempStatement.toString().trim();
                            List<Comment> commentList = tempStatement.getAllContainedComments();
                            if (!commentList.isEmpty()) {
                                for (Comment comment : commentList) {
                                    statementString = statementString.replace(comment.toString(), "");
                                }
                            }
                            index = statementString.indexOf(importIdentifier);
                            if (index == -1) {
                                continue;
                            }
                            hasFound = findKeywordInString(importIdentifier, statementString);
                            if (hasFound != -1) {
                                break;
                            }
                        }
                    }
                }
                if (hasFound != -1) {
                    relatedImportDeclarationList.add(importDeclaration);
                    continue;
                }

                // check the imports involved in the method signature or the return type.
                index = typeString.indexOf(importIdentifier);
                if (index != -1) {
                    hasFound = findKeywordInString(importIdentifier, typeString);
                    if (hasFound != -1) {
                        relatedImportDeclarationList.add(importDeclaration);
                        continue;
                    }
                }

                // check the imports involved in the statements of the method body.
                for (Statement statement : statementList) {
                    String statementString = statement.toString().trim();
                    List<Comment> commentList = statement.getAllContainedComments();
                    if (!commentList.isEmpty()) {
                        /*
                        the type may be contained in the code comments.
                        for example:
                        the 'Bitmap' in the following comment.
                        if (thrownException != null) {
                            // Log when Android returns a non-null Bitmap after swallowing an IOException.
                            throw thrownException;
                         }
                         */
                        for (Comment comment : commentList) {
                            statementString = statementString.replace(comment.toString(), "");
                        }
                    }
                    index = statementString.indexOf(importIdentifier);
                    if (index == -1) {
                        continue;
                    }
                    hasFound = findKeywordInString(importIdentifier, statementString);
                    if (hasFound != -1) {
                        relatedImportDeclarationList.add(importDeclaration);
                        break;
                    }
                }
            }
            if (relatedImportDeclarationList.size() != 0) {
                Collections.sort(relatedImportDeclarationList, new Comparator<ImportDeclaration> (){
                    @Override
                    public int compare(ImportDeclaration i1, ImportDeclaration i2) {
                        if (i1.getBegin().get().line > i2.getBegin().get().line) {
                            return 1;
                        }
                        if (i1.getBegin().get().line < i2.getBegin().get().line) {
                            return -1;
                        }
                        return 0;
                    }
                });
                for (ImportDeclaration importDeclaration : relatedImportDeclarationList) {
                    methodClassCompilationUnit.addImport(importDeclaration);
                }
            }
            relatedImportDeclarationList = null;
        }
        relatedInitializerDeclarationList = null;
        relatedFieldDeclarationList = null;
        if (methodClassOrInterfaceDeclaration == null) {
            if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
                for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                    methodEnumDeclaration.addMember(beforeAndAfterMethodDeclaration);
                }
            }
            methodEnumDeclaration.addMember(constructorOrMethodDeclaration);
        } else {
            if (isTestMethod && beforeAndAfterMethodDeclarationList != null) {
                for (MethodDeclaration beforeAndAfterMethodDeclaration : beforeAndAfterMethodDeclarationList) {
                    methodClassOrInterfaceDeclaration.addMember(beforeAndAfterMethodDeclaration);
                }
            }
            methodClassOrInterfaceDeclaration.addMember(constructorOrMethodDeclaration);
        }

        if (methodDependencyList != null) {
            Set<String> hasAddExtendedSignature = new HashSet<>();
            ExtendedSignatureComponentModel extendedSignatureComponentModel;
            for (MethodCallModel methodCallModel : methodDependencyList) {
                String extendedSignature = methodCallModel.getExtendedSignature();
                if (hasAddExtendedSignature.contains(extendedSignature)) {
                    continue;
                }
                hasAddExtendedSignature.add(extendedSignature);
                String whereAppears = methodCallModel.getWhereAppears();
                extendedSignatureComponentModel = parseExtendedSignature(extendedSignature);
                MethodDeclaration methodDeclaration = new MethodDeclaration();
                methodDeclaration.addMarkerAnnotation("TBooster_External_Method");
                methodDeclaration.addMarkerAnnotation("PN_"
                        + extendedSignatureComponentModel.getPackageName());
                methodDeclaration.addMarkerAnnotation("CN_"
                        + extendedSignatureComponentModel.getClassOrInterfaceOrEnumName());
                methodDeclaration.addMarkerAnnotation("WA_" + whereAppears);
                methodDeclaration.setName(extendedSignatureComponentModel.getMethodName());
                String tempReturnType = extendedSignatureComponentModel.getReturnType();
                if (!"empty".equals(tempReturnType)) {
                    methodDeclaration.setType(extendedSignatureComponentModel.getReturnType());
                }
                List<String> parameterTypeList = extendedSignatureComponentModel.getParameterTypeList();
                if (parameterTypeList != null) {
                    for (int i = 0; i < parameterTypeList.size(); i++) {
                        Parameter parameter = new Parameter();
                        String parameterType = parameterTypeList.get(i);
                        parameter.setType(parameterType);
                        parameter.setName("para" + i);
                        methodDeclaration.addParameter(parameter);
                    }
                }
                if (methodClassOrInterfaceDeclaration == null) {
                    methodEnumDeclaration.addMember(methodDeclaration);
                } else {
                    methodClassOrInterfaceDeclaration.addMember(methodDeclaration);
                }
            }
            hasAddExtendedSignature = null;
        }

        return methodClassCompilationUnit;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/11 11:13 AM
     * @author xxx
     */
    public int findKeywordInString(String keyword, String string) {
        int flag = -1;
        int stringLength = string.length();
        Matcher matcher = Pattern.compile(keyword).matcher(string);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > 0) {
                char beforeChar = string.charAt(start - 1);
                if (isCharConformToJavaNamingRules(beforeChar)) {
                    continue;
                }
            }
            if (end < stringLength) {
                char afterChar = string.charAt(end);
                if (isCharConformToJavaNamingRules(afterChar)) {
                    continue;
                }
            }
            flag = start;
            break;
        }
        return flag;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/10 12:20 AM
     * @author xxx
     */
    public boolean isCharConformToJavaNamingRules(char character) {
        if ('a' <= character && character <= 'z') {
            return true;
        }
        if ('A' <= character && character <= 'Z') {
            return true;
        }
        if ('0' <= character && character <= '9') {
            return true;
        }
        if (character == '_' || character == '$') {
            return true;
        }
        return false;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/12 4:39 PM
     * @author xxx
     */
    public Set<String> extractAllVariableIdentifierFromFieldDeclaration(List<FieldDeclaration> fieldDeclarationList) {
        Set<String> variableIdentifierSet = new HashSet<>();
        for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
            NodeList<VariableDeclarator> variableDeclaratorList = fieldDeclaration.getVariables();
            for (VariableDeclarator variableDeclarator : variableDeclaratorList) {
                String variableIdentifier = variableDeclarator.getName().getIdentifier();
                variableIdentifierSet.add(variableIdentifier);
            }
        }
        return variableIdentifierSet;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/12 4:20 PM
     * @author xxx
     */
    public List<FieldDeclaration> analyzeFieldDependedByInitializer(NodeList<Statement> relatedStatementNodeList
            , List<FieldDeclaration> relatedFieldDeclarationList, Map<String,FieldDeclaration> globalFieldMap) {
        List<FieldDeclaration> fieldListDependedByInitializer = new ArrayList<>();
        Set<String> variableIdentifierSet = extractAllVariableIdentifierFromFieldDeclaration(relatedFieldDeclarationList);
        Iterator<Map.Entry<String, FieldDeclaration>> iterator = globalFieldMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, FieldDeclaration> entry = iterator.next();
            String globalIdentifier = entry.getKey();
            if (variableIdentifierSet.contains(globalIdentifier)) {
                continue;
            }
            for (Statement statement: relatedStatementNodeList) {
                if (findKeywordInString(globalIdentifier, statement.toString()) != -1) {
                    fieldListDependedByInitializer.add(entry.getValue());
                }
            }

        }
        if (fieldListDependedByInitializer.size() == 0) {
            fieldListDependedByInitializer = null;
        }
        variableIdentifierSet = null;
        iterator = null;
        return fieldListDependedByInitializer;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/11 2:52 PM
     * @author xxx
     */
    public List<FieldDeclaration> analysisDependencyAmongFields(List<FieldDeclaration> relatedFieldDeclarationList
            , Map<String,FieldDeclaration> globalFieldMap) {
        List<FieldDeclaration> tempFieldDeclaration = new ArrayList<>();
        int hasFound = -1;

        Set<String> variableIdentifierSet = extractAllVariableIdentifierFromFieldDeclaration(relatedFieldDeclarationList);

        for (FieldDeclaration fieldDeclaration : relatedFieldDeclarationList) {
            String fieldDeclarationString = fieldDeclaration.toString();
            Iterator<Map.Entry<String, FieldDeclaration>> iterator = globalFieldMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, FieldDeclaration> entry = iterator.next();
                String globalIdentifier = entry.getKey();
                if (variableIdentifierSet.contains(globalIdentifier)) {
                    continue;
                }
                int index = fieldDeclarationString.indexOf(globalIdentifier);
                if (index == -1) {
                    continue;
                }
                hasFound = findKeywordInString(globalIdentifier, fieldDeclarationString);
                if (hasFound != -1) {
                    tempFieldDeclaration.add(entry.getValue());
                    hasFound = -1;
                }
            }
            iterator = null;
        }
        if (tempFieldDeclaration.size() == 0) {
            tempFieldDeclaration = null;
        }
        return tempFieldDeclaration;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 10:58 AM
     * @changedate 2020/7/19 11:17 AM
     * @author xxx
     */
    public Map<String, String> saveMethodClassListToTargetPath(List<CompilationUnit> methodClassList
            , String targetDirectoryPath, String basicClassName) {
        StringBuffer targetFilePathBuffer = new StringBuffer(targetDirectoryPath + File.separator);
        StringBuffer extendedSignatureStringBuffer = new StringBuffer();
        Map<String, String> extendedSignatureMD5Map = new HashMap<>();
        String packageName = "src";
        String classOrInterfaceOrEnumName;
        String methodSignature;
        String extendedSignature;
        for (CompilationUnit methodFragmentCompilationUnit : methodClassList) {
            if (methodFragmentCompilationUnit.getPackageDeclaration().isPresent()) {
                packageName = methodFragmentCompilationUnit.getPackageDeclaration().get().getNameAsString();
            }
            extendedSignatureStringBuffer.append(packageName + "+");
            if (methodFragmentCompilationUnit.findFirst(ClassOrInterfaceDeclaration.class).isPresent()) {
                classOrInterfaceOrEnumName = methodFragmentCompilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString();
            } else {
                classOrInterfaceOrEnumName = methodFragmentCompilationUnit.findFirst(EnumDeclaration.class).get().getNameAsString();
            }
            if (basicClassName.equals(classOrInterfaceOrEnumName)) {
                extendedSignatureStringBuffer.append(classOrInterfaceOrEnumName + "+");
            } else {
                extendedSignatureStringBuffer.append(basicClassName + "." + classOrInterfaceOrEnumName + "+");
            }

            List<CallableDeclaration> callableDeclarationList = methodFragmentCompilationUnit.findAll(CallableDeclaration.class);
            CallableDeclaration tempCallableDeclaration = null;
            for (CallableDeclaration callableDeclaration : callableDeclarationList) {
                if (callableDeclaration.getAnnotationByName("BeforeClass").isPresent()
                        || callableDeclaration.getAnnotationByName("BeforeAll").isPresent()
                        || callableDeclaration.getAnnotationByName("Before").isPresent()
                        || callableDeclaration.getAnnotationByName("BeforeEach").isPresent()
                        || callableDeclaration.getAnnotationByName("After").isPresent()
                        || callableDeclaration.getAnnotationByName("AfterEach").isPresent()
                        || callableDeclaration.getAnnotationByName("AfterClass").isPresent()
                        || callableDeclaration.getAnnotationByName("AfterAll").isPresent()) {
                    continue;
                }
                if (callableDeclaration.getParentNode().isPresent()) {
                    String parentNodeString = callableDeclaration.getParentNode().get().toString();
                    if (parentNodeString.matches("\\s*new\\s[\\d\\D]+?\\{[\\d\\D]+?\\}")) {
                        /*
                        filter the method in the anonymous class.
                        for example:
                        requestHandler.load(picasso, data, new RequestHandler.Callback() {
                            @Override public void onSuccess(@Nullable Result result) {
                              resultReference.set(result);
                              latch.countDown();
                            }
                         });
                         */
                        continue;
                    }
                    tempCallableDeclaration = callableDeclaration;
                }
            }

            methodSignature = tempCallableDeclaration.getSignature().asString();
            extendedSignatureStringBuffer.append(methodSignature + "+");

            String returnType;
            if (tempCallableDeclaration.isConstructorDeclaration()) {
                returnType = "empty";
            } else {
                returnType = tempCallableDeclaration.asMethodDeclaration().getTypeAsString();
                if (returnType.equals(classOrInterfaceOrEnumName)
                        && !basicClassName.equals(classOrInterfaceOrEnumName)) {
                    /*
                    内部类中的方法返回类型为内部类的对象
                    2020/7/31 9:00 AM
                     */
                    returnType = basicClassName + "." + returnType;
                }
            }
            extendedSignatureStringBuffer.append(returnType);
            extendedSignature = extendedSignatureStringBuffer.toString();
            String extendedSignatureMD5 = MD5Util.getMD5(extendedSignature);
            extendedSignatureMD5Map.put(extendedSignatureMD5, extendedSignature);
            int start = extendedSignatureStringBuffer.indexOf(extendedSignature);
            extendedSignatureStringBuffer.replace(start, start + extendedSignature.length(), "");

            String fileName = extendedSignatureMD5 + ".java";
            targetFilePathBuffer.append(fileName);
            String targetFilePath = targetFilePathBuffer.toString();
            String methodFragmentString = methodFragmentCompilationUnit.toString();
            FileUtil.writeStringToTargetFile(methodFragmentString, targetFilePath);
            start = targetFilePathBuffer.indexOf(fileName);
            targetFilePathBuffer.replace(start, start + fileName.length(), "");
        }

        extendedSignatureStringBuffer = null;
        targetFilePathBuffer = null;

        if (extendedSignatureMD5Map.isEmpty()) {
            extendedSignatureMD5Map = null;
        }
        return extendedSignatureMD5Map;
    }

    /**
     * Process the external import in method class file, and save into a new direcotry.
     * @param singleMethodInProductionCodeDirectory
     * @param singleMethodInTestCodeDirectory
     * @param singleMethodWithoutExternalImportDirectoryPath
     * @date 2020/6/28 3:12 PM
     * @changedate 2020/7/25 10:19 AM
     * @author xxx
     */
    public void processImportDependency(File singleMethodInProductionCodeDirectory
            , File singleMethodInTestCodeDirectory
            , String singleMethodWithoutExternalImportDirectoryPath) {

        /*
        2020/8/8 12:38
        extended signatures in the production code
         */
        String md5ExtendedSignatureMapFilePathInProductionCode = singleMethodInProductionCodeDirectory.getAbsolutePath()
                + File.separator + this.getMd5ExtendedSignatureMapFileName();
        File md5ExtendedSignatureMapFileInProductionCode = new File(md5ExtendedSignatureMapFilePathInProductionCode);
        if (!md5ExtendedSignatureMapFileInProductionCode.exists()) {
            // no methods in the 'singleMethodInProductionCodeDirectory'
            md5ExtendedSignatureMapFileInProductionCode = null;
            md5ExtendedSignatureMapFilePathInProductionCode = null;
            return;
        }

        Set<String> allMethodExtendedSignatureSet = new HashSet<>();
        String md5ExtendedSignatureFileContentInProductionCode = FileUtil.readFileContentToString(md5ExtendedSignatureMapFileInProductionCode);
        Map<String, String> md5ExtendedSignatureInProductionCodeMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContentInProductionCode, Map.class);
        Collection<String> extendedSignatureInProductionCode = md5ExtendedSignatureInProductionCodeMap.values();
        allMethodExtendedSignatureSet.addAll(extendedSignatureInProductionCode);

        Set<String> packageNameSetInProject = new HashSet<>();
        Set<String> packageNameSet = this.packageDeclarationInAllProductionFilesMap.keySet();
        if (packageNameSet.size() > 0) {
            packageNameSetInProject.addAll(packageNameSet);
        }

        File[] productionMethodClassFileArray = singleMethodInProductionCodeDirectory.listFiles();
        File[] methodClassFileArray = productionMethodClassFileArray;
        Map<String, String> md5ExtendedSignatureMap = md5ExtendedSignatureInProductionCodeMap;

        Map<String, String> md5ExtendedSignatureInTestCodeMap;
        File[] testMethodClassFileArray;
        if (singleMethodInTestCodeDirectory != null) {

            String md5ExtendedSignatureMapFilePathInTestCode = singleMethodInTestCodeDirectory.getAbsolutePath()
                    + File.separator + this.getMd5ExtendedSignatureMapFileName();
            File md5ExtendedSignatureMapFileInTestCode = new File(md5ExtendedSignatureMapFilePathInTestCode);
            if (!md5ExtendedSignatureMapFileInTestCode.exists()) {
                md5ExtendedSignatureMapFileInTestCode = null;
                md5ExtendedSignatureMapFilePathInTestCode = null;
                return;
            }

            String md5ExtendedSignatureFileContentInTestCode = FileUtil.readFileContentToString(md5ExtendedSignatureMapFileInTestCode);
            md5ExtendedSignatureInTestCodeMap = (new JacksonUtil()).json2Bean(md5ExtendedSignatureFileContentInTestCode, Map.class);
            Collection<String> extendedSignatureInTestCode = md5ExtendedSignatureInTestCodeMap.values();
            allMethodExtendedSignatureSet.addAll(extendedSignatureInTestCode);
            extendedSignatureInTestCode = null;

            Set<String> packageNameInTestFileSet = this.getPackageDeclarationInAllTestFilesMap().keySet();
            if (packageNameInTestFileSet.size() > 0) {
                // 2020/8/7 12:18
                packageNameSetInProject.addAll(packageNameInTestFileSet);
            }

            testMethodClassFileArray = singleMethodInTestCodeDirectory.listFiles();
            methodClassFileArray = testMethodClassFileArray;
            md5ExtendedSignatureMap = md5ExtendedSignatureInTestCodeMap;
        }

        Map<String, String> allExtendedSignatureMD5Map = new HashMap<>();

        CompilationUnit methodClassCU;
        String targetFilePath;
        File targetFile;
        StringBuffer targetFilePathStringBuffer = new StringBuffer(singleMethodWithoutExternalImportDirectoryPath + File.separator);
        StringBuffer extendedSignatureStringBuffer = new StringBuffer();
        for (File methodClassFile : methodClassFileArray) {
            String fileName = methodClassFile.getName();
            if (!fileName.endsWith(".java")) {
                continue;
            }
//            if (!"xxx.java".equals(fileName)) {
//                continue;
//            }
//            System.out.println("----------------------" + fileName + "----------------------");

            methodClassCU = JavaParserUtil.constructCompilationUnit(null, methodClassFile, this.srcDirectory);
            if (methodClassCU == null) {
                /*
                Apr 89233128+jaxb-v2+jaxb-ri+xsom
                com.github.javaparser.ParseProblemException: (line 47,col 1) Modules are not supported.
                2020/8/11 07:53
                 */
                continue;
            }

            targetFilePathStringBuffer.append(fileName);
            int start = targetFilePathStringBuffer.indexOf(fileName);
            int end = targetFilePathStringBuffer.length();

            String md5String = fileName.replace(".java", "");
            String oldExtendedSignature = md5ExtendedSignatureMap.get(md5String);
            ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(oldExtendedSignature);
            String methodName = extendedSignatureComponentModel.getMethodName();

            targetFilePath = targetFilePathStringBuffer.toString();
            targetFile = new File(targetFilePath);
            List<ImportDeclaration> importDeclarationList = methodClassCU.findAll(ImportDeclaration.class);
            if (importDeclarationList.isEmpty()) {
                allExtendedSignatureMD5Map.put(md5String, oldExtendedSignature);
                FileUtil.copyFileUsingFileChannels(methodClassFile, targetFile);
                targetFilePathStringBuffer.replace(start, end, "");
                importDeclarationList = null;
                continue;
            }

            List<ImportDeclaration> thirdPartyImportList = new ArrayList<>();
            List<ImportDeclaration> externalImportList = new ArrayList<>();
            for (ImportDeclaration importDeclaration : importDeclarationList) {
                String importString = importDeclaration.getName().asString();
                if (isProjectPackageImport(importString, packageNameSetInProject)) {
                    externalImportList.add(importDeclaration);
                } else {
                    thirdPartyImportList.add(importDeclaration);
                }
            }
            if (externalImportList.isEmpty()) {
                allExtendedSignatureMD5Map.put(md5String, oldExtendedSignature);
                FileUtil.copyFileUsingFileChannels(methodClassFile, targetFile);
                targetFilePathStringBuffer.replace(start, end, "");
                importDeclarationList = null;
                continue;
            }
            importDeclarationList = null;

            String codeString = processExternalImportDependencies(methodClassCU
                    , thirdPartyImportList, externalImportList
                    , packageNameSetInProject, allMethodExtendedSignatureSet);
            CompilationUnit tempCU = JavaParserUtil.constructCompilationUnit(codeString, null, this.srcDirectory);
            if (tempCU == null) {
                /*
                2020/8/7 15:05
                 */
                targetFilePathStringBuffer.replace(start, end, "");
                codeString = null;
                continue;
            }
            List<CallableDeclaration> callableDeclarationList = tempCU.findAll(CallableDeclaration.class);
            CallableDeclaration tempCD = null;
            for (CallableDeclaration callableDeclaration : callableDeclarationList) {
                String callableDeclarationName = callableDeclaration.getNameAsString();
                if (!callableDeclarationName.equals(methodName)) {
                    continue;
                }
                tempCD = callableDeclaration;
                break;
            }
            if (tempCD == null) {
                /*
                2020/8/13 13:13
                 */
                callableDeclarationList = null;
                targetFilePathStringBuffer.replace(start, end, "");
                codeString = null;
                continue;
            }


            extendedSignatureStringBuffer.append(extendedSignatureComponentModel.getPackageName() + "+");
            extendedSignatureStringBuffer.append(extendedSignatureComponentModel.getClassOrInterfaceOrEnumName() + "+");
            String tempSignature = tempCD.getSignature().asString();
            extendedSignatureStringBuffer.append(tempSignature + "+");
            String returnType = "empty";
            if (tempCD.isMethodDeclaration()) {
                returnType = tempCD.asMethodDeclaration().getTypeAsString();
            }
            extendedSignatureStringBuffer.append(returnType);
            String newExtendedSignature = extendedSignatureStringBuffer.toString();
            String extendedSignatureMD5 = MD5Util.getMD5(newExtendedSignature);
            allExtendedSignatureMD5Map.put(extendedSignatureMD5, newExtendedSignature);
            targetFilePathStringBuffer.replace(start, end, "");
            String newFileName = extendedSignatureMD5 + ".java";
            targetFilePathStringBuffer.append(newFileName);
            targetFilePath = targetFilePathStringBuffer.toString();
            FileUtil.writeStringToTargetFile(codeString, targetFilePath);
            extendedSignatureStringBuffer.setLength(0);
            start = targetFilePathStringBuffer.indexOf(newFileName);
            end = targetFilePathStringBuffer.length();
            targetFilePathStringBuffer.replace(start, end, "");

            callableDeclarationList = null;
            tempCD = null;
            newExtendedSignature = null;
            extendedSignatureMD5 = null;
            tempCU = null;
            codeString = null;
        }

        extendedSignatureStringBuffer = null;
        targetFilePathStringBuffer = null;
        packageNameSet = null;
        extendedSignatureInProductionCode = null;

        if (!allExtendedSignatureMD5Map.isEmpty()) {
            String jsonString = (new JacksonUtil()).bean2Json(allExtendedSignatureMD5Map);
            String md5ExtendedSignatureMapFilePath = singleMethodWithoutExternalImportDirectoryPath
                    + File.separator
                    + this.getMd5ExtendedSignatureMapFileName();
            FileUtil.writeStringToTargetFile(jsonString, md5ExtendedSignatureMapFilePath);
        }

        allExtendedSignatureMD5Map = null;
        md5ExtendedSignatureInTestCodeMap = null;
        md5ExtendedSignatureInProductionCodeMap = null;
        targetFile = null;
        methodClassCU = null;
        packageNameSetInProject = null;
        allMethodExtendedSignatureSet = null;
        productionMethodClassFileArray = null;
        methodClassFileArray = null;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 3:53 PM
     * @author xxx
     */
    public static boolean isProjectPackageImport(String importString
            , Set<String> packageNameSet) {
        if (packageNameSet == null) {
            return false;
        }
        for (String packageName : packageNameSet) {
            if (!importString.contains(packageName)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/28 9:53 AM
     * @changedate 2020/7/24 10:16 PM Compatibility test code
     * @author xxx
     */
    public String processExternalImportDependencies(CompilationUnit methodClassCU
            , List<ImportDeclaration> thirdPartyImportList, List<ImportDeclaration> externalImportList
            , Set<String> packageNameSet, Set<String> extendedSignatureSet) {
        StringBuffer codeStringBuffer = new StringBuffer();
        if (methodClassCU.findFirst(PackageDeclaration.class).isPresent()) {
            PackageDeclaration packageDeclaration = methodClassCU.findFirst(PackageDeclaration.class).get();
            codeStringBuffer.append(packageDeclaration.toString());
        }
        for (ImportDeclaration thirdPartyImportDeclaration : thirdPartyImportList) {
            codeStringBuffer.append(thirdPartyImportDeclaration.toString());
        }
        codeStringBuffer.append(System.lineSeparator());

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        EnumDeclaration enumDeclaration = null;
        String classOrInterfaceOrEnumName;
        if (methodClassCU.findFirst(ClassOrInterfaceDeclaration.class).isPresent()) {
            classOrInterfaceDeclaration = methodClassCU.findFirst(ClassOrInterfaceDeclaration.class).get();
            classOrInterfaceOrEnumName = classOrInterfaceDeclaration.getNameAsString();
        } else {
            enumDeclaration = methodClassCU.findFirst(EnumDeclaration.class).get();
            classOrInterfaceOrEnumName = enumDeclaration.getNameAsString();
        }

        Map<String, List<String>> fileIdentifierMap = new HashMap<>();
        for (ImportDeclaration externalImportDeclaration : externalImportList) {
            String importString = externalImportDeclaration.toString();
            if (importString.indexOf("*") != -1) {
                // format: import xxx.xxx.xxx.*;
                continue;
            }
            String importNameString = externalImportDeclaration.getName().asString();
            String tempPackageName = checkPackageNameInImportString(importNameString, packageNameSet);
            if (tempPackageName == null) {
                continue;
            }
            String tempIdentifier = externalImportDeclaration.getName().getIdentifier();
            importNameString = importNameString.replace(tempPackageName, "").trim();

            if (tempIdentifier.equals("$")) {
                /*
                importNameString = "org.yinwang.pysonar.$"
                tempIdentifier = "$" ;
                2020/8/11 08:17
                 */
                tempIdentifier = "\\$";
            }

            /*
            importString = "import static cc.redberry.rings.ChineseRemainders.ChineseRemainders;"
            tempPackageName = "cc.redberry.rings";
            tempIdentifier = "ChineseRemainders";
            old :
            importNameString = importNameString.replace(tempIdentifier, "").trim();
            上述代码会将两个 ChineseRemainders 都替换掉，实际上需要保留第一个；
            2020/8/10 15:22
            修改代码如下；
             */
            Matcher matcher = Pattern.compile(tempIdentifier).matcher(importNameString);
            int start = 0;
            int end = 0;
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
            }
            StringBuffer tempStringBuffer = new StringBuffer(importNameString);
            importNameString = tempStringBuffer.replace(start, end, "").toString();
            tempStringBuffer = null;


            if (".".equals(importNameString)) {
                /*
                for example:
                importNameString = "import io.agroal.test.MockConnection;";
                tempPackageName = "io.agroal.test";
                tempIdentifier = "MockConnection";
                2020/8/5 14:16
                 */
                continue;
            }
            importNameString = importNameString.substring(1, importNameString.length() - 1);


            String tempClassName = importNameString;
            String key = tempPackageName + "+";
            int pointIndex = tempClassName.indexOf(".");
            if (pointIndex == -1) {
                key = key + tempClassName;
            } else {
                String baseClassName = tempClassName.substring(0, pointIndex).trim();
                key = key + baseClassName;
                String subClassName = tempClassName.substring(pointIndex + 1).trim();
                tempIdentifier = subClassName + "+" + tempIdentifier;
            }
            List<String> tempList = fileIdentifierMap.get(key);
            if (tempList == null) {
                tempList = new ArrayList();
            }
            tempList.add(tempIdentifier);
            fileIdentifierMap.put(key, tempList);
        }
        Iterator<Map.Entry<String, List<String>>> fileIdentifierMapIterator = fileIdentifierMap.entrySet().iterator();
        while (fileIdentifierMapIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = fileIdentifierMapIterator.next();
            String targetClassFileName = entry.getKey();
            List<String> identifierList = entry.getValue();
            String[] elementArray = targetClassFileName.split("\\+");
            String tempPackageName = elementArray[0];
            String basicClassName = elementArray[1];
            Iterator<String> identifierIterator = identifierList.iterator();
            while (identifierIterator.hasNext()) {
                String tempIdentifier = identifierIterator.next();
                String methodName = tempIdentifier;
                // check whether the import is a method import.
                String subClassName = null;
                if (tempIdentifier.indexOf("+") != -1) {
                    String[] tempNameArray = tempIdentifier.split("\\+");
                    subClassName = tempNameArray[0];
                    methodName = tempNameArray[1];
                }
                List<String> extendedSignatureList = fuzzySearchExtendedSignaturesByNamesAndParameterTypeNumber(tempPackageName
                        , basicClassName, subClassName, methodName, extendedSignatureSet);
                if (extendedSignatureList != null) {
                    List<MethodCallExpr> methodCallExprList = methodClassCU.findAll(MethodCallExpr.class);
                    for (String extendedSignature : extendedSignatureList) {
                        ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(extendedSignature);
                        if(!findSignatureInMethodCallExprList(extendedSignatureComponentModel, methodCallExprList)) {
                            continue;
                        }
                        MethodDeclaration methodDeclaration = new MethodDeclaration();
                        methodDeclaration.addMarkerAnnotation("TBooster_External_Method");
                        methodDeclaration.addMarkerAnnotation("PN_"
                                + extendedSignatureComponentModel.getPackageName());
                        methodDeclaration.addMarkerAnnotation("CN_"
                                + extendedSignatureComponentModel.getClassOrInterfaceOrEnumName());
                        methodDeclaration.setName(extendedSignatureComponentModel.getMethodName());
                        if (!"empty".equals(extendedSignatureComponentModel.getReturnType())) {
                            methodDeclaration.setType(extendedSignatureComponentModel.getReturnType());
                        }
                        List<String> parameterTypeList = extendedSignatureComponentModel.getParameterTypeList();
                        if (parameterTypeList != null) {
                            for (int i = 0; i < parameterTypeList.size(); i++) {
                                Parameter parameter = new Parameter();
                                String parameterType = parameterTypeList.get(i);
                                parameter.setType(parameterType);
                                parameter.setName("para" + i);
                                methodDeclaration.addParameter(parameter);
                            }
                        }
                        if (classOrInterfaceDeclaration == null) {
                            enumDeclaration.addMember(methodDeclaration);
                        } else {
                            classOrInterfaceDeclaration.addMember(methodDeclaration);
                        }
                    }
                    identifierIterator.remove();
                }
            }
            if (identifierList.isEmpty()) {
                fileIdentifierMapIterator.remove();
                continue;
            }

            // check whether the import is a field import.
            StringBuffer productionOrTestFilePathBuffer = new StringBuffer(this.srcDirectoryPath + File.separator);
            productionOrTestFilePathBuffer.append("main" + File.separator + "java" + File.separator);
            String packagePath = tempPackageName.replace(".", File.separator);
            productionOrTestFilePathBuffer.append(packagePath + File.separator);
            productionOrTestFilePathBuffer.append(basicClassName + ".java");
            String productionOrTestFilePath = productionOrTestFilePathBuffer.toString();
            File productionOrTestFile = new File(productionOrTestFilePath);
            if (!productionOrTestFile.exists()) {
                productionOrTestFilePath = productionOrTestFilePath.replace(File.separatorChar + "main" + File.separator
                        , File.separatorChar + "test" + File.separator);
                productionOrTestFile = new File(productionOrTestFilePath);
            }
            if (!productionOrTestFile.exists()) {
                /*
                肯能存在 import 的 package 不再当前项目中，
                github 一个 repository 可能包含多个 modules，
                我们把每个 modules 都看作一个项目，因此就会出现当导入其他 modules 时找不到文件路径。
                2020/8/7 12:58
                 */
                continue;
            }
            if (productionOrTestFile.isDirectory()) {
                /*
                2018 Jan 116403104+marmaray+marmaray
                productionOrTestFile 是一个文件夹名为 "FileWorkUnitCalculator.java"
                2020/8/12 11:07
                 */
                continue;
            }
            CompilationUnit tempCU = JavaParserUtil.constructCompilationUnit(null
                    , productionOrTestFile, this.srcDirectory);
            if (tempCU == null) {
                /*
                类中包含下面 private static 修饰的方法 JavaPaser 解析会报异常
                com.github.javaparser.ParseProblemException: (line 25,col 5) 'private' is not allowed here.
                    private static long toUnsigned(int value) {
                        return value & 0x1fffff;
                    }
                 */
                continue;
            }
            List<FieldDeclaration> fieldDeclarationList = tempCU.findAll(FieldDeclaration.class);
            List<FieldDeclaration> externalFieldDependencyList = new ArrayList<>();
            identifierIterator = identifierList.iterator();
            while (identifierIterator.hasNext()) {
                String tempIdentifier = identifierIterator.next();
                String subClassName = null;
                if (tempIdentifier.indexOf("+") != -1) {
                    String[] tempNameArray = tempIdentifier.split("\\+");
                    subClassName = tempNameArray[0];
                    tempIdentifier = tempNameArray[1];
                }
                FieldDeclaration fieldDeclaration = searchFieldDeclarationInFieldDeclarationList(tempIdentifier
                        , fieldDeclarationList);
                if (fieldDeclaration != null) {
                    fieldDeclaration.addMarkerAnnotation("TBooster_External_Field");
                    fieldDeclaration.addMarkerAnnotation("PN_" + tempPackageName);
                    if (subClassName == null) {
                        fieldDeclaration.addMarkerAnnotation("CN_" + basicClassName);
                    } else {
                        fieldDeclaration.addMarkerAnnotation("CN_" + basicClassName + "." + subClassName);
                    }
                    externalFieldDependencyList.add(fieldDeclaration);
                    identifierIterator.remove();
                }
            }
            if (!externalFieldDependencyList.isEmpty()) {
                for (FieldDeclaration fieldDeclaration : externalFieldDependencyList) {
                    if (classOrInterfaceDeclaration == null) {
                        enumDeclaration.addMember(fieldDeclaration);
                    } else {
                        classOrInterfaceDeclaration.addMember(fieldDeclaration);
                    }
                }
            }
            externalFieldDependencyList = null;
            fieldDeclarationList = null;
            tempCU = null;
            if (identifierList.isEmpty()) {
                fileIdentifierMapIterator.remove();
            }
        }

        // check whether the import is a type (class, interface and enum) import.
        fileIdentifierMapIterator = fileIdentifierMap.entrySet().iterator();
        String codeString;
        if (classOrInterfaceDeclaration == null) {
            codeString = enumDeclaration.toString();
        } else {
            codeString = classOrInterfaceDeclaration.toString();
        }
        while (fileIdentifierMapIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = fileIdentifierMapIterator.next();
            String fileName = entry.getKey();
            String[] elementArray = fileName.split("\\+");
            String tempPackageName = elementArray[0];
            String tempClassName = elementArray[1];
            StringBuffer productionOrTestFilePathBuffer = new StringBuffer(this.srcDirectoryPath + File.separator);
            productionOrTestFilePathBuffer.append("main" + File.separator + "java" + File.separator);
            String packagePath = tempPackageName.replace(".", File.separator);
            productionOrTestFilePathBuffer.append(packagePath + File.separator);
            productionOrTestFilePathBuffer.append(tempClassName + ".java");
            String productionOrTestFilePath = productionOrTestFilePathBuffer.toString();
            File productionOrTestFile = new File(productionOrTestFilePath);
            if (!productionOrTestFile.exists()) {
                productionOrTestFilePath = productionOrTestFilePath.replace(File.separatorChar + "main" + File.separator
                        , File.separatorChar + "test" + File.separator);
                productionOrTestFile = new File(productionOrTestFilePath);
            }
            if (!productionOrTestFile.exists()) {
                continue;
            }
            if (productionOrTestFile.isDirectory()) {
                continue;
            }
            CompilationUnit tempCU = JavaParserUtil.constructCompilationUnit(null, productionOrTestFile, this.srcDirectory);
            if (tempCU == null) {
                // 2020/8/13 00:05
                continue;
            }
            List<EnumDeclaration> tempEnumDeclarationList = tempCU.findAll(EnumDeclaration.class);
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = tempCU.findAll(ClassOrInterfaceDeclaration.class);
            List<String> identifierList = entry.getValue();
            Iterator<String> listIterator = identifierList.iterator();
            boolean found = false;
            while (listIterator.hasNext()) {
                String tempIdentifier = listIterator.next();
                String innerClassOrInterfaceOrEnumName = "";
                if (tempIdentifier.indexOf("+") != -1) {
                    String[] nameArray = tempIdentifier.split("\\+");
                    innerClassOrInterfaceOrEnumName = nameArray[0];
                    tempIdentifier = nameArray[1];
                }
                if (innerClassOrInterfaceOrEnumName.equals(classOrInterfaceOrEnumName)) {
                    // 同类中的导入 2020.07.09
                    listIterator.remove();
                    continue;
                }

                if (!tempEnumDeclarationList.isEmpty()) {
                    for (EnumDeclaration tempEnumDeclaration : tempEnumDeclarationList) {
                        String enumIdentifier = tempEnumDeclaration.getName().getIdentifier();
                        if (!enumIdentifier.equals(tempIdentifier)) {
                            continue;
                        }
                        found = true;
                        break;
                    }
                }
                if (found) {
                    codeString = reInstrumentDeclarationWithNewType(tempIdentifier, tempClassName, codeString);
                    found = false;
                    listIterator.remove();
                    continue;
                }
                if (!classOrInterfaceDeclarationList.isEmpty()) {
                    for (ClassOrInterfaceDeclaration tempClassOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                        String classOrInterfaceName = tempClassOrInterfaceDeclaration.getName().getIdentifier();
                        if (!classOrInterfaceName.equals(tempIdentifier)) {
                            continue;
                        }
                        found = true;
                        break;
                    }
                }
                if (found) {
                    codeString = reInstrumentDeclarationWithNewType(tempIdentifier, tempClassName, codeString);
                    listIterator.remove();
                    found = false;
                }
            }
            listIterator = identifierList.iterator();
            while (listIterator.hasNext()) {
                // format: import static com.squareup.picasso3.Picasso.LoadedFrom.DISK; DISK is a enum entry.
                String tempIdentifier = listIterator.next();
                String innerEnumName = null;
                if (tempIdentifier.indexOf("+") != -1) {
                    String[] nameArray = tempIdentifier.split("\\+");
                    innerEnumName = nameArray[0];
                    tempIdentifier = nameArray[1];
                }
                for (EnumDeclaration tempEnumDeclaration : tempEnumDeclarationList) {
                    String enumName = tempEnumDeclaration.getName().getIdentifier();
                    if (!enumName.equals(innerEnumName)) {
                        continue;
                    }
                    NodeList<EnumConstantDeclaration> enumConstantDeclarationNodeList = tempEnumDeclaration.getEntries();
                    for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarationNodeList) {
                        String entryName = enumConstantDeclaration.getName().getIdentifier();
                        if (!entryName.equals(tempIdentifier)) {
                            continue;
                        }
                        found = true;
                        break;
                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                    String parentIdentifier = tempClassName + "." + innerEnumName;
                    codeString = reInstrumentDeclarationWithNewType(tempIdentifier, parentIdentifier, codeString);
                    listIterator.remove();
                    found = false;
                }
            }
            tempEnumDeclarationList = null;
            classOrInterfaceDeclarationList = null;
            tempCU = null;
            if (identifierList.isEmpty()) {
                fileIdentifierMapIterator.remove();
            }
        }
        codeStringBuffer.append(codeString);
        return codeStringBuffer.toString();
    }

    /**
     * Check whether the package name involved in the import string belongs to the project itself.
     * If the package name involved in the import string doesn't belong to the project itself, the null will be returned.
     * Otherwise, return the package name.
     * @param importString the import string under checking.
     * @param packageNameSet a set of package names extracted from the project itself.
     * @return null or the string of the package name
     * @date 2020/6/26 3:30 PM
     * @author xxx
     */
    public String checkPackageNameInImportString(String importString, Set<String> packageNameSet) {
        for (String packageName : packageNameSet) {
            if (!packageName.equals(importString)) {
                continue;
            }
            return packageName;
        }
        String tempPackageName = null;
        int end = importString.lastIndexOf(".");
        while (end != -1) {
            importString = importString.substring(0, end);
            for (String packageName : packageNameSet) {
                if (!packageName.equals(importString)) {
                    continue;
                }
                tempPackageName = packageName;
                break;
            }
            if (tempPackageName != null) {
                break;
            }
            end = importString.lastIndexOf(".");
        }
        return  tempPackageName;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/26 4:09 PM
     * @author xxx
     */
    public List<String> fuzzySearchExtendedSignaturesByNamesAndParameterTypeNumber(String packageName, String className
            , String subClassName, String methodName , Set<String> extendedSignatureSet) {
        String fuzzSignature = packageName + "+" + className;
        if (subClassName != null) {
            fuzzSignature = fuzzSignature + "." + subClassName;
        }
        fuzzSignature = fuzzSignature + "+" + methodName;
        List<String> methodExtendedSignatureList = new ArrayList<>();
        for (String methodExtendedSignature : extendedSignatureSet) {
            if (methodExtendedSignature.contains(fuzzSignature)) {
                methodExtendedSignatureList.add(methodExtendedSignature);
            }
        }
        if (methodExtendedSignatureList.isEmpty()) {
            methodExtendedSignatureList = null;
        }
        return methodExtendedSignatureList;
    }


    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/27 2:27 PM
     * @author xxx
     */
    public boolean findSignatureInMethodCallExprList(ExtendedSignatureComponentModel extendedSignatureComponentModel
            , List<MethodCallExpr> methodCallExprList) {
        boolean found = false;
        String methodName = extendedSignatureComponentModel.getMethodName();
        List<String> parameterTypeList = extendedSignatureComponentModel.getParameterTypeList();
        int parameterNumber = parameterTypeList.size();
        for (MethodCallExpr methodCallExpr : methodCallExprList) {
            String tempMethodName = methodCallExpr.getNameAsString();
            if (!tempMethodName.equals(methodName)) {
                continue;
            }
            NodeList<Expression> argumentNodeList = methodCallExpr.getArguments();
            if (argumentNodeList.size() != parameterNumber) {
                continue;
            }
            found = true;
            break;
        }
        return found;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/27 4:16 PM
     * @chagedate 2020/7/25 3:33 PM
     * @author xxx
     */
    public String reInstrumentDeclarationWithNewType(String currentIdentifier
            , String parentIdentifier, String codeString) {
        String newType = parentIdentifier + "." + currentIdentifier;
        StringBuffer stringBuffer = new StringBuffer();
        String[] lineArray = codeString.split(System.lineSeparator());
        StringBuffer lineBuffer = new StringBuffer();
        for (String line : lineArray) {
            if (!line.contains(currentIdentifier)) {
                stringBuffer.append(line + System.lineSeparator());
                continue;
            }
            if (line.trim().startsWith("@CN_")) {
                /*
                for example: @CN_Dispatcher.Dispatcher.NetworkBroadcastReceiver
                currentIdentifier = NetworkBroadcastReceiver
                parentIdentifier = Dispatcher
                line = @CN_Dispatcher.NetworkBroadcastReceiver
                 */
                stringBuffer.append(line + System.lineSeparator());
                continue;
            }
            int stringLength = line.length();
            Matcher matcher = Pattern.compile(currentIdentifier).matcher(line);
            Map<Integer, Integer> startEndMap = new TreeMap<>();
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (start > 0) {
                    char beforeChar = line.charAt(start - 1);
                    if (isCharConformToJavaNamingRules(beforeChar)) {
                        continue;
                    }
                }
                if (end < stringLength) {
                    char afterChar = line.charAt(end);
                    if (isCharConformToJavaNamingRules(afterChar)) {
                        continue;
                    }
                }
                startEndMap.put(start, end);
            }
            if (startEndMap.size() == 0) {
                stringBuffer.append(line + System.lineSeparator());
                continue;
            }
            Iterator<Map.Entry<Integer, Integer>> iterator = startEndMap.entrySet().iterator();
            int lastEnd = -1;
            String subString;
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                int start = entry.getKey();
                if (lastEnd == -1) {
                    subString = line.substring(0, start);
                } else {
                    subString = line.substring(lastEnd, start);
                }
                lineBuffer.append(subString);
                lineBuffer.append(newType);
                int end = entry.getValue();
                lastEnd = end;
            }
            subString = line.substring(lastEnd);
            lineBuffer.append(subString);
            String tempString = lineBuffer.toString();
            stringBuffer.append(tempString + System.lineSeparator());
            lineBuffer.setLength(0);
        }
        return stringBuffer.toString();
    }

    /**
      *
      * @param className
      * @param methodName
      * @param productionMethodExtendedSignatureSet
      * @param testMethodExtendedSignatureSet
      * @return List<String>
      * @date 2020/6/18 8:5610:32 PM
      * @changedate 2020/7/27 4:41 PM
      * @author xxx
      */
    public List<String> searchExtendedSignaturesByClassAndMethodName(String className, String methodName
            , Set<String> productionMethodExtendedSignatureSet
            , Set<String> testMethodExtendedSignatureSet) {
        String fuzzSignature = className + "+" + methodName + "(";
        List<String> methodExtendedSignatureList = new ArrayList<>();

        if (testMethodExtendedSignatureSet != null) {
            /*
            search in test method signatures.
            2020/7/27 4:41 PM
             */
            List<String> matchingTestMethodExtendedSignatureList = searchFuzzSignatureInMethodExtendedSignatureSet(fuzzSignature
                    , testMethodExtendedSignatureSet);
            if (matchingTestMethodExtendedSignatureList == null) {
                int angleIndex = className.indexOf("<");
                if (angleIndex != -1){
                    String tempClassName = className.substring(0, angleIndex);
                    String tempFuzzSignature = tempClassName + "+" + methodName + "(";
                    matchingTestMethodExtendedSignatureList = searchFuzzSignatureInMethodExtendedSignatureSet(tempFuzzSignature
                            , testMethodExtendedSignatureSet);
                }
            }
            if (matchingTestMethodExtendedSignatureList != null) {
                methodExtendedSignatureList.addAll(matchingTestMethodExtendedSignatureList);
            }
        }
        // search in production method signatures.
        List<String> matchingProductionMethodExtendedSignatureList = searchFuzzSignatureInMethodExtendedSignatureSet(fuzzSignature
                , productionMethodExtendedSignatureSet);
        if (matchingProductionMethodExtendedSignatureList == null){
            /*
            2020/8/6 16:10
            for exampel:
            className = "Deque<Integer>";
            ExtendedSignature = "two+Deque+iterator()+Iterator<Item>";
             */
            int angleIndex = className.indexOf("<");
            if (angleIndex != -1){
                String tempClassName = className.substring(0, angleIndex);
                String tempFuzzSignature = tempClassName + "+" + methodName + "(";
                matchingProductionMethodExtendedSignatureList = searchFuzzSignatureInMethodExtendedSignatureSet(tempFuzzSignature
                        , productionMethodExtendedSignatureSet);
            }
        }

        if (matchingProductionMethodExtendedSignatureList != null) {
            methodExtendedSignatureList.addAll(matchingProductionMethodExtendedSignatureList);
        }
        return methodExtendedSignatureList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 8:56 PM
     * @chagedate 2020/7/27 4:41 PM
     * @author xxx
     */
    public List<String> fuzzySearchExtendedSignaturesByNamesAndParameterTypeNumber(String className
            , String methodName, int parameterNumber
            , Set<String> productionMethodExtendedSignatureSet
            , Set<String> testMethodExtendedSignatureSet) {
        List<String> methodExtendedSignatureList = searchExtendedSignaturesByClassAndMethodName(className
                , methodName, productionMethodExtendedSignatureSet, testMethodExtendedSignatureSet);
        Iterator<String> iterator = methodExtendedSignatureList.iterator();
        while (iterator.hasNext()) {
            String extendedSignature = iterator.next();
            String[] elementArray = extendedSignature.split("\\+");
            String signature = elementArray[2];
            int start = signature.indexOf("(");
            int end = signature.lastIndexOf(")");
            String parameterTypeString = signature.substring(start + 1, end);
            List<String> parameterTypeList = extractParameterTypeFromParameterTypeString(parameterTypeString);
            if (parameterTypeList.size() != parameterNumber) {
                iterator.remove();
            }
        }
        if (methodExtendedSignatureList.isEmpty()) {
            methodExtendedSignatureList = null;
        }
        return methodExtendedSignatureList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/27 4:44 PM
      * @author xxx
      */
    private List<String> searchFuzzSignatureInMethodExtendedSignatureSet(String fuzzSignature
            , Set<String> methodExtendedSignatureSet) {
        List<String> matchingExtendedSignatureList = new ArrayList<>();
        for (String methodExtendedSignature : methodExtendedSignatureSet) {
            int start = methodExtendedSignature.indexOf(fuzzSignature);
            if (start == -1) {
                continue;
            }
            char charBeforeFuzzSignature = methodExtendedSignature.charAt(start - 1);
            if (charBeforeFuzzSignature != '+' && charBeforeFuzzSignature != '.') {
                /*
                当 className != null 时，可能出现 className 是子类当情况，前面有个 . 符号；
                com.squareup.picasso3+Callback.EmptyCallback+onError(Throwable)+void
                com.squareup.picasso3+Callback+onError(Throwable)+void
                com.squareup.picasso3+RequestHandler.Callback+onError(Throwable)+void
                 */
                continue;
            }
            matchingExtendedSignatureList.add(methodExtendedSignature);
        }
        if (matchingExtendedSignatureList.isEmpty()) {
            matchingExtendedSignatureList = null;
        }
        return matchingExtendedSignatureList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/27 12:10 AM
     * @author xxx
     */
    public FieldDeclaration searchFieldDeclarationInFieldDeclarationList(String targetFieldIdentifier
            , List<FieldDeclaration> fieldDeclarationList) {
        FieldDeclaration targetFieldDeclaration = null;
        boolean hasFound = false;
        for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
            NodeList<VariableDeclarator> variableDeclaratorNodeList = fieldDeclaration.getVariables();
            for (VariableDeclarator variableDeclarator : variableDeclaratorNodeList) {
                String identifier = variableDeclarator.getName().getIdentifier();
                if (!identifier.equals(targetFieldIdentifier)) {
                    continue;
                }
                hasFound = true;
                break;
            }
            if (hasFound) {
                targetFieldDeclaration = fieldDeclaration;
                break;
            }
        }
        return targetFieldDeclaration;
    }


    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/27 11:12 AM
     * @author xxx
     */
    public ExtendedSignatureComponentModel parseExtendedSignature(String extendedSignature) {
        String[] nameArray = extendedSignature.split("\\+");
        String packageName = nameArray[0];
        String classOrInterfaceOrEnumName = nameArray[1];
        String innerClassOrInterfaceOrEnumName = null;
        int pointIndex = classOrInterfaceOrEnumName.indexOf(".");
        if (pointIndex != -1) {
            innerClassOrInterfaceOrEnumName = classOrInterfaceOrEnumName.substring(pointIndex + 1);
        }
        String signature = nameArray[2];
        String returnType = nameArray[3];
        int firstLeftParenthesisIndex = signature.indexOf("(");
        int lastRightParenthesisIndex = signature.lastIndexOf(")");
        String methodName = signature.substring(0, firstLeftParenthesisIndex);
        String parameterTypeString = signature.substring(firstLeftParenthesisIndex + 1, lastRightParenthesisIndex);
        List<String> parameterTypeList = extractParameterTypeFromParameterTypeString(parameterTypeString);
        return new ExtendedSignatureComponentModel(extendedSignature, packageName, classOrInterfaceOrEnumName
                , innerClassOrInterfaceOrEnumName, returnType, methodName, parameterTypeList);
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 9:27 PM
     * @author xxx
     */
    public List<String> extractParameterTypeFromParameterTypeString(String parameterTypeString) {
        List<String> parameterTypeList = new ArrayList<>();
        int commaIndex = parameterTypeString.indexOf(",");
        if (commaIndex == -1) {
            if (!"".equals(parameterTypeString)) {
                parameterTypeList.add(parameterTypeString);
            }
            return parameterTypeList;
        }
        int leftAngle = parameterTypeString.indexOf("<");
        if (leftAngle == -1) {
            String[] parameterTypeArray = parameterTypeString.split(",");
            for (String parameterType : parameterTypeArray) {
                parameterTypeList.add(parameterType.trim());
            }
        } else {
            StringBuffer tempStringBuffer = new StringBuffer();
            while (commaIndex != -1) {
                tempStringBuffer.append(parameterTypeString.substring(0, commaIndex));
                String tempString = tempStringBuffer.toString();
                if (tempString.indexOf("<") == -1) {
                    parameterTypeList.add(tempString);
                    tempStringBuffer.setLength(0);
                    parameterTypeString = parameterTypeString.substring(commaIndex + 1).trim();
                    commaIndex = parameterTypeString.indexOf(",");
                    continue;
                }
                if (checkAngleMatchingInString(tempString)) {
                    parameterTypeList.add(tempString);
                    tempStringBuffer.setLength(0);
                    parameterTypeString = parameterTypeString.substring(commaIndex + 1).trim();
                    commaIndex = parameterTypeString.indexOf(",");
                    continue;
                }
                tempStringBuffer.append(",");
                parameterTypeString = parameterTypeString.substring(commaIndex + 1);
                commaIndex = parameterTypeString.indexOf(",");
            }
            if (tempStringBuffer.length() != 0) {
                tempStringBuffer.append(parameterTypeString);
                parameterTypeList.add(tempStringBuffer.toString());
                tempStringBuffer = null;
            } else {
                parameterTypeList.add(parameterTypeString);
            }
        }
        return parameterTypeList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 9:39 PM
     * @author xxx
     */
    private static boolean checkAngleMatchingInString(String string) {
        boolean isMatching = false;
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            char tempChar = string.charAt(i);
            if (tempChar == '<') {
                count++;
            }
            if (tempChar == '>') {
                count--;
            }
        }
        if (count == 0) {
            isMatching = true;
        }
        return isMatching;
    }


    /**
      *
      * @param methodCallModel
      * @param externalMethodCallDependencyList
      * @return boolean
      * @throws
      * @date 2020/7/27 8:44 PM
      * @author xxx
      */
    private boolean searchMethodCallInExternalMethodDependencyList(MethodCallModel methodCallModel
            , List<CallableDeclaration> externalMethodCallDependencyList) {
        boolean hasFound = false;
        String className = null;
        ScopeModel scopeModel = methodCallModel.getScopeModel();
        if (scopeModel != null && scopeModel.isResolved()) {
            className = scopeModel.getScopeType();
        }
        String methodName = methodCallModel.getMethodName();
        int argumentNumber = 0;
        List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();
        if (argumentModelList != null) {
            argumentNumber = argumentModelList.size();
        }
        List<CallableDeclaration> callableDeclarationWithSameArgumentNumberList = new ArrayList<>();
        for (CallableDeclaration callableDeclaration : externalMethodCallDependencyList) {
            String callableDeclarationName = callableDeclaration.getNameAsString();
            if (!callableDeclarationName.equals(methodName)) {
                continue;
            }
            NodeList<Parameter> parameterNodeList = callableDeclaration.getParameters();
            if (parameterNodeList.size() != argumentNumber) {
                continue;
            }
            callableDeclarationWithSameArgumentNumberList.add(callableDeclaration);
        }
        int callableDeclarationWithSameArgumentNumberListSize = callableDeclarationWithSameArgumentNumberList.size();
        if (callableDeclarationWithSameArgumentNumberListSize == 0) {
            return false;
        }
        if (callableDeclarationWithSameArgumentNumberListSize == 1) {
            CallableDeclaration callableDeclaration = callableDeclarationWithSameArgumentNumberList.get(0);
            if (callableDeclaration.isConstructorDeclaration()) {
                methodCallModel.setConstructorCall(true);
            }
            String extendedSignature = produceExtendedSignatureForExternalCallableDeclaration(callableDeclaration);
            setMethodCallModelWithExtendedSignature(methodCallModel, extendedSignature);
            return true;
        }

        if (callableDeclarationWithSameArgumentNumberListSize > 1) {
            if (className != null) {
                boolean classNameNotMatching = false;
                Iterator<CallableDeclaration> iterator = callableDeclarationWithSameArgumentNumberList.iterator();
                while (iterator.hasNext()) {
                    CallableDeclaration callableDeclaration = iterator.next();
                    NodeList<AnnotationExpr> annotationExprNodeList = callableDeclaration.getAnnotations();
                    for (AnnotationExpr annotationExpr : annotationExprNodeList) {
                        String annotationName = annotationExpr.getNameAsString();
                        if (!annotationName.startsWith("CN")) {
                            continue;
                        }
                        String classNameOfCallableDeclaration = annotationName.replace("CN_", "");
                        if (!classNameOfCallableDeclaration.equals(className)) {
                            classNameNotMatching = true;
                            break;
                        }
                    }
                    if (classNameNotMatching) {
                        iterator.remove();
                        classNameNotMatching = false;
                    }
                }
            }
        }
        if (callableDeclarationWithSameArgumentNumberListSize == 1) {
            CallableDeclaration callableDeclaration = callableDeclarationWithSameArgumentNumberList.get(0);
            if (callableDeclaration.isConstructorDeclaration()) {
                methodCallModel.setConstructorCall(true);
            }
            String extendedSignature = produceExtendedSignatureForExternalCallableDeclaration(callableDeclaration);
            setMethodCallModelWithExtendedSignature(methodCallModel, extendedSignature);
            return true;
        }

        if (callableDeclarationWithSameArgumentNumberListSize > 1) {
            if (argumentModelList == null) {
                return false;
            }
            String argumentTypeString = extractArgumentTypeStringFromArgumentModelList(argumentModelList);
            if (argumentTypeString == null) {
                return false;
            }
            String[] argumentTypeArray = argumentTypeString.split(",");
            boolean notMatchingType = false;
            for (CallableDeclaration callableDeclaration : callableDeclarationWithSameArgumentNumberList) {
                NodeList<Parameter> parameterNodeList = callableDeclaration.getParameters();
                for (int i = 0; i < parameterNodeList.size(); i++) {
                    Parameter parameter = parameterNodeList.get(i);
                    String parameterType = parameter.getTypeAsString();
                    String argumentType = argumentTypeArray[i].trim();
                    if (argumentType.equals(parameterType)) {
                        continue;
                    }
                    if (checkCommonTypeCompatibility(argumentType, parameterType)) {
                        continue;
                    }
                    notMatchingType = true;
                    break;
                }
                if (notMatchingType) {
                    notMatchingType = false;
                    continue;
                }
                String extendedSignature = produceExtendedSignatureForExternalCallableDeclaration(callableDeclaration);
                methodCallModel.setExtendedSignature(extendedSignature);
                if (callableDeclaration.isConstructorDeclaration()) {
                    methodCallModel.setConstructorCall(true);
                }
                methodCallModel.setReturnType(extractReturnTypeStringFromExtendedSignature(extendedSignature));
                methodCallModel.setResolved(true);
                methodCallModel.setProductionMethodCall(true);
                hasFound = true;
                break;
            }
        }
        return hasFound;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/26 4:03 PM
     * @author xxx
     */
    private String produceExtendedSignatureForExternalCallableDeclaration(CallableDeclaration callableDeclaration) {
        List<AnnotationExpr> annotationExprList = callableDeclaration.getAnnotations();
        StringBuffer extendedSignatureBuffer = new StringBuffer();
        String packageName = "src";
        String className = "";
        for (AnnotationExpr annotationExpr: annotationExprList) {
            String annotationName = annotationExpr.getName().asString();
            if (annotationName.startsWith("PN_")) {
                annotationName = annotationName.replace("PN_", "");
                packageName = annotationName;
            }
            if (annotationName.startsWith("CN_")) {
                annotationName = annotationName.replace("CN_", "");
                className =  annotationName;
            }
        }
        extendedSignatureBuffer.append(packageName + "+" + className + "+");
        String callableDeclarationSignature = callableDeclaration.getSignature().asString();
        extendedSignatureBuffer.append(callableDeclarationSignature + "+");
        String returnType = "";
        if (callableDeclaration.isConstructorDeclaration()) {
            returnType = "empty";
        }
        if (callableDeclaration.isMethodDeclaration()) {
            returnType = callableDeclaration.asMethodDeclaration().getTypeAsString();
        }
        extendedSignatureBuffer.append(returnType);
        return extendedSignatureBuffer.toString();
    }



    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/18 3:34 PM
     * @chagedate 2020/7/27 4:36 PM
     * @author xxx
     */
    public List<MethodCallModel> analyzeMethodDependency(List<? extends Expression> methodCallExprList
            , String classOrInterfaceOrEnumName
            , List<String> extendClassNameList
            , Map<String, String> localVariableNameTypeMap
            , Map<String, String> parameterNameTypeMap
            , Map<String, String> globalVariableNameTypedMap
            , Set<String> productionMethodSignatureSetInProject
            , Set<String> testMethodSignatureSetInProject
            , List<CallableDeclaration> externalMethodCallDependencyList) {
        int productionOrTestFlag = 0;
        if (testMethodSignatureSetInProject != null) {
            productionOrTestFlag = 1;
        }

        // preprocess the scope and arguments for the method call.
        List<MethodCallModel> methodCallModelList = processScopeAndArgument(methodCallExprList
                , localVariableNameTypeMap, parameterNameTypeMap, globalVariableNameTypedMap, productionOrTestFlag);

        List<MethodCallModel> processedMethodCallModelList = new ArrayList<>();
        List<MethodCallModel> resolvedMethodCallModelList = new ArrayList<>();
        Iterator<MethodCallModel> iterator = methodCallModelList.iterator();
        boolean hasFound = false;
        while (iterator.hasNext()) {
            MethodCallModel methodCallModel = iterator.next();
            if (externalMethodCallDependencyList.size() > 0) {
                hasFound = searchMethodCallInExternalMethodDependencyList(methodCallModel
                        , externalMethodCallDependencyList);
            }
            if (hasFound) {
                resolvedMethodCallModelList.add(methodCallModel);
                iterator.remove();
                hasFound = false;
                continue;
            }

            String className = null;
            ScopeModel scopeModel = methodCallModel.getScopeModel();
            if (scopeModel == null) {
                /*
                没有的 scope 的函数调用存在三种情况：
                1. 同一类中的函数调用，此时 className = classOrInterfaceOrEnumName；
                2. import 其他类中的函数，这种情况在 externalMethodCallDependencyList 中已经解决；
                3. import 第三方库中的函数，这种情况很少见。
                 */
                Expression methodCallExpression = methodCallModel.getMethodCallExpression();
                if (methodCallExpression.isObjectCreationExpr()) {
                    className = methodCallExpression.asObjectCreationExpr().getTypeAsString();
                } else {
                    className = classOrInterfaceOrEnumName;
                }
            } else {
                if (scopeModel.isResolved()) {
                    className = scopeModel.getScopeType();
                }
            }
            if (className == null) {
                /*
                当 className == null 时，容易出现 methodName 可能来源于第三方库的可能；
                for example: the method 'equals(Object)' that may from many third-party libraries.
                 */
                continue;
            }
            String methodName = methodCallModel.getMethodName();
            // Step1: 通过类名+方法名搜索匹配的签名
            List<String> extendedSignatureWithSameClassAndMethodNameList = searchExtendedSignaturesByClassAndMethodName(className
                    , methodName, productionMethodSignatureSetInProject, testMethodSignatureSetInProject);
            if (extendedSignatureWithSameClassAndMethodNameList == null) {
                // 说明项目内不存在方法具有相同的 className+methodName
                methodCallModel.setProductionMethodCall(false);
                continue;
            }
            if (extendedSignatureWithSameClassAndMethodNameList.size() == 1) {
                // 说明项目内仅存在一个方法具有相同的 className+methodName
                String extendedSignature = extendedSignatureWithSameClassAndMethodNameList.get(0);
                setMethodCallModelWithExtendedSignature(methodCallModel, extendedSignature);
                resolvedMethodCallModelList.add(methodCallModel);
                iterator.remove();
                continue;
            }
            /*
            说明项目内存在多个方法具有相同的 className+methodName，可能出现如下两种情况：
            for example, className = 'Callback'; methodName = 'onError'
            case 1: Callback+onError 重复且参数相同
                com.squareup.picasso3+Callback+onError(Throwable)+void
                com.squareup.picasso3+RequestHandler.Callback+onError(Throwable)+void
            case 2: Callback+onError 重复且参数不同，多态
            对于 case 1, 通过判断 Callback 出现的所在类中是否有继承 RequestHandler 来解决
            对于 case 2, 通过分析参数的数量、类型、顺序搜索匹配的签名来解决
             */
            // step2: 通过分析类的继承来解决 case 1
            processExtendedWithSameClassNameAndSignatureByExtendClass(extendedSignatureWithSameClassAndMethodNameList
                    , extendClassNameList);

            // Step3: 通过分析参数的数量、类型、顺序搜索匹配的签名来解决 case 2
            List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();
            List<String> extendedSignatureWithMatchingParameterList = searchExtendedSignaturesByParameterComparison(argumentModelList
                    , extendedSignatureWithSameClassAndMethodNameList);
            int extendedSignatureWithMatchingParameterListSize = extendedSignatureWithMatchingParameterList.size();
            if (extendedSignatureWithMatchingParameterListSize == 0) {
                continue;
            }
            if (extendedSignatureWithMatchingParameterListSize > 1) {
                // searchExtendedSignaturesByParameterComparison() 中有解释
                continue;
            }
            String extendedSignature = extendedSignatureWithMatchingParameterList.get(0);
            setMethodCallModelWithExtendedSignature(methodCallModel, extendedSignature);
            resolvedMethodCallModelList.add(methodCallModel);
            iterator.remove();
        }


        // process all unresolved method calls by leveraging resolved method calls.
        processUnresolvedMethodCallsWithResolvedMethodCalls(methodCallModelList
                , resolvedMethodCallModelList
                , classOrInterfaceOrEnumName
                , externalMethodCallDependencyList
                , productionMethodSignatureSetInProject
                , testMethodSignatureSetInProject);

        processedMethodCallModelList.addAll(resolvedMethodCallModelList);

        if (methodCallModelList.size() > 0) {
            StringBuffer fuzzSignatureBuffer = new StringBuffer();
            for (MethodCallModel methodCallModel : methodCallModelList) {
                ScopeModel scopeModel = methodCallModel.getScopeModel();
                if (scopeModel != null && scopeModel.isResolved()) {
                    String scopeType = scopeModel.getScopeType();
                    fuzzSignatureBuffer.append(scopeType + "+");
                }
                String methodName = methodCallModel.getMethodName();
                fuzzSignatureBuffer.append(methodName + "(");
                List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();
                int argumentNumber = 0;
                if (argumentModelList == null) {
                    fuzzSignatureBuffer.append(")");
                    methodCallModel.setExtendedSignature(fuzzSignatureBuffer.toString());
                    fuzzSignatureBuffer.setLength(0);
                    continue;
                }
                argumentNumber = argumentModelList.size();
                int index = 0;
                for (ArgumentModel argumentModel : argumentModelList) {
                    if (!argumentModel.isResolved()) {
                        String argumentExpression = argumentModel.getArgumentExpression().toString();
                        if (index < argumentNumber - 1) {
                            fuzzSignatureBuffer.append(argumentExpression + ", ");
                        } else {
                            fuzzSignatureBuffer.append(argumentExpression);
                        }
                        index++;
                        continue;
                    }
                    String argumentType = argumentModel.getArgumentType();
                    if (index < argumentNumber - 1) {
                        fuzzSignatureBuffer.append(argumentType + ", ");
                    } else {
                        fuzzSignatureBuffer.append(argumentType);
                    }
                    index++;
                }
                fuzzSignatureBuffer.append(")");
                methodCallModel.setExtendedSignature(fuzzSignatureBuffer.toString());
                fuzzSignatureBuffer.setLength(0);
            }
        }
        processedMethodCallModelList.addAll(methodCallModelList);
        return processedMethodCallModelList;
    }


    /**
      *
      * @param extendedSignatureWithSameClassAndMethodNameList
      * @param extendClassNameList
      * @return void
      * @throws
      * @date 2020/8/1 11:02 AM
      * @author xxx
      */
    private void processExtendedWithSameClassNameAndSignatureByExtendClass(List<String> extendedSignatureWithSameClassAndMethodNameList
            , List<String> extendClassNameList) {
        Map<String, List<String>> withSameMethodSignatureMap = new HashMap<>();
        for (String extendedSignatureWithSameClassAndMethodName : extendedSignatureWithSameClassAndMethodNameList) {
            String methodSignature = extendedSignatureWithSameClassAndMethodName.split("\\+")[2];
            List<String> extendedSignatureList = withSameMethodSignatureMap.get(methodSignature);
            if (extendedSignatureList == null) {
                extendedSignatureList = new ArrayList<>();
                extendedSignatureList.add(extendedSignatureWithSameClassAndMethodName);
                withSameMethodSignatureMap.put(methodSignature, extendedSignatureList);
            } else {
                extendedSignatureList.add(extendedSignatureWithSameClassAndMethodName);
            }
        }
        extendedSignatureWithSameClassAndMethodNameList.clear();
        Iterator<Map.Entry<String, List<String>>> entryIterator = withSameMethodSignatureMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = entryIterator.next();
            List<String> extendedSignatureList = entry.getValue();
            if (extendedSignatureList.size() == 1) {
                extendedSignatureWithSameClassAndMethodNameList.add(extendedSignatureList.get(0));
                continue;
            }
            Iterator<String> extendedSignatureListIterator = extendedSignatureList.iterator();
            while (extendedSignatureListIterator.hasNext()) {
                String extendedSignature = extendedSignatureListIterator.next();
                String tempClassOrInterfaceOrEnumName = extendedSignature.split("\\+")[1];
                int pointIndex = tempClassOrInterfaceOrEnumName.indexOf(".");
                if (extendClassNameList == null && pointIndex != -1) {
                    // 无继承-有父类的情况
                    extendedSignatureListIterator.remove();
                    continue;
                }
                if (extendClassNameList != null && pointIndex == -1) {
                    // 有继承-无父类的情况
                    extendedSignatureListIterator.remove();
                    continue;
                }
                if (extendClassNameList == null && pointIndex == -1) {
                    // 无继承-无父类
                    extendedSignatureWithSameClassAndMethodNameList.add(extendedSignature);
                    continue;
                }
                if (extendClassNameList != null && pointIndex != -1) {
                    // 有继承-有父类
                    String parentClassName = tempClassOrInterfaceOrEnumName.substring(0, pointIndex);
                    if (!extendClassNameList.contains(parentClassName)) {
                        extendedSignatureListIterator.remove();
                        continue;
                    }
                    extendedSignatureWithSameClassAndMethodNameList.add(extendedSignature);
                }
            }
        }
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/29 11:08 AM
      * @author xxx
      */
    private void processUnresolvedMethodCallsWithResolvedMethodCalls(List<MethodCallModel> methodCallModelList
            , List<MethodCallModel> resolvedMethodCallModelList
            , String classOrInterfaceOrEnumName
            , List<CallableDeclaration> externalMethodCallDependencyList
            , Set<String> productionMethodSignatureSetInProject
            , Set<String> testMethodSignatureSetInProject) {
        int unresolvedMethodCallNumber = methodCallModelList.size();
        if (unresolvedMethodCallNumber == 0) {
            return;
        }
        Iterator<MethodCallModel> methodCallModelIterator = methodCallModelList.iterator();
        while(methodCallModelIterator.hasNext()) {
            MethodCallModel methodCallModel = methodCallModelIterator.next();

            // 用已解析的方法调用去处理未解析的方法调用中的 scope 和 arguments.
            processUnresolvedScopeAndArgumentInMethodCall(methodCallModel, resolvedMethodCallModelList);

            String className;
            ScopeModel scopeModel = methodCallModel.getScopeModel();
            if (scopeModel == null) {
                Expression methodCallExpression = methodCallModel.getMethodCallExpression();
                if (methodCallExpression.isObjectCreationExpr()) {
                    className = methodCallExpression.asObjectCreationExpr().getTypeAsString();
                } else {
                    className = classOrInterfaceOrEnumName;
                }
            } else {
                if (!scopeModel.isResolved()) {
                    continue;
                }
                String scopeType = scopeModel.getScopeType();
                className = scopeType;
            }
            String methodName = methodCallModel.getMethodName();
            List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();

            boolean hasFound = false;
            if (externalMethodCallDependencyList.size() > 0) {
                hasFound = searchMethodCallInExternalMethodDependencyList(methodCallModel
                        , externalMethodCallDependencyList);
            }
            if (hasFound) {
                resolvedMethodCallModelList.add(methodCallModel);
                methodCallModelIterator.remove();
                continue;
            }

            List<String> extendedSignatureWithSameClassAndMethodNameList = searchExtendedSignaturesByClassAndMethodName(className
                    , methodName, productionMethodSignatureSetInProject, testMethodSignatureSetInProject);
            List<String> extendedSignatureWithMatchingParameterList = searchExtendedSignaturesByParameterComparison(argumentModelList
                    , extendedSignatureWithSameClassAndMethodNameList);
            int extendedSignatureWithMatchingParameterListSize = extendedSignatureWithMatchingParameterList.size();
            if (extendedSignatureWithMatchingParameterListSize == 0) {
                continue;
            }
            if (extendedSignatureWithMatchingParameterListSize > 1) {
                // searchExtendedSignaturesByParameterComparison() 中有解释
                continue;
            }
            String extendedSignature = extendedSignatureWithMatchingParameterList.get(0);
            setMethodCallModelWithExtendedSignature(methodCallModel, extendedSignature);
            resolvedMethodCallModelList.add(methodCallModel);
            methodCallModelIterator.remove();
        }

        int newUnresolvedMethodCallNumber = methodCallModelList.size();
        if (newUnresolvedMethodCallNumber == unresolvedMethodCallNumber) {
            return;
        }
        processUnresolvedMethodCallsWithResolvedMethodCalls(methodCallModelList
                , resolvedMethodCallModelList
                , classOrInterfaceOrEnumName
                , externalMethodCallDependencyList
                , productionMethodSignatureSetInProject
                , testMethodSignatureSetInProject);
    }

    /**
      *
      * @param methodCallModel
      * @param resolvedMethodCallModelList
      * @return void
      * @date 2020/7/29 3:36 PM
      * @author xxx
      */
    private void processUnresolvedScopeAndArgumentInMethodCall(MethodCallModel methodCallModel
            , List<MethodCallModel> resolvedMethodCallModelList) {
        // process unresolved scope
        ScopeModel scopeModel = methodCallModel.getScopeModel();
        if (scopeModel != null && !scopeModel.isResolved()) {
            String expressionType = scopeModel.getExpressionType();
            String scope = scopeModel.getScope();
            if ("isMethodCallExpr".equals(expressionType)) {
                String returnTypeOfMethodCall = searchReturnTypeOfMethodCallInResolvedMethodCalls(scope
                        , resolvedMethodCallModelList);
                if (returnTypeOfMethodCall != null) {
                    scopeModel.setScopeType(returnTypeOfMethodCall);
                    scopeModel.setResolved(true);
                }
            }
        }
        // process unresolved arguments.
        List<ArgumentModel> argumentModelList = methodCallModel.getArgumentModelList();
        if (argumentModelList != null) {
            for (ArgumentModel argumentModel : argumentModelList) {
                if (argumentModel.isResolved()){
                    continue;
                }
                String argumentExpressionType = argumentModel.getExpressionType();
                if ("isMethodCallExpr".equals(argumentExpressionType)) {
                    String argumentExpression = argumentModel.getArgumentExpression().toString();

                    String returnTypeOfMethodCall = searchReturnTypeOfMethodCallInResolvedMethodCalls(argumentExpression
                            , resolvedMethodCallModelList);
                    if (returnTypeOfMethodCall != null) {
                        argumentModel.setArgumentType(returnTypeOfMethodCall);
                        argumentModel.setResolved(true);
                    }
                }
            }
        }
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/29 2:42 PM
      * @author xxx
      */
    private String searchReturnTypeOfMethodCallInResolvedMethodCalls(String methodCall
            , List<MethodCallModel> resolvedMethodCallModelList) {
        MethodCallModel tempResolvedMethodCallModel = null;
        for (MethodCallModel resolvedMethodCallModel : resolvedMethodCallModelList) {
            String callStatement = resolvedMethodCallModel.getCallStatement();
            if (!callStatement.equals(methodCall)) {
                continue;
            }
            tempResolvedMethodCallModel = resolvedMethodCallModel;
            break;
        }
        if (tempResolvedMethodCallModel == null) {
            return null;
        }
        return tempResolvedMethodCallModel.getReturnType();
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/29 10:35 AM
      * @author xxx
      */
    private List<String> searchExtendedSignaturesByParameterComparison(List<ArgumentModel> argumentModelList
            , List<String> extendedSignatureWithSameClassAndMethodNameList) {
        // Step1: 进行参数数量比较
        int argumentNumber = 0;
        if (argumentModelList != null) {
            argumentNumber = argumentModelList.size();
        }
        List<String> extendedSignatureWithSameParameterNumberList = new ArrayList<>();
        List<String> extendedSignatureWithVarargsList = new ArrayList<>();
        for (String extendedSignature : extendedSignatureWithSameClassAndMethodNameList) {
            String[] parameterTypeArray = extractParameterTypesFromExtendedSignature(extendedSignature);
            int parameterNumber = parameterTypeArray.length;
            if (argumentNumber == parameterNumber) {
                extendedSignatureWithSameParameterNumberList.add(extendedSignature);
                continue;
            }
            if (parameterNumber == 0) {
                continue;
            }
            String lastType = parameterTypeArray[parameterNumber - 1];
            if (!lastType.contains("[")) {
                continue;
            }
            /*
            如果最后一个参数是数组，才有可能出现使用“Varargs”机制，Varargs”是“variable number of arguments”
            Varargs 机制使用注意几个条件：
            1. 只有最后一个形参才可以使用 Varargs 机制，如果 parameterNumber == 0，则不可能使用了该机制；
            2. 调用使用 Varargs 机制的方法时，实参的数量可以为 0 个、1个或多个；
            3. 调用使用 Varargs 机制的方法时，实参的类型必须与形参类型一致。
             */
            extendedSignatureWithVarargsList.add(extendedSignature);
        }
        String argumentTypeString = null;
        String[] argumentTypeArray = null;
        if (argumentModelList != null) {
            argumentTypeString = extractArgumentTypeStringFromArgumentModelList(argumentModelList);
        }
        if (argumentTypeString != null) {
            argumentTypeArray = argumentTypeString.split(",");
        }
        if (extendedSignatureWithSameParameterNumberList.size() == 0) {
            /*
            extendedSignatureWithSameParameterNumberList.size() == 0 说明项目内没有方法在参数数量上与之匹配
             */
            if (extendedSignatureWithVarargsList.isEmpty() || argumentTypeString == null) {
                return extendedSignatureWithSameParameterNumberList;
            }
            // 处理方法形参中使用了 Varargs 机制。
            List<String> extendedSignatureWithMatchingVarargsList = processVarargsUsedInMethodDeclaration(argumentNumber
                    , argumentTypeString
                    , extendedSignatureWithVarargsList);

            return extendedSignatureWithMatchingVarargsList;
        }
        if (extendedSignatureWithSameParameterNumberList.size() == 1) {
            /*
            extendedSignatureWithSameParameterNumberList.size() == 1 说明项目内只有一个方法在参数数量上与之匹配
             */
            return extendedSignatureWithSameParameterNumberList;
        }
        /*
        说明项目内不止 1 个方法在参数数量上与之匹配，此时要通过 Step2 进行进一步比较。
        Step2: 进行参数类型比较
         */
        List<String> extendedSignatureWithSameParameterTypeList = new ArrayList<>();
        if (argumentTypeString == null) {
            // argumentModelList 中有参数的类型解析失败，无法进行参数类型比较
            return extendedSignatureWithSameParameterTypeList;
        }
        boolean notMatchingType = false;
        for (String extendedSignatureWithSameParameterNumber : extendedSignatureWithSameParameterNumberList) {
            String parameterTypeString = extractParameterTypeStringFromExtendedSignature(extendedSignatureWithSameParameterNumber);
            if (argumentTypeString.equals(parameterTypeString)) {
                extendedSignatureWithSameParameterTypeList.add(extendedSignatureWithSameParameterNumber);
                continue;
            }
            if (parameterTypeString.indexOf(",") == -1) {
                if (checkTypeIsJavaBasicDataType(argumentTypeString)
                        || checkTypeIsJavaBasicDataType(parameterTypeString)) {
                    continue;
                }
                boolean isCompatibleType = checkCommonTypeCompatibility(argumentTypeString, parameterTypeString);
                if (isCompatibleType) {
                    extendedSignatureWithSameParameterTypeList.add(extendedSignatureWithSameParameterNumber);
                    continue;
                }
            }
            String[] parameterTypeArray = parameterTypeString.split(",");
            for (int j = 0; j < argumentNumber; j++) {
                String argumentType = argumentTypeArray[j].trim();
                String parameterType = parameterTypeArray[j].trim();
                if (argumentType.equals(parameterType)) {
                    continue;
                }
                if (checkCommonTypeCompatibility(argumentType, parameterType)) {
                    continue;
                }
                notMatchingType = true;
                break;
            }
            if (notMatchingType) {
                notMatchingType = false;
                continue;
            }
            extendedSignatureWithSameParameterTypeList.add(extendedSignatureWithSameParameterNumber);
        }
        /*
        extendedSignatureWithSameParameterTypeList.size() == 0 说明项目内没有方法在参数类型和顺序上与之匹配
        extendedSignatureWithSameParameterTypeList.size() == 1 说明项目内只有一个方法在参数类型和顺序上与之匹配
        extendedSignatureWithSameParameterTypeList.size() >= 2 说明项目内不止一个方法在参数类型和顺序上与之匹配
        注意：可能是因为不同 Package 里面出现了相同的函数，当使用不同包中当函数时一定有 import 信息，
        通过 import 导入使用的方法在外部 import 分析中已经解决
         */
        return extendedSignatureWithSameParameterTypeList;
    }

    /**
      * java 使用省略号代替多参数（参数类型... 参数名）
      * J2SE 1.5提供了“Varargs”机制。借助这一机制，可以定义能和多个实参相匹配的形参。从而，可以用一种更简单的方式，来传递个数可变的实参。
      * for example:
      * the method declaration is 'networkPolicy(NetworkPolicy policy, NetworkPolicy... additional)'.
      * the method sinature is 'networkPolicy(NetworkPolicy, NetworkPolicy[])'.
      * @param argumentNumber
      * @param argumentTypeString
      * @param extendedSignatureWithVarargsList
      * @return List<String>
      * @date 2020/7/30 4:49 PM
      * @author xxx
      */
    private List<String> processVarargsUsedInMethodDeclaration(int argumentNumber, String argumentTypeString
            , List<String> extendedSignatureWithVarargsList) {
        List<String> extendedSignatureWithMatchingVarargsList = new ArrayList<>();
        String[] argumentTypeArray = argumentTypeString.split(",");
        boolean notMatchingType = false;
        for (String extendedSignature : extendedSignatureWithVarargsList) {
            String[] parameterTypeArray = extractParameterTypesFromExtendedSignature(extendedSignature);
            int parameterNumber = parameterTypeArray.length;
            if (parameterNumber == 1) {
                // 只有一个形参，并且参数类型为数组类型
                if (argumentNumber == 0) {
                    extendedSignatureWithMatchingVarargsList.add(extendedSignature);
                    continue;
                }
                String parameterType = parameterTypeArray[0];
                int end = parameterType.indexOf("[");
                String arrayElementType = parameterType.substring(0, end);
                for (String argumentType : argumentTypeArray) {
                    if (argumentType.equals(parameterType)) {
                            /*
                            for example:
                            parameterType = "int[]";
                            argumentType = "int[]";
                             */
                        continue;
                    }
                    if (argumentType.equals(arrayElementType)) {
                            /*
                            for example:
                            parameterType = "int[]";
                            argumentType = "int";
                             */
                        continue;
                    }
                    if (checkCommonTypeCompatibility(argumentType, arrayElementType)) {
                        continue;
                    }
                    notMatchingType = true;
                    break;
                }
                if (notMatchingType) {
                    notMatchingType = false;
                    continue;
                }
                extendedSignatureWithMatchingVarargsList.add(extendedSignature);
                continue;
            }
            /*
            如果使用了 Varargs 机制的方法有 N 个形参，那么在调用该方法时至少传递 N-1 个实参，
            并且前 N-1 个参数的类型要匹配或兼容。
             */
            int requiredArgumentNumber = parameterNumber - 1;
            if (argumentNumber < requiredArgumentNumber) {
                continue;
            }
            for (int i = 0; i < requiredArgumentNumber; i++) {
                String parameterType = parameterTypeArray[i];
                String argumentType = argumentTypeArray[i];
                if (argumentType.equals(parameterType)) {
                    continue;
                }
                if (checkCommonTypeCompatibility(argumentType, parameterType)) {
                    continue;
                }
                notMatchingType = true;
            }
            if (notMatchingType) {
                // 说明前 N-1 个不匹配
                notMatchingType = false;
                continue;
            }
            // 说明前 N-1 个匹配
            if (argumentNumber == requiredArgumentNumber) {
                // 说明形参只比实参多一个 Varargs 参数；
                extendedSignatureWithMatchingVarargsList.add(extendedSignature);
                continue;
            }
            // argumentNumber > requiredArgumentNumber
            String lastParameterType = parameterTypeArray[parameterNumber - 1];
            int end = lastParameterType.indexOf("[");
            String arrayElementType = lastParameterType.substring(0, end);
            for (int i = requiredArgumentNumber; i < argumentNumber; i++) {
                String argumentType = argumentTypeArray[i];
                if (argumentType.equals(lastParameterType)) {
                    continue;
                }
                if (argumentType.equals(arrayElementType)) {
                    continue;
                }
                if (checkCommonTypeCompatibility(argumentType, arrayElementType)) {
                    continue;
                }
                notMatchingType = true;
            }
            if (notMatchingType) {
                notMatchingType = false;
                continue;
            }
            extendedSignatureWithMatchingVarargsList.add(extendedSignature);
        }
        return extendedSignatureWithMatchingVarargsList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/28 11:12 PM
      * @author xxx
      */
    private void setMethodCallModelWithExtendedSignature(MethodCallModel methodCallModel, String extendedSignature) {
        ScopeModel scopeModel = methodCallModel.getScopeModel();
        ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(extendedSignature);
        methodCallModel.setExtendedSignature(extendedSignature);
        String returnType = extendedSignatureComponentModel.getReturnType();
        methodCallModel.setReturnType(returnType);
        if (methodCallModel.getMethodCallExpression().isMethodCallExpr()) {
            if (scopeModel == null) {
                scopeModel = new ScopeModel(null);
                scopeModel.setScopeType(extendedSignatureComponentModel.getClassOrInterfaceOrEnumName());
                scopeModel.setResolved(true);
                methodCallModel.setScopeModel(scopeModel);
            }
        }
        methodCallModel.setResolved(true);
        methodCallModel.setProductionMethodCall(true);
    }

    /**
      *
      * @param argumentType
      * @param parameterType
      * @return boolean
      * @date 2020/7/28 5:45 PM
      * @author xxx
      */
    private boolean checkCommonTypeCompatibility(String argumentType
            , String parameterType) {
        if ("Object".equals(parameterType)) {
            return true;
        }
        if ((argumentType.contains("Exception") && "Throwable".equals(parameterType))
                || (argumentType.contains("Error") && "Throwable".equals(parameterType))
                || (argumentType.contains("Exception") && "Exception".equals(parameterType))
                || (argumentType.contains("Error") && "Error".equals(parameterType))
                || (argumentType.contains("ThreadDeath") && "Error".equals(parameterType))) {
            // Object -> Throwable -> Exception|Error -> IOException,RunTimeException|VirtualMachineError,ThreadDeath
            return true;
        }
        if ((argumentType.contains("Set") && "Iterable".equals(parameterType))
                || (argumentType.contains("Queue") && "Iterable".equals(parameterType))
                || (argumentType.contains("List") && "Iterable".equals(parameterType))
                || (argumentType.contains("Set") && "Collection".equals(parameterType))
                || (argumentType.contains("Queue") && "Collection".equals(parameterType))
                || (argumentType.contains("List") && "Collection".equals(parameterType))
                || (argumentType.contains("Set") && "Set".equals(parameterType))
                || (argumentType.contains("Queue") && "Queue".equals(parameterType))
                ||(argumentType.contains("List") && "List".equals(parameterType))
                ) {
            // Object -> Iterable -> Collection -> Set|Queue|List -> HashSet|TransferQueue|ArrayList
            return true;
        }
        if ((argumentType.contains("Map") && "Map".equals(parameterType))) {
            // Object -> Map -> HashMap,TreeMap
            return true;
        }
        return false;
    }

    /**
      * Check wheter the type is a Java basic data type.
      * @param
      * @return
      * @throws
      * @date 2020/7/28 5:58 PM
      * @author xxx
      */
    private boolean checkTypeIsJavaBasicDataType(String type) {
        Set<String> basicDataTypeSet = new HashSet<String>(){
            {
                add("byte");
                add("short");
                add("int");
                add("long");
                add("float");
                add("double");
                add("boolean");
                add("char");
            }
        };
        if (basicDataTypeSet.contains(type)) {
            return true;
        }
        return false;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/26 4:03 PM
      * @author xxx
    private String produceExtendedSignatureForExternalCallableDeclaration(CallableDeclaration callableDeclaration) {
        List<AnnotationExpr> annotationExprList = callableDeclaration.getAnnotations();
        StringBuffer extendedSignatureBuffer = new StringBuffer();
        String packageName = "src";
        String className = "";
        for (AnnotationExpr annotationExpr: annotationExprList) {
            String annotationName = annotationExpr.getName().asString();
            if (annotationName.startsWith("PN_")) {
                annotationName = annotationName.replace("PN_", "");
                packageName = annotationName;
            }
            if (annotationName.startsWith("CN_")) {
                annotationName = annotationName.replace("CN_", "");
                className =  annotationName;
            }
        }
        extendedSignatureBuffer.append(packageName + "+" + className + "+");
        String callableDeclarationSignature = callableDeclaration.getSignature().asString();
        extendedSignatureBuffer.append(callableDeclarationSignature + "+");
        String returnType = "";
        if (callableDeclaration.isConstructorDeclaration()) {
            returnType = "empty";
        }
        if (callableDeclaration.isMethodDeclaration()) {
            returnType = callableDeclaration.asMethodDeclaration().getTypeAsString();
        }
        extendedSignatureBuffer.append(returnType);
        return extendedSignatureBuffer.toString();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/19 9:51 PM
     * @author xxx
     */
    private List<String> searchExtendedSignaturesByArgumentNumber(int argumentNumber
            , List<String> extendedSignatureList) {
        List<String> matchingExtendedSignatureList = new ArrayList<>();
        for (String extendedSignature : extendedSignatureList) {
            String[] elementArray = extendedSignature.split("\\+");
            String signature = elementArray[2];
            int start = signature.indexOf("(");
            int end = signature.lastIndexOf(")");
            String parameterTypeString = signature.substring(start + 1, end);
            List<String> parameterTypeList = extractParameterTypeFromParameterTypeString(parameterTypeString);
            if (parameterTypeList.size() == argumentNumber) {
                matchingExtendedSignatureList.add(extendedSignature);
            }
        }
        return matchingExtendedSignatureList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/20 9:50 AM
     * @author xxx
     */
    public String extractParameterTypeStringFromExtendedSignature(String extendedSignature) {
        int start = extendedSignature.indexOf("(");
        int end = extendedSignature.lastIndexOf(")");
        String parameterTypeString = extendedSignature.substring(start + 1, end);
        return parameterTypeString;
    }

    /**
      *
      * @param extendedSignature: PN+CN+MN(PT)+RT
      * @return
      * @throws
      * @date 2020/7/28 10:51 PM
      * @author xxx
      */
    public String[] extractParameterTypesFromExtendedSignature(String extendedSignature) {
        String[] elementArray = extendedSignature.split("\\+");
        String signature = elementArray[2];
        int start = signature.indexOf("(");
        int end = signature.lastIndexOf(")");
        String parameterTypeString = signature.substring(start + 1, end);
        String[] parameterTypeArray;
        if ("".equals(parameterTypeString)) {
            parameterTypeArray = new String[0];
        } else {
            parameterTypeArray = parameterTypeString.split(",");
        }
        return parameterTypeArray;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/11 3:46 PM
     * @author xxx
     */
    private List<MethodCallModel> processScopeAndArgument(List<? extends Expression> methodCallExprList
            , Map<String, String> localVariableNameTypeMap
            , Map<String, String> parameterNameTypeMap
            , Map<String, String> globalVariableNameTypedMap
            , int productionOrTestFlag) {
        List<MethodCallModel> methodCallModelList = new ArrayList<>(methodCallExprList.size());
        ObjectCreationExpr objectCreationExpr;
        MethodCallExpr methodCallExpr;
        for (Expression expression : methodCallExprList) {
            MethodCallModel methodCallModel = new MethodCallModel(expression.toString());
            methodCallModel.setMethodCallExpression(expression);
            String methodName = null;
            Optional<Expression> scope = null;
            List<Expression> arguments = null;
            if (expression.isMethodCallExpr()) {
                methodCallExpr = expression.asMethodCallExpr();
                methodName = methodCallExpr.getName().getIdentifier();
                methodCallModel.setConstructorCall(false);
                scope = methodCallExpr.getScope();
                arguments = methodCallExpr.getArguments();// 无法获取 null 参数
            }
            if (expression.isObjectCreationExpr()) {
                objectCreationExpr = expression.asObjectCreationExpr();
                methodName = objectCreationExpr.getType().getNameAsString();
                methodCallModel.setConstructorCall(true);
                arguments = objectCreationExpr.getArguments(); // 无法获取 null 参数
            }
            methodCallModel.setMethodName(methodName);

            if (scope != null && scope.isPresent()) {
                // process the scope of the method call
                Expression scopeExpression = scope.get();
                String scopeString = scopeExpression.toString();
//                System.out.println("scope expression type: " + scopeExpression.getClass().getName());
                ScopeModel scopeModel = new ScopeModel(scopeExpression);
                scopeModel.setScope(scopeExpression.toString());
                String scopeType = null;
                if (scopeExpression.isMethodCallExpr()) {
                    scopeModel.setExpressionType("isMethodCallExpr");
                } else if (scopeExpression.isObjectCreationExpr()) {
                    /*
                    old way: scopeType = scopeExpression.asObjectCreationExpr().getType().getNameAsString();
                    new way: scopeType = scopeExpression.asObjectCreationExpr().getTypeAsString();
                    change date:  2020/7/28 3:06 PM
                    change reason:
                    For the expression 'new Request.Builder(URI_1)',
                     the type extracted by the old way is 'Builder' while that extracted by the new way is 'Request.Builder';
                     */
                    scopeType = scopeExpression.asObjectCreationExpr().getTypeAsString();
                    scopeModel.setExpressionType("isObjectCreationExpr");
                } else if (scopeExpression.isThisExpr()) {
                    try {
                        scopeType = scopeExpression.asThisExpr().calculateResolvedType().describe();
                    } catch (UnsolvedSymbolException exception) {
                        scopeType = exception.getName();
                    } catch (UnsupportedOperationException unsupportedOperationException) {
                        //
                    } finally {
                        scopeModel.setExpressionType("isThisExpr");
                    }
                } else if (scopeExpression.isNameExpr()) {
                    if (localVariableNameTypeMap != null) {
                        // 检查 scope 是不是函数体内声明的局部变量
                        scopeType = localVariableNameTypeMap.get(scopeString);
                    }
                    if (scopeType == null && parameterNameTypeMap != null) {
                        // 检查 scope 是不是方法的参数
                        scopeType = parameterNameTypeMap.get(scopeString);
                    }
                    if (scopeType == null && globalVariableNameTypedMap != null) {
                        // 检查 scope 是不是全局字段
                        scopeType = globalVariableNameTypedMap.get(scopeString);
                    }
                    if (scopeType == null) {
                        try {
                            scopeType = scopeExpression.asNameExpr().calculateResolvedType().describe();
                            if (scopeType.contains("java.lang.")) {
                                scopeType = scopeType.replace("java.lang.", "");
                            }
                        } catch (UnsolvedSymbolException exception) {
                            scopeType = exception.getName();
                        } catch (UnsupportedOperationException unsupportedOperationException) {
                            // java.lang.UnsupportedOperationException: CorrespondingDeclaration not available for unsolved symbol.
                            // 匿名内中的变量引用无法解析
                        } catch (RuntimeException runtimeException) {
                        }
                    }
                } else if (scopeExpression.isFieldAccessExpr()) {
                    /*
                    for example: action.request
                    'action' is a instance of the class 'Action'.
                    'request' is a field that declared in the class 'Action'.
                     */
                    String fieldScopeType = null;
                    FieldAccessExpr fieldAccessExpr = scopeExpression.asFieldAccessExpr();
                    Expression fieldScopeExpression = fieldAccessExpr.getScope();
                    try {
                        fieldScopeType = fieldScopeExpression.calculateResolvedType().describe();
                    } catch (UnsolvedSymbolException unsolvedSymbolException) {
                        fieldScopeType = unsolvedSymbolException.getName();
                    } catch (UnsupportedOperationException unsupportedOperationException) {
                        //
                    } catch (RuntimeException runtimeException) {
                        // java.lang.RuntimeException: Error calculating the type of parameter uri of method call matcher.match(uri)
                    } catch (StackOverflowError error) {
                        /*
                        字段访问太长会报错 StackOverflowError
                        例如：BuilderToken.GLOBAL.BuilderToken.PROJECTION.exactToken() 中
                        scope = "BuilderToken.GLOBAL.BuilderToken.PROJECTION";
                        2020/8/9 16:41
                         */
                    }
                    if (fieldScopeType != null) {
                        if (fieldScopeType.contains(".")) {
                            fieldScopeType = removePackageNameIfClassNameIsInProject(fieldScopeType);
                        }
                        String fieldIdentifier = fieldAccessExpr.getName().getIdentifier();
                        scopeType = searchIdentifierTypeInProjectFieldDeclarationMap(fieldIdentifier
                                , fieldScopeType, productionOrTestFlag);
                    }
                } else {
                    try {
                        scopeType = scopeExpression.calculateResolvedType().describe();
                    } catch (UnsolvedSymbolException unsolvedSymbolException) {
                        scopeType = unsolvedSymbolException.getName();
                    } catch (UnsupportedOperationException unsupportedOperationException) {
                        // java.lang.UnsupportedOperationException: CorrespondingDeclaration not available for unsolved symbol.
                    } catch (RuntimeException runtimeException) {
                        //
                    }
                }
                if (scopeType != null) {
                    if (scopeType.startsWith("Solving ")) {
                                /*
                                Note
                                Case 1:
                                Utils.flushStackLocalLeaks(xxx);
                                scope = "Utils";
                                ResolvedType = "Solving Utils";
                                Case 2:
                                Collections.unmodifiableList(allRequestHandlers)
                                scope = "Collections";
                                ResolvedType = "java.util.Collections";
                                 */
                        scopeType = scopeType.replace("Solving ", "");
                    }
                    scopeModel.setScopeType(scopeType);
                    scopeModel.setResolved(true);
                }
                methodCallModel.setScopeModel(scopeModel);
            }

            if (arguments != null) {
                // process the arguments of the method call.
                int argumentNumber = arguments.size();
                List<ArgumentModel> argumentModelList = new ArrayList<>(argumentNumber);
                for (int i = 0; i < argumentNumber; i++) {
                    Expression argumentExpression = arguments.get(i);
                    String argumentType = parseArgumentType(argumentExpression
                            , localVariableNameTypeMap, parameterNameTypeMap, globalVariableNameTypedMap);
                    ArgumentModel argumentModel = new ArgumentModel(i, argumentExpression);
                    if ("isMethodCallExpr".equals(argumentType)) {
                        argumentModel.setExpressionType("isMethodCallExpr");
                        argumentModel.setResolved(false);
                        argumentModelList.add(argumentModel);
                        continue;
                    }
                    if ("isNullLiteralExpr".equals(argumentType)) {
                        argumentModel.setResolved(true);
                        argumentModel.setArgumentType("Object");
                        argumentModel.setExpressionType("isNullLiteralExpr");
                        argumentModelList.add(argumentModel);
                        continue;
                    }
                    if ("unknown".equals(argumentType)) {
                        argumentModel.setResolved(false);
                        argumentModel.setExpressionType("unknown");
                        argumentModelList.add(argumentModel);
                        continue;
                    }
                    argumentModel.setResolved(true);
                    argumentModel.setArgumentType(argumentType);
                    argumentModelList.add(argumentModel);
                }
                if (argumentModelList.size() > 0) {
                    methodCallModel.setArgumentModelList(argumentModelList);
                }
            }
            methodCallModelList.add(methodCallModel);
        }
        return methodCallModelList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/16 3:29 PM
     * @author xxx
     */
    private String removePackageNameIfClassNameIsInProject(String className) {
        Set<String> packageNameSetInProject = this.packageDeclarationInAllProductionFilesMap.keySet();
        for (String packageName : packageNameSetInProject) {
            int index = className.indexOf(packageName);
            if (index == -1) {
                continue;
            }
            className = className.substring(index + packageName.length() + 1);
            break;
        }
        return className;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/15 3:25 PM
     * @author xxx
     */
    private String parseArgumentType(Expression argument
            , Map<String, String> localVariableNameTypeMap
            , Map<String, String> parameterNameTypeMap
            , Map<String, String> globalVariableNameTypedMap) {
        if (argument.isMethodCallExpr()) {
            return "isMethodCallExpr";
        }
        if (argument.isObjectCreationExpr()) {
            /*
            old way: argument.asObjectCreationExpr().getType().getNameAsString();
            new way: argument.asObjectCreationExpr().getTypeAsString();
            change date:  2020/7/28 3:06 PM
            change reason:
            For the expression 'new Request.Builder(URI_1)',
            the type extracted by the old way is 'Builder' while that extracted by the new way is 'Request.Builder';
             */
            return argument.asObjectCreationExpr().getTypeAsString();
        }
        if (argument.isNullLiteralExpr()) {
            return "isNullLiteralExpr";
        }
        if (argument.isStringLiteralExpr()) {
            return "String";
        }
        if (argument.isBooleanLiteralExpr()) {
            return "boolean";
        }
        if (argument.isCharLiteralExpr()) {
            return "char";
        }
        if (argument.isDoubleLiteralExpr()) {
            return "double";
        }
        if (argument.isIntegerLiteralExpr()) {
            return "int";
        }
        if (argument.isLongLiteralExpr()) {
            return "long";
        }
        if (argument.isArrayCreationExpr()) {
            String argumentType = argument.asArrayCreationExpr().createdType().asString();
            if (argumentType.contains("java.lang.")) {
                argumentType = argumentType.replace("java.lang.", "");
            }
            return argumentType;
        }
        if (argument.isArrayAccessExpr()) {
            String argumentType = null;
            try {
                argumentType = argument.calculateResolvedType().describe();
            } catch (UnsolvedSymbolException exception) {
                argumentType = exception.getName();
            } catch (UnsupportedOperationException exception) {
                // no process
            } finally {
                if (argumentType == null) {
                    return "unknown";
                }
                if (argumentType.contains("java.lang.")) {
                    argumentType = argumentType.replace("java.lang.", "");
                }
                return argumentType;
            }
        }
        if (argument.isThisExpr()) {
            String argumentType = null;
            try {
                argumentType = argument.asThisExpr().calculateResolvedType().describe();
            } catch (UnsolvedSymbolException exception) {
                argumentType = exception.getName();
            } catch (UnsupportedOperationException exception) {
                // no process
            } finally {
                if (argumentType == null) {
                    return "unknown";
                }
                if (argumentType.contains("java.lang.")) {
                    argumentType = argumentType.replace("java.lang.", "");
                }
                return argumentType;
            }
        }

        if (argument.isFieldAccessExpr()) {
            String argumentType = argument.asFieldAccessExpr().getScope().toString();
            return argumentType;
        }
        String argumentType = null;
        if (argument.isNameExpr()) {
            String argumentString = argument.toString();
            if (localVariableNameTypeMap != null) {
                argumentType = localVariableNameTypeMap.get(argumentString);
            }
            if (argumentType == null && parameterNameTypeMap != null) {
                argumentType = parameterNameTypeMap.get(argumentString);
            }
            if (argumentType == null && globalVariableNameTypedMap != null) {
                argumentType = globalVariableNameTypedMap.get(argumentString);
            }
        }
        if (argumentType != null) {
            return argumentType;
        }
        try {
            argumentType = argument.calculateResolvedType().describe();
        } catch (UnsolvedSymbolException exception) {
            argumentType = exception.getName();
        } catch (UnsupportedOperationException unsupportedOperationException) {
            // java.lang.UnsupportedOperationException: CorrespondingDeclaration not available for unsolved symbol.
            // 匿名内中的变量引用无法解析
        } catch (RuntimeException runtimeException) {
            // java.lang.RuntimeException: Error calculating the type of parameter uri of method call matcher.match(uri)
        } finally {
            if (argumentType == null) {
                return "unknown";
            }
            if (argumentType.contains("java.lang.")) {
                argumentType = argumentType.replace("java.lang.", "");
            }
            return argumentType;
        }
    }

    /**
     *
     * @param fieldIdentifier
     * @param classOrInterfaceName
     * @param productionOrTestField 0 : fields in the production code; 1 : fields in the test code.
     * @throws
     * @date 2020/7/15 9:19 PM
     * @chagedate 2020/7/27 4:19 PM
     * @author xxx
     */
    private String searchIdentifierTypeInProjectFieldDeclarationMap(String fieldIdentifier
            , String classOrInterfaceName, int productionOrTestField) {
        Set<String> classOrInterfaceIdentifierSet;
        List<FieldDeclaration> fieldDeclarationList;
        String scopeType = null;
        if (productionOrTestField == 1) {
            /*
            先去所有测试代码中的字段中查找
             */
            classOrInterfaceIdentifierSet = this.fieldDeclarationInAllTestFilesMap.keySet();
            for (String classOrInterfaceIdentifier : classOrInterfaceIdentifierSet) {
                if (!classOrInterfaceIdentifier.contains("+" + classOrInterfaceName)) {
                    continue;
                }
                fieldDeclarationList = this.fieldDeclarationInAllTestFilesMap.get(classOrInterfaceIdentifier);
                scopeType = searchIdentifierTypeInFieldDeclarationList(fieldIdentifier, fieldDeclarationList);
            }
            if (scopeType != null) {
                return scopeType;
            }
        }
        classOrInterfaceIdentifierSet = this.fieldDeclarationInAllProductionFilesMap.keySet();
        for (String classOrInterfaceIdentifier : classOrInterfaceIdentifierSet) {
            if (!classOrInterfaceIdentifier.contains("+" + classOrInterfaceName)) {
                continue;
            }
            fieldDeclarationList = this.fieldDeclarationInAllProductionFilesMap.get(classOrInterfaceIdentifier);
            scopeType = searchIdentifierTypeInFieldDeclarationList(fieldIdentifier, fieldDeclarationList);
        }
        return scopeType;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/7/27 4:26 PM
      * @author xxx
      */
    private String searchIdentifierTypeInFieldDeclarationList(String fieldIdentifier
            , List<FieldDeclaration> fieldDeclarationList) {
        String scopeType = null;
        for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
            List<VariableDeclarator> variableDeclaratorList = fieldDeclaration.getVariables();
            for (VariableDeclarator variableDeclarator : variableDeclaratorList) {
                String variableName = variableDeclarator.getName().getIdentifier();
                if (!variableName.equals(fieldIdentifier)) {
                    continue;
                }
                scopeType = variableDeclarator.getTypeAsString();
            }
        }
        return scopeType;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/20 9:54 AM
     * @author xxx
     */
    private String extractReturnTypeStringFromExtendedSignature(String extendedSignature) {
        // format: com.squareup.picasso3+Dispatcher+dispatchFailed(BitmapHunter)+void
        String[] elementArray = extendedSignature.split("\\+");
        return elementArray[3];
    }


    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/7/17 10:38 PM
     * @author xxx
     */
    private String extractArgumentTypeStringFromArgumentModelList(List<ArgumentModel> argumentModelList) {
        int argumentNumber = argumentModelList.size();
        boolean argumentFailedResolved = false;
        int i = 0;
        StringBuffer typeStringBuffer = new StringBuffer();
        for (int j = 0; j < argumentNumber; j++) {
            ArgumentModel argumentModel = argumentModelList.get(i);
            if (!argumentModel.isResolved()) {
                argumentFailedResolved = true;
                break;
            }
            if (j < argumentNumber - 1) {
                typeStringBuffer.append(argumentModel.getArgumentType() + ", ");
            } else {
                typeStringBuffer.append(argumentModel.getArgumentType());
            }
            i++;
        }
        if (argumentFailedResolved) {
            return null;
        }
        return typeStringBuffer.toString();
    }

    /**
     *
     * @param callableDeclaration
     * @param classOrInterfaceOrEnumName
     * @param globalVariableNameTypedMap
     * @param productionMethodSignatureSetInProject
     * @param testMethodSignatureSetInProject
     * @param externalMethodCallDependencyList
     * @return List<MethodCallModel>
     * @date 2020/7/28 10:31 AM
     * @author xxx
     */
    public List<MethodCallModel> analysisMethodDependencyForCallableDeclaration(CallableDeclaration callableDeclaration
            , String classOrInterfaceOrEnumName
            , List<String> extendClassNameList
            , Map<String, String> globalVariableNameTypedMap
            , Set<String> productionMethodSignatureSetInProject
            , Set<String> testMethodSignatureSetInProject
            , List<CallableDeclaration> externalMethodCallDependencyList) {

        List<MethodCallModel> methodDependencyList = new ArrayList<>();

        String methodName = callableDeclaration.getNameAsString();
        // collect parameter variables and the corresponding types.
        Map<String, String> parameterNameTypeMap = new HashMap<>();
        List<Parameter> parameterList = callableDeclaration.getParameters();
        if (!parameterList.isEmpty()) {
            for (Parameter parameter : parameterList) {
                String parameterName = parameter.getNameAsString();
                String parameterType = parameter.getTypeAsString();
                parameterNameTypeMap.put(parameterName, parameterType);
            }
        }
        // collect local variables and the corresponding types.
        List<VariableDeclarator> localVariableDeclaratorList = callableDeclaration.findAll(VariableDeclarator.class);
        Map<String, String> localVariableNameTypeMap = new HashMap<>();
        if (!localVariableDeclaratorList.isEmpty()) {
            for (VariableDeclarator localVariableDeclarator : localVariableDeclaratorList) {
                String localVariableName = localVariableDeclarator.getNameAsString();
                String localVariableType = localVariableDeclarator.getTypeAsString();
                localVariableNameTypeMap.put(localVariableName, localVariableType);
            }
        }
        // collect local variables in methods that belong to the anonymous class.
        List<MethodDeclaration> methodDeclarationListInCallableDeclaration = callableDeclaration.findAll(MethodDeclaration.class);
        if (methodDeclarationListInCallableDeclaration.size() > 1) {
            /*
            说明匿名内部类中有方法声明；
            处理匿名内部类中的函数，提取函数的参数；
            因为匿名内部类中也可能包含外部函数调用依赖，而 Javapaser 本身无法解析匿名内部类中的变量使用。
             */
            for (MethodDeclaration methodDeclarationInCallableDeclaration : methodDeclarationListInCallableDeclaration) {
                String innerMethodName = methodDeclarationInCallableDeclaration.getNameAsString();
                if (innerMethodName.equals(methodName)) {
                    continue;
                }
                List<Parameter> innerMethodParameterList =  methodDeclarationInCallableDeclaration.getParameters();
                if (innerMethodParameterList != null && innerMethodParameterList.size() > 0) {
                    for (Parameter parameter : innerMethodParameterList) {
                        String parameterName = parameter.getNameAsString();
                        String parameterType = parameter.getTypeAsString();
                        localVariableNameTypeMap.put(parameterName, parameterType);
                    }
                }
            }
        }

//        System.out.println("Analyze method call dependencies --- ");
        List<MethodCallExpr> methodCallExprList = callableDeclaration.findAll(MethodCallExpr.class);
        if (!methodCallExprList.isEmpty()) {
            if (methodCallExprList.size() > 1) {
                // remove all repeat method calls.
                Map<String, MethodCallExpr> methodCallExprMap = new HashMap<>();
                for (MethodCallExpr methodCallExpr : methodCallExprList) {
                    String methodCallExprString = methodCallExpr.toString();
                    if (methodCallExprMap.containsKey(methodCallExprString)) {
                        continue;
                    }
                    methodCallExprMap.put(methodCallExprString, methodCallExpr);
                }
                Iterator<Map.Entry<String, MethodCallExpr>> iterator = methodCallExprMap.entrySet().iterator();
                methodCallExprList.clear();
                while (iterator.hasNext()) {
                    Map.Entry<String, MethodCallExpr> entry = iterator.next();
                    methodCallExprList.add(entry.getValue());
                    iterator.remove();
                }
            }

            List<MethodCallModel> methodCallList = analyzeMethodDependency(methodCallExprList
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , localVariableNameTypeMap
                    , parameterNameTypeMap
                    , globalVariableNameTypedMap
                    , productionMethodSignatureSetInProject
                    , testMethodSignatureSetInProject
                    , externalMethodCallDependencyList);
            methodDependencyList.addAll(methodCallList);

        }

//        System.out.println("Analyze constructor call dependencies ---");
        List<ObjectCreationExpr> objectCreationExprList = callableDeclaration.findAll(ObjectCreationExpr.class);
        if (!objectCreationExprList.isEmpty()) {
            List<MethodCallModel> constructorCallList = analyzeMethodDependency(objectCreationExprList
                    , classOrInterfaceOrEnumName
                    , extendClassNameList
                    , localVariableNameTypeMap
                    , parameterNameTypeMap
                    , globalVariableNameTypedMap
                    , productionMethodSignatureSetInProject
                    , testMethodSignatureSetInProject
                    , externalMethodCallDependencyList);
            methodDependencyList.addAll(constructorCallList);
        }

        return methodDependencyList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/30 10:07 PM
     * @author xxx
     */
    public void extractMethodFragmentFromMethodClass(CompilationUnit currentMethodClassCU
            , String extendedSignatureOfCurrentMethod
            , String fragmentOfSingleMethodDirectoryPath) {
        List<ImportDeclaration> importDeclarationList = currentMethodClassCU.findAll(ImportDeclaration.class);
        Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationOptional = currentMethodClassCU.findFirst(ClassOrInterfaceDeclaration.class);
        Optional<EnumDeclaration> enumDeclarationOptional = currentMethodClassCU.findFirst(EnumDeclaration.class);

        List<MethodDeclaration> methodDeclarationList = null;
        List<CallableDeclaration> callableDeclarationList = null;
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
            callableDeclarationList = classOrInterfaceDeclaration.findAll(CallableDeclaration.class);
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
            callableDeclarationList = enumDeclaration.findAll(CallableDeclaration.class);
            fieldDeclarationList = enumDeclaration.findAll(FieldDeclaration.class);
            initializerDeclarationList = enumDeclaration.findAll(InitializerDeclaration.class);
            enumConstantDeclarationList = enumDeclaration.getEntries();
        }

        List<CallableDeclaration> externalMethodCallDependencyList = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : methodDeclarationList) {
            if (methodDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                externalMethodCallDependencyList.add(methodDeclaration);
            }
        }

        ExtendedSignatureComponentModel extendedSignatureComponentModel = parseExtendedSignature(extendedSignatureOfCurrentMethod);
        String classOrInterfaceOrEnumName = extendedSignatureComponentModel.getClassOrInterfaceOrEnumName();
        String methodName = extendedSignatureComponentModel.getMethodName();

        CallableDeclaration callableDeclaration = null;
        for (CallableDeclaration tempCallableDeclaration : callableDeclarationList) {
            String callableDeclarationName = tempCallableDeclaration.getNameAsString();
            if (tempCallableDeclaration.getAnnotationByName("TBooster_External_Method").isPresent()) {
                continue;
            }
            if (!callableDeclarationName.equals(methodName)) {
                continue;
            }
            callableDeclaration = tempCallableDeclaration;
        }
        if (callableDeclaration == null) {
            /*
            2020/8/7 13:25
             */
            return;
        }

//        System.out.println("Analyze the basic information: ");
        NewMethodModel newMethodModel = new NewMethodModel();
        newMethodModel.setPackageName(extendedSignatureComponentModel.getPackageName());
        newMethodModel.setClassOrInterfaceOrEnumName(classOrInterfaceOrEnumName);
        if (classAnnotationList != null) {
            newMethodModel.setClassAnnotationList(classAnnotationList);
        }
        if (extendClassNameList != null) {
            newMethodModel.setExtendedTypeList(extendClassNameList);
        }
        if (implementInterfaceNameList != null) {
            newMethodModel.setImplementedTypeList(implementInterfaceNameList);
        }
        newMethodModel.setMethodName(methodName);
        newMethodModel.setExtendedSignature(extendedSignatureOfCurrentMethod);
        String returnType = extendedSignatureComponentModel.getReturnType();
        if ("empty".equals(returnType)) {
            newMethodModel.setConstructor(true);
        }
        newMethodModel.setReturnType(returnType);
        newMethodModel.setMethodCode(callableDeclaration.toString());
        newMethodModel.setSignature(callableDeclaration.getSignature().asString());
        NodeList<Modifier> modifierNodeList = callableDeclaration.getModifiers();
        if (!modifierNodeList.isEmpty()) {
            List<String> modifierList = new ArrayList<>();
            for (Modifier modifier : modifierNodeList) {
                String modifierString = modifier.getKeyword().asString();
                modifierList.add(modifierString);
            }
            if (!modifierList.isEmpty()) {
                newMethodModel.setModifierList(modifierList);
                modifierList = null;
            }
        }
        String methodComment = callableDeclaration.getJavadoc().toString();
        if (!"Optional.empty".equals(methodComment)) {
            newMethodModel.setMethodComment(methodComment);
        }
        Map<String, String> parameterNameTypeMap = new HashMap<>();
        List<Parameter> parameterList = callableDeclaration.getParameters();
        if (!parameterList.isEmpty()) {
            newMethodModel.setParameters(parameterList.toString());
            for (Parameter parameter : parameterList) {
                String parameterName = parameter.getNameAsString();
                String parameterType = parameter.getTypeAsString();
                parameterNameTypeMap.put(parameterName, parameterType);
            }
        }
//        System.out.println("\t" + newMethodModel.toString());

//        System.out.println("Analyze third party import: ");
        List<String> importDependencyList = new ArrayList<>();
        if (!importDeclarationList.isEmpty()) {
            for (ImportDeclaration importDeclaration : importDeclarationList) {
                String importString = importDeclaration.toString();
                if (importString.indexOf(System.lineSeparator()) != -1) {
                    importString = importString.replace(System.lineSeparator(), "");
                }
                importDependencyList.add(importString);
            }
            newMethodModel.setImportDependencyList(importDependencyList);
        }

//        System.out.println("Analyze Global Variable Dependencies: ");
        List<String> globalVariableDependencyList = new ArrayList<>();
        if (!fieldDeclarationList.isEmpty()) {
            for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
                String fieldDeclarationString = fieldDeclaration.toString();
                globalVariableDependencyList.add(fieldDeclarationString);
            }
            if (!globalVariableDependencyList.isEmpty()) {
                newMethodModel.setGlobalVariableDependencyList(globalVariableDependencyList);
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
                newMethodModel.setInitializerDependencyList(initializerDependencyList);
                initializerDependencyList = null;
            }
        }

        if (enumConstantDeclarationList != null) {
//            System.out.println("Analyze Enum Entry Dependencies:");
            List<String> enumEntryDependencyList = new ArrayList<>();
            for (EnumConstantDeclaration enumConstantDeclaration :enumConstantDeclarationList) {
                String enumEntryDependency = enumConstantDeclaration.toString();
                enumEntryDependencyList.add(enumEntryDependency);
            }
            newMethodModel.setEnumEntryDependencyList(enumEntryDependencyList);
        }
        if (!externalMethodCallDependencyList.isEmpty()) {
            List<String> methodDependencyList = extractMethodDependencyFromExternalMethodCallDependencyList(externalMethodCallDependencyList);
            newMethodModel.setMethodDependencyList(methodDependencyList);
        }
//        System.out.println("Save Method Fragment.");
        saveMethodFragmentToTargetPath(newMethodModel, fragmentOfSingleMethodDirectoryPath);
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/6/30 10:07 PM
     * @author xxx
     */
    private void saveMethodFragmentToTargetPath(NewMethodModel newMethodModel
            , String targetDirectoryPath) {
        /*
        new 2020/8/8 14:13
         */
        String extendedSignature = newMethodModel.getExtendedSignature();
        String md5String = MD5Util.getMD5(extendedSignature);
        String targetFilePath = targetDirectoryPath + File.separator + md5String + ".json";
        String jsonString = (new JacksonUtil()).bean2Json(newMethodModel);
        FileUtil.writeStringToTargetFile(jsonString, targetFilePath);
        newMethodModel = null;

        /*
        old

        StringBuffer targetFilePathBuffer = new StringBuffer(targetDirectoryPath + File.separator);
        String packageName = newMethodModel.getPackageName();
        targetFilePathBuffer.append(packageName + "+");
        String classOrInterfaceOrEnumName = newMethodModel.getClassOrInterfaceOrEnumName();
        targetFilePathBuffer.append(classOrInterfaceOrEnumName + "+");
        String methodSignature = newMethodModel.getSignature();
        targetFilePathBuffer.append(methodSignature + "+");
        targetFilePathBuffer.append(newMethodModel.getReturnType() + ".json");
        String jsonString = JacksonUtil.bean2Json(newMethodModel);
        String targetFilePath = targetFilePathBuffer.toString();
        FileUtil.writeStringToTargetFile(jsonString, targetFilePath);
        targetFilePath = null;
        targetFilePathBuffer = null;
         */
    }


    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/1 3:48 PM
     * @author xxx
     */
    private List<String> extractMethodDependencyFromExternalMethodCallDependencyList(List<CallableDeclaration> externalMethodCallDependencyList) {
        List<String> methodDependencyList = new ArrayList<>(externalMethodCallDependencyList.size());
        for (CallableDeclaration callableDeclaration : externalMethodCallDependencyList) {
            String packageName = null;
            String className = null;
            String methodSignature = callableDeclaration.getSignature().asString();
            String returnType = callableDeclaration.asMethodDeclaration().getTypeAsString();
            NodeList<AnnotationExpr> annotationExprNodeList = callableDeclaration.getAnnotations();
            for (AnnotationExpr annotationExpr : annotationExprNodeList) {
                String annotationName = annotationExpr.getNameAsString();
                if ("TBooster_External_Method".equals(annotationName)) {
                    continue;
                }
                int underlineIndex = annotationName.indexOf("_");
                if (annotationName.startsWith("PN_")){
                    packageName = annotationName.substring(underlineIndex + 1);
                }
                if (annotationName.startsWith("CN_")){
                    className = annotationName.substring(underlineIndex + 1);
                }
            }
            String extendedSignature = packageName + "+" + className + "+" + methodSignature + "+" + returnType;
            methodDependencyList.add(extendedSignature);
        }
        return methodDependencyList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/2 1:24 PM
     * @author xxx
     */
    public void saveSingleMethodToDatabase(ProjectInfoTableModel projectInfoTableModel
            , String fragmentOfSingleMethodDirectoryPath, int fromWhere) {
        File fragmentOfSingleMethodInProductionCodeDirectory = new File(fragmentOfSingleMethodDirectoryPath);
        File[] fragmentOfSingleMethodFileArray = fragmentOfSingleMethodInProductionCodeDirectory.listFiles();
        MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();
        for (File fragmentOfSingleMethodFile : fragmentOfSingleMethodFileArray) {
            String fileName = fragmentOfSingleMethodFile.getName();
            if (!fileName.endsWith(".json")) {
                continue;
            }
            if ("md5_extended_signature_map.json".equals(fileName)) {
                continue;
            }
            String fileContentString = FileUtil.readFileContentToString(fragmentOfSingleMethodFile);
            NewMethodModel newMethodModel = (new JacksonUtil()).json2Bean(fileContentString, NewMethodModel.class);
            List<String> importDependencyList = newMethodModel.getImportDependencyList();
            Set<String> importDependencySet = null;
            if (importDependencyList != null) {
                importDependencySet = saveImportDependencyListToDatabase(importDependencyList);
            }
            MethodInfoTableModel methodInfoTableModel = prepareMethodInfoTableModel(projectInfoTableModel
                    , importDependencySet, newMethodModel);
            methodInfoTableModel.setFromWhere(fromWhere);
            methodInfoTableDao.saveMethodInfoToDatabase(methodInfoTableModel);

            methodInfoTableModel = null;
            newMethodModel = null;
            fragmentOfSingleMethodFile = null;
        }
        methodInfoTableDao = null;
        fragmentOfSingleMethodFileArray = null;
        fragmentOfSingleMethodInProductionCodeDirectory = null;
    }

    /**
      *
      * @param projectInfoTableModel
      * @param importDependencySet
      * @param newMethodModel
      * @return MethodInfoTableModel
      * @date 2020/8/4 1:51 PM
      * @author xxx
      */
    private MethodInfoTableModel prepareMethodInfoTableModel(ProjectInfoTableModel projectInfoTableModel
            , Set<String> importDependencySet, NewMethodModel newMethodModel) {
        String repositoryId = projectInfoTableModel.getRepositoryId();
        String repositoryName = projectInfoTableModel.getRepositoryName();
        String projectName = projectInfoTableModel.getProjectName();

        MethodInfoTableModel methodInfoTableModel = new MethodInfoTableModel();
        methodInfoTableModel.setPackageName(newMethodModel.getPackageName());
        methodInfoTableModel.setClassName(newMethodModel.getClassOrInterfaceOrEnumName());
        List<String> classAnnotationList = newMethodModel.getClassAnnotationList();
        if (classAnnotationList != null) {
            methodInfoTableModel.setClassAnnotations(classAnnotationList.toString());
            classAnnotationList = null;
        }
        List<String> extendedTypeList = newMethodModel.getExtendedTypeList();
        if (extendedTypeList != null) {
            methodInfoTableModel.setExtendsClasses(extendedTypeList.toString());
            extendedTypeList = null;
        }
        List<String> implementedTypeList = newMethodModel.getImplementedTypeList();
        if (implementedTypeList != null) {
            methodInfoTableModel.setImplementsInterfaces(implementedTypeList.toString());
            implementedTypeList = null;
        }
        methodInfoTableModel.setMethodName(newMethodModel.getMethodName());
        methodInfoTableModel.setParameterTypes(newMethodModel.getParameters());
        methodInfoTableModel.setReturnType(newMethodModel.getReturnType());
        String extendedSignature = newMethodModel.getExtendedSignature();
        methodInfoTableModel.setExtendedSignature(newMethodModel.getExtendedSignature());
        List<String> modifierList = newMethodModel.getModifierList();
        if (modifierList != null) {
            methodInfoTableModel.setModifiers(modifierList.toString());
            modifierList = null;
        }
        methodInfoTableModel.setSignature(newMethodModel.getSignature());
        if (importDependencySet != null) {
            methodInfoTableModel.setImportDependencies(importDependencySet.toString());
            importDependencySet = null;
        }
        List<String> globalVariableDependencyList = newMethodModel.getGlobalVariableDependencyList();
        if (globalVariableDependencyList!= null) {
            methodInfoTableModel.setVariableDependencies(globalVariableDependencyList.toString());
            globalVariableDependencyList = null;
        }
        List<String> initializerDependencyList = newMethodModel.getInitializerDependencyList();
        if (initializerDependencyList != null) {
            methodInfoTableModel.setInitializerDependencies(initializerDependencyList.toString());
            initializerDependencyList = null;
        }
        List<String> enumEntryDependencyList = newMethodModel.getEnumEntryDependencyList();
        if (enumEntryDependencyList != null) {
            methodInfoTableModel.setEnumDependencies(enumEntryDependencyList.toString());
            enumEntryDependencyList = null;
        }
        List<String> methodDependencyExtendedSignatureList = newMethodModel.getMethodDependencyList();
        if (methodDependencyExtendedSignatureList != null) {
            Set<String> methodDependencySet = new HashSet<>();
            for (String methodDependencyExtendedSignature : methodDependencyExtendedSignatureList) {
                String methodIdString = repositoryId + "+" + repositoryName + "+" + projectName + "+"
                        + methodDependencyExtendedSignature;
                String methodId = MD5Util.getMD5(methodIdString);
                methodDependencySet.add(methodId);
            }
            methodInfoTableModel.setMethodDependencies(methodDependencySet.toString());
            methodDependencyExtendedSignatureList = null;
        }
        methodInfoTableModel.setMethodCode(newMethodModel.getMethodCode());
        methodInfoTableModel.setMethodCommentSummary(newMethodModel.getMethodComment());
        methodInfoTableModel.setProjectId(projectInfoTableModel.getProjectId());
        String methodIdString = repositoryId + "+" + repositoryName + "+" + projectName + "+" + extendedSignature;
        String methodId = MD5Util.getMD5(methodIdString);
        methodInfoTableModel.setMethodId(methodId);
        return methodInfoTableModel;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/2 3:29 PM
     * @author xxx
     */
    public Set<String> saveImportDependencyListToDatabase(List<String> importDependencyList) {
        Set<String> importDependencySet = new HashSet<>();
        ImportInfoTableDao importInfoTableDao = new ImportInfoTableDao();
        for (String importDependency : importDependencyList) {
            importDependency = importDependency.replace("import ", "").trim();
            ImportInfoTableModel importInfoTableModel = new ImportInfoTableModel();
            String[] importComponentArray = importDependency.split("\\s");
            int importComponentNumber = importComponentArray.length;
            String importName = importComponentArray[importComponentNumber - 1];
            if ("".equals(importName)) {
                importName = importComponentArray[importComponentNumber - 2];
            }
            if (importName.contains(";")) {
                importName = importName.replace(";", "");
            }
            importInfoTableModel.setImportName(importName);
            String importModifiers = importDependency.replace(importName, "");
            importModifiers = importModifiers.replace(";", "").trim();
            String importIdString = importName;
            if (!"".equals(importModifiers)) {
                importInfoTableModel.setImportModifiers(importModifiers);
                importIdString = importModifiers + " " + importName;
            }
            String importId = MD5Util.getMD5(importIdString);
            importInfoTableModel.setImportId(importId);
            importInfoTableDao.saveImportInfoToDatabase(importInfoTableModel);
            importDependencySet.add(importId);
        }
        importInfoTableDao = null;
        return importDependencySet;
    }


}
