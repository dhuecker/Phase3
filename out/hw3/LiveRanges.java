

import java.util.List;

public class LiveRanges {
    List<LiveRange> rangesLive;

    public LiveRanges(List<LiveRange> rangesLive) {
        this.rangesLive = rangesLive;
    }

    public LiveRange getRange(int x) {
        return rangesLive.get(x);
    }

    public int size() {
        return rangesLive.size();
    }

    public void print() {
        for (LiveRange temp : rangesLive) {
            temp.print();
        }
    }

    public void sortIncreaseStartPoint() {
        rangesLive.sort((o1, o2) -> o1.begin < o2.begin ? -1 : 1);
    }

    public List<LiveRange> getRangesLive() {
        return this.rangesLive;
    }

    public LiveRange getAlloc(int line, String i) {
        for (LiveRange lr : rangesLive) {
            if (inRange(lr,line) && lr.ident.equals(i))
                return lr;
        }
        return null;
    }

    boolean inRange(LiveRange temp, int line) {
        // temp.end + 1 to deal with labels
        return (temp.begin <= line && line <= temp.end + 1);
    }
}
//end LiveRanges class
