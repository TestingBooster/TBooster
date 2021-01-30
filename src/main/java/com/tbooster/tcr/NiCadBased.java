package com.tbooster.tcr;

import com.tbooster.dao.ImportInfoTableDao;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.*;
import com.tbooster.utils.*;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/4/22 9:23 PM
  */
public class NiCadBased {

    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";

    final private static int k = 10; // top k

    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static ImportInfoTableDao importInfoTableDao = new ImportInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();

    public static void main(String[] args) {
        NiCadBased niCadBased = new NiCadBased();
        niCadBased.run();

        methodInfoTableDao = null;
        testInfoTableDao = null;
        importInfoTableDao = null;
        experimentDataDirectoryPath = null;

        DBUtil.closeConnection();
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/23 16:21
      * @author xxx
      */
    public void run() {
        String methodSimilarityFileName = "method_similarity.json";
        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
        String searchCorpusForNiCadDirectoryPath = experimentDataDirectoryPath + File.separator + "search_corpus_for_NiCad";
        String searchQueryDirectoryPath = pathStringBuffer.toString();

        Map<String, Long> queryCostTimeMap = new HashMap<>();
        pathStringBuffer.append(File.separator);
        File searchQueryDirectory = new File(searchQueryDirectoryPath);
        File[] queryDirectoryArray = searchQueryDirectory.listFiles();
        for (File queryDirectory : queryDirectoryArray) {
            if (queryDirectory.isFile()) {
                continue;
            }
            String queryDirectoryName = queryDirectory.getName();
            if (!queryDirectoryName.startsWith("query_")) {
                continue;
            }

            System.out.println("---------------- " + queryDirectoryName + " ----------------");
            String methodCodeDirectoryPath = queryDirectory.getAbsolutePath() + File.separator + "method_code";

            long startTime = System.currentTimeMillis();

            int exitValue = NiCad.detectFunctionLevelCloneWithNiCad5cross(methodCodeDirectoryPath, searchCorpusForNiCadDirectoryPath);
            if (exitValue == -1) {
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                continue;
            }

            String xmlFilePath = queryDirectory.getAbsolutePath()
                    + File.separator + "method_code_functions-blind-crossclones"
                    + File.separator + "method_code_functions-blind-crossclones-0.10.xml";
            Map<String, Double> methodIdAndSimilarityMap = NiCad.extractCloneMethodIdAndSimilarityFromXmlFile(xmlFilePath);
            if (methodIdAndSimilarityMap == null) {
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                continue;
            }
            pathStringBuffer.append(queryDirectoryName);
            System.out.println("Clone Method Number: " + methodIdAndSimilarityMap.size());

            LinkedHashMap<String, Double> topKMethodIdAndSimilarityMap = new LinkedHashMap<>();
            Iterator<Map.Entry<String, Double>> iterator = methodIdAndSimilarityMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                count++;
                Map.Entry<String, Double> entry = iterator.next();
                String methodId = entry.getKey();
                double similarity = entry.getValue();
                topKMethodIdAndSimilarityMap.put(methodId, similarity);
                if (count == k) {
                    break;
                }
            }
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);
            methodIdAndSimilarityMap = null;


            pathStringBuffer.append(File.separator + "test_cases_recommended_by_NiCadBased");
            String recommendedTestCasesDirectoryPath = pathStringBuffer.toString();
            File recommendedTestCasesDirectory = new File(recommendedTestCasesDirectoryPath);
            if (!recommendedTestCasesDirectory.exists()) {
                recommendedTestCasesDirectory.mkdir();
            }

            String methodIdSimilarityJsonString = (new JacksonUtil()).bean2Json(topKMethodIdAndSimilarityMap);
            String methodSimilarityFilePath = pathStringBuffer.toString() + File.separator + methodSimilarityFileName;
            FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
            methodSimilarityFilePath = null;
            methodIdSimilarityJsonString = null;

            pathStringBuffer.append(File.separator);
            count = 0;
            iterator = topKMethodIdAndSimilarityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                count++;
                Map.Entry<String, Double> entry = iterator.next();
                String methodId = entry.getKey();
                MethodInfoTableModel testTarget = methodInfoTableDao.searchMethodByMethodId(methodId);

                String tcRankDirectoryName = "tc_rank_" + count;
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
                String testTargetString = (new JacksonUtil()).bean2Json(testTarget);
                FileUtil.writeStringToTargetFile(testTargetString, testTargetFilePath);

                // 移除 tt_xxx.json 文件名
                int testTargetFileNameIndex = pathStringBuffer.indexOf(testTargetFileName);
                pathStringBuffer.replace(testTargetFileNameIndex, pathStringBuffer.length(), "");


                String testCaseIds = testTarget.getTestCaseIds();
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
                    Map.Entry<String, TestInfoTableModel> testCaseEntry = testCaseIterator.next();
                    String testCaseId = testCaseEntry.getKey();
                    TestInfoTableModel testInfoTableModel = testCaseEntry.getValue();
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

            topKMethodIdAndSimilarityMap = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_NiCadBased.json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);
        queryCostTimeMapJsonString = null;
        costTimeFilePath = null;


