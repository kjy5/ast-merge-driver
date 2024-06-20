package org.kjy5;

import static com.github.javaparser.utils.PositionUtils.sortByBeginPosition;
import static java.util.Objects.requireNonNull;

import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import java.util.LinkedList;
import java.util.List;
import org.kjy5.Common.*;
import org.w3c.dom.Element;

@SuppressWarnings("StringTemplateMigration")
public class JavaParserToXML {
  // region Fields
  private final XMLDocument xmlDocument;

  // endregion

  // region Constructors
  public JavaParserToXML(Node parsingRoot) {
    xmlDocument = new XMLDocument();
    xmlDocument.setRootElement(serialize(null, parsingRoot, xmlDocument));
  }

  // endregion

  // region Serialization methods

  // This implementation is from Randoop's `Minimize.java` file, and before that from JavaParser's
  // PrettyPrintVisitor.printOrphanCommentsBeforeThisChildNode.  The JavaParser maintainers refuse
  // to provide such functionality in JavaParser proper.

  /**
   * This implementation is from Randoop's `Minimize.java` file, and before that from JavaParser's
   * PrettyPrintVisitor.printOrphanCommentsBeforeThisChildNode. The JavaParser maintainers refuse to
   * provide such functionality in JavaParser proper.
   *
   * @param node The node for which comments are to be extracted from.
   * @param result The list to which the comments are to be added.
   */
  private static void getOrphanCommentsBeforeThisChildNode(final Node node, List<Comment> result) {
    if (node instanceof Comment) {
      return;
    }

    Node parent = node.getParentNode().orElse(null);
    if (parent == null) {
      return;
    }
    List<Node> everything = new LinkedList<>(parent.getChildNodes());
    sortByBeginPosition(everything);
    int positionOfTheChild = -1;
    for (int i = 0; i < everything.size(); i++) {
      if (everything.get(i) == node) {
        positionOfTheChild = i;
      }
    }
    if (positionOfTheChild == -1) {
      throw new AssertionError("I am not a child of my parent.");
    }
    int positionOfPreviousChild = -1;
    for (int i = positionOfTheChild - 1; i >= 0 && positionOfPreviousChild == -1; i--) {
      if (!(everything.get(i) instanceof Comment)) {
        positionOfPreviousChild = i;
      }
    }
    for (int i = positionOfPreviousChild + 1; i < positionOfTheChild; i++) {
      Node nodeToPrint = everything.get(i);
      if (!(nodeToPrint instanceof Comment)) {
        throw new RuntimeException(
            "Expected comment, instead "
                + nodeToPrint.getClass()
                + ". Position of previous child: "
                + positionOfPreviousChild
                + ", position of child "
                + positionOfTheChild);
      }
      result.add((Comment) nodeToPrint);
    }
  }

  /**
   * Recursive depth-first method that serializes nodes into XML
   *
   * @param nodeName nullable String. If null, it is the root object, otherwise it is the property
   *     key for the object
   * @param node the current node to be serialized
   * @param xmlDocument the XML document for writing the XML
   * @return the XML element representing the node
   */
  private Element serialize(String nodeName, Node node, XMLDocument xmlDocument) {
    // Get node meta model.
    requireNonNull(node);
    BaseNodeMetaModel nodeMetaModel =
        JavaParserMetaModel.getNodeMetaModel(node.getClass())
            .orElseThrow(() -> new IllegalStateException("Unknown Node: " + node.getClass()));

    // Create Element.
    var element = xmlDocument.createElement(nodeName);

    // Set class name as attribute.
    element.setAttribute(XMLNode.CLASS.propertyKey, node.getClass().getName());

    // Write non-meta properties.
    //    writeNonMetaProperties(node, element, xmlDocument);

    // Write meta properties.
    for (PropertyMetaModel propertyMetaModel : nodeMetaModel.getAllPropertyMetaModels()) {
      // Get property name and value.
      var name = propertyMetaModel.getName();
      var value = propertyMetaModel.getValue(node);

      // Skip if value is null.
      if (value == null) {
        continue;
      }

      if (propertyMetaModel.isNodeList()) {
        // Handle node list.
        @SuppressWarnings("unchecked")
        NodeList<Node> list = (NodeList<Node>) value;

        // Skip if list is empty.
        if (list.isEmpty()) {
          continue;
        }

        // Create element for list.
        var listElement = xmlDocument.createChildElement(name, element);

        // Populate list element.
        for (Node n : list) {
          listElement.appendChild(serialize(null, n, xmlDocument));
        }
      } else if (propertyMetaModel.isNode()) {
        // Handle single node.
        element.appendChild(serialize(name, (Node) value, xmlDocument));
      } else {
        // Otherwise, treat it as content.
        var valueElement = xmlDocument.createChildElement(name, element);
        valueElement.setTextContent(value.toString());
      }
    }
    
    // Handle orphan comments.
    List<Comment> orphanComments = new LinkedList<>();
    getOrphanCommentsBeforeThisChildNode(node, orphanComments);
    for (Comment comment : orphanComments) {
      element.appendChild(serialize("orphan", comment, xmlDocument));
    }

    // Return Element.
    return element;
  }

