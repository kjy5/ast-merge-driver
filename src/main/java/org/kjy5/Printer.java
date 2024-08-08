package org.kjy5;

import com.github.gumtreediff.tree.Tree;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.kjy5.spork.ContentTuple;

/** Printer for GumTree ASTs. */
public class Printer {
  /**
   * Print a GumTree AST to a file.
   *
   * @param tree The AST to print.
   * @param contentTuples The set of content tuples associated with this AST.
   * @param outputFilePath The path to the output file.
   * @param nodeToSourceFile A mapping from nodes to source files.
   * @param contentTupleToSourceFile A mapping from content tuples to source files.
   */
  public static void print(
      Tree tree,
      Set<ContentTuple> contentTuples,
      String outputFilePath,
      Map<Tree, String> nodeToSourceFile,
      Map<ContentTuple, String> contentTupleToSourceFile) {
    // TODO: current implementation assumes old and new content start at the same place. Need to
    // adjust for when they don't.

    // Why is this an array?  Wouldn't a StringBuilder or StringBuffer suffice?
    // Why does this code manipulate bytes instead of characters?
    // Create output buffer.
    // What is the invariant about the value of mergedBuffer?  That is, at every point in the code,
    // how much content has been copied into `mergedBuffer`?
    var mergedBuffer = new ArrayList<Byte>();

    // Read from merged tree.
    // If "node" must come from a merged tre, I would name it "mergedNode".
    for (var node : tree.preOrder()) {
      // If node is a leaf and has content, use its content.
      if (node.isLeaf() && node.hasLabel()) {
        // Abstract this body into a method, to make the implementation of `print()` shorter and
        // easier to read.
        // Get content tuple for node.
        var maybeContentTuple =
            contentTuples.stream().filter(contentTuple -> contentTuple.node() == node).findFirst();

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
        mergedBuffer.subList(node.getPos(), node.getPos() + node.getLength()).clear();

        // Insert new content.
        // Minor: inserting into the middle of a String is very inefficient.  A StringBuilder or
        // StringBuffer can be efficiently inserted into the middle of.  But perhaps you don't need
        // to do any inserting into the middle, only appending to the end.
        for (int i = 0; i < contentBytes.length; i++) {
          mergedBuffer.add(node.getPos() + i, contentBytes[i]);
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
        var file = new RandomAccessFile(nodeToSourceFile.get(node), "r");
        file.seek(node.getPos());

        // Insertion index (changes to replacing node if there was a previous node).
        var insertionIndex = node.getPos();

        // Is the replaced node in the left?  In the right?
        // Get node this is replacing.
        var replacingNode = (Tree) node.getMetadata("replacing");

        // Delete old content if there is a node to be replaced.
        if (replacingNode != null) {
          mergedBuffer
              .subList(replacingNode.getPos(), replacingNode.getPos() + replacingNode.getLength())
              .clear();

          // Update insertion index to be replacing node's position.
          insertionIndex = replacingNode.getPos();
        }

        // Get new content.
        var newContent = new byte[node.getLength()];
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
}
