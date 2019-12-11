import cs132.vapor.ast.*;

import java.util.*;

public class MipsTranslator extends VInstr.Visitor {
    ArrayList<String> mipsRet = new ArrayList<>();
    int indentCount = 0;

    public MipsTranslator(VaporProgram program) {
        printData(program.dataSegments);
        printText();
        printFunction(program.functions);
        printMips();
    }

    public String indent() {
        String indentations = "";
        for (int i = 0; i < indentCount; i++) {
            indentations += "  ";
        }

        return indentations;
    }

    public void printData(VDataSegment[] vData) {
        mipsRet.add(".data");
        indentCount++;
        for (VDataSegment vd : vData) {
            mipsRet.add(indent() + vd.ident + ":");
            indentCount++;
            for (VOperand vo : vd.values) {
                String ref = vo.toString();
                ref = ref.substring(1, ref.length());
                mipsRet.add(indent() + ref);
            }
            indentCount--;
        }
        indentCount--;
        mipsRet.add("\n");
    }

    public void printText() {
        mipsRet.add(".text");
        indentCount++;
        mipsRet.add(indent() + "jal Main");
        mipsRet.add(indent() + "li $v0 10");
        mipsRet.add(indent() + "syscall");
        mipsRet.add("\n");
        indentCount--;
    }

    public void printFunction(VFunction[] functions) {
        for (VFunction vf : functions) {
            mipsRet.add(vf.ident + ":");
            indentCount++;
            //save fp to location $sp - 8 ...each word is 4 bytes
            mipsRet.add(indent() + "sw $fp -8($sp)");
            // Move fp to sp
            mipsRet.add(indent() + "move $fp $sp");
            // pushing the frame
            // Decrease $sp by size = (Local + Out + 2) * 4
            int size = (vf.stack.local + vf.stack.out + 2) * 4;
            mipsRet.add(indent() + "subu $sp $sp " + size);

            mipsRet.add(indent() + "sw $ra -4($fp)");

            // visit function body
            ArrayList<VCodeLabel> codeLabels = new ArrayList<VCodeLabel>(Arrays.asList(vf.labels));
            for (VInstr instr : vf.body) {
                while (!codeLabels.isEmpty() && codeLabels.get(0).sourcePos.line < instr.sourcePos.line) {
                    indentCount--;
                    mipsRet.add(indent() + codeLabels.get(0).ident + ":");
                    indentCount++;
                    codeLabels.remove(0);
                }

                instr.accept(this);
            }
            // Restore the return register $ra (in the function calls any other function)
            mipsRet.add(indent() + "lw $ra -4 ($fp)");
            //Restore fp
            mipsRet.add(indent() + "lw $fp -8 ($fp)");
            // Popping the frame
            mipsRet.add(indent() + "addu $sp $sp " + size);
            // Jumping to the return register
            mipsRet.add(indent() + "jr $ra");
            mipsRet.add("\n");
            indentCount--;
        }

        mipsRet.add("_print:\n" +
                "  li $v0 1   # syscall: print integer\n" +
                "  syscall\n" +
                "  la $a0 _newline\n" +
                "  li $v0 4   # syscall: print string\n" +
                "  syscall\n" +
                "  jr $ra\n" +
                "\n" +
                "_error:\n" +
                "  li $v0 4   # syscall: print string\n" +
                "  syscall\n" +
                "  li $v0 10  # syscall: exit\n" +
                "  syscall\n" +
                "\n" +
                "_heapAlloc:\n" +
                "  li $v0 9   # syscall: sbrk\n" +
                "  syscall\n" +
                "  jr $ra\n" +
                "\n" +
                ".data\n" +
                ".align 0\n" +
                "_newline: .asciiz \"\\n\"\n" +
                "_str0: .asciiz \"null pointer\\n\"");

    }

    public void printMips() {
        for (String mips : mipsRet) {
            System.out.println(mips);
        }
    }


    /* ----------------------------------------------- Visitor ----------------------------------------------- */

    // This is only used for assignments of simple operands to
    // registers and local variables.
    @Override
    public void visit(VAssign a) {
        String dest = a.dest.toString();
        String source = a.source.toString();
        String instruction = "";
        if (a.source instanceof VLitInt) {
            instruction = "li";
        } else if (a.source instanceof VLabelRef) {
            instruction = "la ";
            source = substring(1, source.length());
        } else {
            instruction = "move";
        }
        mipsRet.add(indent() + instruction + " " + dest + " " + source);
    }

    @Override
    public void visit(VBranch b) {
        String instruction = "";
        if (b.positive) {
            instruction = "bnez";
        } else {
            instruction = "beqz";
        }
        String target = b.target.toString();
        target = target.substring(1, target.length());
        mipsRet.add(indent() + instruction + " " + b.value.toString() + " " + target);
    }

