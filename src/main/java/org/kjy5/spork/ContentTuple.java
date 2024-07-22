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
   * Print the content tuple.
   *
   * @return A string representation of the content tuple.
   */
  @Override
  public String toString() {
    return "ContentTuple(" + node + ", " + content + ')';
  }
}
