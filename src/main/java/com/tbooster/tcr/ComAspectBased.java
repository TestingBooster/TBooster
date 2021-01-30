package com.tbooster.tcr;

import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;
import com.tbooster.utils.MapUtil;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.File;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/8/23 23:11
 */
public class ComAspectBased {
    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";

    final private static int k = 10; // top k
    private static double threshold = 0.6;

    private static LinkedHashMap<String, String> methodIdCommentKeywordsMap;

    static {
        String searchCorpusForTBoosterDirectoryPath = experimentDataDirectoryPath + File.separator + "search_corpus_for_TBooster";
        String methodCommentKeywordsFilePath = searchCorpusForTBoosterDirectoryPath + File.separator + "method_comment_keywords.json";
        File methodCommentKeywordsFile = new File(methodCommentKeywordsFilePath);
        String jsonString = FileUtil.readFileContentToString(methodCommentKeywordsFile);
        methodIdCommentKeywordsMap = (new JacksonUtil()).json2Bean(jsonString, LinkedHashMap.class);
    }

    public static void main(String[] args) {
        ComAspectBased comAspectBased = new ComAspectBased();
        comAspectBased.run();
        comAspectBased = null;

        methodIdCommentKeywordsMap = null;
        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/24 00:43
     * @author xxx
     */
    public void run() {
        JacksonUtil jacksonUtil = new JacksonUtil();
        String methodCommentSimilarityFileName = "method_similarity.json";
        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
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
            String methodCommentKeywords = methodInfoTableModel.getMethodCommentKeywords();
            if (methodCommentKeywords == null) {
                queryCostTimeMap.put(queryDirectoryName, 0l);
                methodInfoTableModel = null;
                continue;
            }

            pathStringBuffer.append(queryDirectoryName);
            methodCommentKeywords = methodCommentKeywords.substring(1, methodCommentKeywords.length() - 1);
            methodCommentKeywords = methodCommentKeywords.replace(",", "");

            long startTime = System.currentTimeMillis();
            LinkedHashMap<String, Double> topKMethodIdAndSimilarityMap = new LinkedHashMap<>();
            Map<String, Double> methodIdCommentSimilarityMap = searchInCommentCorpus(methodCommentKeywords);
            if (!methodIdCommentSimilarityMap.isEmpty()) {
                methodIdCommentSimilarityMap = MapUtil.sortByValueDescending(methodIdCommentSimilarityMap);
                Iterator<Map.Entry<String, Double>> tempIterator = methodIdCommentSimilarityMap.entrySet().iterator();
                int count = 0;
                while (tempIterator.hasNext()) {
                    count++;
                    Map.Entry<String, Double> entry = tempIterator.next();
                    String methodId = entry.getKey();
                    topKMethodIdAndSimilarityMap.put(methodId, entry.getValue());
                    if (count == k) {
                        break;
                    }
                }
                methodIdCommentSimilarityMap = null;
            }
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);

            if (!topKMethodIdAndSimilarityMap.isEmpty()) {
                pathStringBuffer.append(File.separator + "test_cases_recommended_by_ComBased" + "_" + threshold + "-1.0");
                String recommendedResultDirectoryPath = pathStringBuffer.toString();
                File recommendedTestCasesDirectory = new File(recommendedResultDirectoryPath);
                if (!recommendedTestCasesDirectory.exists()) {
                    recommendedTestCasesDirectory.mkdir();
                }

                // save the similarity information.
                String methodIdSimilarityJsonString = jacksonUtil.bean2Json(topKMethodIdAndSimilarityMap);
                String methodSimilarityFilePath = recommendedResultDirectoryPath
                        + File.separator
                        + methodCommentSimilarityFileName;
                FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
                methodSimilarityFilePath = null;
                methodIdSimilarityJsonString = null;

                // save detail information.
                searchAndDetailInfoFromDatabase(recommendedResultDirectoryPath, topKMethodIdAndSimilarityMap);
            }

            // 移除 query_xxx 目录名
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            topKMethodIdAndSimilarityMap = null;
            methodInfoTableModel = null;
        }

