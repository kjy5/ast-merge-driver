/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.FakeTree;
import com.github.gumtreediff.tree.Tree;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Change set merger.
 *
 * <p>This is the core merge algorithm.
 *
 * @author Kenneth Yang
 */
public class Merger {
  // region Constants.
  private static final Tree FAKE_TREE = new FakeTree();
  // endregion

  // region Fields.
  private final ChangeSet mergedChangeSet;

  // endregion

  // region Constructor.
  /**
   * Construct and perform Spork merge.
   *
   * @param baseChangeSet Base branch change set.
   * @param leftChangeSet Left branch change set.
   * @param rightChangeSet Right branch change set.
   */
  public Merger(ChangeSet baseChangeSet, ChangeSet leftChangeSet, ChangeSet rightChangeSet) {
    // Union the three PCSs.
    var mergePcsSet = new HashSet<>(baseChangeSet.pcsSet);
    mergePcsSet.addAll(leftChangeSet.pcsSet);
    mergePcsSet.addAll(rightChangeSet.pcsSet);

    // Union the three content tuples.
    var mergeContentTupleSet = new HashSet<>(baseChangeSet.contentTupleSet);
    mergeContentTupleSet.addAll(leftChangeSet.contentTupleSet);
    mergeContentTupleSet.addAll(rightChangeSet.contentTupleSet);

    // Merged change set.
    this.mergedChangeSet = new ChangeSet(mergePcsSet, mergeContentTupleSet);

    System.out.println(
        "Raw\t\t"
            + this.mergedChangeSet.pcsSet.size()
            + "\t\t"
            + this.mergedChangeSet.contentTupleSet.size());

    // Handle inconsistencies.
    for (var pcs : new HashSet<>(this.mergedChangeSet.pcsSet)) {
      removeSoftPcsInconsistencies(pcs, this.mergedChangeSet, baseChangeSet);
      handleContent(pcs, this.mergedChangeSet, baseChangeSet);
    }
  }

  // endregion

  // region Getters
  /**
   * Get the merged change set.
   *
   * @return The merged change set.
   */
  public ChangeSet getMergedChangeSet() {
    return mergedChangeSet;
  }

  // endregion

  // region Spork-3DM methods.

  private void removeSoftPcsInconsistencies(
      Pcs pcs, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    // Get all inconsistent PCSs.
    var inconsistentPcs = getAllInconsistentPcs(pcs, mergeChangeSet);

    // Short-circuit if there are no inconsistencies.
    if (inconsistentPcs.isEmpty()) return;

    // Short-circuit if this pcs is in the base change set (remove it from the merge change set).
    if (baseChangeSet.pcsSet.contains(pcs)) {
      mergeChangeSet.pcsSet.remove(pcs);
      return;
    }

    // Remove all inconsistent PCSs from the merge change set if they were in the base change set.
    for (var otherPcs : inconsistentPcs) {
      if (baseChangeSet.pcsSet.contains(otherPcs)) {
        mergeChangeSet.pcsSet.remove(otherPcs);
      } else {
        // Otherwise, mark the PCS as a hard inconsistency.
        hardPcsInconsistency(pcs, otherPcs, mergeChangeSet);
      }
    }
  }

  private void handleContent(Pcs pcs, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    removeSoftContentInconsistencies(pcs.parent(), mergeChangeSet, baseChangeSet);
    removeSoftContentInconsistencies(pcs.child(), mergeChangeSet, baseChangeSet);
    removeSoftContentInconsistencies(pcs.successor(), mergeChangeSet, baseChangeSet);
  }

  private void removeSoftContentInconsistencies(
      Tree tree, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    var contentTuples = getContentTuples(tree, mergeChangeSet);

    // Short-circuit if there are one or less content tuple (this tree is basically a single linked
    // list).
    if (contentTuples.size() <= 1) return;

    // Get all content tuples not in the base change set.
    var nonBaseContentTuples =
        contentTuples.stream()
            .filter(contentTuple -> !baseChangeSet.contentTupleSet.contains(contentTuple))
            .collect(Collectors.toUnmodifiableSet());

    // Update content tuples with non-base content tuples.
    setContentTuples(tree, nonBaseContentTuples, mergeChangeSet);

    // Mark hard content inconsistencies.
    if (nonBaseContentTuples.size() > 1) {
      hardContentInconsistency(nonBaseContentTuples, mergeChangeSet);
    }
  }

