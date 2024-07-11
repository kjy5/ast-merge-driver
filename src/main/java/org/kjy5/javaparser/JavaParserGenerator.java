/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright (c) 2024 Kenneth Yang (kjy5@uw.edu)
 */

package org.kjy5.javaparser;

import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.SyntaxException;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.TreeContext;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.Reader;

/**
 * A generator for JavaParser trees.
 *
 * @author Jean-Rémy Falleri
 * @author Kenneth Yang
 */
@Register(id = "java-javaparser", accept = "\\.java$")
public class JavaParserGenerator extends TreeGenerator {

  @Override
  public TreeContext generate(Reader r) {
    LineReader lr = new LineReader(r);
    try {
      CompilationUnit cu = StaticJavaParser.parse(lr);
      JavaParserVisitor v = new JavaParserVisitor(lr);
      v.visitPreOrder(cu);
      return v.getTreeContext();
    } catch (ParseProblemException e) {
      throw new SyntaxException(this, r, e);
    }
  }
}
