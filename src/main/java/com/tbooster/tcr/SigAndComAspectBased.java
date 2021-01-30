package com.tbooster.tcr;

import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/9/1 23:18
 */
public class SigAndComAspectBased {

    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";
    final private static int k = 10; // top k


    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();
    private static JacksonUtil jacksonUtil = new JacksonUtil();


    public static void main(String[] args) {
        SigAndComAspectBased sigAndComAspectBased = new SigAndComAspectBased();
        sigAndComAspectBased.run();
        sigAndComAspectBased = null;


        jacksonUtil = null;
        methodInfoTableDao = null;
        testInfoTableDao = null;

        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/15 16:10
     * @author xxx
     */
    private void run() {
        String methodSimilarityFileName = "method_similarity.json";
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
//            if (!"query_56".equals(queryDirectoryName)) {
//                continue;
//            }
            System.out.println("--------------- " + queryDirectoryName + " ---------------");
            pathStringBuffer.append(queryDirectoryName);

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

            // query =: <signature, keywordSet>
            String mutFileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = (new JacksonUtil()).json2Bean(mutFileContent, MethodInfoTableModel.class);
            String methodCommentKeywords = methodInfoTableModel.getMethodCommentKeywords();

            // begin search
            long startTime = System.currentTimeMillis();
            if (methodCommentKeywords == null) {
                // no keywords
                // degenerate from SumAndComBased to SumBased.
                SigAspectBased sigAspectBased = new SigAspectBased();
                List<String> methodIdListFromSumBasedSearch = sigAspectBased.sigBasedSearch(methodInfoTableModel, k);
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                if (!methodIdListFromSumBasedSearch.isEmpty()) {
                    pathStringBuffer.append(File.separator + "test_cases_recommended_by_SigAndComAspectBased");
                    String recommendedTestCasesDirectoryPath = pathStringBuffer.toString();
                    File recommendedTestCasesDirectory = new File(recommendedTestCasesDirectoryPath);
                    if (!recommendedTestCasesDirectory.exists()) {
                        recommendedTestCasesDirectory.mkdir();
                    }
                    PostProcessing.saveTopKTCDetailsToRecommendedResultDirectory(methodIdListFromSumBasedSearch
                            , recommendedTestCasesDirectoryPath);
                }
                methodIdListFromSumBasedSearch = null;
                sigAspectBased = null;
            } else {
                // Step 1: SumBasedSearch.
                SigAspectBased sigAspectBased = new SigAspectBased();
                List<String> methodIdListFromSumBasedSearch = sigAspectBased.sigBasedSearch(methodInfoTableModel, k);
                sigAspectBased = null;
                int methodIdListFromSumBasedSearchSize = methodIdListFromSumBasedSearch.size();
                if (methodIdListFromSumBasedSearchSize == k) {
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    queryCostTimeMap.put(queryDirectoryName, costTime);
                    pathStringBuffer.append(File.separator + "test_cases_recommended_by_SigAndComAspectBased");
                    String recommendedTestCasesDirectoryPath = pathStringBuffer.toString();
                    File recommendedTestCasesDirectory = new File(recommendedTestCasesDirectoryPath);
                    if (!recommendedTestCasesDirectory.exists()) {
                        recommendedTestCasesDirectory.mkdir();
                    }
                    PostProcessing.saveTopKTCDetailsToRecommendedResultDirectory(methodIdListFromSumBasedSearch
                            , recommendedTestCasesDirectoryPath);
                } else {
                    int gap = k - methodIdListFromSumBasedSearchSize;
                    // Step 2: ComBasedSearch.
                    ComAspectBased comAspectBased = new ComAspectBased();
                    methodCommentKeywords = methodCommentKeywords.substring(1, methodCommentKeywords.length() - 1);
                    methodCommentKeywords = methodCommentKeywords.replace(",", "");
                    // full search
                    LinkedHashMap<String, Double> topKMethodIdAndCommentSimilarityMap = comAspectBased.searchForTopKByCommentKeywords(methodCommentKeywords
                            , methodIdListFromSumBasedSearch, gap);

                    comAspectBased = null;
                    // Step 3 : Merge
                    LinkedHashMap<String, Double> recommendMethodIdAndComSimMap = new LinkedHashMap<>();
                    for (String methodId : methodIdListFromSumBasedSearch) {
                        recommendMethodIdAndComSimMap.put(methodId, 0.0);
                    }
                    recommendMethodIdAndComSimMap.putAll(topKMethodIdAndCommentSimilarityMap);

                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    queryCostTimeMap.put(queryDirectoryName, costTime);

                    /*
                    Step 4: load details
                     */
                    if (!recommendMethodIdAndComSimMap.isEmpty()) {
                        pathStringBuffer.append(File.separator + "test_cases_recommended_by_SigAndComAspectBased");
                        String recommendedResultDirectoryPath = pathStringBuffer.toString();
                        File recommendedResultDirectory = new File(recommendedResultDirectoryPath);
                        if (!recommendedResultDirectory.exists()) {
                            recommendedResultDirectory.mkdir();
                        }

                        // save the similarity information.
                        String methodIdSimilarityJsonString = (new JacksonUtil()).bean2Json(recommendMethodIdAndComSimMap);
                        String methodSimilarityFilePath = recommendedResultDirectoryPath
                                + File.separator
                                + methodSimilarityFileName;

                        FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
                        methodSimilarityFilePath = null;
                        methodIdSimilarityJsonString = null;

                        // save detail information.
                        searchAndDetailInfoFromDatabase(recommendedResultDirectoryPath, recommendMethodIdAndComSimMap);
                    }
                    recommendMethodIdAndComSimMap = null;
                }

                methodIdListFromSumBasedSearch = null;
            }
            // 移除 query_xxx 目录名
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            methodInfoTableModel = null;
            methodCommentKeywords = null;
            methodInfoTableModel = null;
            mutFileContent = null;
            subFileArray = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_SigAndComAspectBased.json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);

        pathStringBuffer = null;
        queryCostTimeMap = null;
        queryDirectoryArray = null;
        searchQueryDirectory = null;
        searchQueryDirectoryPath = null;
        methodSimilarityFileName = null;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/2 09:57
     * @author xxx
     */
    private void searchAndDetailInfoFromDatabase(String recommendedResultDirectoryPath
            , Map<String, Double> recommendMethodIdAndComSimMap) {
        Iterator<Map.Entry<String, Double>> iterator = recommendMethodIdAndComSimMap.entrySet().iterator();
        List<String> methodIdList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            String methodId = entry.getKey();
            methodIdList.add(methodId);
        }
        PostProcessing.saveTopKTCDetailsToRecommendedResultDirectory(methodIdList, recommendedResultDirectoryPath);
        methodIdList = null;
        recommendMethodIdAndComSimMap = null;
    }
}
