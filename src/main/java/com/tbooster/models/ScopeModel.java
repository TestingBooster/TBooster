package com.tbooster.models;


import com.github.javaparser.ast.expr.Expression;

/**
  * @Author xxx
  * @Date 2020/5/30 9:57 PM
  */
public class ScopeModel {
    private Expression scopeExpression;
    private String expressionType;
    private String scope;
    private boolean isResolved;
    private String scopeType;

    public ScopeModel(Expression scopeExpression) {
        this.scopeExpression = scopeExpression;
    }

    public Expression getScopeExpression() {
        return scopeExpression;
    }

    public void setScopeExpression(Expression scopeExpression) {
        this.scopeExpression = scopeExpression;
    }

    public String getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public String toString() {
        return "ScopeModel{" +
                "scopeExpression=" + scopeExpression +
                ", expressionType='" + expressionType + '\'' +
                ", scope='" + scope + '\'' +
                ", isResolved=" + isResolved +
                ", scopeType='" + scopeType + '\'' +
                '}';
    }
}
