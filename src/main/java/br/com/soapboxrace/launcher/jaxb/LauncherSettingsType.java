package br.com.soapboxrace.launcher.jaxb;

import javax.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LauncherSettingsType", propOrder = { "clientData", "serverData", "preferences" })
@XmlRootElement(name = "LauncherSettings")
@XmlSeeAlso(value = { ClientDataType.class, ServerDataType.class, PreferencesType.class })
public class LauncherSettingsType {
	@XmlElement(name = "Client", required = true, nillable = false)
	private ClientDataType clientData = new ClientDataType();
	@XmlElement(name = "Server", required = true, nillable = false)
	private ServerDataType serverData = new ServerDataType();
	@XmlElement(name = "Preferences", required = true, nillable = false)
	private PreferencesType preferences = new PreferencesType();

	public ClientDataType getClientData() {
		return clientData;
	}

	public void setClientData(ClientDataType clientData) {
		this.clientData = clientData;
	}

	public ServerDataType getServerData() {
		return serverData;
	}

	public void setServerData(ServerDataType serverData) {
		this.serverData = serverData;
	}

	public PreferencesType getPreferences() {
		return preferences;
	}

	public void setPreferences(PreferencesType preferences) {
		this.preferences = preferences;
	}
}