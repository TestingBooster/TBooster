package com.tbooster.tcr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.tbooster.dao.ImportInfoTableDao;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.*;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/4/23 1:04 AM
  */
public class TestTenderer {
  private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";

    final private static int k = 10; // top k

    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static ImportInfoTableDao importInfoTableDao = new ImportInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();

    private static List<TestTendererNeedMethodModel> testTendererNeedMethodModelList = new ArrayList<>();

    public static void main(String[] args) {
        TestTenderer testTenderer = new TestTenderer();
        testTenderer.run();
        testTenderer = null;

        testTendererNeedMethodModelList = null;
        testInfoTableDao = null;
        methodInfoTableDao = null;
        importInfoTableDao = null;
        DBUtil.closeConnection();
    }

    static {
        String searchCorpusForTBoosterDirectoryPath = experimentDataDirectoryPath + File.separator + "search_corpus_for_TestTenderer";
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
            String returnType = methodInfoTableModel.getReturnType();

            Map<String, Integer> methodIdRankMap = new HashMap<>();
            long startTime = System.currentTimeMillis();
            /*
            1) Search for exact match of the query.
            signature = className + methodName + parameterTypes  + returnType; --> base
             */
            int rank = 0;
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
                String tempClassName = testTendererNeedMethodModel.getClassName();
                if (!tempClassName.equals(className)) {
                    continue;
                }
                String tempSignature = testTendererNeedMethodModel.getSignature();
                if (!tempSignature.equals(signature)) {
                    continue;
                }
                String tempReturnType = testTendererNeedMethodModel.getReturnType();
                if (!tempReturnType.equals(returnType)) {
                    continue;
                }

                String methodId = testTendererNeedMethodModel.getMethodId();
                if (methodIdRankMap.get(methodId) == null) {
                    rank++;
                    methodIdRankMap.put(methodId, rank);
                }
                if (methodIdRankMap.size() == k) {
                    break;
                }
            }

            /*
            2) Add wildcards to the method names.
            signature = className + "xxx" + parameterTypes  + returnType; --> relax 1
             */
            if (methodIdRankMap.size() < k) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
                    String tempClassName = testTendererNeedMethodModel.getClassName();
                    if (!tempClassName.equals(className)) {
                        continue;
                    }
                    String tempReturnType = testTendererNeedMethodModel.getReturnType();
                    if (!tempReturnType.equals(returnType)) {
                        continue;
                    }
                    String tempSignature = testTendererNeedMethodModel.getSignature();
                    String tempMethodName = testTendererNeedMethodModel.getMethodName();
                    tempSignature = tempSignature.replaceFirst(tempMethodName, "");
                    if (!(signature.replaceFirst(methodName, "")).equals(tempSignature)) {
                        continue;
                    }
                    String methodId = testTendererNeedMethodModel.getMethodId();
                    if (methodIdRankMap.get(methodId) == null) {
                        rank++;
                        methodIdRankMap.put(methodId, rank);
                    }
                    if (methodIdRankMap.size() == k) {
                        break;
                    }
                }
            }
            /*
            3) Remove the methods and search only for the classname. --> relax 2
             */
            if (methodIdRankMap.size() < k) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
                String tempClassName = testTendererNeedMethodModel.getClassName();
                if (!tempClassName.equals(className)) {
                    continue;
                }
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (methodIdRankMap.get(methodId) == null) {
                    rank++;
                    methodIdRankMap.put(methodId, rank);
                }
                if (methodIdRankMap.size() == k) {
                    break;
                }
            }
            }

            /*
            4) Add wildcards to the classname.
            signature = "xxx" + methodName + parameterTypes  + returnType; --> relax 3
             */
            if (methodIdRankMap.size() < k) {
                for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
                    String tempSignature = testTendererNeedMethodModel.getSignature();
                    if (!tempSignature.equals(signature)) {
                        continue;
                    }
                    String tempReturnType = testTendererNeedMethodModel.getReturnType();
                    if (!tempReturnType.equals(returnType)) {
                        continue;
                    }

                    String methodId = testTendererNeedMethodModel.getMethodId();
                    if (methodIdRankMap.get(methodId) == null) {
                        rank++;
                        methodIdRankMap.put(methodId, rank);
                    }
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


            pathStringBuffer.append(File.separator + "test_cases_recommended_by_TestTenderer");
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


                    int testCaseFileNameIndex = pathStringBuffer.indexOf(testCaseFileName);
                    pathStringBuffer.replace(testCaseFileNameIndex, pathStringBuffer.length(), "");
                }


                int tcRankDirectoryNameIndex = pathStringBuffer.indexOf(tcRankDirectoryName);
                pathStringBuffer.replace(tcRankDirectoryNameIndex, pathStringBuffer.length(), "");

                testCaseIdAndDetailInfoMap = null;
                testCaseIdSet = null;
                tcRankDirectory = null;
            }


            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            methodIdList = null;
            methodIdSet = null;
            methodIdRankMap = null;
            similarMethodInfoTableModelList = null;
            methodInfoTableModel = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_TestTenderer.json");
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
}
