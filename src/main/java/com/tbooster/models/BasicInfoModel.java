package com.tbooster.models;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
  * @Author xxx
  * @Date 2020/6/17 10:39 AM
  */
public class BasicInfoModel {

    private String name;
    private NodeList<Modifier> modifiers;
    private NodeList<ClassOrInterfaceType> implementedTypes;
    private NodeList<ClassOrInterfaceType> extendedTypes;
    private NodeList<AnnotationExpr> annotationExprs;

    public BasicInfoModel() {
    }

    public BasicInfoModel(NodeList<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public BasicInfoModel(String name, NodeList<Modifier> modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    public BasicInfoModel(String name, NodeList<Modifier> modifiers, NodeList<ClassOrInterfaceType> implementedTypes) {
        this.name = name;
        this.modifiers = modifiers;
        this.implementedTypes = implementedTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeList<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(NodeList<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public NodeList<ClassOrInterfaceType> getImplementedTypes() {
        return implementedTypes;
    }

    public void setImplementedTypes(NodeList<ClassOrInterfaceType> implementedTypes) {
        this.implementedTypes = implementedTypes;
    }

    public NodeList<ClassOrInterfaceType> getExtendedTypes() {
        return extendedTypes;
    }

    public void setExtendedTypes(NodeList<ClassOrInterfaceType> extendedTypes) {
        this.extendedTypes = extendedTypes;
    }

    public NodeList<AnnotationExpr> getAnnotationExprs() {
        return annotationExprs;
    }

    public void setAnnotationExprs(NodeList<AnnotationExpr> annotationExprs) {
        this.annotationExprs = annotationExprs;
    }
}
