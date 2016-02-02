package ctop.v3.vendor.authrization.data;

public class ComResponse {
	/*========== Constant =========*/

	/*========== Properties ==========*/
	public int errCode;
	public String desc;
	public Object obj;

	/*========== Constructor ==========*/
	public ComResponse(int errCode, String desc) {
		this.errCode = errCode;
		this.desc = desc;
	}

	/*==========  ==========*/
}
