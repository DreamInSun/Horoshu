package cyan.svc.mngm.etcd;

import java.util.List;

/**
 * Value Object
 */
public class EtcdNode {

	/*======== General values ========*/
	public String key;
	public long createdIndex;
	public long modifiedIndex;
	public String value;

	/*======== For TTL keys =========*/
	public String expiration;
	public Integer ttl;

	/*======== For listings =========*/
	public boolean dir;
	public List<EtcdNode> nodes;

	/*======== toString =========*/
	@Override
	public String toString() {
		return EtcdClient.format(this);
	}
}
