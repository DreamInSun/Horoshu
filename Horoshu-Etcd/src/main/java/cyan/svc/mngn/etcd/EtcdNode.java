package cyan.svc.mngn.etcd;

import java.util.List;

/**
 * Value Object
 */
public class EtcdNode implements Comparable {

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

	@Override
	public int compareTo(Object o) {
		EtcdNode target = (EtcdNode)o;
		if(this.createdIndex - target.createdIndex == 0){
			return (int) (this.modifiedIndex - target.modifiedIndex);
		} else {
			return (int) (this.createdIndex - target.createdIndex);
		}
	}
}
