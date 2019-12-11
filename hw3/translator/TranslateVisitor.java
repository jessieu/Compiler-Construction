package translator;

import syntaxtree.*;
import visitor.*;

import java.util.*;

public class TranslateVisitor extends DepthFirstVisitor {
    PreVisitor pv = PreVisitor.getInstance();
    ArrayList<VClass> allClasses = pv.getAllClasses(); // get all classes from PreVisitor

    ArrayList<String> vaporRet = new ArrayList<>(); // return vapor code

    // store the current class and method for data passing down
    String mainClass; // identify mainClass and skip printing
    String currentClass;
    String tempClass;
    String currentMethod;

    int indentCount = 0;
    int labelCount = 0;
    int nullCount = 0;
    int temporalCount = 0;
    int outOfBoundCount = 0;
    int whileCount = 0;
    int ifCount = 0;

    String[] lastExpr = new String[2]; //0 - reg/name; 1 - type
    ArrayList<String> arguList = new ArrayList<>(); // argument list

    boolean hasAlloc = false; // decide whether the allocFunc should be printed or not

    public void printVapor() {
        printConst();
        System.out.println();
        for (String s : vaporRet) {
            System.out.println(s);
        }
    }

    // return the class information
    public VClass getClass(String className) {
        for (VClass vc : allClasses) {
            if (vc.getClassName() == className) {
                return vc;
            }
        }
        return null; // should not reach here
    }

    // search method in a class
    public VTable getMethod(String methodName, String className) {
        VClass vc = getClass(className);
        for (VTable vt : vc.getMemberMethods()) {
            if (vt.getMethodName() == methodName) {
                return vt;
            }
        }
        // check chain inheritance
        while (vc.isHasParent()) {
            String parent = vc.getParent();
            VClass parentClass = getClass(parent);
            for (VTable vt : parentClass.getMemberMethods()) {
                if (vt.getMethodName() == methodName) {
                    return vt;
                }
            }
            vc = getClass(parent);
        }

        return null;
    }

    public boolean isVar(String id) {
        VClass vs = getClass(currentClass);
        for (VTable vt : vs.getMemberMethods()) {
            if ((vt.getParams()).contains(id)) {
                return true;
            }
            if ((vt.getLocals()).contains(id)) {
                return true;
            }

        }
        return false;
    }

    public boolean isClass(String className) {
        if (getClass(className) != null) {
            return true;
        }
        return false;
    }

    public String getClassSize(String className) {
        ArrayList<String> fList = getFieldList(className);
        int size = fList.size();
        return Integer.toString(size * 4 + 4);
    }

    public ArrayList<String> getFieldList(String className) {
        VClass vc = getClass(className);
        ArrayList<String> mList = new ArrayList<>();
        VClass temp = vc;
        while (temp.isHasParent()) {
            String parentName = temp.getParent();
            VClass parentClass = getClass(parentName);
            for (String field : parentClass.getMemberFields()) {
                if (mList.contains(field)) {
                    int index = mList.indexOf(field);
                    mList.set(index, field);
                } else {
                    mList.add(field);
                }
            }
            temp = getClass(parentName);
        }

        for (String field : vc.getMemberFields()) {
            if (mList.contains(field)) {
                int index = mList.indexOf(field);
                mList.set(index, field);
            } else {
                mList.add(field);
            }
        }
        return mList;

    }

    // return the offset of a field -- include parent's field
    public int getFieldIndex(String id) {
        ArrayList<String> fList = getFieldList(currentClass);
        return fList.indexOf(id) * 4 + 4;
    }

    public ArrayList<String> getMethodList(String className, boolean printConst) {
        ArrayList<String> mList = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        VClass vc = getClass(className);
        VClass temp = vc;
        while (temp.isHasParent()) {
            String parentName = temp.getParent();
            VClass parentClass = getClass(parentName);
            for (VTable vt : parentClass.getMemberMethods()) {
                String m = vt.getMethodName();
                if (mList.contains(m)) {
                    int index = mList.indexOf(m);
                    mList.set(index, m);
                    mList.set(index, indent() + ":" + temp.getClassName() + "." + m);
                } else {
                    mList.add(m);
                    result.add(indent() + ":" + parentName + "." + m);
                }
            }
            temp = getClass(parentName);
        }

        for (VTable vt : vc.getMemberMethods()) {
            String m = vt.getMethodName();
            if (mList.contains(m)) {
                int index = mList.indexOf(m);
                mList.set(index, m);
                result.set(index, indent() + ":" + vc.getClassName() + "." + m);
            } else {
                mList.add(m);
                result.add(indent() + ":" + vc.getClassName() + "." + m);
            }
        }
        if (printConst) {
            return result;
        }
        return mList;
    }

