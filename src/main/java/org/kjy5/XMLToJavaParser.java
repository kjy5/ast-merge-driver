package org.kjy5;

import static com.github.javaparser.metamodel.JavaParserMetaModel.getNodeMetaModel;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.kjy5.Common.*;
import org.w3c.dom.Element;

@SuppressWarnings("StringTemplateMigration")
public class XMLToJavaParser {
  // region Fields
  private final Node astRoot;

  // endregion

  // region Constructors
  public XMLToJavaParser(XMLDocument xmlDocument) {
    astRoot = deserialize(xmlDocument.getRootElement());
  }

  // endregion

  // region Deserialization methods

  /**
   * Recursive depth-first deserializing method that creates a Node instance from XML element.
   *
   * @param element XML element at current level containing values as properties
   * @return deserialized node including all children.
   * @implNote the Node instance will be constructed by the properties defined in the meta model.
   *     Non-meta properties will be set after Node is instantiated.
   * @implNote comment is included in the propertyKey meta model, but not set when constructing the
   *     Node instance. That is, comment is not included in the constructor propertyKey list, and
   *     therefore needs to be set after constructing the node. See {@link
   *     com.github.javaparser.metamodel.BaseNodeMetaModel#construct(Map)} how the node is
   *     contructed
   */
  private Node deserialize(Element element) {
    try {
      String serializedNodeType = element.getTagName();
      BaseNodeMetaModel nodeMetaModel =
          getNodeMetaModel(Class.forName(serializedNodeType))
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Trying to deserialize an unknown node type: " + serializedNodeType));
      var parameters = new HashMap<String, Object>();
      var deferredElements = new HashMap<String, Element>();

      // Loop through each child node.
      for (int i = 0; i < element.getChildNodes().getLength(); i++) {
        // Get this child node.
        var childNode = element.getChildNodes().item(i);

        // Skip if not an element.
        if (childNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
          continue;
        }

        // Get the node as an element.
        Element childElement = (Element) childNode;
        var name = childElement.getTagName();

        Optional<PropertyMetaModel> optionalPropertyMetaModel =
            nodeMetaModel.getAllPropertyMetaModels().stream()
                .filter(mm -> mm.getName().equals(name))
                .findFirst();
        if (!optionalPropertyMetaModel.isPresent()) {
          deferredElements.put(name, childElement);
          continue;
        }

        // Recurse and handle each property.
        PropertyMetaModel propertyMetaModel = optionalPropertyMetaModel.get();
        if (propertyMetaModel.isNodeList()) {
          parameters.put(name, deserializeNodeList(childElement));
        } else if (propertyMetaModel.isNode()) {
          parameters.put(name, deserialize(childElement));
        } else {
          String value = childElement.getTextContent();
          Class<?> type = propertyMetaModel.getType();
          if (type == String.class) {
            parameters.put(name, value);
          } else if (type == boolean.class) {
            parameters.put(name, Boolean.parseBoolean(value));
          } else if (Enum.class.isAssignableFrom(type)) {
            parameters.put(name, Enum.valueOf((Class<? extends Enum>) type, value));
          } else {
            throw new IllegalStateException("Don't know how to convert: " + type);
          }
        }
      }

      Node node = nodeMetaModel.construct(parameters);
      // COMMENT is in the propertyKey meta model, but not required as constructor parameter.
      // Set it after construction
      if (parameters.containsKey(XMLNode.COMMENT.propertyKey)) {
        node.setComment((Comment) parameters.get(XMLNode.COMMENT.propertyKey));
      }

      for (String name : deferredElements.keySet()) {
        if (!readNonMetaProperties(name, deferredElements.get(name), node)) {
          throw new IllegalStateException(
              "Unknown propertyKey: " + nodeMetaModel.getQualifiedClassName() + "." + name);
        }
      }
      setSymbolResolverIfCompilationUnit(node);

      return node;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private NodeList deserializeNodeList(Element nodeListElements) {
    var childNodes = nodeListElements.getChildNodes();
    var deserializedNodes = new NodeList<>();

    // Loop through each child node.
    for (int i = 0; i < childNodes.getLength(); i++) {
      // Get this child node.
      var childNode = childNodes.item(i);

      // Skip if not an element.
      if (childNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
        continue;
      }

      // Get the node as an element.
      deserializedNodes.add(deserialize((Element) childNode));
    }

    // Return the deserialized nodes.
    return deserializedNodes;
  }

  /**
   * Reads properties from json not included in meta model (i.e., RANGE and TOKEN_RANGE). When read,
   * it sets the deserialized value to the node instance.
   *
   * @param name propertyKey name for json value
   * @param element json value that needs to be deserialized for this propertyKey
   * @param node instance to which the deserialized value will be set to
   * @return true if propertyKey is read from json and set to Node instance
   */
  protected boolean readNonMetaProperties(String name, Element element, Node node) {
    return readRange(name, element, node) || readTokenRange(name, element, node);
  }

  protected boolean readRange(String name, Element element, Node node) {
    // Exit if not a range property.
    if (!name.equals(XMLNode.RANGE.propertyKey)) {
      return false;
    }

    // Get the range node.
    var rangeNode = element.getElementsByTagName(XMLNode.RANGE.propertyKey).item(0);

    // Exit if the range node is not an element.
    if (rangeNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
      return false;
    }
    var rangeElement = (Element) rangeNode;

    // Parse the range.
    Position begin =
        new Position(
            Integer.parseInt(
                rangeElement
                    .getElementsByTagName(XMLRange.BEGIN_LINE.propertyKey)
                    .item(0)
                    .getTextContent()),
            Integer.parseInt(
                rangeElement
                    .getElementsByTagName(XMLRange.BEGIN_COLUMN.propertyKey)
                    .item(0)
                    .getTextContent()));
    Position end =
        new Position(
            Integer.parseInt(
                rangeElement
                    .getElementsByTagName(XMLRange.END_LINE.propertyKey)
                    .item(0)
                    .getTextContent()),
            Integer.parseInt(
                rangeElement
                    .getElementsByTagName(XMLRange.END_COLUMN.propertyKey)
                    .item(0)
                    .getTextContent()));

    node.setRange(new Range(begin, end));
    return true;
  }

  protected boolean readTokenRange(String name, Element element, Node node) {
    // Skip if not a token range property.
    if (!name.equals(XMLNode.TOKEN_RANGE.propertyKey)) {
      return false;
    }

    // Get token range node.
    var tokenRangeNode = element.getElementsByTagName(XMLNode.TOKEN_RANGE.propertyKey).item(0);

    // Exit if token range node is not an element.
    if (tokenRangeNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
      return false;
    }
    var tokenRangeElement = (Element) tokenRangeNode;

    // Parse the token range.
    JavaToken begin = readToken(XMLTokenRange.BEGIN_TOKEN.propertyKey, tokenRangeElement);
    JavaToken end = readToken(XMLTokenRange.END_TOKEN.propertyKey, tokenRangeElement);

    node.setTokenRange(new TokenRange(begin, end));
    return true;
  }

  protected JavaToken readToken(String name, Element element) {
    var tokenNode = element.getElementsByTagName(name).item(0);

    // Exit if token node is not an element.
    if (tokenNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
      return null;
    }
    var tokenElement = (Element) tokenNode;

    return new JavaToken(
        Integer.parseInt(
            tokenElement.getElementsByTagName(XMLToken.KIND.propertyKey).item(0).getTextContent()),
        tokenElement.getElementsByTagName(XMLToken.TEXT.propertyKey).item(0).getTextContent());
  }

  /**
   * This method sets symbol resolver to Node if it is an instance of CompilationUnit and a
   * SymbolResolver is configured in the static configuration. This is necessary to be able to
   * resolve symbols within the cu after deserialization. Normally, when parsing java with
   * JavaParser, the symbol resolver is injected to the cu as a data element with key
   * SYMBOL_RESOLVER_KEY.
   *
   * @param node instance to which symbol resolver will be set to when instance of a Compilation
   *     Unit
   * @see com.github.javaparser.ast.Node#SYMBOL_RESOLVER_KEY
   * @see com.github.javaparser.ParserConfiguration#ParserConfiguration()
   */
  private void setSymbolResolverIfCompilationUnit(Node node) {
    if (node instanceof CompilationUnit
        && StaticJavaParser.getConfiguration().getSymbolResolver().isPresent()) {
      CompilationUnit cu = (CompilationUnit) node;
      cu.setData(
          Node.SYMBOL_RESOLVER_KEY, StaticJavaParser.getConfiguration().getSymbolResolver().get());
    }
  }

  // endregion
}
