package registerAlloc;

import java.io.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class V2VMTest {

    private static final InputStream DEFAULT_STDIN = System.in;
    private static final ByteArrayOutputStream outputContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
    private static final PrintStream ogOutput = System.out;
    private static final PrintStream ogError = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outputContent));
        System.setErr(new PrintStream(errorContent));
    }

    @After
    public void rollbackChangesToStdin() {
        try {
            outputContent.reset();
            errorContent.reset();
        } catch (Exception e) {

        }

        System.setIn(DEFAULT_STDIN);
        System.setOut(ogOutput);
        System.setErr(ogError);
        System.out.println(errorContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
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
        assertEquals("", outputContent.toString());
    }
}