    public int getMethodIndex(String id, String className) {
        ArrayList<String> mList = new ArrayList<>();
        mList = getMethodList(className, false);
        if (mList.contains(id)) {
            return mList.indexOf(id) * 4;
        }

        return -1; // should not reach here
    }


    public String getLabel(String which) {
        switch (which) {
            case "temporal": // temporal label
                return "t." + temporalCount++;
            case "ifBegin": // ifelse begin label
                ifCount++;
                return "if" + ifCount + "_else";
            case "ifEnd":   // ifelse end label
                return "if" + ifCount + "_end";
            case "wTop":   // while begin
                whileCount++;
                return "while" + whileCount + "_top";
            case "wEnd":    // while end
                return "while" + whileCount + "_end";
            case "bound":
                outOfBoundCount++;
                return "bounds" + outOfBoundCount;
            case "null":
                nullCount++;
                return "null" + nullCount;
            default:
                return null;    // should not reach here

        }
    }

    // increment the nullCount after calling nullptr and add the label into vaporRet
    public void nullPtr(String addr) {
        String nullLabel = getLabel("null");
        String np = "if " + addr + " goto :" + nullLabel;
        String err = "Error(\"null pointer\")";
        vaporRet.add(indent() + np);
        indentCount++;
        vaporRet.add(indent() + err);
        indentCount--;
        vaporRet.add(indent() + nullLabel + ":");
    }

    // before calling this, need to load baseAddr and check
    public void outOfBound(String ok) {
        String boundLabel = getLabel("bound");
        String ltS = "if " + ok + " goto :" + boundLabel;
        String err = "Error(\"array index out of bounds\")";
        vaporRet.add(indent() + ltS);
        indentCount++;
        vaporRet.add(indent() + err);
        indentCount--;
        vaporRet.add(indent() + boundLabel + ": ");
    }

    // add parent method --- exclude parent's method if child override
    public void printConst() {
        ArrayList<String> result = new ArrayList<>();
        for (VClass vc : allClasses) {
            // skip the main class
            if (vc.getClassName() == mainClass) {
                continue;
            }
            System.out.println("const vmt_" + vc.getClassName());
            indentCount++;
            result = getMethodList(vc.getClassName(), true);
            for (String mName : result) {
                System.out.println(mName);
            }
            result.clear();
            indentCount--;
            System.out.println();
        }
    }

    public String indent() {
        String numSpaces = "";
        for (int i = 0; i < indentCount; i++) {
            numSpaces += "    ";
        }
        return numSpaces;
    }

    // add at the end of the return value
    public void allocFunc() {
        vaporRet.add("func AllocArray (size)");
        indentCount++;
        vaporRet.add(indent() + "bytes = MulS(size 4)");
        vaporRet.add(indent() + "bytes = Add(bytes 4)");
        vaporRet.add(indent() + "v = HeapAllocZ(bytes)");
        vaporRet.add(indent() + "[v] = size");
        vaporRet.add(indent() + "ret v");
        indentCount--;
    }

    public void setLastExpr(String name, String type) {
        lastExpr[0] = name;
        lastExpr[1] = type;
    }

