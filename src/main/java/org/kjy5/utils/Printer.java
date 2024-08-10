package org.kjy5.utils;

import com.github.gumtreediff.tree.Tree;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.kjy5.spork.ContentTuple;

/** Printer for GumTree ASTs. */
public class Printer {
  /**
   * Print a GumTree AST to a file.
   *
   * <p>Requires AST to have relative positioning of nodes (position is relative to previous sibling
   * or parent).
   *
   * @param tree the AST to print
   * @param contentTuples the set of content tuples associated with this AST
   * @param outputFilePath the path to the output file
   * @param branchToSourceFile a mapping from branches to source files
   * @param treeToSourceBranch a mapping from trees to source branches
   * @param contentTupleToSourceFile a mapping from content tuples to source files
   */
  public static void print(
      Tree tree,
      Set<ContentTuple> contentTuples,
      String outputFilePath,
      Map<Branch, String> branchToSourceFile,
      Map<Tree, Branch> treeToSourceBranch,
      Map<ContentTuple, Branch> contentTupleToSourceFile) {
    // TODO: current implementation assumes old and new content start at the same place. Need to
    // adjust for when they don't.

    // Get sources as strings.
    var branchToSourceCode = new LinkedHashMap<Branch, String>();
    try {
      branchToSourceCode.put(
          Branch.BASE,
          new String(Files.readAllBytes(Paths.get(branchToSourceFile.get(Branch.BASE)))));
      branchToSourceCode.put(
          Branch.LEFT,
          new String(Files.readAllBytes(Paths.get(branchToSourceFile.get(Branch.LEFT)))));
      branchToSourceCode.put(
          Branch.RIGHT,
          new String(Files.readAllBytes(Paths.get(branchToSourceFile.get(Branch.RIGHT)))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    final var printData =
        new PrintData(
            tree,
            contentTuples,
            outputFilePath,
            branchToSourceCode,
            treeToSourceBranch,
            contentTupleToSourceFile);

    // Create output buffer.
    // Starts off by printing the entirety of one of the sources (depending on where the root of the
    // AST came from) then overwrites parts of it with the other sources based on the AST.
    var outputBuffer = new StringBuffer();

    // Why is this an array?  Wouldn't a StringBuilder or StringBuffer suffice?
    // Why does this code manipulate bytes instead of characters?
    // Create output buffer.
    // What is the invariant about the value of mergedBuffer?  That is, at every point in the code,
    // how much content has been copied into `mergedBuffer`?
    var mergedBuffer = new ArrayList<Byte>();

    // Read from tree.
    for (var subtree : tree.preOrder()) {
      // If subtree is a leaf and has content, use its content.
      if (subtree.isLeaf() && subtree.hasLabel()) {
        // Abstract this body into a method, to make the implementation of `print()` shorter and
        // easier to read.
        // Get content tuple for node.
        var maybeContentTuple =
            contentTuples.stream()
                .filter(contentTuple -> contentTuple.node() == subtree)
                .findFirst();

        // Short-circuit if no content.
        if (maybeContentTuple.isEmpty()) continue;

        // Get content tuple.
        var contentTuple = maybeContentTuple.get();

        // What does it mean to "Declare"?
        // Declare content as bytes.
        var contentBytes = contentTuple.content().getBytes();

        // Check for conflicts.
        if (contentTuple.hardInconsistencyWith() != null) {
          var conflict = contentTuple.hardInconsistencyWith();

          // Update content string to show conflict.
          contentBytes =
              ("<<<<<<< "
                      + contentTupleToSourceFile.get(contentTuple)
                      + contentTuple.content()
                      + " ======= "
                      + conflict.content()
                      + " >>>>>>> "
                      + contentTupleToSourceFile.get(conflict))
                  .getBytes();
        }

        // Delete old content.
        // I am concerned about the use of `node.getPos()`.  Is the value constantly updated to
        // account for all previous insertions and deletions?
        // A better design choice than writing everything to mergedBuffer in advance (if that is
        // what the invariant for mergedBuffer is) would be to fill it up on demand.
        // An alternative but probably uglier approach would be to make mergedBuffer an array of
        // strings.  Initially every string is a single character.  You ensure that the i'th element
        // of merge is always the i'th character of the file, by doing replacements like such:  to
        // replace characters 22 through 25, set those array elements to "", then do the insertion
        // all at character 22.
        mergedBuffer.subList(subtree.getPos(), subtree.getPos() + subtree.getLength()).clear();

        // Insert new content.
        // Minor: inserting into the middle of a String is very inefficient.  A StringBuilder or
        // StringBuffer can be efficiently inserted into the middle of.  But perhaps you don't need
        // to do any inserting into the middle, only appending to the end.
        for (int i = 0; i < contentBytes.length; i++) {
          mergedBuffer.add(subtree.getPos() + i, contentBytes[i]);
        }

        // Skip to next node.
        continue;
      }

      // What is a "structure"?
      // TODO: need to identify what a structure is replacing. Adding new structures in a list won't
      // work though (e.g. adding new parameters to a method because the commas won't be generated).
      // Otherwise, read from file.
      try {
        // Open file to read from.
        var file =
            new RandomAccessFile(branchToSourceFile.get(treeToSourceBranch.get(subtree)), "r");
        file.seek(subtree.getPos());

        // Insertion index (changes to replacing node if there was a previous node).
        var insertionIndex = subtree.getPos();

        // Is the replaced node in the left?  In the right?
        // Get node this is replacing.
        var replacingNode = (Tree) subtree.getMetadata("replacing");

        // Delete old content if there is a node to be replaced.
        if (replacingNode != null) {
          mergedBuffer
              .subList(replacingNode.getPos(), replacingNode.getPos() + replacingNode.getLength())
              .clear();

          // Update insertion index to be replacing node's position.
          insertionIndex = replacingNode.getPos();
        }

        // Get new content.
        var newContent = new byte[subtree.getLength()];
        file.read(newContent);

        // Insert into buffer.
        for (int i = 0; i < newContent.length; i++) {
          mergedBuffer.add(insertionIndex + i, newContent[i]);
        }

        // Close file.
        file.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // How do null bytes arise in the buffer?
    // Remove null bytes from buffer.
    var cleanedBufferOutputStream = new ByteArrayOutputStream();
    for (var b : mergedBuffer) {
      if (b != 0) cleanedBufferOutputStream.write(b);
    }

    // Write to file.
    System.out.println();
    System.out.println("Merged result:");
    System.out.println(cleanedBufferOutputStream);
    try {
      var mergedFile = new FileOutputStream(outputFilePath);
      mergedFile.write(cleanedBufferOutputStream.toByteArray());
      mergedFile.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void print(PrintData printData, StringBuffer outputBuffer, int cursor) {
    // Declare content to print.
    var content = "";

    // If tree is a leaf and has contents, print it.
    if (printData.tree.isLeaf() && printData.tree.hasLabel()) {
      var maybeContentTuple =
          printData.contentTuples.stream()
              .filter(contentTuple -> contentTuple.node() == printData.tree)
              .findFirst();

      // Exit if no content.
      if (maybeContentTuple.isEmpty()) return;

      // Get content tuple.
      var contentTuple = maybeContentTuple.get();

      // Get content string.
      content = contentTuple.content();

      // Check for conflicts.
      if (contentTuple.hardInconsistencyWith() != null) {
        var conflictTuple = contentTuple.hardInconsistencyWith();

        // Update content string to show conflict.
        content =
            "\n<<<<<<< "
                + printData.treeToSourceBranch.get(contentTuple.node())
                + "\n"
                + content
                + "\n ======= \n"
                + conflictTuple.content()
                + "\n >>>>>>> "
                + printData.treeToSourceBranch.get(conflictTuple.node());
      }

      // Remove text that was already printed.
      outputBuffer.delete(
          cursor + printData.tree.getPos(),
          cursor + printData.tree.getPos() + printData.tree.getLength());
    } else {
      // Get node this is replacing (getting the structure that has already been printed).
      var replacingNode = (Tree) printData.tree.getMetadata("replacing");

      // If there is a node to replace, remove it.
      if (replacingNode != null) {
        outputBuffer.delete(
            cursor + replacingNode.getPos(),
            cursor + replacingNode.getPos() + replacingNode.getLength());
      }

      // TODO: During parsing need to extract source code and put it into metadata.
      // Get new content.
      content =
          printData
              .branchToSourceCode
              .get(printData.treeToSourceBranch.get(printData.tree))
              .substring(
                  cursor + printData.tree.getPos(),
                  cursor + printData.tree.getPos() + printData.tree.getLength());
    }

    // Write content to buffer.
    outputBuffer.insert(cursor + printData.tree.getPos(), content);
  }

  private record PrintData(
      Tree tree,
      Set<ContentTuple> contentTuples,
      String outputFilePath,
      Map<Branch, String> branchToSourceCode,
      Map<Tree, Branch> treeToSourceBranch,
      Map<ContentTuple, Branch> contentTupleToSourceFile) {}
}
