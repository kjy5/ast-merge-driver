package org.kjy5;

import java.io.IOException;
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
import org.xml.sax.SAXException;

public class XMLDocument {
  // region Fields
  private final Document document;

  // Output fields.
  private final Transformer transformer;
  private final DOMSource domSource;

  // endregion

  // region Constructors
  public XMLDocument() {
    // Setup writer.
    var transformerFactory = TransformerFactory.newInstance();
    try {
      transformer = transformerFactory.newTransformer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Create a new document.
    document = makeDocumentBuilder().newDocument();

    // Create a new DOM source.
    domSource = new DOMSource(document);
  }

  /**
   * Create a new XML document from a source XML.
   *
   * @param sourcePath The path to the source XML.
   */
  public XMLDocument(String sourcePath) {
    try {
      document = makeDocumentBuilder().parse(sourcePath);
    } catch (SAXException | IOException e) {
      throw new RuntimeException(e);
    }
    transformer = null;
    domSource = null;
  }

  private DocumentBuilder makeDocumentBuilder() {
    var documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      return documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  // endregion

  // region Document manipulation methods

  /**
   * Get the root element of the document.
   *
   * @return The root element.
   */
  public Element getRootElement() {
    return document.getDocumentElement();
  }

  /**
   * Add a root element to the document.
   *
   * @param rootElement The root element to add.
   */
  public void setRootElement(Element rootElement) {
    document.appendChild(rootElement);
  }

  /**
   * Create a blank element with the name of the node or "node" if one is not provided.
   *
   * @param nodeName The name of the node.
   * @return The created element.
   */
  public Element createElement(String nodeName) {
    return document.createElement(Optional.ofNullable(nodeName).orElse("node"));
  }

  /**
   * Create a child element with the name of the node or "Node" if one is not provided.
   *
   * @param nodeName The name of the node.
   * @param parentElement The parent element to add the child to.
   * @return The created element.
   */
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
