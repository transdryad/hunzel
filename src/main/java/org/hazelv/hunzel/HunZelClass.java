package org.hazelv.hunzel;

import java.util.List;
import java.util.Map;

class HunZelClass implements HunZelCallable {
    final String name;
    final HunZelClass superclass;
    private final Map<String, HunZelFunction> methods;
    HunZelClass(String name, HunZelClass superclass, Map<String, HunZelFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }
    HunZelFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }
    @Override
    public String toString() {
        return name;
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        HunZelInstance instance = new HunZelInstance(this);
        HunZelFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
    @Override
    public int arity() {
        HunZelFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
