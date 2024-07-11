/**
 * Main class for the merge driver.
 *
 * <p>Reimplements Spork by Larsén et al. (2022) in Java with JavaParser.
 *
 * <p>Copyright 2024 Kenneth Yang (kjy5@uw.edu)
 */
package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import java.io.IOException;
import java.util.HashMap;
import org.kjy5.javaparser.JavaParserGenerator;
import org.kjy5.tdm.ChangeSet;

/**
 * Main class for the merge driver.
 *
 * @author Kenneth Yang
 */
public class Main {
  // region File path constants.
  private static final String ASSETS_FOLDER_PATH = "assets/";
  private static final String BASE_FILE_PATH = "/file_base";
  private static final String LEFT_FILE_PATH = "/file_left";
  private static final String RIGHT_FILE_PATH = "/file_right";
  //  private static final String MERGED_FILE_PATH = "/file_merged";
  private static final String JAVA_FILE_EXTENSION = ".java";

  // endregion

  /**
   * Entry point of the program.
   *
   * <p>Runs the full pipeline
   *
   * @param args Command line arguments (folder name)
   */
  public static void main(String[] args) {
    // region File path specifications.
    final var folder = args.length > 0 ? args[0] : "0";

    // Source files.
    final var fileBasePath = ASSETS_FOLDER_PATH + folder + BASE_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileLeftPath = ASSETS_FOLDER_PATH + folder + LEFT_FILE_PATH + JAVA_FILE_EXTENSION;
    final var fileRightPath = ASSETS_FOLDER_PATH + folder + RIGHT_FILE_PATH + JAVA_FILE_EXTENSION;
    //    final var fileMergedPath = ASSETS_FOLDER_PATH + folder + MERGED_FILE_PATH +
    // JAVA_FILE_EXTENSION;
    // endregion

    // region Create matching between branches.
    final var javaParserGenerator = new JavaParserGenerator();

    // Create parsings.
    final Tree baseTree, leftTree, rightTree;
    try {
      baseTree = javaParserGenerator.generateFrom().file(fileBasePath).getRoot();
      leftTree = javaParserGenerator.generateFrom().file(fileLeftPath).getRoot();
      rightTree = javaParserGenerator.generateFrom().file(fileRightPath).getRoot();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // TODO: Consider mapping from left/right to base to better follow class representative logic.
    // Match the trees.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseToLeftMapping = matcher.match(baseTree, leftTree);
    final var baseToRightMapping = matcher.match(baseTree, rightTree);
    final var leftToRightMapping = matcher.match(leftTree, rightTree);
    // endregion

    // region Create class representative mappings.
    var classRepresentativeMapping = new HashMap<Tree, Tree>();

    // Base nodes are mapped to themselves.
    baseTree.preOrder().forEach(node -> classRepresentativeMapping.put(node, node));

    /*
     * Left nodes are mapped to base if a matching exists, otherwise they're mapped to themselves.
     */
    leftTree
        .preOrder()
        .forEach(
            node -> {
              final var matchedBaseNode = baseToLeftMapping.getSrcForDst(node);
              if (matchedBaseNode != null) {
                // A matching exists, map to it.
                classRepresentativeMapping.put(node, matchedBaseNode);
              } else {
                // No matching exists, map to self.
                classRepresentativeMapping.put(node, node);
              }
            });

    /*
     * Right nodes are mapped to base first if a matching exists, then left, otherwise themselves.
     * Breadth first ordering is used to ensure parents are mapped before children.
     * This is used in preventing spurious left-to-right mappings.
     */
    rightTree
        .breadthFirst()
        .forEach(
            node -> {
              final var matchedBaseNode = baseToRightMapping.getSrcForDst(node);
              if (matchedBaseNode != null) {
                // A matching exists, map to it.
                classRepresentativeMapping.put(node, matchedBaseNode);
              } else {
                // No base matching, try left.
                final var matchedLeftNode = leftToRightMapping.getSrcForDst(node);
                if (matchedLeftNode != null
                    && !baseToLeftMapping.isSrcMapped(matchedLeftNode)
                    && classRepresentativeMapping
                        .get(matchedLeftNode.getParent())
                        .equals(classRepresentativeMapping.get(node.getParent()))) {
                  // A matching to left exists, left node is not matched to base, and parents of
                  // left and right nodes are matched to the same class representative.
                  classRepresentativeMapping.put(node, matchedLeftNode);
                } else {
                  // No matching exists, map to self.
                  classRepresentativeMapping.put(node, node);
                }
              }
            });
    // endregion

    // region Create change sets (PCS and content tuples).
    var baseChangeSet = new ChangeSet(baseTree);
    var leftChangeSet = new ChangeSet(leftTree);
    var rightChangeSet = new ChangeSet(rightTree);
    // endregion

    // region Raw merge (union of all change sets).
    var rawMerge = new ChangeSet(baseChangeSet, leftChangeSet, rightChangeSet);
    // endregion
  }
}
