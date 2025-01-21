package org.hazelv.hunzel;

import org.hazelv.hunzel.stdlib.Main;
import java.util.HashMap;
// This is just to add a layer between stdlib and HunZel. EG: non-standard libraries.
public class Natives {
    public static HashMap<String, HunZelCallable> funcs = new HashMap<>();
    public static void define_all(Interpreter interpreter) {
        funcs.putAll(Main.collect_std());
        funcs.forEach(interpreter.globals::define);
    }
}
