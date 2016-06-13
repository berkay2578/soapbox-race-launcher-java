package br.com.soapboxrace.launcher.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientDataType", propOrder = { "path", "moduleName" })
@XmlRootElement(name = "Client")
public class ClientDataType {
	@XmlElement(name = "Path", required = true, nillable = true)
	private String path;
	@XmlElement(name = "ModuleName", required = true, nillable = true)
	private String moduleName;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
}