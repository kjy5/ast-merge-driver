package org.kjy5;

import static com.github.javaparser.utils.Utils.decapitalize;
import static java.util.Objects.requireNonNull;

import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import org.w3c.dom.Element;

@SuppressWarnings("StringTemplateMigration")
public class JavaParserToXMLSerializer {
  // region Fields
  private final XMLDocument xmlDocument;

  // endregion

  // region Constructors
  public JavaParserToXMLSerializer(Node parsingRoot) {
    xmlDocument = new XMLDocument();
    xmlDocument.addRootElement(serialize(null, parsingRoot, xmlDocument));
  }

  // endregion

  // region Serialization methods
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
    requireNonNull(node);
    BaseNodeMetaModel nodeMetaModel =
        JavaParserMetaModel.getNodeMetaModel(node.getClass())
            .orElseThrow(() -> new IllegalStateException("Unknown Node: " + node.getClass()));

    // Create Element.
    var element = xmlDocument.createElement(nodeName);

    // Add class name.
    element.setAttribute(XMLNode.CLASS.propertyKey, node.getClass().getName());

    // Write non-meta properties.
    //    writeNonMetaProperties(node, element);

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
        // Otherwise, treat it as an attribute.
        element.setAttribute(name, value.toString());
      }
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
  protected void writeNonMetaProperties(Node node, Element element) {
    writeRange(node, element);
    writeTokens(node, element);
  }

  protected void writeRange(Node node, Element element) {
    // Skip if no range.
    if (!node.hasRange() || node.getRange().isEmpty()) {
      return;
    }

    // Get range.
    Range range = node.getRange().get();

    // Write range.
    element.setAttribute(XMLRange.BEGIN_LINE.propertyKey, Integer.toString(range.begin.line));
    element.setAttribute(XMLRange.BEGIN_LINE.propertyKey, Integer.toString(range.begin.line));
    element.setAttribute(XMLRange.BEGIN_COLUMN.propertyKey, Integer.toString(range.begin.column));
    element.setAttribute(XMLRange.END_LINE.propertyKey, Integer.toString(range.end.line));
    element.setAttribute(XMLRange.END_COLUMN.propertyKey, Integer.toString(range.end.column));
  }

  protected void writeTokens(Node node, Element element) {
    // Skip if no token range.
    if (node.getTokenRange().isEmpty()) {
      return;
    }

    // Get token range.
    TokenRange tokenRange = node.getTokenRange().get();

    // Write token range.
    element.setAttribute(
        XMLTokenRange.BEGIN_TOKEN.propertyKey + "." + XMLToken.KIND.propertyKey,
        Integer.toString(tokenRange.getBegin().getKind()));
    element.setAttribute(
        XMLTokenRange.BEGIN_TOKEN.propertyKey + "." + XMLToken.TEXT.propertyKey,
        tokenRange.getBegin().getText());
    element.setAttribute(
        XMLTokenRange.END_TOKEN.propertyKey + "." + XMLToken.KIND.propertyKey,
        Integer.toString(tokenRange.getEnd().getKind()));
    element.setAttribute(
        XMLTokenRange.END_TOKEN.propertyKey + "." + XMLToken.TEXT.propertyKey,
        tokenRange.getEnd().getText());
  }

  // endregion

  // region Getters
  public XMLDocument getXmlDocument() {
    return xmlDocument;
  }

  // endregion

  // region XML enums
  /** excludes properties from meta model (except comment) */
  public enum XMLNode {
    RANGE("range"),
    TOKEN_RANGE("tokenRange"),
    COMMENT(decapitalize(JavaParserMetaModel.commentMetaModel.getTypeName())),
    CLASS("Class");
    final String propertyKey;

    XMLNode(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLRange {
    BEGIN_LINE("beginLine"),
    BEGIN_COLUMN("beginColumn"),
    END_LINE("endLine"),
    END_COLUMN("endColumn");
    final String propertyKey;

    XMLRange(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLTokenRange {
    BEGIN_TOKEN("beginToken"),
    END_TOKEN("endToken");
    final String propertyKey;

    XMLTokenRange(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLToken {
    TEXT("text"),
    KIND("kind");
    final String propertyKey;

    XMLToken(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }
  // endregion
}
