/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.tdm;

import com.github.gumtreediff.tree.Tree;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A 3DM change set.
 *
 * @author Kenneth Yang
 */
public class ChangeSet {
  public final Set<Pcs> pcsSet;
  public final Set<ContentTuple> contentTupleSet;

  public ChangeSet(Set<Pcs> pcsSet, Set<ContentTuple> contentTupleSet) {
    this.pcsSet = new HashSet<>(pcsSet);
    this.contentTupleSet = new HashSet<>(contentTupleSet);
  }

  /**
   * Create a 3DM change set from a tree.
   *
   * @param tree The tree to create the change set from.
   */
  public ChangeSet(Tree tree) {
    // Initialize with empty content tuple and virtual root.
    this.contentTupleSet = new HashSet<>();
    this.pcsSet =
        new HashSet<>(Arrays.asList(new Pcs(null, null, tree), new Pcs(null, tree, null)));

    // Traverse the tree and build.
    tree.breadthFirst()
        .forEach(
            node -> {
              // Add content tuple (if it has content).
              if (node.hasLabel())
                this.contentTupleSet.add(new ContentTuple(node, node.getLabel()));

              // Short-circuit if node is root.
              if (node.getChildren().isEmpty()) {
                this.pcsSet.add(new Pcs(node, null, null));
                return;
              }

              // TODO: Check later if virtual nodes are needed to separate children (i.e.
              // parameters, thrown exceptions).

              // Start children list (add virtual start).
              this.pcsSet.add(new Pcs(node, null, node.getChild(0)));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < node.getChildren().size() - 1; i++) {
                this.pcsSet.add(new Pcs(node, node.getChild(i), node.getChild(i + 1)));
              }

              // End children list (add virtual end).
              this.pcsSet.add(new Pcs(node, node.getChild(node.getChildren().size() - 1), null));
            });
  }

  /**
   * Create a 3DM change set by taking the union of existing change sets.
   *
   * @param base The base change set.
   * @param left The left change set.
   * @param right The right change set.
   */
  public ChangeSet(ChangeSet base, ChangeSet left, ChangeSet right) {
    // Initialize with base's change set.
    this(base.pcsSet, base.contentTupleSet);

    // Add left's change set.
    this.pcsSet.addAll(left.pcsSet);
    this.contentTupleSet.addAll(left.contentTupleSet);

    // Add right's change set.
    this.pcsSet.addAll(right.pcsSet);
    this.contentTupleSet.addAll(right.contentTupleSet);
  }
}
