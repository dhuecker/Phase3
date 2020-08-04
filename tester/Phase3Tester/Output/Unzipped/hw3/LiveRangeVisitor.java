

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.*;

public class LiveRangeVisitor <E extends Throwable> extends Visitor<E> {

    VFunction currFunction;
    List<CFGNode> nodes; // Each line is a node

    public void setCurrFunction(VFunction currFunction) {
        this.currFunction = currFunction;
        nodes = new ArrayList<>();

        // Set up nodes
        for (int i = 0; i < currFunction.body.length + currFunction.labels.length; i++) {
            CFGNode curr = new CFGNode(i);
            if (i != (currFunction.body.length + currFunction.labels.length) - 1)
            curr.addSingleSucc(i);
            nodes.add(curr);
        }
    }

    // For debugging
    public void inspect() {
        System.out.println(currFunction.ident);
        for (CFGNode node : nodes) {
            System.out.println("node " + node.index + ": ");
            node.inspect();
        }
    }

    public int getRelativePos(int sourcePos) {
        return (sourcePos - currFunction.sourcePos.line) - 1;
    }

    public void printCFG() {
        for (CFGNode node : nodes) {
            for (int succ : node.succ) {
                System.out.println(node.index + " -> " + succ);
            }
        }
    }

    public CFGNode getNodeFromIndex(int index) {
        for (CFGNode node : nodes) {
            if (node.index == index)
                return node;
        }

        return null;
    }

    // Computes live ranges on CFGNodes in nodes list
    public void computeNodeSets() {
        // Add function header data into CFGNodes
        CFGNode funcHeader = new CFGNode(-1);
        for (int i = 0; i < currFunction.params.length; i++) {
            funcHeader.def.add(currFunction.params[i].ident);
        }
        funcHeader.addSingleSucc(-1);
        nodes.add(0, funcHeader);

        do {
            for (int i = 0; i < nodes.size(); i++) {
                CFGNode currNode = nodes.get(i);

                currNode.inPrime = new TreeSet<>();
                currNode.inPrime.addAll(currNode.in);
                currNode.outPrime = new TreeSet<>();
                currNode.outPrime.addAll(currNode.out);

                // out[n] - def[n]
                Set<String> diff = new TreeSet<>(currNode.out);
                diff.removeAll(currNode.def);
                // use[n] u (out[n] - def[n])
                Set<String> newIn = new TreeSet<>(currNode.use);
                newIn.addAll(diff);
                currNode.in = new TreeSet<>();
                currNode.in.addAll(newIn);

                Set<String> newOut = new TreeSet<>(currNode.out);
                for (int j = 0; j < currNode.succ.size(); j++) {
                    newOut.addAll(getNodeFromIndex(currNode.succ.get(j)).in);
                }
                currNode.out = new TreeSet<>();
                currNode.out.addAll(newOut);
            }

        } while(!converged());
    }

    boolean converged() {
        for (CFGNode node : nodes) {
            if (!(node.inPrime.equals(node.in) && node.outPrime.equals(node.out)))
                return false;
        }
        return true;
    }

    LiveRange getRangeByIdent(String ident, List<LiveRange> ranges) {

        for (LiveRange range : ranges) {
            if (range.ident.equals(ident)) {
                return range;
            }
        }

        return null;
    }

