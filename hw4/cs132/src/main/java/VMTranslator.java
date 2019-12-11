import cs132.vapor.ast.*;

import java.util.*;

public class VMTranslator extends VInstr.Visitor {
    // Temporay storage of function body translation
    ArrayList<String> vaporMRet = new ArrayList<>();

    // Hold all vaporM translation result
    ArrayList<String> vaporM = new ArrayList<>();

    // a map map local variable name to local stack position
    HashMap<String, String> localMap = new HashMap<>();

    ArrayList<String> freeRegisters = new ArrayList<>();
    ArrayList<String> callerSave = new ArrayList<>();
    ArrayList<String> calleeSave = new ArrayList<>();
    ArrayList<String> arguReg = new ArrayList<>();

    int indentCount = 0;
    int inForCallee = 0;
    int outForCaller = 0;
    int locals = 0;


    public VMTranslator(VaporProgram program) {
        // set free registers
        for (int i = 0; i < 8; i++) {
            calleeSave.add("$s" + i);
        }
        for (int i = 0; i < 9; i++) {
            callerSave.add("$t" + i);
        }

        for (int i = 0; i < 4; i++) {
            arguReg.add("$a" + i);
        }

        freeRegisters.addAll(callerSave);
        freeRegisters.addAll(calleeSave);
        freeRegisters.addAll(arguReg);

        printData(program.dataSegments);
        printFunction(program.functions);

        printVaporM();

    }

    public String getFreeRegister() {
        String register = "";
        // This should be always true
        if (freeRegisters.size() > 0) {
            register = freeRegisters.get(0);
            freeRegisters.remove(0);
        } else { // should not reach here
            System.err.println("No available register");
        }

        return register;
    }

    // either return the variable stack location or assign a new location
    public String getStoreLocation(String varName) {
        String location = localMap.get(varName);

        if (location == null) {
            location = "local[" + locals + "]";
            locals++;
            localMap.put(varName, location);
        }

        return location;
    }

    // VOperand usually appear on the right hand side
    // of assignments and as arguments to built-in operation and function calls.
    // Direct Known Subclasses: VLitStr, VOperand.Static, VVarRef
    public String getOperand(VOperand operand) {
        String result = "";

        // VLitStr
        if (operand instanceof VLitStr) {
            // just return the literal sting
            result = ((VLitStr) operand).toString();
        } else if (operand instanceof VVarRef.Local) {     // we don't care about register in this assignment
            // store all locals in stack
            String localVar = ((VVarRef.Local) operand).toString();
            // since operands appear on the rhs,
            // it guarantees local variables are in the local map
            // otherwise it does not make sense
            String location = getStoreLocation(localVar);
            result = getFreeRegister();
            // load from memory
            vaporMRet.add(indent() + result + " = " + location);
        } else {     // Static
            // VLabelRef
            if (operand instanceof VLabelRef) {
                result = ((VLabelRef) operand).toString();
            }
            //VLitInt
            if (operand instanceof VLitInt) {
                // just return the literal integer
                result = ((VLitInt) operand).toString();
            }
        }

        return result;
    }

    public void resetRegister() {
        // reset registers
        freeRegisters.clear();
        freeRegisters.addAll(callerSave);
        freeRegisters.addAll(calleeSave);
        freeRegisters.addAll(arguReg);
    }

    // reset for every function
    public void reset() {
        locals = 0;
        indentCount = 0;
        inForCallee = 0;
        outForCaller = 0;

        localMap.clear();
        vaporMRet.clear();

        resetRegister();

    }

    // for debug
    public String indent() {
        String indentations = "";
        for (int i = 0; i < indentCount; i++) {
            indentations += "  ";
        }

        return indentations;
    }

    public void printData(VDataSegment[] vData) {
        for (VDataSegment vd : vData) {
            vaporM.add("const " + vd.ident);
            indentCount++;
            for (VOperand vo : vd.values) {
                vaporM.add(indent() + getOperand(vo));
            }
        }

        vaporMRet.add("\n");
    }

    public void printFunction(VFunction[] functions) {
        for (VFunction vf : functions) {
            // get the function body first
            indentCount++;

            if (vf.params.length != 0) {
                String loadFromArgu = "";

                for (int i = 0; i < vf.params.length; i++) {
                    if (i < 4) {
                        String location = getStoreLocation(vf.params[i].toString());
                        loadFromArgu = indent() + location + " = $a" + i;
                    } else {
                        inForCallee = vf.params.length - 4;
                        String tempLocation = getFreeRegister();
                        String location = getStoreLocation(vf.params[i].toString());
                        vaporMRet.add(indent() + tempLocation + " = in[" + (i - 4) + "]");
                        loadFromArgu = indent() + location + " = " + tempLocation;
                    }

                    vaporMRet.add(loadFromArgu);
                }
                resetRegister();
            }

            ArrayList<VCodeLabel> codeLabels = new ArrayList<VCodeLabel>(Arrays.asList(vf.labels));
            for (VInstr instr : vf.body) {
                while (!codeLabels.isEmpty() && codeLabels.get(0).sourcePos.line < instr.sourcePos.line) {
                    indentCount--;

                    vaporMRet.add(indent() + codeLabels.get(0).ident + ":");
                    indentCount++;
                    codeLabels.remove(0);
                }

                instr.accept(this);
                resetRegister();
            }
            indentCount--;

            vaporM.add("func " + vf.ident + "[in " + inForCallee + ", out " + outForCaller + ", local " + locals + "]");
            vaporM.addAll(vaporMRet);

            reset();
        }
    }

