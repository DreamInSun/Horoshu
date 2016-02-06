package cyan.core.horoshu.http;

import com.cyan.arsenal.Console;
import cyan.core.config.BaseConfig;
import orange.core.horoshu.dns.DnsItem;
import orange.core.horoshu.http.*;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpSvc Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>���� 5, 2016</pre>
 */
public class HttpSvcTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getHttpClient()
     */
    @Test
    public void testGetHttpClient() throws Exception {
        /*===== Start Request =====*/
        /*===== Create Hooks =====*/
        HttpSvc.IHook hooks = new HttpSvc.IHook() {
            @Override
            public void preInvoke(HttpReqChain httpReq) {
                Console.info("========================================");
                Console.info("START\t{" + httpReq.getReqId() + "} [" + new Date(httpReq.markTime()) + "]: \t " + httpReq.getUriBuilder());
            }

            @Override
            public void postDns(HttpReqChain httpReq) {
                Console.info("SVCDNS\t{" + httpReq.getReqId() + "}\tused:" + httpReq.getElapsedTime() + "ms \t" + httpReq.getReq());
            }

            @Override
            public void postInvoke(HttpReqChain httpReq) {
                Console.info("FINISH\t{" + httpReq.getReqId() + "}\tHttp invoke used: " + httpReq.getElapsedTime() + " ms");
            }
        };
        /*===== Create Config =====*/
        HttpSvc.config(new BaseConfig().set(HttpSvc.CONFIG_HOOKS, hooks));
        /*===== Create Response Handler =====*/
        IHttpRespHandler respHandler = new IHttpRespHandler() {
            @Override
            public void procHttpResponse(long reqId, HttpResponse resp) {
                Console.info("########## Async Callback ##########");
                Console.info("FINISH\t{" + reqId + "} \t" + resp);
                HttpRespUtil.printResp(resp);
            }
        };
        /*===== Create URI =====*/
        URI uri = new URI("http://wthrcdn.etouch.cn/weather_mini?citykey=101020800");
        /*===== Create Content =====*/
        final DnsItem dns1 = new DnsItem("cyan.core.Test", "dreaminsun.ngrok.natapp.cn", 80, "proj", DnsItem.SVC_TYPE_HTTP);
        Map<String, Object> content = new HashMap<String, Object>() {{
            put("key1", "val1");
            put("Dns", dns1);
        }};
        /*===== Header Map =====*/
        Map<String, Object> headerMap = new HashMap<String, Object>() {{
            put(HttpSvc.HEADER_FIELD_ACCESSTOKEN, "123456");
            put(HttpSvc.HEADER_FIELD_AUTHORIZATION, "OK");
        }};
        /*===== Param Map =====*/
        Map<String, Object> paramMap = new HashMap<String, Object>() {{
            put("s", "/home/user/logout");
            put("openId", "66668888");
        }};
        /*===== Start Sync Request =====*/
        HttpResponse res0 = HttpSvc.build().setURI("http://127.0.0.1:8080/Working").setHeader("access-token", "123456").setContent("Hello").setParam("userId", "1").setPath("/Library/Path").post();
        HttpRespUtil.printResp(res0);

        HttpResponse res1 = HttpSvc.build("http://www.baidu.com").get();
        HttpRespUtil.printResp(res1);

        HttpResponse res2 = HttpSvc.build().setURI(uri).get();
        HttpRespUtil.printResp(res2);

        HttpResponse res3 = HttpSvc.build("http://www.baidu.com").get();
        HttpRespUtil.printResp(res3);

        HttpResponse res4 = HttpSvc.build("http://www.weather.com.cn/adat/sk/101010100.html").get();
        HttpRespUtil.printResp(res2);

         /*===== Start Async Request =====*/
        HttpSvc.build("http://cyan.core.Test")
                .setRespHandler(respHandler)
                .options();

        HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/")
                .setRespHandler(respHandler)
                .head();

        HttpSvc.build()
                .setURI("http://dreaminsun.ngrok.natapp.cn/weiphp/ppp")
                .setParam("s", "/home/user/login")
                .setHeader(HttpSvc.HEADER_FIELD_CONNECTION, HttpSvc.CONN_STAT_KEEP_ALIVE)
                .setHeader(HttpSvc.HEADER_FIELD_VERSION, "1.0.0")
                .setHeader(HttpSvc.HEADER_FIELD_ACCESSTOKEN, "HKLJHJWEQPOWJ")
                .setFragment("Chapter1")
                .setHeaders(headerMap)
                .setParams(paramMap)
                .setContent(content, HttpRequest.CONTENT_TYPE_JSON)
                .setRespHandler(respHandler)
                .post();

        HttpSvc.build()
                .setURI("http://dreaminsun.ngrok.natapp.cn/ppj").setParam("s", "/home/user/login")
                .setContent(content, HttpRequest.CONTENT_TYPE_PLAIN)
                .setRespHandler(respHandler)
                .put();

        HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/delete")
                .setRespHandler(respHandler)
                .setContent(new Object())
                .delete();


        /*===== Start Request =====*/
        Console.info("/*========== Wait for Return. ==========*/");
        Thread.sleep(2000);
        Console.info("/*========== Test Finished ==========*/");
    }
}
