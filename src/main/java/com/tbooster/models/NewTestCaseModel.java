package com.tbooster.models;

import java.util.List;

/**
  * @Author xxx
  * @Date 2020/8/4 3:04 PM
  */
public class NewTestCaseModel {
    private String packageName;
    private String className;
    private List<String> classAnnotationList;
    private List<String> extendedTypeList;
    private List<String> implementedTypeList;
    private String testMethodComment;
    private String testMethodName;
    private String extendedSignature;
    private String testMethodCode;
    private List<String> testTargetList;
    private List<String> initializerDependencyList;
    private List<String> enumEntryDependencyList;
    private List<String> importDependencyList;
    private List<String> variableDependencyList;
    private List<String> methodDependencyList;
    private String beforeClassMethod;
    private String beforeMethod;
    private String afterMethod;
    private String afterClassMethod;

    private int testFramework;// JUnit:1, :TestNG:2;
    private int junitVersion; // 3, 4, 5;
    private int assertFramework; // JUnit:1, AssertJ:2, Truth:3;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getClassAnnotationList() {
        return classAnnotationList;
    }

    public void setClassAnnotationList(List<String> classAnnotationList) {
        this.classAnnotationList = classAnnotationList;
    }

    public List<String> getExtendedTypeList() {
        return extendedTypeList;
    }

    public void setExtendedTypeList(List<String> extendedTypeList) {
        this.extendedTypeList = extendedTypeList;
    }

    public List<String> getImplementedTypeList() {
        return implementedTypeList;
    }

    public void setImplementedTypeList(List<String> implementedTypeList) {
        this.implementedTypeList = implementedTypeList;
    }

    public String getTestMethodComment() {
        return testMethodComment;
    }

    public void setTestMethodComment(String testMethodComment) {
        this.testMethodComment = testMethodComment;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public void setTestMethodName(String testMethodName) {
        this.testMethodName = testMethodName;
    }

    public String getExtendedSignature() {
        return extendedSignature;
    }

    public void setExtendedSignature(String extendedSignature) {
        this.extendedSignature = extendedSignature;
    }

    public String getTestMethodCode() {
        return testMethodCode;
    }

    public void setTestMethodCode(String testMethodCode) {
        this.testMethodCode = testMethodCode;
    }

    public List<String> getTestTargetList() {
        return testTargetList;
    }

    public void setTestTargetList(List<String> testTargetList) {
        this.testTargetList = testTargetList;
    }

    public List<String> getInitializerDependencyList() {
        return initializerDependencyList;
    }

    public void setInitializerDependencyList(List<String> initializerDependencyList) {
        this.initializerDependencyList = initializerDependencyList;
    }

    public List<String> getEnumEntryDependencyList() {
        return enumEntryDependencyList;
    }

    public void setEnumEntryDependencyList(List<String> enumEntryDependencyList) {
        this.enumEntryDependencyList = enumEntryDependencyList;
    }

    public List<String> getImportDependencyList() {
        return importDependencyList;
    }

    public void setImportDependencyList(List<String> importDependencyList) {
        this.importDependencyList = importDependencyList;
    }

    public List<String> getVariableDependencyList() {
        return variableDependencyList;
    }

    public void setVariableDependencyList(List<String> variableDependencyList) {
        this.variableDependencyList = variableDependencyList;
    }

    public List<String> getMethodDependencyList() {
        return methodDependencyList;
    }

    public void setMethodDependencyList(List<String> methodDependencyList) {
        this.methodDependencyList = methodDependencyList;
    }

    public String getBeforeMethod() {
        return beforeMethod;
    }

    public void setBeforeMethod(String beforeMethod) {
        this.beforeMethod = beforeMethod;
    }

    public String getAfterMethod() {
        return afterMethod;
    }

    public void setAfterMethod(String afterMethod) {
        this.afterMethod = afterMethod;
    }

    public int getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(int testFramework) {
        this.testFramework = testFramework;
    }

    public int getJunitVersion() {
        return junitVersion;
    }

    public void setJunitVersion(int junitVersion) {
        this.junitVersion = junitVersion;
    }

    public int getAssertFramework() {
        return assertFramework;
    }

    public void setAssertFramework(int assertFramework) {
        this.assertFramework = assertFramework;
    }

    public String getBeforeClassMethod() {
        return beforeClassMethod;
    }

    public void setBeforeClassMethod(String beforeClassMethod) {
        this.beforeClassMethod = beforeClassMethod;
    }

    public String getAfterClassMethod() {
        return afterClassMethod;
    }

    public void setAfterClassMethod(String afterClassMethod) {
        this.afterClassMethod = afterClassMethod;
    }

    @Override
    public String toString() {
        return "NewTestCaseModel{" +
                "packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", classAnnotationList=" + classAnnotationList +
                ", extendedTypeList=" + extendedTypeList +
                ", implementedTypeList=" + implementedTypeList +
                ", testMethodComment='" + testMethodComment + '\'' +
                ", testMethodName='" + testMethodName + '\'' +
                ", extendedSignature='" + extendedSignature + '\'' +
                ", testMethodCode='" + testMethodCode + '\'' +
                ", testTargetList=" + testTargetList +
                ", initializerDependencyList=" + initializerDependencyList +
                ", enumEntryDependencyList=" + enumEntryDependencyList +
                ", importDependencyList=" + importDependencyList +
                ", variableDependencyList=" + variableDependencyList +
                ", methodDependencyList=" + methodDependencyList +
                ", beforeClassMethod='" + beforeClassMethod + '\'' +
                ", beforeMethod='" + beforeMethod + '\'' +
                ", afterMethod='" + afterMethod + '\'' +
                ", afterClassMethod='" + afterClassMethod + '\'' +
                ", testFramework=" + testFramework +
                ", junitVersion=" + junitVersion +
                ", assertFramework=" + assertFramework +
                '}';
    }
}
