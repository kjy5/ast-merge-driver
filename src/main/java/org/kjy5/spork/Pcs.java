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
 * @param hardInconsistencyWith The hard inconsistency with another PCS triple.
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
   * Check for equality.
   *
   * @param other The reference object with which to compare.
   * @return True if the PCS triples are equal, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof Pcs pcs)) return false;
    return parent.equals(pcs.parent)
        && child.equals(pcs.child)
        && successor.equals(pcs.successor)
        && hardInconsistencyWith.equals(pcs.hardInconsistencyWith);
  }

  /**
   * Print the PCS triple.
   *
   * @return A string representation of the PCS triple.
   */
  @Override
  public String toString() {
    return "PCS(" + parent + ", " + child + ", " + successor + ')';
  }
}
