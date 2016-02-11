package cyan.svc.horoshu.svcmngr;

import cyan.svc.horoshu.dns.SvcRouteMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public abstract class BasicSvcMngr implements ISvcMngr {

    /*========== Static Properties ==========*/
    //public final static ehcache

    /*========== Properties ==========*/
    protected SvcRouteMap m_SvcRouteMap;
    ScheduledExecutorService m_scheduledThreadPool;
    /* Fresh DNS Item Interval, in Second */
    private Integer m_freshInterval = 300;
    /*========== Constructor ==========*/
    public BasicSvcMngr(SvcRouteMap svcRouteMap) {
        m_SvcRouteMap = svcRouteMap;
        initSyncSchedul();
    }

    /*========== Factory ==========*/
    public synchronized static BasicSvcMngr getInstance() {
        return null;
    }

    public void initSyncSchedul() {
        /*=====  =====*/
        m_scheduledThreadPool = Executors.newScheduledThreadPool(1);
        /*===== Runnable =====*/
        m_scheduledThreadPool.schedule(new Runnable() {
            @Override
            public void run() {
                refreshSvcRoute();
            }
        }, m_freshInterval, TimeUnit.SECONDS);

    }

    /*========== Assistatn Function ==========*/


    /*========== Interface : ISvcMngr ==========*/
    @Override
    public abstract void registerSvc(String svcName);

    @Override
    public abstract void unregisterSvc();

    @Override
    public abstract void discoverSvc();

    @Override
    public abstract void refreshSvcRoute();

}
