package org.kjy5;

import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import org.plumelib.javacparse.JavacParse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String file = "assets/file1.java";

        // Parse with JDT.
        Tree jdtTree;
        try {
            jdtTree = new JdtTreeGenerator().generateFrom().file(file).getRoot();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file" + e);
        }
        System.out.println(jdtTree.toTreeString());

        // Parse with JavaParser.
        Tree javaparserTree;
        try {
            javaparserTree = new JavaParserGenerator().generateFrom().file(file).getRoot();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file" + e);
        }
        System.out.println(javaparserTree.toTreeString());

        // Parse with Javac-parse.
        JCTree.JCCompilationUnit javacTree;
        try {
            javacTree = JavacParse.parseJavaFile(file);
            System.out.println(javacTree);
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file" + e);
        }
    }
}