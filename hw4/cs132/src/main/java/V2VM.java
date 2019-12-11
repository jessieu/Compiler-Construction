import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

import java.io.IOException;
import java.io.InputStreamReader;

public class V2VM {
    public static VaporProgram parseVapor() throws IOException {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram program;
        try {
            program = VaporParser.run(new InputStreamReader(System.in), 1, 1,
                    java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);
        } catch (ProblemException ex) {
            System.err.println(ex.getMessage());
            return null;
        }

        return program;
    }

    public static void main(String[] args) {
        try {
            VaporProgram program = parseVapor();
            VMTranslator translator = new VMTranslator(program);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
