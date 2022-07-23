package parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Constantine on 16.07.2022
 */
public class Test {

    private final static Logger LOG = LoggerFactory.getLogger(Test.class.getName());

    public static int testFileLog1(int a, int b) {
        int result = 0;
        try {
            result = a / b;
        } catch (Exception e) {
            LOG.error("Exception write to file", e);
        }
        return result;
    }

    public static String testFileLog2(char[] number) {
        String rsl = null;
        try {
            rsl = String.valueOf(number, 0, 5);
        } catch (Exception e) {
            LOG.error("Exception write to file", e);
        }
        return rsl;
    }

    public static void testFileLog3() {
        try {
            throw new Exception("There is no code here");
        } catch (Exception e) {
            LOG.error("Exception write to file", e);
        }
    }

    public static void testFileLog4() {
        LOG.error("Some error..");
    }

    public static void main(String[] args) {
        int a = 5;
        int b = 0;
        char[] c = new char[] {1, 3, 5};
        System.out.println(testFileLog1(a, b));
        System.out.println(testFileLog2(c));
        testFileLog3();
        testFileLog4();
        LOG.info("Info message..");
        LOG.warn("Warn message..");
        LOG.debug("Debug message..");
        LOG.error("Error message..");
    }
}
