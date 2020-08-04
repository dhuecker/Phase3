package registerAlloc;

import java.io.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class V2VMTest {

    private static final InputStream DEFAULT_STDIN = System.in;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void rollbackChangesToStdin() {
        try {
            outContent.reset();
            errContent.reset();
        } catch (Exception e) {

        }

        System.setIn(DEFAULT_STDIN);
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.out.println(errContent.toString());
    }

    @Test
    public void basicTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/1-Basic.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void loopTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/2-Loop.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void bookTest() {
        try {
            File inputFile = new File("./src/test/resources/book.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void binaryTreeTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/BinaryTree.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void factorialTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/Factorial.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void factorialOptTest () {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/Factorial.opt.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void moreThan4Test() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/MoreThan4.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void linkedListOptTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/LinkedList.opt.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void quickSortOptTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/QuickSort.opt.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        V2VM.allocRegs();
        assertEquals("ALWAYS FAIL", outContent.toString());
    }
}