  // endregion

  // region Spork-3DM helper methods.
  // TODO: Consider using "well formed" criteria for consistency.

  /**
   * Get all inconsistent PCSs inside a change set given a PCS.
   *
   * @param pcs The PCS to find inconsistencies with.
   * @param changeSet The change set to search in.
   * @return The set of inconsistent PCSs.
   */
  private Set<Pcs> getAllInconsistentPcs(Pcs pcs, ChangeSet changeSet) {
    var inconsistentPcs = new HashSet<Pcs>();

    // Setup markers for found criteria.
    Tree parentFoundParent = FAKE_TREE,
        parentFoundPredecessor = FAKE_TREE,
        parentFoundSuccessor = FAKE_TREE;
    Tree childFoundPredecessor = FAKE_TREE;
    Tree successorFoundSuccessor = FAKE_TREE;

    // Loop through change set and find inconsistencies.
    for (var otherPcs : changeSet.pcsSet) {
      // Skip if it's the same PCS.
      if (pcs.equals(otherPcs)) continue;

      // Skip if it's already considered inconsistent.
      if (inconsistentPcs.contains(otherPcs)) continue;

      // Check parent node.

      // If the parent is a child or successor, ensure that the parent is the same as the found one
      // (or initialize it).
      if (otherPcs.child().equals(pcs.parent()) || otherPcs.successor().equals(pcs.parent())) {
        // Initialize found parent if not initialized.
        if (parentFoundParent.equals(FAKE_TREE)) parentFoundParent = otherPcs.parent();

        // There's an inconsistency if this is a different parent.
        if (!parentFoundParent.equals(otherPcs.parent())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // If the parent node is a successor, ensure that the predecessor is the same as the found one
      // (or initialize it).
      if (otherPcs.successor().equals(pcs.parent())) {
        // Initialize found predecessor if not initialized.
        if (parentFoundPredecessor.equals(FAKE_TREE)) parentFoundPredecessor = otherPcs.child();

        // There's an inconsistency if this is a different predecessor.
        if (!parentFoundPredecessor.equals(otherPcs.child())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // If the parent node is a child, ensure that the successor is the same as the found one (or
      // initialize it).
      if (otherPcs.child().equals(pcs.parent())) {
        // Initialize found successor if not initialized.
        if (parentFoundSuccessor.equals(FAKE_TREE)) parentFoundSuccessor = otherPcs.successor();

        // There's an inconsistency if this is a different successor.
        if (!parentFoundSuccessor.equals(otherPcs.successor())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // Check child node.

      // If the child is a child or successor, ensure that the parent is the same as the current
      // one.
      if (otherPcs.child().equals(pcs.child()) || otherPcs.successor().equals(pcs.child())) {
        // There's an inconsistency if this is a different parent.
        if (!otherPcs.parent().equals(pcs.parent())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // If the child is a successor, ensure that the predecessor is the same as the found one (or
      // initialize it).
      if (otherPcs.successor().equals(pcs.child())) {
        // Initialize found predecessor if not initialized.
        if (childFoundPredecessor.equals(FAKE_TREE)) childFoundPredecessor = otherPcs.child();

        // There's an inconsistency if this is a different predecessor.
        if (!childFoundPredecessor.equals(otherPcs.child())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // TODO: Cases might be redundant. There should only be one PCS where the child is the child.
      // If the child is a child, ensure that the successor is the same as the current one.
      if (otherPcs.child().equals(pcs.child())) {
        // There's an inconsistency if this is a different successor.
        if (!otherPcs.successor().equals(pcs.successor())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // Check successor node.

      // If the successor is a child or successor, ensure that the parent is the same as the current
      // one.
      if (otherPcs.child().equals(pcs.successor())
          || otherPcs.successor().equals(pcs.successor())) {
        // There's an inconsistency if this is a different parent.
        if (!otherPcs.parent().equals(pcs.parent())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // If the successor is a successor, ensure that the predecessor is the same as the current
      // one.
      if (otherPcs.successor().equals(pcs.successor())) {
        // There's an inconsistency if this is a different predecessor.
        if (!otherPcs.child().equals(pcs.child())) {
          inconsistentPcs.add(otherPcs);
          continue;
        }
      }

      // If the successor is a child, ensure that the successor is the same as the found one (or
      // initialize it).
      if (otherPcs.child().equals(pcs.successor())) {
        // Initialize found successor if not initialized.
        if (successorFoundSuccessor.equals(FAKE_TREE))
          successorFoundSuccessor = otherPcs.successor();

        // There's an inconsistency if this is a different successor.
        if (!successorFoundSuccessor.equals(otherPcs.successor())) {
          inconsistentPcs.add(otherPcs);
        }
      }
    }

    return Collections.unmodifiableSet(inconsistentPcs);
  }

  /**
   * Get all content tuples related to the tree according to the change set.
   *
   * @param tree The tree to get content tuples for.
   * @param changeSet The change set to search in.
   * @return The set of content tuples related to the tree.
   */
  private Set<ContentTuple> getContentTuples(Tree tree, ChangeSet changeSet) {
    return changeSet.contentTupleSet.stream()
        .filter(contentTuple -> contentTuple.node().equals(tree))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Set the content tuples associated with the tree in the change set.
   *
   * @param tree The tree to set content tuples for.
   * @param contents The content tuples to set.
   * @param changeSet The change set to update the content tuples in.
   */
  private void setContentTuples(Tree tree, Set<ContentTuple> contents, ChangeSet changeSet) {
    // Remove all content tuples associated with the tree (they're to be replaced).
    changeSet.contentTupleSet.removeIf(contentTuple -> contentTuple.node().equals(tree));

    // Filter for content tuples associated with the tree and add them to the change set.
    changeSet.contentTupleSet.addAll(
        contents.stream().filter(contentTuple -> contentTuple.node().equals(tree)).toList());
  }

  /**
   * Register PCS and other has a hard inconsistency.
   *
   * @param pcs The PCS to mark as inconsistent.
   * @param otherPcs The PCS to mark as inconsistent with.
   * @param mergeChangeSet The change set these PCSs are in.
   */
  private void hardPcsInconsistency(Pcs pcs, Pcs otherPcs, ChangeSet mergeChangeSet) {
    // Short-circuit if this PCS already has a hard inconsistency.
    if (pcs.hardInconsistencyWith().isPresent()) return;

    // Mark the PCS as inconsistent with the other PCS and replace the original from the change set.
    var updatedPcs = new Pcs(pcs.parent(), pcs.child(), pcs.successor(), Optional.of(otherPcs));
    mergeChangeSet.pcsSet.remove(pcs);
    mergeChangeSet.pcsSet.add(updatedPcs);

    // Short-circuit if the other PCS is already inconsistent.
    if (otherPcs.hardInconsistencyWith().isPresent()) return;

    // Mark the other PCS as inconsistent with this PCS and replace the original from the change
    // set.
    var updatedOtherPcs =
        new Pcs(otherPcs.parent(), otherPcs.child(), otherPcs.successor(), Optional.of(pcs));
    mergeChangeSet.pcsSet.remove(otherPcs);
    mergeChangeSet.pcsSet.add(updatedOtherPcs);
  }

  private void hardContentInconsistency(Set<ContentTuple> contentTuples, ChangeSet mergeChangeSet) {
    // In a three-way merge, there are exactly 2 inconsistencies: left and right.
    if (contentTuples.size() != 2) {
      for (var contentTuple : contentTuples) {
        System.out.println(contentTuple);
      }
      throw new IllegalStateException(
          "Content inconsistency should only have 2 content tuples. "
              + contentTuples.size()
              + " found.");
    }

    var contentTuplesList = new ArrayList<>(contentTuples);
    var first = contentTuplesList.get(0);
    var second = contentTuplesList.get(1);

    // Mark the first content tuple as inconsistent with the second and replace the original from
    // the change set.
    var updatedFirstContentTuple =
        new ContentTuple(first.node(), first.content(), Optional.of(second));
    mergeChangeSet.contentTupleSet.remove(first);
    mergeChangeSet.contentTupleSet.add(updatedFirstContentTuple);

    // Mark the second content tuple as inconsistent with the first and replace the original from
    // the change set.
    var updatedSecondContentTuple =
        new ContentTuple(second.node(), second.content(), Optional.of(first));
    mergeChangeSet.contentTupleSet.remove(second);
    mergeChangeSet.contentTupleSet.add(updatedSecondContentTuple);
  }
  // endregion
}
