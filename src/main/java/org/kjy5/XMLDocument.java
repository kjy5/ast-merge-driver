package org.kjy5;

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
    this.document = documentBuilder.newDocument();
  }
  // endregion
  
  // region Public methods
  public void appendElement(Element element) {
    this.document.appendChild(element);
  }
  // endregion
}
