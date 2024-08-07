package org.kjy5.spork;

import com.github.gumtreediff.tree.Tree;
import java.util.Optional;

// This comment does not mention `hardInconsistencyWith`.
/**
 * A Spork content tuple.
 *
 * <p>Contains a node and its content (i.e. value for a literal, name for variable).
 *
 * @param node The node.
 * @param content The content associated with the node.
 * @author Kenneth Yang
 */
// It's a bit inconsistent that the type of `node` is `Tree`.  Why not name the field `tree`?
public record ContentTuple(
    Tree node, String content, Optional<ContentTuple> hardInconsistencyWith) {

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
