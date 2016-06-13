package br.com.soapboxrace.launcher.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginDataType", propOrder = { "email", "passwordHash" })
@XmlRootElement(name = "LoginData")
public class LoginDataType {
	@XmlElement(name = "Email", required = true, nillable = true)
	private String email;
	@XmlElement(name = "PasswordHash", required = true, nillable = true)
	private String passwordHash;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}