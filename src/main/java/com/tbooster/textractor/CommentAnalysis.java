package com.tbooster.textractor;

import com.tbooster.models.SynonymPair;
import com.tbooster.models.TokenModel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author xxx
 * @Date 2020/3/2 4:51 PM
 */
public class CommentAnalysis {

    final static String REGEX_SINGLE_LINE_COMMENT = "\\s*(//)[\\d\\D]*";

    final static String REGEX_MULTI_LINE_COMMENT = "\\s*(/\\*)([\\d\\D]*)(\\*/)";

    final static String REGEX_DOC_COMMENT = "\\s*(/\\*\\*)([\\d\\D]*)(\\*/)";

    // https://stackoverflow.com/questions/43418812/check-whether-a-string-contains-japanese-chinese-characters
    final static String REGEX_CHINESE_AND_JAPANESE = "[\\u3040-\\u30ff\\u3400-\\u4dbf\\u4e00-\\u9fff\\uf900-\\ufaff\\uff66-\\uff9f\\u0400-\\u04FF]";


    final static List<String> posAbbrList = Arrays.asList("NN", "NNS", "NNP", "NNPS"
            , "VB", "VBD", "VBG", "VBN", "VBP", "VBZ");


    /**
     * Get the synonym pairs.
     * @param  synonymMap
     * @param  differenceOfKeywordSetAndIntersection
     * @return List<SynonymPair>
     * @date 2020/3/5 3:22 PM
     * @author xxx
     */
    private static List<SynonymPair> getSynonymPairs(Map<String,Set<String>> synonymMap
            , Set<String> differenceOfKeywordSetAndIntersection) {
        List<SynonymPair> synonymPairs = new ArrayList<>();
        Iterator<Map.Entry<String, Set<String>>> entries = synonymMap.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<String, Set<String>> entry = entries.next();
            String word = entry.getKey();
            Set<String> synonymSet = entry.getValue();
            if (synonymSet == null) {
                continue;
            }
            for (String keyword : differenceOfKeywordSetAndIntersection) {
                if (!synonymSet.contains(keyword)) {
                    continue;
                }
                synonymPairs.add(new SynonymPair(word, keyword));
                differenceOfKeywordSetAndIntersection.remove(keyword);
                break;
            }
        }
        if (synonymPairs.size() == 0) {
            synonymPairs = null;
        }
        return synonymPairs;
    }

    /**
     * Normalize the tokens.
     * @param tokenModelList
     * @return Map<String, String>:<token, pos>
     * @throws
     * @date 2020/3/5 2:18 PM
     * @author xxx
     */
    private static Map<String, String> getNormalizedToken(List<TokenModel> tokenModelList) {
        Map<String, String> keywordMap = new HashMap<>();
        for (TokenModel tokenModel : tokenModelList) {
            String lemma = tokenModel.getLemma();
            String pos = tokenModel.getPos();
            if (pos.startsWith("N")) {
                keywordMap.put(lemma, "NOUN");
            } else {
                keywordMap.put(lemma, "VERB");
            }
        }
        return keywordMap;
    }

    /**
     * Preprocessing the comments including tokenize, pos and lemma
     * @param commentStr
     * @return List<TokenModel>
     * @date 2020/3/3 7:48 PM
     * @author xxx
     */
    public static List<TokenModel> commentNLPProcessing(String commentStr) {
        List<TokenModel> tokenModelList = new ArrayList<>();
        Properties props = new Properties();
        // 设置相应的properties
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        props.setProperty("tokenize.options", "ptb3Escaping=false");
        // 获得StanfordCoreNLP 对象
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(commentStr);
        pipeline.annotate(document);
        if(document.get(CoreAnnotations.SentencesAnnotation.class).size()>0){
            CoreMap sentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);
            for (CoreLabel tempToken : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String token = tempToken.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = tempToken.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the lemma of the token
                String lemma = tempToken.get(CoreAnnotations.LemmaAnnotation.class);
                if (posAbbrList.contains(pos)) {
                    tokenModelList.add(new TokenModel(token, pos, lemma));
                }
            }
        }
        return tokenModelList;
    }


    /**
     * Extract the comment description from the comment string.
     * @param commentStr
     * @return String
     * @date 2020/3/3 9:12 PM
     * @author xxx
     */
    public static String extractCommentDescription(String commentStr) {
        String commentDescription = "";
        if (commentStr.matches(REGEX_DOC_COMMENT)) {
            System.out.println("DOC_COMMENT");
            String[] lineArray = commentStr.split("\n");
            for (String line : lineArray) {
                line = line.trim();
                if (line.contains("/**")) {
                    line = line.substring(3).trim();
                } else {
                    line = line.substring(1).trim();
                }
                if ("".equals(line)) {
                    continue;
                }
                commentDescription = line;
                break;
            }
        } else if (commentStr.matches(REGEX_MULTI_LINE_COMMENT)) {
            System.out.println("MULTI_LINE_COMMENT");
            int start = commentStr.indexOf("/*");
            int end = commentStr.lastIndexOf("*/");
            commentStr = commentStr.substring(start + 2, end).trim();
            commentDescription = commentStr.replaceAll("\n", " ");
        } else if (commentStr.matches(REGEX_SINGLE_LINE_COMMENT)) {
            System.out.println("SINGLE_LINE_COMMENT");
            int start = commentStr.indexOf("//");
            commentDescription = commentStr.substring(start + 2).trim();
        } else {
            System.err.println("未识别的注释！");
        }
        return commentDescription;
    }

    /**
     * Judge whether the string contains the Chinese or Japanese.
     * @param
     * @return
     * @throws
     * @date 2020/4/24 1:49 AM
     * @author xxx
     */
    public boolean isContainOtherLanguage(String string) {
        Pattern p = Pattern.compile(REGEX_CHINESE_AND_JAPANESE);
        Matcher m = p.matcher(string);
        if (m.find()) {
            return true;
        }
        return false;
    }

}
