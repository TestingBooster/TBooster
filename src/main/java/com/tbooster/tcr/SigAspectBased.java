package com.tbooster.tcr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.models.TestInfoTableModel;
import com.tbooster.models.TestTendererNeedMethodModel;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/10/21 14:37
 */
public class SigAspectBased {
    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";
    final private static int k = 10; // top k

    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();

    private static List<TestTendererNeedMethodModel> testTendererNeedMethodModelList = new ArrayList<>();

    static {
        String searchCorpusForTBoosterDirectoryPath = experimentDataDirectoryPath + File.separator + "search_corpus_for_SumBased";
        String methodSummaryFilePath = searchCorpusForTBoosterDirectoryPath + File.separator + "method_summary.json";
        File methodSummaryFile = new File(methodSummaryFilePath);
        String jsonString = FileUtil.readFileContentToString(methodSummaryFile);
        JacksonUtil jacksonUtil = new JacksonUtil();
        JavaType javaType = jacksonUtil.getCollectionType(List.class, TestTendererNeedMethodModel.class);
        try {
            testTendererNeedMethodModelList = jacksonUtil.getMapper().readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SigAspectBased sigAspectBased = new SigAspectBased();
        sigAspectBased.run();
        sigAspectBased = null;

        testTendererNeedMethodModelList = null;
        testInfoTableDao = null;
        methodInfoTableDao = null;
        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/26 00:42
     * @author xxx
     */
    public void run() {
        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
        String searchQueryDirectoryPath = pathStringBuffer.toString();
        File searchQueryDirectory = new File(searchQueryDirectoryPath);
        File[] queryDirectoryArray = searchQueryDirectory.listFiles();
        Map<String, Long> queryCostTimeMap = new HashMap<>();
        pathStringBuffer.append(File.separator);
        for (File queryDirectory : queryDirectoryArray) {
            if (queryDirectory.isFile()) {
                continue;
            }
            String queryDirectoryName = queryDirectory.getName();
            System.out.println("--------------- " + queryDirectoryName + " ---------------");
            File[] subFileArray = queryDirectory.listFiles();
            File mutFile = null;
            for (File subFile : subFileArray) {
                String subFileName = subFile.getName();
                if (!subFileName.startsWith("mut_")) {
                    continue;
                }
                mutFile = subFile;
                break;
            }

            String fileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = (new JacksonUtil()).json2Bean(fileContent, MethodInfoTableModel.class);
            String className = methodInfoTableModel.getClassName();
            String methodName = methodInfoTableModel.getMethodName();
            String signature = methodInfoTableModel.getSignature();
            int start = signature.indexOf("(");
            int end = signature.lastIndexOf(")");
            String parameterType = signature.substring(start + 1, end).trim();
            Map<String, Integer> methodIdRankMap = new HashMap<>();

            List<TestTendererNeedMethodModel> methodWithSameCNMNIdList = new ArrayList<>();
            List<TestTendererNeedMethodModel> methodWithSameCNIdList = new ArrayList<>();
            List<TestTendererNeedMethodModel> methodWithSameMNIdList = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            int rank = 0;
            /*
            1) Search for exact match of the query.
             CN + S(MN + PT)
             */
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (testTendererNeedMethodModel.getClassName().equals(className)) {
                    // with same CN
                    if (testTendererNeedMethodModel.getMethodName().equals(methodName)) {
                        // with same CN + MN
                        if (testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                            // with same PT
                            rank++;
                            methodIdRankMap.put(methodId, rank);
                            if (methodIdRankMap.size() == k) {
                                break;
                            }
                        } else {
                            // with same CN + MN
                            methodWithSameCNMNIdList.add(testTendererNeedMethodModel);
                        }
                    } else {
                        // with same CN
                        methodWithSameCNIdList.add(testTendererNeedMethodModel);
                    }
                } else {
                    if (testTendererNeedMethodModel.getMethodName().equals(methodName)) {
                        // with same MN
                        methodWithSameMNIdList.add(testTendererNeedMethodModel);
                    }
                }
            }

            List<String> methodIdFromLevel1FMList = new ArrayList<>();

            /*
            2) Add wildcards to the parameter types.
            CN + MN + *
             */
            if (methodIdRankMap.size() < k && !methodWithSameCNMNIdList.isEmpty()) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNMNIdList) {
                    rank++;
                    methodIdRankMap.put(testTendererNeedMethodModel.getMethodId(), rank);
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                    methodIdFromLevel1FMList.add(testTendererNeedMethodModel.getMethodId());
                }
            }

            /*
            3) Add wildcards to the method names.
            CN + * + PT
             */
            if (methodIdRankMap.size() < k && !methodWithSameCNIdList.isEmpty()) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNIdList) {
                    if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                        continue;
                    }
                    rank++;
                    methodIdRankMap.put(testTendererNeedMethodModel.getMethodId(), rank);
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                    methodIdFromLevel1FMList.add(testTendererNeedMethodModel.getMethodId());
                }
            }

            /*
            4) Add wildcards to the classname.
            * + MN + PT
             */
            if (methodIdRankMap.size() < k && !methodWithSameMNIdList.isEmpty()) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNIdList) {
                    if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                        continue;
                    }
                    rank++;
                    methodIdRankMap.put(testTendererNeedMethodModel.getMethodId(), rank);
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                    methodIdFromLevel1FMList.add(testTendererNeedMethodModel.getMethodId());
                }
            }

            /*
            5) Search only for the class name.
            CN + * + *
             */
            if (methodIdRankMap.size() < k && !methodWithSameCNIdList.isEmpty()) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNIdList) {
                    String methodId = testTendererNeedMethodModel.getMethodId();
                    if (methodIdFromLevel1FMList.contains(methodId)) {
                        continue;
                    }
                    rank++;
                    methodIdRankMap.put(methodId, rank);
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                }
            }

            /*
            6) Search only for the method name.
            * + MN + *
             */
            if (methodIdRankMap.size() < k && !methodWithSameMNIdList.isEmpty()) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNIdList) {
                    String methodId = testTendererNeedMethodModel.getMethodId();
                    if (methodIdFromLevel1FMList.contains(methodId)) {
                        continue;
                    }
                    rank++;
                    methodIdRankMap.put(methodId, rank);
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);
            if (methodIdRankMap.isEmpty()) {
                continue;
            }

            pathStringBuffer.append(queryDirectoryName);

            pathStringBuffer.append(File.separator + "test_cases_recommended_by_SigAspectBased");
            String recommendedTestCasesDirectoryPath = pathStringBuffer.toString();
            File recommendedTestCasesDirectory = new File(recommendedTestCasesDirectoryPath);
            if (!recommendedTestCasesDirectory.exists()) {
                recommendedTestCasesDirectory.mkdir();
            }

            Set<String> methodIdSet = methodIdRankMap.keySet();
            List<String> methodIdList = new ArrayList<>();
            methodIdList.addAll(methodIdSet);

            List<MethodInfoTableModel> similarMethodInfoTableModelList = methodInfoTableDao.searchMethodListByMethodIdList(methodIdList);
            pathStringBuffer.append(File.separator);
            for (MethodInfoTableModel similarMethodInfoTableModel : similarMethodInfoTableModelList) {
                String methodId = similarMethodInfoTableModel.getMethodId();
                int rankIndex = methodIdRankMap.get(methodId);
                String tcRankDirectoryName = "tc_rank_" + rankIndex;
                pathStringBuffer.append(tcRankDirectoryName);
                String tcRankDirectoryPath = pathStringBuffer.toString();
                File tcRankDirectory = new File(tcRankDirectoryPath);
                if (!tcRankDirectory.exists()) {
                    tcRankDirectory.mkdir();
                }
                pathStringBuffer.append(File.separator);

                String testTargetFileName = "tt_" + methodId + ".json";
                pathStringBuffer.append(testTargetFileName);
                String testTargetFilePath = pathStringBuffer.toString();
                String testTargetString = (new JacksonUtil()).bean2Json(similarMethodInfoTableModel);
                FileUtil.writeStringToTargetFile(testTargetString, testTargetFilePath);

                // 移除 tt_xxx.json 文件名
                int testTargetFileNameIndex = pathStringBuffer.indexOf(testTargetFileName);
                pathStringBuffer.replace(testTargetFileNameIndex, pathStringBuffer.length(), "");

                String testCaseIds = similarMethodInfoTableModel.getTestCaseIds();
                testCaseIds = testCaseIds.substring(1, testCaseIds.length() - 1).trim();
                Set<String> testCaseIdSet = new HashSet<>();
                if (testCaseIds.indexOf(",") != -1) {
                    String[] testCaseIdArray = testCaseIds.split(",");
                    for (String testCaseId : testCaseIdArray) {
                        testCaseIdSet.add(testCaseId.trim());
                    }
                } else {
                    testCaseIdSet.add(testCaseIds);
                }
                Map<String, TestInfoTableModel> testCaseIdAndDetailInfoMap = testInfoTableDao.searchTestCaseByTestCaseIdSet(testCaseIdSet);
                Iterator<Map.Entry<String, TestInfoTableModel>> testCaseIterator = testCaseIdAndDetailInfoMap.entrySet().iterator();
                while (testCaseIterator.hasNext()) {
                    Map.Entry<String, TestInfoTableModel> entry = testCaseIterator.next();
                    String testCaseId = entry.getKey();
                    TestInfoTableModel testInfoTableModel = entry.getValue();
                    String testCaseString = (new JacksonUtil()).bean2Json(testInfoTableModel);
                    String testCaseFileName = "tc_" + testCaseId + ".json";
                    pathStringBuffer.append(testCaseFileName);
                    String testCaseFilePath = pathStringBuffer.toString();
                    FileUtil.writeStringToTargetFile(testCaseString, testCaseFilePath);

                    // 移除 tc_xxx.json 文件名
                    int testCaseFileNameIndex = pathStringBuffer.indexOf(testCaseFileName);
                    pathStringBuffer.replace(testCaseFileNameIndex, pathStringBuffer.length(), "");
                }

                // 移除 tc_rank_xxx 目录名
                int tcRankDirectoryNameIndex = pathStringBuffer.indexOf(tcRankDirectoryName);
                pathStringBuffer.replace(tcRankDirectoryNameIndex, pathStringBuffer.length(), "");

                testCaseIdAndDetailInfoMap = null;
                testCaseIdSet = null;
                tcRankDirectory = null;
            }

            // 移除 query_xxx 目录名
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            methodIdList = null;
            methodIdSet = null;
            methodIdRankMap = null;
            similarMethodInfoTableModelList = null;
            methodInfoTableModel = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_SigAspectBased.json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);

        costTimeFilePath = null;
        queryCostTimeMap = null;
        pathStringBuffer = null;
        queryCostTimeMapJsonString = null;
        queryDirectoryArray = null;
        searchQueryDirectoryPath = null;
        searchQueryDirectory = null;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/21 21:27
     * @author xxx
     */
    public List<String> sigBasedSearch(MethodInfoTableModel methodInfoTableModel, int k) {
        String className = methodInfoTableModel.getClassName();
        String methodName = methodInfoTableModel.getMethodName();
        String signature = methodInfoTableModel.getSignature();
        int start = signature.indexOf("(");
        int end = signature.lastIndexOf(")");
        String parameterType = signature.substring(start + 1, end).trim();

        List<String> similarMethodIdList = new ArrayList<>();

        List<TestTendererNeedMethodModel> methodWithSameCNMNList = new ArrayList<>();
        List<TestTendererNeedMethodModel> methodWithSameCNList = new ArrayList<>();
        List<TestTendererNeedMethodModel> methodWithSameMNList = new ArrayList<>();

        // 1) CN + S
        for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
            String methodId = testTendererNeedMethodModel.getMethodId();
            if (testTendererNeedMethodModel.getClassName().equals(className)) {
                if (testTendererNeedMethodModel.getMethodName().equals(methodName)) {
                    if (testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                        similarMethodIdList.add(methodId);
                        if (similarMethodIdList.size() == k) {
                            break;
                        }
                    } else {
                        methodWithSameCNMNList.add(testTendererNeedMethodModel);
                    }
                } else {
                    methodWithSameCNList.add(testTendererNeedMethodModel);
                }
            } else {
                if (testTendererNeedMethodModel.getMethodName().equals(methodName)) {
                    methodWithSameMNList.add(testTendererNeedMethodModel);
                }
            }
        }
        // 2) CN + MN + *
        if (similarMethodIdList.size() < k && !methodWithSameCNMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNMNList) {
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                if (similarMethodIdList.size() == k) {
                    break;
                }
            }
        }
        // 3) CN + * + PT
        if (similarMethodIdList.size() < k && !methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                if (similarMethodIdList.size() == k) {
                    break;
                }
            }
        }
        // 4) * + MN + PT
        if (similarMethodIdList.size() < k && !methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                if (similarMethodIdList.size() == k) {
                    break;
                }
            }
        }
        // 5) CN + * + *
        if (similarMethodIdList.size() < k && !methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (similarMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
                if (similarMethodIdList.size() == k) {
                    break;
                }
            }
        }
        // 6) * + MN + *
        if (similarMethodIdList.size() < k && !methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (similarMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
                if (similarMethodIdList.size() == k) {
                    break;
                }
            }
        }
        return similarMethodIdList;
    }

}
