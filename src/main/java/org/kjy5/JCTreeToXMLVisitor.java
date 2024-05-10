package org.kjy5;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.TypeSet;
import com.sun.source.tree.*;
import com.sun.tools.javac.tree.JCTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.lang.model.element.Name;

public class JCTreeToXMLVisitor implements TreeVisitor<Element, Document> {
    // region Static Helper Methods.
    private static Element createTree(com.sun.source.tree.Tree node, Document document) {
        // Return default tree.
        return document.createElement(node.getClass().getSimpleName());
    }

    private static Element createTree(com.sun.source.tree.Tree node, String label, Document document) {
        // Create default tree.
        var tree = createTree(node, document);

        // Set label.
        tree.setAttribute("label", label);

        // Return tree.
        return tree;
    }

    private static Element createTree(com.sun.source.tree.Tree node, Name label, Document document) {
        return createTree(node, label.toString(), document);
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
        return null;
    }

    @Override
    public Element visitAssert(AssertTree node, Document document) {
        return null;
    }

    @Override
    public Element visitAssignment(AssignmentTree node, Document document) {
        return null;
    }

    @Override
    public Element visitCompoundAssignment(CompoundAssignmentTree node, Document document) {
        return null;
    }

    @Override
    public Element visitBinary(BinaryTree node, Document document) {
        return null;
    }

    @Override
    public Element visitBlock(BlockTree node, Document document) {
        return null;
    }

    @Override
    public Element visitBreak(BreakTree node, Document document) {
        return null;
    }

    @Override
    public Element visitCase(CaseTree node, Document document) {
        return null;
    }

    @Override
    public Element visitCatch(CatchTree node, Document document) {
        return null;
    }

    @Override
    public Element visitClass(ClassTree node, Document document) {
        return null;
    }

    @Override
    public Element visitConditionalExpression(ConditionalExpressionTree node, Document document) {
        return null;
    }

    @Override
    public Element visitContinue(ContinueTree node, Document document) {
        return null;
    }

    @Override
    public Element visitDoWhileLoop(DoWhileLoopTree node, Document document) {
        return null;
    }

    @Override
    public Element visitErroneous(ErroneousTree node, Document document) {
        return null;
    }

    @Override
    public Element visitExpressionStatement(ExpressionStatementTree node, Document document) {
        return null;
    }

    @Override
    public Element visitEnhancedForLoop(EnhancedForLoopTree node, Document document) {
        return null;
    }

    @Override
    public Element visitForLoop(ForLoopTree node, Document document) {
        return null;
    }

    @Override
    public Element visitIdentifier(IdentifierTree node, Document document) {
        return null;
    }

    @Override
    public Element visitIf(IfTree node, Document document) {
        return null;
    }

    @Override
    public Element visitImport(ImportTree node, Document document) {
        return null;
    }

    @Override
    public Element visitArrayAccess(ArrayAccessTree node, Document document) {
        return null;
    }

    @Override
    public Element visitLabeledStatement(LabeledStatementTree node, Document document) {
        return null;
    }

    @Override
    public Element visitLiteral(LiteralTree node, Document document) {
        return null;
    }

    @Override
    public Element visitStringTemplate(StringTemplateTree node, Document document) {
        return null;
    }

    @Override
    public Element visitAnyPattern(AnyPatternTree node, Document document) {
        return null;
    }

    @Override
    public Element visitBindingPattern(BindingPatternTree node, Document document) {
        return null;
    }

    @Override
    public Element visitDefaultCaseLabel(DefaultCaseLabelTree node, Document document) {
        return null;
    }

    @Override
    public Element visitConstantCaseLabel(ConstantCaseLabelTree node, Document document) {
        return null;
    }

    @Override
    public Element visitPatternCaseLabel(PatternCaseLabelTree node, Document document) {
        return null;
    }

    @Override
    public Element visitDeconstructionPattern(DeconstructionPatternTree node, Document document) {
        return null;
    }

    @Override
    public Element visitMethod(MethodTree node, Document document) {
        return null;
    }

    @Override
    public Element visitModifiers(ModifiersTree node, Document document) {
        return null;
    }

    @Override
    public Element visitNewArray(NewArrayTree node, Document document) {
        return null;
    }

    @Override
    public Element visitNewClass(NewClassTree node, Document document) {
        return null;
    }

    @Override
    public Element visitLambdaExpression(LambdaExpressionTree node, Document document) {
        return null;
    }

    @Override
    public Element visitPackage(PackageTree node, Document document) {
        return null;
    }

    @Override
    public Element visitParenthesized(ParenthesizedTree node, Document document) {
        return null;
    }

    @Override
    public Element visitReturn(ReturnTree node, Document document) {
        return null;
    }

    @Override
    public Element visitMemberSelect(MemberSelectTree node, Document document) {
        return null;
    }

    @Override
    public Element visitMemberReference(MemberReferenceTree node, Document document) {
        return null;
    }

    @Override
    public Element visitEmptyStatement(EmptyStatementTree node, Document document) {
        return null;
    }

    @Override
    public Element visitSwitch(SwitchTree node, Document document) {
        return null;
    }

    @Override
    public Element visitSwitchExpression(SwitchExpressionTree node, Document document) {
        return null;
    }

    @Override
    public Element visitSynchronized(SynchronizedTree node, Document document) {
        return null;
    }

    @Override
    public Element visitThrow(ThrowTree node, Document document) {
        return null;
    }

    @Override
    public Element visitCompilationUnit(CompilationUnitTree node, Document document) {
        return null;
    }

    @Override
    public Element visitTry(TryTree node, Document document) {
        return null;
    }

    @Override
    public Element visitParameterizedType(ParameterizedTypeTree node, Document document) {
        return null;
    }

    @Override
    public Element visitUnionType(UnionTypeTree node, Document document) {
        return null;
    }

    @Override
    public Element visitIntersectionType(IntersectionTypeTree node, Document document) {
        return null;
    }

    @Override
    public Element visitArrayType(ArrayTypeTree node, Document document) {
        return null;
    }

    @Override
    public Element visitTypeCast(TypeCastTree node, Document document) {
        return null;
    }

    @Override
    public Element visitPrimitiveType(PrimitiveTypeTree node, Document document) {
        return null;
    }

    @Override
    public Element visitTypeParameter(TypeParameterTree node, Document document) {
        return null;
    }

    @Override
    public Element visitInstanceOf(InstanceOfTree node, Document document) {
        return null;
    }

    @Override
    public Element visitUnary(UnaryTree node, Document document) {
        return null;
    }

    @Override
    public Element visitVariable(VariableTree node, Document document) {
        return null;
    }

    @Override
    public Element visitWhileLoop(WhileLoopTree node, Document document) {
        return null;
    }

    @Override
    public Element visitWildcard(WildcardTree node, Document document) {
        return null;
    }

    @Override
    public Element visitModule(ModuleTree node, Document document) {
        return null;
    }

    @Override
    public Element visitExports(ExportsTree node, Document document) {
        return null;
    }

    @Override
    public Element visitOpens(OpensTree node, Document document) {
        return null;
    }

    @Override
    public Element visitProvides(ProvidesTree node, Document document) {
        return null;
    }

    @Override
    public Element visitRequires(RequiresTree node, Document document) {
        return null;
    }

    @Override
    public Element visitUses(UsesTree node, Document document) {
        return null;
    }

    @Override
    public Element visitOther(Tree node, Document document) {
        return null;
    }

    @Override
    public Element visitYield(YieldTree node, Document document) {
        return null;
    }
    // endregion
}
