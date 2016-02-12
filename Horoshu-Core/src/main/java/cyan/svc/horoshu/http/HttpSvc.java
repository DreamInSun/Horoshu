package cyan.svc.horoshu.http;

import com.cyan.arsenal.Console;
import com.google.common.collect.Maps;
import cyan.core.config.BasicConfig;
import cyan.core.config.IConfig;
import cyan.svc.horoshu.dns.SvcRouteMap;
import cyan.svc.horoshu.support.ConsulSvcMngr;
import cyan.svc.horoshu.svcmngr.ISvcMngr;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * HttpSvc 可以创建并存储，多套不同配置的服务调用实例。
 * 每个HttpSvc实例维护了一组同步HttpClient，一组异步NHttpClient。
 * <p/>
 * 支付“服务名称DNS”功能，可以将注册的服务名称，转为实际的“IP+端口+工程根路径”。
 *
 * <p/>
 * 若使用静态HttpSvc.build()方式发起请求，则使用公用默认配置的HttpSvc。
 * 若使用 new HttpSvc(config).start 发起请求，则根据config的实例区分请求。
 * 如有特别要求，如设置DNS路径，建议使用全局静态config创建HttpSvc实例，不污染全局调用。
 */
public class HttpSvc {

    /*========== Constants ==========*/
    /* Header Field */
    public static final String HEADER_FIELD_ACCESSTOKEN = "access";
    public static final String HEADER_FIELD_AUTHORIZATION = "authen";
    public static final String HEADER_FIELD_VERSION = "ver";
    public static final String HEADER_FIELD_CONNECTION = "Connection";
    /* Connection Status */
    public static final String CONN_STAT_CLOSE = "close";
    public static final String CONN_STAT_KEEP_ALIVE = "keep-alive";


