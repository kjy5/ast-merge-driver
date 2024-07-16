/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.kjy5.parser.HashableTree;

/**
 * Class representatives for a merge.
 *
 * <p>Collected matched nodes together into a common "representative" node.
 *
 * @author Kenneth Yang
 */
public class ClassRepresentatives {
  private final Map<Tree, Tree> classRepresentativesMap = new HashMap<>();

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
  public ClassRepresentatives(
      Tree baseTree,
      Tree leftTree,
      Tree rightTree,
      MappingStore baseToLeftMapping,
      MappingStore baseToRightMapping,
      MappingStore leftToRightMapping) {
    // Base nodes are mapped to themselves.
    baseTree
        .preOrder()
        .forEach(
            node -> {
              final var hashableNode = new HashableTree(node);
              classRepresentativesMap.put(hashableNode, hashableNode);
            });

    /*
     * Left nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
     */
    leftTree
        .preOrder()
        .forEach(
            node -> {
              final var matchedBaseNode = baseToLeftMapping.getSrcForDst(node);
              final var hashableNode = new HashableTree(node);
              if (matchedBaseNode != null) {
                // A matching exists, map to it.
                classRepresentativesMap.put(hashableNode, new HashableTree(matchedBaseNode));
              } else {
                // No matching exists, map to self.
                classRepresentativesMap.put(hashableNode, hashableNode);
              }
            });

    /*
     * Right nodes are mapped to base first if a matching exists, then left, otherwise themselves.
     * Breadth first ordering is used to ensure parents are mapped before children.
     * This is used in preventing spurious left-to-right mappings.
     */
    rightTree
        .breadthFirst()
        .forEach(
            node -> {
              final var matchedBaseNode = baseToRightMapping.getSrcForDst(node);
              final var hashableNode = new HashableTree(node);
              if (matchedBaseNode != null) {
                // A matching exists, map to it.
                classRepresentativesMap.put(hashableNode, new HashableTree(matchedBaseNode));
              } else {
                // No base matching, try left.
                final var matchedLeftNode = leftToRightMapping.getSrcForDst(node);
                if (matchedLeftNode != null
                    && !baseToLeftMapping.isSrcMapped(matchedLeftNode)
                    && classRepresentativesMap
                        .get(new HashableTree(matchedLeftNode.getParent()))
                        .equals(classRepresentativesMap.get(node.getParent()))) {
                  // A matching to left exists, left node is not matched to base, and parents of
                  // left and right nodes are matched to the same class representative.
                  classRepresentativesMap.put(hashableNode, new HashableTree(matchedLeftNode));
                } else {
                  // No matching exists, map to self.
                  classRepresentativesMap.put(hashableNode, hashableNode);
                }
              }
            });
  }

  /**
   * Get a read-only reference to the class representatives mapping.
   *
   * @return Read-only class representatives mapping.
   */
  public Map<Tree, Tree> getMapping() {
    return Collections.unmodifiableMap(classRepresentativesMap);
  }
}
