package com.tbooster.tcr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.MethodCodeModel;
import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.models.TestInfoTableModel;
import com.tbooster.utils.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/8/24 08:23
 */
public class CodAspectBased {
    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";

    final private static int k = 10; // top k

    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();


    private static List<MethodCodeModel> methodCodeModelList;

    static {
        String searchCorpusForTBoosterDirectoryPath = experimentDataDirectoryPath + File.separator + "search_corpus_for_TBooster";
        String methodCodeFilePath = searchCorpusForTBoosterDirectoryPath + File.separator + "method_code_with_lines.json";
        File methodCodeFile = new File(methodCodeFilePath);
        String jsonString = FileUtil.readFileContentToString(methodCodeFile);
        JacksonUtil jacksonUtil = new JacksonUtil();
        JavaType javaType = jacksonUtil.getCollectionType(List.class, MethodCodeModel.class);
        try {
            methodCodeModelList = jacksonUtil.getMapper().readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        CodAspectBased codAspectBased = new CodAspectBased();
        int gapPercentage = 10;
        double threshold = 0.9;
        codAspectBased.run(gapPercentage, threshold);
        codAspectBased = null;

        methodCodeModelList = null;
        methodInfoTableDao = null;
        testInfoTableDao = null;

        DBUtil.closeConnection();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/24 09:28
     * @author xxx
     */
    public void run(int gapPercentage, double threshold) {
        String methodCodeSimilarityFileName = "method_similarity.json";
        JacksonUtil jacksonUtil = new JacksonUtil();

        StringBuffer pathStringBuffer = new StringBuffer(experimentDataDirectoryPath);
        pathStringBuffer.append(File.separator + "search_queries");
        String searchQueryDirectoryPath = pathStringBuffer.toString();

        Map<String, Integer> compareTimesMap = new HashMap<>();
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

//            String numberString = queryDirectoryName.replace("query_","").trim();
//            int number = Integer.parseInt(numberString);
//
//            if (number < 881 || number > 1000) {
//                continue;
//            }

            System.out.println("---------------- " + queryDirectoryName + " ----------------");
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
            subFileArray = null;

            String fileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = jacksonUtil.json2Bean(fileContent, MethodInfoTableModel.class);
            String methodCode = methodInfoTableModel.getMethodCode();
            methodCode = CodeUtil.cleanCommentAndAnnotation(methodCode);
            int lines = methodCode.split(System.lineSeparator()).length;
            double lineGap = Math.ceil(lines * gapPercentage / 100.0);
            double minLines = lines - lineGap;
            double maxLines = lines + lineGap;
            System.out.println("lines: " + lines);
            System.out.println("minLines: " + minLines);
            System.out.println("maxLines: " + maxLines);

            Map<String, Double> methodIdCodeSimilarityMap = new HashMap<>();
            int compareTimes = 0;
            long startTime = System.currentTimeMillis();
            for (MethodCodeModel methodCodeModel : methodCodeModelList) {
                int methodCodeModelLines = methodCodeModel.getLines();
                if (methodCodeModelLines < minLines || methodCodeModelLines > maxLines) {
                    // optimization 1
                    continue;
                }
                String code = methodCodeModel.getCode();
                code = CodeUtil.cleanCommentAndAnnotation(code);
                double similarity = LiteralTextAnalysis.measureSimilarityBetweenTwoCodesByRatio(methodCode, code);
                compareTimes++;
                // optimization 2
//                double similarity = LiteralTextAnalysis.measureSimilarityBetweenTwoCodesByQuickRatio(methodCode, code);
                if (similarity < threshold) {
                    // ratio() returns a float in [0, 1], measuring the similarity of the sequences.
                    // As a rule of thumb, a ratio() value over 0.6 means the sequences are close matches
                    continue;
                }
                methodIdCodeSimilarityMap.put(methodCodeModel.getMethodId(), similarity);
            }
            long endTime =  System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);

            compareTimesMap.put(queryDirectoryName, compareTimes);

            if (methodIdCodeSimilarityMap.isEmpty()) {
                methodIdCodeSimilarityMap = null;
                methodInfoTableModel = null;
                // remove query_xxx
                int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
                pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");
                continue;
            }

            methodIdCodeSimilarityMap = MapUtil.sortByValueDescending(methodIdCodeSimilarityMap);
            LinkedHashMap<String, Double> topKMethodIdAndSimilarityMap = new LinkedHashMap<>();
            Iterator<Map.Entry<String, Double>> tempIterator = methodIdCodeSimilarityMap.entrySet().iterator();
            int count = 0;
            while (tempIterator.hasNext()) {
                count++;
                Map.Entry<String, Double> entry = tempIterator.next();
                String methodId = entry.getKey();
                double similarity = entry.getValue();
                BigDecimal bigDecimal = new BigDecimal(Double.toString(similarity));
                similarity = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                topKMethodIdAndSimilarityMap.put(methodId, similarity);
                if (count == k) {
                    break;
                }
            }
            methodIdCodeSimilarityMap = null;


            pathStringBuffer.append(File.separator + "test_cases_recommended_by_CodBased_" + gapPercentage
                    + "_" + threshold);
            String recommendedTestCasesDirectoryPath = pathStringBuffer.toString();
            File recommendedTestCasesDirectory = new File(recommendedTestCasesDirectoryPath);
            if (!recommendedTestCasesDirectory.exists()) {
                recommendedTestCasesDirectory.mkdir();
            }

            String methodIdSimilarityJsonString = jacksonUtil.bean2Json(topKMethodIdAndSimilarityMap);
            String methodSimilarityFilePath = pathStringBuffer.toString() + File.separator + methodCodeSimilarityFileName;
            FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
            methodSimilarityFilePath = null;
            methodIdSimilarityJsonString = null;

            searchAndSaveDetailInformation(topKMethodIdAndSimilarityMap
                    , recommendedTestCasesDirectoryPath);
            topKMethodIdAndSimilarityMap = null;

            // 移除 query_xxx 目录名
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");


            methodCode = null;
            methodInfoTableModel = null;
            fileContent = null;
            mutFile = null;
        }

        String compareTimesFilePath = searchQueryDirectory + File.separator + "compare_times_CodBased_"
                + gapPercentage + "_" + threshold + ".json";
        String compareTimesMapJsonString = jacksonUtil.bean2Json(compareTimesMap);
        FileUtil.writeStringToTargetFile(compareTimesMapJsonString, compareTimesFilePath);
        compareTimesFilePath = null;
        compareTimesMapJsonString = null;

        String queryCostTimeMapJsonString = jacksonUtil.bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_CodBased" + "_" + gapPercentage + "_" + threshold + ".json");
        String costTimeFilePath = pathStringBuffer.toString();
        FileUtil.writeStringToTargetFile(queryCostTimeMapJsonString, costTimeFilePath);
        costTimeFilePath = null;
        queryCostTimeMapJsonString = null;

        pathStringBuffer = null;
        queryDirectoryArray = null;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/4 00:32
     * @author xxx
     */
    private void searchAndSaveDetailInformation(Map<String, Double> topKMethodIdAndSimilarityMap
            , String recommendedResultDirectoryPath) {
        StringBuffer pathStringBuffer = new StringBuffer(recommendedResultDirectoryPath);
        pathStringBuffer.append(File.separator);
        int count = 0;
        Iterator<Map.Entry<String, Double>> iterator = topKMethodIdAndSimilarityMap.entrySet().iterator();
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

            // remove tt_xxx.json
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

                // remove tc_xxx.json
                int testCaseFileNameIndex = pathStringBuffer.indexOf(testCaseFileName);
                pathStringBuffer.replace(testCaseFileNameIndex, pathStringBuffer.length(), "");
            }

            // remove tc_rank_xxx
            int tcRankDirectoryNameIndex = pathStringBuffer.indexOf(tcRankDirectoryName);
            pathStringBuffer.replace(tcRankDirectoryNameIndex, pathStringBuffer.length(), "");

            testCaseIdAndDetailInfoMap = null;
            testCaseIdSet = null;
            tcRankDirectory = null;
        }
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/20 22:51
     * @author xxx
     */
    public Map<String, Double> sortByCodSim(List<String> methodWithSameSignatureIdList, String methodCode) {
        methodCode = CodeUtil.cleanCommentAndAnnotation(methodCode);
        Map<String, Double> methodIdCodeSimilarityMap = new LinkedHashMap<>();
        for (String methodId : methodWithSameSignatureIdList) {
            for (MethodCodeModel methodCodeModel : methodCodeModelList) {
                if (!methodCodeModel.getMethodId().equals(methodId)) {
                    continue;
                }
                String tempMethodCode = methodCodeModel.getCode();
                tempMethodCode = CodeUtil.cleanCommentAndAnnotation(tempMethodCode);
                double codeSimilarity = LiteralTextAnalysis.measureSimilarityBetweenTwoCodesByRatio(methodCode, tempMethodCode);
                BigDecimal bigDecimal = new BigDecimal(Double.toString(codeSimilarity));
                codeSimilarity = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                methodIdCodeSimilarityMap.put(methodId, codeSimilarity);
                break;
            }
        }
        methodIdCodeSimilarityMap = MapUtil.sortByValueDescending(methodIdCodeSimilarityMap);
        return methodIdCodeSimilarityMap;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/21 15:18
     * @author xxx
     */
    public Map<String, Double> sortByCodSim(Map<String, Double> topKMethodWithSimComIdAndComSimMap, String methodCode) {
        methodCode = CodeUtil.cleanCommentAndAnnotation(methodCode);
        Map<String, Double> methodIdCodeSimilarityMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Double>> iterator = topKMethodWithSimComIdAndComSimMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            String methodId = entry.getKey();
            for (MethodCodeModel methodCodeModel : methodCodeModelList) {
                if (!methodCodeModel.getMethodId().equals(methodId)) {
                    continue;
                }
                String tempMethodCode = methodCodeModel.getCode();
                tempMethodCode = CodeUtil.cleanCommentAndAnnotation(tempMethodCode);
                double codeSimilarity = LiteralTextAnalysis.measureSimilarityBetweenTwoCodesByRatio(methodCode, tempMethodCode);
                BigDecimal bigDecimal = new BigDecimal(Double.toString(codeSimilarity));
                codeSimilarity = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                methodIdCodeSimilarityMap.put(methodId, codeSimilarity);
                break;
            }
        }
        methodIdCodeSimilarityMap = MapUtil.sortByValueDescending(methodIdCodeSimilarityMap);
        return methodIdCodeSimilarityMap;
    }


}
