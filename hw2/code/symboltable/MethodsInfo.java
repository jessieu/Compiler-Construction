package symboltable;
import syntaxtree.*;
import java.util.*;

public class MethodsInfo extends TypeInfo{
    String methodName; // method name
    String parentClassName;     // class name it belongs to ===> used to determine p.m
    ArrayList<String> paramsType;
    HashMap<String, TypeInfo> locals; // local variables

    public MethodsInfo(String name,String ty){
        super(name, ty);
        methodName = name;
        paramsType = new ArrayList<>();
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

    public String getReturnType(){
        return super.getTypeName();
    }

    public boolean addLocals(String lName, TypeInfo ty){
        if (locals.containsKey(lName)){
            return false;
        }
        locals.put(lName, ty);
        return true;
    }

    public ArrayList<String> getParamsType() {
      return paramsType;
    }

    public HashMap<String, TypeInfo> getLocals() {
        return locals;
    }

    public TypeInfo getLocals(String localName){
        return locals.get(localName);
    }

    public String isInitialized(String varName){
        String ty = null;
        if (getLocals(varName) == null){
            // check whether it is initialized at parent class
            SymbolTable st = SymbolTable.getInstance();

            ClassInfo parentTable = st.lookUp(parentClassName);
            if (parentTable.getFields(varName) != null){
                ty = ((TypeInfo)parentTable.getFields(varName)).getTypeName();
            }else {
                if (parentTable.isHasParent()){
                    String parentClass = parentTable.getParentClassName();
                    if (parentClass != null){
                        ty = ((st.lookUp(parentClass)).getFields(varName)).getTypeName();
                    }
                }
            }
        }else { // in local
            ty = ((TypeInfo)getLocals(varName)).getTypeName();
        }
        return ty;
    }

    public void addPType(String ty){
        paramsType.add(ty);
    }

    public boolean matchedParams(ArrayList<String> pList){
        // for (String ty: paramsType){
        //     System.out.println("paramsType: " + ty);
        // }
        
        boolean _ret = true;
        if (paramsType.size() != pList.size()){
            // System.out.println("size not equal");
            _ret = false;
        }else {
            if (paramsType.size() == 0){
                _ret = true;
            }
            for (int i = 0; i < paramsType.size(); i++){
                // System.out.println("Inside MethodsInfo -- paramsType: " + paramsType.get(i));
                // System.out.println("Inside MethodsInfo -- pList: " + pList.get(i));
                if (!isEqual(pList.get(i), paramsType.get(i))){
                    _ret = false;
                }
            }
        }
        // System.out.println(_ret);
        return _ret;
    }

    public boolean isEqual(String lhs, String rhs){
        SymbolTable st = SymbolTable.getInstance();
        // System.out.println("Left: " + lhs);
        // System.out.println("Right: " + rhs);
        if ((st.lookUp(lhs) != null) && (st.lookUp(rhs) != null)){
            if (st.isSubtype(lhs, rhs)){
                return true;
            }
        }else {
            if (lhs == rhs){
                return true;
            }
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