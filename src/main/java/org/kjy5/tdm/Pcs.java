/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.tdm;

import com.github.gumtreediff.tree.Tree;

/**
 * A 3DM parent-child-successor triple.
 *
 * <p>A null node in the triple is the virtual node.
 *
 * @author Kenneth Yang
 */
public class Pcs {
  public final Tree parent, child, successor;

  /**
   * Create a PCS triple.
   *
   * <p>A null parent is the virtual root, a null child is the virtual start, a null successor is
   * the virtual end.
   *
   * @param parent Parent node (null is virtual)
   * @param child Child node (null is virtual start)
   * @param successor Successor node (null is virtual end)
   */
  public Pcs(Tree parent, Tree child, Tree successor) {
    this.parent = parent;
    this.child = child;
    this.successor = successor;
  }
}
