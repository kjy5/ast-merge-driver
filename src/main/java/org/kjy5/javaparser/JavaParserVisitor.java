/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Kenneth Yang added putting the JavaParser node into the
 * metadata of the tree and applied IntelliJ suggested code fixes.
 *
 * Copyright 2018 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2024 Kenneth Yang (kjy5@uw.edu)
 */

package org.kjy5.javaparser;

import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.Type;
import com.github.gumtreediff.tree.TypeSet;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.TreeVisitor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class JavaParserVisitor extends TreeVisitor {

  private final Deque<Tree> trees;
  private final LineReader reader;
  protected TreeContext context;

  public JavaParserVisitor(LineReader reader) {
    this.context = new TreeContext();
    this.trees = new ArrayDeque<>();
    this.reader = reader;
  }

  public TreeContext getTreeContext() {
    return context;
  }

  @Override
  public void visitPreOrder(Node node) {
    process(node);
    new ArrayList<>(node.getChildNodes()).forEach(this::visitPreOrder);
    if (!trees.isEmpty()) trees.pop();
  }

  @Override
  public void process(Node node) {
    String label = "";
    if (node instanceof Name)
      label = ((Name) node).getIdentifier(); // FIXME: might be better to flatten name hierarchies.
    else if (node instanceof SimpleName) label = ((SimpleName) node).getIdentifier();
    else if (node instanceof StringLiteralExpr) label = ((StringLiteralExpr) node).asString();
    else if (node instanceof BooleanLiteralExpr)
      label = Boolean.toString(((BooleanLiteralExpr) node).getValue());
    else if (node instanceof LiteralStringValueExpr)
      label = ((LiteralStringValueExpr) node).getValue();
    else if (node instanceof PrimitiveType) label = ((PrimitiveType) node).asString();
    else if (node instanceof Modifier) label = ((Modifier) node).getKeyword().asString();

    pushNode(node, label);
  }

  protected void pushNode(Node n, String label) {
    if (n.getRange().isPresent()) {
      Position begin = n.getRange().get().begin;
      Position end = n.getRange().get().end;
      int startPos = reader.positionFor(begin.line, begin.column);
      int length = reader.positionFor(end.line, end.column) - startPos + 1;
      push(nodeAsSymbol(n), label, startPos, length, n);
    }
  }

  protected Type nodeAsSymbol(Node n) {
    return TypeSet.type(n.getClass().getSimpleName());
  }

  private void push(Type type, String label, int startPosition, int length, Node node) {
    Tree t = context.createTree(type, label);
    t.setPos(startPosition);
    t.setLength(length);
    t.setMetadata("node", node);

    if (trees.isEmpty()) context.setRoot(t);
    else {
      Tree parent = trees.peek();
      t.setParentAndUpdateChildren(parent);
    }

    trees.push(t);
  }
}
