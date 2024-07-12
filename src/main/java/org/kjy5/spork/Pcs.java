/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;
import java.util.Objects;

/**
 * A Spork parent-child-successor triple.
 *
 * <p>A null node in the triple is the virtual node.
 *
 * @param parent The parent node (null = virtual root).
 * @param child The child node (null = virtual start of child list).
 * @param successor The successor node (null = virtual end of child list).
 * @author Kenneth Yang
 */
public record Pcs(Tree parent, Tree child, Tree successor) {

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
    return Objects.equals(parent, pcs.parent)
        && Objects.equals(child, pcs.child)
        && Objects.equals(successor, pcs.successor);
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
