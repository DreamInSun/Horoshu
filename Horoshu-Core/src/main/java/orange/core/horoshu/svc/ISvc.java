package orange.core.horoshu.svc;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public interface ISvc {

    /**
     *
     * @param svcName
     * @param path
     * @param request
     * @return
     */
    Object invoke(String svcName, String path, Object request );

    /**
     *
     * @param svcName
     * @param path
     * @param request
     * @return
     */
    String invokeSync(String svcName, String path, Object request );

    /**
     *
     * @param seqId
     * @return
     */
    Object getResultSync(String seqId);
}
