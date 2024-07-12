/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.FakeTree;
import com.github.gumtreediff.tree.Tree;
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

      //      // Check content tuple list for uniqueness.
      //      boolean parentContentTupleFound = false;
      //      boolean childContentTupleFound = false;
      //      boolean successorContentTupleFound = false;
      //      for (var contentTuple : changeSet.contentTupleSet) {
      //        // Check if this is the parent's content tuple.
      //        if (contentTuple.node().equals(otherPcs.parent())) {
      //          // There's an inconsistency if the parent's content tuple is already found.
      //          if (parentContentTupleFound) {
      //            inconsistentPcs.add(otherPcs);
      //            break;
      //          }
      //          // Otherwise, mark the parent's content tuple as found.
      //          parentContentTupleFound = true;
      //        }
      //
      //        // Check if this is the child's content tuple.
      //        if (contentTuple.node().equals(otherPcs.child())) {
      //          // There's an inconsistency if the child's content tuple is already found.
      //          if (childContentTupleFound) {
      //            inconsistentPcs.add(otherPcs);
      //            break;
      //          }
      //          // Otherwise, mark the child's content tuple as found.
      //          childContentTupleFound = true;
      //        }
      //
      //        // Check if this is the successor's content tuple.
      //        if (contentTuple.node().equals(otherPcs.successor())) {
      //          // There's an inconsistency if the successor's content tuple is already found.
      //          if (successorContentTupleFound) {
      //            inconsistentPcs.add(otherPcs);
      //            break;
      //          }
      //          // Otherwise, mark the successor's content tuple as found.
      //          successorContentTupleFound = true;
      //        }
      //      }
    }

    return Collections.unmodifiableSet(inconsistentPcs);
  }
  // endregion
}
