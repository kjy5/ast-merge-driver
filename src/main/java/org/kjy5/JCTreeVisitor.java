package org.kjy5;

import com.github.gumtreediff.tree.Tree;
import com.sun.source.tree.*;

public class JCTreeVisitor implements TreeVisitor<Tree, Tree> {
    @Override
    public Tree visitAnnotatedType(AnnotatedTypeTree node, Tree tree) {
        System.out.println("visit Annotated Type");
        return null;
    }

    @Override
    public Tree visitAnnotation(AnnotationTree node, Tree tree) {
        System.out.println("visit Annotation");
        return null;
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Tree tree) {
        System.out.println("visit Method Invocation");
        return null;
    }

    @Override
    public Tree visitAssert(AssertTree node, Tree tree) {
        System.out.println("visit Assert");
        return null;
    }

    @Override
    public Tree visitAssignment(AssignmentTree node, Tree tree) {
        System.out.println("visit Assignment");
        return null;
    }

    @Override
    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Tree tree) {
        System.out.println("visit Compound Assignment");
        return null;
    }

    @Override
    public Tree visitBinary(BinaryTree node, Tree tree) {
        System.out.println("visit Binary");
        return null;
    }

    @Override
    public Tree visitBlock(BlockTree node, Tree tree) {
        System.out.println("visit Block");
        return null;
    }

    @Override
    public Tree visitBreak(BreakTree node, Tree tree) {
        System.out.println("visit Break");
        return null;
    }

    @Override
    public Tree visitCase(CaseTree node, Tree tree) {
        System.out.println("visit Case");
        return null;
    }

    @Override
    public Tree visitCatch(CatchTree node, Tree tree) {
        System.out.println("visit Catch");
        return null;
    }

    @Override
    public Tree visitClass(ClassTree node, Tree tree) {
        System.out.println("visit Class");
        return null;
    }

    @Override
    public Tree visitConditionalExpression(ConditionalExpressionTree node, Tree tree) {
        System.out.println("visit Conditional Expression");
        return null;
    }

    @Override
    public Tree visitContinue(ContinueTree node, Tree tree) {
        System.out.println("visit Continue");
        return null;
    }

    @Override
    public Tree visitDoWhileLoop(DoWhileLoopTree node, Tree tree) {
        System.out.println("visit Do While Loop");
        return null;
    }

    @Override
    public Tree visitErroneous(ErroneousTree node, Tree tree) {
        System.out.println("visit Erroneous");
        return null;
    }

    @Override
    public Tree visitExpressionStatement(ExpressionStatementTree node, Tree tree) {
        System.out.println("visit Expression Statement");
        return null;
    }

    @Override
    public Tree visitEnhancedForLoop(EnhancedForLoopTree node, Tree tree) {
        System.out.println("visit Enhanced For Loop");
        return null;
    }

    @Override
    public Tree visitForLoop(ForLoopTree node, Tree tree) {
        System.out.println("visit For Loop");
        return null;
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Tree tree) {
        System.out.println("visit Identifier");
        return null;
    }

    @Override
    public Tree visitIf(IfTree node, Tree tree) {
        System.out.println("visit If");
        return null;
    }

    @Override
    public Tree visitImport(ImportTree node, Tree tree) {
        System.out.println("visit Import");
        return null;
    }

    @Override
    public Tree visitArrayAccess(ArrayAccessTree node, Tree tree) {
        System.out.println("visit Array Access");
        return null;
    }

    @Override
    public Tree visitLabeledStatement(LabeledStatementTree node, Tree tree) {
        System.out.println("visit Labeled Statement");
        return null;
    }

    @Override
    public Tree visitLiteral(LiteralTree node, Tree tree) {
        System.out.println("visit Literal");
        return null;
    }

    @SuppressWarnings("preview")
    @Override
    public Tree visitStringTemplate(StringTemplateTree node, Tree tree) {
        System.out.println("visit String Template");
        return null;
    }

    @Override
    public Tree visitAnyPattern(AnyPatternTree node, Tree tree) {
        System.out.println("visit Any Pattern");
        return null;
    }

    @Override
    public Tree visitBindingPattern(BindingPatternTree node, Tree tree) {
        System.out.println("visit Binding Pattern");
        return null;
    }

    @Override
    public Tree visitDefaultCaseLabel(DefaultCaseLabelTree node, Tree tree) {
        System.out.println("visit Default Case Label");
        return null;
    }

    @Override
    public Tree visitConstantCaseLabel(ConstantCaseLabelTree node, Tree tree) {
        System.out.println("visit Constant Case Label");
        return null;
    }

    @Override
    public Tree visitPatternCaseLabel(PatternCaseLabelTree node, Tree tree) {
        System.out.println("visit Pattern Case Label");
        return null;
    }

    @Override
    public Tree visitDeconstructionPattern(DeconstructionPatternTree node, Tree tree) {
        System.out.println("visit Deconstruction Pattern");
        return null;
    }

    @Override
    public Tree visitMethod(MethodTree node, Tree tree) {
        System.out.println("visit Method");
        return null;
    }

    @Override
    public Tree visitModifiers(ModifiersTree node, Tree tree) {
        System.out.println("visit Modifiers");
        return null;
    }

    @Override
    public Tree visitNewArray(NewArrayTree node, Tree tree) {
        System.out.println("visit New Array");
        return null;
    }

    @Override
    public Tree visitNewClass(NewClassTree node, Tree tree) {
        System.out.println("visit New Class");
        return null;
    }

    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree node, Tree tree) {
        System.out.println("visit Lambda Expression");
        return null;
    }

    @Override
    public Tree visitPackage(PackageTree node, Tree tree) {
        System.out.println("visit Package");
        return null;
    }

    @Override
    public Tree visitParenthesized(ParenthesizedTree node, Tree tree) {
        System.out.println("visit Parenthesized");
        return null;
    }

    @Override
    public Tree visitReturn(ReturnTree node, Tree tree) {
        System.out.println("visit Return");
        return null;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Tree tree) {
        System.out.println("visit Member Select");
        return null;
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree node, Tree tree) {
        System.out.println("visit Member Reference");
        return null;
    }

    @Override
    public Tree visitEmptyStatement(EmptyStatementTree node, Tree tree) {
        System.out.println("visit Empty Statement");
        return null;
    }

    @Override
    public Tree visitSwitch(SwitchTree node, Tree tree) {
        System.out.println("visit Switch");
        return null;
    }

    @Override
    public Tree visitSwitchExpression(SwitchExpressionTree node, Tree tree) {
        System.out.println("visit Switch Expression");
        return null;
    }

    @Override
    public Tree visitSynchronized(SynchronizedTree node, Tree tree) {
        System.out.println("visit Synchronized");
        return null;
    }

    @Override
    public Tree visitThrow(ThrowTree node, Tree tree) {
        System.out.println("visit Throw");
        return null;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Tree tree) {
        System.out.println("visit Compilation Unit");
        return null;
    }

    @Override
    public Tree visitTry(TryTree node, Tree tree) {
        System.out.println("visit Try");
        return null;
    }

    @Override
    public Tree visitParameterizedType(ParameterizedTypeTree node, Tree tree) {
        System.out.println("visit Parameterized Type");
        return null;
    }

    @Override
    public Tree visitUnionType(UnionTypeTree node, Tree tree) {
        System.out.println("visit Union Type");
        return null;
    }

    @Override
    public Tree visitIntersectionType(IntersectionTypeTree node, Tree tree) {
        System.out.println("visit Intersection Type");
        return null;
    }

    @Override
    public Tree visitArrayType(ArrayTypeTree node, Tree tree) {
        System.out.println("visit Array Type");
        return null;
    }

    @Override
    public Tree visitTypeCast(TypeCastTree node, Tree tree) {
        System.out.println("visit Type Cast");
        return null;
    }

    @Override
    public Tree visitPrimitiveType(PrimitiveTypeTree node, Tree tree) {
        System.out.println("visit Primitive Type");
        return null;
    }

    @Override
    public Tree visitTypeParameter(TypeParameterTree node, Tree tree) {
        System.out.println("visit Type Parameter");
        return null;
    }

    @Override
    public Tree visitInstanceOf(InstanceOfTree node, Tree tree) {
        System.out.println("visit Instance Of");
        return null;
    }

    @Override
    public Tree visitUnary(UnaryTree node, Tree tree) {
        System.out.println("visit Unary");
        return null;
    }

    @Override
    public Tree visitVariable(VariableTree node, Tree tree) {
        System.out.println("visit Variable");
        return null;
    }

    @Override
    public Tree visitWhileLoop(WhileLoopTree node, Tree tree) {
        System.out.println("visit While Loop");
        return null;
    }

    @Override
    public Tree visitWildcard(WildcardTree node, Tree tree) {
        System.out.println("visit Wildcard");
        return null;
    }

    @Override
    public Tree visitModule(ModuleTree node, Tree tree) {
        System.out.println("visit Module");
        return null;
    }

    @Override
    public Tree visitExports(ExportsTree node, Tree tree) {
        System.out.println("visit Exports");
        return null;
    }

    @Override
    public Tree visitOpens(OpensTree node, Tree tree) {
        System.out.println("visit Opens");
        return null;
    }

    @Override
    public Tree visitProvides(ProvidesTree node, Tree tree) {
        System.out.println("visit Provides");
        return null;
    }

    @Override
    public Tree visitRequires(RequiresTree node, Tree tree) {
        System.out.println("visit Requires");
        return null;
    }

    @Override
    public Tree visitUses(UsesTree node, Tree tree) {
        System.out.println("visit Uses");
        return null;
    }

    @Override
    public Tree visitOther(com.sun.source.tree.Tree node, Tree tree) {
        System.out.println("visit Other");
        return null;
    }

    @Override
    public Tree visitYield(YieldTree node, Tree tree) {
        System.out.println("visit Yield");
        return null;
    }
}
