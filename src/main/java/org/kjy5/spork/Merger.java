/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

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
  /**
   * Perform a Spork merge.
   *
   * @param baseChangeSet Base branch change set.
   * @param leftChangeSet Left branch change set.
   * @param rightChangeSet Right branch change set.
   */
  public static ChangeSet merge(
      ChangeSet baseChangeSet, ChangeSet leftChangeSet, ChangeSet rightChangeSet) {
    // Union the three PCSs.
    var mergePcsSet = new LinkedHashSet<>(baseChangeSet.pcsSet());
    mergePcsSet.addAll(leftChangeSet.pcsSet());
    mergePcsSet.addAll(rightChangeSet.pcsSet());

    // Union the three content tuples.
    var mergeContentTupleSet = new LinkedHashSet<>(baseChangeSet.contentTupleSet());
    mergeContentTupleSet.addAll(leftChangeSet.contentTupleSet());
    mergeContentTupleSet.addAll(rightChangeSet.contentTupleSet());

    // Merged change set.
    var mergedChangeSet = new ChangeSet(mergePcsSet, mergeContentTupleSet);

    System.out.format(
        "%-10s%-10s%-15s%n",
        "Raw", mergedChangeSet.pcsSet().size(), mergedChangeSet.contentTupleSet().size());

    // Remove soft-inconsistencies and mark hard-inconsistencies.
    for (var pcs : new LinkedHashSet<>(mergedChangeSet.pcsSet())) {
      // TODO: Algorithm doesn't say so but we should skip if the PCS is already removed.
      if (!mergedChangeSet.pcsSet().contains(pcs)) continue;

      removeSoftPcsInconsistencies(pcs, mergedChangeSet, baseChangeSet);
      handleContent(pcs, mergedChangeSet, baseChangeSet);
    }

    // Return the merged change set.
    return mergedChangeSet;
  }

  // endregion

  // region Spork-3DM methods.

  /**
   * Remove PCS inconsistencies that are not caused by conflicting changes.
   *
   * @param pcs the PCS to check for inconsistencies
   * @param mergeChangeSet the change set to update
   * @param baseChangeSet the base change set to check with
   */
  private static void removeSoftPcsInconsistencies(
      Pcs pcs, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    // Get all inconsistent PCSs.
    var inconsistentPcs = getAllInconsistentPcs(pcs, mergeChangeSet);

    // Short-circuit if there are no inconsistencies.
    if (inconsistentPcs.isEmpty()) return;

    // Short-circuit if this pcs is in the base change set (remove it from the merge change set).
    if (baseChangeSet.pcsSet().contains(pcs)) {
      mergeChangeSet.pcsSet().remove(pcs);
      return;
    }

    // Remove all inconsistent PCSs from the merge change set if they are in the base change set.
    for (var otherPcs : inconsistentPcs) {
      if (baseChangeSet.pcsSet().contains(otherPcs)) {
        mergeChangeSet.pcsSet().remove(otherPcs);
      } else {
        // Otherwise, mark the PCS as a hard inconsistency.
        hardPcsInconsistency(pcs, otherPcs, mergeChangeSet);
      }
    }
  }

  /**
   * Handle content inconsistencies.
   *
   * @param pcs the PCS to check for content inconsistencies
   * @param mergeChangeSet the change set to update
   * @param baseChangeSet the base change set to check with
   */
  private static void handleContent(Pcs pcs, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    removeSoftContentInconsistencies(pcs.parent(), mergeChangeSet, baseChangeSet);
    removeSoftContentInconsistencies(pcs.child(), mergeChangeSet, baseChangeSet);
    removeSoftContentInconsistencies(pcs.successor(), mergeChangeSet, baseChangeSet);
  }

  /**
   * Remove content inconsistencies that are not caused by conflicting changes.
   *
   * @param tree the tree to check for content inconsistencies
   * @param mergeChangeSet the change set to update
   * @param baseChangeSet the base change set to check with
   */
  private static void removeSoftContentInconsistencies(
      Tree tree, ChangeSet mergeChangeSet, ChangeSet baseChangeSet) {
    var contentTuples = getContentTuples(tree, mergeChangeSet);

    // Short-circuit if there are one or fewer content tuples (no inconsistencies).
    if (contentTuples.size() <= 1) return;

    // Get all content tuples not in the base change set.
    var nonBaseContentTuples =
        contentTuples.stream()
            .filter(contentTuple -> !baseChangeSet.contentTupleSet().contains(contentTuple))
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
   * Get inconsistent PCSs inside a change set given a PCS.
   *
   * <p>If two PCSs have the same parent, both children and both successors must be different. If
   * two PCSs have different parents, all children and successors must be different.
   *
   * @param pcs The PCS to find inconsistencies with.
   * @param changeSet The change set to search in.
   * @return The set of inconsistent PCSs.
   */
  private static Set<Pcs> getAllInconsistentPcs(Pcs pcs, ChangeSet changeSet) {
    var inconsistentPcs = new LinkedHashSet<Pcs>();

    // Loop through change set and find inconsistencies.
    for (var otherPcs : changeSet.pcsSet()) {
      // Skip if it's the same PCS.
      if (pcs == otherPcs) continue;

      // Check criteria and add.
      if (isInconsistent(pcs, otherPcs)) {
        inconsistentPcs.add(otherPcs);
      }
    }

    return Collections.unmodifiableSet(inconsistentPcs);
  }

  /**
   * Check if two PCSs are inconsistent.
   *
   * @param pcs The first PCS.
   * @param otherPcs The second PCS.
   * @return True if the PCSs are inconsistent, false otherwise.
   */
  private static boolean isInconsistent(Pcs pcs, Pcs otherPcs) {
    return (pcs.parent() == otherPcs.parent()
            && (pcs.child() == otherPcs.child() || pcs.successor() == otherPcs.successor()))
        || (pcs.parent() != otherPcs.parent()
            && (pcs.child() == otherPcs.child()
                || pcs.child() == otherPcs.successor()
                || pcs.successor() == otherPcs.successor()
                || pcs.successor() == otherPcs.child()));
  }

  /**
   * Get all content tuples related to the tree according to the change set.
   *
   * @param tree the tree to get content tuples for
   * @param changeSet the change set to search in
   * @return the set of content tuples related to the tree
   */
  private static Set<ContentTuple> getContentTuples(Tree tree, ChangeSet changeSet) {
    return changeSet.contentTupleSet().stream()
        .filter(contentTuple -> contentTuple.node().equals(tree))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Set the content tuples associated with the node in the change set.
   *
   * @param node the node to set content tuples for
   * @param contents the content tuples to set
   * @param changeSet the change set to update the content tuples in
   */
  private static void setContentTuples(Tree node, Set<ContentTuple> contents, ChangeSet changeSet) {
    // Remove all content tuples associated with the tree (they're to be replaced).
    changeSet.contentTupleSet().removeIf(contentTuple -> contentTuple.node().equals(node));

    // Filter for content tuples associated with the tree and add them to the change set.
    changeSet
        .contentTupleSet()
        .addAll(
            contents.stream().filter(contentTuple -> contentTuple.node().equals(node)).toList());
  }

  // What does it mean to "register"?
  // Document that both `pcs` and `mergeChangeSet` may be side-effected by this method.
  /**
   * Update this PCS and another one as being in conflict with each other.
   *
   * @param pcs the PCS to mark as inconsistent
   * @param otherPcs the PCS to mark as inconsistent with
   * @param mergeChangeSet the change set these PCSs are in
   */
  private static void hardPcsInconsistency(Pcs pcs, Pcs otherPcs, ChangeSet mergeChangeSet) {
    // Short-circuit if this PCS already has a hard inconsistency.
    if (pcs.hardInconsistencyWith().isPresent()) return;

    // Mark the PCS as inconsistent with the other PCS and replace the original from the change set.
    var updatedPcs = new Pcs(pcs.parent(), pcs.child(), pcs.successor(), Optional.of(otherPcs));
    mergeChangeSet.pcsSet().remove(pcs);
    mergeChangeSet.pcsSet().add(updatedPcs);

    // Short-circuit if the other PCS is already inconsistent.
    if (otherPcs.hardInconsistencyWith().isPresent()) return;

    // Mark the other PCS as inconsistent with this PCS and replace the original from the change
    // set.
    var updatedOtherPcs =
        new Pcs(otherPcs.parent(), otherPcs.child(), otherPcs.successor(), Optional.of(pcs));
    mergeChangeSet.pcsSet().remove(otherPcs);
    mergeChangeSet.pcsSet().add(updatedOtherPcs);
  }

  private static void hardContentInconsistency(
      Set<ContentTuple> contentTuples, ChangeSet mergeChangeSet) {
    // In a three-way merge, there are exactly 2 inconsistencies: left and right.
    if (contentTuples.size() != 2) {
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
    var updatedFirstContentTuple = new ContentTuple(first.node(), first.content(), second);
    mergeChangeSet.contentTupleSet().remove(first);
    mergeChangeSet.contentTupleSet().add(updatedFirstContentTuple);

    // Mark the second content tuple as inconsistent with the first and replace the original from
    // the change set.
    var updatedSecondContentTuple = new ContentTuple(second.node(), second.content(), first);
    mergeChangeSet.contentTupleSet().remove(second);
    mergeChangeSet.contentTupleSet().add(updatedSecondContentTuple);
  }
  // endregion
}
