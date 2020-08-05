package registerAlloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.*;

public class LRVisitor<E extends Throwable> extends Visitor<E> {

    VFunction currentFunction;
    List<CFGNode> lineNodes; // Each line is a node

    public void setCurrentFunction(VFunction currentFunction) {
        this.currentFunction = currentFunction;
        lineNodes = new ArrayList<>();

        // Set up lineNodes
        for (int a = 0; a < currentFunction.body.length + currentFunction.labels.length; a++) {
            CFGNode current = new CFGNode(a);
            if (a != (currentFunction.body.length + currentFunction.labels.length) - 1)
            current.addSingleSucc(a);
            lineNodes.add(current);
        }
    }

    // For debugging
    public void inspect() {
        System.out.println(currentFunction.ident);
        for (CFGNode nodeTemp : lineNodes) {
            System.out.println("node " + nodeTemp.index + ": ");
            nodeTemp.inspect();
        }
    }

    public int getRelativePos(int sourcePos) {
        return (sourcePos - currentFunction.sourcePos.line) - 1;
    }

    public void printCFG() {
        for (CFGNode nodeTemp : lineNodes) {
            for (int succ : nodeTemp.succ) {
                System.out.println(nodeTemp.index + " -> " + succ);
            }
        }
    }

    public CFGNode getNodeFromIndex(int i) {
        for (CFGNode nodeTemp : lineNodes) {
            if (nodeTemp.index == i)
                return nodeTemp;
        }

        return null;
    }

    // Computes live rangesLive on CFGNodes in lineNodes list
    public void computeNodeSets() {

        CFGNode functionHeaderData = new CFGNode(-1);
        for (int a = 0; a < currentFunction.params.length; a++) {
            functionHeaderData.def.add(currentFunction.params[a].ident);
        }
        functionHeaderData.addSingleSucc(-1);
        lineNodes.add(0, functionHeaderData);

        do {
            for (int a = 0; a < lineNodes.size(); a++) {
                CFGNode currentNode = lineNodes.get(a);

                currentNode.inPrime = new TreeSet<>();
                currentNode.inPrime.addAll(currentNode.in);
                currentNode.outPrime = new TreeSet<>();
                currentNode.outPrime.addAll(currentNode.out);

                // out[n] - def[n]
                Set<String> difference = new TreeSet<>(currentNode.out);
                difference.removeAll(currentNode.def);
                // use[n] u (out[n] - def[n])
                Set<String> newInTemp = new TreeSet<>(currentNode.use);
                newInTemp.addAll(difference);
                currentNode.in = new TreeSet<>();
                currentNode.in.addAll(newInTemp);

                Set<String> newOutTemp = new TreeSet<>(currentNode.out);
                for (int b = 0; b < currentNode.succ.size(); b++) {
                    newOutTemp.addAll(getNodeFromIndex(currentNode.succ.get(b)).in);
                }
                currentNode.out = new TreeSet<>();
                currentNode.out.addAll(newOutTemp);
            }

        } while(!converged());
    }

    boolean converged() {
        for (CFGNode nodeTemp : lineNodes) {
            if (!(nodeTemp.inPrime.equals(nodeTemp.in) && nodeTemp.outPrime.equals(nodeTemp.out)))
                return false;
        }
        return true;
    }

    LiveRange getRangeByIdent(String i, List<LiveRange> r) {

        for (LiveRange rTemp : r) {
            if (rTemp.ident.equals(i)) {
                return rTemp;
            }
        }

        return null;
    }

    // Creates data structure to be used later
    public LiveRanges getCurrentRanges() {
        List<LiveRange> fRanges = new ArrayList<>();
        List<LiveRange> incRanges = new ArrayList<>();

        computeNodeSets();

        // active[n] = in[n] union def[n]
        for (CFGNode nodeTemp : lineNodes) {
            nodeTemp.active = new TreeSet<>(nodeTemp.in);
            nodeTemp.active.addAll(nodeTemp.def);
        }

        for (CFGNode nodeTemp : lineNodes) {
            // Create or extend rangesLive
            for(String ident : nodeTemp.active) {
                LiveRange temp = getRangeByIdent(ident, incRanges);
                if (temp == null) {
                    incRanges.add(new LiveRange(nodeTemp.index, nodeTemp.index, ident));
                } else {
                    temp.end++;
                }
            }

            // Add a terminated range to fRanges
            for (int a = 0; a < incRanges.size(); a++) {
                fRanges.add(incRanges.get(a));
                incRanges.remove(a);
            }
        }

        // Add lineNodes that are active until the end
        for (LiveRange r : incRanges) {
            r.end++;
            fRanges.add(r);
        }

        List<LiveRange> duplicatesList = new ArrayList<>();
        int x = 0;
        for (LiveRange r : fRanges) {
            for (int a = x; a < fRanges.size(); a++) {
                LiveRange c = fRanges.get(a);
                if (r != c && r.ident.equals(c.ident)) {
                    r.begin = Math.min(r.begin, c.begin);
                    r.end = Math.max(r.end, c.end) + 1;
                    duplicatesList.add(c);
                }
            }
            x++;
        }

        for (LiveRange dupTemp : duplicatesList) {
            fRanges.remove(dupTemp);
        }

        return new LiveRanges(fRanges);
    }

