package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.isger.brick.util.reflect.Standin;

public class BrickUtilTest extends TestCase {

    public BrickUtilTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickUtilTest.class);
    }

    public void testUtil() {
        // System.out.println(Hitchers
        // .getHitcher("net/isger/brick/util/reflect/conversion"));
        // System.out.println(Void.TYPE.getName());
        TestBean testBean = (TestBean) new Standin(TestBean.class).getSource();
        testBean.test();
        assertTrue(true);
    }

    public static interface TestBean {

        public void test();

    }
}