    public String checkExpr() {
        String name = lastExpr[0];
        if (lastExpr[1] == "EXPR") {
            String temp1 = getLabel("temporal");
            vaporRet.add(indent() + temp1 + " = " + name);
            name = temp1;
        }
        return name;
    }


    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public void visit(Goal n) {
        n.f0.accept(this);
        for (Node nd : n.f1.nodes) {
            nd.accept(this);
        }
        if (hasAlloc) {
            allocFunc();
        }

    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public void visit(MainClass n) {
        vaporRet.add("func Main()");
        //String currentClass = n.f1.f0.toString();
        currentClass = n.f1.f0.toString();
        mainClass = currentClass;
        //classScope.push(currentClass);
        currentMethod = "Main";
        indentCount++;
        for (Node nd : n.f15.nodes) {
            nd.accept(this);
        }
        vaporRet.add(indent() + "ret");
        indentCount--;
        currentClass = null;
        //classScope.clear();
        currentMethod = null;

    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    public void visit(TypeDeclaration n) {
        n.f0.accept(this);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public void visit(ClassDeclaration n) {
        //String currentClass = n.f1.f0.toString();
        currentClass = n.f1.f0.toString();
        //classScope.push(currentClass);
        for (Node nd : n.f4.nodes) {
            nd.accept(this);
        }
        //classScope.clear();
        currentClass = null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public void visit(ClassExtendsDeclaration n) {
        //String currentClass = n.f1.f0.toString();
        //classScope.push(currentClass);
        currentClass = n.f1.f0.toString();
        for (Node nd : n.f6.nodes) {
            nd.accept(this);
        }
        //classScope.clear();
        currentClass = null;
    }


    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public void visit(MethodDeclaration n) {
        vaporRet.add("\n");
        indentCount = 0;
        //VClass parentClass = getClass(classScope.get(0));
        VClass parentClass = getClass(currentClass);
        String method = n.f2.f0.toString();
        currentMethod = method;
        ArrayList<VTable> methods = parentClass.getMemberMethods();

        ArrayList<String> params = new ArrayList<>();
        for (VTable vt : methods) {
            if (vt.getMethodName() == method) {
                params = vt.getParams();
            }
        }
        String paramList = "";
        if (params.size() > 0) {
            for (String p : params) {
                paramList += " ";
                paramList += p;
            }
        }
        vaporRet.add("func " + parentClass.getClassName() + "." + method + "(this" + paramList + ")");

        temporalCount = 0;  // reset for each function

        indentCount++;
        n.f8.accept(this);
        n.f10.accept(this);
        String result = checkExpr();
        vaporRet.add(indent() + "ret " + result);
        indentCount--;
        currentMethod = null;
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     */
    public void visit(Statement n) {
        n.f0.accept(this);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public void visit(Block n) {
        n.f1.accept(this);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    // handle lhs and let the Expression() handles the rhs
    // need to know whether the id is a field or a local/param
    // if it is local, easy, just need to use its name as var name
    // if it is a field, need to load from memory
    public void visit(AssignmentStatement n) {
        n.f0.accept(this);
        String id = n.f0.f0.toString();
        n.f2.accept(this);
        String expr = checkExpr();

        if (isVar(id)) { //local variable - can use its name directly
            vaporRet.add(indent() + id + " = " + expr);

        } else { // filed - need to load from memory and store in register
            // get field offset
            int offset = getFieldIndex(id);
            //System.out.println("Offset: " + offset);
            vaporRet.add(indent() + "[this + " + offset + "] = " + expr);
        }
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    // handle lhs and let the Expression() handles the rhs
    public void visit(ArrayAssignmentStatement n) {
        String id = n.f0.f0.toString();
        String t1 = getLabel("temporal");

        // Out of Bound check
        String load;

        // load array base address from memory
        if (isVar(id)) {
            load = t1 + " = [" + id + "]"; //s = [b]where b is the base address
        } else { //field
            int offset = getFieldIndex(id);
            load = t1 + " = [this + " + offset + "]";
        }

        vaporRet.add(indent() + load);
        nullPtr(t1); // check null pointer

        String t2 = getLabel("temporal");
        String t3 = getLabel("temporal");

        n.f2.accept(this);
        String index = checkExpr();

        String length = t2 + " = [" + t1 + "]"; // get the length of array i = [s]
        String indexCheck = t3 + " = LtS(" + index + " " + t2 + ")"; //ok = LtS(i, s) where i is the index

        vaporRet.add(indent() + length);
        vaporRet.add(indent() + indexCheck);
        outOfBound(t3);

        indexCheck = t3 + " = LtS(-1 " + index + ")";
        vaporRet.add(indent() + indexCheck);
        outOfBound(t3);

        // setup of array
        String t4 = getLabel("temporal");
        vaporRet.add(indent() + t4 + " = MulS(" + index + " 4)"); //o = MulS(i 4)
        vaporRet.add(indent() + t4 + " = Add (" + t1 + " " + t4 + ")"); // d = Add(b, o)

        n.f5.accept(this);
        String result = checkExpr();

        // assignment
        vaporRet.add(indent() + "[" + t4 + " + 4] = " + result); // [d + 4] = Expression();
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public void visit(IfStatement n) {
        n.f2.accept(this);
        String cond = checkExpr();

        String ifBegin = getLabel("ifBegin");
        String ifEnd = getLabel("ifEnd");
        vaporRet.add(indent() + "if0 " + cond + " goto :" + ifBegin); // goto else statement
        indentCount++;
        n.f4.accept(this);
        vaporRet.add(indent() + "goto :" + ifEnd);
        indentCount--;
        vaporRet.add(indent() + ifBegin + ":");
        indentCount++;
        n.f6.accept(this);
        indentCount--;
        vaporRet.add(indent() + ifEnd + ":");
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public void visit(WhileStatement n) {
        String whileTop = getLabel("wTop");
        String whileEnd = getLabel("wEnd");
        vaporRet.add(indent() + whileTop + ": ");
        indentCount++;
        n.f2.accept(this);
        String cond = checkExpr();

        vaporRet.add(indent() + "if0 " + cond + " goto :" + whileEnd);
        indentCount++;
        n.f4.accept(this);
        vaporRet.add(indent() + "goto :" + whileTop);
        indentCount--;
        vaporRet.add(indent() + whileEnd + ": ");

    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public void visit(PrintStatement n) {
        n.f2.accept(this);
        String expr = checkExpr();

        vaporRet.add(indent() + "PrintIntS(" + expr + ")");
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | PrimaryExpression()
     */
    public void visit(Expression n) {
        n.f0.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */

    /* two if0 instructions that check
       if each of the operands is zero,
       assign zero to the result and goto
       the end label. Before the end label,
       one is assigned to the result.*/
    public void visit(AndExpression n) {
        n.f0.accept(this);
        String lhs = checkExpr();

        String ifBegin1 = getLabel("ifBegin");
        String ifEnd1 = getLabel("ifEnd");
        vaporRet.add(indent() + "if0 " + lhs + " goto :" + ifEnd1);
        indentCount++;

        String ifBegin2 = getLabel("ifBegin");
        String ifEnd2 = getLabel("ifEnd");
        n.f2.accept(this);
        String rhs = checkExpr();

        vaporRet.add(indent() + "if0 " + rhs + " goto :" + ifEnd1);
        indentCount++;
        String result = getLabel("temporal"); // hold the result
        vaporRet.add(indent() + result + " = 1");
        vaporRet.add(indent() + "goto :" + ifEnd2);
        indentCount--;
        indentCount--;
        vaporRet.add(indent() + ifEnd1 + ": ");
        indentCount++;
        vaporRet.add(indent() + result + " = 0");
        indentCount--;
        vaporRet.add(indent() + ifEnd2 + ": ");

        setLastExpr(result, null);

    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public void visit(CompareExpression n) {
        n.f0.accept(this);
        String lhs = checkExpr();

        n.f2.accept(this);
        String rhs = checkExpr();

        String result = getLabel("temporal");
        vaporRet.add(indent() + result + " = " + "LtS(" + lhs + " " + rhs + ")");

        setLastExpr(result, null);

    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public void visit(PlusExpression n) {
        n.f0.accept(this);
        String lhs = checkExpr();

        n.f2.accept(this);
        String rhs = checkExpr();

        setLastExpr("Add(" + lhs + " " + rhs + ")", "EXPR");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public void visit(MinusExpression n) {
        n.f0.accept(this);
        String lhs = checkExpr();

        n.f2.accept(this);
        String rhs = checkExpr();

        setLastExpr("Sub(" + lhs + " " + rhs + ")", "EXPR");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public void visit(TimesExpression n) {
        n.f0.accept(this);
        String lhs = checkExpr();

        n.f2.accept(this);
        String rhs = checkExpr();

        setLastExpr("MulS(" + lhs + " " + rhs + ")", "EXPR");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public void visit(ArrayLookup n) {
        n.f0.accept(this);
        String lhs = checkExpr();
        nullPtr(lhs);

        n.f2.accept(this);
        String index = checkExpr();

        String t1 = getLabel("temporal");
        String t2 = getLabel("temporal");

        // Out of Bound check
        String load;
        load = t1 + " = [" + lhs + "]"; //s = [b]where b is the base address
        String indexCheck;
        indexCheck = t2 + " = LtS(" + index + " " + t1 + ")"; //ok = LtS(i, s) where i is the index

        vaporRet.add(indent() + load);
        nullPtr(t1);//check address load

        vaporRet.add(indent() + indexCheck);
        outOfBound(t2);// ensure index < size

        indexCheck = t2 + " = LtS(-1 " + index + ")";
        vaporRet.add(indent() + indexCheck);
        outOfBound(t2); // ensure index > 0

        // setup of array
        String t3 = getLabel("temporal");
        vaporRet.add(indent() + t3 + " = MulS(" + index + " 4)"); //o = MulS(i 4)
        vaporRet.add(indent() + t3 + " = Add (" + lhs + " " + t3 + ")"); // d = Add(b, o)

        setLastExpr("[" + t3 + " + 4]", "EXPR");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    // The array length instruction is simply translated to an access to the array base address.
    public void visit(ArrayLength n) {
        n.f0.accept(this); // return the temporal label?
        String expr = checkExpr();
        nullPtr(expr);
        setLastExpr("[" + expr + "]", "EXPR");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public void visit(MessageSend n) {
        n.f0.accept(this);
        String id = checkExpr();
        if (id != "this") {
            nullPtr(id);
        }
        String curClass = "";
        if (id == "this") {
            curClass = currentClass;
        } else {
            curClass = tempClass;
        }

        String method = n.f2.f0.toString();
        VTable vt = getMethod(method, curClass);
        // function return type is a class object
        if (getClass(vt.getReturnType()) != null) {
            tempClass = vt.getReturnType();
        }

        String offset = Integer.toString(getMethodIndex(method, curClass));
        // load class from memory and get the method
        String t1 = getLabel("temporal");
        String result = getLabel("temporal");
        vaporRet.add(indent() + t1 + " = [" + id + "]");
        vaporRet.add(indent() + t1 + " = [" + t1 + " + " + offset + "]");
        String str = "";

        if (n.f4.present()) {
            n.f4.accept(this);
            for (String p : arguList) {
                str += " ";
                str += p;
            }
            arguList.clear(); //clear the argument list
        }
        vaporRet.add(indent() + result + " = call " + t1 + "(" + id + str + ")");
        setLastExpr(result, null);
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public void visit(ExpressionList n) {
        n.f0.accept(this);
        String expr = checkExpr();
        arguList.add(expr);
        if (n.f1.present()) {
            n.f1.accept(this);
        }
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public void visit(ExpressionRest n) {
        n.f1.accept(this);
        String expr = checkExpr();
        arguList.add(expr);
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | NotExpression()
     * | BracketExpression()
     */
    public void visit(PrimaryExpression n) {
        n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public void visit(IntegerLiteral n) {
        setLastExpr(n.f0.toString(), null);
    }

    /**
     * f0 -> "true"
     */
    public void visit(TrueLiteral n) {
        setLastExpr("1", null);
    }

    /**
     * f0 -> "false"
     */
    public void visit(FalseLiteral n) {
        setLastExpr("0", null);
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public void visit(Identifier n) {
        String id = n.f0.toString();

        if (isClass(id)) {
            tempClass = id;
            setLastExpr(id, null);
        } else if (getMethod(id, currentClass) != null) {
            String ret = (getMethod(id, currentClass)).getReturnType();
            if (getClass(ret) != null) {
                tempClass = ret;
            }
            setLastExpr(id, null);
        } else if (isVar(id)) {
            for (VTable method : (getClass(currentClass)).getMemberMethods()) {
                if (method.getMethodName() == currentMethod) {
                    if (method.getType(id) != null) {
                        if (getClass(method.getType(id)) != null) {
                            tempClass = method.getType(id);
                        }
                    }
                }
            }
            setLastExpr(id, null);
        } else { // get field offset and pass up register name
            int offset = getFieldIndex(id);
            setLastExpr("[this + " + offset + "]", "EXPR");
            VClass vc = getClass(currentClass);
            if (getClass(vc.getType(id)) != null) {
                tempClass = vc.getType(id);
            }
        }
    }

    /**
     * f0 -> "this"
     */
    public void visit(ThisExpression n) {
        // retrieve information from currentClass
        setLastExpr("this", null);
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public void visit(ArrayAllocationExpression n) {
        n.f3.accept(this);
        String size = checkExpr();

        String indexCheck;
        String t1 = getLabel("temporial");
        indexCheck = t1 + " = LtS(-1" + " " + size + ")";
        vaporRet.add(indent() + indexCheck);
        outOfBound(t1);

        hasAlloc = true;
        setLastExpr("call :AllocArray(" + size + ")", "EXPR");
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    // class object must be allocated before use
    public void visit(AllocationExpression n) {
        String allocClass = n.f1.f0.toString();
        tempClass = allocClass;
        String size = getClassSize(allocClass);

        String result = getLabel("temporal");
        vaporRet.add(indent() + result + " = HeapAllocZ" + "(" + size + ")");
        nullPtr(result);
        vaporRet.add(indent() + "[" + result + "] = :vmt_" + allocClass);
        setLastExpr(result, null);
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public void visit(NotExpression n) {
        n.f1.accept(this);
        String expr = checkExpr();
        String result = getLabel("temporal");
        vaporRet.add(indent() + result + " = Sub(1 " + expr + ")");
        setLastExpr(result, null);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public void visit(BracketExpression n) {
        n.f1.accept(this);
    }

}
