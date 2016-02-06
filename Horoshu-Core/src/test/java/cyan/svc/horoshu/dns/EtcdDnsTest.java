package cyan.svc.horoshu.dns;

import com.google.common.util.concurrent.ListenableFuture;
import cyan.svc.etcd.EtcdClient;
import cyan.svc.etcd.EtcdClientException;
import cyan.svc.etcd.EtcdResult;
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
public class EtcdDnsTest extends TestCase {
    public EtcdDnsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(EtcdDnsTest.class);
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

            EtcdResult result = client.set(key, "hello");
            Assert.assertEquals("hello", result.node.value);

            result = client.get(key);
            Assert.assertEquals("hello", result.node.value);

            ListenableFuture<EtcdResult> watchFuture = client.watch(key, result.node.createdIndex + 1, true);
            Assert.assertFalse(watchFuture.isDone());

            result = client.set(key, "world");

            Assert.assertEquals("world", result.node.value);

            EtcdResult watchResult = watchFuture.get(100, TimeUnit.MILLISECONDS);
            Assert.assertNotNull(result);
            Assert.assertEquals("world", result.node.value);
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
