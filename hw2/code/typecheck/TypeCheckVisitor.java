package typecheck;
import visitor.*;
import syntaxtree.*;
import symboltable.*;
import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class TypeCheckVisitor extends GJDepthFirst<String, TypeInfo> {
    SymbolTable st = SymbolTable.getInstance();

    private ArrayList<String> mParams = new ArrayList<String>();

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
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

    // To do: pass the main method to varDeclaration & Statement()
    // Typecheck them later
    public String visit(MainClass n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);

        String className = n.f1.f0.toString();
        ClassInfo classTable = st.lookUp(className);
        MethodsInfo mTable = classTable.getMethod("main");
        if (mTable == null) {
           
        }
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, mTable);
        n.f15.accept(this, mTable);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    // To do: get the class name
    //        retrive class table and pass it down
    public String visit(ClassDeclaration n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        // we check the repetition when add it to symboltable
        // no need to check again, just pass it down
        String className = n.f1.f0.toString();
        SymbolTable st = SymbolTable.getInstance();
        ClassInfo classTable = st.lookUp(className);
        if (classTable == null) {
            printErrMsg("Cannot find class ." + className);
        }

        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, classTable);
        n.f4.accept(this, classTable);
        n.f5.accept(this, argu);
        return _ret;
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

    // To do: check parent class exist or not
    //        check overloading
    //        check acylical
    public String visit(ClassExtendsDeclaration n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);

        String className = n.f1.accept(this, argu);
        //System.out.println("ClassName inside extends: " + className);
        ClassInfo classTable = st.lookUp(className);

        if (classTable == null) {
            printErrMsg("Class ERROR");
        }
        String parentName = n.f3.accept(this, argu);
        //System.out.println("parent ClassName inside extends: " + parentName);
        ClassInfo parentClassTable = st.lookUp(parentName);
        // parent exists or not
        if (parentClassTable == null) {
            printErrMsg("Parent Class not exits");
        } else {
            // inherience cycle
            if (!st.acyclic(className)) {
                printErrMsg("Cylical Class ERROR");
            }
            // overloading
            if (st.isOverloading(className, parentName)) {
                printErrMsg("Class Overloading ERROR");
            }
        }
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, classTable);
        n.f6.accept(this, classTable);
        n.f7.accept(this, argu);
        return _ret;
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
    // To do: check method type and return statement type
    public String visit(MethodDeclaration n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        String methodType = n.f1.accept(this, argu);
        String methodName = n.f2.accept(this, argu); // remember to modify the identifier()
        MethodsInfo mTable = ((ClassInfo) argu).getMethod(methodName);
        if (mTable == null) {
            printErrMsg("Method not declared");
        } else {
            n.f3.accept(this, argu);
            n.f4.accept(this, mTable);
            n.f5.accept(this, argu);
            n.f6.accept(this, argu);
            n.f7.accept(this, mTable);
            n.f8.accept(this, mTable);
            n.f9.accept(this, argu);
            String returnType = n.f10.accept(this, argu);

            if (!isBasicType(returnType)) { // return an identifier => lookup the type in method and parent class
                String rt = mTable.isInitialized(returnType);
                if (rt != null) {
                    _ret = rt;
                }
            } else {
                _ret = returnType;
            }
            if (!methodType.equals(_ret)) {
                printErrMsg("Method Return Type ERROR");
            }
            n.f11.accept(this, argu);
            n.f12.accept(this, argu);
        }

        return _ret;
    }


    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | Identifier()
     */
    // pass the type upward
    public String visit(Type n, TypeInfo argu) {
        String _ret = null;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, TypeInfo argu) {
        String _ret = null;
        _ret = "ARRAY";
        return _ret;
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, TypeInfo argu) {
        String _ret = null;
        _ret = "BOOLEAN";
        return _ret;
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, TypeInfo argu) {
        String _ret = null;
        _ret = "INTEGER";
        return _ret;
    }


    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    // To do: check whether the variable name declared or not
    //        check whether the type of left and right match
    public String visit(AssignmentStatement n, TypeInfo argu) {
        String _ret = null;
        String id = n.f0.f0.toString(); //identifier() returns name
        // get the type of id
        String idType = null;

        String exprType;
        n.f1.accept(this, argu);
        String exprName = n.f2.accept(this, argu); // May return a class Type or a regular Type

        if (exprName == null){
            printErrMsg("Invalid Expression");
        }
        // both are classes
        if (st.lookUp(id) != null && st.lookUp(exprName) != null) {
            if (!st.isSubtype(id, exprName)) {
                printErrMsg("Not a subtype");
            }
        } else {
            // exprName is regular type
            if (isBasicType(exprName)) {
                exprType = exprName;
            } else if (st.lookUp(exprName) == null) { //Not return basic type, then it may return a varName
                exprType = ((MethodsInfo) argu).isInitialized(exprName);
                if (exprType == null){
                    printErrMsg(exprName + " not initialized");
                }
            } else { // exprName is class name, skip lookup
                exprType = exprName;

            }

            if (isBasicType(id)) {
                idType = id;
            } else {
                if (st.lookUp(id) == null) {
                    idType = ((MethodsInfo) argu).isInitialized(id);
                } else { //classname
                    idType = id;
                }
            }

            if (idType == null) {
                printErrMsg("Identifier not initialized");
            } else {
                if (idType == "ARRAY") {
                    // System.out.println("ID type1: " + idType);
                    // System.out.println("expr type1: " + exprType);
                    if (exprType != "INTEGER") {
                        printErrMsg("Assignment type not match");
                    }
                } else if (!st.isSubtype(exprType, idType)){
                    if (!idType.equals(exprType)) {
                    // System.out.println("ID type2: " + idType);
                    // System.out.println("expr type2: " + exprType);
                    printErrMsg("Assignment type not match");
                }
                }
                
            }
        }
        n.f3.accept(this,argu);
        return _ret;
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
    // To do: get identifier type
    //        [experssion] type must be int
    //        expression must be the same as identifier type
    public String visit(ArrayAssignmentStatement n, TypeInfo argu) {
        String _ret = null;
        // get the type of identifier
        String idName = n.f0.accept(this, argu);
        String idType = " ";
        if (argu instanceof MethodsInfo) {
            idType = ((MethodsInfo) argu).isInitialized(idName);
        } else if (argu instanceof ClassInfo) {
            idType = (((ClassInfo) argu).getFields(idName)).getTypeName();
        } else {
            printErrMsg("Unknown argument type");
        }

        if (idType != "ARRAY") {
            printErrMsg("Expect an array, but get " + idType);
        }

        n.f1.accept(this, argu);
        String indexName = n.f2.accept(this, argu);
        String indexType = " ";
        if (!isBasicType(indexType)) {
            if (argu instanceof MethodsInfo) {
                indexType = ((MethodsInfo) argu).isInitialized(indexName);
            } else if (argu instanceof ClassInfo) {
                indexType = (((ClassInfo) argu).getFields(indexName)).getTypeName();
            }
        } else {
            if (indexType != "INTEGER") {
                printErrMsg("Array index is not an integer.");
            }
            indexType = indexName;
        }

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        String exprName = n.f5.accept(this, argu);
        String exprType = " ";
        if (!isBasicType(exprName)) {
            if (argu instanceof MethodsInfo) {
                exprType = ((MethodsInfo) argu).isInitialized(exprName);
            } else if (argu instanceof ClassInfo) {
                exprType = (((ClassInfo) argu).getFields(exprName)).getTypeName();
            }
        } else {
            exprType = exprName;
        }

        if (!exprType.equals("INTEGER")) {
            printErrMsg("Array Assignment Type not match");
        }
        n.f6.accept(this, argu);
        return _ret;
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
    public String visit(IfStatement n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String exprName = n.f2.accept(this, argu);
        String exprType = " ";
        if (isBasicType(exprName)) {
            exprType = exprName;
        } else {
            if (argu instanceof ClassInfo) {
                exprType = (((ClassInfo) argu).getFields(exprName)).getTypeName();
            } else if (argu instanceof MethodsInfo) {
                exprType = ((MethodsInfo) argu).isInitialized(exprName);
            }
        }
        //System.out.println(exprType);
        if (!exprType.equals("BOOLEAN")) {
            printErrMsg("If statement condition type is not boolean.");
        }
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String exprName = n.f2.accept(this, argu);
        String exprType = " ";
        if (isBasicType(exprName)) {
            exprType = exprName;
        } else {
            if (argu instanceof ClassInfo) {
                exprType = (((ClassInfo) argu).getFields(exprName)).getTypeName();
            } else if (argu instanceof MethodsInfo) {
                exprType = ((MethodsInfo) argu).isInitialized(exprName);
            }
        }
        //System.out.println(exprType);
        if (!exprType.equals("BOOLEAN")) {
            printErrMsg("If statement condition type is not boolean.");
        }
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "System,out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String exprName = n.f2.accept(this, argu);
        String exprType = " ";

        if (isBasicType(exprName)) {
            exprType = exprName;
        } else {
            if (argu instanceof ClassInfo) {
                exprType = (((ClassInfo) argu).getFields(exprName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                exprType = ((MethodsInfo) argu).isInitialized(exprName);
            } else {
                printErrMsg("Invalid argument");
            }
        }
        if (!exprType.equals("INTEGER")) {
            printErrMsg("Print statement condition type is not integer.");
        }

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
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
    public String visit(Expression n, TypeInfo argu) {
        String _ret = null;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public String visit(AndExpression n, TypeInfo argu) {
        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (lhsType != "BOOLEAN" || rhsType != "BOOLEAN") {
            printErrMsg("&& error");
        } else {
            _ret = "BOOLEAN";
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, TypeInfo argu) {
        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (lhsType != "INTEGER" || rhsType != "INTEGER") {
            printErrMsg("< error");
        }
        // System.out.println(_ret);
        return "BOOLEAN";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, TypeInfo argu) {
        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (lhsType != "INTEGER" || rhsType != "INTEGER") {
            printErrMsg("+ error");
        } else {
            _ret = "INTEGER";
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, TypeInfo argu) {
        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        // System.out.println(lhsType);
        // System.out.println(rhsType);
        if (lhsType != "INTEGER" || rhsType != "INTEGER") {
            printErrMsg("< error");
        } else {
            _ret = "INTEGER";
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, TypeInfo argu) {
        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (lhsType != "INTEGER" || rhsType != "INTEGER") {
            printErrMsg("< error");
        } else {
            _ret = "INTEGER";
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    // only integer array is allowed => return type must be INTEGER
    public String visit(ArrayLookup n, TypeInfo argu) {

        String _ret = null;

        String lhsName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String rhsName = n.f2.accept(this, argu);
        String lhsType = " ";
        String rhsType = " ";

        if (isBasicType(lhsName)) {
            lhsType = lhsName;
        } else {
            if (argu instanceof ClassInfo) {
                lhsType = (((ClassInfo) argu).getFields(lhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                lhsType = ((MethodsInfo) argu).isInitialized(lhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (isBasicType(rhsName)) {
            rhsType = rhsName;
        } else {
            if (argu instanceof ClassInfo) {
                rhsType = (((ClassInfo) argu).getFields(rhsName)).getTypeName();

            } else if (argu instanceof MethodsInfo) {
                rhsType = ((MethodsInfo) argu).isInitialized(rhsName);
            } else {
                printErrMsg("Invalid argument");
            }
        }

        if (rhsType != "INTEGER") {
            printErrMsg("ARRAY LOOKUP error");
        } else {
            _ret = "INTEGER";
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, TypeInfo argu) {
        String _ret = null;
        String exprType = n.f0.accept(this, argu);
        if (!exprType.equals("ARRAY")) {
            printErrMsg("Not an array");
        } else {
            n.f1.accept(this, argu);
            n.f2.accept(this, argu);
            _ret = "INTEGER";
        }

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    // argu is a method/class
    // check whether primaryExpression declared or not
    // get the PrimaryExpression classname
    public String visit(MessageSend n, TypeInfo argu) {
        String _ret = null;
        String exprName = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String idName = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        String exprList = n.f4.accept(this, argu);
        // System.out.println("ExprList inside MessageSend: " + exprList);

        String exprTypeName = " ";

        // find the class name
        if (st.lookUp(exprName) == null) {
            if (argu instanceof MethodsInfo){
                exprTypeName = ((MethodsInfo) argu).isInitialized(exprName);
            }else if (argu instanceof ClassInfo) {
                exprTypeName = (((ClassInfo)argu).getFields(exprName)).getTypeName();
            }

            if (exprTypeName == null) {
                printErrMsg(exprName + " not initialized inside MessageSend.");
            }

            if (st.lookUp(exprTypeName) != null) {
                MethodsInfo mTable = ((ClassInfo) st.lookUp(exprTypeName)).getMethod(idName);

                if (mTable != null) {
                    for (String ty : mParams){
                    // System.out.println("mParams1: " + mParams);
                }
                    if (!mTable.matchedParams(mParams)) {
                        printErrMsg("Parameter list does not match");
                    }
                    _ret = mTable.getReturnType();
                }

            }
        } else {
            MethodsInfo mTable = ((ClassInfo) st.lookUp(exprName)).getMethod(idName);
            if (mTable != null) {
                if (!mTable.matchedParams(mParams)) {
                    printErrMsg("Parameter list does not match");
                }
                _ret = mTable.getReturnType();
            }

        }
        mParams.clear(); //for reuse
        n.f5.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public String visit(ExpressionList n, TypeInfo argu) {
        String _ret = null;
        String exprType = n.f0.accept(this, argu);

        if (isBasicType(exprType) || st.lookUp(exprType) != null) {
            mParams.add(exprType); //global method parameters
            _ret = exprType;
        } else {
            if (argu instanceof MethodsInfo) {
                String ty = ((MethodsInfo) argu).isInitialized(exprType);
                if (ty != null) {
                    mParams.add(ty);
                    _ret = ty;
                } else {
                    printErrMsg(ty + " not initialized inside ExpressionList.");
                }
            }
        }

        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionRest n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        String exprType = n.f1.accept(this, argu);
        if (isBasicType(exprType)) {
            mParams.add(exprType); //global method parameters
            _ret = exprType;
        } else {
            if (argu instanceof MethodsInfo) {
                String ty = ((MethodsInfo) argu).isInitialized(exprType);
                if (ty != null) {
                    mParams.add(ty);
                    _ret = ty;
                } else {
                    printErrMsg(ty + " not initialized inside expressionRest.");
                }
            }
        }
        // System.out.println("Inside expressionRest: " + _ret);
        return _ret;
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
    public String visit(PrimaryExpression n, TypeInfo argu) {
        String _ret = null;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, TypeInfo argu) {
        n.f0.accept(this, argu);
        return "INTEGER";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, TypeInfo argu) {
        n.f0.accept(this, argu);
        return "BOOLEAN";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, TypeInfo argu) {
        n.f0.accept(this, argu);
        return "BOOLEAN";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, TypeInfo argu) {
        String _ret = null;
        _ret = n.f0.toString();
        return _ret;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, TypeInfo argu) {
        String _ret = null;
        if (argu instanceof ClassInfo) {
            _ret = ((ClassInfo) argu).getClassName();
        } else if (argu instanceof MethodsInfo) {
            _ret = ((MethodsInfo) argu).getParentClassName();
        }
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        String exprName = n.f3.accept(this, argu);
        if (isBasicType(exprName)){
            _ret = exprName;
        }else {
            if (argu instanceof MethodsInfo) {
            _ret = ((MethodsInfo) argu).isInitialized(exprName);
        } else if (argu instanceof ClassInfo) {
            _ret = (((ClassInfo) argu).getFields(exprName)).getTypeName();
        } else {
            printErrMsg("Unknown argument type");
        }
        }
        
        if (_ret == null){
            printErrMsg("Array not defined.");
        }
        if (!_ret.equals("INTEGER")) {
            printErrMsg("Array allocation index is not an integer.");
        }
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        _ret = n.f1.accept(this, argu); // return class name
    
        if (st.lookUp(_ret) == null) {
            printErrMsg("No such class exists. Cannot allocated.");
        }
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public String visit(NotExpression n, TypeInfo argu) {
        String _ret = null;
        String expr = n.f1.accept(this, argu);
        if (isBasicType(expr)) {
            if (expr == "BOOLEAN") {
                _ret = expr;
            }
        } else {
            if (argu instanceof MethodsInfo) {
                _ret = ((MethodsInfo) argu).isInitialized(expr);
            }
        }
        // System.out.println("Inside NotExpression: " + _ret);
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        _ret = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    public boolean isBasicType(String ty) {
        final String[] TYPE = {"ARRAY", "BOOLEAN", "INTEGER"};
        for (int i = 0; i < TYPE.length; i++) {
            if (TYPE[i] == ty) {
                return true;
            }
        }
        return false;
    }

    public void printErrMsg(String msg){
        //System.err.println(msg);
        System.out.println("Type error");
        System.exit(0);
    }

}