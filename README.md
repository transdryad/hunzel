**HunZel: The Language**

HunZel is a object-oriented programming language written in Java that was built using the help of [Crafting Interpreters part 1](https://craftinginterpreters.com/a-tree-walk-interpreter.html).
There are several stylistic syntax changes, as well as some extra functionality. You can import from other files in the working directory or from the library installation directory that is set in the environment variable HUNZEL_DIR.
Print statements need parentheses, and println must be used if you want an automatic new line.
Other than that, pretty much everything's the same as in the book. That's also where the rest of the documentation is.
The demo video is of the execution of the below example file.

An example file:
```
fun fib(n) {
    if (n < 2) return n;
    return fib(n - 1) + fib(n - 2);
}

fun main() {
    var before = clock();
    println(fib(40));
    var after = clock();
    println(after - before);
}

main();
```
Unfortunately, it takes 70 seconds to run.

There are two modes: file mode and prompt mode.
To enter prompt mode, run: 
```
java -jar HunZel-1.0-SNAPSHOT-all.jar
```

To execute a file, run:
```
java -jar HunZel-1.0-SNAPSHOT-all.jar <filename>
```

To build:
```
./gradlew shadowJar
```