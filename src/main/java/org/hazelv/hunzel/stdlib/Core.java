package org.hazelv.hunzel.stdlib;

import org.hazelv.hunzel.HunZelCallable;
import org.hazelv.hunzel.Interpreter;

import java.util.List;

public class Core {
    static final HunZelCallable print = new HunZelCallable() {
        @Override
        public int arity() {
            return 1;
        }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            System.out.print(stringify(arguments.getFirst()));
            return null;
        }
        @Override
        public String toString() { return "<stdlib function 'print'>"; }
    };
    static final HunZelCallable println = new HunZelCallable() {
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
        public String toString() { return "<stdlib function 'print'>"; }
    };
    static final HunZelCallable str = new HunZelCallable() {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return stringify(arguments.getFirst());
        }
        @Override
        public String toString() { return "<stdlib function 'str'>"; }
    };
    static String stringify(Object object) {
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
