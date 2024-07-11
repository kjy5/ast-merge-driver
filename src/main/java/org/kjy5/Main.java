/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.Matchers;
import org.kjy5.parser.JavaParserGenerator;
import org.kjy5.tdm.ChangeSet;
import org.kjy5.tdm.ClassRepresentatives;

/**
 * Main class for the merge driver.
 *
 * <p>Reimplements Spork by Larsén et al. (2022) in Java with JavaParser.
 *
 * @author Kenneth Yang
 */
public class Main {
  // region File path constants.
  private static final String ASSETS_FOLDER_PATH = "assets/";
  private static final String BASE_FILE_PATH = "/file_base";
  private static final String LEFT_FILE_PATH = "/file_left";
  private static final String RIGHT_FILE_PATH = "/file_right";
  //  private static final String MERGED_FILE_PATH = "/file_merged";
  private static final String JAVA_FILE_EXTENSION = ".java";

  // endregion

  /**
   * Entry point of the program.
   *
   * <p>Runs the full pipeline
   *
   * @param args Command line arguments (folder name)
   */
  public static void main(String[] args) {
    // region File path specifications.
    final var folder = args.length > 0 ? args[0] : "0";

    // Source files.
    final var fileBasePath = ASSETS_FOLDER_PATH + folder + BASE_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileLeftPath = ASSETS_FOLDER_PATH + folder + LEFT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileRightPath = ASSETS_FOLDER_PATH + folder + RIGHT_FILE_PATH + JAVA_FILE_EXTENSION;
    //    final var fileMergedPath = ASSETS_FOLDER_PATH + folder + MERGED_FILE_PATH +
    // JAVA_FILE_EXTENSION;
    // endregion

    // region Create matching between branches.
    final var javaParserGenerator = new JavaParserGenerator();

    // Create parsings.
    final var baseTree = javaParserGenerator.ParseFileIntoTree(fileBasePath);
    final var leftTree = javaParserGenerator.ParseFileIntoTree(fileLeftPath);
    final var rightTree = javaParserGenerator.ParseFileIntoTree(fileRightPath);

    // TODO: Consider mapping from left/right to base to better follow class representative logic.
    // Match the trees.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseToLeftMapping = matcher.match(baseTree, leftTree);
    final var baseToRightMapping = matcher.match(baseTree, rightTree);
    final var leftToRightMapping = matcher.match(leftTree, rightTree);
    // endregion

    // region Create class representative mappings.
    var classRepresentatives =
        new ClassRepresentatives(
            baseTree,
            leftTree,
            rightTree,
            baseToLeftMapping,
            baseToRightMapping,
            leftToRightMapping);
    // endregion

    // region Create change sets (PCS and content tuples).
    var baseChangeSet = new ChangeSet(baseTree, classRepresentatives);
    var leftChangeSet = new ChangeSet(leftTree, classRepresentatives);
    var rightChangeSet = new ChangeSet(rightTree, classRepresentatives);
    // endregion

    // region Raw merge (union of all change sets).
    var mergedChangeSet = new ChangeSet(baseChangeSet, leftChangeSet, rightChangeSet);
    // endregion
  }
}
