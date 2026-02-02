package com.alibaba.qlexpress4.aparser;

public abstract class ScopeStackVisitor extends QLParserBaseVisitor<Void> {
    
    private ExistStack existStack;
    
    protected ScopeStackVisitor(ExistStack existStack) {
        this.existStack = existStack;
    }
    
    // scope
    @Override
    public Void visitBlockExpr(QLParser.BlockExprContext ctx) {
        push();
        super.visitBlockExpr(ctx);
        pop();
        return null;
    }
    
    @Override
    public Void visitQlIf(QLParser.QlIfContext qlIfContext) {
        qlIfContext.condition.accept(this);
        
        push();
        qlIfContext.thenBody().accept(this);
        pop();
        
        QLParser.ElseBodyContext elseBodyContext = qlIfContext.elseBody();
        if (elseBodyContext != null) {
            push();
            elseBodyContext.accept(this);
            pop();
        }
        
        return null;
    }
    
    @Override
    public Void visitSwitchExpr(QLParser.SwitchExprContext ctx) {
        // Visit switch expression
        ctx.expression().accept(this);
        
        // Visit switch body with scope
        QLParser.SwitchBlockStatementGroupsContext switchBlockContext = ctx.switchBlockStatementGroups();
        if (switchBlockContext != null) {
            push();
            // Visit each case group
            for (QLParser.SwitchBlockStatementGroupContext group : switchBlockContext.switchBlockStatementGroup()) {
                // Visit case labels (they contain expressions)
                if (group.switchLabels() != null) {
                    for (QLParser.SwitchLabelContext label : group.switchLabels().switchLabel()) {
                        if (label.expression() != null) {
                            label.expression().accept(this);
                        }
                    }
                }
                // Visit case body
                if (group.blockStatements() != null) {
                    group.blockStatements().accept(this);
                }
            }
            pop();
        }
        
        return null;
    }
    
    @Override
    public Void visitTryCatchExpr(QLParser.TryCatchExprContext ctx) {
        QLParser.BlockStatementsContext blockStatementsContext = ctx.blockStatements();
        if (blockStatementsContext != null) {
            push();
            blockStatementsContext.accept(this);
            pop();
        }
        
        QLParser.TryCatchesContext tryCatchesContext = ctx.tryCatches();
        if (tryCatchesContext != null) {
            tryCatchesContext.accept(this);
        }
        
        QLParser.TryFinallyContext tryFinallyContext = ctx.tryFinally();
        if (tryFinallyContext != null) {
            push();
            tryFinallyContext.accept(this);
            pop();
        }
        
        return null;
    }
    
    @Override
    public Void visitTryCatch(QLParser.TryCatchContext ctx) {
        push();
        super.visitTryCatch(ctx);
        pop();
        return null;
    }
    
    @Override
    public Void visitFunctionStatement(QLParser.FunctionStatementContext ctx) {
        ctx.varId().accept(this);
        
        push();
        QLParser.FormalOrInferredParameterListContext paramList = ctx.formalOrInferredParameterList();
        if (paramList != null) {
            paramList.accept(this);
        }
        
        QLParser.BlockStatementsContext functionBlockStatements = ctx.blockStatements();
        if (functionBlockStatements != null) {
            functionBlockStatements.accept(this);
        }
        pop();
        
        return null;
    }
    
    public void push() {
        this.existStack = this.existStack.push();
    }
    
    public void pop() {
        this.existStack = this.existStack.pop();
    }
    
    public ExistStack getStack() {
        return existStack;
    }
}
