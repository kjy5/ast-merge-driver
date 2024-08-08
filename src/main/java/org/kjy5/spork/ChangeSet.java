/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.ImmutableTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A Spork change set.
 *
 * <p>Represents an AST as a set of PCSs triples and a set of content tuples. There is potential for
 * this change set to be not-well-formed (meaning it does not resolve into a complete AST).
 *
 * @see org.kjy5.spork.Pcs
 * @see org.kjy5.spork.ContentTuple
 * @author Kenneth Yang
 */
public record ChangeSet(Set<Pcs> pcsSet, Set<ContentTuple> contentTupleSet) {
  // region Factory.
  /**
   * Create a Spork change set from a tree.
   *
   * <p>Nodes from the tree are converted to their class representatives so that a common node is
   * used between matching branches. Virtual nodes are added to mark the root of the tree and the
   * start and end of child lists.
   *
   * @param tree the tree to create the change set from
   * @param nodeToClassRepresentatives the mapping of nodes to class representatives
   */
  public static ChangeSet from(
      Tree tree,
      Map<Tree, Tree> nodeToClassRepresentatives,
      Map<Tree, Tree> virtualRootMapping,
      Map<Tree, String> nodeToSourceFileMapping,
      Map<ContentTuple, String> contentTupleToSourceFileMapping,
      Map<Tree, ChildListVirtualNodes> childListVirtualNodesMapping) {
    // Initialize an empty content tuple set.
    var wipContentTupleSet = new LinkedHashSet<ContentTuple>();

    // Build root of PCS set.
    final var rootClassRepresentative = nodeToClassRepresentatives.get(tree);

    // Create the virtual root for this PCS set.
    final Tree virtualRoot;
    if (virtualRootMapping.containsKey(rootClassRepresentative)) {
      virtualRoot = virtualRootMapping.get(rootClassRepresentative);
    } else {
      virtualRoot = makeVirtualRoot();
      virtualRootMapping.put(rootClassRepresentative, virtualRoot);
    }

    final ChildListVirtualNodes virtualRootChildListVirtualNodes;
    if (childListVirtualNodesMapping.containsKey(virtualRoot)) {
      virtualRootChildListVirtualNodes = childListVirtualNodesMapping.get(virtualRoot);
    } else {
      virtualRootChildListVirtualNodes =
          new ChildListVirtualNodes(makeVirtualChildListStart(), makeVirtualChildListEnd());
      childListVirtualNodesMapping.put(virtualRoot, virtualRootChildListVirtualNodes);
    }

    var wipPcsSet =
        new LinkedHashSet<>(
            Arrays.asList(
                new Pcs(
                    virtualRoot,
                    virtualRootChildListVirtualNodes.childListStart(),
                    rootClassRepresentative),
                new Pcs(
                    virtualRoot,
                    rootClassRepresentative,
                    virtualRootChildListVirtualNodes.childListEnd())));

    // Traverse the tree and build.
    tree.breadthFirst()
        .forEach(
            node -> {
              // Get class representative.
              var classRepresentative = nodeToClassRepresentatives.get(node);

              // Add content tuple (if it has content).
              if (node.hasLabel()) {
                var contentTuple = new ContentTuple(classRepresentative, node.getLabel(), null);

                // Add to set.
                wipContentTupleSet.add(contentTuple);

                // Add to source file mapping.
                contentTupleToSourceFileMapping.put(
                    contentTuple, nodeToSourceFileMapping.get(node));
              }

              // Get or create child list virtual nodes.
              final ChildListVirtualNodes childListVirtualNodes;
              if (childListVirtualNodesMapping.containsKey(classRepresentative)) {
                childListVirtualNodes = childListVirtualNodesMapping.get(classRepresentative);
              } else {
                childListVirtualNodes =
                    new ChildListVirtualNodes(
                        makeVirtualChildListStart(), makeVirtualChildListEnd());
                childListVirtualNodesMapping.put(classRepresentative, childListVirtualNodes);
              }

              // Short-circuit if classRepresentative is leaf.
              if (node.getChildren().isEmpty()) {
                wipPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        childListVirtualNodes.childListStart(),
                        childListVirtualNodes.childListEnd()));
                return;
              }

              // TODO: Check later if virtual classRepresentatives are needed to separate children
              // (i.e. parameters, thrown exceptions).

              // Start children list (add virtual start).
              wipPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      childListVirtualNodes.childListStart(),
                      nodeToClassRepresentatives.get(node.getChild(0))));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < node.getChildren().size() - 1; i++) {
                wipPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        nodeToClassRepresentatives.get(node.getChild(i)),
                        nodeToClassRepresentatives.get(node.getChild(i + 1))));
              }

              // End children list (add virtual end).
              wipPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      nodeToClassRepresentatives.get(node.getChild(node.getChildren().size() - 1)),
                      childListVirtualNodes.childListEnd()));
            });

    // Set the final change set.
    return new ChangeSet(
        Collections.unmodifiableSet(wipPcsSet), Collections.unmodifiableSet(wipContentTupleSet));
  }

  // endregion

  // region Virtual node factories.

  /**
   * Create a virtual root node to mark the root of an AST in a PCS set.
   *
   * @return a new virtual root node
   */
  private static Tree makeVirtualRoot() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualRoot"));
  }

  /**
   * Create a virtual child list start node to mark the start of a child list in a PCS set.
   *
   * @return a new virtual child list start node
   */
  private static Tree makeVirtualChildListStart() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListStart"));
  }

  /**
   * Create a virtual child list end node to mark the end of a child list in a PCS set.
   *
   * @return a new virtual child list end node
   */
  private static Tree makeVirtualChildListEnd() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListEnd"));
  }

  // endregion

  // region Tree conversion methods.
  /**
   * Convert this change set to a GumTree AST.
   *
   * @return the corresponding GumTree AST
   */
  public Tree toGumTreeTree() {
    // Find root.
    var maybeRootPcs =
        pcsSet.stream()
            .filter(
                pcs ->
                    pcs.parent().getLabel().equals("virtualRoot")
                        && pcs.child().getLabel().equals("virtualChildListStart"))
            .findFirst();
    if (maybeRootPcs.isEmpty()) {
      throw new RuntimeException("Unable to find root in merged change set.");
    }

    // Rebuild the tree.
    return toGumTreeTree(maybeRootPcs.get().successor());
  }

  /**
   * Convert a node and its children to a GumTree AST.
   *
   * @param node the node to convert
   * @return the corresponding GumTree AST
   */
  private Tree toGumTreeTree(Tree node) {
    // Create new children list.
    var children = new LinkedList<Tree>();

    // Find the first child.
    var maybeFirstChildPcs =
        pcsSet.stream()
            .filter(
                pcs ->
                    pcs.parent() == node && pcs.child().getLabel().equals("virtualChildListStart"))
            .findFirst();

    // Short-circuit if first child is not found.
    if (maybeFirstChildPcs.isEmpty()) {
      throw new RuntimeException(
          "Unable to find first child of " + node + " in merged change set.");
    }
    var currentChild = maybeFirstChildPcs.get().successor();

    // Ensure there is an end node.
    if (pcsSet.stream()
        .noneMatch(
            pcs ->
                pcs.parent() == node && pcs.successor().getLabel().equals("virtualChildListEnd"))) {
      throw new RuntimeException("Unable to find end node of " + node + " in merged change set.");
    }

    // Iterate through children.
    while (!currentChild.getLabel().equals("virtualChildListEnd")) {
      // Recuse add this child to the list.
      children.add(toGumTreeTree(currentChild));

      // Get next child.
      var currentScopeChild = currentChild;
      var maybeNextChildPcs =
          pcsSet.stream()
              .filter(pcs -> pcs.parent() == node && pcs.child() == currentScopeChild)
              .findFirst();
      if (maybeNextChildPcs.isEmpty()) {
        throw new RuntimeException(
            "Unable to find next child of " + currentChild + " in merged change set.");
      }
      currentChild = maybeNextChildPcs.get().successor();
    }

    // Set nodes new children are replacing in metadata.
    for (int i = 0; i < children.size(); i++) {
      children.get(i).setMetadata("replacing", node.getChild(i));
    }

    // Update the children set of the node.
    node.setChildren(children);

    return node;
  }
  // endregion
}