    public void visit(VAssign a) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(a.sourcePos.line));

        // defs
        currentNode.def.add(a.dest.toString());

        // uses
        if (a.source instanceof VVarRef) {
            currentNode.use.add(a.source.toString());
        }

        currentNode.addSingleSucc(getRelativePos(a.sourcePos.line));
    }

    public void visit(VCall x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // defs
        currentNode.def.add(x.dest.toString());

        // uses
        for (int a = 0; a < x.args.length; a++) {
            if (x.args[a] instanceof VVarRef) {
                currentNode.use.add(x.args[a].toString());
            }
        }
        currentNode.use.add(x.addr.toString());

        currentNode.addSingleSucc(getRelativePos(x.sourcePos.line));
    }

    public void visit(VBuiltIn x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // defs
        if (x.dest != null) {
            currentNode.def.add(x.dest.toString());
        }

        // uses
        for (int a = 0; a < x.args.length; a++) {
            if (x.args[a] instanceof VVarRef) {
                currentNode.use.add(x.args[a].toString());
            }
        }

        currentNode.addSingleSucc(getRelativePos(x.sourcePos.line));
    }

    public void visit(VMemWrite x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // uses
        if (x.dest instanceof VMemRef.Global) {
            if (((VMemRef.Global) x.dest).base instanceof VAddr.Var) {
                currentNode.use.add(((VMemRef.Global) x.dest).base.toString());
            }
        }

        if (x.source instanceof VVarRef) {
            currentNode.use.add(x.source.toString());
        }

        currentNode.addSingleSucc(getRelativePos(x.sourcePos.line));
    }

    public void visit(VMemRead x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // defs
        currentNode.def.add(x.dest.toString());

        // uses
        if (x.source instanceof VMemRef.Global) {
            if (((VMemRef.Global) x.source).base instanceof VAddr.Var) {
                currentNode.use.add(((VMemRef.Global) x.source).base.toString());
            }
        }

        currentNode.addSingleSucc(getRelativePos(x.sourcePos.line));
    }

    public void visit(VBranch x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // uses
        currentNode.use.add(x.value.toString());

        currentNode.addSingleSucc(getRelativePos(x.sourcePos.line));
        int targetLocation = x.target.getTarget().sourcePos.line;
        currentNode.succ.add(getRelativePos(targetLocation));
    }

    public void visit(VGoto x) throws E {
        CFGNode currentNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // uses

        int targetPos = ((VAddr.Label)x.target).label.getTarget().sourcePos.line;
        currentNode.succ.add(getRelativePos(targetPos));
    }

    public void visit(VReturn x) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(x.sourcePos.line));

        // uses
        if (x.value instanceof VVarRef) {
            currNode.use.add(x.value.toString());
        }

    }

    //defition of class CFG and declartion of it's members below
    class CFGNode {
        public int index;
        public Set<String> in;
        public Set<String> out;
        public Set<String> def;
        public Set<String> use;
        public Set<String> inPrime;
        public Set<String> outPrime;
        public Set<String> active;

        List<Integer> succ;

        public CFGNode(int index) {
            this.index = index;
            in = new TreeSet();
            out = new TreeSet();
            def = new TreeSet();
            use = new TreeSet();
            inPrime = new TreeSet();
            outPrime = new TreeSet();
            active = new TreeSet<>();
            succ = new ArrayList<>();
        }

        //used to inspect and check to see if algorithm is working as expected

        public void inspect() {
            System.out.print("    in:   {");
            for (String s : in) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    out:   {");
            for (String s : out) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    def:   {");
            for (String s : def) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    use:   {");
            for (String s : use) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    active:   {");
            for (String s : active) {
                System.out.print(s);
            }
            System.out.println("}");
        }

        public void addSingleSucc(int x) {
            if (!succ.contains(x+1))
                succ.add(x + 1);
        }
    }
}
//end LRVisitor class
