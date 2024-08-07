/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;
import java.util.Optional;

/**
 * A Spork parent-child-successor triple.
 *
 * @param parent The parent node.
 * @param child The child node.
 * @param successor The successor node.
 * @param hardInconsistencyWith the hard inconsistency with another PCS triple, if any
 * @author Kenneth Yang
 */
public record Pcs(Tree parent, Tree child, Tree successor, Optional<Pcs> hardInconsistencyWith) {

  /**
   * Construct a PCS triple on required fields.
   *
   * @param parent The parent node.
   * @param child The child node.
   * @param successor The successor node.
   */
  public Pcs(Tree parent, Tree child, Tree successor) {
    this(parent, child, successor, Optional.empty());
  }

  /**
   * Print the PCS triple.
   *
   * @return A string representation of the PCS triple.
   */
  @Override
  public String toString() {
    // Note that `System.identityHashCode()` is not unique.  Two different values may have the same
    // identity hash code.  If it's important that the values are unique (or if it would be
    // convenient that they are small in-order values), use
    // https://plumelib.org/plume-util/api/org/plumelib/util/UniqueId.html .
    return "PCS "
        + Integer.toHexString(System.identityHashCode(this))
        + ":\t"
        + parent
        + "("
        + Integer.toHexString(System.identityHashCode(parent))
        + ")"
        + "\t"
        + child
        + "("
        + Integer.toHexString(System.identityHashCode(child))
        + ")"
        + "\t"
        + successor
        + "("
        + Integer.toHexString(System.identityHashCode(successor))
        + ")"
        + "\t"
        + hardInconsistencyWith;
  }
}
