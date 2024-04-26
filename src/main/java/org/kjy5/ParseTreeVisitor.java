package org.kjy5;

import com.sun.tools.javac.tree.JCTree;

import java.util.List;

public class ParseTreeVisitor extends JCTree.Visitor {
    //region Constructor
    public ParseTreeVisitor() {
        super();
    }
    //endregion

    //region Visitors

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit that) {
        System.out.println("Top Level");

        visitDefs(that.defs);
    }

    @Override
    public void visitPackageDef(JCTree.JCPackageDecl that) {
        super.visitPackageDef(that);
    }

    @Override
    public void visitImport(JCTree.JCImport that) {
        super.visitImport(that);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl that) {
        System.out.println("Class: " + that.getSimpleName().toString());

        visitDefs(that.getMembers());
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl that) {
        System.out.println("Method: " + that.getName().toString());

        that.getBody().accept(this);
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl that) {
        System.out.println("Variable Decl: " + that.getName().toString());

        // Visit the variable type
        if (that.getType() != null) {
            that.getType().accept(this);
        }

        // Visit the initializer, if it exists
        if (that.getInitializer() != null) {
            that.getInitializer().accept(this);
        }
    }

    @Override
    public void visitSkip(JCTree.JCSkip that) {
        super.visitSkip(that);
    }

    @Override
    public void visitBlock(JCTree.JCBlock that) {
        System.out.println("Block");

        for (JCTree.JCStatement statement : that.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visitDoLoop(JCTree.JCDoWhileLoop that) {
        super.visitDoLoop(that);
    }

    @Override
    public void visitWhileLoop(JCTree.JCWhileLoop that) {
        super.visitWhileLoop(that);
    }

    @Override
    public void visitForLoop(JCTree.JCForLoop that) {
        super.visitForLoop(that);
    }

    @Override
    public void visitForeachLoop(JCTree.JCEnhancedForLoop that) {
        super.visitForeachLoop(that);
    }

    @Override
    public void visitLabelled(JCTree.JCLabeledStatement that) {
        super.visitLabelled(that);
    }

    @Override
    public void visitSwitch(JCTree.JCSwitch that) {
        System.out.println("Switch");
        for (JCTree.JCCase caseNode : that.getCases()) {
            caseNode.accept(this);
        }
    }

    @Override
    public void visitCase(JCTree.JCCase that) {
        System.out.println("Case");

        // Visit the case expression, if it exists
        for (JCTree.JCExpression expression : that.getExpressions()) {
            expression.accept(this);
        }

        // Visit each statement in the case
        for (JCTree.JCStatement statement : that.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visitSwitchExpression(JCTree.JCSwitchExpression that) {
        super.visitSwitchExpression(that);
    }

    @Override
    public void visitSynchronized(JCTree.JCSynchronized that) {
        super.visitSynchronized(that);
    }

    @Override
    public void visitTry(JCTree.JCTry that) {
        super.visitTry(that);
    }

    @Override
    public void visitCatch(JCTree.JCCatch that) {
        super.visitCatch(that);
    }

    @Override
    public void visitConditional(JCTree.JCConditional that) {
        super.visitConditional(that);
    }

    @Override
    public void visitIf(JCTree.JCIf that) {
        System.out.println("If Statement");

        // Visit the condition
        if (that.getCondition() != null) {
            that.getCondition().accept(this);
        }

        // Visit the then branch
        if (that.getThenStatement() != null) {
            that.getThenStatement().accept(this);
        }

        // Visit the else branch, if it exists
        if (that.getElseStatement() != null) {
            that.getElseStatement().accept(this);
        }
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement that) {
        System.out.println("Expression Statement");

        // Visit the expression
        if (that.getExpression() != null) {
            that.getExpression().accept(this);
        }
    }

    @Override
    public void visitBreak(JCTree.JCBreak that) {
        System.out.println("Break Statement");

        // Print the label, if it exists
        if (that.getLabel() != null) {
            System.out.println("Label: " + that.getLabel());
        }
    }

    @Override
    public void visitYield(JCTree.JCYield that) {
        super.visitYield(that);
    }

    @Override
    public void visitContinue(JCTree.JCContinue that) {
        super.visitContinue(that);
    }

    @Override
    public void visitReturn(JCTree.JCReturn that) {
        super.visitReturn(that);
    }

    @Override
    public void visitThrow(JCTree.JCThrow that) {
        super.visitThrow(that);
    }

    @Override
    public void visitAssert(JCTree.JCAssert that) {
        super.visitAssert(that);
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation that) {
        System.out.println("Method Invocation");

        // Visit the method select
        if (that.getMethodSelect() != null) {
            that.getMethodSelect().accept(this);
        }

        // Visit each argument in the method invocation
        for (JCTree.JCExpression argument : that.getArguments()) {
            argument.accept(this);
        }
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass that) {
        super.visitNewClass(that);
    }

    @Override
    public void visitNewArray(JCTree.JCNewArray that) {
        super.visitNewArray(that);
    }

    @Override
    public void visitLambda(JCTree.JCLambda that) {
        super.visitLambda(that);
    }

    @Override
    public void visitParens(JCTree.JCParens that) {
        System.out.println("Parenthesized Expression");

        // Visit the expression
        if (that.getExpression() != null) {
            that.getExpression().accept(this);
        }
    }

    @Override
    public void visitAssign(JCTree.JCAssign that) {
        System.out.println("Assignment");

        // Visit the variable
        if (that.getVariable() != null) {
            that.getVariable().accept(this);
        }

        // Visit the expression
        if (that.getExpression() != null) {
            that.getExpression().accept(this);
        }
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp that) {
        super.visitAssignop(that);
    }

    @Override
    public void visitUnary(JCTree.JCUnary that) {
        super.visitUnary(that);
    }

    @Override
    public void visitBinary(JCTree.JCBinary that) {
        System.out.println("Binary Operation");

        // Visit the left operand
        if (that.getLeftOperand() != null) {
            that.getLeftOperand().accept(this);
        }

        // Visit the right operand
        if (that.getRightOperand() != null) {
            that.getRightOperand().accept(this);
        }

        // Print the operator
        System.out.println("Operator: " + that.getOperator());
    }

    @Override
    public void visitTypeCast(JCTree.JCTypeCast that) {
        super.visitTypeCast(that);
    }

    @Override
    public void visitTypeTest(JCTree.JCInstanceOf that) {
        super.visitTypeTest(that);
    }

    @Override
    public void visitAnyPattern(JCTree.JCAnyPattern that) {
        super.visitAnyPattern(that);
    }

    @Override
    public void visitBindingPattern(JCTree.JCBindingPattern that) {
        super.visitBindingPattern(that);
    }

    @Override
    public void visitDefaultCaseLabel(JCTree.JCDefaultCaseLabel that) {
        super.visitDefaultCaseLabel(that);
    }

    @Override
    public void visitConstantCaseLabel(JCTree.JCConstantCaseLabel that) {
        super.visitConstantCaseLabel(that);
    }

    @Override
    public void visitPatternCaseLabel(JCTree.JCPatternCaseLabel that) {
        super.visitPatternCaseLabel(that);
    }

    @Override
    public void visitRecordPattern(JCTree.JCRecordPattern that) {
        super.visitRecordPattern(that);
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess that) {
        super.visitIndexed(that);
    }

    @Override
    public void visitSelect(JCTree.JCFieldAccess that) {
        System.out.println("Field Access");

        // Visit the selected field
        if (that.getIdentifier() != null) {
            System.out.println("Field: " + that.getIdentifier());
        }

        // Visit the expression
        if (that.getExpression() != null) {
            that.getExpression().accept(this);
        }
    }

    @Override
    public void visitReference(JCTree.JCMemberReference that) {
        super.visitReference(that);
    }

    @Override
    public void visitIdent(JCTree.JCIdent that) {
        System.out.println("Identifier: " + that.getName());
    }

    @Override
    public void visitLiteral(JCTree.JCLiteral that) {
        System.out.println("Literal: " + that.getValue());
    }

    @Override
    public void visitStringTemplate(JCTree.JCStringTemplate that) {
        super.visitStringTemplate(that);
    }

    @Override
    public void visitTypeIdent(JCTree.JCPrimitiveTypeTree that) {
        System.out.println("Type: " + that.getPrimitiveTypeKind());
    }

    @Override
    public void visitTypeArray(JCTree.JCArrayTypeTree that) {
        super.visitTypeArray(that);
    }

    @Override
    public void visitTypeApply(JCTree.JCTypeApply that) {
        super.visitTypeApply(that);
    }

    @Override
    public void visitTypeUnion(JCTree.JCTypeUnion that) {
        super.visitTypeUnion(that);
    }

    @Override
    public void visitTypeIntersection(JCTree.JCTypeIntersection that) {
        super.visitTypeIntersection(that);
    }

    @Override
    public void visitTypeParameter(JCTree.JCTypeParameter that) {
        super.visitTypeParameter(that);
    }

    @Override
    public void visitWildcard(JCTree.JCWildcard that) {
        super.visitWildcard(that);
    }

    @Override
    public void visitTypeBoundKind(JCTree.TypeBoundKind that) {
        super.visitTypeBoundKind(that);
    }

    @Override
    public void visitAnnotation(JCTree.JCAnnotation that) {
        super.visitAnnotation(that);
    }

    @Override
    public void visitModifiers(JCTree.JCModifiers that) {
        super.visitModifiers(that);
    }

    @Override
    public void visitAnnotatedType(JCTree.JCAnnotatedType that) {
        super.visitAnnotatedType(that);
    }

    @Override
    public void visitErroneous(JCTree.JCErroneous that) {
        super.visitErroneous(that);
    }

    @Override
    public void visitModuleDef(JCTree.JCModuleDecl that) {
        super.visitModuleDef(that);
    }

    @Override
    public void visitExports(JCTree.JCExports that) {
        super.visitExports(that);
    }

    @Override
    public void visitOpens(JCTree.JCOpens that) {
        super.visitOpens(that);
    }

    @Override
    public void visitProvides(JCTree.JCProvides that) {
        super.visitProvides(that);
    }

    @Override
    public void visitRequires(JCTree.JCRequires that) {
        super.visitRequires(that);
    }

    @Override
    public void visitUses(JCTree.JCUses that) {
        super.visitUses(that);
    }

    @Override
    public void visitLetExpr(JCTree.LetExpr that) {
        super.visitLetExpr(that);
    }

    @Override
    public void visitTree(JCTree that) {
        super.visitTree(that);
    }

    //endregion

    //region Helper methods
    private void visitDefs(List<JCTree> defs) {
        for (JCTree def : defs) {
            def.accept(this);
        }
    }
    //endregion
}
