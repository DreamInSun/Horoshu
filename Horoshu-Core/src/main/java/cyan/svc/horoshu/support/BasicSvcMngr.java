package cyan.svc.horoshu.support;

import cyan.svc.horoshu.ISvcMngm;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public abstract class BasicSvcMngr implements ISvcMngm {

    /*========== Static Properties ==========*/
    //public final static ehcache

    /*========== Factory ==========*/
    public synchronized static BasicSvcMngr getInstance() {
        return null;
    }

    /*========== Constructor ==========*/
    public void initCache() {

    }

    /*========== Interface : ISvcMngm ==========*/
    @Override
    public abstract void registerSvc(String svcName);

    @Override
    public abstract void unregisterSvc();

    @Override
    public abstract void discoverSvc();


}
