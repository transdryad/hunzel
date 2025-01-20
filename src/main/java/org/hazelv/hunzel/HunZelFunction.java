package org.hazelv.hunzel;

import java.util.List;

class HunZelFunction implements HunZelCallable {
    private final Statement.Function declaration;
    private final Environment closure;
    HunZelFunction(Statement.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }
        @Override
    public int arity() {
        return declaration.params.size();
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
    @Override
    public String toString() {
        return "<function " + declaration.name.lexeme + ">";
    }
}
