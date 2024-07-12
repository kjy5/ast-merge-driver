/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import java.util.HashSet;

/**
 * Change set merger.
 *
 * <p>This is the core merge algorithm.
 *
 * @author Kenneth Yang
 */
public class Merger {
  private final ChangeSet mergedChangeSet;

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

  /**
   * Get the merged change set.
   *
   * @return The merged change set.
   */
  public ChangeSet getMergedChangeSet() {
    return mergedChangeSet;
  }
}
