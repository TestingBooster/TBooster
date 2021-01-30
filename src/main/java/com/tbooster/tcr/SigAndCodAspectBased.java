package com.tbooster.tcr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.models.TestTendererNeedMethodModel;
import com.tbooster.utils.DBUtil;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.util.*;

/**
 * @Author xxx
 * @Date 2020/9/4 16:06
 */
public class SigAndCodAspectBased {
    private static String experimentDataDirectoryPath = "xxx/projects_from_github/expriment_data";
    final private static int k = 10; // top k


    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();
    private static JacksonUtil jacksonUtil = new JacksonUtil();

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
        SigAndCodAspectBased sigAndCodAspectBased = new SigAndCodAspectBased();
        sigAndCodAspectBased.run();
        sigAndCodAspectBased = null;

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
     * @date 2020/9/4 12:01
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
//            if (!"query_1".equals(queryDirectoryName)) {
//                continue;
//            }
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
            subFileArray = null;


            String mutFileContent = FileUtil.readFileContentToString(mutFile);
            MethodInfoTableModel methodInfoTableModel = (new JacksonUtil()).json2Bean(mutFileContent, MethodInfoTableModel.class);
            String methodCode = methodInfoTableModel.getMethodCode();

            long startTime = System.currentTimeMillis();
            // search without limiting K and sort by code similarity
            Map<String, Double> topKMethodIdAndCodSimMap = sumBasedSearchLimitKAndEHSortByCodSim(methodInfoTableModel, methodCode);

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            queryCostTimeMap.put(queryDirectoryName, costTime);
            if (topKMethodIdAndCodSimMap.size() == 0) {
                continue;
            }

            // Step 3: load details
            pathStringBuffer.append(queryDirectoryName);

            pathStringBuffer.append(File.separator + "test_cases_recommended_by_SigAndCodAspectBased");
            String recommendedResultDirectoryPath = pathStringBuffer.toString();
            File recommendedResultDirectory = new File(recommendedResultDirectoryPath);
            if (!recommendedResultDirectory.exists()) {
                recommendedResultDirectory.mkdir();
            }

            // save the similarity information.
            String methodIdSimilarityJsonString = jacksonUtil.bean2Json(topKMethodIdAndCodSimMap);
            String methodSimilarityFilePath = recommendedResultDirectoryPath + File.separator + methodSimilarityFileName;
            FileUtil.writeStringToTargetFile(methodIdSimilarityJsonString, methodSimilarityFilePath);
            methodSimilarityFilePath = null;
            methodIdSimilarityJsonString = null;

            // save detail information.
            searchAndDetailInfoFromDatabase(recommendedResultDirectoryPath, topKMethodIdAndCodSimMap);

            // remove query_xxx
            int queryDirectoryNameIndex = pathStringBuffer.indexOf(queryDirectoryName);
            pathStringBuffer.replace(queryDirectoryNameIndex, pathStringBuffer.length(), "");

