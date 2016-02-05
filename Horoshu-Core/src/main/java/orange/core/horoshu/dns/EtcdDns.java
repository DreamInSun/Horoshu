package orange.core.horoshu.dns;


/**
 * Created by DreamInSun on 2016/2/2.
 */
public class EtcdDns {

    public void test() {
        //Etcdclient client;
        /*
        EtcdClient client = new EtcdClient(URI.create("http://127.0.0.1:4001/"));

        String key = "/watch";

        EtcdResult result = this.client.set(key, "hello");
        Assert.assertEquals("hello", result.value);

        result = this.client.get(key);
        Assert.assertEquals("hello", result.value);

        ListenableFuture<EtcdResult> watchFuture = this.client.watch(key, result.index + 1);
        Assert.assertFalse(watchFuture.isDone());

        result = this.client.set(key, "world");
        Assert.assertEquals("world", result.value);

        EtcdResult watchResult = watchFuture.get(100, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(result);
        Assert.assertEquals("world", result.value);
        */
    }
}
