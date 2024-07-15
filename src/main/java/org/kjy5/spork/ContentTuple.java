package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;
import java.util.Optional;

/**
 * A Spork content tuple.
 *
 * <p>Contains a node and its content (i.e. value for a literal, name for variable).
 *
 * @param node The node.
 * @param content The content associated with the node.
 * @author Kenneth Yang
 */
public record ContentTuple(
    Tree node, String content, Optional<ContentTuple> hardInconsistencyWith) {

  /**
   * Construct a content tuple on required fields.
   *
   * @param node The node.
   * @param content The content associated with the node.
   */
  public ContentTuple(Tree node, String content) {
    this(node, content, Optional.empty());
  }

  /**
   * Check for equality.
   *
   * @param other The reference object with which to compare.
   * @return True if the content tuples are equal, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof ContentTuple contentTuple)) return false;
    return node.equals(contentTuple.node)
        && content.equals(contentTuple.content)
        && hardInconsistencyWith.equals(contentTuple.hardInconsistencyWith);
  }

  /**
   * Print the content tuple.
   *
   * @return A string representation of the content tuple.
   */
  @Override
  public String toString() {
    return "ContentTuple(" + node + ", " + content + ')';
  }
}
