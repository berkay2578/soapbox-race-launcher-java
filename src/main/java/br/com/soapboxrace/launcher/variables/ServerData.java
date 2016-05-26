package br.com.soapboxrace.launcher.variables;

public final class ServerData {
	public static String Email;
	public static String PasswordHash;

	public static void init(String email, String passwordHash) {
		ServerData.Email = email;
		ServerData.PasswordHash = passwordHash;
	}
}