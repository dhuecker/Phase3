

import java.util.List;

public class LiveRanges {
    List<LiveRange> ranges;

    public LiveRanges(List<LiveRange> ranges) {
        this.ranges = ranges;
    }

    public LiveRange getRange(int i) {
        return ranges.get(i);
    }

    public int size() {
        return ranges.size();
    }

    public void print() {
        for (LiveRange lr : ranges) {
            lr.print();
        }
    }

    public void sortIncreaseStartPoint() {
        ranges.sort((o1, o2) -> o1.start < o2.start ? -1 : 1);
    }

    public List<LiveRange> getRanges() {
        return this.ranges;
    }

    public LiveRange getAlloc(int line, String ident) {
        for (LiveRange lr : ranges) {
            if (inRange(lr,line) && lr.ident.equals(ident))
                return lr;
        }
        return null;
    }

    // Helper function
    boolean inRange(LiveRange lr, int line) {
        // lr.end + 1 to deal with labels
        return (lr.start <= line && line <= lr.end + 1);
    }
}
