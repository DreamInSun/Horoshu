package ctop.v3.setting.preference;

public class PerferenceManager {

	/*========== Constant =========*/

	/*========== Properties ==========*/
	private static Preference g_Preference;

	/*========== Constructor ==========*/
	/**
	 * 
	 * @param appNamespace
	 * @return
	 */
	public static Preference getPreperence(String appNamespace) {
		if (g_Preference == null) {
			g_Preference = new Preference();
		}
		return g_Preference;
	}

	/*==========  ==========*/
}
