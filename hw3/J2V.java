import syntaxtree.*;
import translator.*;
import visitor.*;

import java.io.*;

public class J2V {
    public static void main(String[] args) throws ParseException, FileNotFoundException {
        try {
            InputStream in = System.in;

            Node root = new MiniJavaParser(in).Goal();
            PreVisitor pv = PreVisitor.getInstance();
            root.accept(pv);
            //pv.printClasses();
            TranslateVisitor tv = new TranslateVisitor();
            //tv.printClasses();
            root.accept(tv);
            tv.printVapor();
        } catch (ParseException e) {

            // Handle Grammar Errors
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
