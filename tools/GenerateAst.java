package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
           "Binary   : Expr left, Token operator, Expr right",
           "Grouping : Expr expression",
           "Literal  : Object value",
           "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // The AST classes
        for (String type : types) {
            String[] stuff = type.split(":");
            String className = stuff[0].trim();
            String fields = stuff[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");

        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        String tab = "    ";
        writer.println(tab + "static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println(tab + tab + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(tab + tab + tab + "this." + name + " = " + name + ";");
        }
        writer.println(tab + tab + "}");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println(tab + tab + "final " + field + ";");
        }

        writer.println(tab + "}");
    }
}
