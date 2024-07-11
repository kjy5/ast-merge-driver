package org.kjy5;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) {
    // region File path specifications
    var folder = args.length > 0 ? args[0] : "0";

    // Constants for the file paths.
    var assetsPath = "assets/";
    var fileBaseName = "/file_base";
    var fileLeftName = "/file_left";
    var fileRightName = "/file_right";
    var fileMergeName = "/file_merged";
    var javaExtension = ".java";
    var xmlExtension = ".xml";

    // Source files.
    var fileBasePath = assetsPath + folder + fileBaseName + javaExtension;
    var fileLeftPath = assetsPath + folder + fileLeftName + javaExtension;
    var fileRightPath = assetsPath + folder + fileRightName + javaExtension;
    var fileMergedPath = assetsPath + folder + fileMergeName + javaExtension;

    // XML output files.
    var fileBaseXmlPath = assetsPath + folder + fileBaseName + xmlExtension;
    var fileLeftXmlPath = assetsPath + folder + fileLeftName + xmlExtension;
    var fileRightXmlPath = assetsPath + folder + fileRightName + xmlExtension;
    var fileMergedXmlPath = assetsPath + folder + fileMergeName + xmlExtension;
    // endregion

    // region Parse source files
    CompilationUnit fileBaseParsing, fileLeftParsing, fileRightParsing;
    try {
      fileBaseParsing = StaticJavaParser.parse(Paths.get(fileBasePath));
      fileLeftParsing = StaticJavaParser.parse(Paths.get(fileLeftPath));
      fileRightParsing = StaticJavaParser.parse(Paths.get(fileRightPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // endregion

    // region Serialize parsed files to XML
    var fileBaseXmlSerializer = new JavaParserToXML(fileBaseParsing);
    fileBaseXmlSerializer.getXmlDocument().writeToFile(fileBaseXmlPath);

    var fileLeftXmlSerializer = new JavaParserToXML(fileLeftParsing);
    fileLeftXmlSerializer.getXmlDocument().writeToFile(fileLeftXmlPath);

    var fileRightXmlSerializer = new JavaParserToXML(fileRightParsing);
    fileRightXmlSerializer.getXmlDocument().writeToFile(fileRightXmlPath);
    // endregion

    // region Deserialize XML files to Java
    if (!Files.exists(Paths.get(fileMergedXmlPath))) {
      return;
    }
    XMLDocument mergedXMLDocument = new XMLDocument(fileMergedXmlPath);
    var fileMergeXmlDeserializer = new XMLToJavaParser(mergedXMLDocument);
    var fileMergedNode = fileMergeXmlDeserializer.getAstRoot();

    // Setup LexicalPreservingPrinter
    LexicalPreservingPrinter.setup(fileMergedNode);
    
    // Write the deserialized node to a file.
    try {
      Files.write(
          Paths.get(fileMergedPath), fileMergedNode.toString().getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // endregion
  }
}
