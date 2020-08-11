package registerAlloc;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.List;

public class V2VM {

    //main method below
    public static void main(String[] args) {
        allocRegs();
    }

    //called method inside of main declaration below
    public static void allocRegs() {
        try {
            VaporProgram Vtree = ParseVapInput.parseVapor(System.in, System.err);
            LRVisitor<Exception> rVisitor = new LRVisitor();

            // One set of rangesLive per function
            List<LiveRanges> rangesLive = new ArrayList<>();
            for (int a = 0; a < Vtree.functions.length; a++) {
                VFunction currentFunction = Vtree.functions[a];
                rVisitor.setCurrentFunction(currentFunction);

                for (int b = 0; b < currentFunction.body.length; b++) {
                    Vtree.functions[a].body[b].accept(rVisitor);
                }

                rangesLive.add(rVisitor.getCurrentRanges());
            }

            List<RegAllocation> allocs = new ArrayList<>();
            for (int a = 0; a < Vtree.functions.length; a++) {
                RegAllocation currentAlloc = new RegAllocation(Vtree.functions[a], rangesLive.get(a));
                currentAlloc.LinearScanRegisterAllocation();
                allocs.add(currentAlloc);
            }


            for (int a = 0; a < Vtree.dataSegments.length; a++) {
                System.out.println("const " + Vtree.dataSegments[a].ident);
                for (int b = 0; b < Vtree.dataSegments[a].values.length; b++) {
                    System.out.println("    " + Vtree.dataSegments[a].values[b]);
                }
                System.out.println("");
            }


            TrVisitor<Exception> trVisitor = new TrVisitor<>();
            List<List<String>> bufferTemp = new ArrayList<>();
            for (int a = 0; a < Vtree.functions.length; a++) {
               RegAllocation currentAllocation = allocs.get(a);
               VFunction currentFunc = Vtree.functions[a];
               trVisitor.setData(currentFunc, currentAllocation);

               for (int b = 0; b < currentFunc.body.length; b++) {
                   Vtree.functions[a].body[b].accept(trVisitor);
               }

               bufferTemp.add(trVisitor.bufferVisitor);
            }

            printBuffers(bufferTemp);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }

    public static void printBuffers(List<List<String>> b) {
        for (List<String> bufferTemp : b) {
            for (String lineTemp : bufferTemp) {
                System.out.print(lineTemp);
            }
            System.out.println();
        }
    }
}
//end V2VM class
