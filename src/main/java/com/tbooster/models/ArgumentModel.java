package com.tbooster.models;

import com.github.javaparser.ast.expr.Expression;

/**
  * @Author xxx
  * @Date 2020/5/30 4:37 PM
  */
public class ArgumentModel {
    private int argumentOrder;
    private Expression argumentExpression;
    private String expressionType;
    private boolean isResolved;
    private String argumentType;

    public ArgumentModel(int argumentOrder) {
        this.argumentOrder = argumentOrder;
    }

    public ArgumentModel(int argumentOrder, Expression argumentExpression) {
        this.argumentOrder = argumentOrder;
        this.argumentExpression = argumentExpression;
    }

    public int getArgumentOrder() {
        return argumentOrder;
    }

    public void setArgumentOrder(int argumentOrder) {
        this.argumentOrder = argumentOrder;
    }

    public Expression getArgumentExpression() {
        return argumentExpression;
    }

    public void setArgumentExpression(Expression argumentExpression) {
        this.argumentExpression = argumentExpression;
    }

    public String getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public String getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(String argumentType) {
        this.argumentType = argumentType;
    }

    @Override
    public String toString() {
        return "ArgumentModel{" +
                "argumentOrder=" + argumentOrder +
                ", argumentExpression=" + argumentExpression +
                ", expressionType='" + expressionType + '\'' +
                ", isResolved=" + isResolved +
                ", argumentType='" + argumentType + '\'' +
                '}';
    }
}
