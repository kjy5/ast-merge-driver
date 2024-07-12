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
public class ChangeSet {
  // region Fields.
  public final Set<Pcs> pcsSet;
  public final Set<ContentTuple> contentTupleSet;
  // endregion

  // region Internal properties.
  private final Map<Tree, Tree> classRepresentativesMapping;

  // endregion

  // region Constructors.
  /**
   * Create a Spork change set from a tree.
   *
   * @param tree The tree to create the change set from.
   * @param classRepresentativesMapping The mapping of nodes to class representatives.
   */
  public ChangeSet(Tree tree, Map<Tree, Tree> classRepresentativesMapping) {
    // Initialize class representatives.
    this.classRepresentativesMapping = classRepresentativesMapping;

    // Initialize an empty content tuple and virtual root.
    var localContentTupleSet = new HashSet<ContentTuple>();
    final var virtualRoot = makeVirtualRootFor(tree);
    var localPcsSet =
        new HashSet<>(
            Arrays.asList(
                new Pcs(virtualRoot, makeVirtualChildListStartFor(virtualRoot), tree),
                new Pcs(virtualRoot, tree, makeVirtualChildListEndFor(virtualRoot))));

    // Traverse the tree and build.
    tree.breadthFirst()
        .forEach(
            node -> {
              // Get class representative.
              var classRepresentative = getClassRepresentative(node);

              // Add content tuple (if it has content).
              if (classRepresentative.hasLabel())
                localContentTupleSet.add(
                    new ContentTuple(classRepresentative, classRepresentative.getLabel()));

              // TODO: are we supposed to look at node's children or the classRepresentative's
              // children?
              // Short-circuit if classRepresentative is root.
              if (node.getChildren().isEmpty()) {
                localPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        makeVirtualChildListStartFor(classRepresentative),
                        makeVirtualChildListEndFor(classRepresentative)));
                return;
              }

              // TODO: Check later if virtual classRepresentatives are needed to separate children
              // (i.e. parameters, thrown exceptions).

              // Start children list (add virtual start).
              localPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      makeVirtualChildListStartFor(classRepresentative),
                      getClassRepresentative(node.getChild(0))));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < node.getChildren().size() - 1; i++) {
                localPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        getClassRepresentative(node.getChild(i)),
                        getClassRepresentative(node.getChild(i + 1))));
              }

              // End children list (add virtual end).
              localPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      getClassRepresentative(node.getChild(node.getChildren().size() - 1)),
                      makeVirtualChildListEndFor(classRepresentative)));
            });

    // Set the final change set.
    this.pcsSet = Collections.unmodifiableSet(localPcsSet);
    this.contentTupleSet = Collections.unmodifiableSet(localContentTupleSet);
  }

  /**
   * Basic constructor for a Spork change set.
   *
   * @param pcsSet PCS set to build the change set from.
   * @param contentTupleSet Content tuple set to build the change set from.
   */
  public ChangeSet(Set<Pcs> pcsSet, Set<ContentTuple> contentTupleSet) {
    // Ensure immutability.
    this.pcsSet = Collections.unmodifiableSet(pcsSet);
    this.contentTupleSet = Collections.unmodifiableSet(contentTupleSet);

    // Use empty class representatives mapping (it's not used).
    this.classRepresentativesMapping = new HashMap<>();
  }

  // endregion

  // region Helper methods.
  private Tree getClassRepresentative(Tree node) {
    var classRepresentative = classRepresentativesMapping.get(node);
    if (classRepresentative == null)
      throw new IllegalStateException("Class representative not found for node: " + node);
    return classRepresentative;
  }

  private Tree makeVirtualRootFor(Tree child) {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualRoot for " + child));
  }

  private Tree makeVirtualChildListStartFor(Tree root) {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListStart for " + root));
  }

  private Tree makeVirtualChildListEndFor(Tree root) {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListEnd for " + root));
  }
  // endregion
}
