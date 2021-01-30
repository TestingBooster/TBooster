package com.tbooster.models;

import java.util.List;

/**
  * @Author xxx
  * @Date 2020/6/3 11:11 PM
  */
public class NewMethodModel {
    private String packageName;
    private String classOrInterfaceOrEnumName;
    private List<String> classAnnotationList;
    private List<String> extendedTypeList;
    private List<String> implementedTypeList;
    private String methodComment;
    private String methodName;
    private String parameters;
    private String returnType;
    private String methodCode;
    private String signature;
    private String extendedSignature;
    private boolean isConstructor;
    private List<String> initializerDependencyList;
    private List<String> enumEntryDependencyList;
    private List<String> importDependencyList;
    private List<String> globalVariableDependencyList;
    private List<String> modifierList;
    private List<String> methodDependencyList;

    public NewMethodModel() {
    }

    public NewMethodModel(String packageName, String classOrInterfaceOrEnumName, String signature) {
        this.packageName = packageName;
        this.classOrInterfaceOrEnumName = classOrInterfaceOrEnumName;
        this.signature = signature;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassOrInterfaceOrEnumName() {
        return classOrInterfaceOrEnumName;
    }

    public void setClassOrInterfaceOrEnumName(String classOrInterfaceOrEnumName) {
        this.classOrInterfaceOrEnumName = classOrInterfaceOrEnumName;
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodComment() {
        return methodComment;
    }

    public void setMethodComment(String methodComment) {
        this.methodComment = methodComment;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getExtendedSignature() {
        return extendedSignature;
    }

    public void setExtendedSignature(String extendedSignature) {
        this.extendedSignature = extendedSignature;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
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

    public List<String> getGlobalVariableDependencyList() {
        return globalVariableDependencyList;
    }

    public void setGlobalVariableDependencyList(List<String> globalVariableDependencyList) {
        this.globalVariableDependencyList = globalVariableDependencyList;
    }

    public List<String> getModifierList() {
        return modifierList;
    }

    public void setModifierList(List<String> modifierList) {
        this.modifierList = modifierList;
    }

    public List<String> getMethodDependencyList() {
        return methodDependencyList;
    }

    public void setMethodDependencyList(List<String> methodDependencyList) {
        this.methodDependencyList = methodDependencyList;
    }


    @Override
    public String toString() {
        return "NewMethodModel{" +
                "packageName='" + packageName + '\'' +
                ", classOrInterfaceOrEnumName='" + classOrInterfaceOrEnumName + '\'' +
                ", classAnnotationList='" + classAnnotationList + '\'' +
                ", extendedTypeList='" + extendedTypeList + '\'' +
                ", implementedTypeList='" + implementedTypeList + '\'' +
                ", methodComment='" + methodComment + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters='" + parameters + '\'' +
                ", returnType='" + returnType + '\'' +
                ", methodCode='" + methodCode + '\'' +
                ", signature='" + signature + '\'' +
                ", extendedSignature='" + extendedSignature + '\'' +
                ", isConstructor=" + isConstructor +
                '}';
    }
}
