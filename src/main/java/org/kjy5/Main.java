package org.kjy5;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
  public static void main() {
    // region File path specifications
    // Source files.
    var fileBasePath = "assets/file_base.java";
    var fileLeftPath = "assets/file_left.java";
    var fileRightPath = "assets/file_right.java";

    // XML output files.
    var fileBaseXmlPath = "assets/file_base.xml";
    var fileLeftXmlPath = "assets/file_left.xml";
    var fileRightXmlPath = "assets/file_right.xml";
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
    var fileBaseXmlSerializer = new JavaParserToXMLSerializer(fileBaseParsing);
    fileBaseXmlSerializer.getXmlDocument().writeToFile(fileBaseXmlPath);
    // endregion
  }
}
