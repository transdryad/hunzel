package org.hazelv.hunzel;

import java.util.List;

public class Natives {
    public static void define(Interpreter interpreter) {
        interpreter.globals.define("clock", clock);
        interpreter.globals.define("print", print);
        interpreter.globals.define("str", str);
    }
    //func definitions here
    private static final HunZelCallable clock = new HunZelCallable() {
        @Override
        public int arity() {
            return 0;
        }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double)System.currentTimeMillis() / 1000.0;
        }
        @Override
        public String toString() { return "<native function 'clock'>"; }
    };
    private static final HunZelCallable print = new HunZelCallable() {
        @Override
        public int arity() {
            return 1;
        }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            System.out.println(stringify(arguments.getFirst()));
            return null;
        }
        @Override
        public String toString() { return "<native function 'print'>"; }
    };
    private static final HunZelCallable str = new HunZelCallable() {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return stringify(arguments.getFirst());
        }
        @Override
        public String toString() { return "<native function 'str'>"; }
    };
    //helper methods here
    private static String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}
