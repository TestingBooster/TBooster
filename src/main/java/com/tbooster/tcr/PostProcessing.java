package com.tbooster.tcr;

import com.tbooster.dao.MethodInfoTableDao;
import com.tbooster.dao.TestInfoTableDao;
import com.tbooster.models.MethodInfoTableModel;
import com.tbooster.models.TestInfoTableModel;
import com.tbooster.utils.FileUtil;
import com.tbooster.utils.JacksonUtil;

import java.io.File;
import java.util.*;

/**
  * @Author xxx
  * @Date 2020/9/15 10:57
  */
public class PostProcessing {

    private static TestInfoTableDao testInfoTableDao = new TestInfoTableDao();
    private static MethodInfoTableDao methodInfoTableDao = new MethodInfoTableDao();

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/9/15 11:04
      * @author xxx
      */
    public static void saveTopKTCDetailsToRecommendedResultDirectory(List<String> methodIdList
            , String recommendedResultDirectoryPath) {
        JacksonUtil jacksonUtil = new JacksonUtil();
        StringBuffer pathStringBuffer = new StringBuffer(recommendedResultDirectoryPath);
        pathStringBuffer.append(File.separator);
        for (int i = 0; i < methodIdList.size(); i++) {
            String methodId = methodIdList.get(i);
            MethodInfoTableModel testTarget = methodInfoTableDao.searchMethodByMethodId(methodId);

            String tcRankDirectoryName = "tc_rank_" + (i + 1);
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
            String testTargetString = jacksonUtil.bean2Json(testTarget);
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
                String testCaseString = jacksonUtil.bean2Json(testInfoTableModel);
                String testCaseFileName = "tc_" + testCaseId + ".json";
                pathStringBuffer.append(testCaseFileName);
                String testCaseFilePath = pathStringBuffer.toString();
                FileUtil.writeStringToTargetFile(testCaseString, testCaseFilePath);

                // remove tc_xxx.json
                int testCaseFileNameIndex = pathStringBuffer.indexOf(testCaseFileName);
                pathStringBuffer.replace(testCaseFileNameIndex, pathStringBuffer.length(), "");

                testCaseString = null;
                testInfoTableModel = null;
            }

            // remove tc_rank_xxx
            int tcRankDirectoryNameIndex = pathStringBuffer.indexOf(tcRankDirectoryName);
            pathStringBuffer.replace(tcRankDirectoryNameIndex, pathStringBuffer.length(), "");

            testCaseIdAndDetailInfoMap = null;
            testCaseIdSet = null;
            tcRankDirectory = null;
            testTarget = null;
        }
    }
}
