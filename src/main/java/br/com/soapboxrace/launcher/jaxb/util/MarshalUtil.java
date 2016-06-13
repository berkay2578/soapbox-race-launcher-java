package br.com.soapboxrace.launcher.jaxb.util;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import br.com.soapboxrace.launcher.jaxb.LauncherSettingsType;

public class MarshalUtil {

	public static String marshal(LauncherSettingsType lType) {
		StringWriter stringWriter = new StringWriter();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(LauncherSettingsType.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(lType, stringWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	public static boolean marshalToFile(LauncherSettingsType lType, File path) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(LauncherSettingsType.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(lType, path);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static LauncherSettingsType unMarshal(File inputRef) {
		LauncherSettingsType lRes = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(LauncherSettingsType.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement<LauncherSettingsType> root = jaxbUnmarshaller.unmarshal(new StreamSource(inputRef),
					LauncherSettingsType.class);
			lRes = root.getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lRes;
	}
}
