package org.kjy5;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.serialization.JavaParserJsonSerializer;
import jakarta.json.Json;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

public class Main {
  public static void main() {
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

    // Parse the source files.
    var jsonSerializer = new JavaParserJsonSerializer();
    var stringWriter = new StringWriter();
    var jsonGeneratorFactory = Json.createGeneratorFactory(null);
    var jsonGenerator = jsonGeneratorFactory.createGenerator(stringWriter);
    try {
      var file0Parse = StaticJavaParser.parse(Paths.get(file0Path));
      jsonSerializer.serialize(file0Parse, jsonGenerator);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    jsonGenerator.close();
    var file0Json = stringWriter.toString();
    System.out.println(file0Json);
  }
}
