/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// What is a "class representative"?  This definition is circular and provides no information.
// I cannot parse the second sentence of the following comment.
/**
 * Class representatives for a merge.
 *
 * <p>Collected matched nodes together into a common "representative" node.
 *
 * @author Kenneth Yang
 */
public class ClassRepresentatives {
  /**
   * Map class representatives for a merge.
   *
   * @param baseTree The base tree.
   * @param leftTree The left tree.
   * @param rightTree The right tree.
   * @param baseToLeftMapping The match mapping from base to left.
   * @param baseToRightMapping The match mapping from base to right.
   * @param leftToRightMapping The match mapping from left to right.
   */
  public static Map<Tree, Tree> from(
      Tree baseTree,
      Tree leftTree,
      Tree rightTree,
      MappingStore baseToLeftMapping,
      MappingStore baseToRightMapping,
      MappingStore leftToRightMapping) {
    // Initialize an empty class representatives mapping.
    var classRepresentativesMap = new LinkedHashMap<Tree, Tree>();

    // Nodes in the base tree are mapped to themselves.
    baseTree.preOrder().forEach(node -> classRepresentativesMap.put(node, node));

    // Left nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
    // If the following code is identical becteen left and right nodes, abstract it out and call it
    // twice.
    // Is there a reason not to use the `forEach()` method rather than a standard Java enhanced for
    // loop, here and elsewhere?  I thin the for loop might be easier to read and more efficient.
    leftTree
        .preOrder()
        .forEach(
            leftNode -> {
              final var matchedBaseNode = baseToLeftMapping.getSrcForDst(leftNode);
              if (matchedBaseNode != null) {
                // A matching exists, classRepresentativesMap to it.
                classRepresentativesMap.put(leftNode, matchedBaseNode);
              } else {
                // No matching exists, classRepresentativesMap to self.
                classRepresentativesMap.put(leftNode, leftNode);
              }
            });

    // Right nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
    rightTree
        .preOrder()
        .forEach(
            rightNode -> {
              final var matchedBaseNode = baseToRightMapping.getSrcForDst(rightNode);
              if (matchedBaseNode != null) {
                // A matching exists, classRepresentativesMap to it.
                classRepresentativesMap.put(rightNode, matchedBaseNode);
              } else {
                // No matching exists, classRepresentativesMap to self.
                classRepresentativesMap.put(rightNode, rightNode);
              }
            });

    // Does "their parents are mapped" mean that left's parent is mapped to right's parent?  How can
    // that happen, given that above every node is mapped to itself or to a node in the base tree?
    // Map right nodes to left nodes if their parents are also mapped (breadth-first is used for
    // parent ordering).
    leftTree
        .breadthFirst()
        .forEach(
            leftNode -> {
              // Here and elsewhere, "do nothing" is more accurate than "short-circuit".
              // Short-circuit if the left node is already mapped to base.
              if (classRepresentativesMap.get(leftNode) != leftNode) return;

              // Short-circuit if the left node is not mapped to right.
              if (!leftToRightMapping.isSrcMapped(leftNode)) return;

              final var matchedRightNode = leftToRightMapping.getDstForSrc(leftNode);

              // Short-circuit if the matched right node is already mapped to base.
              if (classRepresentativesMap.get(matchedRightNode) != matchedRightNode) return;

              // Update the right node's mapping to the left node if the parents are also mapped.
              if (classRepresentativesMap.get(leftNode.getParent())
                  == classRepresentativesMap.get(matchedRightNode.getParent())) {
                classRepresentativesMap.put(matchedRightNode, leftNode);
              }
            });

    // What is "the class representatives mapping"?  Is it from left to right only?
    // Return the class representatives mapping.
    return Collections.unmodifiableMap(classRepresentativesMap);
  }
}
