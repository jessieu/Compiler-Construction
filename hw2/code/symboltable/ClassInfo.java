package symboltable;
import syntaxtree.*;
import java.util.HashMap;

public class ClassInfo extends TypeInfo{
    String className;
    String parentClassName;

    boolean hasParent;

    HashMap<String, MethodsInfo> memberMethods;
    HashMap<String, TypeInfo> memberFields;

    // Initialize
    public ClassInfo(String name, String ty){
        super(name, ty);
        className = name;
        memberMethods = new HashMap<>();
        memberFields = new HashMap<>();
    }

    public void setParent(String parent, boolean hasParent){
        this.parentClassName = parent;
        this.hasParent = hasParent;

    }

    public String getClassName(){
        return className;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public boolean isHasParent() {
        return hasParent;
    }

    public MethodsInfo getMethod(String name){
        return memberMethods.get(name);
    }

    public TypeInfo getFields(String name){
        return memberFields.get(name);
    }

    public boolean addMethods(MethodsInfo methodTable){
        if (memberMethods.get(methodTable.getName()) == null){
            memberMethods.put(methodTable.getName(), methodTable);
            return true;
        }
        return false;
    }

    public boolean addFields(String name, TypeInfo ty){
        if (memberFields.get(name) == null){
            memberFields.put(name, ty);
            return true;
        }
        return false;
    }

    public void printClass(){
        for (String name : memberFields.keySet())
            System.out.println("Field Name: " + name);

        // using values() for iteration over keys
        for (TypeInfo ty : memberFields.values())
            System.out.println("Field Type: " + ty.getTypeName());

        for (String name : memberMethods.keySet())
            System.out.println("Method Name: " + name);

        // using values() for iteration over keys
        for (MethodsInfo mTable : memberMethods.values()) {
            mTable.printLocal();
        }
    }

}