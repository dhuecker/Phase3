

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr;

import java.util.List;
import java.util.ArrayList;

public class TrVisitor<E extends Throwable> extends VInstr.Visitor<E> {

    //setup data members for TrVisitor below
    RegAllocation currentAllocation;
    VFunction currentFunction;

    List<String> bufferVisitor;
    int indentLevel;
    List<String> takenSXRegs;
    List<String> takenTXRegs;
    int outCounter;

    //TrVisitor simple constructor below
    public TrVisitor() {
        bufferVisitor = new ArrayList<>();
    }

    //helping methods for Tr below
    public void setBuffer(int loc, String x) {
        StringBuilder tempBuilder = new StringBuilder(x);
        for (int a = 0; a < indentLevel * 4; a++)
            tempBuilder.insert(0, " ");
        x = tempBuilder.toString();

        if (x.charAt(x.length() - 1) != '\n')
            bufferVisitor.set(loc, x + "\n");
        else
            bufferVisitor.set(loc, x);
    }

    public void printBuffer() {
        for (String temp : bufferVisitor) {
            System.out.print(temp);
        }
        System.out.println();
    }

    public void insertLabels() {
        for (int a = 0; a < currentFunction.labels.length; a++) {
            int locTemp = getRelativeLoc(currentFunction.labels[a].sourcePos.line);
            setBuffer(locTemp, currentFunction.labels[a].ident + ":\n");
        }

        // Set up function header
        StringBuilder fHeader = new StringBuilder();


        int inCounter = Math.max(currentFunction.params.length - 4, 0);
        // Save all $sx  registers
        int sxCounter = takenSXRegs.size();
        int txCounter = takenTXRegs.size();
        fHeader = new StringBuilder("func " + currentFunction.ident + " [in " + inCounter + ", out " + outCounter + ", local " + (sxCounter + txCounter) + "]\n");
        // Save $sx registers
        int stackLocation = 0;
        for (String regTemp : takenSXRegs) {
            fHeader.append("local[").append(stackLocation).append("] = ").append(regTemp).append("\n");
            stackLocation++;
        }

        // Unload argument registers onto local variables
        int argRegsTaken = 0;
        for (int a = 0; a < currentFunction.params.length; a++) {
            LiveRange currentArg = currentAllocation.getAlloc(-1, currentFunction.params[a].ident);
            if (argRegsTaken < inCounter + 1 || inCounter == 0) {
                if (currentArg != null) {
                    fHeader.append(currentArg.getPlace()).append(" = ").append("$a").append(a).append("\n");
                }
                argRegsTaken++;
            } else {
                fHeader.append(currentArg.getPlace()).append(" = ").append("in[").append(a- argRegsTaken).append("]\n");
            }
        }

        bufferVisitor.add(0, fHeader.toString());
    }

    public void setData(VFunction currentFunc, RegAllocation currentAlloc) {
        this.currentFunction = currentFunc;
        this.currentAllocation = currentAlloc;

        takenSXRegs = new ArrayList<>();
        for (LiveRange lrange : currentAlloc.rangesLive.getRangesLive()) {
            if (lrange.getPlace().contains("s")) {
                if (!takenSXRegs.contains(lrange.getPlace()))
                    takenSXRegs.add(lrange.getPlace());
            }
        }

        takenTXRegs = new ArrayList<>();
        for (LiveRange trange : currentAlloc.rangesLive.getRangesLive()) {
            if (trange.getPlace().contains("t")) {
                if (!takenTXRegs.contains(trange.getPlace()))
                    takenTXRegs.add(trange.getPlace());
            }
        }

        outCounter = 0;


        // Set up bufferVisitor
        bufferVisitor = new ArrayList<>();
        for (int a = 0; a <= currentFunc.body.length + currentFunc.labels.length; a++) {
            bufferVisitor.add("");
        }
    }

    public int getRelativeLoc(int srcLoc) {
        return (srcLoc - currentFunction.sourcePos.line) - 1;
    }
    
    //visit methods below


    public void visit(VAssign x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, x.dest.toString());
        LiveRange sAlloc = currentAllocation.getAlloc(sourceLocation, x.source.toString());

        String line = "";
        if (dAlloc != null) {
            line += dAlloc.getPlace();
        }

        line += " = ";

        if (sAlloc != null) {
            line += sAlloc.getPlace();
        } else {
            line += x.source.toString();
        }