        String queryCostTimeMapJsonString = jacksonUtil.bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_ComBased_" + threshold + "-1.0" + ".json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);

        costTimeFilePath = null;
        queryCostTimeMapJsonString = null;
        queryCostTimeMap = null;
        pathStringBuffer = null;
        queryDirectoryArray = null;

        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/15 10:43
     * @author xxx
     */
    private Map<String, Double> searchInCommentCorpus(String methodCommentKeywords) {
        Iterator<Map.Entry<String, String>> iterator = methodIdCommentKeywordsMap.entrySet().iterator();
        Map<String, Double> methodIdCommentSimilarityMap = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String tempMethodId = entry.getKey();
            String tempMethodCommentKeywords = entry.getValue();
            tempMethodCommentKeywords = tempMethodCommentKeywords.substring(1, tempMethodCommentKeywords.length() - 1);
            tempMethodCommentKeywords = tempMethodCommentKeywords.replace(",", "");
            double similarity =  FuzzySearch.tokenSortRatio(methodCommentKeywords, tempMethodCommentKeywords);
            similarity = similarity / 100.0;
            if (similarity < threshold) {
                continue;
            }
            methodIdCommentSimilarityMap.put(tempMethodId, similarity);
        }
        return methodIdCommentSimilarityMap;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/15 10:53
     * @author xxx
     */
    private void searchAndDetailInfoFromDatabase(String recommendedResultDirectoryPath
            , LinkedHashMap<String,Double> topKMethodIdAndSimilarityMap) {
        Iterator<Map.Entry<String, Double>> iterator = topKMethodIdAndSimilarityMap.entrySet().iterator();
        List<String> methodIdList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            String methodId = entry.getKey();
            methodIdList.add(methodId);
        }
        PostProcessing.saveTopKTCDetailsToRecommendedResultDirectory(methodIdList, recommendedResultDirectoryPath);
        methodIdList = null;
        topKMethodIdAndSimilarityMap = null;
    }

    /**
     *
     * @param methodCommentKeywords
     * @return Map<String, Double>
     * @date 2020/9/10 11:07
     * @author xxx
     */
    public LinkedHashMap<String, Double> searchForTopKByCommentKeywords(String methodCommentKeywords
            , Collection<String> filterTestTargetIdList, int number) {
        LinkedHashMap<String, Double> topKMethodIdAndCommentSimilarityMap = new LinkedHashMap<>();
        // searching process
        Map<String, Double> methodIdCommentSimilarityMap = searchInCommentCorpus(methodCommentKeywords
                , filterTestTargetIdList);
        int count = methodIdCommentSimilarityMap.size();
        if (count == 0) {
            return topKMethodIdAndCommentSimilarityMap;
        }
        // sorting process
        methodIdCommentSimilarityMap = MapUtil.sortByValueDescending(methodIdCommentSimilarityMap);
        if (count <= number) {
            topKMethodIdAndCommentSimilarityMap.putAll(methodIdCommentSimilarityMap);
            return topKMethodIdAndCommentSimilarityMap;
        }
        count = 0;
        Iterator<Map.Entry<String, Double>> iterator = methodIdCommentSimilarityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            topKMethodIdAndCommentSimilarityMap.put(entry.getKey(), entry.getValue());
            count++;
            if (count == number) {
                break;
            }
        }
        methodIdCommentSimilarityMap = null;
        return topKMethodIdAndCommentSimilarityMap;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/15 10:43
     * @author xxx
     */
    private Map<String, Double> searchInCommentCorpus(String methodCommentKeywords
            , Collection<String> filterTestTargetIdList) {
        Iterator<Map.Entry<String, String>> iterator = methodIdCommentKeywordsMap.entrySet().iterator();
        Map<String, Double> methodIdCommentSimilarityMap = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String tempMethodId = entry.getKey();
            if (filterTestTargetIdList.contains(tempMethodId)) {
                continue;
            }
            String tempMethodCommentKeywords = entry.getValue();
            tempMethodCommentKeywords = tempMethodCommentKeywords.substring(1, tempMethodCommentKeywords.length() - 1);
            tempMethodCommentKeywords = tempMethodCommentKeywords.replace(",", "");
            double similarity =  FuzzySearch.tokenSortRatio(methodCommentKeywords, tempMethodCommentKeywords);
            similarity = similarity / 100.0;
            if (similarity < threshold) {
                continue;
            }
            methodIdCommentSimilarityMap.put(tempMethodId, similarity);
        }
        return methodIdCommentSimilarityMap;
    }

    /**
     *
     * @param methodCommentKeywords
     * @return Map<String, Double>
     * @date 2020/9/10 11:07
     * @author xxx
     */
    public LinkedHashMap<String, Double> searchForTopKByCommentKeywords(String methodCommentKeywords) {
        LinkedHashMap<String, Double> topKMethodIdAndCommentSimilarityMap = new LinkedHashMap<>();

        // searching process
        Map<String, Double> methodIdCommentSimilarityMap = searchInCommentCorpus(methodCommentKeywords);
        int count = methodIdCommentSimilarityMap.size();
        if (count == 0) {
            return topKMethodIdAndCommentSimilarityMap;
        }

        // sorting process
        methodIdCommentSimilarityMap = MapUtil.sortByValueDescending(methodIdCommentSimilarityMap);
        if (count <= k) {
            topKMethodIdAndCommentSimilarityMap.putAll(methodIdCommentSimilarityMap);
        } else {
            int index = 0;
            Iterator<Map.Entry<String, Double>> commentSimilarityIterator = methodIdCommentSimilarityMap.entrySet().iterator();
            while (commentSimilarityIterator.hasNext()) {
                index++;
                Map.Entry<String, Double> entry = commentSimilarityIterator.next();
                double commentSimilarity = entry.getValue();
                topKMethodIdAndCommentSimilarityMap.put(entry.getKey(), commentSimilarity);
                if (index == k) {
                    break;
                }
            }
        }
        methodIdCommentSimilarityMap = null;
        return topKMethodIdAndCommentSimilarityMap;
    }


}
