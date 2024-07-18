/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import java.util.LinkedList;
import org.kjy5.parser.JavaParserGenerator;
import org.kjy5.spork.ChangeSet;
import org.kjy5.spork.ClassRepresentatives;
import org.kjy5.spork.Merger;

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
    final var classRepresentatives =
        new ClassRepresentatives(
                baseTree,
                leftTree,
                rightTree,
                baseToLeftMapping,
                baseToRightMapping,
                leftToRightMapping)
            .getMapping();
    // endregion

    // region Create change sets (PCS and content tuples).
    final var baseChangeSet = new ChangeSet(baseTree, classRepresentatives);
    final var leftChangeSet = new ChangeSet(leftTree, classRepresentatives);
    final var rightChangeSet = new ChangeSet(rightTree, classRepresentatives);
    System.out.println("State\tPCSs\tContentTuples");
    System.out.println(
        "Base\t" + baseChangeSet.pcsSet.size() + "\t\t" + baseChangeSet.contentTupleSet.size());
    System.out.println(
        "Left\t" + leftChangeSet.pcsSet.size() + "\t\t" + leftChangeSet.contentTupleSet.size());
    System.out.println(
        "Right\t" + rightChangeSet.pcsSet.size() + "\t\t" + rightChangeSet.contentTupleSet.size());
    System.out.println(
        "Total\t"
            + (baseChangeSet.pcsSet.size()
                + leftChangeSet.pcsSet.size()
                + rightChangeSet.pcsSet.size())
            + "\t\t"
            + (baseChangeSet.contentTupleSet.size()
                + leftChangeSet.contentTupleSet.size()
                + rightChangeSet.contentTupleSet.size()));
    // endregion

    // region Merge.
    final var mergedChangeSet =
        new Merger(baseChangeSet, leftChangeSet, rightChangeSet).getMergedChangeSet();
    System.out.println(
        "Merged\t"
            + mergedChangeSet.pcsSet.size()
            + "\t\t"
            + mergedChangeSet.contentTupleSet.size());
    // endregion

    // region Rebuild AST from merged change set.
    var maybeRootPcs =
        mergedChangeSet.pcsSet.stream()
            .filter(
                pcs ->
                    pcs.parent().getLabel().contains("virtualRoot")
                        && pcs.child().getLabel().contains("virtualChildListStart"))
            .findFirst();
    if (maybeRootPcs.isPresent()) {
      var rootTree = maybeRootPcs.get().successor();
      var rebuiltTree = rootTree.deepCopy();
      var children = new LinkedList<Tree>();

      // Find the first child.
      var maybeFirstChildPcs =
          mergedChangeSet.pcsSet.stream()
              .filter(
                  pcs ->
                      pcs.parent().equals(rootTree)
                          && pcs.child().getLabel().contains("virtualChildListStart"))
              .findFirst();
      if (maybeFirstChildPcs.isPresent()) {
        var firstChild = maybeFirstChildPcs.get().successor();
        children.add(firstChild);
      }
    }
    // endregion
  }
}
