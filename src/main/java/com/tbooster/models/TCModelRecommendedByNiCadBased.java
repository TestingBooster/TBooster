package com.tbooster.models;

import java.util.List;

/**
  * @Author xxx
  * @Date 2020/8/23 09:37
  */
public class TCModelRecommendedByNiCadBased {

    private double similarity; // similarity measured by NiCad

    private MethodInfoTableModel testTarget;

    private List<TestInfoTableModel> testCases;

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public MethodInfoTableModel getTestTarget() {
        return testTarget;
    }

    public void setTestTarget(MethodInfoTableModel testTarget) {
        this.testTarget = testTarget;
    }

    public List<TestInfoTableModel> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestInfoTableModel> testCases) {
        this.testCases = testCases;
    }
}
