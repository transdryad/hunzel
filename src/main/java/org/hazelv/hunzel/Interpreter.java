package org.hazelv.hunzel;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();
    Interpreter() {
        Natives.define_all(this);
    }
    @Override
    public Object visitLiteralExpression(Expression.Literal expr) {
        return expr.value;
    }
    @Override
    public Object visitLogicalExpression(Expression.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }
    @Override
    public Object visitSetExpression(Expression.Set expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof HunZelInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }
        Object value = evaluate(expr.value);
        ((HunZelInstance)object).set(expr.name, value);
        return value;
    }
    @Override
    public Object visitSuperExpression(Expression.Super expr) {
        int distance = locals.get(expr);
        HunZelClass superclass = (HunZelClass)environment.getAt(distance, "super");
        HunZelInstance object = (HunZelInstance)environment.getAt(distance - 1, "this");
        HunZelFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }
    @Override
    public Object visitThisExpression(Expression.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }
    @Override
    public Object visitUnaryExpression(Expression.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        // Unreachable.
        return null;
    }
    @Override
    public Object visitVariableExpression(Expression.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }
    private Object lookUpVariable(Token name, Expression expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }
    void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            HunZel.runtimeError(error);
        }
    }
    @Override
    public Object visitGroupingExpression(Expression.Grouping expr) {
        return evaluate(expr.expression);
    }
    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }
    private void execute(Statement stmt) {
        stmt.accept(this);
    }
    void resolve(Expression expr, int depth) {
        locals.put(expr, depth);
    }
    void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
    @Override
    public Void visitBlockStatement(Statement.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    @Override
    public Void visitClassStatement(Statement.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof HunZelClass)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class.");
            }
        }
        environment.define(stmt.name.lexeme, null);
        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }
        Map<String, HunZelFunction> methods = new HashMap<>();
        for (Statement.Function method : stmt.methods) {
            HunZelFunction function = new HunZelFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }
        HunZelClass klass = new HunZelClass(stmt.name.lexeme, (HunZelClass)superclass, methods);
        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }
    @Override
    public Void visitExprStatement(Statement.Expr stmt) {
        evaluate(stmt.expression);
        return null;
    }
    @Override
    public Void visitFunctionStatement(Statement.Function stmt) {
        HunZelFunction function = new HunZelFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }
    @Override
    public Void visitIfStatement(Statement.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
    //replaced with native function
    //@Override
    //public Void visitPrintStatement(Statement.Print stmt) {
        //Object value = evaluate(stmt.expression);
        //System.out.println(stringify(value));
        //return null;
    //}
    @Override
    public Void visitReturnStatement(Statement.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }
    @Override
    public Void visitVarStatement(Statement.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }
    @Override
    public Void visitWhileStatement(Statement.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        // Unreachable.
        return null;
    }
    @Override
    public Object visitCallExpression(Expression.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof HunZelCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }
        HunZelCallable function = (HunZelCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }
        return function.call(this, arguments);
    }
    @Override
    public Object visitGetExpression(Expression.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof HunZelInstance) {
            return ((HunZelInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }
}
