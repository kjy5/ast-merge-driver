package org.kjy5;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.Matchers;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.plumelib.javacparse.JavacParse;

@SuppressWarnings("StringTemplateMigration")
public class Main {
  public static void main(String[] args) {
    // Source files.
    var file1 = "assets/file1.java";
    var file2 = "assets/file2.java";

    // Parse file1 with Javac-parse.
    JCTree.JCCompilationUnit file1JavacTree;
    try {
      file1JavacTree = JavacParse.parseJavaFile(file1);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file " + e);
    }

    // Exit if parsing failed.
    if (file1JavacTree == null) {
      return;
    }

    // Parse file2 with Javac-parse.
    JCTree.JCCompilationUnit file2JavacTree;
    try {
      file2JavacTree = JavacParse.parseJavaFile(file2);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file " + e);
    }

    // Exit if parsing failed.
    if (file2JavacTree == null) {
      return;
    }

    // Process with JCTree to Gumtree Tree Visitor.
    var file1Tree = file1JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);
    var file2Tree = file2JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);

    // Compute mappings.
    Run.initMatchers();
    var defaultMatcher = Matchers.getInstance().getMatcher();
    var mappings = defaultMatcher.match(file1Tree, file2Tree);
    System.out.println(mappings);

    // Setup document builder.
    var factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Error creating document builder " + e);
    }

    // Create DOM documents.
    var document1 = builder.newDocument();
    var document2 = builder.newDocument();

    // Process with JCTree to XML Visitor.
    var file1Xml = file1JavacTree.accept(new JCTreeToXMLVisitor(), document1);
    document1.appendChild(file1Xml);

    var file2Xml = file2JavacTree.accept(new JCTreeToXMLVisitor(), document2);
    document2.appendChild(file2Xml);

//    // Write to XML files.
//    TransformerFactory transformerFactory = TransformerFactory.newInstance();
//    Transformer transformer;
//    try {
//      transformer = transformerFactory.newTransformer();
//    } catch (Exception e) {
//      throw new RuntimeException("Error creating transformer " + e);
//    }
//    DOMSource source1 = new DOMSource(document1);
//    DOMSource source2 = new DOMSource(document2);
//
//    // Write to file1.xml.
//    var file1XmlPath = "assets/file1.xml";
//    var result1 = new StreamResult(file1XmlPath);
//    try {
//      transformer.transform(source1, result1);
//    } catch (TransformerException e) {
//      throw new RuntimeException("Error transforming DOM 1 to XML " + e);
//    }
//
//    // Write to file2.xml.
//    var file2XmlPath = "assets/file2.xml";
//    var result2 = new StreamResult(file2XmlPath);
//    try {
//      transformer.transform(source2, result2);
//    } catch (TransformerException e) {
//      throw new RuntimeException("Error transforming DOM 2 to XML " + e);
//    }
//
//    System.out.println("XML files written to " + file1XmlPath + " and " + file2XmlPath);
  }
}
