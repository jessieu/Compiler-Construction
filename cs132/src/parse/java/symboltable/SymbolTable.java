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
            return true;
        }
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
