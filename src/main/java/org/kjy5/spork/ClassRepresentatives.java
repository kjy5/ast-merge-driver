/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class representative nodes in a merge.
 *
 * <p>"Class representatives" are AST nodes that are chosen to represent the same node between the
 * three branches of a merge. They are used to collapse the reference of matching nodes between the
 * three branches into a single node.
 *
 * @author Kenneth Yang
 */
public class ClassRepresentatives {
  /**
   * Create mapping from nodes in the three branches to their class representatives.
   *
   * @param baseTree the base tree
   * @param leftTree the left tree
   * @param rightTree the right tree
   * @param baseToLeft the match mapping from base to left
   * @param baseToRight the match mapping from base to right
   * @param leftToRight the match mapping from left to right
   */
  public static Map<Tree, Tree> from(
      Tree baseTree,
      Tree leftTree,
      Tree rightTree,
      MappingStore baseToLeft,
      MappingStore baseToRight,
      MappingStore leftToRight) {
    // Initialize an empty class representatives mapping.
    var nodeToClassRepresentative = new LinkedHashMap<Tree, Tree>();

    // Nodes in the base tree are mapped to themselves.
    baseTree.preOrder().forEach(node -> nodeToClassRepresentative.put(node, node));

    // Left nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
    mapToBaseOrToSelf(leftTree, baseToLeft, nodeToClassRepresentative);

    // Right nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
    mapToBaseOrToSelf(rightTree, baseToRight, nodeToClassRepresentative);

    // Map right nodes to left nodes if their parents are mapped to the same class representative.
    // Use breadth-first ordering to ensure parents have been handled before looking at children.
    for (var leftNode : leftTree.breadthFirst()) {
      // Skip if the left node is already mapped to base.
      if (nodeToClassRepresentative.get(leftNode) != leftNode) continue;

      // Skip if the left node is not mapped to right.
      if (!leftToRight.isSrcMapped(leftNode)) continue;

      // Find the matched right node (could be null).
      final var matchedRightNode = leftToRight.getDstForSrc(leftNode);

      // Skip if the matched right node is already mapped to base.
      if (nodeToClassRepresentative.get(matchedRightNode) != matchedRightNode) continue;

      // Update the right node's mapping to the left node if the parents are also mapped.
      if (nodeToClassRepresentative.get(leftNode.getParent())
          == nodeToClassRepresentative.get(matchedRightNode.getParent())) {
        nodeToClassRepresentative.put(matchedRightNode, leftNode);
      }
    }

    // Return the class representatives mapping.
    return Collections.unmodifiableMap(nodeToClassRepresentative);
  }

  /**
   * Map the base node as the class representative if a matching exists, otherwise map to self.
   *
   * <p>This will update the nodeToClassRepresentative mapping.
   *
   * @param tree the tree to map
   * @param baseToBranch the mapping from base to this tree
   * @param nodeToClassRepresentative the mapping from nodes to their class representatives
   */
  private static void mapToBaseOrToSelf(
      Tree tree, MappingStore baseToBranch, Map<Tree, Tree> nodeToClassRepresentative) {
    for (var node : tree.preOrder()) {
      final var matchedBaseNode = baseToBranch.getSrcForDst(node);
      if (matchedBaseNode != null) {
        // A matching exists, classRepresentativesMap to it.
        nodeToClassRepresentative.put(node, matchedBaseNode);
      } else {
        // No matching exists, classRepresentativesMap to self.
        nodeToClassRepresentative.put(node, node);
      }
    }
  }
}
