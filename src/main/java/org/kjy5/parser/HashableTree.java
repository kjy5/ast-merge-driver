package org.kjy5.parser;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;

public class HashableTree extends DefaultTree {

  public HashableTree(Type type) {
    super(type);
  }

  public HashableTree(Type type, String label) {
    super(type, label);
  }

  public HashableTree(Tree other) {
    super(other);
  }

  @Override
  public boolean equals(Object other) {
    // Short-circuit if it's the same object.
    if (other == this) return true;

    // Check if other is of type HashableTree.
    if (!(other instanceof HashableTree otherHashableTree)) return false;

    // Get attached parsed nodes.
    final var thisNode = this.getMetadata("node");
    final var otherNode = otherHashableTree.getMetadata("node");

    // Check if type, label, and metadata match.
    return this.getType().equals(otherHashableTree.getType())
        && this.getLabel().equals(otherHashableTree.getLabel())
        && (thisNode == null || thisNode.equals(otherNode));
  }

  @Override
  public int hashCode() {
    int result = 17; // Non-zero constant
    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
    result = 31 * result + (getLabel() != null ? getLabel().hashCode() : 0);
    var nodeMetadata = getMetadata("node");
    result = 31 * result + (nodeMetadata != null ? nodeMetadata.hashCode() : 0);
    return result;
  }
}
