package cyan.svc.horoshu.svcmngr;

/**
 * Created by DreamInSun on 2016/2/9.
 */
public interface ISvcMngr {

    void registerSvc(String svcName);

    void unregisterSvc();

    void discoverSvc();

    void refreshSvcRoute();
}