    @Override
    public void visit(VBuiltIn c) {
        String operation = c.op.name;

        String args = "";
        String type = "u";

        if (c.dest != null) {
            // Muls, Add, LtS, Sub, HeapAlloc
            if (operation == "HeapAllocZ" || operation == "HeapAlloc") {
                String instruction = "move";
                if (c.args[0] instanceof VLitInt) {
                    instruction = "li";
                }
                mipsRet.add(indent() + instruction + " $a0 " + c.args[0]);
                mipsRet.add(indent() + "jal _heapAlloc");
                mipsRet.add(indent() + "move " + c.dest + " $v0");
            } else {
                boolean hasLiteral = false;
                VOperand fstArg = c.args[0];
                VOperand sndArg = c.args[1];
                if (fstArg instanceof VLitInt) {
                    hasLiteral = true;
                    mipsRet.add(indent() + "li $t9 " + fstArg.toString());
                    args += "$t9";
                } else {
                    args += fstArg.toString();
                }
                args += " ";
                args += sndArg.toString();

                if (operation == "LtS") {
                    operation = "slt";
                    type = "i";
                } else if (operation == "MulS") {
                    operation = "mul";
                } else if (operation == "Sub") {
                    operation = "sub";
                } else { // should be add
                    operation = "add";
                }
                if (hasLiteral || sndArg instanceof VLitInt) {
                    mipsRet.add(indent() + operation + type + " " + c.dest.toString() + " " + args);
                } else {
                    mipsRet.add(indent() + operation + " " + c.dest.toString() + " " + args);
                }
            }
        } else {
            args = c.args[0].toString();
            // error, printIntS
            if (operation == "Error") {
                mipsRet.add(indent() + "la $a0 _str0");
                mipsRet.add(indent() + "j _error");
            } else if (operation == "PrintIntS") {
                String instruction = "";
                if (c.args[0] instanceof VLitInt) {
                    instruction = "li";
                } else {
                    instruction = "move";
                }
                mipsRet.add(indent() + instruction + " $a0 " + args);
                mipsRet.add(indent() + "jal _print");
            }
        }

    }

    @Override
    public void visit(VCall c) {
        int argLength = c.args.length;
        String args = "";
        int sp = 0; // may or may not use it

        for (int i = 0; i < argLength; i++) {
            if (i < 4) {
                if (c.args[i] instanceof VLitInt) {
                    mipsRet.add(indent() + "li $a " + i + c.args[i].toString());
                } else {
                    mipsRet.add(indent() + "move $a " + i + c.args[i].toString());
                }
            } else {
                mipsRet.add(indent() + "li $t9 " + c.args[i]);
                mipsRet.add(indent() + "sw $t9 " + Integer.toString(sp) + "($sp)");
                sp += 4;
            }
        }
        String address = c.addr.toString();
        String instruction = "jalr";
        if (c.addr instanceof VAddr.Label) {
            address = c.addr.toString();
            address = address.substring(1, address.length());
            instruction = "jal";
        }
        mipsRet.add(indent() + instruction + " " + address);
        if (c.dest != null) {
            mipsRet.add(indent() + "move " + c.dest.toString() + " " + "$v0");
        }
    }

    @Override
    public void visit(VGoto g) {
        String target = g.target.toString();
        target = target.substring(1, target.length());
        mipsRet.add(indent() + "j " + target);
    }

    @Override
    public void visit(VMemRead r) {
        String dest = r.dest.toString();
        String baseAddr = "";
        String offset = "";

        if (r.source instanceof VMemRef.Global) {
            baseAddr = ((VMemRef.Global) r.source).base.toString();
            offset = Integer.toString(((VMemRef.Global) r.source).byteOffset);
            mipsRet.add(indent() + "lw " + dest + " " + offset + "(" + baseAddr + ")");
        } else { // stack
            VMemRef.Stack stack = (VMemRef.Stack) r.source;

            if ((stack.region.toString()).equals("In")) {
                baseAddr = "$fp";
            } else {
                baseAddr = "$sp";
            }
            offset = Integer.toString(stack.index * 4);
            mipsRet.add(indent() + "lw " + dest + " " + offset + "(" + baseAddr + ")");

        }
    }

    @Override
    public void visit(VMemWrite w) {
        String dest = w.dest.toString();
        if (w.dest instanceof VMemRef.Global) {
            String baseAddr = ((VMemRef.Global) w.dest).base.toString();
            String offset = Integer.toString(((VMemRef.Global) w.dest).byteOffset);
            if (w.source instanceof VLabelRef) {
                String label = w.source.toString();
                label = label.substring(1, label.length());
                mipsRet.add(indent() + "la $t9 " + label);
                mipsRet.add(indent() + "sw $t9 " + offset + "(" + baseAddr + ")");
            } else { // register
                String src = w.source.toString();
                if (w.source instanceof VLitInt) {
                    mipsRet.add(indent() + "li $t9 " + src);
                    mipsRet.add(indent() + "sw $t9 " + offset + "(" + baseAddr + ")");
                } else {
                    mipsRet.add(indent() + "sw " + src + " " + offset + "(" + baseAddr + ")");
                }

            }
        } else { // stack
            VMemRef.Stack stack = (VMemRef.Stack) w.dest;
            String offset = Integer.toString(stack.index * 4);
            String src = w.source.toString();
            if (w.source instanceof VLitInt) {
                mipsRet.add(indent() + "li $t9 " + src);
                mipsRet.add(indent() + "sw $t9 " + offset + "($sp)");
            } else {
                mipsRet.add(indent() + "sw " + src + " " + offset + "($sp)");
            }
        }

    }

    @Override
    public void visit(VReturn r) {
        if (r.value != null) {
            if (r.value instanceof VLitInt) {
                mipsRet.add(indent() + "li $v0 " + r.value.toString());
            } else {
                mipsRet.add(indent() + "move $v0 " + r.value.toString());
            }
        }
    }
}
