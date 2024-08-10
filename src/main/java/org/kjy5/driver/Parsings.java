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
    baseTree.preOrder().forEach(tree -> nodeToSourceBranch.put(tree, Branch.BASE));
    leftTree.preOrder().forEach(tree -> nodeToSourceBranch.put(tree, Branch.LEFT));
    rightTree.preOrder().forEach(tree -> nodeToSourceBranch.put(tree, Branch.RIGHT));

    // Return the parsings and the mapping of nodes to their source branches.
    return new Parsings(baseTree, leftTree, rightTree, nodeToSourceBranch);
  }

  /**
   * Change a tree's source code position to be relative to its previous sibling or parent (if the
   * first child).
   *
   * <p>Starting from the leafs, work up the tree to ensure that the position of each node is
   * relative to its previous sibling or if it's the first child, relative to the parent.
   *
   * @param tree the tree to change the position of
   */
  private static void changeTreeSourceCodePositionToRelative(Tree tree) {
    // Exit if this tree is a leaf node.
    if (tree.isLeaf()) {
      return;
    }

    // Ensure own position range covers children's range.
    tree.setPos(Math.min(tree.getChild(0).getPos(), tree.getPos()));
    tree.setLength(
        Math.max(
            tree.getChild(tree.getChildren().size() - 1).getPos()
                + tree.getChild(tree.getChildren().size() - 1).getLength()
                - tree.getPos(),
            tree.getLength()));

    // Loop through each child in reverse order.
    for (int i = tree.getChildren().size() - 1; i >= 0; --i) {
      // Get this child (start from the back and work to front).
      var child = tree.getChild(i);

      // Update the child's children first.
      changeTreeSourceCodePositionToRelative(child);

      if (i == 0) {
        // First child's position is relative to the parent.
        child.setPos(child.getPos() - tree.getPos());
      } else {
        // All other children's position are relative to the previous sibling.
        var previousChild = tree.getChild(i - 1);
        child.setPos(child.getPos() - previousChild.getPos() - previousChild.getLength());
      }
    }
  }
}
