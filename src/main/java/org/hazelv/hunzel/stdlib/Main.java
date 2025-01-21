package org.hazelv.hunzel.stdlib;

import org.hazelv.hunzel.HunZelCallable;

import java.util.HashMap;

public class Main {
    public static HashMap<String, HunZelCallable> stdlib_funcs = new HashMap<>();
    public static HashMap<String, HunZelCallable> collect_std() {
        stdlib_funcs.put("str", Core.str);
        stdlib_funcs.put("print", Core.print);
        stdlib_funcs.put("println", Core.println);
        stdlib_funcs.put("input", Input.input);
        stdlib_funcs.put("clock", Time.clock);
        return stdlib_funcs;
    }
}
