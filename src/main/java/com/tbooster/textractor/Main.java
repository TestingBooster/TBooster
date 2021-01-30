package com.tbooster.textractor;

import com.tbooster.dao.ProjectInfoTableDao;
import com.tbooster.models.ProjectInfoTableModel;
import com.tbooster.utils.MD5Util;

import java.io.File;

/**
  * @Author xxx
  * @Date 2020/5/26 11:46 PM
  */
public class Main {


    /**
     * Check whether the project under analysis is an empty project.
     * Check whether the project under analysis contains tests.
     * Analyze the production code and extract methods under test (MUT).
     * Analyze the test code and extract test cases (TC);
     * Save MUT and TC to the database.
     * @param  srcDirectoryPath
     *         the path of the 'src' directory of the project.
     * @return int
     *         If the srcDirectoryPath doesn't exist, -1 will be returned.
     *         If the project under analysis is an empty project or doesn't contain tests, 1 will be returned,
     *         otherwise, 1 will be returned.
     * @date 2020/8/2 9:07 AM
     * @author xxxx
     */
    public int run(String srcDirectoryPath) {
        File srcDirectory = new File(srcDirectoryPath);
        if (!srcDirectory.exists()) {
            return -1;
        }
        SourceCodeAnalysis sca = new SourceCodeAnalysis(srcDirectoryPath);

        // Check whether the project under analysis is an empty project.
        int productionFileNumber = sca.getProductionFileNumber();
        if (productionFileNumber == 0) {
            return 0;
        }

        // Check whether the project under analysis contains tests.
        int testFileNumber = sca.getTestFileNumber();
        if (testFileNumber == 0) {
            return 0;
        }
        long beginTime = System.currentTimeMillis();

        // create some directories that would be used to store the analysis results produced by TBooster.
        System.out.println("Initialize TBooster Directory ...");
        sca.initTBoosterAnalysisDirectories();

        // load global package and field declaration information.
        System.out.println("Load Global Information ... ");
        sca.initPackageAndFieldDeclarationMaps();

        // analyze the production code of the project
        System.out.println("Begin analyzing the production code ...");
        ProductionCodeAnalysis pca = sca.initInstanceOfProductionCodeAnalysis();
        pca.productionCodeAnalysis();

        // analyze the test code of the project
        System.out.println("Begin analyzing the test code ...");
        TestCodeAnalysis tca = sca.initInstanceOfTestCodeAnalysis();
        int testCaseCount =  tca.testCodeAnalysis();
        System.out.println("The number of test cases: " + testCaseCount);

        String fragmentOfTestMethodWithTestTargetDirectoryPath = tca.getFragmentOfTestMethodWithTestTargetDirectoryPath();
        File fragmentOfTestMethodWithTestTargetDirectory = new File(fragmentOfTestMethodWithTestTargetDirectoryPath);
        File[] testCaseFileArray = fragmentOfTestMethodWithTestTargetDirectory.listFiles();
        if (testCaseFileArray == null) {
            return 0;
        }
        int testCaseNumber = testCaseFileArray.length;
        if (testCaseNumber == 0) {
            return 0;
        }
        boolean noTestCase = false;
        if (testCaseNumber == 1) {
            File testCaseFile = testCaseFileArray[0];
            String fileName = testCaseFile.getName();
            if (!fileName.endsWith(".json")) {
                noTestCase = true;
            }
        }
        if (noTestCase) {
            return 0;
        }

        System.out.println("Begin saving to the database ...");
        saveToDatabase(srcDirectoryPath, pca, tca);

        long endTime = System.currentTimeMillis();
        System.out.println("Total cost time of analyzing the project: " + (endTime - beginTime) + "ms");


        tca.setFieldDeclarationInAllProductionFilesMap(null);
        tca.setFieldDeclarationInAllTestFilesMap(null);
        tca.setPackageDeclarationInAllProductionFilesMap(null);
        tca.setPackageDeclarationInAllTestFilesMap(null);
        tca.setProductionFileArray(null);
        tca.setTestFileArray(null);
        tca = null;

        pca.setFieldDeclarationInAllProductionFilesMap(null);
        pca.setFieldDeclarationInAllTestFilesMap(null);
        pca.setPackageDeclarationInAllProductionFilesMap(null);
        pca.setPackageDeclarationInAllTestFilesMap(null);
        pca.setProductionFileArray(null);
        pca.setTestFileArray(null);
        pca = null;

        sca.setFieldDeclarationInAllProductionFilesMap(null);
        sca.setFieldDeclarationInAllTestFilesMap(null);
        sca.setPackageDeclarationInAllProductionFilesMap(null);
        sca.setPackageDeclarationInAllTestFilesMap(null);
        sca.setProductionFileArray(null);
        sca.setTestFileArray(null);
        sca = null;

        return testCaseNumber;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/10 11:34
     * @author xxx
     */
    private void saveToDatabase(String srcDirectoryPath, ProductionCodeAnalysis pca, TestCodeAnalysis tca) {
        ProjectInfoTableModel projectInfoTableModel = extractProjectInformationFromSrcDirectoryPath(srcDirectoryPath);
        // save the basic information of the project under analysis to the database.
        saveProjectInfoToDatabase(projectInfoTableModel);
        // save MUT and TC to the database.
        String fragmentOfSingleMethodInProductionCodeDirectoryPath = pca.getFragmentOfSingleMethodInProductionCodeDirectoryPath();
        pca.saveSingleMethodToDatabase(projectInfoTableModel, fragmentOfSingleMethodInProductionCodeDirectoryPath, 0);
        tca.saveSingleMethodAndTestCaseToDatabase(projectInfoTableModel);
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/2 10:24 AM
     * @author xxx
     */
    private int saveProjectInfoToDatabase(ProjectInfoTableModel projectInfoTableModel) {
        ProjectInfoTableDao projectInfoTableDao = new ProjectInfoTableDao();
        int returnValue = projectInfoTableDao.saveProjectInfoToDatabase(projectInfoTableModel);
        return returnValue;
    }

    /**
     * extract the basic information of the project under test.
     * @param srcDirectoryPath
     * @return ProjectInfoTableModel
     * @throws
     * @date 2020/8/2 11:14 AM
     * @author xxx
     */
    private ProjectInfoTableModel extractProjectInformationFromSrcDirectoryPath(String srcDirectoryPath) {
        String[] directoryNameArray = srcDirectoryPath.split(File.separator);
        String projectDirectoryName = null;
        for (String directoryName : directoryNameArray) {
            int addCharacterIndex = directoryName.indexOf("+");
            if (addCharacterIndex == -1) {
                continue;
            }
            projectDirectoryName = directoryName;
            break;
        }
        String[] projectInformationArray = projectDirectoryName.split("\\+");
        String repositoryId = projectInformationArray[0];
        String repositoryName = projectInformationArray[1];
        int projectInformationArrayLength =  projectInformationArray.length;
        String projectName = repositoryName;
        if (projectInformationArrayLength > 2) {
            projectName = projectDirectoryName;
            projectName = projectName.replace(repositoryId + "+", "");
            projectName = projectName.replace(repositoryName + "+", "");
        }
        ProjectInfoTableModel projectInfoTableModel = new ProjectInfoTableModel(projectName, repositoryId, repositoryName);
        String projectId = MD5Util.getMD5(projectDirectoryName);
        projectInfoTableModel.setProjectId(projectId);
        return projectInfoTableModel;
    }
}
