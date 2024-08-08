/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;
import java.util.Optional;

/**
 * A Spork parent-child-successor triple.
 *
 * @param parent the parent node
 * @param child the child node
 * @param successor the successor node
 * @param hardInconsistencyWith the hard inconsistency with another PCS triple, if any
 * @author Kenneth Yang
 */
public record Pcs(Tree parent, Tree child, Tree successor, Optional<Pcs> hardInconsistencyWith) {

  /**
   * Construct a PCS triple on required fields.
   *
   * @param parent the parent node.
   * @param child the child node.
   * @param successor the successor node.
   */
  public Pcs(Tree parent, Tree child, Tree successor) {
    this(parent, child, successor, Optional.empty());
  }

  /**
   * Print the PCS triple.
   *
   * @return a string representation of the PCS triple
   */
  @Override
  public String toString() {
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
