package org.kjy5.driver;

import com.github.gumtreediff.gen.javaparser.JavaParserGenerator;
import com.github.gumtreediff.tree.Tree;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.kjy5.utils.Branch;

/**
 * Parsed Java source code into GumTree Trees.
 *
 * <p>Parsings are done with JavaParser and source code positions are updated to relative positions
 * from the closest sibling / parent.
 *
 * @param baseTree the base branch as a GumTree Tree
 * @param leftTree the left branch as a GumTree Tree
 * @param rightTree the right branch as a GumTree Tree
 * @param treeToSourceBranch the mapping of nodes to their source branches
 */
public record Parsings(
    Tree baseTree, Tree leftTree, Tree rightTree, Map<Tree, Branch> treeToSourceBranch) {
  /**
   * Create a Parsings object from the source file paths.
   *
   * @param baseSourceFilePath the base source file path
   * @param leftSourceFilePath the left source file path
   * @param rightSourceFilePath the right source file path
   * @return a new Parsings object with GumTree Trees with relative source code positions
   */
  public static Parsings from(
      String baseSourceFilePath, String leftSourceFilePath, String rightSourceFilePath) {
    final var javaParserGenerator = new JavaParserGenerator();

    // Create raw parsings.
    final Tree baseTree, leftTree, rightTree;
    try {
      baseTree = javaParserGenerator.generateFrom().file(baseSourceFilePath).getRoot();
      leftTree = javaParserGenerator.generateFrom().file(leftSourceFilePath).getRoot();
      rightTree = javaParserGenerator.generateFrom().file(rightSourceFilePath).getRoot();
    } catch (IOException e) {
      throw new RuntimeException("Unable to parse source code: " + e);
    }

    // Update node positions to relative positions.
    changeTreeSourceCodePositionToRelative(baseTree);
    changeTreeSourceCodePositionToRelative(leftTree);
    changeTreeSourceCodePositionToRelative(rightTree);

    // Mark down the source branch for each node.
    var nodeToSourceBranch = new LinkedHashMap<Tree, Branch>();
    nodeToSourceBranch.put(baseTree, Branch.BASE);
    nodeToSourceBranch.put(leftTree, Branch.LEFT);
    nodeToSourceBranch.put(rightTree, Branch.RIGHT);

    return new Parsings(baseTree, leftTree, rightTree, nodeToSourceBranch);
  }

  /**
   * Change a tree's source code position to be relative to its previous sibling or parent (if the
   * first child).
   *
   * <p>Update the given tree's children's position instead of itself.
   *
   * @param tree the tree to change the position of
   */
  private static void changeTreeSourceCodePositionToRelative(Tree tree) {
    // Exit if this tree is a leaf node.
    if (tree.isLeaf()) {
      return;
    }

    // For each child, update the position of their children first before updating themselves.
    for (int i = 0; i < tree.getChildren().size(); i++) {
      var child = tree.getChild(i);

      // Update the child's children first.
      changeTreeSourceCodePositionToRelative(child);

      // Update the child's position to be relative.
      if (i == 0) {
        // If this is the first child, set the position relative to the parent.
        child.setPos(child.getPos() - tree.getPos() - tree.getLength());
      } else {
        // Otherwise, set the position relative to the previous child.
        var previousChild = tree.getChild(i - 1);
        child.setPos(child.getPos() - previousChild.getPos() - previousChild.getLength());
      }
    }
  }
}
