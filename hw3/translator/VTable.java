// Maps method names of the class to their assigned offsets.
// The v-table of a subclass is a copy of the v-table of superclass that is extended for new methods of the subclass
package translator;

import java.util.*;

public class VTable {
    String methodName;
    String returnType;
    ArrayList<String> params;
    ArrayList<String> pTypes;
    ArrayList<String> locals;
    ArrayList<String> lTypes;

    public VTable(String mName, String ret) {
        methodName = mName;
        returnType = ret;
        params = new ArrayList<>();
        pTypes = new ArrayList<>();
        locals = new ArrayList<>();
        lTypes = new ArrayList<>();
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<String> getLocals() {
        return locals;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    public String getReturnType() {
        return returnType;
    }

    // get the type of local/params for later translation of identifier
    public String getType(String name) {
        if (params.contains(name)) {
            return pTypes.get(params.indexOf(name));
        } else if (locals.contains(name)) {
            return lTypes.get(locals.indexOf(name));
        }
        return null;
    }

    public void addLocals(String var, String type) {
        locals.add(var);
        lTypes.add(type);
    }

    public void addParams(String par, String type) {
        params.add(par);
        pTypes.add(type);
    }
}
