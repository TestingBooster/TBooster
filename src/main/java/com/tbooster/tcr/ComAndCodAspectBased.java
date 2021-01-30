package com.tbooster.tcr;

import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/9/21 14:30
 */
public class ComAndCodAspectBased {
    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";

    final private static int k = 10; // top k

    public static void main(String[] args) {
        ComAndCodAspectBased comAndCodAspectBased = new ComAndCodAspectBased();
        comAndCodAspectBased.run();
        comAndCodAspectBased = null;

        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/21 11:30
     * @author xxx
     */
    private void run() {
        String methodSimilarityFileName = "method_similarity.json";
        JacksonUtil jacksonUtil = new JacksonUtil();
        Map<String, Long> queryCostTimeMap = new HashMap<>();

        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
        String searchQueryDirectoryPath = pathStringBuffer.toString();

        ComAspectBased comAspectBased = new ComAspectBased();
        CodAspectBased codAspectBased = new CodAspectBased();

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
//            if (!"query_2".equals(queryDirectoryName)) {
//                continue;
//            }

            System.out.println("---------------- " + queryDirectoryName + " ----------------");

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

            // query =: <com, cod>
            String mutFileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = jacksonUtil.json2Bean(mutFileContent, MethodInfoTableModel.class);
            String methodCommentKeywords = methodInfoTableModel.getMethodCommentKeywords();
            String methodCode = methodInfoTableModel.getMethodCode();

            long startTime = System.currentTimeMillis();
            if (methodCommentKeywords == null) {
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                continue;
            }
            // Step 1: ComBasedSearch.
            methodCommentKeywords = methodCommentKeywords.substring(1, methodCommentKeywords.length() - 1);
            methodCommentKeywords = methodCommentKeywords.replace(",", "");
            Map<String, Double> topKMethodIdAndComSimMap = comAspectBased.searchForTopKByCommentKeywords(methodCommentKeywords);
            if (topKMethodIdAndComSimMap.isEmpty()) {
                long endTime = System.currentTimeMillis();
                long costTime = endTime - startTime;
                queryCostTimeMap.put(queryDirectoryName, costTime);
                continue;
            }

            // Step 2: sort by code similarity
            Map<String, Double> topKMethodIdAndCodSimMap = codAspectBased.sortByCodSim(topKMethodIdAndComSimMap, methodCode);
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);

            pathStringBuffer.append(queryDirectoryName);

            // Step 3: load details
            if (!topKMethodIdAndCodSimMap.isEmpty()) {
                pathStringBuffer.append(File.separator + "test_cases_recommended_by_ComAndCodAspectBased");
                String recommendedResultDirectoryPath = pathStringBuffer.toString();
                File recommendedResultDirectory = new File(recommendedResultDirectoryPath);
                if (!recommendedResultDirectory.exists()) {
                    recommendedResultDirectory.mkdir();
                }

                // save the similarity information.
                String methodIdSimilarityJsonString = jacksonUtil.bean2Json(topKMethodIdAndCodSimMap);
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
            topKMethodIdAndComSimMap = null;

            // remove query_xxx
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            methodInfoTableModel = null;
            methodCode = null;
            methodInfoTableModel = null;
            mutFileContent = null;
            subFileArray = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_ComAndCodAspectBased.json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);

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
     * @date 2020/9/21 15:20
     * @author xxx
     */
    private void searchAndDetailInfoFromDatabase(String recommendedResultDirectoryPath
            , Map<String,Double> recommendMethodIdAndCodSimMap) {
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
