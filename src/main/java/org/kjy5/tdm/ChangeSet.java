/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.tdm;

import com.github.gumtreediff.tree.Tree;
import java.util.*;

/**
 * A 3DM change set.
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
   * Create a 3DM change set from a tree.
   *
   * @param tree The tree to create the change set from.
   */
  public ChangeSet(Tree tree, ClassRepresentatives classRepresentatives) {
    // Initialize class representatives.
    this.classRepresentativesMapping = classRepresentatives.getMapping();

    // Initialize an empty content tuple and virtual root.
    var localContentTupleSet = new HashSet<ContentTuple>();
    var localPcsSet =
        new HashSet<>(Arrays.asList(new Pcs(null, null, tree), new Pcs(null, tree, null)));

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
                localPcsSet.add(new Pcs(classRepresentative, null, null));
                return;
              }

              // TODO: Check later if virtual classRepresentatives are needed to separate children
              // (i.e.
              // parameters, thrown exceptions).

              // Start children list (add virtual start).
              localPcsSet.add(
                  new Pcs(classRepresentative, null, getClassRepresentative(node.getChild(0))));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < classRepresentative.getChildren().size() - 1; i++) {
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
                      null));
            });

    // Set the final change set.
    this.pcsSet = Collections.unmodifiableSet(localPcsSet);
    this.contentTupleSet = Collections.unmodifiableSet(localContentTupleSet);
  }

  /**
   * Create a 3DM change set by taking the union of existing change sets.
   *
   * @param base The base change set.
   * @param left The left change set.
   * @param right The right change set.
   */
  public ChangeSet(ChangeSet base, ChangeSet left, ChangeSet right) {
    // Initialize base change set.
    var localPcsSet = new HashSet<>(base.pcsSet);
    var localContentTupleSet = new HashSet<>(base.contentTupleSet);

    // Add left's change set.
    localPcsSet.addAll(left.pcsSet);
    localContentTupleSet.addAll(left.contentTupleSet);

    // Add right's change set.
    localPcsSet.addAll(right.pcsSet);
    localContentTupleSet.addAll(right.contentTupleSet);

    // Set the final change set (copy over base's class representatives mapping).
    this.pcsSet = Collections.unmodifiableSet(localPcsSet);
    this.contentTupleSet = Collections.unmodifiableSet(localContentTupleSet);
    this.classRepresentativesMapping = base.classRepresentativesMapping;
  }

  // endregion

  // region Helper methods.
  private Tree getClassRepresentative(Tree node) {
    var classRepresentative = classRepresentativesMapping.get(node);
    if (classRepresentative == null)
      throw new IllegalStateException("Class representative not found for node: " + node);
    return classRepresentative;
  }
  // endregion
}
