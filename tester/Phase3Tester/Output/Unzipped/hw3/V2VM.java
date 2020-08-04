

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.List;

public class V2VM {
    public static void main(String[] args) {
        allocRegs();
    }

    public static void allocRegs() {
        try {
            VaporProgram tree = ParseVapor.parseVapor(System.in, System.err);
            LiveRangeVisitor<Exception> rangeVisitor = new LiveRangeVisitor();

            // One set of rangesLive per function
            List<LiveRanges> rangesLive = new ArrayList<>();
            for (int i = 0; i < tree.functions.length; i++) {
                VFunction currFunc = tree.functions[i];
                rangeVisitor.setCurrFunction(currFunc);

                for (int j = 0; j < currFunc.body.length; j++) {
                    tree.functions[i].body[j].accept(rangeVisitor);
                }

                rangesLive.add(rangeVisitor.getCurrRanges());
                //if (tree.functions[i].ident.equals("QS.Sort")) {
                 //   rangeVisitor.inspect();
                   // rangeVisitor.printCFG();
                //}
            }

            // For each function use LSRA to allocate registers
            List<RegisterAllocation> allocations = new ArrayList<>();
            for (int i = 0; i < tree.functions.length; i++) {
                RegisterAllocation currAlloc = new RegisterAllocation(tree.functions[i], rangesLive.get(i));
                currAlloc.LinearScanRegisterAllocation();
                allocations.add(currAlloc);
                //if (tree.functions[i].ident.equals("QS.Sort"))
                //currAlloc.print();
            }

            // Print out the data section
            for (int i = 0; i < tree.dataSegments.length; i++) {
                System.out.println("const " + tree.dataSegments[i].ident);
                for (int j = 0; j < tree.dataSegments[i].values.length; j++) {
                    System.out.println("    " + tree.dataSegments[i].values[j]);
                }
                System.out.println("");
            }

            // Translate function to use registers instead of locals
            TranslationVisitor<Exception> translationVisitor = new TranslationVisitor<>();
            List<List<String>> buffers = new ArrayList<>();
            for (int i = 0; i < tree.functions.length; i++) {
               RegisterAllocation currAllocation = allocations.get(i);
               VFunction currFunc = tree.functions[i];
               translationVisitor.setData(currFunc, currAllocation);

               for (int j = 0; j < currFunc.body.length; j++) {
                   tree.functions[i].body[j].accept(translationVisitor);
               }

               buffers.add(translationVisitor.buffer);
            }

            //translationVisitor.printBuffer();
            printBuffers(buffers);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }

    public static void printBuffers(List<List<String>> buffers) {
        for (List<String> buffer : buffers) {
            for (String line : buffer) {
                System.out.print(line);
            }
            System.out.println();
        }
    }
}
