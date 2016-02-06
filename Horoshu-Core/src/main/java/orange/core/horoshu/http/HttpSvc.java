package orange.core.horoshu.http;

import cyan.core.config.BaseConfig;
import cyan.core.config.IConfig;
import orange.core.horoshu.dns.SvcDns;
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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpSvc 可以创建并存储，多套不同配置的服务调用实例。
 * 每个HttpSvc实例维护了一组同步HttpClient，一组异步NHttpClient。
 * <p/>
 * 支付“服务名称DNS”功能，可以将注册的服务名称，转为实际的“IP+端口+工程根路径”。
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
    /*=================================================*/
    /*==================== Factory ====================*/
    /*=================================================*/
    public static Map<IConfig, HttpSvc> g_httpSvcMap = new HashMap<>();
    public static IConfig g_httpSvcConfig = BaseConfig.getEmptyConfig();
    /*========== Static Properties ==========*/
    private static Logger g_logger = org.slf4j.LoggerFactory.getLogger(HttpSvc.class.getName());
    /*========== Properties ==========*/
    private SvcDns m_SvcDns = SvcDns.getInstance();
    private IConfig m_Config;
    private PoolingHttpClientConnectionManager m_httpClientMngr;


    /*=================================================*/
    /*==================== Instant ====================*/
    /*=================================================*/
    private PoolingNHttpClientConnectionManager m_nHttpClientMngr;
    private ConnectingIOReactor m_ioReactor;
    private IHooks m_hooks;

    /*========== Constructor ==========*/
    private HttpSvc(IConfig config) {
        /*===== Store Config =====*/
        m_Config = config;
        /*===== Init Hooks =====*/
        this.setHooks((IHooks) config.getObject(HttpSvc.CONFIG_HOOKS, null));
        /*===== Init Properties =====*/
        initClientMngr(config);
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
     * .setContent(content, CHttpRequest.CONTENT_TYPE_JSON)
     * .post();
     *
     * @return Http链式构造类
     */
    public static HttpReqChain build() {
        return new HttpReqChain(HttpSvc.getInstance());
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

    private CloseableHttpAsyncClient getAsyncHttpClient() {
        return HttpAsyncClients.custom().setConnectionManager(initAsyncClientMngr()).build();
    }


    /*=========================================================*/
    /*==================== Chain Operation ====================*/
    /*=========================================================*/

    /*========== Export Function ==========*/
    public URIBuilder translateDNS(URIBuilder uriBuilder) throws URISyntaxException {
        return m_SvcDns.translateAddr(uriBuilder);
    }

    public HttpResponse syncRequest(CHttpRequest req) {
         /*===== STEP 1. Prepare =====*/
        HttpResponse res = null;
        CloseableHttpClient httpClient = getHttpClient();
        /*===== STEP 2. Execute =====*/
        try {
            res = httpClient.execute(req);
        } catch (IOException e) {
            g_logger.error("func:build", e);
        }
        return res;
    }

    public void asyncRequest(CHttpRequest req, FutureCallback<HttpResponse> httpRespHandler) {
        CloseableHttpAsyncClient httpclient = getAsyncHttpClient();
        try {
            httpclient.start();
            httpclient.execute(req, httpRespHandler);
            //httpclient.close();
        } catch (Exception e) {
            g_logger.error(e.getMessage());
        }
    }

    public IHooks getHooks() {
        return this.m_hooks;
    }

    public void setHooks(HttpSvc.IHooks hooks) {
        m_hooks = hooks;
    }

    /*==========================================================*/
    /*==================== Hooks Management ====================*/
    /*==========================================================*/
    public interface IHooks {
        void preInvoke(HttpReqChain httpReq);

        void postDns(HttpReqChain httpReq);

        void postInvoke(HttpReqChain httpReq);
    }


}
