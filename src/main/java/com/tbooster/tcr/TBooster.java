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
public class TBooster {

    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";
    final private static int k = 10; // top k


    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();
    private static JacksonUtil jacksonUtil = new JacksonUtil();

    public static void main(String[] args) {
        TBooster tBooster = new TBooster();
        tBooster.run();
        tBooster = null;

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
     * @date 2020/9/15 16:06
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

        SigAndCodAspectBased sigAndCodAspectBased = new SigAndCodAspectBased();
        ComAspectBased comAspectBased = new ComAspectBased();
        CodAspectBased codAspectBased = new CodAspectBased();


        for (File queryDirectory : queryDirectoryArray) {
            if (queryDirectory.isFile()) {
                continue;
            }
            String queryDirectoryName = queryDirectory.getName();
//            if (!"query_111".equals(queryDirectoryName)) {
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

            // query =: <method_summary, keywordSet>
            String mutFileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = (new JacksonUtil()).json2Bean(mutFileContent, MethodInfoTableModel.class);
            String methodCommentKeywords = methodInfoTableModel.getMethodCommentKeywords();
            String methodCode = methodInfoTableModel.getMethodCode();

            Map<String, Double> topKMethodIdAndCodSimMap = new LinkedHashMap<>();


            // begin search
            long startTime = System.currentTimeMillis();
            // Step 1: SumBasedSearch.
            Map<String, Double> topKMethodWithMSIdAndCodSimMap = sigAndCodAspectBased.sumBasedSearchAndEHSortByCodSim(methodInfoTableModel, methodCode);
            int topKMethodWithMSIdAndCodSimMapSize = topKMethodWithMSIdAndCodSimMap.size();
            if (topKMethodWithMSIdAndCodSimMapSize == k) {
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                topKMethodIdAndCodSimMap = topKMethodWithMSIdAndCodSimMap;
            } else {
                // < k
                if (methodCommentKeywords == null) {
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    queryCostTimeMap.put(queryDirectoryName, costTime);
                    if (topKMethodWithMSIdAndCodSimMapSize > 0) {
                        topKMethodIdAndCodSimMap = topKMethodWithMSIdAndCodSimMap;
                    }
                } else {
                    int gap = k - topKMethodWithMSIdAndCodSimMapSize;
                    // Step 2: ComBasedSearch.
                    methodCommentKeywords = methodCommentKeywords.substring(1, methodCommentKeywords.length() - 1);
                    methodCommentKeywords = methodCommentKeywords.replace(",", "");
                    LinkedHashMap<String, Double> topKMethodIdAndComSimMap;
                    if (topKMethodWithMSIdAndCodSimMapSize == 0) {
                        topKMethodIdAndComSimMap = comAspectBased.searchForTopKByCommentKeywords(methodCommentKeywords
                                , new ArrayList<>(), gap);
                    } else {
                        topKMethodIdAndCodSimMap.putAll(topKMethodWithMSIdAndCodSimMap);
                        Set<String> methodISet = topKMethodIdAndCodSimMap.keySet();
                        topKMethodIdAndComSimMap = comAspectBased.searchForTopKByCommentKeywords(methodCommentKeywords
                                , methodISet, gap);
                    }

                    if (!topKMethodIdAndComSimMap.isEmpty()) {
                        // sort by code similarity
                        Map<String, Double> methodWithSimComIdAndCodSimMap = codAspectBased.sortByCodSim(topKMethodIdAndComSimMap, methodCode);
                        topKMethodIdAndCodSimMap.putAll(methodWithSimComIdAndCodSimMap);
                    }
                    long endTime = System.currentTimeMillis();
                    long costTime = endTime - startTime;
                    queryCostTimeMap.put(queryDirectoryName, costTime);
                }
            }

            /*
            Step 3: load details
            */
            if (!topKMethodIdAndCodSimMap.isEmpty()) {
                pathStringBuffer.append(File.separator + "test_cases_recommended_by_TBooster");
                String recommendedResultDirectoryPath = pathStringBuffer.toString();
                File recommendedResultDirectory = new File(recommendedResultDirectoryPath);
                if (!recommendedResultDirectory.exists()) {
                    recommendedResultDirectory.mkdir();
                }

                // save the similarity information.
                String methodIdSimilarityJsonString = (new JacksonUtil()).bean2Json(topKMethodIdAndCodSimMap);
                String methodSimilarityFilePath = recommendedResultDirectoryPath
                        + File.separator
                        + methodSimilarityFileName;

                FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
                methodSimilarityFilePath = null;
                methodIdSimilarityJsonString = null;

                // save detail information.
                searchAndDetailInfoFromDatabase(recommendedResultDirectoryPath, topKMethodIdAndCodSimMap);
            }
            topKMethodIdAndCodSimMap = null;
            // remove query_xxx
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            methodInfoTableModel = null;
            methodCommentKeywords = null;
            methodInfoTableModel = null;
            mutFileContent = null;
            subFileArray = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_TBooster.json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);

        sigAndCodAspectBased = null;
        comAspectBased = null;
        codAspectBased = null;
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
            , Map<String, Double> recommendMethodIdAndCodSimMap) {
        Iterator<Map.Entry<String, Double>> iterator = recommendMethodIdAndCodSimMap.entrySet().iterator();
        List<String> methodIdList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            String methodId = entry.getKey();
            methodIdList.add(methodId);
        }
        PostProcessing.saveTopKTCDetailsToRecommendedResultDirectory(methodIdList, recommendedResultDirectoryPath);
        methodIdList = null;
        recommendMethodIdAndCodSimMap = null;
    }

}
