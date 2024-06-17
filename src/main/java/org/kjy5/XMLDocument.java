package org.kjy5;

import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLDocument {
  // region Fields
  private final Document document;

  // Output fields.
  private final Transformer transformer;
  private final DOMSource domSource;

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

    // Setup writer.
    var transformerFactory = TransformerFactory.newInstance();
    try {
      transformer = transformerFactory.newTransformer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Create a new document.
    document = documentBuilder.newDocument();

    // Create a new DOM source.
    domSource = new DOMSource(document);
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
   * @param nodeName The name of the node.
   * @return The created element.
   */
  public Element createElement(String nodeName) {
    return appendElement(document.createElement(Optional.ofNullable(nodeName).orElse("Node")));
  }

  public Element createChildElement(String nodeName, Element parentElement) {
    return (Element) parentElement.appendChild(createElement(nodeName));
  }

  // endregion

  // region File output methods

  /**
   * Writes the document to a file.
   *
   * @param filePath The path to the file to write to.
   */
  public void writeToFile(String filePath) {
    try {
      transformer.transform(domSource, new StreamResult(filePath));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  // endregion
}
