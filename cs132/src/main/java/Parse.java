// An LL1 Recursive Descent Parser
// Reference: http://www.cs.nott.ac.uk/~psztxa/g51mal/ParseE0.java
// import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Parse extends Tokenizer {

static Tokenizer tok = new Tokenizer();
// current symbol in the input string
static String current;

// token lists of input string
static List<String> tokens;
static ListIterator<String> iterator;

// get the next token in the token list
public static void next() {
        if (iterator.hasNext()) {
                current = iterator.next();
        }
}

public static void error() {
    System.out.println("Parse error");
    System.exit(-1);
}

// test whether current token in token list match the expected token
public static void match(String expected) {
        if (current.equals(expected)) {
                next();
        }else {
            error();
        }
}

public static Boolean parseP() {
    parseS();
    if (current == "$") {
        return true;
    }

    return false;
}

// parsing grammar S
public static void parseS() {
        if (current == "{") {
            match("{");
            parseL();
            match("}");
        }else if (current == "System.out.println") {
            match("System.out.println");
            match("(");
            parseE();
            match(")");
            match(";");
        }else if (current == "if") {
            match("if");
            match("(");
            parseE();
            match(")");
            parseS();
            match("else");
            parseS();
        }else if (current == "while") {
            match("while");
            match("(");
            parseE();
            match(")");
            parseS();
        }else {
            error();
        }
}

// parsing grammar L
public static void parseL() {
    if (current == "}") {
    }else {
        parseS();
        parseL();
    }

}

// parsing grammar E
public static void parseE() {
        if (current == "true") {
            match("true");
        }else if (current == "false") {
            match("false");
        }else if (current == "!") {
            match("!");
            parseE();
        }else {
            error();
        }
}

public static void main(String[] args) throws IOException {
        List<String> inStr = new ArrayList<String>();
        String temp;
        BufferedReader r =
                new BufferedReader(new InputStreamReader(System.in));

        // Read inputs into stringbuilder
        while ((temp = r.readLine()) != null) {
            String[] sp = temp.split(" ");
            for (String st : sp){
                inStr.add(st);
            }
        }

        tokens = tok.getTokens(inStr);

        // indicate the end of input
        tokens.add("$");
        iterator = tokens.listIterator();

        next();
        // parsing
        if (parseP()) {
                System.out.println("Program parsed successfully");
        }else {
                System.out.println("Parse error");
                System.exit(-1);
        }
}
}
