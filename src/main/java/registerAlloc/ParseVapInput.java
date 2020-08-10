package registerAlloc;

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class ParseVapInput {
    public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
        //declare op array that contains all legal Vapor operations
        Op[] operates = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
    //Setup for register, stack, and locals below
        String[] regs = null;
        boolean okStack = false;
        boolean okLocals = true;
    //now set up vapor input program as vtree and start try/catch block
        VaporProgram Vtree;
        try {
            Vtree = VaporParser.run(new InputStreamReader(in), 1, 1,
                    java.util.Arrays.asList(operates),
                    okLocals, regs, okStack);
        }
        catch (ProblemException ex) {
            err.println(ex.getMessage());
            return null;
        }

        return Vtree;
    }
}
//end ParseVaporInput class