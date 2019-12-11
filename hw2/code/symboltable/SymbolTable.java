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

    public boolean acyclic(String className){
        int size = symTable.size();

        Stack<String> stack = new Stack<String>(); 
        stack.push(className);
        for (int i = 0; i < size; i++){
            ClassInfo classTable = lookUp(className);
            String parentName = classTable.getParentClassName();
            if (parentName == null){
                return true;
            }else {
                if (stack.contains(parentName)){
                //System.out.println(parentName);
                return false;
            }else {
                stack.push(parentName);
                className = parentName;
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
        // check the parameter list
        for (String childName : m1.keySet()){
            // System.out.println("Child function name: " + childName);
            for (String parentName : m2.keySet()){
                // System.out.println("Parent function name: " + parentName);
                if (childName.equals(parentName)){
                    MethodsInfo mTable = m1.get(childName);
                    ArrayList<String> rhsParams = (m2.get(childName)).getParamsType();
                    return !mTable.matchedParams(rhsParams);
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