  /***
   * This method writes XML for properties not included in meta model (i.e., RANGE and TOKEN_RANGE).
   * This method could be overriden so that - for example - tokens are not written to XML to save space
   *
   * @see com.github.javaparser.metamodel.BaseNodeMetaModel#getAllPropertyMetaModels()
   */
  protected void writeNonMetaProperties(Node node, Element element, XMLDocument xmlDocument) {
    writeRange(node, element, xmlDocument);
    writeTokens(node, element, xmlDocument);
  }

  protected void writeRange(Node node, Element element, XMLDocument xmlDocument) {
    // Skip if no range.
    if (!node.hasRange() || node.getRange().isEmpty()) {
      return;
    }

    // Get range.
    Range range = node.getRange().get();

    // Create range element.
    var rangeElement = xmlDocument.createChildElement(XMLNode.RANGE.propertyKey, element);

    // Add properties to range element.
    var beginLineElement =
        xmlDocument.createChildElement(XMLRange.BEGIN_LINE.propertyKey, rangeElement);
    beginLineElement.setTextContent(Integer.toString(range.begin.line));

    var beginColumnElement =
        xmlDocument.createChildElement(XMLRange.BEGIN_COLUMN.propertyKey, rangeElement);
    beginColumnElement.setTextContent(Integer.toString(range.begin.column));

    var endLineElement =
        xmlDocument.createChildElement(XMLRange.END_LINE.propertyKey, rangeElement);
    endLineElement.setTextContent(Integer.toString(range.end.line));

    var endColumnElement =
        xmlDocument.createChildElement(XMLRange.END_COLUMN.propertyKey, rangeElement);
    endColumnElement.setTextContent(Integer.toString(range.end.column));
  }

  protected void writeTokens(Node node, Element element, XMLDocument xmlDocument) {
    // Skip if no token range.
    if (node.getTokenRange().isEmpty()) {
      return;
    }

    // Get token range.
    TokenRange tokenRange = node.getTokenRange().get();

    // Create token range element.
    var tokenRangeElement =
        xmlDocument.createChildElement(XMLNode.TOKEN_RANGE.propertyKey, element);

    // Create begin token element.
    var beginTokenElement =
        xmlDocument.createChildElement(XMLTokenRange.BEGIN_TOKEN.propertyKey, tokenRangeElement);

    // Add properties to begin token element.
    var beginTokenKindElement =
        xmlDocument.createChildElement(XMLToken.KIND.propertyKey, beginTokenElement);
    beginTokenKindElement.setTextContent(Integer.toString(tokenRange.getBegin().getKind()));

    var beginTokenTextElement =
        xmlDocument.createChildElement(XMLToken.TEXT.propertyKey, beginTokenElement);
    beginTokenTextElement.setTextContent(tokenRange.getBegin().getText());

    // Create end token element.
    var endTokenElement =
        xmlDocument.createChildElement(XMLTokenRange.END_TOKEN.propertyKey, tokenRangeElement);

    // Add properties to end token element.
    var endTokenKindElement =
        xmlDocument.createChildElement(XMLToken.KIND.propertyKey, endTokenElement);
    endTokenKindElement.setTextContent(Integer.toString(tokenRange.getEnd().getKind()));

    var endTokenTextElement =
        xmlDocument.createChildElement(XMLToken.TEXT.propertyKey, endTokenElement);
    endTokenTextElement.setTextContent(tokenRange.getEnd().getText());
  }

  // endregion

  // region Getters
  public XMLDocument getXmlDocument() {
    return xmlDocument;
  }

  // endregion

}
