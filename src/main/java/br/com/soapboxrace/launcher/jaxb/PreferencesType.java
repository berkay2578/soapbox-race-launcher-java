package br.com.soapboxrace.launcher.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PreferencesType", propOrder = { "autoLogin", "autoUpdateServersList", "keepServerDataCache" })
@XmlRootElement(name = "Preferences")
public class PreferencesType {
	@XmlElement(name = "AutoLogin", defaultValue = "false", required = true, nillable = false)
	private boolean autoLogin = false;
	@XmlElement(name = "AutoUpdateServersList", defaultValue = "false", required = true, nillable = false)
	private boolean autoUpdateServersList = false;
	@XmlElement(name = "KeepServerDataCache", defaultValue = "true", required = true, nillable = false)
	private boolean keepServerDataCache = true;

	public boolean isAutoLogin() {
		return autoLogin;
	}

	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
	}

	public boolean isAutoUpdateServersList() {
		return autoUpdateServersList;
	}

	public void setAutoUpdateServersList(boolean autoUpdateServersList) {
		this.autoUpdateServersList = autoUpdateServersList;
	}

	public boolean isKeepServerDataCache() {
		return keepServerDataCache;
	}

	public void setKeepServerDataCache(boolean keepServerDataCache) {
		this.keepServerDataCache = keepServerDataCache;
	}
}