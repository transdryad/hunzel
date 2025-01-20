package org.hazelv.hunzel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }
    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }
    private ClassType currentClass = ClassType.NONE;
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }
    private void endScope() {
        scopes.pop();
    }
    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            HunZel.error(name, "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
    private void resolveLocal(Expression expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
    void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }
    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }
    @Override
    public Void visitBlockStatement(Statement.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }
    @Override
    public Void visitClassStatement(Statement.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);
        if (stmt.superclass != null &&
                stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            HunZel.error(stmt.superclass.name, "A class can't inherit from itself.");
        }
        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }
        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }
        beginScope();
        scopes.peek().put("this", true);
        for (Statement.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
        if (stmt.superclass != null) endScope();
        endScope();
        currentClass = enclosingClass;
        return null;
    }
    @Override
    public Void visitExprStatement(Statement.Expr stmt) {
        resolve(stmt.expression);
        return null;
    }
    @Override
    public Void visitFunctionStatement(Statement.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }
    @Override
    public Void visitIfStatement(Statement.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }
    //replaced with native function
    //@Override
    //public Void visitPrintStmt(Stmt.Print stmt) {
    //    resolve(stmt.expression);
    //    return null;
    //}
    @Override
    public Void visitReturnStatement(Statement.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            HunZel.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                HunZel.error(stmt.keyword, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }
    @Override
    public Void visitVarStatement(Statement.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }
    @Override
    public Void visitWhileStatement(Statement.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }
    @Override
    public Void visitAssignExpression(Expression.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }
    @Override
    public Void visitBinaryExpression(Expression.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitCallExpression(Expression.Call expr) {
        resolve(expr.callee);
        for (Expression argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }
    @Override
    public Void visitGetExpression(Expression.Get expr) {
        resolve(expr.object);
        return null;
    }
    @Override
    public Void visitGroupingExpression(Expression.Grouping expr) {
        resolve(expr.expression);
        return null;
    }
    @Override
    public Void visitLiteralExpression(Expression.Literal expr) {
        return null;
    }
    @Override
    public Void visitLogicalExpression(Expression.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitSetExpression(Expression.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }
    @Override
    public Void visitSuperExpression(Expression.Super expr) {
        if (currentClass == ClassType.NONE) {
            HunZel.error(expr.keyword, "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            HunZel.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }
    @Override
    public Void visitThisExpression(Expression.This expr) {
        if (currentClass == ClassType.NONE) {
            HunZel.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }
    @Override
    public Void visitUnaryExpression(Expression.Unary expr) {
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitVariableExpression(Expression.Variable expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            HunZel.error(expr.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }
    private void resolve(Statement stmt) {
        stmt.accept(this);
    }
    private void resolve(Expression expr) {
        expr.accept(this);
    }
}
