package cyan.svc.etcd;

/**
 * Value Object
 */
public class EtcdResult {
    /*======== General values ========*/
    public String action;
    public EtcdNode node;
    public EtcdNode prevNode;

    /*======== For errors ========*/
    public int errorIndex;
    public Integer errorCode;
    public String message;
    public String cause;

    /*======== Export Function ========*/
    public boolean isError() {
        return errorCode != null;
    }

    /*======== toString ========*/
    @Override
    public String toString() {
        return EtcdClient.format(this);
    }
}
