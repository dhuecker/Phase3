package registerAlloc;

public class LiveRange {

    // Used during register allocation
    public String register;
    public int place;

    public int begin;
    public int end;
    public String ident;



    public LiveRange(int b, int e, String i) {
        this.begin = b;
        this.end = e;
        this.ident = i;

        register = null;
        place = -1;
    }

    public String getPlace() {
        if (place != -1) {
            return "local[" + place + "]";
        }

        return register;
    }

    void print() {
        System.out.println(ident + "(" + begin + ", " + end + ") loc: " + place);
        System.out.println("    reg: " + register + " loc: " + place);
        for (int a = 0;a < begin; a++) {
            System.out.print(".");
        }
        for (int a = begin - 1; a < end; a++) {
            System.out.print("+");
        }
        System.out.println();
    }
}
