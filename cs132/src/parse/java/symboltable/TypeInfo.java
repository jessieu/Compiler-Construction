package symboltable;
import syntaxtree.*;

public class TypeInfo{
    String typeName;

    public TypeInfo(String name){
        typeName = name;
    }
    public String getTypeName(){
        return typeName;
    }
/*
 *         ArrayType()
 *       | BooleanType()
 *       | IntegerType()
 *       | Identifier()
 */

}