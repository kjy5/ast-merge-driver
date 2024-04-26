package org.kjy5;

import com.sun.tools.javac.tree.JCTree;
import org.plumelib.javacparse.JavacParse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String file = "assets/file1.java";

        // Parse with Javac-parse.
        JCTree.JCCompilationUnit javacTree;
        try {
            javacTree = JavacParse.parseJavaFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file" + e);
        }

        // Exit if parsing failed.
        if (javacTree == null) {
            return;
        }

        // Process with visitor.
        JCTree.Visitor visitor = new ParseTreeVisitor();
        javacTree.accept(visitor);
    }
}