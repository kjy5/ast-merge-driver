package org.kjy5;

import com.github.javaparser.ast.Node;
import com.github.javaparser.metamodel.JavaParserMetaModel;

public class JavaParserToXMLSerializer {
  public void serialize(Node node, XMLDocument xmlDocument) {
    var nodeMetaModel = JavaParserMetaModel.getNodeMetaModel(node.getClass());
    
  }
}