        queryCostTimeMap = null;
        pathStringBuffer = null;
        queryDirectoryArray = null;
    }

    /**
      * Sort the recommended test cases by their similarities.
      * @param recommendedTestCaseModelList
      * @return
      * @throws
      * @date 2020/4/22 11:04 PM
      * @author xxx
      */
    private List<RecommendedTestCaseModel> sortRecommendedTCBySimilarity(List<RecommendedTestCaseModel> recommendedTestCaseModelList) {
        int count = recommendedTestCaseModelList.size();
        List<RecommendedTestCaseModel> newList = new ArrayList<>(count);
        double maxSimilarity = 0.0;
        int index = 0;
        while (index < count) {
            for (RecommendedTestCaseModel tc : recommendedTestCaseModelList) {
                double similarity = tc.getSimilarity();
                if (maxSimilarity < similarity) {
                    maxSimilarity = similarity;
                }
            }
            for (RecommendedTestCaseModel tc : recommendedTestCaseModelList) {
                if (tc.getSimilarity() == maxSimilarity) {
                    newList.add(tc);
                    recommendedTestCaseModelList.remove(tc);
                    break;
                }
            }
            maxSimilarity = 0.0;
            index++;
        }
        return newList;
    }


    /**
      * Update the set of recommended test cases.
      * @param recommendedTestCaseModelList
      * @param recommendedTestCaseModel
      * @param minSimilarity
      * @return double
      * @date 2020/4/22 10:34 PM
      * @author xxx
      */
    private double updateRecommendedTC(List<RecommendedTestCaseModel> recommendedTestCaseModelList
            , RecommendedTestCaseModel recommendedTestCaseModel, double minSimilarity) {
        double tempSimilarity = 1.0;
        Iterator<RecommendedTestCaseModel> iterator = recommendedTestCaseModelList.iterator();
        // replace the test case that has the minimum similarity with the new recommended test case.
        while (iterator.hasNext()) {
            RecommendedTestCaseModel tc = iterator.next();
            if (tc.getSimilarity() == minSimilarity) {
                iterator.remove();
                break;
            }
        }
        recommendedTestCaseModelList.add(recommendedTestCaseModel);
        // find the new minimum similarity.
        for (RecommendedTestCaseModel tc : recommendedTestCaseModelList) {
            if (tc.getSimilarity() < tempSimilarity) {
                tempSimilarity = tc.getSimilarity();
            }
        }
        return tempSimilarity;
    }


    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/23 09:41
      * @author xxx
      */
    private double updateRecommendedTC(List<TCModelRecommendedByNiCadBased> tcModelRecommendedByNiCadBasedList
            , TCModelRecommendedByNiCadBased tcModelRecommendedByNiCadBased, double minSimilarity) {
        double tempMinSimilarity = 1.0;
        Iterator<TCModelRecommendedByNiCadBased> iterator = tcModelRecommendedByNiCadBasedList.iterator();
        // replace the test case that has the minimum similarity with the new recommended test case.
        while (iterator.hasNext()) {
            TCModelRecommendedByNiCadBased tc = iterator.next();
            if (tc.getSimilarity() == minSimilarity) {
                iterator.remove();
                break;
            }
        }
        tcModelRecommendedByNiCadBasedList.add(tcModelRecommendedByNiCadBased);
        // find the new minimum similarity.
        for (TCModelRecommendedByNiCadBased tc : tcModelRecommendedByNiCadBasedList) {
            if (tc.getSimilarity() < tempMinSimilarity) {
                tempMinSimilarity = tc.getSimilarity();
            }
        }
        return tempMinSimilarity;
    }

    /**
     * Search the methods.
     * @param methodDependencies
     * @return List<MethodInfoTableModel>
     * @date 2020/4/23 3:27 PM
     * @author xxx
     */
    private List<MethodInfoTableModel> searchMethodInfoTableModels(String methodDependencies) {
        List<MethodInfoTableModel> methodInfoTableModelList = null;
        if (methodDependencies.indexOf(",") != -1) {
            List<String> methodIdList = new ArrayList<>();
            String[] methodIdArray = methodDependencies.split(",");
            for (String methodId : methodIdArray) {
                if ("".equals(methodId)) {
                    continue;
                }
                methodIdList.add(methodId);
            }
            if (methodIdList.size() > 0) {
                methodInfoTableModelList = methodInfoTableDao.searchMethodListByMethodIdList(methodIdList);
                methodIdList = null;
            }
        } else {
            String methodId = methodDependencies.trim();
            if (!"".equals(methodId)) {
                MethodInfoTableModel methodInfoTableModel = methodInfoTableDao.searchMethodByMethodId(methodId);
                if (methodInfoTableModel != null) {
                    methodInfoTableModelList = new ArrayList<>(1);
                    methodInfoTableModelList.add(methodInfoTableModel);
                }
            }
        }
        return methodInfoTableModelList;
    }

    /**
     * Search the import strings.
     * @param importDependencies
     * @return List<String>
     * @date 2020/4/23 3:09 PM
     * @author xxx
     */
    private List<String> searchImportStrings(String importDependencies) {
        List<String> importStringList = null;
        if (importDependencies.indexOf(",") != -1) {
            List<String> importIdList = new ArrayList<>();
            String[] importIdArray = importDependencies.split(",");
            for (String importId : importIdArray) {
                if ("".equals(importId)) {
                    continue;
                }
                importIdList.add(importId);
            }
            if (importIdList.size() > 0) {
                importStringList = importInfoTableDao.searchImportStringListByImportIdList(importIdList);
                importIdList = null;
            }
        } else {
            String importId = importDependencies.trim();
            if (!"".equals(importId)) {
                String importString = importInfoTableDao.searchImportStringByImportId(importId);
                if (importString != null) {
                    importStringList = new ArrayList<>(1);
                    importStringList.add(importString);
                }
            }
        }
        return importStringList;
    }

}
