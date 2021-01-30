package com.tbooster.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  * @Author xxx
  * @Date 2020/4/20 3:31 PM
  */
public class NiCad {

    final static String NiCAD_PATH = "xxx/NiCad-5.2";   // the installation directory of NiCad


    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/23 14:54
      * @author xxx
      */
    public static int detectFunctionLevelCloneWithNiCad5cross(String project1DirectoryPath
            , String project2DirectoryPath) {
        //./nicad5cross functions java examples/search_queries/query_1 examples/search_corpus default-report
        StringBuffer commandStringBuffer = new StringBuffer("cd " + NiCAD_PATH);
        commandStringBuffer.append(" && ./nicad5cross functions java");
        commandStringBuffer.append(" " + project1DirectoryPath);
        commandStringBuffer.append(" " + project2DirectoryPath);
        commandStringBuffer.append(" default-report");
        String command = commandStringBuffer.toString();
        System.out.println("command: " + command);
        int exitValue = execute(command);
        if (exitValue != 0) {
            return -1;
        }
        return exitValue;
    }

    /**
      * Judge whether two methods are clone pair.
      * @param method1Code
      * @param method2Code
      * @return double
      * @date 2020/4/20 9:24 PM
      * @author xxx
      */
    public static double judgeTwoMethodIsClone(String method1Code, String method2Code) {
        String tempFolderPath = NiCAD_PATH + File.separator + "temp_folder";
        File tempFolder = new File(tempFolderPath);
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }
        String srcFolderPath = tempFolderPath + File.separator + "src";
        File srcFolder = new File(srcFolderPath);
        if (!srcFolder.exists()) {
            srcFolder.mkdir();
        } else {
            deleteOldDetectionFile(tempFolderPath);
        }
        String method1CodeFilePath = srcFolderPath + File.separator + "TempMethod1Code.java";
        String method2CodeFilePath = srcFolderPath + File.separator + "TempMethod2Code.java";
        FileUtil.writeMethodCodeToFile(method1Code, method1CodeFilePath);
        FileUtil.writeMethodCodeToFile(method2Code, method2CodeFilePath);
        int exitValue = execute("cd " + NiCAD_PATH + " && ./nicad5 functions java " + srcFolderPath + " default-report");
        if (exitValue != 0) {
            System.err.println("NiCad Error");
            return -1;
        }
        String xmlFilePath = tempFolderPath + "/src_functions-blind-clones/src_functions-blind-clones-0.10.xml";
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()) {
            System.out.println("The xml file does not exist!");
            return -1;
        }
        try {
            Document document = (new SAXReader()).read(xmlFile);
            Element root = document.getRootElement();
            Element cloneElement = root.element("clone");
            if (cloneElement == null) {
                return -1;
            }
            Attribute similarityAttribute = cloneElement.attribute("similarity");
            String similarityValue = similarityAttribute.getValue();
            return Integer.parseInt(similarityValue) / 100.0;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
      * Delete the previous detection file.
      * @param tempFolderPath
      * @return void
      * @date 2020/4/20 9:41 PM
      * @author xxx
      */
    private static void deleteOldDetectionFile(String tempFolderPath) {
        File tempFolder = new File(tempFolderPath);
        File[] fileList = tempFolder.listFiles();
        for (File file : fileList) {
            if (file.isFile()) {
                file.delete();
                continue;
            }
            String fileName = file.getName();
            if (!fileName.contains("src_functions")) {
                continue;
            }
            File[] subFileList = file.listFiles();
            for (File subFile : subFileList) {
                subFile.delete();
            }
            file.delete();
        }
    }

    /**
      *
      * @param command
      * @return int
      * @date 2020/4/20 9:42 PM
      * @author xxx
      */
    public static int execute(String command) {
        String[] cmd = {"/bin/bash"};
        Runtime rt = Runtime.getRuntime();
        int exitValue = 0;
        Process process = null;
        BufferedReader br = null;
        InputStream fis = null;
        BufferedWriter bw = null;
        OutputStream os = null;
        StringBuffer cmdOut = new StringBuffer();
        String line;
        try {
            process = rt.exec(cmd);
            os = process.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(command);
            bw.flush();
            bw.close();

            fis = process.getInputStream();
            br = new BufferedReader(new InputStreamReader(fis));
            while ((line = br.readLine()) != null) {
                cmdOut.append(line).append(System.getProperty("line.separator"));
            }

            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                exitValue = process.exitValue();
                process.destroy();
            }
        }
        if (exitValue != 0) {
            System.err.println(cmdOut.toString());
            cmdOut = null;
        }
        return exitValue;
    }


    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/23 15:30
      * @author xxx
      */
    public static Map<String,Double> extractCloneMethodIdAndSimilarityFromXmlFile(String xmlFilePath) {
        Map<String, Double> methodIdAndSimilarityMap = new HashMap<>();
        File xmlFile = new File(xmlFilePath);
        try {
            Document document = (new SAXReader()).read(xmlFile);
            Element root = document.getRootElement();
            List<Element> cloneElementList = root.elements("clone");
            if (cloneElementList != null) {
                for (Element cloneElement : cloneElementList) {
                    Attribute similarityAttribute = cloneElement.attribute("similarity");
                    String similarityValue = similarityAttribute.getValue();
                    double similarity = Integer.parseInt(similarityValue) / 100.0;
                    List<Element> sourceElementList = cloneElement.elements("source");
                    for (Element sourceElement : sourceElementList) {
                        Attribute fileAttribute = sourceElement.attribute("file");
                        String filePath = fileAttribute.getValue();
                        if (filePath.contains("method_code")) {
                            continue;
                        }
                        String[] pathArray = filePath.split(File.separator);
                        String fileName = pathArray[pathArray.length - 1];
                        String methodId = fileName.replace(".java", "").trim();
                        methodIdAndSimilarityMap.put(methodId, similarity);
                        break;
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        if (methodIdAndSimilarityMap.isEmpty()) {
            methodIdAndSimilarityMap = null;
            return methodIdAndSimilarityMap;
        }
        methodIdAndSimilarityMap = MapUtil.sortByValueDescending(methodIdAndSimilarityMap);
        return methodIdAndSimilarityMap;
    }
}
