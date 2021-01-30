package com.tbooster.utils;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;

/**
  * @Author xxx
  * @Date 2020/6/5 3:34 PM
  */
public class JavaParserUtil {

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2020/6/5 3:36 PM
      * @author xxx
      */
    public static CompilationUnit constructCompilationUnit(String code, File file, File srcDirectory) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(srcDirectory);
        combinedTypeSolver.add(javaParserTypeSolver);
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        CompilationUnit compilationUnit = null;
        try {
            if (code == null) {
                compilationUnit = StaticJavaParser.parse(file);
            } else {
                compilationUnit = StaticJavaParser.parse(code);
            }
        } catch (ParseProblemException e) {
//            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return compilationUnit;
        }
    }
}
