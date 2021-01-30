package com.tbooster.utils;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Optional;

/**
  * @Author xxx
  * @Date 2020/8/23 11:02
  */
public class CodeUtil {

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2020/8/23 11:02
     * @author xxx
     */
    public static String cleanCommentAndAnnotation(String methodCode) {
        if (methodCode.indexOf("default ") != -1) {
            methodCode = methodCode.replace("default ", " ");
        }

        methodCode = "class MethodClass{" + methodCode + "}";
        CompilationUnit cu = null;
        try {
            cu = constructCompilationUnit(methodCode,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (cu == null) {
            return null;
        }
        Optional<MethodDeclaration> methodDeclaration = cu.findFirst(MethodDeclaration.class);
        if (methodDeclaration.isPresent()) {
            MethodDeclaration md = methodDeclaration.get();
            NodeList<AnnotationExpr> annotationExprNodeList = md.getAnnotations();
            Iterator<AnnotationExpr> iterator = annotationExprNodeList.iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
            md.getAllContainedComments().forEach(Comment::remove);
            if(md.getJavadocComment().isPresent()){
                md.getJavadocComment().get().remove();
            }
            return md.toString();
        }

        Optional<ConstructorDeclaration> constructorDeclaration = cu.findFirst(ConstructorDeclaration.class);
        if (constructorDeclaration.isPresent()) {
            ConstructorDeclaration cd = constructorDeclaration.get();
            NodeList<AnnotationExpr> annotationExprNodeList = cd.getAnnotations();
            Iterator<AnnotationExpr> iterator = annotationExprNodeList.iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
            cd.getAllContainedComments().forEach(Comment::remove);
            if(cd.getJavadocComment().isPresent()){
                cd.getJavadocComment().get().remove();
            }
            return cd.toString();
        }

        return null;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/8/23 11:02
      * @author xxx
      */
    public static CompilationUnit constructCompilationUnit(String code, String FILE_PATH) throws FileNotFoundException {
        String srcPath = "";
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(srcPath));
        combinedTypeSolver.add(javaParserTypeSolver);
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        CompilationUnit compilationUnit = null;
        try {
            if (code == null) {
                compilationUnit = StaticJavaParser.parse(new File(FILE_PATH));
            } else {
                compilationUnit = StaticJavaParser.parse(code);
            }
        } catch (ParseProblemException e) {
//            e.printStackTrace();
        }
        return compilationUnit;
    }
}
