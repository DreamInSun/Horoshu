package cyan.svc.mngn.etcd;

import com.google.common.util.concurrent.ListenableFuture;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * EtcdDns Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>02/06/2016</pre>
 */
public class EtcdClientTest extends TestCase {
    public EtcdClientTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(EtcdClientTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAll() {
        EtcdClient client = new EtcdClient(URI.create("http://lord.17orange.com:4001/"));

        try {
            String key = "/watch";

            EtcdResult result1 = client.set(key, "hello");
            Assert.assertFalse(result1.isError());

            EtcdResult result2 = client.get(key);
            Assert.assertEquals("hello", result2.node.value);

            ListenableFuture<EtcdResult> watchFuture = client.watch(key, result2.node.createdIndex + 1, true);
            Assert.assertFalse(watchFuture.isDone());

            EtcdResult result3 = client.set(key, "world");
            Assert.assertFalse(result3.isError());

            EtcdResult result4 = watchFuture.get(100, TimeUnit.MILLISECONDS);
            Assert.assertNotNull(result4);
            Assert.assertEquals("world", result4.node.value);
        } catch (EtcdClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
