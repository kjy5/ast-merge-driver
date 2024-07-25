/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import org.kjy5.spork.*;

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
  private static final String MERGED_FILE_PATH = "/file_merged";
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
    final var fileMergedPath = ASSETS_FOLDER_PATH + folder + MERGED_FILE_PATH + JAVA_FILE_EXTENSION;
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
      throw new RuntimeException("Unable to read source code: " + e);
    }

    // Annotate trees with their source files.
    baseTree.preOrder().forEach(node -> node.setMetadata("src", fileBasePath));
    leftTree.preOrder().forEach(node -> node.setMetadata("src", fileLeftPath));
    rightTree.preOrder().forEach(node -> node.setMetadata("src", fileRightPath));

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
        ClassRepresentatives.from(
                baseTree,
                leftTree,
                rightTree,
                baseToLeftMapping,
                baseToRightMapping,
                leftToRightMapping)
            .classRepresentativesMap();
    // endregion

    // region Create change sets (PCS and content tuples).

    // Virtual node mappings.
    var virtualRootMapping = new LinkedHashMap<Tree, Tree>();
    var ChildListVirtualNodesMapping = new LinkedHashMap<Tree, ChildListVirtualNodes>();

    final var baseChangeSet =
        ChangeSet.from(
            baseTree, classRepresentatives, virtualRootMapping, ChildListVirtualNodesMapping);
    final var leftChangeSet =
        ChangeSet.from(
            leftTree, classRepresentatives, virtualRootMapping, ChildListVirtualNodesMapping);
    final var rightChangeSet =
        ChangeSet.from(
            rightTree, classRepresentatives, virtualRootMapping, ChildListVirtualNodesMapping);
    System.out.println("State\tPCSs\tContentTuples");
    System.out.println(
        "Base\t" + baseChangeSet.pcsSet().size() + "\t\t" + baseChangeSet.contentTupleSet().size());
    System.out.println(
        "Left\t" + leftChangeSet.pcsSet().size() + "\t\t" + leftChangeSet.contentTupleSet().size());
    System.out.println(
        "Right\t"
            + rightChangeSet.pcsSet().size()
            + "\t\t"
            + rightChangeSet.contentTupleSet().size());
    System.out.println(
        "Total\t"
            + (baseChangeSet.pcsSet().size()
                + leftChangeSet.pcsSet().size()
                + rightChangeSet.pcsSet().size())
            + "\t\t"
            + (baseChangeSet.contentTupleSet().size()
                + leftChangeSet.contentTupleSet().size()
                + rightChangeSet.contentTupleSet().size()));
    // endregion

    // region Merge.
    final var mergedChangeSet =
        new Merger(baseChangeSet, leftChangeSet, rightChangeSet).getMergedChangeSet();
    System.out.println(
        "Merged\t"
            + mergedChangeSet.pcsSet().size()
            + "\t\t"
            + mergedChangeSet.contentTupleSet().size());
    // endregion

    // region Rebuild AST from merged change set.
    final var mergedTree = mergedChangeSet.toTree();
    System.out.println();
    mergedTree
        .preOrder()
        .forEach(node -> System.out.println(node.getMetadata("src") + ": " + node));
    // endregion

    // region Write merged tree to file.

    // Create output buffer with compilation unit length (may need expanding later).
    var mergedBuffer = new byte[mergedTree.getLength()];

    // Read from merged tree.
    for (var node : mergedTree.preOrder()) {
      try {
        // Open file to read from.
        var file = new RandomAccessFile(node.getMetadata("src").toString(), "r");
        file.seek(node.getPos());

        // Check if output buffer is large enough.
        if (mergedBuffer.length < node.getPos() + node.getLength()) {
          // Expand output buffer.
          var newOutputBuffer = new byte[node.getPos() + node.getLength()];
          System.arraycopy(mergedBuffer, 0, newOutputBuffer, 0, mergedBuffer.length);
          mergedBuffer = newOutputBuffer;
        }

        // Read from file in output buffer.
        file.read(mergedBuffer, node.getPos(), node.getLength());

        file.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Write to file.
    System.out.println();
    System.out.println(new String(mergedBuffer));
    try {
      var mergedFile = new FileOutputStream(fileMergedPath);
      mergedFile.write(mergedBuffer);
      mergedFile.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // endregion
  }
}
