package org.kjy5;

import com.github.javaparser.metamodel.JavaParserMetaModel;

import static com.github.javaparser.utils.Utils.decapitalize;

public class Common {
  /** excludes properties from meta model (except comment) */
  public enum XMLNode {
    RANGE("range"),
    TOKEN_RANGE("tokenRange"),
    COMMENT(decapitalize(JavaParserMetaModel.commentMetaModel.getTypeName())),
    CLASS("Class");
    final String propertyKey;

    XMLNode(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLRange {
    BEGIN_LINE("beginLine"),
    BEGIN_COLUMN("beginColumn"),
    END_LINE("endLine"),
    END_COLUMN("endColumn");
    final String propertyKey;

    XMLRange(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLTokenRange {
    BEGIN_TOKEN("beginToken"),
    END_TOKEN("endToken");
    final String propertyKey;

    XMLTokenRange(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }

  public enum XMLToken {
    TEXT("text"),
    KIND("kind");
    final String propertyKey;

    XMLToken(String p) {
      this.propertyKey = p;
    }

    public String toString() {
      return this.propertyKey;
    }
  }
}
