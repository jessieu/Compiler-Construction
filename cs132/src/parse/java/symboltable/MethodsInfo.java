package symboltable;
import syntaxtree.*;
import java.util.HashMap;

public class MethodsInfo extends TypeInfo{
    String methodName; // method name
    String parentClassName;     // class name it belongs to ===> used to determine p.m
    HashMap<String, TypeInfo> params;
    HashMap<String, TypeInfo> locals; // local variables

    public MethodsInfo(String name,String t){
        super(t);
        methodName = name;
        params = new HashMap<>();
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

    public boolean addParams(String pName, TypeInfo ty){
        if (params.get(pName) != null){
            return false; // repeated local variable name
        }
        params.put(pName, ty);
        return true;
    }

    public boolean addLocals(String lName, TypeInfo ty){
        if (locals.get(lName) != null || params.get(lName) != null){
            return false; // repeated local variable name/parameter name
        }
        locals.put(lName, ty);
        return true;
    }

    public HashMap<String, TypeInfo> getParams(){
        return params;
    }

    public TypeInfo getParams(String pName){
        return params.get(pName);
    }

    public HashMap<String, TypeInfo> getLocals() {
        return locals;
    }

    public TypeInfo getLocals(String localName){
        return locals.get(localName);
    }

    public String isInitialized(String varName){
        String ty = null;
        if (getLocals(varName) == null && getParams(varName) == null){
            // check whether it is initialized at parent class
            SymbolTable st = SymbolTable.getInstance();
            ClassInfo parentTable = st.lookUp(parentClassName);

            if (parentTable.getFields(varName) != null){
                ty = ((TypeInfo)parentTable.getFields(varName)).getTypeName();
            }
        }else {
            if (getLocals(varName) != null){
                ty = ((TypeInfo)getLocals(varName)).getTypeName();
            }else if (getParams(varName) != null){
                ty = ((TypeInfo)getParams(varName)).getTypeName();
            }
        }
        return ty;
    }

    public void printParams(){
        for (String name : params.keySet())
            System.out.println("Parameter Name: " + name);

        // using values() for iteration over keys
        for (TypeInfo ty : params.values())
            System.out.println("Parameter Type: " + ty.getTypeName());
    }

    public void printLocal(){
        for (String name : locals.keySet())
            System.out.println("Local Variable Name: " + name);

        // using values() for iteration over keys
        for (TypeInfo ty : locals.values())
            System.out.println("Local Variable Type: " + ty.getTypeName());
    }
}