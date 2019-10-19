import syntaxtree.*;
import visitor.*;
import java.io.*;
import symboltable.*;

public class TypeCheck {
    public static void main(String [] args) throws ParseException, FileNotFoundException {

        // Input program file
//        String filename = "/Users/weilanyu/code/CS132_Compiler_Construction/cs132/testcases/hw2/Basic.java";
//        InputStream in = new FileInputStream(filename);

        // but you should be using stdin instead of reading from a file
        //InputStream in = System.in;
        try {
            InputStream in = System.in;

            Node root = new MiniJavaParser(in).Goal();
            // Up until this point, we have read the program from input and built the AST in memory

            // We make a new visitor
            //GJDepthFirst mv = new SymbolTableBuilder();
            SymbolTableBuilder st = new SymbolTableBuilder();
            SymbolTable instance = SymbolTable.getInstance();
            root.accept(st, null);
            instance.printTable();

            // Right now for the purpose of demonstration, I'm simply putting an empty string as the second argument
            // start traversing the AST
//        root.accept(mv, "");

            //SymbolTable instance = SymbolTable.getInstance();
            //System.out.println(instance.symTable);
        }catch (TokenMgrError e) {

            // Handle Lexical Errors
            e.printStackTrace();
        } catch (ParseException e) {

            // Handle Grammar Errors
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}