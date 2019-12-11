// A class record maps field names to their offsets.
package translator;

import java.util.*;

public class VClass {
    String className;
    String parent;
    boolean hasParent;
    // arraylist stores fields of the class
    ArrayList<String> memberFields;
    ArrayList<String> fTypes;
    ArrayList<VTable> memberMethods;

    public VClass(String className) {
        this.className = className;
        memberFields = new ArrayList<>();
        fTypes = new ArrayList<>();
        memberMethods = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public String getParent() {
        return parent;
    }

    public boolean isHasParent() {
        return hasParent;
    }

    public void setParent(String parent) {
        hasParent = true;
        this.parent = parent;
    }

    public ArrayList<String> getMemberFields() {
        return memberFields;
    }

    public ArrayList<VTable> getMemberMethods() {
        return memberMethods;
    }

    public String getType(String name) {
        if (memberFields.contains(name)) {
            return fTypes.get(memberFields.indexOf(name));
        }
        return null;
    }

    public void addFields(String fields, String type) {
        this.memberFields.add(fields);
        this.fTypes.add(type);
    }

    public void addMethod(VTable method) {
        memberMethods.add(method);
    }

}
