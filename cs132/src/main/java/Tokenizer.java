import java.util.*;
import java.io.IOException;
import java.lang.String;

public class Tokenizer{
    static String[] terminals = {"{", "}", "System.out.println", "(", ")", ";",
              ";", "if", "else", "while", "true", "false",
              "!"};

    // output token list
    static List<String> tokens = new ArrayList<String>();
    // It should take a string as argument
    public static List<String> getTokens (List<String> in) throws IOException {
        for (String i : in){
            findTokens(i);
        }
        return tokens;
    }

    public static void findTokens(String s) {
        int pos = 0;
        while (pos < s.length()) {
            Boolean matched = false;
            for (String t : terminals) {
                if (s.charAt(pos) == t.charAt(0)) {
                    if (s.length() >= t.length()){
                        if (t.equals(s.substring(pos, pos + t.length()))) {
                            //append string to token list
                            tokens.add(t);
                            // move the pos to the end of matched substring
                            pos += t.length();
                            matched = true;
                            break;
                    }else {
                        System.out.println("Parse error");
        		        System.exit(-1);
                    }

                    }else{
                        System.out.println("Parse error");
        		        System.exit(-1);
                    }
                }
            }

            if (!matched) {
                System.out.println("Parse error");
		        System.exit(-1);
            }
        }
    }
}
