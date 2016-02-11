package cyan.svc.mngm;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public abstract class SvcMngr implements ISvcMngm {

    /*========== Static Properties ==========*/
    //public final static ehcache

    /*========== Factory ==========*/
    public synchronized static SvcMngr getInstance() {
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
