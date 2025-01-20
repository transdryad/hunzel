package org.hazelv.hunzel.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression left, Token operator, Expression right",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Get      : Expression object, Token name",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression left, Token operator, Expression right",
                "Set      : Expression object, Token name, Expression value",
                "This     : Token keyword",
                "Unary    : Token operator, Expression right",
                "Variable : Token name"
        ));
        defineAst(outputDir, "Statement", Arrays.asList(
                "Block      : List<Statement> statements",
                "Class      : Token name, List<Statement.Function> methods",
                "Expr : Expression expression",
                "Function   : Token name, List<Token> params, List<Statement> body",
                "If         : Expression condition, Statement thenBranch, Statement elseBranch",
                //replaced with native function.
                //"Print      : Expression expression",
                "Return     : Token keyword, Expression value",
                "Var        : Token name, Expression initializer",
                "While      : Expression condition, Statement body"
        ));
    }
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package org.hazelv.hunzel;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  public static class " + className + " extends " + baseName + " {");

        // Constructor.
        writer.println("    " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }

}
