import syntaxtree.*;

import visitor.GJDepthFirst;

import java.io.*;

public class Typecheck {
    public static void main(String [] args) throws ParseException, FileNotFoundException {

        // Input program file
        String filename = "testcases/hw2/MyTest.java";
        InputStream in = new FileInputStream(filename);

        // but you should be using stdin instead of reading from a file
        //InputStream in = System.in;

        Node root = new MiniJavaParser(in).Goal();
        // Up until this point, we have read the program from input and built the AST in memory

        // We make a new visitor
        GJDepthFirst mv = new MyVisiter2();

        // Right now for the purpose of demonstration, I'm simply putting an empty string as the second argument
        // start traversing the AST
        root.accept(mv, "");

        SymbolTable instance = SymbolTable.getInstance();
        //System.out.println(instance.symTable);
    }
}