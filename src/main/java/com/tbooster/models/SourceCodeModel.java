package com.tbooster.models;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;

import java.util.List;

/**
 * @Author xxx
 * @Date 2020/5/28 12:01 PM
 */
public class SourceCodeModel {

    private PackageDeclaration packageDeclaration;
    private List<ImportDeclaration> importDeclarationList;

    private int classOrInterfaceOrEnum; // -1: empty file; 0: class; 1: interface; 2: enum;
    private String className;
    private String interfaceName;
    private ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
    private EnumDeclaration enumDeclaration;


    private List<ClassOrInterfaceDeclaration> innerClassList;
    private List<ClassOrInterfaceDeclaration> innerInterfaceList;
    private List<EnumDeclaration> innerEnumDeclarationList;


    public SourceCodeModel() {
    }

    public void setPackageDeclaration(PackageDeclaration packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
    }

    public PackageDeclaration getPackageDeclaration() {
        return packageDeclaration;
    }

    public List<ImportDeclaration> getImportDeclarationList() {
        return importDeclarationList;
    }

    public void setImportDeclarationList(List<ImportDeclaration> importDeclarationList) {
        this.importDeclarationList = importDeclarationList;
    }

    public int getClassOrInterfaceOrEnum() {
        return classOrInterfaceOrEnum;
    }

    public void setClassOrInterfaceOrEnum(int classOrInterfaceOrEnum) {
        this.classOrInterfaceOrEnum = classOrInterfaceOrEnum;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration() {
        return classOrInterfaceDeclaration;
    }

    public void setClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        this.classOrInterfaceDeclaration = classOrInterfaceDeclaration;
    }

    public EnumDeclaration getEnumDeclaration() {
        return enumDeclaration;
    }

    public void setEnumDeclaration(EnumDeclaration enumDeclaration) {
        this.enumDeclaration = enumDeclaration;
    }

    public List<ClassOrInterfaceDeclaration> getInnerClassList() {
        return innerClassList;
    }

    public void setInnerClassList(List<ClassOrInterfaceDeclaration> innerClassList) {
        this.innerClassList = innerClassList;
    }

    public List<ClassOrInterfaceDeclaration> getInnerInterfaceList() {
        return innerInterfaceList;
    }

    public void setInnerInterfaceList(List<ClassOrInterfaceDeclaration> innerInterfaceList) {
        this.innerInterfaceList = innerInterfaceList;
    }

    public List<EnumDeclaration> getInnerEnumDeclarationList() {
        return innerEnumDeclarationList;
    }

    public void setInnerEnumDeclarationList(List<EnumDeclaration> innerEnumDeclarationList) {
        this.innerEnumDeclarationList = innerEnumDeclarationList;
    }
}
