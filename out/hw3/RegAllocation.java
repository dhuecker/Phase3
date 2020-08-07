

/*
 * Algorithm based on MASSIMILIANO POLETTO & VIVEK SARKAR Linear Scan Register Allocation
 * http://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
 */

import cs132.vapor.ast.VFunction;

import java.util.*;

public class RegAllocation {

    // General Purpose Registers
    // $s0...$s7 are callee-saved
    // $t0...$t8 are caller-saved
    //all general purpose registers are declared as a string inside of the array GPRs
    public static String[] GPRs = {"$s0","$s1","$s2","$s3","$s4","$s5","$s6","$s7",
             "$t0","$t1","$t2","$t3","$t4","$t5","$t6","$t7","$t8"};

    //setup data members for register allocation
    VFunction currentFunction;
    LiveRanges rangesLive;

    List<String> freePlace;
    List<LiveRange> active;
    int stackLocation;

    //constructor for RegAllocation
    public RegAllocation(VFunction x, LiveRanges y) {
        currentFunction = x;
        rangesLive = y;

        // All registers are available initially
        freePlace = new ArrayList<>();
        freePlace.addAll(Arrays.asList(GPRs));
        active = new ArrayList<>();
        stackLocation = 0;
    }

    //helping methods below
    public LiveRange getAlloc(int line, String id) {
        return rangesLive.getAlloc(line, id);
    }

    public void print() {
        for (LiveRange temp : rangesLive.getRangesLive()) {
            temp.print();
        }
    }

    int R() {
        return GPRs.length;
    }

    public void LinearScanRegisterAllocation() {
        rangesLive.sortIncreaseStartPoint();
        for (LiveRange temp : rangesLive.getRangesLive()) {
            ExpireOldIntervals(temp);
            if (active.size() == R()) {
                SpillAtInterval(temp);
            } else {
                temp.register = getRegFromPlace();
                active.add(temp);
                sortByIncreasingEndPoint(active);
            }
        }
    }

    public void ExpireOldIntervals(LiveRange x) {
        sortByIncreasingEndPoint(active);
        List<LiveRange> tempRemove = new ArrayList<>();
        for (LiveRange temp : active) {
            if (temp.end >= x.begin) {
                deleteExpire(tempRemove);
                return;
            }
            addRegToPlace(temp.register);
            tempRemove.add(temp);
        }

        deleteExpire(tempRemove);
    }

    public void deleteExpire(List<LiveRange> Remove) {
        for (LiveRange temp : Remove) {
            active.remove(temp);
        }
    }

    public void SpillAtInterval(LiveRange x) {

        LiveRange spillTemp = active.get(active.size()-1);
        if (spillTemp.end > x.end) {
            x.register = spillTemp.register;
            spillTemp.place = getNewStackLocation();
            active.remove(spillTemp);
            active.add(x);
            sortByIncreasingEndPoint(active);
        } else {
            x.place = getNewStackLocation();
        }
    }
    
    void sortByIncreasingEndPoint(List<LiveRange> x) {
        x.sort((o1, o2) -> o1.end < o2.end ? -1 : 1);
    }

    int getOrgPos(String x) {
        int a = 0;
        for (String temp : GPRs) {
            if (temp.equals(x))
                return a;
            a++;
        }

        return -1;
    }

    String getRegFromPlace() {
        String regTemp = freePlace.get(0);
        freePlace.remove(0);
        return regTemp;
    }

    void addRegToPlace(String x) {
        freePlace.add(x);
        freePlace.sort((o1, o2) -> getOrgPos(o1) < getOrgPos(o2) ? -1 : 1);
        if (freePlace.size() > GPRs.length)
            System.out.println("freePlace is larger than available: " + freePlace.size());
    }

    int getNewStackLocation() {
        stackLocation++;
        return stackLocation;
    }
}
//end RegAllocation class
