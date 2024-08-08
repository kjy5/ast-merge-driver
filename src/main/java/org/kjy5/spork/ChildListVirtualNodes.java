package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;

/**
 * Virtual nodes that mark the start and end of a child list.
 *
 * @param childListStart Virtual node that marks the start of a child list.
 * @param childListEnd Virtual node that marks the end of a child list.
 */
public record ChildListVirtualNodes(Tree childListStart, Tree childListEnd) {}
