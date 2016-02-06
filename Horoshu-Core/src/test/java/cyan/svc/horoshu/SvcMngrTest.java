package cyan.svc.horoshu;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SvcMngr Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>02/07/2016</pre>
 */
public class SvcMngrTest extends TestCase {
    public SvcMngrTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SvcMngrTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetInstance() throws Exception {

    }
}
