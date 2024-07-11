package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import java.io.IOException;

public class Main {
  // Constants for the file paths.
  private static final String ASSETS_FOLDER_PATH = "assets/";
  private static final String BASE_FILE_PATH = "/file_base";
  private static final String LEFT_FILE_PATH = "/file_left";
  private static final String RIGHT_FILE_PATH = "/file_right";
  private static final String MERGED_FILE_PATH = "/file_merged";
  private static final String JAVA_FILE_EXTENSION = ".java";
  private static final String XML_FILE_EXTENSION = ".xml";

  /**
   * Entry point of the program.
   *
   * <p>Runs the full pipeline
   *
   * @param args Command line arguments (folder name)
   */
  public static void main(String[] args) {
    // region File path specifications
    final var folder = args.length > 0 ? args[0] : "0";

    // Source files.
    final var fileBasePath = ASSETS_FOLDER_PATH + folder + BASE_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileLeftPath = ASSETS_FOLDER_PATH + folder + LEFT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileRightPath = ASSETS_FOLDER_PATH + folder + RIGHT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileMergedPath = ASSETS_FOLDER_PATH + folder + MERGED_FILE_PATH + JAVA_FILE_EXTENSION;

    // XML output files.
    final var fileBaseXmlPath = ASSETS_FOLDER_PATH + folder + BASE_FILE_PATH + XML_FILE_EXTENSION;
    final var fileLeftXmlPath = ASSETS_FOLDER_PATH + folder + LEFT_FILE_PATH + XML_FILE_EXTENSION;
    final var fileRightXmlPath = ASSETS_FOLDER_PATH + folder + RIGHT_FILE_PATH + XML_FILE_EXTENSION;
    final var fileMergedXmlPath = ASSETS_FOLDER_PATH + folder + MERGED_FILE_PATH + XML_FILE_EXTENSION;
    // endregion

    // region Create matching between branches.
    final var javaParserGenerator = new JavaParserGenerator();

    // Create parsings.
    final Tree baseTree, leftTree, rightTree;
    try {
      baseTree = javaParserGenerator.generateFrom().file(fileBasePath).getRoot();
      leftTree = javaParserGenerator.generateFrom().file(fileLeftPath).getRoot();
      rightTree = javaParserGenerator.generateFrom().file(fileRightPath).getRoot();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Match the trees.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseLeftMapping = matcher.match(baseTree, leftTree);
    final var baseRightMapping = matcher.match(baseTree, rightTree);
    final var leftRightMapping = matcher.match(leftTree, rightTree);

    // Print mappings.
    System.out.println(baseLeftMapping);

    // endregion
  }
}
