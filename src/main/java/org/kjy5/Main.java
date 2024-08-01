/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import java.io.ByteArrayOutputStream;
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

    // TODO: current implementation assumes old and new content start at the same place. Need to
    // adjust for when they don't.

    // Create output buffer with compilation unit length (may need expanding later).
    var mergedBuffer = new byte[mergedTree.getLength()];

    // Read from merged tree.
    for (var node : mergedTree.preOrder()) {
      // Expand output buffer if necessary.
      if (node.getPos() + node.getLength() > mergedBuffer.length) {
        var newOutputBuffer = new byte[node.getPos() + node.getLength()];
        System.arraycopy(mergedBuffer, 0, newOutputBuffer, 0, mergedBuffer.length);
        mergedBuffer = newOutputBuffer;
      }

      // If node is a leaf and has content, use its content.
      if (node.isLeaf() && node.hasLabel()) {
        // Get content tuple for node.
        var maybeContentTuple =
            mergedChangeSet.contentTupleSet().stream()
                .filter(contentTuple -> contentTuple.node() == node)
                .findFirst();

        // Short-circuit if no content.
        if (maybeContentTuple.isEmpty()) continue;

        // Get content as bytes.
        var content = maybeContentTuple.get().content().getBytes();

        // Replace old content with new content.

        // Case 1: new content is longer. Need to expand buffer.
        if (content.length > node.getLength()) {
          // Make new buffer that is longer.
          var expandedBuffer = new byte[mergedBuffer.length + content.length - node.getLength()];

          // Copy old buffer up to content position.
          System.arraycopy(mergedBuffer, 0, expandedBuffer, 0, node.getPos());

          // Copy new content.
          System.arraycopy(content, 0, expandedBuffer, node.getPos(), content.length);

          // Copy old buffer after content position.
          System.arraycopy(
              mergedBuffer,
              node.getPos() + node.getLength(),
              expandedBuffer,
              node.getPos() + content.length,
              mergedBuffer.length - node.getPos() - node.getLength());

          // Update buffer.
          mergedBuffer = expandedBuffer;
        }

        // Case 2: new content is shorter or same length. Write-over old content and shrink buffer
        // (if needed).
        else {
          // Copy content in.
          System.arraycopy(content, 0, mergedBuffer, node.getPos(), content.length);

          // Clear excess old content.
          for (var i = node.getPos() + content.length; i < node.getPos() + node.getLength(); i++) {
            mergedBuffer[i] = 0;
          }
        }

        continue;
      }

      // TODO: need to identify what a structure is replacing. Adding new structures in a list won't
      // work though (e.g. adding new parameters to a method becuase the commas won't be generated).
      // Otherwise, read from file.
      try {
        // Open file to read from.
        var file = new RandomAccessFile(node.getMetadata("src").toString(), "r");
        file.seek(node.getPos());

        // Get node this is replacing.
        var replacingNode = (Tree) node.getMetadata("replacing");
        if (replacingNode == null) {
          // TODO: handle case where node is not replacing anything (i.e. more children in a list).
          // Read from file in output buffer.
          file.read(mergedBuffer, node.getPos(), node.getLength());
        } else {
          // Case 1: new content is longer than what it is replacing. Need to expand buffer.
          if (node.getLength() > replacingNode.getLength()) {
            // Make new buffer that is longer.
            var expandedBuffer =
                new byte[mergedBuffer.length + node.getLength() - replacingNode.getLength()];

            // Copy old buffer up to content position.
            System.arraycopy(mergedBuffer, 0, expandedBuffer, 0, node.getPos());

            // Copy new content.
            file.read(expandedBuffer, node.getPos(), node.getLength());

            // Copy old buffer after content position.
            System.arraycopy(
                mergedBuffer,
                node.getPos() + replacingNode.getLength(),
                expandedBuffer,
                node.getPos() + node.getLength(),
                mergedBuffer.length - node.getPos() - replacingNode.getLength());

            // Update buffer.
            mergedBuffer = expandedBuffer;
          }

          // Case 2: new content is shorter or same length. Write-over old content and shrink buffer
          // (if needed).
          else {
            // Copy content in.
            file.read(mergedBuffer, node.getPos(), node.getLength());

            // Clear excess old content.
            for (var i = node.getPos() + node.getLength();
                i < node.getPos() + replacingNode.getLength();
                i++) {
              mergedBuffer[i] = 0;
            }
          }
        }

        file.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Remove null bytes from buffer.
    var cleanedBufferOutputStream = new ByteArrayOutputStream();
    for (var b : mergedBuffer) {
      if (b != 0) cleanedBufferOutputStream.write(b);
    }

    // Write to file.
    System.out.println();
    System.out.println(cleanedBufferOutputStream);
    try {
      var mergedFile = new FileOutputStream(fileMergedPath);
      mergedFile.write(cleanedBufferOutputStream.toByteArray());
      mergedFile.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // endregion
  }
}
