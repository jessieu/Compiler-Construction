import syntaxtree.*;

public class Typecheck {
    public static void main(String[] args) throws IOException {
        // Read the Minijava program from stdin
        InputStream in = System.in;

        // Build an AST in memory using the Minijava parser
        Node root = new MiniJavaParser(in).Goal();

        // Traverse the AST using visitor pattern and do the Type checking

    }
}