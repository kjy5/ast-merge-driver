package org.kjy5;

import com.github.gumtreediff.tree.Tree;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.kjy5.spork.ChangeSet;

public class Printer {
  public static void print(Tree mergedTree, ChangeSet mergedChangeSet, String outputFilePath) {
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

        // Get content tuple.
        var contentTuple = maybeContentTuple.get();

        // Declare content as bytes.
        var content = contentTuple.content().getBytes();

        // Check for conflicts.
        //        if (contentTuple.hardInconsistencyWith().isPresent()){
        //          var conflict = contentTuple.hardInconsistencyWith().get();
        //
        //          // Update content string to show conflict.
        //          content = ("<<<<<<< "+ contentTuple.src() + Arrays.toString(content) + " =======
        // " + conflict.content() + " >>>>>>> " + conflict.src()).getBytes();
        //        }

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
      var mergedFile = new FileOutputStream(outputFilePath);
      mergedFile.write(cleanedBufferOutputStream.toByteArray());
      mergedFile.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