    /* Config Key */
    public static final String CONFIG_HOOKS = "hooks";
    public static final String CONFIG_SVC_MNGR_URI = "svc.mngr.uri";
    /*=================================================*/
    /*==================== Factory ====================*/
    /*=================================================*/
    public static Map<IConfig, HttpSvc> g_httpSvcMap = Maps.newHashMap();
    public static IConfig g_httpSvcConfig = BasicConfig.getEmptyConfig();
    /*===== Create Hooks =====*/
    public static IHook defaultHooks = new IHook() {
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
    /*========== Static Properties ==========*/
    private static Logger g_logger = org.slf4j.LoggerFactory.getLogger(HttpSvc.class.getName());
    /*========== Properties ==========*/
    private ISvcMngr m_SvcMngr;
    private SvcRouteMap m_SvcDns = SvcRouteMap.getInstance();
    private IConfig m_Config;
    private PoolingHttpClientConnectionManager m_httpClientMngr;
    /*=================================================*/
    /*==================== Instant ====================*/
    /*=================================================*/
    private PoolingNHttpClientConnectionManager m_nHttpClientMngr;
    private ConnectingIOReactor m_ioReactor;
    private IHook m_hooks;

    /*========== Constructor ==========*/
    private HttpSvc(IConfig config) {
        /*===== Store Config =====*/
        m_Config = config;
        /*===== Init Hooks =====*/
        this.setHooks((IHook) config.getObject(HttpSvc.CONFIG_HOOKS, null));
        /*===== Init Properties =====*/
        initClientMngr(config);
        /*===== Init DN =====*/
        initSvcMngr(config);

    }

    /**
     * 设置默认创建HttpSvc的配置，新创建的HttpSvc，若不指定配置，则均使用该配置。
     *
     * @param config 配置文件
     */
    public static synchronized void config(final IConfig config) {
        g_httpSvcConfig = config;
    }

    /**
     * 获取默认配置的HttpSvc
     *
     * @return HttpSvc
     */
    public static synchronized HttpSvc getInstance() {
        return HttpSvc.getInstance(g_httpSvcConfig);
    }

    /**
     * 获取制定配置的HttpSvc
     *
     * @param config 配置使用静态变量，减少重复创建。
     * @return HttpSvc
     */
    public static synchronized HttpSvc getInstance(final IConfig config) {
        HttpSvc httpSvc = g_httpSvcMap.get(config);
        if (null == httpSvc) {
            httpSvc = new HttpSvc(config);
            g_httpSvcMap.put(config, httpSvc);
        }
        return httpSvc;
    }

    /**
     * 快速创建一个Http请求并调用。
     * <p/>
     * HttpResponse res = HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/").get();
     *
     * @param uri 构造好的有效URI
     * @return Http链式构造类
     */
    public static HttpReqChain build(URI uri) {
        HttpReqChain chain = new HttpReqChain(HttpSvc.getInstance());
        return chain.setURI(uri);
    }

    /**
     * 快速创建一个Http请求并调用。
     * <p/>
     * HttpResponse res = HttpSvc.build("http://dreaminsun.ngrok.natapp.cn/").get();
     *
     * @param uriStr 合法的URI字符串
     * @return Http链式构造类
     */
    public static HttpReqChain build(String uriStr) {
        HttpReqChain chain = new HttpReqChain(HttpSvc.getInstance());
        return chain.setURI(uriStr);
    }

    /**
     * 快速创建一个Http请求构造器并链式调用。
     * <p/>
     * 例如：
     * HttpResponse res = HttpSvc.build()
     * .setURI("http://dreaminsun.ngrok.natapp.cn/weiphp/ppp")
     * .setParam("s", "/home/user/login")
     * .setHeader(HttpSvc.HEADER_FIELD_ACCESSTOKEN, "HKLJHJWEQPOWJ")
     * .setHeaders(headerMap)
     * .setParams(paramMap)
     * .setContent(content, HttpRequest.CONTENT_TYPE_JSON)
     * .post();
     *
     * @return Http链式构造类
     */
    public static HttpReqChain build() {
        return new HttpReqChain(HttpSvc.getInstance());
    }


    public HttpReqChain start() {
        return new HttpReqChain(this);
    }

    public HttpReqChain start(URI uri) {
        HttpReqChain chain = new HttpReqChain(this);
        return chain.setURI(uri);
    }

    public HttpReqChain start(String string) {
        HttpReqChain chain = new HttpReqChain(this);
        return chain.setURI(string);
    }

    /*========== Assistant Function :Sync ==========*/
    private HttpClientConnectionManager initClientMngr() {
        return initClientMngr(m_Config);
    }

    private HttpClientConnectionManager initClientMngr(IConfig config) {
        if (null == m_httpClientMngr) {
            m_httpClientMngr = new PoolingHttpClientConnectionManager();
            m_httpClientMngr.setMaxTotal(config.getInt("httpclient.max_total", 50));
            m_httpClientMngr.setDefaultMaxPerRoute(config.getInt("httpclient.max_per_route", 5));
        }
        return m_httpClientMngr;
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().setConnectionManager(initClientMngr()).build();
    }

    private void initSvcMngr(IConfig config) {
        String svcMngrUri = config.getString(CONFIG_SVC_MNGR_URI);
        /*===== Input Protection =====*/
        if (null == svcMngrUri) return;
        /*===== Init =====*/
        try {
            m_SvcMngr = new ConsulSvcMngr(m_SvcDns, svcMngrUri);
        } catch (URISyntaxException e) {
            g_logger.error(e.getMessage());
        }
        m_SvcMngr.refreshSvcRoute();
    }

    /*========== Assistant Function : Async ==========*/
    private NHttpClientConnectionManager initAsyncClientMngr() {
        return initAsyncClientMngr(m_Config);
    }

    private NHttpClientConnectionManager initAsyncClientMngr(IConfig config) {
        if (null == m_httpClientMngr) {
            try {
                m_ioReactor = new DefaultConnectingIOReactor();
            } catch (IOReactorException e) {
                e.printStackTrace();
            }
            m_nHttpClientMngr = new PoolingNHttpClientConnectionManager(m_ioReactor);
            m_nHttpClientMngr.setMaxTotal(config.getInt("httpclient.max_total", 50));
            m_nHttpClientMngr.setDefaultMaxPerRoute(config.getInt("httpclient.max_per_route", 5));
        }
        return m_nHttpClientMngr;
    }


    /*=========================================================*/
    /*==================== Chain Operation ====================*/
    /*=========================================================*/

    private CloseableHttpAsyncClient getAsyncHttpClient() {
        return HttpAsyncClients.custom().setConnectionManager(initAsyncClientMngr()).build();
    }

    /*========== Export Function ==========*/
    public URIBuilder translateDNS(URIBuilder uriBuilder) throws URISyntaxException {
        if( m_SvcMngr == null ) return uriBuilder;
        return m_SvcDns.translateAddr(uriBuilder);
    }

    public HttpResponse async2syncRequest(final HttpRequest req) {
         /*===== STEP 1. Prepare Http Client =====*/
        final HttpResponse[] res = {null};
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRequest(req, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(HttpResponse result) {
                        res[0] = result;
                        latch.countDown();
                    }

                    @Override
                    public void failed(Exception ex) {
                        g_logger.error(ex.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void cancelled() {
                        g_logger.error("Request Cancelled : " + req);
                        latch.countDown();
                    }
                });
            }
        }).start();
        /*===== STEP 3. Wait for return =====*/
        try {
            latch.await();
        } catch (InterruptedException e) {
            Console.debug(e);
        }
        /*===== STEP 4. Return =====*/
        return res[0];
    }

    public HttpResponse syncRequest(HttpRequest req) {
         /*===== STEP 1. Prepare Http Client =====*/
        HttpResponse res = null;
        CloseableHttpClient httpClient = getHttpClient();
        /*===== STEP 2. Execute =====*/
        try {
            res = httpClient.execute(req);
            httpClient.close();
        } catch (IOException e) {
            g_logger.error(e.getMessage());
        } finally {
        }
        return res;
    }

    public void asyncRequest(HttpRequest req, FutureCallback<HttpResponse> httpRespHandler) {
        CloseableHttpAsyncClient httpclient = getAsyncHttpClient();
        try {
            httpclient.start();
            httpclient.execute(req, httpRespHandler);
        } catch (Exception e) {
            g_logger.error(e.getMessage());
        }
    }

    /*==========================================================*/
    /*==================== Hooks Management ====================*/
    /*==========================================================*/

    public IHook getHooks() {
        return this.m_hooks;
    }

    public void setHooks(IHook hooks) {
        m_hooks = hooks;
    }

    public interface IHook {
        void preInvoke(HttpReqChain httpReq);

        void postDns(HttpReqChain httpReq);

        void postInvoke(HttpReqChain httpReq);
    }
}
