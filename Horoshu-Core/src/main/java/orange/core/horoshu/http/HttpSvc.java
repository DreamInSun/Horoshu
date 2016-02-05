package orange.core.horoshu.http;

import cyan.core.config.BaseConfig;
import cyan.core.config.IConfig;
import orange.core.horoshu.dns.SvcDns;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpSvc 可以创建并存储，多套不同配置的服务调用实例。
 * HttpSvc 建立了
 * <p/>
 * 支付“服务名称DNS”功能，可以将注册的服务名称，转为实际的“IP+端口+工程根路径”。
 */
public class HttpSvc {

    /*========== Constants ==========*/
    /* Header Field */
    public static final String HEADER_FIELD_ACCESSTOKEN = "access";
    public static final String HEADER_FIELD_AUTHORIZATION = "authen";
    public static final String HEADER_FIELD_VERSION = "ver";

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
    private IHooks m_hooks;
    /*========== Constructor ==========*/
    private HttpSvc(IConfig config) {
        m_Config = config;
        m_hooks = config.getObject(HttpSvc.CONFIG_HOOKS, null);
        /*===== Init Properties =====*/
        getClientMngr();
    }

    /**
     * 设置默认创建HttpSvc的配置，新创建的HttpSvc，若不指定配置，则均使用该配置。
     *
     * @param config
     */
    public static void config(IConfig config) {
        g_httpSvcConfig = config;
    }

    /**
     * 获取默认配置的HttpSvc
     *
     * @return HttpSvc
     */
    public static HttpSvc getInstance() {
        return HttpSvc.getInstance(g_httpSvcConfig);
    }

    /**
     * 获取制定配置的HttpSvc
     *
     * @param config 配置使用静态变量，减少重复创建。
     * @return HttpSvc
     */
    public static HttpSvc getInstance(IConfig config) {
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
     * 例如：
     * HttpResponse res = HttpSvc.execRequest("http://dreaminsun.ngrok.natapp.cn/").get();
     *
     * @param uriStr
     * @return　HttpReqChain
     */
    public static HttpReq execRequest(String uriStr) {
        HttpReq chain = new HttpReq(HttpSvc.getInstance());
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
     * @return
     */
    public static HttpReq build() {
        return new HttpReq(HttpSvc.getInstance());
    }

    /*========== Assistant Function ==========*/
    private HttpClientConnectionManager getClientMngr() {
        if (null == m_httpClientMngr) {
            m_httpClientMngr = new PoolingHttpClientConnectionManager();
            m_httpClientMngr.setMaxTotal(m_Config.getInt("httpclient.max_total", 50));
            m_httpClientMngr.setDefaultMaxPerRoute(m_Config.getInt("httpclient.max_per_route", 5));
        }
        return m_httpClientMngr;
    }

    /*=========================================================*/
    /*==================== Chain Operation ====================*/
    /*=========================================================*/

    /*===== Prepare Request =====*/

    private HttpClient getHttpClient() {
        return HttpClientBuilder.create().setConnectionManager(getClientMngr()).build();
    }

    /*========== Export Function ==========*/
    public URIBuilder translateDNS(URIBuilder uriBuilder) throws URISyntaxException {
        return m_SvcDns.translateAddr(uriBuilder);
    }

    public HttpResponse execRequest(HttpRequest req) {
         /*===== STEP 1. Prepare =====*/
        HttpResponse res = null;
        HttpClient httpClient = getHttpClient();
        /*===== STEP 2. Execute =====*/
        try {
            res = httpClient.execute(req);
        } catch (ClientProtocolException e) {
            g_logger.error("func:execRequest", e);
        } catch (IOException e) {
            g_logger.error("func:execRequest", e);
        }
        return res;
    }

    public IHooks getHooks() {
        return this.m_hooks;
    }

    public void setHooks(HttpSvc.IHooks hooks) {
        m_hooks = hooks;
    }

    /*==================================*/
    /*========== Public Hooks ==========*/
    /*==================================*/
    public interface IHooks {
        void preInvoke(HttpReq httpReqChain);

        void postDns(HttpReq httpReqChain);

        void postInvoke(HttpReq httpReqChain);
    }
}
