package ctop.v3.msb.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.*;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MemCacheInvoke {

	/*========== Constructor ==========*/
	public MemCacheInvoke() {

	}

	/*========== testMemcached ==========*/
	public void testMemcached() {
		/*===== STEP 1.  =====*/
		List<InetSocketAddress> destMemCachedAddre = AddrUtil.getAddresses("10.168.6.1:11211");
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(destMemCachedAddre);

		try {
			/*===== STEP 2. Build Memcached Client =====*/
			MemcachedClient memcachedClient = builder.build();

			/*===== STEP 3.  =====*/
			memcachedClient.set("hello", 0, "Hello,xmemcached");
			memcachedClient.set("hello2", 0, "Hello,xmemcached2");
			String value = memcachedClient.get("hello2");
			System.out.println("hello=" + value);

			memcachedClient.delete("hello");
			value = memcachedClient.get("hello");
			System.out.println("hello=" + value);

			/*===== STEP 4. Close Memcached Client =====*/
			memcachedClient.shutdown();

		} catch (MemcachedException e) {
			System.err.println("MemcachedClient operation fail");
			e.printStackTrace();
		} catch (TimeoutException e) {
			System.err.println("MemcachedClient operation timeout");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// ignore
		} catch (IOException e) {
			System.err.println("Shutdown MemcachedClient fail");
			e.printStackTrace();
		}
	}
}
