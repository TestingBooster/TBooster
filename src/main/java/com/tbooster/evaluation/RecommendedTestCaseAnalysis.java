package com.tbooster.evaluation;

import com.tbooster.models.TestInfoTableModel;
import com.tbooster.models.TestSimilarityModel;
import com.tbooster.utils.CodeUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.File;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/8/25 15:30
  */
public class RecommendedTestCaseAnalysis {

    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";


    public static void main(String[] args) {

//        String recommendedTCDirectoryName = "test_cases_recommended_by_SigAspectBased";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_ComAspectBased";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_CodAspectBased";

//        String recommendedTCDirectoryName = "test_cases_recommended_by_SigAndComAspectBased";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_SigAndCodAspectBased";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_ComAndCodAspectBased";

        String recommendedTCDirectoryName = "test_cases_recommended_by_TBooster";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_TestTenderer";
//        String recommendedTCDirectoryName = "test_cases_recommended_by_NiCadBased";


        RecommendedTestCaseAnalysis rtca = new RecommendedTestCaseAnalysis();
        rtca.analyzeSimilarityBetweenRecommendedTCAndGroundTruthTC(recommendedTCDirectoryName);
        rtca = null;
        experimentDataDirectoryPath = null;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2020/8/25 22:29
      * @author xxx
      */
    private void analyzeSimilarityBetweenRecommendedTCAndGroundTruthTC(String recommendedTCDirectoryName) {
        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
        String searchQueryDirectoryPath = pathStringBuffer.toString();

        pathStringBuffer.append(File.separator);
        File searchQueryDirectory = new File(searchQueryDirectoryPath);
        File[] queryDirectoryArray = searchQueryDirectory.listFiles();
        int count = 0;
        JacksonUtil jacksonUtil = new JacksonUtil();
        for (File queryDirectory : queryDirectoryArray) {
            if (queryDirectory.isFile()) {
                continue;
            }
            String queryDirectoryName = queryDirectory.getName();
            if (!queryDirectoryName.startsWith("query_")) {
                continue;
            }
//            if (!"query_63".equals(queryDirectoryName)) {
//                continue;
//            }
            System.out.println("---------------- " + queryDirectoryName + " ----------------");
            pathStringBuffer.append(queryDirectoryName);

            String groundTruthTCDirectoryPath = pathStringBuffer.toString() + File.separator + "ground_truth_test_cases";

            pathStringBuffer.append(File.separator + recommendedTCDirectoryName);
            String recommendedResultDirectoryPath = pathStringBuffer.toString();
            File recommendedResultDirectory = new File(recommendedResultDirectoryPath);
            if (!recommendedResultDirectory.exists()) {
                int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
                pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");
                continue;
            }

            count++;

            File groundTruthTCDirectory = new File(groundTruthTCDirectoryPath);
            File[] groundTruthTCFileArray = groundTruthTCDirectory.listFiles();
            Map<String, String> groundTruthTCMethodIdAndCodeMap = new HashMap<>();
            for (File groundTruthTCFile : groundTruthTCFileArray) {
                String groundTruthTCFileName = groundTruthTCFile.getName();
                if (!groundTruthTCFileName.startsWith("tc_")) {
                    continue;
                }
                groundTruthTCFileName = groundTruthTCFileName.replace("tc_", "");
                String testCaseId = groundTruthTCFileName.replace(".json", "");
                String tcJsonString = FileUtil.readFileContentToString(groundTruthTCFile);
                TestInfoTableModel testInfoTableModel = jacksonUtil.json2Bean(tcJsonString, TestInfoTableModel.class);
                String testMethodCode = testInfoTableModel.getTestMethodCode();
                groundTruthTCMethodIdAndCodeMap.put(testCaseId, testMethodCode);
            }


            Map<String, Double> tcRankAndSimilarityMap = new HashMap<>();
            File[] tcRankDirectoryArray = recommendedResultDirectory.listFiles();
            for (File tcRankDirectory : tcRankDirectoryArray) {
                if (tcRankDirectory.isFile()) {
                    continue;
                }
                String tcRankDirectoryName = tcRankDirectory.getName();
                File[] subFileArray = tcRankDirectory.listFiles();
                Map<String, String> recommendedTCMethodIdCodeMap = new HashMap<>();
                for (File subFile : subFileArray) {
                    String subFileName = subFile.getName();
                    if (!subFileName.startsWith("tc_")) {
                       continue;
                    }
                    subFileName = subFileName.replace("tc_", "");
                    String testCaseId = subFileName.replace(".json", "");
                    String tcJsonString = FileUtil.readFileContentToString(subFile);
                    TestInfoTableModel testInfoTableModel = jacksonUtil.json2Bean(tcJsonString, TestInfoTableModel.class);
                    String testMethodCode = testInfoTableModel.getTestMethodCode();
                    recommendedTCMethodIdCodeMap.put(testCaseId, testMethodCode);
                }

                TestSimilarityModel testSimilarityModel = measureSimilarityBetweenTwoTestCasesMap(recommendedTCMethodIdCodeMap
                        , groundTruthTCMethodIdAndCodeMap);
                double maxSimilarity = testSimilarityModel.getSimilarity();
                System.out.println("maxSimilarity: " + maxSimilarity);
                tcRankAndSimilarityMap.put(tcRankDirectoryName, maxSimilarity);
            }

            int number = tcRankAndSimilarityMap.size();
            LinkedHashMap<String, Double> tcRankAndSimilarityLinkedHashMap = new LinkedHashMap<>(number);
            for (int i = 1; i <= number; i++) {
                String key = "tc_rank_" + i;
                double similarity = tcRankAndSimilarityMap.get(key);
                tcRankAndSimilarityLinkedHashMap.put(key, similarity);
            }
            tcRankAndSimilarityMap = null;

            pathStringBuffer.append(File.separator + "tc_rank_similarity.json");
            String tcRankSimilarityFilePath = pathStringBuffer.toString();
            String tcRankSimilarityJsonString = jacksonUtil.bean2Json(tcRankAndSimilarityLinkedHashMap);
            FileUtil.writeStringToTargetFile(tcRankSimilarityJsonString, tcRankSimilarityFilePath);
            tcRankSimilarityJsonString = null;
            tcRankSimilarityFilePath = null;

            tcRankAndSimilarityLinkedHashMap = null;

            // 移除 query_xxx 目录名称
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");
        }

        System.out.println("count: " + count);

        pathStringBuffer = null;
        queryDirectoryArray = null;
        searchQueryDirectory = null;


    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/25 18:06
      * @author xxx
      */
    private TestSimilarityModel measureSimilarityBetweenTwoTestCasesMap(Map<String,String> recommendedTCMethodIdCodeMap
            , Map<String,String> groundTruthTCMethodIdAndCodeMap) {
        double maxSimilarity = 0;
        String groundTruthTCIdWithMaxSimilarity = null;;
        String recommendedTCIdWithMaxSimilarity = null;
        Iterator<Map.Entry<String, String>> recommendedIterator = recommendedTCMethodIdCodeMap.entrySet().iterator();
        while (recommendedIterator.hasNext()) {
            Map.Entry<String, String> recommendedEntry = recommendedIterator.next();
            String recommendedTCId = recommendedEntry.getKey();
            recommendedTCIdWithMaxSimilarity = recommendedTCId;

            String recommendedTCMethodCode = recommendedEntry.getValue();
            recommendedTCMethodCode = CodeUtil.cleanCommentAndAnnotation(recommendedTCMethodCode);

            Iterator<Map.Entry<String, String>> groundTruthIterator = groundTruthTCMethodIdAndCodeMap.entrySet().iterator();
            while (groundTruthIterator.hasNext()) {
                Map.Entry<String, String> groundTruthEntry = groundTruthIterator.next();
                String groundTruthTCId = groundTruthEntry.getKey();
                String groundTruthTCMethodCode = groundTruthEntry.getValue();
                groundTruthTCMethodCode = CodeUtil.cleanCommentAndAnnotation(groundTruthTCMethodCode);
                if (recommendedTCMethodCode == null || recommendedTCMethodCode == null) {
                    continue;
                }
                System.out.println("groundTruthTCId:" + groundTruthTCId);
                System.out.println("recommendedTCId:" + recommendedTCId);
                double similarity = FuzzySearch.ratio(recommendedTCMethodCode, groundTruthTCMethodCode);
                // FuzzySearch.ratio 返回 [0, 100] 整数
                similarity = similarity / 100.0;
                System.out.println("similarity: " + similarity);
                if (similarity <= maxSimilarity) {
                    continue;
                }
                maxSimilarity = similarity;
                groundTruthTCIdWithMaxSimilarity = groundTruthTCId;
            }
        }
        return new TestSimilarityModel(groundTruthTCIdWithMaxSimilarity, recommendedTCIdWithMaxSimilarity, maxSimilarity);
    }
}
