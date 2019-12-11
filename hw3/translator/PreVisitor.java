package translator;

import syntaxtree.*;
import visitor.*;

import java.util.*;

public class PreVisitor extends DepthFirstVisitor {
    public ArrayList<VClass> allClasses = new ArrayList<>();
    private static PreVisitor instance;
    private VClass currentClass;
    private VTable currentMethod;
    boolean inClass; // identify the fields add to class or method
    String currentType;


    public static PreVisitor getInstance() {
        if (instance == null) {
            instance = new PreVisitor();
        }
        return instance;
    }

    public ArrayList<VClass> getAllClasses() {
        return allClasses;
    }

    public String getType(int which) {
        switch (which) {
            case 0:
                return "ARRAY";
            case 1:
                return "BOOLEAN";
            case 2:
                return "INTEGER";
            default:
                return "IDENTIFIER";
        }
    }

    // for debug -- looks fine
    public void printClasses() {
        for (VClass vc : allClasses) {
            System.out.println(vc.getClassName());
            for (String var : vc.getMemberFields()) {
                int index = (vc.getMemberFields()).indexOf(var);
                System.out.println("Type: " + (vc.fTypes).get(index));
                System.out.println("\t" + var);
            }
            for (VTable vt : vc.getMemberMethods()) {
                System.out.println("\t" + vt.getMethodName());
                for (String mParam : vt.getParams()) {
                    int index = (vt.getParams()).indexOf(mParam);
                    System.out.println("Type: " + (vt.pTypes).get(index));
                    System.out.println("\t\t" + mParam);
                }
                for (String mLocal : vt.getLocals()) {
                    int index = (vt.getLocals()).indexOf(mLocal);
                    System.out.println("Type: " + (vt.lTypes).get(index));
                    System.out.println("\t\t" + mLocal);
                }
            }
        }
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public void visit(Goal n) {
        n.f0.accept(this);
        for (Node td : n.f1.nodes) {
            td.accept(this);
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
        String className = n.f1.f0.toString();
        VTable mainMethod = new VTable("Main", "VOID");
        VClass mainClass = new VClass(className);
        mainClass.addMethod(mainMethod);

        inClass = true;
        currentClass = mainClass;

        for (Node td : n.f14.nodes) {
            td.accept(this);
        }
        allClasses.add(currentClass);
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
        String className = n.f1.f0.toString();
        VClass vClass = new VClass(className);

        inClass = true;
        currentClass = vClass;

        for (Node td : n.f3.nodes) {
            td.accept(this);
        }
        for (Node td : n.f4.nodes) {
            td.accept(this);
        }
        allClasses.add(currentClass);
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
        String className = n.f1.f0.toString();
        VClass vClass = new VClass(className);
        String parent = n.f3.f0.toString();

        vClass.setParent(parent);

        inClass = true;
        currentClass = vClass;

        for (Node td : n.f5.nodes) {
            td.accept(this);
        }
        for (Node td : n.f6.nodes) {
            td.accept(this);
        }
        allClasses.add(currentClass);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public void visit(VarDeclaration n) {
        String type = getType(n.f0.f0.which);
        if (type == "IDENTIFIER") {
            type = ((Identifier) n.f0.f0.choice).f0.toString();
        }
        String id = n.f1.f0.toString();
        if (inClass) {
            currentClass.addFields(id, type);
        } else {
            currentMethod.addLocals(id, type);
        }
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
        String ret = getType(n.f1.f0.which);
        String methodName = n.f2.f0.toString();
        inClass = false;
        VTable method = new VTable(methodName, ret);
        currentMethod = method;

        if (n.f4.present()) {
            n.f4.accept(this);
        }

        for (Node td : n.f7.nodes) {
            td.accept(this);
        }
        currentClass.addMethod(currentMethod);
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    public void visit(FormalParameterList n) {
        n.f0.accept(this);
        for (Node td : n.f1.nodes) {
            td.accept(this);
        }

    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public void visit(FormalParameter n) {
        String type = getType(n.f0.f0.which);
        if (type == "IDENTIFIER") {
            type = ((Identifier) n.f0.f0.choice).f0.toString();
        }
        String pName = n.f1.f0.toString();
        currentMethod.addParams(pName, type);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public void visit(FormalParameterRest n) {
        n.f1.accept(this);
    }
}
