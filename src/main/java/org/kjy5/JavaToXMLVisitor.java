package org.kjy5;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import org.w3c.dom.Element;

public class JavaToXMLVisitor implements GenericVisitor<Element, XMLDocument> {
    @Override
    public Element visit(CompilationUnit n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(PackageDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(TypeParameter n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LineComment n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(BlockComment n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ClassOrInterfaceDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(RecordDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(CompactConstructorDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(EnumDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(EnumConstantDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(AnnotationDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(AnnotationMemberDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(FieldDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(VariableDeclarator n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ConstructorDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(MethodDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(Parameter n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(InitializerDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(JavadocComment n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ClassOrInterfaceType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(PrimitiveType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ArrayType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ArrayCreationLevel n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(IntersectionType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(UnionType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(VoidType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(WildcardType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(UnknownType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ArrayAccessExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ArrayCreationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ArrayInitializerExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(AssignExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(BinaryExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(CastExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ClassExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ConditionalExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(EnclosedExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(FieldAccessExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(InstanceOfExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(StringLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(IntegerLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LongLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(CharLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(DoubleLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(BooleanLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(NullLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(MethodCallExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(NameExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ObjectCreationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ThisExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SuperExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(UnaryExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(VariableDeclarationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(MarkerAnnotationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SingleMemberAnnotationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(NormalAnnotationExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(MemberValuePair n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ExplicitConstructorInvocationStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LocalClassDeclarationStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LocalRecordDeclarationStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(AssertStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(BlockStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LabeledStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(EmptyStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ExpressionStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SwitchStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SwitchEntry n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(BreakStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ReturnStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(IfStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(WhileStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ContinueStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(DoStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ForEachStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ForStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ThrowStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SynchronizedStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(TryStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(CatchClause n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(LambdaExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(MethodReferenceExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(TypeExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(NodeList n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(Name n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SimpleName n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ImportDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleDeclaration n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleRequiresDirective n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleExportsDirective n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleProvidesDirective n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleUsesDirective n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ModuleOpensDirective n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(UnparsableStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(ReceiverParameter n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(VarType n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(Modifier n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(SwitchExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(YieldStmt n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(TextBlockLiteralExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(TypePatternExpr n, XMLDocument arg) {
        return null;
    }

    @Override
    public Element visit(RecordPatternExpr n, XMLDocument arg) {
        return null;
    }
}
