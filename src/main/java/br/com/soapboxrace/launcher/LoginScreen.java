package br.com.soapboxrace.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class LoginScreen extends Shell {
	private Text txtPassword;
	private Text txtEmail;
	private Label lblStatus;

	private String userId;
	private String loginToken;
	private String serverURL;
	
	private String dirLauncherSettings = "launcher/";
	private String fileLauncherSettings = "settings.xml";

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			LoginScreen shell = new LoginScreen(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public LoginScreen(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		setText("Soapbox-Hill | server launcher");
		setSize(450, 300);
		setLayout(null);

		Menu menu = new Menu(this, SWT.BAR);
		setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("Settings");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmServerSelect = new MenuItem(menu_1, SWT.NONE);
		mntmServerSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Shell a = getShell();
				ServerSelection dialog = new ServerSelection(a, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				serverURL = dialog.open();
				setServerURL(serverURL);
			}
		});
		mntmServerSelect.setText("Select a server...");
		
		MenuItem mntmAutoUpdateServers = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoUpdateServers.setText("Auto-Update servers on start");

		MenuItem mntmAutoLogin = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoLogin.setText("Auto-Login on start");

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.setText("About");

		Composite composite = new Composite(this, SWT.NONE);
		composite.setBounds(0, 10, 260, 99);
		composite.setLayout(null);

		Label lblEmail = new Label(composite, SWT.NONE);
		lblEmail.setBounds(22, 13, 35, 15);
		lblEmail.setText("Email: ");

		Label lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setBounds(22, 40, 56, 15);
		lblPassword.setText("Password: ");

		txtEmail = new Text(composite, SWT.BORDER);
		txtEmail.setBounds(90, 10, 160, 21);
		txtEmail.setTextLimit(254);

		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setBounds(90, 37, 160, 21);
		txtPassword.setTextLimit(64);

		Button btnLogin = new Button(composite, SWT.NONE);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				doLogin(txtEmail.getText(), DigestUtils.sha1Hex(txtPassword.getText()));
			}
		});
		btnLogin.setBounds(194, 64, 56, 25);
		btnLogin.setText("Login");
				
		Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_IN);
		label.setBounds(0, 228, 444, 2);
		
		lblStatus = new Label(this, SWT.NONE);
		lblStatus.setBounds(5, 232, 439, 15);
		lblStatus.setText("Status: Idle");
		
		readSettings();
	}

	private void readSettings() {
		try {
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList(
						"<LauncherSettings>",
						"	<Server>",
						"		<URL>http://localhost:1337</URL>",
						"	</Server>",
						"	<Preferences>",
						"		<AutoLogin>false</AutoLogin>",
						"		<AutoUpdateServers>false</AutoUpdateServers>",
						"		<KeepServerCache>true</KeepServerCache>",
						"	</Preferences>",
						"	<LoginData>",
						"		<Email/>",
						"		<Password/>",
						"	</LoginData>",
						"</LauncherSettings>"
						);
				Files.write(Paths.get(dirLauncherSettings.concat(fileLauncherSettings)), defaultSettings, StandardCharsets.UTF_8);
			}
			
			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();
			Document doc = dcBuilder.parse(settings);
			doc.getDocumentElement().normalize();
			
			serverURL = doc.getElementsByTagName("URL").item(0).getTextContent();			
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setServerURL(String serverURL) {		
		try {
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();
			Document doc = dcBuilder.parse(settings);
			doc.getDocumentElement().normalize();
			
			doc.getElementsByTagName("URL").item(0).setTextContent(serverURL);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(settings);

			transformer.transform(source, result);
		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doLogin(String email, String password) {
		try {
			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(password, StandardCharsets.UTF_8.toString()));

			URL serverAuth = new URL(serverURL.concat("/nfsw/Engine.svc/User/AuthenticateUser?").concat(param));
			HttpURLConnection serverCon = (HttpURLConnection) serverAuth.openConnection();
			serverCon.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					serverCon.getResponseCode() == 200 ? serverCon.getInputStream() : serverCon.getErrorStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			Document doc = dcBuilder.parse(new InputSource(new StringReader(response.toString())));

			if (serverCon.getResponseCode() == 200) {
				userId = doc.getElementsByTagName("UserId").item(0).getTextContent();
				loginToken = doc.getElementsByTagName("LoginToken").item(0).getTextContent();
				lblStatus.setText("Status: Logged in!");
			} else {
				lblStatus.setText(doc.getElementsByTagName("Message").item(0).getTextContent());
			}

		} catch (UnsupportedEncodingException e) {
			lblStatus.setText("Environment Error: Couldn't encode URL to UTF-8.");
		} catch (MalformedURLException e) {
			lblStatus.setText("Connection Error: Server URL is malformed.");
		} catch (ConnectException e) {
			lblStatus.setText("Connection Error: Couldn't connect to the server.");
		} catch (ProtocolException e) {
			lblStatus.setText("Connection Error: Couldn't setup GET request.");
		} catch (SAXException | ParserConfigurationException e) {
			lblStatus.setText("Login Error: Invalid response data.");
		} catch (IOException e) {
			e.printStackTrace();
			lblStatus.setText(String.format("Error: %s.", e.getCause().getLocalizedMessage()));
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
