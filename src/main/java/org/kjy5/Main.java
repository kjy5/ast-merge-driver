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

  // What is the "full pipeline"?
  // What does the given folder contain?
  // Document that the folder name must be relative, not absolute.
  /**
   * Entry point of the program.
   *
   * <p>Runs the full pipeline.
   *
   * @param args Command line arguments (folder name)
   */
  public static void main(String[] args) {
    // region File path specifications.
    // This should err if more than one command-line argument is provided.
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
      // Should this be "parse" rather than "read"?
      throw new RuntimeException("Unable to read source code: " + e);
    }

    // Annotate trees with their source files.
    var nodeToSourceFileMapping = new HashMap<Tree, String>();
    baseTree.preOrder().forEach(node -> nodeToSourceFileMapping.put(node, fileBasePath));
    leftTree.preOrder().forEach(node -> nodeToSourceFileMapping.put(node, fileLeftPath));
    rightTree.preOrder().forEach(node -> nodeToSourceFileMapping.put(node, fileRightPath));

    // What is "class representative logic"?
    // TODO: Consider mapping from left/right to base to better follow class representative logic.
    // Match the trees.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseToLeftMapping = matcher.match(baseTree, leftTree);
    final var baseToRightMapping = matcher.match(baseTree, rightTree);
    final var leftToRightMapping = matcher.match(leftTree, rightTree);
    // endregion

    // It seems inconsistent that the variables above have "Mapping" in their name but this one does
    // not.  In general, I don't think "Mapping" is necessary; a name like "baseToLeft" is
    // sufficiently expressive.  In any event, please be consistent.
    // region Create class representative mappings.
    final var classRepresentatives =
        ClassRepresentatives.from(
            baseTree,
            leftTree,
            rightTree,
            baseToLeftMapping,
            baseToRightMapping,
            leftToRightMapping);
    // endregion

    // region Create change sets (PCS and content tuples).

    // Virtual node mappings.
    var virtualRootMapping = new LinkedHashMap<Tree, Tree>();
    var ChildListVirtualNodesMapping = new LinkedHashMap<Tree, ChildListVirtualNodes>();

    // Would it be better to include the string in the ContentTuple?  Why or why not?  Also, is a
    // String necessary, or could it be an enum (BASE, LEFT, RIGHT, maybe MERGED)?
    // Content tuple source file mapping.
    var contentTupleToSourceFileMapping = new HashMap<ContentTuple, String>();

    final var baseChangeSet =
        ChangeSet.from(
            baseTree,
            classRepresentatives,
            virtualRootMapping,
            nodeToSourceFileMapping,
            contentTupleToSourceFileMapping,
            ChildListVirtualNodesMapping);
    final var leftChangeSet =
        ChangeSet.from(
            leftTree,
            classRepresentatives,
            virtualRootMapping,
            nodeToSourceFileMapping,
            contentTupleToSourceFileMapping,
            ChildListVirtualNodesMapping);
    final var rightChangeSet =
        ChangeSet.from(
            rightTree,
            classRepresentatives,
            virtualRootMapping,
            nodeToSourceFileMapping,
            contentTupleToSourceFileMapping,
            ChildListVirtualNodesMapping);
    System.out.println("State\t# PCSs\t# ContentTuples");
    System.out.println(
        // Why are there two "\t" in a row here, but there was only one in the header line that was
        // printed just above?
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
    final var mergedChangeSet = Merger.merge(baseChangeSet, leftChangeSet, rightChangeSet);
    System.out.println(
        "Merged\t"
            + mergedChangeSet.pcsSet().size()
            + "\t\t"
            + mergedChangeSet.contentTupleSet().size());
    // endregion

    // region Rebuild AST from merged change set.
    final var mergedTree = mergedChangeSet.toTree();
    System.out.println();
    System.out.println("Merged tree:");
    mergedTree
        .preOrder()
        .forEach(node -> System.out.println(nodeToSourceFileMapping.get(node) + ": " + node));
    // endregion

    // region Write merged tree to file.
    Printer.print(
        mergedTree,
        mergedChangeSet,
        fileMergedPath,
        nodeToSourceFileMapping,
        contentTupleToSourceFileMapping);
    // endregion
  }
}
