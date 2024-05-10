package org.kjy5;

import com.sun.source.tree.*;
import javax.lang.model.element.Name;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JCTreeToXMLVisitor implements TreeVisitor<Element, Document> {
  // region Static Helper Methods.
  private static Element createElement(com.sun.source.tree.Tree node, Document document) {
    // Return default tree.
    return document.createElement(node.getClass().getSimpleName());
  }

  private static Element createElement(
      com.sun.source.tree.Tree node, String label, Document document) {
    // Create default tree.
    var tree = createElement(node, document);

    // Set label.
    tree.setAttribute("label", label);

    // Return tree.
    return tree;
  }

  private static Element createElement(
      com.sun.source.tree.Tree node, Name label, Document document) {
    return createElement(node, label.toString(), document);
  }

  // endregion

  // region TreeVisitor Overrides.
  @Override
  public Element visitAnnotatedType(AnnotatedTypeTree node, Document document) {
    return null;
  }

  @Override
  public Element visitAnnotation(AnnotationTree node, Document document) {
    return null;
  }

  @Override
  public Element visitMethodInvocation(MethodInvocationTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add type arguments.
    if (node.getTypeArguments() != null)
      for (var typeArgument : node.getTypeArguments())
        element.appendChild(typeArgument.accept(this, document));

    // Add method select.
    if (node.getMethodSelect() != null)
      element.appendChild(node.getMethodSelect().accept(this, document));

    // Add arguments.
    if (node.getArguments() != null)
      for (var argument : node.getArguments()) element.appendChild(argument.accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitAssert(AssertTree node, Document document) {
    System.out.println("visit Assert");
    return null;
  }

  @Override
  public Element visitAssignment(AssignmentTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add variable.
    if (node.getVariable() != null) element.appendChild(node.getVariable().accept(this, document));

    // Add expression.
    if (node.getExpression() != null)
      element.appendChild(node.getExpression().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitCompoundAssignment(CompoundAssignmentTree node, Document document) {
    System.out.println("visit Compound Assignment");
    return null;
  }

  @Override
  public Element visitBinary(BinaryTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add left operand.
    if (node.getLeftOperand() != null)
      element.appendChild(node.getLeftOperand().accept(this, document));

    // Add right operand.
    if (node.getRightOperand() != null)
      element.appendChild(node.getRightOperand().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitBlock(BlockTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add statements.
    if (node.getStatements() != null)
      for (var statement : node.getStatements())
        element.appendChild(statement.accept(this, document));

    // Add isStatic to element attributes.
    element.setAttribute("isStatic", String.valueOf(node.isStatic()));

    // Return Element.
    return element;
  }

  @Override
  public Element visitBreak(BreakTree node, Document document) {
    // Create and return element.
    if (node.getLabel() != null) return createElement(node, node.getLabel(), document);

    return createElement(node, document);
  }

  @Override
  public Element visitCase(CaseTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add expression.
    if (node.getExpressions() != null)
      for (var expression : node.getExpressions())
        element.appendChild(expression.accept(this, document));

    // Add labels.
    if (node.getLabels() != null)
      for (var label : node.getLabels()) element.appendChild(label.accept(this, document));

    // Add guard.
    if (node.getGuard() != null) element.appendChild(node.getGuard().accept(this, document));

    // Add statements/body.
    if (node.getCaseKind() == CaseTree.CaseKind.STATEMENT) {
      if (node.getStatements() != null) {
        for (var statement : node.getStatements())
          element.appendChild(statement.accept(this, document));
      }
    } else {
      if (node.getBody() != null) {
        element.appendChild(node.getBody().accept(this, document));
      }
    }

    // Return Element.
    return element;
  }

  @Override
  public Element visitCatch(CatchTree node, Document document) {
    System.out.println("visit Catch");
    return null;
  }

  @Override
  public Element visitClass(ClassTree node, Document document) {
    // Create Element.
    var element = createElement(node, node.getSimpleName(), document);

    // Add modifiers.
    if (node.getModifiers() != null)
      element.appendChild(node.getModifiers().accept(this, document));

    // Add type parameters.
    if (node.getTypeParameters() != null)
      for (var typeParameter : node.getTypeParameters())
        element.appendChild(typeParameter.accept(this, document));

    // Add extends clause.
    if (node.getExtendsClause() != null)
      element.appendChild(node.getExtendsClause().accept(this, document));

    // Add implements clause.
    if (node.getImplementsClause() != null)
      for (var implement : node.getImplementsClause())
        element.appendChild(implement.accept(this, document));

    // Add permits clause.
    if (node.getPermitsClause() != null)
      for (var permit : node.getPermitsClause()) element.appendChild(permit.accept(this, document));

    // Add members.
    if (node.getMembers() != null)
      for (var member : node.getMembers()) element.appendChild(member.accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitConditionalExpression(ConditionalExpressionTree node, Document document) {
    System.out.println("visit Conditional Expression");
    return null;
  }

  @Override
  public Element visitContinue(ContinueTree node, Document document) {
    System.out.println("visit Continue");
    return null;
  }

  @Override
  public Element visitDoWhileLoop(DoWhileLoopTree node, Document document) {
    System.out.println("visit Do While Loop");
    return null;
  }

  @Override
  public Element visitErroneous(ErroneousTree node, Document document) {
    System.out.println("visit Erroneous");
    return null;
  }

  @Override
  public Element visitExpressionStatement(ExpressionStatementTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add expression.
    if (node.getExpression() != null)
      element.appendChild(node.getExpression().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitEnhancedForLoop(EnhancedForLoopTree node, Document document) {
    System.out.println("visit Enhanced For Loop");
    return null;
  }

  @Override
  public Element visitForLoop(ForLoopTree node, Document document) {
    System.out.println("visit For Loop");
    return null;
  }

  @Override
  public Element visitIdentifier(IdentifierTree node, Document document) {
    // Create and return Element.
    return createElement(node, node.getName(), document);
  }

  @Override
  public Element visitIf(IfTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add condition.
    if (node.getCondition() != null)
      element.appendChild(node.getCondition().accept(this, document));

    // Add then statement.
    if (node.getThenStatement() != null)
      element.appendChild(node.getThenStatement().accept(this, document));

    // Add else statement.
    if (node.getElseStatement() != null)
      element.appendChild(node.getElseStatement().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitImport(ImportTree node, Document document) {
    System.out.println("visit Import");
    return null;
  }

  @Override
  public Element visitArrayAccess(ArrayAccessTree node, Document document) {
    System.out.println("visit Array Access");
    return null;
  }

  @Override
  public Element visitLabeledStatement(LabeledStatementTree node, Document document) {
    System.out.println("visit Labeled Statement");
    return null;
  }

  @Override
  public Element visitLiteral(LiteralTree node, Document document) {
    // Create and return Element.
    return createElement(node, node.getValue().toString(), document);
  }

  @SuppressWarnings("preview")
  @Override
  public Element visitStringTemplate(StringTemplateTree node, Document document) {
    System.out.println("visit String Template");
    return null;
  }

  @Override
  public Element visitAnyPattern(AnyPatternTree node, Document document) {
    System.out.println("visit Any Pattern");
    return null;
  }

  @Override
  public Element visitBindingPattern(BindingPatternTree node, Document document) {
    System.out.println("visit Binding Pattern");
    return null;
  }

  @Override
  public Element visitDefaultCaseLabel(DefaultCaseLabelTree node, Document document) {
    System.out.println("visit Default Case Label");
    return null;
  }

  @Override
  public Element visitConstantCaseLabel(ConstantCaseLabelTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add constant expression.
    if (node.getConstantExpression() != null)
      element.appendChild(node.getConstantExpression().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitPatternCaseLabel(PatternCaseLabelTree node, Document document) {
    System.out.println("visit Pattern Case Label");
    return null;
  }

  @Override
  public Element visitDeconstructionPattern(DeconstructionPatternTree node, Document document) {
    System.out.println("visit Deconstruction Pattern");
    return null;
  }

  @Override
  public Element visitMethod(MethodTree node, Document document) {
    // Create Element.
    var element = createElement(node, node.getName(), document);

    // Add modifiers.
    if (node.getModifiers() != null)
      element.appendChild(node.getModifiers().accept(this, document));

    // Add return type.
    if (node.getReturnType() != null)
      element.appendChild(node.getReturnType().accept(this, document));

    // Add type parameters.
    if (node.getTypeParameters() != null)
      for (var typeParameter : node.getTypeParameters())
        element.appendChild(typeParameter.accept(this, document));

    // Add parameters.
    if (node.getParameters() != null)
      for (var parameter : node.getParameters())
        element.appendChild(parameter.accept(this, document));

    // Add receiver parameter.
    if (node.getReceiverParameter() != null)
      element.appendChild(node.getReceiverParameter().accept(this, document));

    // Add throws.
    if (node.getThrows() != null)
      for (var thrown : node.getThrows()) element.appendChild(thrown.accept(this, document));

    // Add body.
    if (node.getBody() != null) element.appendChild(node.getBody().accept(this, document));

    // Add default value.
    if (node.getDefaultValue() != null)
      element.appendChild(node.getDefaultValue().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitModifiers(ModifiersTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add flags.
    if (node.getFlags() != null)
      for (var flag : node.getFlags()) {
        var modifier = document.createElement("Modifier");
        modifier.setAttribute("label", flag.toString());
        element.appendChild(modifier);
      }

    // Add annotations.
    if (node.getAnnotations() != null)
      for (var annotation : node.getAnnotations())
        element.appendChild(annotation.accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitNewArray(NewArrayTree node, Document document) {
    System.out.println("visit New Array");
    return null;
  }

  @Override
  public Element visitNewClass(NewClassTree node, Document document) {
    System.out.println("visit New Class");
    return null;
  }

  @Override
  public Element visitLambdaExpression(LambdaExpressionTree node, Document document) {
    System.out.println("visit Lambda Expression");
    return null;
  }

  @Override
  public Element visitPackage(PackageTree node, Document document) {
    System.out.println("visit Package");
    return null;
  }

  @Override
  public Element visitParenthesized(ParenthesizedTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add expression.
    if (node.getExpression() != null)
      element.appendChild(node.getExpression().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitReturn(ReturnTree node, Document document) {
    System.out.println("visit Return");
    return null;
  }

  @Override
  public Element visitMemberSelect(MemberSelectTree node, Document document) {
    // Create Element.
    var element = createElement(node, node.getIdentifier(), document);

    // Add expression.
    if (node.getExpression() != null)
      element.appendChild(node.getExpression().accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitMemberReference(MemberReferenceTree node, Document document) {
    System.out.println("visit Member Reference");
    return null;
  }

  @Override
  public Element visitEmptyStatement(EmptyStatementTree node, Document document) {
    System.out.println("visit Empty Statement");
    return null;
  }

  @Override
  public Element visitSwitch(SwitchTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add expression.
    if (node.getExpression() != null)
      element.appendChild(node.getExpression().accept(this, document));

    // Add cases.
    if (node.getCases() != null)
      for (var caseTree : node.getCases()) element.appendChild(caseTree.accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitSwitchExpression(SwitchExpressionTree node, Document document) {
    System.out.println("visit Switch Expression");
    return null;
  }

  @Override
  public Element visitSynchronized(SynchronizedTree node, Document document) {
    System.out.println("visit Synchronized");
    return null;
  }

  @Override
  public Element visitThrow(ThrowTree node, Document document) {
    System.out.println("visit Throw");
    return null;
  }

  @Override
  public Element visitCompilationUnit(CompilationUnitTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);

    // Add modules.
    if (node.getModule() != null) element.appendChild(node.getModule().accept(this, document));

    // Add package annotations.
    if (node.getPackageAnnotations() != null)
      for (var annotation : node.getPackageAnnotations())
        element.appendChild(annotation.accept(this, document));

    // Add package.
    if (node.getPackageName() != null)
      element.appendChild(node.getPackageName().accept(this, document));

    // Add imports.
    if (node.getImports() != null)
      for (var importTree : node.getImports())
        element.appendChild(importTree.accept(this, document));

    // Add type declarations.
    if (node.getTypeDecls() != null)
      for (var typeDecl : node.getTypeDecls()) element.appendChild(typeDecl.accept(this, document));

    // Return Element.
    return element;
  }

  @Override
  public Element visitTry(TryTree node, Document document) {
    System.out.println("visit Try");
    return null;
  }

  @Override
  public Element visitParameterizedType(ParameterizedTypeTree node, Document document) {
    System.out.println("visit Parameterized Type");
    return null;
  }

  @Override
  public Element visitUnionType(UnionTypeTree node, Document document) {
    System.out.println("visit Union Type");
    return null;
  }

  @Override
  public Element visitIntersectionType(IntersectionTypeTree node, Document document) {
    System.out.println("visit Intersection Type");
    return null;
  }

  @Override
  public Element visitArrayType(ArrayTypeTree node, Document document) {
    // Create Element.
    var element = createElement(node, document);
    
    // Add type.
    if (node.getType() != null)
      element.appendChild(node.getType().accept(this, document));
    
    // Return Element.
    return element;
  }

  @Override
  public Element visitTypeCast(TypeCastTree node, Document document) {
    System.out.println("visit Type Cast");
    return null;
  }

  @Override
  public Element visitPrimitiveType(PrimitiveTypeTree node, Document document) {
    // Create and return Element.
    return createElement(node, node.getPrimitiveTypeKind().toString(), document);
  }

  @Override
  public Element visitTypeParameter(TypeParameterTree node, Document document) {
    System.out.println("visit Type Parameter");
    return null;
  }

  @Override
  public Element visitInstanceOf(InstanceOfTree node, Document document) {
    System.out.println("visit Instance Of");
    return null;
  }

  @Override
  public Element visitUnary(UnaryTree node, Document document) {
    System.out.println("visit Unary");
    return null;
  }

  @Override
  public Element visitVariable(VariableTree node, Document document) {
    // Create Element.
    var element = createElement(node, node.getName(), document);
    
    // Add modifiers.
    if (node.getModifiers() != null)
      element.appendChild(node.getModifiers().accept(this, document));
    
    // Add name expression.
    if (node.getNameExpression() != null)
      element.appendChild(node.getNameExpression().accept(this, document));
    
    // Add type.
    if (node.getType() != null)
      element.appendChild(node.getType().accept(this, document));
    
    // Add initializer.
    if (node.getInitializer() != null)
      element.appendChild(node.getInitializer().accept(this, document));
    
    // Return Element.
    return element;
  }

  @Override
  public Element visitWhileLoop(WhileLoopTree node, Document document) {
    System.out.println("visit While Loop");
    return null;
  }

  @Override
  public Element visitWildcard(WildcardTree node, Document document) {
    System.out.println("visit Wildcard");
    return null;
  }

  @Override
  public Element visitModule(ModuleTree node, Document document) {
    System.out.println("visit Module");
    return null;
  }

  @Override
  public Element visitExports(ExportsTree node, Document document) {
    System.out.println("visit Exports");
    return null;
  }

  @Override
  public Element visitOpens(OpensTree node, Document document) {
    System.out.println("visit Opens");
    return null;
  }

  @Override
  public Element visitProvides(ProvidesTree node, Document document) {
    System.out.println("visit Provides");
    return null;
  }

  @Override
  public Element visitRequires(RequiresTree node, Document document) {
    System.out.println("visit Requires");
    return null;
  }

  @Override
  public Element visitUses(UsesTree node, Document document) {
    System.out.println("visit Uses");
    return null;
  }

  @Override
  public Element visitOther(Tree node, Document document) {
    System.out.println("visit Other");
    return null;
  }

  @Override
  public Element visitYield(YieldTree node, Document document) {
    System.out.println("visit Yield");
    return null;
  }
  // endregion
}
