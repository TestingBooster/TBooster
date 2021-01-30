package com.tbooster.models;

import com.github.javaparser.ast.expr.Expression;

import java.util.List;

/**
  * @Author xxx
  * @Date 2020/5/30 4:57 PM
  */
public class MethodCallModel {
    private String callStatement;
    private Expression methodCallExpression;
    private String methodName;
    private boolean isConstructorCall;
    private boolean isResolved;
    private ScopeModel scopeModel;
    private List<ArgumentModel> argumentModelList;
    private String extendedSignature;
    private String returnType;
    private boolean isProductionMethodCall;
    private String whereAppears; // "NotInMethod", "InMethod", "InTestMethod", "InBeforeOrAfterMethod";

    public String getWhereAppears() {
        return whereAppears;
    }

    public void setWhereAppears(String whereAppears) {
        this.whereAppears = whereAppears;
    }

    public Expression getMethodCallExpression() {
        return methodCallExpression;
    }

    public void setMethodCallExpression(Expression methodCallExpression) {
        this.methodCallExpression = methodCallExpression;
    }

    public boolean isProductionMethodCall() {
        return isProductionMethodCall;
    }

    public void setProductionMethodCall(boolean productionMethodCall) {
        isProductionMethodCall = productionMethodCall;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public boolean isConstructorCall() {
        return isConstructorCall;
    }

    public void setConstructorCall(boolean constructorCall) {
        isConstructorCall = constructorCall;
    }

    public MethodCallModel(String callStatement) {
        this.callStatement = callStatement;
    }

    public MethodCallModel(String callStatement, String methodName) {
        this.callStatement = callStatement;
        this.methodName = methodName;
    }

    public String getCallStatement() {
        return callStatement;
    }

    public void setCallStatement(String callStatement) {
        this.callStatement = callStatement;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ScopeModel getScopeModel() {
        return scopeModel;
    }

    public void setScopeModel(ScopeModel scopeModel) {
        this.scopeModel = scopeModel;
    }

    public List<ArgumentModel> getArgumentModelList() {
        return argumentModelList;
    }

    public void setArgumentModelList(List<ArgumentModel> argumentModelList) {
        this.argumentModelList = argumentModelList;
    }

    public String getExtendedSignature() {
        return extendedSignature;
    }

    public void setExtendedSignature(String extendedSignature) {
        this.extendedSignature = extendedSignature;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    @Override
    public String toString() {
        return "MethodCallModel{" +
                "callStatement='" + callStatement + '\'' +
                ", methodName='" + methodName + '\'' +
                ", isConstructorCall=" + isConstructorCall +
                ", isResolved=" + isResolved +
                ", scopeModel=" + scopeModel +
                ", argumentModelList=" + argumentModelList +
                ", extendedSignature='" + extendedSignature + '\'' +
                ", returnType='" + returnType + '\'' +
                ", isProductionMethodCall=" + isProductionMethodCall +
                '}';
    }
}
