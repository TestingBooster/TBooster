package com.tbooster.models;

import java.util.List;

/**
  * @Author xxx
  * @Date 2020/6/27 11:04 AM
  */
public class ExtendedSignatureComponentModel {

    private String extendedSignature;
    private String packageName;
    private String classOrInterfaceOrEnumName;
    private String innerClassOrInterfaceOrEnumName;
    private String returnType;
    private String methodName;
    private List<String> parameterTypeList;

    public ExtendedSignatureComponentModel(String extendedSignature, String packageName
            , String classOrInterfaceOrEnumName
            , String innerClassOrInterfaceOrEnumName
            , String returnType, String methodName, List<String> parameterTypeList) {
        this.extendedSignature = extendedSignature;
        this.packageName = packageName;
        this.classOrInterfaceOrEnumName = classOrInterfaceOrEnumName;
        this.innerClassOrInterfaceOrEnumName = innerClassOrInterfaceOrEnumName;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterTypeList = parameterTypeList;
    }

    public String getExtendedSignature() {
        return extendedSignature;
    }

    public void setExtendedSignature(String extendedSignature) {
        this.extendedSignature = extendedSignature;
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

    public String getInnerClassOrInterfaceOrEnumName() {
        return innerClassOrInterfaceOrEnumName;
    }

    public void setInnerClassOrInterfaceOrEnumName(String innerClassOrInterfaceOrEnumName) {
        this.innerClassOrInterfaceOrEnumName = innerClassOrInterfaceOrEnumName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParameterTypeList() {
        return parameterTypeList;
    }

    public void setParameterTypeList(List<String> parameterTypeList) {
        this.parameterTypeList = parameterTypeList;
    }

    @Override
    public String toString() {
        return "ExtendedSignatureBasicInfoModel{" +
                "packageName='" + packageName + '\'' +
                ", classOrInterfaceOrEnumName='" + classOrInterfaceOrEnumName + '\'' +
                ", innerClassOrInterfaceOrEnumName='" + innerClassOrInterfaceOrEnumName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypeList=" + parameterTypeList +
                '}';
    }
}
