package org.kjy5;

import static com.github.javaparser.ast.NodeList.toNodeList;
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
      String serializedNodeType = element.getAttribute(XMLNode.CLASS.propertyKey);
      BaseNodeMetaModel nodeMetaModel =
          getNodeMetaModel(Class.forName(serializedNodeType))
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Trying to deserialize an unknown node type: " + serializedNodeType));
      Map<String, Object> parameters = new HashMap<>();
      Map<String, Element> deferredElements = new HashMap<>();
      
      // Handle attributes.
      var attributes = element.getAttributes();
      for(int i = 0; i < attributes.getLength(); i++) {
        var currentAttribute = attributes.item(i);
      }

      for (String name : element.keySet()) {
        if (name.equals(XMLNode.CLASS.propertyKey)) {
          continue;
        }

        Optional<PropertyMetaModel> optionalPropertyMetaModel =
            nodeMetaModel.getAllPropertyMetaModels().stream()
                .filter(mm -> mm.getName().equals(name))
                .findFirst();
        if (!optionalPropertyMetaModel.isPresent()) {
          deferredElements.put(name, element.get(name));
          continue;
        }

        PropertyMetaModel propertyMetaModel = optionalPropertyMetaModel.get();
        if (propertyMetaModel.isNodeList()) {
          JsonArray nodeListJson = element.getJsonArray(name);
          parameters.put(name, deserializeNodeList(nodeListJson));
        } else if (propertyMetaModel.isNode()) {
          parameters.put(name, deserialize(element.getJsonObject(name)));
        } else {
          Class<?> type = propertyMetaModel.getType();
          if (type == String.class) {
            parameters.put(name, element.getString(name));
          } else if (type == boolean.class) {
            parameters.put(name, Boolean.parseBoolean(element.getString(name)));
          } else if (Enum.class.isAssignableFrom(type)) {
            parameters.put(
                name, Enum.valueOf((Class<? extends Enum>) type, element.getString(name)));
          } else {
            throw new IllegalStateException("Don't know how to convert: " + type);
          }
        }
      }

      Node node = nodeMetaModel.construct(parameters);
      // COMMENT is in the propertyKey meta model, but not required as constructor parameter.
      // Set it after construction
      if (parameters.containsKey(JsonNode.COMMENT.propertyKey)) {
        node.setComment((Comment) parameters.get(JsonNode.COMMENT.propertyKey));
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

  private NodeList<?> deserializeNodeList(JsonArray nodeListJson) {
    return nodeListJson.stream()
        .map(nodeJson -> deserialize((JsonObject) nodeJson))
        .collect(toNodeList());
  }

  /**
   * Reads properties from json not included in meta model (i.e., RANGE and TOKEN_RANGE). When read,
   * it sets the deserialized value to the node instance.
   *
   * @param name propertyKey name for json value
   * @param jsonValue json value that needs to be deserialized for this propertyKey
   * @param node instance to which the deserialized value will be set to
   * @return true if propertyKey is read from json and set to Node instance
   */
  protected boolean readNonMetaProperties(String name, JsonValue jsonValue, Node node) {
    return readRange(name, jsonValue, node) || readTokenRange(name, jsonValue, node);
  }

  protected boolean readRange(String name, JsonValue jsonValue, Node node) {
    if (name.equals(JsonNode.RANGE.propertyKey)) {
      JsonObject jsonObject = (JsonObject) jsonValue;
      Position begin =
          new Position(
              jsonObject.getInt(JsonRange.BEGIN_LINE.propertyKey),
              jsonObject.getInt(JsonRange.BEGIN_COLUMN.propertyKey));
      Position end =
          new Position(
              jsonObject.getInt(JsonRange.END_LINE.propertyKey),
              jsonObject.getInt(JsonRange.END_COLUMN.propertyKey));
      node.setRange(new Range(begin, end));
      return true;
    }
    return false;
  }

  protected boolean readTokenRange(String name, JsonValue jsonValue, Node node) {
    if (name.equals(JsonNode.TOKEN_RANGE.propertyKey)) {
      JsonObject jsonObject = (JsonObject) jsonValue;
      JavaToken begin = readToken(JsonTokenRange.BEGIN_TOKEN.propertyKey, jsonObject);
      JavaToken end = readToken(JsonTokenRange.END_TOKEN.propertyKey, jsonObject);
      node.setTokenRange(new TokenRange(begin, end));
      return true;
    }
    return false;
  }

  protected JavaToken readToken(String name, JsonObject jsonObject) {
    JsonObject tokenJson = jsonObject.getJsonObject(name);
    return new JavaToken(
        tokenJson.getInt(JsonToken.KIND.propertyKey),
        tokenJson.getString(JsonToken.TEXT.propertyKey));
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