            topKMethodIdAndCodSimMap = null;
            methodInfoTableModel = null;
        }

        String queryCostTimeMapJsonString = (new JacksonUtil()).bean2Json(queryCostTimeMap);
        pathStringBuffer.append("cost_time_by_SigAndCodAspectBased.json");
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
    public Map<String, Double> sumBasedSearchLimitKAndEHSortByCodSim(MethodInfoTableModel methodInfoTableModel
            , String methodCode) {
        String className = methodInfoTableModel.getClassName();
        String methodName = methodInfoTableModel.getMethodName();
        String signature = methodInfoTableModel.getSignature();
        int start = signature.indexOf("(");
        int end = signature.lastIndexOf(")");
        String parameterType = signature.substring(start + 1, end).trim();

        Map<String, Double> topKMethodIdAndCodSimMap = new LinkedHashMap<>();
        List<String> similarMethodIdList = new ArrayList<>();
        CodAspectBased codAspectBased = new CodAspectBased();

        List<TestTendererNeedMethodModel> methodWithSameCNMNList = new ArrayList<>();
        List<TestTendererNeedMethodModel> methodWithSameCNList = new ArrayList<>();
        List<TestTendererNeedMethodModel> methodWithSameMNList = new ArrayList<>();

        int count = 0;
        // 1) CN + S
        for (TestTendererNeedMethodModel testTendererNeedMethodModel : testTendererNeedMethodModelList) {
            String methodId = testTendererNeedMethodModel.getMethodId();
            if (testTendererNeedMethodModel.getClassName().equals(className)) {
                if (testTendererNeedMethodModel.getMethodName().equals(methodName)) {
                    if (testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                        similarMethodIdList.add(methodId);
                        count++;
                        if (count == k) {
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
        if (similarMethodIdList.size() >= 1) {
            Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                    , methodCode);
            topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
            similarMethodIdList.clear();
            if (count == k) {
                return topKMethodIdAndCodSimMap;
            }
        }

        List<String> haveSearchedMethodIdList = new ArrayList<>();
        // 2) CN + MN + *
        if (!methodWithSameCNMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNMNList) {
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                count++;
                if (count == k) {
                    break;
                }
            }
            Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                    , methodCode);
            topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
            similarMethodIdList.clear();
            if (count == k) {
                return topKMethodIdAndCodSimMap;
            }
        }

        // 3) CN + * + PT
        if (!methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                haveSearchedMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                count++;
                if (count == k) {
                    break;
                }
            }
            if (similarMethodIdList.size() >= 1) {
                Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                        , methodCode);
                topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
                similarMethodIdList.clear();
                if (count == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        // 4) * + MN + PT
        if (!methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                haveSearchedMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                count++;
                if (count == k) {
                    break;
                }
            }
            if (similarMethodIdList.size() >= 1) {
                Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                        , methodCode);
                topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
                similarMethodIdList.clear();
                if (count == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        // 5) CN + * + *
        if (!methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (haveSearchedMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
                count++;
                if (count == k) {
                    break;
                }
            }
            if (similarMethodIdList.size() >= 1) {
                Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                        , methodCode);
                topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
                similarMethodIdList.clear();
                if (count == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        // 6) * + MN + *
        if (!methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (haveSearchedMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
                count++;
                if (count == k) {
                    break;
                }
            }
        }
        if (similarMethodIdList.size() >= 1) {
            Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                    , methodCode);
            topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
        }
        similarMethodIdList = null;
        haveSearchedMethodIdList = null;
        codAspectBased = null;
        return topKMethodIdAndCodSimMap;
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

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/21 21:27
     * @author xxx
     */
    public Map<String, Double> sumBasedSearchAndEHSortByCodSim(MethodInfoTableModel methodInfoTableModel, String methodCode) {
        String className = methodInfoTableModel.getClassName();
        String methodName = methodInfoTableModel.getMethodName();
        String signature = methodInfoTableModel.getSignature();
        int start = signature.indexOf("(");
        int end = signature.lastIndexOf(")");
        String parameterType = signature.substring(start + 1, end).trim();

        Map<String, Double> topKMethodIdAndCodSimMap = new LinkedHashMap<>();
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
        if (similarMethodIdList.size() >= 1) {
            Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, k);
            topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
            similarMethodIdList.clear();
        }
        if (topKMethodIdAndCodSimMap.size() == k) {
            return topKMethodIdAndCodSimMap;
        }

        List<String> haveSearchedMethodIdList = new ArrayList<>();
        /*
        Level 1 Fuzz Search
         */
        // 2) CN + MN + *
        if (!methodWithSameCNMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNMNList) {
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
            }
            int gap = k - topKMethodIdAndCodSimMap.size();
            Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, gap);
            topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
            similarMethodIdList.clear();
            if (topKMethodIdAndCodSimMap.size() == k) {
                return topKMethodIdAndCodSimMap;
            }
        }

        // 3) CN + * + PT
        if (!methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                haveSearchedMethodIdList.add(testTendererNeedMethodModel.getMethodId());
            }
            if (similarMethodIdList.size() >= 1) {
                int gap = k - topKMethodIdAndCodSimMap.size();
                Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, gap);
                topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
                similarMethodIdList.clear();
                if (topKMethodIdAndCodSimMap.size() == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        // 4) * + MN + PT
        if (!methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                if (!testTendererNeedMethodModel.getParameterType().equals(parameterType)) {
                    continue;
                }
                similarMethodIdList.add(testTendererNeedMethodModel.getMethodId());
                haveSearchedMethodIdList.add(testTendererNeedMethodModel.getMethodId());
            }
            if (similarMethodIdList.size() >= 1) {
                int gap = k - topKMethodIdAndCodSimMap.size();
                Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, gap);
                topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
                similarMethodIdList.clear();
                if (topKMethodIdAndCodSimMap.size() == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        /*
        Level 2 Fuzz Search
         */
        // 5) CN + * + *
        if (!methodWithSameCNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameCNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (haveSearchedMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
            }
            if (similarMethodIdList.size() >= 1) {
                int gap = k - topKMethodIdAndCodSimMap.size();
                Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, gap);
                topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
                similarMethodIdList.clear();
                if (topKMethodIdAndCodSimMap.size() == k) {
                    return topKMethodIdAndCodSimMap;
                }
            }
        }

        // 6) * + MN + *
        if (!methodWithSameMNList.isEmpty()) {
            for (TestTendererNeedMethodModel testTendererNeedMethodModel : methodWithSameMNList) {
                String methodId = testTendererNeedMethodModel.getMethodId();
                if (haveSearchedMethodIdList.contains(methodId)) {
                    continue;
                }
                similarMethodIdList.add(methodId);
            }
        }
        if (similarMethodIdList.size() >= 1) {
            int gap = k - topKMethodIdAndCodSimMap.size();
            Map<String, Double> tempMethodIdAndCodSimMap = sortAndSelectTopK(similarMethodIdList, methodCode, gap);
            topKMethodIdAndCodSimMap.putAll(tempMethodIdAndCodSimMap);
        }
        similarMethodIdList = null;
        haveSearchedMethodIdList = null;
        return topKMethodIdAndCodSimMap;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/9/5 00:36
     * @author xxx
     */
    private Map<String,Double> sortAndSelectTopK(List<String> similarMethodIdList, String methodCode, int k) {
        Map<String, Double> topKMethodIdAndCodSimMap = new LinkedHashMap<>();
        if (similarMethodIdList.size() == 1) {
            topKMethodIdAndCodSimMap.put(similarMethodIdList.get(0), -1.0);
        } else {
            CodAspectBased codAspectBased = new CodAspectBased();
            Map<String, Double> methodWithMatchingSumIdAndCodSimMap = codAspectBased.sortByCodSim(similarMethodIdList
                    , methodCode);
            codAspectBased = null;
            if (methodWithMatchingSumIdAndCodSimMap.size() <= k) {
                topKMethodIdAndCodSimMap.putAll(methodWithMatchingSumIdAndCodSimMap);
            } else {
                Iterator<Map.Entry<String, Double>> iterator = methodWithMatchingSumIdAndCodSimMap.entrySet().iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    count++;
                    Map.Entry<String, Double> entry = iterator.next();
                    topKMethodIdAndCodSimMap.put(entry.getKey(), entry.getValue());
                    if (count == k) {
                        break;
                    }
                }
            }
        }
        return topKMethodIdAndCodSimMap;
    }
}
