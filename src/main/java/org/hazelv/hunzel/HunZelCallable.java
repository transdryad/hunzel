package org.hazelv.hunzel;

import java.util.List;

interface HunZelCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