    // Creates data structure to be used later
    public LiveRanges getCurrRanges() {
        List<LiveRange> finalRanges = new ArrayList<>();
        List<LiveRange> incompleteRanges = new ArrayList<>();

        computeNodeSets();

        // active[n] = in[n] union def[n]
        for (CFGNode node : nodes) {
            node.active = new TreeSet<>(node.in);
            node.active.addAll(node.def);
        }

        for (CFGNode node : nodes) {
            // Create or extend ranges
            for(String ident : node.active) {
                LiveRange temp = getRangeByIdent(ident, incompleteRanges);
                if (temp == null) {
                    incompleteRanges.add(new LiveRange(node.index, node.index, ident));
                } else {
                    temp.end++;
                }
            }

            if (node.index == 194) {
                System.out.print("");
            }

            // Add a terminated range to finalRanges
            for (int i = 0; i < incompleteRanges.size(); i++) {
               // if (!node.active.contains(incompleteRanges.get(i).ident)) {
                    finalRanges.add(incompleteRanges.get(i));
                    incompleteRanges.remove(i);
                //}
            }
        }

        // Add nodes that are active until the end
        for (LiveRange r : incompleteRanges) {
            r.end++;
            finalRanges.add(r);
        }

        // Make a conservative approximation and union all live ranges
        // for each variable
        List<LiveRange> duplicates = new ArrayList<>();
        int i = 0;
        for (LiveRange r : finalRanges) {
            for (int j = i; j < finalRanges.size(); j++) {
                LiveRange c = finalRanges.get(j);
                if (r != c && r.ident.equals(c.ident)) {
                    r.start = Math.min(r.start, c.start);
                    r.end = Math.max(r.end, c.end) + 1;
                    duplicates.add(c);
                }
            }
            i++;
        }

        for (LiveRange dup : duplicates) {
            finalRanges.remove(dup);
        }

        return new LiveRanges(finalRanges);
    }

    public void visit(VAssign a) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(a.sourcePos.line));

        // defs
        currNode.def.add(a.dest.toString());

        // uses
        if (a.source instanceof VVarRef) {
            currNode.use.add(a.source.toString());
        }

        currNode.addSingleSucc(getRelativePos(a.sourcePos.line));
    }

    public void visit(VCall c) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(c.sourcePos.line));

        // defs
        currNode.def.add(c.dest.toString());

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.add(c.args[i].toString());
            }
        }
        currNode.use.add(c.addr.toString());

        currNode.addSingleSucc(getRelativePos(c.sourcePos.line));
    }

    public void visit(VBuiltIn c) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(c.sourcePos.line));

        // defs
        if (c.dest != null) {
            currNode.def.add(c.dest.toString());
        }

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.add(c.args[i].toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(c.sourcePos.line));
    }

    public void visit(VMemWrite w) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(w.sourcePos.line));

        // defs

        // uses
        if (w.dest instanceof VMemRef.Global) {
            if (((VMemRef.Global) w.dest).base instanceof VAddr.Var) {
                currNode.use.add(((VMemRef.Global) w.dest).base.toString());
            }
        }

        if (w.source instanceof VVarRef) {
            currNode.use.add(w.source.toString());
        }

        currNode.addSingleSucc(getRelativePos(w.sourcePos.line));
    }

    public void visit(VMemRead r) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(r.sourcePos.line));

        // defs
        currNode.def.add(r.dest.toString());

        // uses
        if (r.source instanceof VMemRef.Global) {
            if (((VMemRef.Global) r.source).base instanceof VAddr.Var) {
                currNode.use.add(((VMemRef.Global) r.source).base.toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(r.sourcePos.line));
    }

    public void visit(VBranch b) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(b.sourcePos.line));

        // defs

        // uses
        currNode.use.add(b.value.toString());

        currNode.addSingleSucc(getRelativePos(b.sourcePos.line));
        int targetPos = b.target.getTarget().sourcePos.line;
        currNode.succ.add(getRelativePos(targetPos));
    }

    public void visit(VGoto g) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(g.sourcePos.line));

        // defs

        // uses

        int targetPos = ((VAddr.Label)g.target).label.getTarget().sourcePos.line;
        currNode.succ.add(getRelativePos(targetPos));
    }

    public void visit(VReturn r) throws E {
        CFGNode currNode = getNodeFromIndex(getRelativePos(r.sourcePos.line));

        // defs

        // uses
        if (r.value instanceof VVarRef) {
            currNode.use.add(r.value.toString());
        }

        // Does not have any successors
    }

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

        public void addSingleSucc(int pos) {
            if (!succ.contains(pos+1))
                succ.add(pos + 1);
        }
    }
}
