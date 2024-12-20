# Book wrote GenerateAst.java, wrote again in Python to be more "script"

TAB = "    "

def defineAst(out_dir: str, base_name: str, types: list[str]):
    path: str = out_dir + "/" + base_name + ".java"

    with open(path, "w") as file:
        file.write("package jlox;" + "\n\n")
        file.write("import java.util.List;" + "\n\n")
        file.write("abstract class " + base_name + " {" + "\n")

        # Define each type's AST class
        for t in types:
            class_name, field_list = t.split(":")
            class_name = class_name.strip()
            field_list = field_list.strip()

            file.write(TAB + "static class " + class_name + " extends " + base_name + " {" + "\n")

            # Constructor
            file.write(TAB + TAB + class_name + "(" + field_list + ") {" + "\n")

            fields = field_list.split(", ")
            for field in fields:
                field_name = field.split()[1].strip()
                file.write(TAB + TAB + TAB + "this." + field_name + " = " + field_name + ";" + "\n")
            file.write(TAB + TAB + "}" + "\n");

            # Fields
            file.write("\n")
            for field in fields:
                file.write(TAB + TAB + "final " + field.strip() + ";" + "\n")

            file.write(TAB + "}" + "\n")

        file.write("}\n")

if __name__ == '__main__':
    import sys

    if len(sys.argv) < 2:
        print("Usage: generate_ast <output dir>")
        exit(64)

    out_dir = sys.argv[1]
    defineAst(out_dir, "Expr", [
           "Binary   : Expr left, Token operator, Expr right",
           "Grouping : Expr expression",
           "Literal  : Object value",
           "Unary    : Token operator, Expr right"
    ])