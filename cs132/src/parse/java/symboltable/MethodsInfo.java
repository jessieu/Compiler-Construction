package symboltable;
import syntaxtree.*;
import java.util.HashMap;

public class MethodsInfo extends TypeInfo{
    String methodName; // method name
    String parentClassName;     // class name it belongs to ===> used to determine p.m
    HashMap<String, TypeInfo> locals; // local variables

    public MethodsInfo(String name,String t){
        super(t);
        methodName = name;
        locals = new HashMap<>();
    }

    public String getName(){
        return methodName;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String name){
        parentClassName = name;
    }

    public boolean addLocals(String lName, TypeInfo ty){
        if (locals.get(lName) != null){
            return false; // repeated local variable name
        }
        locals.put(lName, ty);
        return true;
    }

    public boolean isInitialized(String lName, TypeInfo ty){
        if (locals.get(lName) == ty){
            return true;
        }
        return false;
    }

    public void printLocal(){
        for (String name : locals.keySet())
            System.out.println("Local Variable Name: " + name);

        // using values() for iteration over keys
        for (TypeInfo ty : locals.values())
            System.out.println("Local Variable Type: " + ty.getTypeName());
    }
}