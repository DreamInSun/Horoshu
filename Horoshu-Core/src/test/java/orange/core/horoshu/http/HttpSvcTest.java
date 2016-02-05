package orange.core.horoshu.http;

import com.cyan.arsenal.Console;
import cyan.core.config.BaseConfig;
import orange.core.horoshu.dns.DnsItem;
import org.apache.http.HttpEntity;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
        /*===== Create Hooks =====*/
        HttpSvc.IHooks hooks = new HttpSvc.IHooks() {
            @Override
            public void preInvoke(HttpReqChain httpReq) {
                Console.info("/*========================================*/");
                long ts = System.currentTimeMillis();
                Console.info("Start : [" + new Date(httpReq.markTime()) + "]: \t " + httpReq.getUriBuilder());
            }

            @Override
            public void postDns(HttpReqChain httpReq) {
                Console.info("Service DNS used: " + httpReq.getElapsedTime() + "ms \t" + httpReq.getReq());
            }

            @Override
            public void postInvoke(HttpReqChain httpReq) {
                Console.info("Http remote invoke used: " + httpReq.getElapsedTime() + "ms");
            }
        };
        /*===== Create Config =====*/
        HttpSvc.config(new BaseConfig().set(HttpSvc.CONFIG_HOOKS, hooks));
        /*===== Create Response Handler =====*/
        FutureCallback<CHttpResponse> respFutureClbk = new FutureCallback<CHttpResponse>() {
            @Override
            public void completed(CHttpResponse resp) {
                Console.info("########## Async Callback ##########");
                Console.info(resp);
                if (resp != null) {
                    Console.info("Status Code:" + resp.getStatusLine().getStatusCode());
                    HttpEntity entity = resp.getEntity();
                    if (resp.getEntity() != null) {

                        String contentType = entity.getContentType().getValue();
                        String[] arr = contentType.split(";");
                        String mime = arr[0];
                        Console.info("MIME:" + mime);

                        try {
                            String outputStr;
                            switch (arr[0]) {
                                case CHttpRequest.CONTENT_TYPE_JSON:
                                    outputStr = EntityUtils.toString(entity);
                                    //Console.info(outputStr);
                                    break;
                                case CHttpRequest.CONTENT_TYPE_JSON_UTF8:
                                    outputStr = EntityUtils.toString(entity);
                                    //Console.info(outputStr);
                                    break;
                                case CHttpRequest.CONTENT_TYPE_HTML:
                                    outputStr = EntityUtils.toString(entity);
                                    //Console.info(outputStr);
                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void failed(Exception ex) {
                Console.error(ex.toString());
            }

            @Override
            public void cancelled() {
                Console.warn("Request Cancelled.");
            }
        };
        /*===== Create URI =====*/
        URI uri = new URI("http://jandan.net");
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
        CHttpResponse res0 = HttpSvc.build().setURI("http://127.0.0.1:8080/Working").setHeader("access-token", "123456").setContent("Hello").setParam("userId", "1").setPath("/Library/Path").post();
        CHttpResponse res1 = HttpSvc.build().setURI(uri).setRespFutureClbk(respFutureClbk).get();
        CHttpResponse res2 = HttpSvc.build("http://cyan.core.Test").get();
        CHttpResponse res3 = HttpSvc.build("http://cyan.core.Test").setRespFutureClbk(respFutureClbk).options();
        res2.getLocale();
        CHttpResponse res4 = HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/").setRespFutureClbk(respFutureClbk).head();
        CHttpResponse res5 = HttpSvc.build()
                .setURI("http://dreaminsun.ngrok.natapp.cn/weiphp/ppp")
                .setParam("s", "/home/user/login")
                .setHeader(HttpSvc.HEADER_FIELD_CONNECTION, HttpSvc.CONN_STAT_KEEP_ALIVE)
                .setHeader(HttpSvc.HEADER_FIELD_VERSION, "1.0.0")
                .setHeader(HttpSvc.HEADER_FIELD_ACCESSTOKEN, "HKLJHJWEQPOWJ")
                .setFragment("Chapter1")
                .setHeaders(headerMap)
                .setParams(paramMap)
                .setContent(content, CHttpRequest.CONTENT_TYPE_JSON)
                .setRespFutureClbk(respFutureClbk)
                .post();

        CHttpResponse res6 = HttpSvc.build()
                .setURI("http://dreaminsun.ngrok.natapp.cn/ppj").setParam("s", "/home/user/login")
                .setContent(content, CHttpRequest.CONTENT_TYPE_PLAIN)
                .setRespFutureClbk(respFutureClbk)
                .put();

        CHttpResponse res7 = HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/delete")
                .setRespFutureClbk(respFutureClbk)
                .setContent(new Object())
                .delete();
        CHttpResponse res8 = HttpSvc.build("http://www.baidu.com").get();

        /*===== Start Request =====*/
        Console.info("/*========== Wait for Return. ==========*/");
        Thread.sleep(2000);
        Console.info("/*========== Test Finished ==========*/");
    }
}
