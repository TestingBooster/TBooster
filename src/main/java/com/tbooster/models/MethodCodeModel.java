package com.tbooster.models;

/**
  * @Author xxx
  * @Date 2020/8/24 17:15
  */
public class MethodCodeModel {
    private String methodId;
    private String code;
    private int lines;

    public MethodCodeModel() {
    }

    public MethodCodeModel(String methodId, String code, int lines) {
        this.methodId = methodId;
        this.code = code;
        this.lines = lines;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "MethodCodeModel{" +
                "methodId='" + methodId + '\'' +
                ", code='" + code + '\'' +
                ", lines='" + lines + '\'' +
                '}';
    }
}
