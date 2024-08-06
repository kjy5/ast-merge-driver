package org.kjy5;

import com.github.gumtreediff.tree.Tree;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Map;
import org.kjy5.spork.ChangeSet;
import org.kjy5.spork.ContentTuple;

public class Printer {
  public static void print(
      Tree mergedTree,
      ChangeSet mergedChangeSet,
      String outputFilePath,
      Map<Tree, String> nodeToSourceFileMapping,
      Map<ContentTuple, String> contentTupleToSourceFileMapping) {
    // TODO: current implementation assumes old and new content start at the same place. Need to
    // adjust for when they don't.

    // Create output buffer.
    var mergedBuffer = new ArrayList<Byte>();

    // Read from merged tree.
    for (var node : mergedTree.preOrder()) {
      // If node is a leaf and has content, use its content.
      if (node.isLeaf() && node.hasLabel()) {
        // Get content tuple for node.
        var maybeContentTuple =
            mergedChangeSet.contentTupleSet().stream()
                .filter(contentTuple -> contentTuple.node() == node)
                .findFirst();

        // Short-circuit if no content.
        if (maybeContentTuple.isEmpty()) continue;

        // Get content tuple.
        var contentTuple = maybeContentTuple.get();

        // Declare content as bytes.
        var contentBytes = contentTuple.content().getBytes();

        // Check for conflicts.
        if (contentTuple.hardInconsistencyWith().isPresent()) {
          var conflict = contentTuple.hardInconsistencyWith().get();

          // Update content string to show conflict.
          contentBytes =
              ("<<<<<<< "
                      + contentTupleToSourceFileMapping.get(contentTuple)
                      + contentTuple.content()
                      + " ======= "
                      + conflict.content()
                      + " >>>>>>> "
                      + contentTupleToSourceFileMapping.get(conflict))
                  .getBytes();
        }

        // Delete old content.
        mergedBuffer.subList(node.getPos(), node.getPos() + node.getLength()).clear();

        // Insert new content.
        for (int i = 0; i < contentBytes.length; i++) {
          mergedBuffer.add(node.getPos() + i, contentBytes[i]);
        }

        // Skip to next node.
        continue;
      }

      // TODO: need to identify what a structure is replacing. Adding new structures in a list won't
      // work though (e.g. adding new parameters to a method because the commas won't be generated).
      // Otherwise, read from file.
      try {
        // Open file to read from.
        var file = new RandomAccessFile(nodeToSourceFileMapping.get(node), "r");
        file.seek(node.getPos());

        // Insertion index (changes to replacing node if there was a previous node).
        var insertionIndex = node.getPos();

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
