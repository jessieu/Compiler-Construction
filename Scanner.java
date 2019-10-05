import java.util.*;
import java.io.IOException;

public class Scanner{
    // It should take a string as argument
    public static List<String> scan (String in) throws IOException {
        String[] terminals = {"{", "}", "System.out.println", "(", ")", ";",
                              ";", "if", "else", "while", "true", "false",
                              "!"};

        Boolean matched = false;
        // output token list
        List<String> tokens = new ArrayList<String>();

        // Strip out of whitespace
        String inStr = in.replaceAll("\\s+","");

        int pos = 0;
        while (pos < inStr.length()) {
            for (String t : terminals) {
                if (inStr.charAt(pos) == t.charAt(0)) {
                    if (t.equals(inStr.substring(pos, pos + t.length()))) {
                        //append string to token list
                        tokens.add(t);
                        // move the pos to the end of matched substring
                        pos += t.length();
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                System.out.println("Parse error");
                System.exit(-1);
            }
            matched = false;
        }


        return tokens;
    }

}
