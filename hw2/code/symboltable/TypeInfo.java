package symboltable;
import syntaxtree.*;

public class TypeInfo {
    String typeName;
    String name;

    public TypeInfo(String name, String ty) {
        this.name = name;
        this.typeName = ty;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getName() {
        return name;
    }


}