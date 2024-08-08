/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.kjy5.spork.*;

/**
 * Main class for the merge driver.
 *
 * <p>Reimplements Spork by Larsén et al. (2022) in Java.
 *
 * @author Kenneth Yang
 */
public class Main {
  // region Constants.
  private static final String RESOURCES_FOLDER_PATH = "resources/";
  private static final String BASE_FILE_PATH = "/file_base";
  private static final String LEFT_FILE_PATH = "/file_left";
  private static final String RIGHT_FILE_PATH = "/file_right";
  private static final String MERGED_FILE_PATH = "/file_merged";
  private static final String JAVA_FILE_EXTENSION = ".java";

  private static final String MERGE_TABLE_FORMAT = "%-10s%-10s%-15s%n";

  // endregion

  /**
   * Entry point of the program.
   *
   * <p>Runs the full Spork algorithm.
   *
   * @param args Command line arguments (test folder name relative to the "resources/" directory)
   */
  public static void main(String[] args) {
    // region File path specifications.

    // Throw error if the number of arguments is not 1.
    if (args.length != 1) {
      throw new IllegalArgumentException("Expected 1 argument, but got " + args.length);
    }

    // Get the folder name from the command line arguments.
    final var folder = args[0];

    // Source files.
    final var fileBasePath = RESOURCES_FOLDER_PATH + folder + BASE_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileLeftPath = RESOURCES_FOLDER_PATH + folder + LEFT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileRightPath =
        RESOURCES_FOLDER_PATH + folder + RIGHT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileMergedPath =
        RESOURCES_FOLDER_PATH + folder + MERGED_FILE_PATH + JAVA_FILE_EXTENSION;
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
      throw new RuntimeException("Unable to parse source code: " + e);
    }

    // Annotate trees with their source files.
    var nodeToSourceFile = new HashMap<Tree, String>();
    baseTree.preOrder().forEach(node -> nodeToSourceFile.put(node, fileBasePath));
    leftTree.preOrder().forEach(node -> nodeToSourceFile.put(node, fileLeftPath));
    rightTree.preOrder().forEach(node -> nodeToSourceFile.put(node, fileRightPath));

    // TODO: Consider mapping from left/right to base to better follow usage direction later.
    // Match the trees.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseToLeft = matcher.match(baseTree, leftTree);
    final var baseToRight = matcher.match(baseTree, rightTree);
    final var leftToRight = matcher.match(leftTree, rightTree);
    // endregion

    // region Create class representative mappings.
    final var nodeToClassRepresentatives =
        ClassRepresentatives.from(
            baseTree, leftTree, rightTree, baseToLeft, baseToRight, leftToRight);
    // endregion

    // region Create change sets (PCS and content tuples).

    // Virtual node mappings.
    var astRootToVirtualRoot = new LinkedHashMap<Tree, Tree>();
    var nodeToChildListVirtualNodes = new LinkedHashMap<Tree, ChildListVirtualNodes>();

    // Content tuple source file mapping.
    var contentTupleToSourceFile = new HashMap<ContentTuple, String>();

    final var baseChangeSet =
        ChangeSet.from(
            baseTree,
            nodeToClassRepresentatives,
            astRootToVirtualRoot,
            nodeToSourceFile,
            contentTupleToSourceFile,
            nodeToChildListVirtualNodes);
    final var leftChangeSet =
        ChangeSet.from(
            leftTree,
            nodeToClassRepresentatives,
            astRootToVirtualRoot,
            nodeToSourceFile,
            contentTupleToSourceFile,
            nodeToChildListVirtualNodes);
    final var rightChangeSet =
        ChangeSet.from(
            rightTree,
            nodeToClassRepresentatives,
            astRootToVirtualRoot,
            nodeToSourceFile,
            contentTupleToSourceFile,
            nodeToChildListVirtualNodes);
    System.out.format(MERGE_TABLE_FORMAT, "State", "# PCSs", "# ContentTuples");
    System.out.format(
        MERGE_TABLE_FORMAT,
        "Base",
        baseChangeSet.pcsSet().size(),
        baseChangeSet.contentTupleSet().size());
    System.out.format(
        MERGE_TABLE_FORMAT,
        "Left",
        leftChangeSet.pcsSet().size(),
        leftChangeSet.contentTupleSet().size());
    System.out.format(
        MERGE_TABLE_FORMAT,
        "Right",
        rightChangeSet.pcsSet().size(),
        rightChangeSet.contentTupleSet().size());
    System.out.format(
        MERGE_TABLE_FORMAT,
        "Total",
        baseChangeSet.pcsSet().size()
            + leftChangeSet.pcsSet().size()
            + rightChangeSet.pcsSet().size(),
        baseChangeSet.contentTupleSet().size()
            + leftChangeSet.contentTupleSet().size()
            + rightChangeSet.contentTupleSet().size());
    // endregion

    // region Merge.
    final var mergedChangeSet = Merger.merge(baseChangeSet, leftChangeSet, rightChangeSet);
    System.out.format(
        MERGE_TABLE_FORMAT,
        "Merged",
        mergedChangeSet.pcsSet().size(),
        mergedChangeSet.contentTupleSet().size());
    // endregion

    // region Rebuild AST from merged change set.
    final var mergedTree = mergedChangeSet.toTree();
    System.out.println();
    System.out.println("Merged tree:");
    mergedTree
        .preOrder()
        .forEach(node -> System.out.println(nodeToSourceFile.get(node) + ": " + node));
    // endregion

    // region Write merged tree to file.
    Printer.print(
        mergedTree,
        mergedChangeSet.contentTupleSet(),
        fileMergedPath,
        nodeToSourceFile,
        contentTupleToSourceFile);
    // endregion
  }
}
