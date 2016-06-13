package br.com.soapboxrace.launcher.variables;

import br.com.soapboxrace.launcher.jaxb.LauncherSettingsType;

public class Settings {
	private static LauncherSettingsType launcherSettings;

	public static LauncherSettingsType getLauncherSettings() {
		return launcherSettings;
	}

	public static void setLauncherSettings(LauncherSettingsType launcherSettings) {
		Settings.launcherSettings = launcherSettings;
	}	
}