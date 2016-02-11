package cyan.svc.horoshu;

/**
 * Created by DreamInSun on 2016/2/9.
 */
public interface ISvcMngm {

    void registerSvc(String svcName);

    void unregisterSvc();

    void discoverSvc();
}
