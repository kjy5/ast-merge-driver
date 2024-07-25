/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.ImmutableTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import java.util.*;

/**
 * A Spork change set.
 *
 * @author Kenneth Yang
 */
public record ChangeSet(Set<Pcs> pcsSet, Set<ContentTuple> contentTupleSet) {
  // region Factory.
  /**
   * Create a Spork change set from a tree.
   *
   * @param tree The tree to create the change set from.
   * @param classRepresentativesMapping The mapping of nodes to class representatives.
   */
  public static ChangeSet from(
      Tree tree,
      Map<Tree, Tree> classRepresentativesMapping,
      Map<Tree, Tree> virtualRootMapping,
      Map<Tree, ChildListVirtualNodes> childListVirtualNodesMapping) {
    // Initialize an empty content tuple.
    var wipContentTupleSet = new LinkedHashSet<ContentTuple>();

    // Build root of PCS set.
    final var rootClassRepresentative = classRepresentativesMapping.get(tree);

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
              var classRepresentative = classRepresentativesMapping.get(node);

              // Add content tuple (if it has content).
              if (node.hasLabel())
                wipContentTupleSet.add(new ContentTuple(classRepresentative, node.getLabel()));

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
                      classRepresentativesMapping.get(node.getChild(0))));

              // Loop through children (except last one which needs virtual end).
              for (int i = 0; i < node.getChildren().size() - 1; i++) {
                wipPcsSet.add(
                    new Pcs(
                        classRepresentative,
                        classRepresentativesMapping.get(node.getChild(i)),
                        classRepresentativesMapping.get(node.getChild(i + 1))));
              }

              // End children list (add virtual end).
              wipPcsSet.add(
                  new Pcs(
                      classRepresentative,
                      classRepresentativesMapping.get(node.getChild(node.getChildren().size() - 1)),
                      childListVirtualNodes.childListEnd()));
            });

    // Set the final change set.
    return new ChangeSet(
        Collections.unmodifiableSet(wipPcsSet), Collections.unmodifiableSet(wipContentTupleSet));
  }

  // endregion

  // region Virtual node factories.
  private static Tree makeVirtualRoot() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualRoot"));
  }

  private static Tree makeVirtualChildListStart() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListStart"));
  }

  private static Tree makeVirtualChildListEnd() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListEnd"));
  }

  // endregion

  // region Tree conversion methods.
  /**
   * Convert this change set to an AST.
   *
   * @return The corresponding AST (as a GumTree Tree).
   */
  public Tree toTree() {
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
    return toTree(maybeRootPcs.get().successor());
  }

  private Tree toTree(Tree child) {
    var rebuiltNode = child.deepCopy();

    // Copy metadata of the rebuilt tree.
    child
        .getMetadata()
        .forEachRemaining(entry -> rebuiltNode.setMetadata(entry.getKey(), entry.getValue()));

    // Set the content of the rebuilt tree.
    contentTupleSet.stream()
        .filter(contentTuple -> contentTuple.node() == child)
        .findFirst()
        .ifPresent(contentTuple -> rebuiltNode.setLabel(contentTuple.content()));

    // Set children of the rebuilt tree.
    var children = new LinkedList<Tree>();

    // Find the first child.
    var maybeFirstChildPcs =
        pcsSet.stream()
            .filter(
                pcs ->
                    pcs.parent() == child && pcs.child().getLabel().equals("virtualChildListStart"))
            .findFirst();

    // Short-circuit if first child is not found.
    if (maybeFirstChildPcs.isEmpty()) {
      throw new RuntimeException(
          "Unable to find first child of " + child + " in merged change set.");
    }
    var currentChild = maybeFirstChildPcs.get().successor();

    // Ensure there is an end node.
    if (pcsSet.stream()
        .noneMatch(
            pcs ->
                pcs.parent() == child
                    && pcs.successor().getLabel().equals("virtualChildListEnd"))) {
      throw new RuntimeException("Unable to find end node of " + child + " in merged change set.");
    }

    // Iterate through children.
    while (!currentChild.getLabel().equals("virtualChildListEnd")) {
      // Recuse add this child to the list.
      children.add(toTree(currentChild));

      // Get next child.
      var currentScopeChild = currentChild;
      var maybeNextChildPcs =
          pcsSet.stream()
              .filter(pcs -> pcs.parent() == child && pcs.child() == currentScopeChild)
              .findFirst();
      if (maybeNextChildPcs.isEmpty()) {
        throw new RuntimeException(
            "Unable to find next child of " + currentChild + " in merged change set.");
      }
      currentChild = maybeNextChildPcs.get().successor();
    }

    // Set children of the rebuilt tree.
    rebuiltNode.setChildren(children);

    return rebuiltNode;
  }
  // endregion
}
