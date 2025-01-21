package org.hazelv.hunzel.stdlib;

import org.hazelv.hunzel.HunZelCallable;
import org.hazelv.hunzel.Interpreter;

import java.util.List;
import java.util.Scanner;

import static org.hazelv.hunzel.stdlib.Core.stringify;

public class Input {
    static final HunZelCallable input = new HunZelCallable() {
        @Override
        public int arity() {
            return 1;
        }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Scanner scn = new Scanner(System.in);
            System.out.print(stringify(arguments.getFirst()));
            String instr = scn.nextLine();
            scn.close();
            return instr;
        }
        @Override
        public String toString() { return "<stdlib function 'input'>"; }
    };
}
