package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;

/**
 * A Spork content tuple.
 *
 * <p>Contains a node, its content (i.e. value for a literal, name for variable), and a reference to
 * the content tuple it is hard inconsistent with (if any)
 *
 * @param node the node for which the content is associated with
 * @param content the content associated with the node
 * @param hardInconsistencyWith the content tuple it is hard inconsistent with
 * @author Kenneth Yang
 */
public record ContentTuple(Tree node, String content, ContentTuple hardInconsistencyWith) {

  /**
   * Print the content tuple.
   *
   * @return A string representation of the content tuple.
   */
  @Override
  public String toString() {
    return "ContentTuple(" + node + ", " + content + ", " + hardInconsistencyWith + ')';
  }
}
