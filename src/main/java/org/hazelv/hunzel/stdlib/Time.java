package org.hazelv.hunzel.stdlib;

import org.hazelv.hunzel.HunZelCallable;
import org.hazelv.hunzel.Interpreter;

import java.util.List;

public class Time {
    static final HunZelCallable clock = new HunZelCallable() {
        @Override
        public int arity() {
            return 0;
        }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double)System.currentTimeMillis() / 1000.0;
        }
        @Override
        public String toString() { return "<stdlib function 'clock'>"; }
    };
}
