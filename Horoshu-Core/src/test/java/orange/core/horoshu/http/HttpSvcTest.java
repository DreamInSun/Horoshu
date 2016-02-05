package orange.core.horoshu.http;

import com.cyan.arsenal.Console;
import cyan.core.config.BaseConfig;
import orange.core.horoshu.dns.DnsItem;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        /*===== Create Hooks =====*/
        HttpSvc.IHooks hooks = new HttpSvc.IHooks() {
            @Override
            public void preInvoke(HttpReq httpReqChain) {
                Console.info("/*========================================*/");
                Console.info("Pre Invoke : " + System.currentTimeMillis() + "\n" + httpReqChain.getUriBuilder());
            }

            @Override
            public void postDns(HttpReq httpReqChain) {
                Console.info("After DNS : " + System.currentTimeMillis() + "\n" + httpReqChain.getReq());
            }

            @Override
            public void postInvoke(HttpReq httpReqChain) {
                Console.info("After Invoke : " + System.currentTimeMillis());
            }
        };
        /*===== Create Config =====*/
        HttpSvc.config(new BaseConfig().set(HttpSvc.CONFIG_HOOKS, hooks));
        /*===== Create Response Handler =====*/
        IHttpRespHandler respHandler = new IHttpRespHandler() {
            @Override
            public void parseResp(HttpResponse resp) {
                Console.info(resp);
                if (resp != null) {
                    Console.info(resp.getStatusLine().getStatusCode());
                    Console.info(resp.getHeaders(HTTP.CONTENT_TYPE).toString());
                    Console.info(resp.getEntity());
                }
            }
        };
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

        /*===== Start Request =====*/
        HttpResponse res0 = HttpSvc.build().setURI("http://127.0.0.1:8080/Working").setHeader("access-token", "123456").setContent("Hello").setParam("userId", "1").setPath("/Library/Path").post();
        HttpResponse res1 = HttpSvc.execRequest("http://cyan.core.Test").setRespHandler(respHandler).get();
        HttpResponse res2 = HttpSvc.execRequest("http://cyan.core.Test").setRespHandler(respHandler).options();
        HttpResponse res3 = HttpSvc.execRequest("http://dreaminsun.ngrok.natapp.cn/").setRespHandler(respHandler).head();
        HttpResponse res4 = HttpSvc.build()
                .setURI("http://dreaminsun.ngrok.natapp.cn/weiphp/ppp")
                .setParam("s", "/home/user/login")
                .setHeader(HttpSvc.HEADER_FIELD_ACCESSTOKEN, "HKLJHJWEQPOWJ")
                .setHeaders(headerMap)
                .setParams(paramMap)
                .setContent(content, HttpRequest.CONTENT_TYPE_JSON)
                .setRespHandler(respHandler)
                .post();
        HttpResponse res5 = HttpSvc.build().setURI("http://dreaminsun.ngrok.natapp.cn/ppj").setParam("s", "/home/user/login").setContent(content, HttpRequest.CONTENT_TYPE_PLAIN).setRespHandler(respHandler).put();
        HttpResponse res6 = HttpSvc.execRequest("http://dreaminsun.ngrok.natapp.cn/delete").setRespHandler(respHandler).delete();
        //HttpResponse res7 = HttpSvc.execRequest("http://dreaminsun.ngrok.natapp.cn/").setRespHandler(respHandler).patch();
        //HttpResponse res8 = HttpSvc.execRequest("http://dreaminsun.ngrok.natapp.cn/").setRespHandler(respHandler).trace();
        HttpResponse res9 = HttpSvc.execRequest("http://www.baidu.com").setRespHandler(respHandler).get();

        Console.info("/*========== Test Finished ==========*/");
    }
}
