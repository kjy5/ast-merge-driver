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
  Set<Pcs> getAllInconsistentPcs(Pcs pcs, ChangeSet changeSet) {
    var inconsistentPcs = new HashSet<Pcs>();
    return Collections.unmodifiableSet(inconsistentPcs);
  }
  // endregion
}
