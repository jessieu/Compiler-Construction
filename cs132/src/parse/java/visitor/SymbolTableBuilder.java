package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.*;
import java.io.*;

/**
 * Construct a symbol table for later typr check reference.
 * Will check classes, methods, and variables name repeated.
 */
public class SymbolTableBuilder extends GJDepthFirst<String,TypeInfo> {
//    //
//    // Auto class visitors--probably don't need to be overridden.
//    //
//    public String visit(NodeList n, TypeInfo argu) {
//        String _ret=null;
//        int _count=0;
//        for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//            e.nextElement().accept(this,argu);
//            _count++;
//        }
//        return _ret;
//    }
//
//    public String visit(NodeListOptional n, TypeInfo argu) {
//        if ( n.present() ) {
//            String _ret=null;
//            int _count=0;
//            for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//                e.nextElement().accept(this,argu);
//                _count++;
//            }
//            return _ret;
//        }
//        else
//            return null;
//    }
//
//    public String visit(NodeOptional n, TypeInfo argu) {
//        if ( n.present() )
//            return n.node.accept(this,argu);
//        else
//            return null;
//    }
//
//    public String visit(NodeSequence n, TypeInfo argu) {
//        String _ret=null;
//        int _count=0;
//        for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//            e.nextElement().accept(this,argu);
//            _count++;
//        }
//        return _ret;
//    }
//
//    public String visit(NodeToken n, TypeInfo argu) { return null; }

    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, TypeInfo argu) {
        String _ret=null;
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
    public String visit(MainClass n, TypeInfo argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        String className = n.f1.f0.toString();
        ClassInfo classTable = new ClassInfo(className, null);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        MethodsInfo methodTable = new MethodsInfo("main","VOID");
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        String varName = n.f11.f0.toString();
        if(!methodTable.addLocals(varName, new TypeInfo("ARRAY"))){
            System.out.println("MainClass ERROR");
        }
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, methodTable);
        n.f15.accept(this, methodTable);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        classTable.addMethods(methodTable);
        SymbolTable st = SymbolTable.getInstance();
        if(!st.addClass(classTable)){
            System.out.println("MainClass ERROR");
        }
        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, TypeInfo argu) {
        String _ret=null;
        n.f0.accept(this, argu);
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
    public String visit(ClassDeclaration n, TypeInfo argu) {
        String _ret=null;
        n.f0.accept(this, argu);

        String className = n.f1.f0.toString();
        ClassInfo classTable = new ClassInfo(className, null);
        n.f2.accept(this, argu);
        n.f3.accept(this, classTable);
        n.f4.accept(this, classTable);
        n.f5.accept(this, argu);
        SymbolTable st = SymbolTable.getInstance();
        if (!st.addClass(classTable)){
            System.out.println("ClassDeclaration ERROR");
        }
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
    public String visit(ClassExtendsDeclaration n, TypeInfo argu) {
        String _ret=null;

        n.f0.accept(this, argu);

        String className = n.f1.f0.toString();
        ClassInfo classTable = new ClassInfo(className, null);

        n.f2.accept(this, argu);

        String parentClassName = n.f3.f0.toString();
        classTable.setParent(parentClassName);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, classTable);
        n.f6.accept(this, classTable);
        n.f7.accept(this, argu);

        SymbolTable st = SymbolTable.getInstance();
        if (!st.addClass(classTable)){
            System.out.println("ClassExtendsDeclaration ERROR");
        }
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, TypeInfo argu) {
        String _ret=null;
        String varType = n.f0.accept(this,argu);
        String varName = n.f1.f0.toString();
        // check the varDeclaration belongs to class or method
        if (argu instanceof ClassInfo){
            if(!((ClassInfo)argu).addFields(varName, new TypeInfo(varType))){
                System.out.println("VarDeclaration ERROR");
            }
        }else if (argu instanceof MethodsInfo){
            if (!((MethodsInfo)argu).addLocals(varName, new TypeInfo(varType))){
                System.out.println("VarDeclaration ERROR");
            }
        }
        n.f2.accept(this, argu);
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
    public String visit(MethodDeclaration n, TypeInfo argu) {
        String _ret = null;
        n.f0.accept(this, argu);

        String mType = n.f1.accept(this, argu);
        String mName = n.f2.f0.toString();

        MethodsInfo mTable = new MethodsInfo(mName, mType);
        String parentClass = ((ClassInfo)argu).getClassName();
        mTable.setParentClassName(parentClass);

        n.f3.accept(this, mTable);
        n.f4.accept(this, mTable);
        n.f5.accept(this, mTable);
        n.f6.accept(this, mTable);
        n.f7.accept(this, mTable);
        n.f8.accept(this, mTable);
        n.f9.accept(this, mTable);
        n.f10.accept(this, mTable);
        n.f11.accept(this, mTable);
        n.f12.accept(this, mTable);
        if (!((ClassInfo)argu).addMethods(mTable)){
            System.out.println("MethodDeclaration ERROR");
        }
        return _ret;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, TypeInfo argu) {
        String _ret=null;
        String varType = n.f0.accept(this, argu);
        String varName = n.f1.f0.toString();
        if(!((MethodsInfo)argu).addLocals(varName, new TypeInfo(varType))){
            System.out.println("FormalParameter ERROR");
        }
        return _ret;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, TypeInfo argu) {
        String _ret=null;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, TypeInfo argu) {
        // return the type back to the upper level
        String _ret = "ARRAY";
        return _ret;
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, TypeInfo argu) {
        String _ret = "BOOLEAN";
        return _ret;
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, TypeInfo argu) {
        String _ret = "INTEGER";
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, TypeInfo argu) {
        String _ret=null;
        _ret = n.f0.toString(); // return the name of identifier
        return _ret;
    }

}
