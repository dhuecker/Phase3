

/*
 * Algorithm based on MASSIMILIANO POLETTO & VIVEK SARKAR Linear Scan Register Allocation
 * http://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
 */

import cs132.vapor.ast.VFunction;

import java.util.*;

public class RegisterAllocation {

    // General Purpose Registers
    // $s0...$s7 are callee-saved
    // $t0...$t8 are caller-saved
    public static String[] GPRs = {"$s0","$s1","$s2","$s3","$s4","$s5","$s6","$s7",
             "$t0","$t1","$t2","$t3","$t4","$t5","$t6","$t7","$t8"};

    VFunction currFunction;
    LiveRanges rangesLive;

    List<String> freePool;
    List<LiveRange> active;
    int stackLoc;

    public RegisterAllocation(VFunction c, LiveRanges r) {
        currFunction = c;
        rangesLive = r;

        // All registers are available initially
        freePool = new ArrayList<>();
        freePool.addAll(Arrays.asList(GPRs));
        active = new ArrayList<>();
        stackLoc = 0;
    }

    public LiveRange getAlloc(int line, String ident) {
        return rangesLive.getAlloc(line, ident);
    }

    public void print() {
        for (LiveRange lr : rangesLive.getRanges()) {
            lr.print();
        }
    }

    int R() {
        return GPRs.length;
    }

    public void LinearScanRegisterAllocation() {
        rangesLive.sortIncreaseStartPoint();
        for (LiveRange i : rangesLive.getRanges()) {
            ExpireOldIntervals(i);
            if (active.size() == R()) {
                SpillAtInterval(i);
            } else {
                i.register = getRegFromPool();
                active.add(i);
                sortByIncreasingEndPoint(active);
            }
        }
    }

    public void ExpireOldIntervals(LiveRange i) {
        sortByIncreasingEndPoint(active);
        List<LiveRange> toRemove = new ArrayList<>();
        for (LiveRange j : active) {
            if (j.end >= i.start) {
                deleteExpire(toRemove);
                return;
            }
            addRegToPool(j.register);
            toRemove.add(j);
        }

        deleteExpire(toRemove);
    }

    public void deleteExpire(List<LiveRange> toRemove) {
        for (LiveRange r : toRemove) {
            active.remove(r);
        }
    }

    public void SpillAtInterval(LiveRange i) {
        //sortByIncreasingEndPoint(active);
        LiveRange spill = active.get(active.size()-1);
        if (spill.end > i.end) {
            i.register = spill.register;
            spill.location = getNewStackLoc();
            active.remove(spill);
            active.add(i);
            sortByIncreasingEndPoint(active);
        } else {
            i.location = getNewStackLoc();
        }
    }
    
    void sortByIncreasingEndPoint(List<LiveRange> r) {
        r.sort((o1, o2) -> o1.end < o2.end ? -1 : 1);
    }

    int getOrgPos(String reg) {
        int i = 0;
        for (String r : GPRs) {
            if (r.equals(reg))
                return i;
            i++;
        }

        return -1;
    }

    String getRegFromPool() {
        String reg = freePool.get(0);
        freePool.remove(0);
        return reg;
    }

    void addRegToPool(String reg) {
        freePool.add(reg);
        freePool.sort((o1, o2) -> getOrgPos(o1) < getOrgPos(o2) ? -1 : 1);
        if (freePool.size() > GPRs.length)
            System.out.println("freePool is larger than available: " + freePool.size());
    }

    int getNewStackLoc() {
        stackLoc++;
        return stackLoc;
    }
}
