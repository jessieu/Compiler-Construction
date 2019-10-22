package symboltable;
import syntaxtree.*;
import java.util.*;

// symTable is a list of hashmaps, each hashmap store a scope
public class SymbolTable {
    ArrayList<HashMap<String, ClassInfo>> symTable;

    private static SymbolTable instance;

    private SymbolTable(){
        symTable = new ArrayList<HashMap<String, ClassInfo>>();
    }

    // singleton -- only one symbol table
    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    // Traverse the table and look for the scope name with this name
    // if contains the same name, then check the type
    // same type, return true
    // else, return false
    public boolean isRepeated(String name){
        for (int i =0; i<symTable.size(); i++) {
            HashMap<String, ClassInfo> classTable = (HashMap<String, ClassInfo>) symTable.get(i);
            Set<String> key = classTable.keySet();
            Iterator iter = key.iterator();
            while (iter.hasNext()) {
                String className = (String)iter.next();
                if (className == name){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSubtype(String child, String parent){
        while (child != parent) {
            ClassInfo childClass = lookUp(child);
            child = childClass.getParentClassName();
            if (child == null)
                return false;
        }
        return true;
    }

    // Add the new scope into symTable
    // check the name in the first table
    // if not exist, create a new one and insert to the first table of the list
    // else, error
    public boolean addClass(ClassInfo classTable){
        if (!isRepeated(classTable.getClassName())){
            if (classTable.hasParent){
                if (!isRepeated((classTable.getParentClassName()))){
                    return false; // parent doesn't declare
                }
            }
            HashMap <String, ClassInfo> classObj = new HashMap<>();
            classObj.put(classTable.getClassName(), classTable);
            symTable.add(classObj);
            return true;
        }
        return false;
    }

    public ClassInfo lookUp(String name){
        for (int i =0; i<symTable.size(); i++) {
            HashMap<String, ClassInfo> classTable = (HashMap<String, ClassInfo>) symTable.get(i);
            Set<String> key = classTable.keySet();
            Iterator iter = key.iterator();
            while (iter.hasNext()) {
                String className = (String)iter.next();
                ClassInfo classData = classTable.get(className);
                if (className == name){
                    return classData;
                }
            }
        }
        return null;
    }

    // Need to double check whether it works or not
    public boolean acyclic(String className, String parentClassName){
        String parent = parentClassName;
        ClassInfo p = lookUp(parent);
        while (p != null){
            if (p.isHasParent()){
                p = lookUp(p.getParentClassName());
                if (p.getParentClassName() == className){
                    return false;
                }
            }
        }
        return true;
    }

    // if there's a function name in child the same as the parent's
    // the return type and the parameter list must be the same
    public boolean isOverloading(String className, String parentClassName){
        // get the class information
        HashMap<String, MethodsInfo> m1 = ((ClassInfo)lookUp(className)).memberMethods;
        HashMap<String, MethodsInfo> m2 = ((ClassInfo)lookUp(parentClassName)).memberMethods;

        // traverse c1's method and c2's
        // if there are same name methods
        // check the length of their parameter lists
        // must be the same lenght, then check each parameter type in order
        for (String childName : m1.keySet()){
            for (String parentName : m2.keySet()){
                if (childName.equals(parentName)){
                    HashMap<String, TypeInfo> childParams = (m1.get(childName)).getParams();
                    HashMap<String, TypeInfo> parentParams = (m2.get(childName)).getParams();
                    if (childParams.size() != parentParams.size()){
                        return true; // overloading
                    }else { // check parameter type
                        List<TypeInfo> child = new ArrayList<TypeInfo>(childParams.values());
                        List<TypeInfo> parent = new ArrayList<TypeInfo>(parentParams.values());

                        for (int i = 0; i < child.size(); i++){
                            if ((child.get(i).getTypeName()).equals(parent.get(i).getTypeName())){
                                continue;
                            }else {
                                return true; // type not match => overloading
                            }

                        }

                    }
                }
            }
        }
        return false;
    }

    public void printTable() {
        for (int i = 0; i < symTable.size(); i++) {
            HashMap<String, ClassInfo> classTable = (HashMap<String, ClassInfo>) symTable.get(i);
            Set<String> key = classTable.keySet();
            Iterator iter = key.iterator();
            while (iter.hasNext()) {
                String className = (String) iter.next();
                System.out.println("Class name: " + className);
                (classTable.get(className)).printClass();
            }
        }


    }
}