    public void printVaporM() {
        for (String str : vaporM) {
            System.out.println(str);
        }
    }

    /* --------- Debug -------- */
    public void printLocalMap() {
        if (!localMap.isEmpty()) {
            for (String key : localMap.keySet()) {
                System.out.println(key + " " + localMap.get(key));
            }
        }
    }

    public void printFreeRegister() {
        System.out.println("Free Register List: ");
        for (String reg : freeRegisters) {
            System.out.println(reg);
        }
    }


    /* ----------------------------------------------- Visitor ----------------------------------------------- */

    // This is only used for assignments of simple operands to
    // registers and local variables.
    @Override
    public void visit(VAssign a) {
        // a.source is a VOperand, check type
        String dest = getStoreLocation(a.dest.toString());
        String src = getOperand(a.source);

        vaporMRet.add(indent() + dest + " = " + src);
    }

    @Override
    public void visit(VBranch b) {
        // b.value is VOperand, check type
        String value = getOperand(b.value);

        if (b.positive) {
            vaporMRet.add(indent() + "if " + value + " goto " + b.target.toString());
        } else {
            vaporMRet.add(indent() + "if0 " + value + " goto " + b.target.toString());
        }

        indentCount++;
    }

    @Override
    public void visit(VBuiltIn c) {
        String operation = c.op.name;

        String args = "";
        for (VOperand arg : c.args) {
            args += " ";
            args += getOperand(arg);
        }

        // Has assignment
        if (c.dest != null) {
            String dest = getStoreLocation(c.dest.toString());
            String tempLocation = getFreeRegister();

            vaporMRet.add(indent() + tempLocation + " = " + operation + "(" + args + " )");
            vaporMRet.add(indent() + dest + " = " + tempLocation);
        } else {
            vaporMRet.add(indent() + operation + "(" + args + " )");
        }

    }

    @Override
    public void visit(VCall c) {
        int argLength = c.args.length;
        for (int i = 0; i < argLength; i++) {
            String arg = getOperand(c.args[i]);

            if (i < 4) {
                vaporMRet.add(indent() + "$a" + i + " = " + arg);
            } else {
                vaporMRet.add(indent() + "out[" + (i - 4) + "] = " + arg);
                outForCaller++;
            }
        }

        String fLocation;
        String tempReg = getFreeRegister();
        if (c.addr instanceof VAddr.Label) {
            fLocation = ((VAddr.Label) c.addr).toString();
            
        } else { // asign a register to variable
            fLocation = getStoreLocation(c.addr.toString());
        }

        vaporMRet.add(indent() + tempReg + " = " + fLocation);
        vaporMRet.add(indent() + "call " + temp);

        String retVal = c.dest.toString();
        // function has return value - retrieve it from $v0
        if (retVal != null) {
            String retLocation = getStoreLocation(retVal);
            vaporMRet.add(indent() + retLocation + " = $v0");
        }


    }

    @Override
    public void visit(VGoto g) {
        vaporMRet.add(indent() + "goto " + g.target.toString());
        indentCount--;
    }

    @Override
    public void visit(VMemRead r) {
        // vapor code does not have stack reference
        VMemRef.Global mRef = (VMemRef.Global) r.source;
        // Variable reference
        String baseAddr = getStoreLocation((mRef.base).toString());
        String offset = Integer.toString(mRef.byteOffset);
        String tempLocation = getFreeRegister();

        String src = "";

        vaporMRet.add(indent() + tempLocation + " = " + baseAddr);

        // load from memory
        if (offset.equals("0")) {
            src = "[" + tempLocation + "]";
        } else {
            src = "[" + tempLocation + " + " + offset + "]";
        }

        String dest = getStoreLocation(r.dest.toString());
        String temp = getFreeRegister();

        vaporMRet.add(indent() + temp + " = " + src);
        vaporMRet.add(indent() + dest + " = " + temp);
    }

    @Override
    public void visit(VMemWrite w) {
        String src = getOperand(w.source);
        String dest = "";
        VMemRef.Global mRef = (VMemRef.Global) w.dest;

        String offset = Integer.toString(mRef.byteOffset);

        String tempLocation = getFreeRegister();
        String location = getStoreLocation(mRef.base.toString());

        vaporMRet.add(indent() + tempLocation + " = " + location);
        if (offset.equals("0")) {
            dest = "[" + tempLocation + "]";
        } else {
            dest = "[" + tempLocation + " + " + offset + "]";
        }
        // need to double check make sure the it indeed write to memory
        vaporMRet.add(indent() + dest + " = " + src);

    }

    @Override
    public void visit(VReturn r) {
        if (r.value != null) {
            String retVal = getOperand(r.value);
            vaporMRet.add(indent() + "$v0 = " + retVal);
        }

        vaporMRet.add(indent() + "ret");
    }


}
