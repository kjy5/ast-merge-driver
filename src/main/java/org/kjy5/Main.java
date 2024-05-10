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
    // region File path specifications.
    // Source files.
    var file0Path = "assets/file0.java";
    var file1Path = "assets/file1.java";
    var file2Path = "assets/file2.java";

    // XML output files.
    var file0XmlPath = "assets/file0.xml";
    var file1XmlPath = "assets/file1.xml";
    var file2XmlPath = "assets/file2.xml";

    // endregion

    // region Parse java source code to JCTree.
    
    // Parse file0 with Javac-parse.
    JCTree.JCCompilationUnit file0JavacTree;
    try {
      file0JavacTree = JavacParse.parseJavaFile(file0Path);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file " + e);
    }
    
    // Exit if parsing failed.
    if (file0JavacTree == null) {
      return;
    }

    // Parse file1 with Javac-parse.
    JCTree.JCCompilationUnit file1JavacTree;
    try {
      file1JavacTree = JavacParse.parseJavaFile(file1Path);
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
      file2JavacTree = JavacParse.parseJavaFile(file2Path);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file " + e);
    }

    // Exit if parsing failed.
    if (file2JavacTree == null) {
      return;
    }

    // endregion

    // region Process with JCTree to Gumtree Tree Visitor.

    var file1Tree = file1JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);
    var file2Tree = file2JavacTree.accept(new JCTreeToGumtreeTreeVisitor(), null);

    // Compute mappings.
    Run.initMatchers();
    var defaultMatcher = Matchers.getInstance().getMatcher();
    var mappings = defaultMatcher.match(file1Tree, file2Tree);
    System.out.println(mappings);

    // endregion

    // region Process with JCTree to XML Visitor.

    // Setup document builder.
    var factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Error creating document builder " + e);
    }

    // Create DOM documents.
    var document0 = builder.newDocument();
    var document1 = builder.newDocument();
    var document2 = builder.newDocument();

    // Process with JCTree to XML Visitor.
    var file0Xml = file0JavacTree.accept(new JCTreeToXMLVisitor(), document0);
    document0.appendChild(file0Xml);

    var file1Xml = file1JavacTree.accept(new JCTreeToXMLVisitor(), document1);
    document1.appendChild(file1Xml);

    var file2Xml = file2JavacTree.accept(new JCTreeToXMLVisitor(), document2);
    document2.appendChild(file2Xml);

    // Write to XML files.
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;
    try {
      transformer = transformerFactory.newTransformer();
    } catch (Exception e) {
      throw new RuntimeException("Error creating transformer " + e);
    }
    DOMSource source0 = new DOMSource(document0);
    DOMSource source1 = new DOMSource(document1);
    DOMSource source2 = new DOMSource(document2);

    // Write to file0.xml.
    var result0 = new StreamResult(file0XmlPath);
    try {
      transformer.transform(source0, result0);
    } catch (TransformerException e) {
      throw new RuntimeException("Error transforming DOM 0 to XML " + e);
    }

    // Write to file1.xml.
    var result1 = new StreamResult(file1XmlPath);
    try {
      transformer.transform(source1, result1);
    } catch (TransformerException e) {
      throw new RuntimeException("Error transforming DOM 1 to XML " + e);
    }

    // Write to file2.xml.
    var result2 = new StreamResult(file2XmlPath);
    try {
      transformer.transform(source2, result2);
    } catch (TransformerException e) {
      throw new RuntimeException("Error transforming DOM 2 to XML " + e);
    }

    System.out.println(
        "XML files written to " + file0XmlPath + ", " + file1XmlPath + ", and " + file2XmlPath);

    // endregion
  }
}
