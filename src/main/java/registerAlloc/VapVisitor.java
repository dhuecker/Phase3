package registerAlloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

public class VapVisitor<E extends Throwable> extends Visitor<E> {
    //setup for visit methods below

    public void visit(VAssign x) throws E {
    }

    public void visit(VCall x) throws E {
    }

    public void visit(VBuiltIn x) throws E {
    }

    public void visit(VMemWrite x) throws E {
    }

    public void visit(VMemRead x) throws E {
    }

    public void visit(VBranch x) throws E {
    }

    public void visit(VGoto x) throws E {
    }

    public void visit(VReturn x) throws E {
    }
}
//end VaporVisitor class
