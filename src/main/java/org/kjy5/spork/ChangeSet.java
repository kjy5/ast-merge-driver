/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.ImmutableTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import java.util.*;

/**
 * A Spork change set.
 *
 * @author Kenneth Yang
 */
public record ChangeSet(Set<Pcs> pcsSet, Set<ContentTuple> contentTupleSet) {
  // region Factory.
  /**
   * Create a Spork change set from a tree.
   *
   * @param tree The tree to create the change set from.
   * @param classRepresentativesMapping The mapping of nodes to class representatives.
   */
  public static ChangeSet from(
      Tree tree,
      Map<Tree, Tree> classRepresentativesMapping,
      Map<Tree, Tree> virtualRootMapping,
      Map<Tree, ChildListVirtualNodes> childListVirtualNodesMapping) {
    // Initialize an empty content tuple.
    var wipContentTupleSet = new LinkedHashSet<ContentTuple>();

    // Build root of PCS set.
    final var rootClassRepresentative = classRepresentativesMapping.get(tree);

    final Tree virtualRoot;
    if (virtualRootMapping.containsKey(rootClassRepresentative)) {
      virtualRoot = virtualRootMapping.get(rootClassRepresentative);
    } else {
      virtualRoot = makeVirtualRootFor(rootClassRepresentative);
      virtualRootMapping.put(rootClassRepresentative, virtualRoot);
    }

    final ChildListVirtualNodes virtualRootChildListVirtualNodes;
    if (childListVirtualNodesMapping.containsKey(virtualRoot)) {
      virtualRootChildListVirtualNodes = childListVirtualNodesMapping.get(virtualRoot);
    } else {
      virtualRootChildListVirtualNodes =
          new ChildListVirtualNodes(
              makeVirtualChildListStartFor(virtualRoot), makeVirtualChildListEndFor(virtualRoot));
      childListVirtualNodesMapping.put(virtualRoot, virtualRootChildListVirtualNodes);
    }

    var wipPcsSet =
        new LinkedHashSet<>(
            Arrays.asList(
                new Pcs(
                    virtualRoot,
                    virtualRootChildListVirtualNodes.childListStart(),
                    rootClassRepresentative),
                new Pcs(
                    virtualRoot,
                    rootClassRepresentative,
                    virtualRootChildListVirtualNodes.childListEnd())));

    // Traverse the tree and build.
    tree.breadthFirst()
        .forEach(
            node -> {
              // Get class representative.
              var classRepresentative = classRepresentativesMapping.get(node);

              // Add content tuple (if it has content).
              if (classRepresentative.hasLabel())
                wipContentTupleSet.add(
                    new ContentTuple(classRepresentative, classRepresentative.getLabel()));

              // Get or create child list virtual nodes.
              final ChildListVirtualNodes childListVirtualNodes;
              if (childListVirtualNodesMapping.containsKey(classRepresentative)) {
                childListVirtualNodes = childListVirtualNodesMapping.get(classRepresentative);
              } else {
                childListVirtualNodes =
                    new ChildListVirtualNodes(
                        makeVirtualChildListStartFor(classRepresentative),
                        makeVirtualChildListEndFor(classRepresentative));
                childListVirtualNodesMapping.put(classRepresentative, childListVirtualNodes);
              }

              // Short-circuit if classRepresentative is leaf.
              if (node.getChildren().isEmpty()) {
                wipPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        childListVirtualNodes.childListStart(),
                        childListVirtualNodes.childListEnd()));
                return;
              }

              // TODO: Check later if virtual classRepresentatives are needed to separate children
              // (i.e. parameters, thrown exceptions).

              // Start children list (add virtual start).
              wipPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      childListVirtualNodes.childListStart(),
                      classRepresentativesMapping.get(node.getChild(0))));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < node.getChildren().size() - 1; i++) {
                wipPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        classRepresentativesMapping.get(node.getChild(i)),
                        classRepresentativesMapping.get(node.getChild(i + 1))));
              }

              // End children list (add virtual end).
              wipPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      classRepresentativesMapping.get(node.getChild(node.getChildren().size() - 1)),
                      childListVirtualNodes.childListEnd()));
            });

    // Set the final change set.
    return new ChangeSet(
        Collections.unmodifiableSet(wipPcsSet), Collections.unmodifiableSet(wipContentTupleSet));
  }

  // endregion

  // region Helper methods.
  private static Tree makeVirtualRootFor(Tree child) {
    var virtualRoot = new DefaultTree(Type.NO_TYPE, "virtualRoot");
    virtualRoot.setMetadata("child", child);
    return new ImmutableTree(virtualRoot);
  }

  private static Tree makeVirtualChildListStartFor(Tree root) {
    var virtualChildListStart = new DefaultTree(Type.NO_TYPE, "virtualChildListStart");
    virtualChildListStart.setMetadata("root", root);
    return new ImmutableTree(virtualChildListStart);
  }

  private static Tree makeVirtualChildListEndFor(Tree root) {
    var virtualChildListEnd = new DefaultTree(Type.NO_TYPE, "virtualChildListEnd");
    virtualChildListEnd.setMetadata("root", root);
    return new ImmutableTree(virtualChildListEnd);
  }
  // endregion
}
