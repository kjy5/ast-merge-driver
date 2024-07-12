/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Change set merger.
 *
 * <p>This is the core merge algorithm.
 *
 * @author Kenneth Yang
 */
public class Merger {
  private final ChangeSet mergedChangeSet;

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
    var mergedPcsSet = new HashSet<>(baseChangeSet.pcsSet);
    mergedPcsSet.addAll(leftChangeSet.pcsSet);
    mergedPcsSet.addAll(rightChangeSet.pcsSet);

    // Union the three content tuples.
    var mergedContentTupleSet = new HashSet<>(baseChangeSet.contentTupleSet);
    mergedContentTupleSet.addAll(leftChangeSet.contentTupleSet);
    mergedContentTupleSet.addAll(rightChangeSet.contentTupleSet);

    // Create the final merged change set.
    this.mergedChangeSet = new ChangeSet(mergedPcsSet, mergedContentTupleSet);
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

  // region Algorithm (helper) methods.

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

    // Loop through change set and find inconsistencies.
    for (var otherPcs : changeSet.pcsSet) {
      // Shortcut if the same PCS.
      if (pcs.equals(otherPcs)) continue;

      // Setup consistency markers.
      boolean parentFoundParent, parentFoundPredecessor, parentFoundSuccessor;
      boolean childFoundParent, childFoundPredecessor, childFoundSuccessor;
      boolean successorFoundParent, successorFoundPredecessor, successorFoundSuccessor;

      // Check parent node.

    }

    return Collections.unmodifiableSet(inconsistentPcs);
  }
  // endregion
}
