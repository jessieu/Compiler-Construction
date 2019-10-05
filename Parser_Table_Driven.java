// An LL1 Recursive Descent Parser
// Reference: http://www.cs.nott.ac.uk/~psztxa/g51mal/ParseE0.java
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Parse extends Scanner {

    static String[] terminals = {"{", "}", "System.out.println", "(", ")", ";",
                                 ";", "if", "else", "while", "true", "false",
                                 "!"};

    static Stack<String> stack = new Stack<String>();

    static Scanner s = new Scanner();
    // current symbol in the input string
    static String current;

    // token lists of input string
    static List<String> tokens;
    static ListIterator<String> iterator;

    static void error() {
        System.out.println("Parse error");
        System.exit(-1);
    }

    static Boolean isTerminal(String symbol) {
        for (String t : terminals) {
            if (symbol.equals(t))
                return true;
        }
            return false;
    }

    public static void predict(String nonTer, String ter) {
        if (nonTer == "S") {
            if (ter == "{") {
                stack.push("}");
                stack.push("L");
                stack.push("{");
            }else if(ter == "System.out.println") {
                stack.push(";");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("System.out.println");
            }else if (ter == "if") {
                stack.push("S");
                stack.push("else");
                stack.push("S");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("if");
            }else if (ter == "while") {
                stack.push("S");
                stack.push(")");
                stack.push("E");
                stack.push("(");
                stack.push("while");
            }else {
                error();
            }

        }else if (nonTer == "L") {
            if (ter == "{" || ter == "System.out.println" || ter == "if" || ter == "while") {
                stack.push("L");
                stack.push("S");
            }else if (ter == "}") {}
            else {
                error();
            }

        }else if (nonTer == "E") {
            if (ter == "true") {
                stack.push("true");
            }else if (ter == "false") {
                stack.push("false");
            }else if (ter == "!") {
                stack.push("E");
                stack.push("!");
            }else {
                error();
            }
        }else {
            error();
        }
    }


    public static void main(String[] args) throws IOException {
            StringBuilder sb = new StringBuilder();
            String temp;
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));

            // Read inputs into stringbuilder
            while ((temp = reader.readLine()) != null) {
                    sb.append(temp);
            }

            String inStr = sb.toString();

            Scanner s = new Scanner();
            List<String> tokens = s.scan(inStr);
            tokens.add("$");

            stack.push("$");
            stack.push("S");

            String top;
            while (stack.size() != 0) {
                top = (String) stack.peek();
                    if (isTerminal(top) || top.equals("$")) {
                            if (top.equals(tokens.get(0))) {
                                    stack.pop();
                                    tokens.remove(0);
                            }else {
                                error();
                            }
                    }else{
                            stack.pop();
                            predict(top, tokens.get(0));
                    }
            }

            System.out.println("Program parsed successfully");
    }
}
