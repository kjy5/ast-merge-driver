package org.kjy5;

import com.github.javaparser.ast.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLDocument {
  // region Fields
  private final Document document;

  // endregion

  // region Constructors
  public XMLDocument() {
    // Setup to create a new document.
    var documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder;
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }

    // Create a new document.
    document = documentBuilder.newDocument();
  }

  // endregion

  // region Document manipulation methods

  /**
   * Appends an element to the document.
   *
   * @param element The element to append.
   * @return The appended element.
   */
  public Element appendElement(Element element) {
    return (Element) document.appendChild(element);
  }

  /**
   * Create a blank element with the name of the node.
   *
   * @param node The node to create an element for.
   * @return The created element.
   */
  public Element createElement(Node node) {
    return appendElement(document.createElement(node.getClass().getSimpleName()));
  }
  // endregion
}
