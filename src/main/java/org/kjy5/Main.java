package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.Matchers;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;
import org.plumelib.javacparse.JavacParse;

@SuppressWarnings("StringTemplateMigration")
public class Main {
  public static void main(String[] args) {
    // Source files.
    String file1 = "assets/file1.java";
    String file2 = "assets/file2.java";

    // Parse file1 with Javac-parse.
    JCTree.JCCompilationUnit file1JavacTree;
    try {
      file1JavacTree = JavacParse.parseJavaFile(file1);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file" + e);
    }

    // Exit if parsing failed.
    if (file1JavacTree == null) {
      return;
    }

    // Parse file2 with Javac-parse.
    JCTree.JCCompilationUnit file2JavacTree;
    try {
      file2JavacTree = JavacParse.parseJavaFile(file2);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file" + e);
    }

    // Exit if parsing failed.
    if (file2JavacTree == null) {
      return;
    }

    // Process with visitor.
    var file1Tree = file1JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);
    var file2Tree = file2JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);

    // Compute mappings.
    Run.initMatchers();
    var defaultMatcher = Matchers.getInstance().getMatcher();
    var mappings = defaultMatcher.match(file1Tree, file2Tree);
    System.out.println(mappings);
  }
}
