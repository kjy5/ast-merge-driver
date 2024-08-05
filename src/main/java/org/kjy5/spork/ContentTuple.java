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
 * @param src The source file of the content.
 * @author Kenneth Yang
 */
public record ContentTuple(
    Tree node, String content, String src, Optional<ContentTuple> hardInconsistencyWith) {

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
