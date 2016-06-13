package br.com.soapboxrace.launcher.jaxb;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerDataType", propOrder = { "url", "httpPort", "xmppPort", "udpPort", "udpRelayPort", "loginData" })
@XmlRootElement(name = "Server")
@XmlSeeAlso(LoginDataType.class)
public class ServerDataType {
	@XmlElement(name = "URL", defaultValue = "127.0.0.1", required = true, nillable = false)
	private String url = "127.0.0.1";
	@XmlElement(name = "HttpPort", defaultValue = "1337", required = true, nillable = false)
	private Integer httpPort = 1337;
	@XmlElement(name = "XmppPort", defaultValue = "5222", required = true, nillable = false)
	private Integer xmppPort = 5222;
	@XmlElement(name = "UdpPort", defaultValue = "9998", required = true, nillable = false)
	private Integer udpPort = 9998;
	@XmlElement(name = "UdpRelayPort", defaultValue = "9999", required = true, nillable = false)
	private Integer udpRelayPort = 9999;
	@XmlElement(name = "LoginData", required = true, nillable = true)
	private LoginDataType loginData = new LoginDataType();

	@XmlTransient
	private URL literalURL;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}

	public Integer getXmppPort() {
		return xmppPort;
	}

	public void setXmppPort(Integer xmppPort) {
		this.xmppPort = xmppPort;
	}

	public Integer getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(Integer udpPort) {
		this.udpPort = udpPort;
	}

	public Integer getUdpRelayPort() {
		return udpRelayPort;
	}

	public void setUdpRelayPort(Integer udpRelayPort) {
		this.udpRelayPort = udpRelayPort;
	}

	public LoginDataType getLoginData() {
		return loginData;
	}

	public void setLoginData(LoginDataType loginData) {
		this.loginData = loginData;
	}

	public URL getLiteralURL() {
		return literalURL;
	}

	public void setLiteralURL(URL literalURL) {
		this.literalURL = literalURL;
	}
}