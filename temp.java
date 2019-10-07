// construct the table
HashMap <String, String> pairs = new HashMap<String, String>();
HashMap <pairs, String[]> transition_table = new HashMap<pairs, String[]>();

transition_table.put(new HashMap<String, String>("S", "{"), new String[]{"{", "L", "}"});
transition_table.put(new HashMap<String, String>("S", "System.out.println"),
                     new ArrayList<String>.asList("System.out.println", "(", "E", ")"));
transition_table.put(new HashMap<String, String>("S", "if"),
                     new ArrayList<String>.asList("if", "(", "E", ")", "S", "else", "S"));
transition_table.put(new HashMap<String, String>("S", "while"),
                     new ArrayList<String>.asList("while", "(", "E", ")", "S"));

transition_table.put(new HashMap<String, String>("L", "{"),
                     new ArrayList<String>.asList("S", "L"));
transition_table.put(new HashMap<String, String>("L", "}"),
                     new ArrayList<String>.asList("epsilon"));
transition_table.put(new HashMap<String, String>("L", "System.out.println"),
                     new ArrayList<String>.asList("S", "L") );
transition_table.put(new HashMap<String, String>("L", "if"),
                     new ArrayList<String>.asList("S", "L"));
transition_table.put(new HashMap<String, String>("L", "while"),
                     new ArrayList<String>.asList("while", "(", "E", ")", "S"));

transition_table.put(new HashMap<String, String>("E", "true"),
                     new ArrayList<String>.asList("true"));
transition_table.put(new HashMap<String, String>("E", "false"),
                     new ArrayList<String>.asList("false"));
transition_table.put(new HashMap<String, String>("E", "!"),
                     new ArrayList<String>.asList("!", "E"));
}

static Boolean isTerminal(String symbol) {
for (String t : terminals) {
    if (symbol.isequals(t))
        return true;
}
return false;
}

static Boolean isNonTerminal(String symbol) {
String[] nonterminals = {"S", "L", "E"};
for (String n : nonterminals) {
    if (symbol.isequals(n))
        return true;
}
return false;
}

static void error(String msg) {
System.err.println(msg);
System.exit(-1);
}

static void parse(String inStr) {
Stack<String> stack = new Stack<String>();
stack.push("$");
stack.push("S");

Scanner s = new Scanner();
List<String> tokens = s.scan(inStr);

String top;
while ((top = (String) stack.peek()) != "$") {
    if (isTerminal(top) || top.isequals("$")) {
        if (top.isequals(tokens.get(0))) {
            stack.pop();
            tokens.remove(0);
        }else {
            error("Unexpected: " + top);
        }
    } else{
        stack.pop();
        HashMap<String, String> current = new HashMap<String, String>(top, tokens.get(0));
        String[] strList = transition_table.get(current);
        for (int i = strList.length() - 1; i >=0; i--) {
            stack.push(strList[i]);
        }
    }
}
}
