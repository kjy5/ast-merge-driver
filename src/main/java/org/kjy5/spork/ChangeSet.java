/*
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5.spork;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.ImmutableTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import java.util.*;

// What is the definition of "A Spork change set"?  That comment, on its own, is not descriptive.
/**
 * A Spork change set.
 *
 * @author Kenneth Yang
 */
// I realize that Spork calls the data structure a "change set", but I think "PcsSet" would be more
// descriptive, or even "something along the lines of "Java file representation".  (The comment here
// could say that Spork calls it a "change set", without using that naming throughout.)
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
      Map<Tree, String> nodeToSourceFileMapping,
      Map<ContentTuple, String> contentTupleToSourceFileMapping,
      Map<Tree, ChildListVirtualNodes> childListVirtualNodesMapping) {
    // This isn't "an empty content tuple", but a set.
    // Why does this need to be a set as opposed to (say) a list?
    // Initialize an empty content tuple.
    var wipContentTupleSet = new LinkedHashSet<ContentTuple>();

    // Build root of PCS set.
    // What is a "class representative"?
    final var rootClassRepresentative = classRepresentativesMapping.get(tree);

    // What does "virtual" mean?
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
              if (node.hasLabel()) {
                var contentTuple =
                    new ContentTuple(classRepresentative, node.getLabel(), Optional.empty());

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

  // These methods return a fresh value every time.  Could they return the same value every time,
  // for efficiency?  Or is use of a fresh value important?
  private static Tree makeVirtualChildListStart() {
    return new ImmutableTree(new DefaultTree(Type.NO_TYPE, "virtualChildListStart"));
  }

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
  // Consider naming this "toGtTree()" to emphasize that it returns a GumTree tree rather than some
  // other type.
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

  private Tree toTree(Tree node) {
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
      children.add(toTree(currentChild));

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
