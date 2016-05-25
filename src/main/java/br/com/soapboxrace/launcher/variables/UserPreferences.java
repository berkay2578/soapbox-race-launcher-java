package br.com.soapboxrace.launcher.variables;

public final class UserPreferences {
	public static boolean AutoLogin = false;
	public static boolean AutoUpdateServers = false;
	public static boolean KeepServerCache = true;
	public static String ServerURL;
	public static String ServerPort;
	
	public static void init(boolean autoLogin, boolean autoUpdateServers, boolean keepServerCache, String serverURL, String serverPort) {
		UserPreferences.AutoLogin = autoLogin;
		UserPreferences.AutoUpdateServers = autoUpdateServers;
		UserPreferences.KeepServerCache = keepServerCache;
		UserPreferences.ServerURL = serverURL;
		UserPreferences.ServerPort = serverPort;
	}
}