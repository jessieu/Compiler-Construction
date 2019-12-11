import syntaxtree.*;
import visitor.*;
import java.io.*;
import symboltable.*;
import typecheck.*;

public class Typecheck {
    public static void main(String [] args) throws ParseException, FileNotFoundException {
        try {
            InputStream in = System.in;

            Node root = new MiniJavaParser(in).Goal();

            SymbolTableBuilder st = new SymbolTableBuilder();
            SymbolTable instance = SymbolTable.getInstance();
            root.accept(st, null);
            //instance.printTable();

            TypeCheckVisitor tyChecker = new TypeCheckVisitor();
            root.accept(tyChecker, null);

            System.out.println("Program type checked successfully");

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