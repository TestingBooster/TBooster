package com.tbooster.tcr;


import org.python.util.PythonInterpreter;

/**
  * @Author xxx
  * @Date 2020/4/16 6:44 PM
  */
public class LiteralTextAnalysis {

    private static PythonInterpreter interpreter = new PythonInterpreter();

    /**
     * Measure the similarity between literal texts of two code blocks.
     * @param codeBlock1
     * @param codeBlock2
     * @return double
     * @date 2020/4/18 5:43 PM
     * @author xxx
     */
    public static double measureSimilarityBetweenTwoCodesByRatio(String codeBlock1, String codeBlock2) {
        codeBlock1 = codeBlock1.replaceAll("\n", " ");
        codeBlock2 = codeBlock2.replaceAll("\n", " ");
        codeBlock1 = codeBlock1.replaceAll("\t", " ");
        codeBlock2 = codeBlock2.replaceAll("\t", " ");
        codeBlock1 = codeBlock1.replaceAll("\r\n", " "); // for windows
        codeBlock2 = codeBlock2.replaceAll("\r\n", " ");
        codeBlock1 = codeBlock1.replaceAll("\'", "\"");
        codeBlock2 = codeBlock2.replaceAll("\'", "\"");
        String str1 = "'" + codeBlock1 + "'";
        String str2 = "'" + codeBlock2 + "'";
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import difflib as diff");
        interpreter.exec("s = diff.SequenceMatcher(lambda x: x==\" \", " + str1 + ", " + str2 + ", autojunk=True)");
        interpreter.exec("sim = s.ratio()");
        double sim = interpreter.getLocals().__finditem__("sim").asDouble();
        interpreter.close();
        return sim;
    }

    /**
      * Measure the distance between literal texts of two code blocks.
      * @param codeBlock1
      * @param codeBlock2
      * @return double
      * @date 2020/4/18 5:43 PM
      * @author xxx
      */
    public static double measureDLBetweenTwoCodeBlocks(String codeBlock1, String codeBlock2) {
        codeBlock1 = codeBlock1.replaceAll("\n", " ");
        codeBlock2 = codeBlock2.replaceAll("\n", " ");
        codeBlock1 = codeBlock1.replaceAll("\t", " ");
        codeBlock2 = codeBlock2.replaceAll("\t", " ");
        codeBlock1 = codeBlock1.replaceAll("\r\n", " "); // for windows
        codeBlock2 = codeBlock2.replaceAll("\r\n", " ");
        codeBlock1 = codeBlock1.replaceAll("\'", "\""); // 很重要
        codeBlock2 = codeBlock2.replaceAll("\'", "\"");
        codeBlock1 = codeBlock1.replaceAll("\\s{2,}", " ");
        codeBlock2 = codeBlock2.replaceAll("\\s{2,}", " ");
        String str1 = "'" + codeBlock1 + "'";
        String str2 = "'" + codeBlock2 + "'";
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("from functools import reduce");
        interpreter.exec("import difflib as diff");
        interpreter.exec("s1 = diff.SequenceMatcher(lambda x: x==\" \", " + str1 + ", " + str2 + ", autojunk=True)");
        interpreter.exec("matching_blocks1 = s1.get_matching_blocks()");
        interpreter.exec("s2 = diff.SequenceMatcher(lambda x: x==\" \", " + str2 + ", " + str1 + ", autojunk=True)");
        interpreter.exec("matching_blocks2 = s2.get_matching_blocks()");
        interpreter.exec("matches1 = reduce(lambda sum, triple: sum + triple[-1], matching_blocks1, 0)");
        interpreter.exec("matches2 = reduce(lambda sum, triple: sum + triple[-1], matching_blocks2, 0)");
        interpreter.exec("matches = max(matches1, matches2)");
        interpreter.exec("total = len(s1.a) + len(s1.b)");
        interpreter.exec("sim = diff._calculate_ratio(matches, total)");
        double sim = interpreter.getLocals().__finditem__("sim").asDouble();
        return 1 - sim;
    }
}