        setBuffer(sourceLocation, line);
    }


    public void visit(VCall x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, x.dest.toString());

        String lineTemp = "";

        // Save usedTs
        for (int a = 0; a < takenTXRegs.size(); a++) {
            lineTemp += "local[" + (takenSXRegs.size() + a) + "] = " + takenTXRegs.get(a) + "\n";
        }

        // Set up arguments
        int argRegTaken = 0;
        for (int a = 0; a < x.args.length; a++) {
            if (argRegTaken < 4) {
                if (x.args[a] instanceof VVarRef) {
                    LiveRange currentArgAlloc = currentAllocation.getAlloc(sourceLocation, x.args[a].toString());
                    if (currentArgAlloc != null)
                        lineTemp += "$a" + a + " = " + currentArgAlloc.getPlace() + "\n";
                } else if (x.args[a] instanceof VOperand.Static) {
                    lineTemp += "$a" + a + " = " + x.args[a].toString() + "\n";
                } else if (x.args[a] instanceof VLitStr) {
                    lineTemp += "\"" + ((VLitStr) x.args[a]).value + "\"";
                }
                argRegTaken++;
            } else {
                if (x.args[a] instanceof VVarRef) {
                    LiveRange currentArgAlloc = currentAllocation.getAlloc(sourceLocation, x.args[a].toString());
                    if (currentArgAlloc != null)
                        lineTemp += "out[" + outCounter + "] = " + currentArgAlloc.getPlace() + "\n";
                } else if (x.args[a] instanceof VOperand.Static) {
                    lineTemp += "out[" + outCounter + "] = " + x.args[a].toString() + "\n";
                } else if (x.args[a] instanceof VLitStr) {
                    lineTemp += "\"" + ((VLitStr) x.args[a]).value + "\"";
                }
                outCounter++;
            }
        }

        if (x.addr instanceof VAddr.Label) {
            lineTemp += "call :" + ((VAddr.Label<VFunction>) x.addr).label.ident + "\n";
        } else {
            LiveRange addressAlloc = currentAllocation.getAlloc(sourceLocation, x.addr.toString());
            lineTemp += "call " + addressAlloc.getPlace() + "\n";
        }

        // Restore Ts
        for (int a = 0; a < takenTXRegs.size(); a++) {
            lineTemp += takenTXRegs.get(a) + " = local[" + (takenSXRegs.size() + a) + "]\n";
        }

        // Get the return value
        if (dAlloc != null) {
            lineTemp += dAlloc.getPlace();
        }

        lineTemp += " = $v0";

        setBuffer(sourceLocation, lineTemp);
    }

    public void visit(VBuiltIn x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);

        String lineTemp = "";

        // Sometimes BuiltIn does not have a dest
        if (x.dest != null) {
            LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, x.dest.toString());

            if (dAlloc != null) {
                lineTemp += dAlloc.getPlace();
            }

            lineTemp += " = ";
        }

        lineTemp += x.op.name + "(";

        for (int a = 0; a < x.args.length; a++) {
            if (a != 0) lineTemp += " ";
            if (x.args[a] instanceof VVarRef) {
                LiveRange currentArgAlloc = currentAllocation.getAlloc(sourceLocation, x.args[a].toString());
                if (currentArgAlloc != null)
                    lineTemp += currentArgAlloc.getPlace();
            } else if (x.args[a] instanceof VOperand.Static) {
                lineTemp += x.args[a].toString();
            } else if (x.args[a] instanceof VLitStr) {
                lineTemp += "\""+ ((VLitStr)x.args[a]).value + "\"";
            }
        }

        lineTemp += ")";

        setBuffer(sourceLocation, lineTemp);
    }

    public void visit(VMemWrite x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, ((VMemRef.Global) x.dest).base.toString());
        LiveRange sAlloc = currentAllocation.getAlloc(sourceLocation, x.source.toString());

        String lineTemp = "";
        if (dAlloc != null) {
            lineTemp += "[" + dAlloc.getPlace() + "+" + ((VMemRef.Global) x.dest).byteOffset + "]";
        }

        lineTemp += " = ";

        if (sAlloc != null) {
            lineTemp += sAlloc.getPlace();
        } else {
            lineTemp += x.source.toString();
        }

        setBuffer(sourceLocation, lineTemp);
    }

    public void visit(VMemRead x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, x.dest.toString());
        LiveRange sAlloc = currentAllocation.getAlloc(sourceLocation, ((VMemRef.Global) x.source).base.toString());

        String lineTemp = "";
        if (dAlloc != null) {
            lineTemp += dAlloc.getPlace();
        }

        lineTemp += " = ";

        if (sAlloc != null) {
            lineTemp += "[" + sAlloc.getPlace() + "+" + ((VMemRef.Global) x.source).byteOffset + "]";
        } else {
            lineTemp += "[" +x.source.toString() + "]";
        }

        setBuffer(sourceLocation, lineTemp);
    }

    public void visit(VBranch x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        LiveRange dAlloc = currentAllocation.getAlloc(sourceLocation, x.value.toString());

        String lineTemp = "";
        if (dAlloc != null) {
            if (x.positive) {
                lineTemp += "if " + dAlloc.getPlace() + " goto :" + x.target.ident;
            } else {
                lineTemp += "if0 " + dAlloc.getPlace() + " goto :" + x.target.ident;
            }
        }

        setBuffer(sourceLocation, lineTemp);
    }

    public void visit(VGoto x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);
        setBuffer(sourceLocation, "goto " + x.target.toString());
    }

    public void visit(VReturn x) throws E {
        int sourceLocation = getRelativeLoc(x.sourcePos.line);

        StringBuilder retTempString = new StringBuilder();

        if (x.value != null) {
            if (x.value instanceof VVarRef) {
                LiveRange rAlloc = currentAllocation.getAlloc(sourceLocation, x.value.toString());
                if (rAlloc != null)
                    retTempString.append("$v0 = ").append(rAlloc.getPlace()).append("\n");
            } else if (x.value instanceof VOperand.Static) {
                retTempString.append("$v0 = ").append(x.value.toString()).append("\n");
            }
        }
        // Restore used sx registers
        int counterTemp = 0;
        for (String regTemp : takenSXRegs) {
            retTempString.append(regTemp).append(" = local[").append(counterTemp).append("]\n");
            counterTemp++;
        }

        retTempString.append("ret");

        setBuffer(sourceLocation, retTempString.toString());
        insertLabels();
    }
}
//end TrVisitor class