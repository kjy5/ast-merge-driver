package org.kjy5.tdm;

import com.github.gumtreediff.tree.Tree;

/**
 * A 3DM content tuple.
 *
 * <p>Contains a node and its content (i.e. value for a literal, name for variable).
 *
 * @author Kenneth Yang
 */
public class ContentTuple {
  public final Tree node;
  public final String content;

  public ContentTuple(Tree node, String content) {
    this.node = node;
    this.content = content;
  }
}
