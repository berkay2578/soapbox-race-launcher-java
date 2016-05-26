package br.com.soapboxrace.launcher.forms;

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
import java.net.UnknownHostException;
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

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.soapboxrace.launcher.variables.UserPreferences;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Group;

public class LoginScreen extends Shell {
	private Text txtPassword;
	private Text txtEmail;
	private CLabel lblStatus;
	private CLabel lblServerURL;
	private CLabel lblHttpPort;
	private Composite compositeEntrance;
	private Composite compositeNfsw;

	private String userId = null;
	private String loginToken = null;

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
		addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				SWTResourceManager.dispose();
			}
		});
		setText("Soapbox-Hill | server launcher");
		setSize(497, 363);
		setLayout(null);
		readSettings();

		Menu menu = new Menu(this, SWT.BAR);
		setMenuBar(menu);

		MenuItem mntmSettings = new MenuItem(menu, SWT.CASCADE);
		mntmSettings.setText("Settings");

		Menu menu_1 = new Menu(mntmSettings);
		mntmSettings.setMenu(menu_1);

		MenuItem mntmServerSelect = new MenuItem(menu_1, SWT.NONE);
		mntmServerSelect.setToolTipText("Change your current server with another one");
		mntmServerSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Shell a = getShell();
				ServerSelection dialog = new ServerSelection(a, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				String result[] = dialog.open();
				UserPreferences.ServerURL = result[0];
				UserPreferences.ServerHttpPort = result[1];
				saveSettings();
				lblServerURL.setText(UserPreferences.ServerURL.replace("http://", ""));
				lblHttpPort.setText(UserPreferences.ServerHttpPort);
			}
		});
		mntmServerSelect.setText("Select a server...");

		MenuItem mntmKeepServerCache = new MenuItem(menu_1, SWT.CHECK);
		mntmKeepServerCache.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UserPreferences.KeepServerCache = mntmKeepServerCache.getSelection();
				saveSettings();
			}
		});
		mntmKeepServerCache.setToolTipText(
				"When server data is retrieved, keep them for later use\r\n(Note: this will cause old data to be shown unless manually refreshed in the launcher)\r\n(Note-2: disabling this will also delete your current saved server data cache)");
		mntmKeepServerCache.setText("Keep server cache");
		mntmKeepServerCache.setSelection(UserPreferences.KeepServerCache);

		MenuItem mntmAutoUpdateServers = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoUpdateServers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UserPreferences.AutoUpdateServers = mntmAutoUpdateServers.getSelection();
				saveSettings();
			}
		});
		mntmAutoUpdateServers.setToolTipText(
				"Whether should the launcher auto-retrieve latest list of servers and also download their latest data\r\n(Note: do not check this if your PC or your internet is slow)");
		mntmAutoUpdateServers.setText("Auto-Update servers on start");
		mntmAutoUpdateServers.setSelection(UserPreferences.AutoUpdateServers);

		MenuItem mntmAutoLogin = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UserPreferences.AutoLogin = mntmAutoLogin.getSelection();
				saveSettings();
			}
		});
		mntmAutoLogin.setToolTipText("Whether should the launcher auto-log you in to the current server");
		mntmAutoLogin.setText("Auto-Login on start");
		mntmAutoLogin.setSelection(UserPreferences.AutoLogin);

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.setText("About");

		compositeEntrance = new Composite(this, SWT.NONE);
		compositeEntrance.setBounds(9, 10, 304, 129);
		compositeEntrance.setLayout(null);

		CLabel lblStep1 = new CLabel(compositeEntrance, SWT.NONE);
		lblStep1.setLocation(0, 6);
		lblStep1.setSize(67, 30);
		lblStep1.setText("Step 1.");
		lblStep1.setFont(new Font(lblStep1.getDisplay(), new FontData("Segoe UI Semibold", 16, SWT.NONE)));

		Label lblEmail = new Label(compositeEntrance, SWT.NONE);
		lblEmail.setBounds(24, 41, 35, 15);
		lblEmail.setText("Email: ");

		Label lblPassword = new Label(compositeEntrance, SWT.NONE);
		lblPassword.setBounds(24, 68, 56, 15);
		lblPassword.setText("Password: ");

		txtEmail = new Text(compositeEntrance, SWT.BORDER);
		txtEmail.setBounds(92, 38, 199, 21);
		txtEmail.setTextLimit(254);

		txtPassword = new Text(compositeEntrance, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setBounds(92, 65, 199, 21);
		txtPassword.setTextLimit(64);

		Button btnLogin = new Button(compositeEntrance, SWT.NONE);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						doLogin(txtEmail.getText(), DigestUtils.sha1Hex(txtPassword.getText()));
					}
				});
			}
		});
		btnLogin.setBounds(131, 92, 56, 25);
		btnLogin.setText("Login");

		Label lbl1 = new Label(compositeEntrance, SWT.NONE);
		lbl1.setBounds(193, 97, 16, 15);
		lbl1.setText("or");

		Button btnRegister = new Button(compositeEntrance, SWT.NONE);
		btnRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						doRegister(txtEmail.getText(), DigestUtils.sha1Hex(txtPassword.getText()));
					}
				});
			}
		});
		btnRegister.setBounds(215, 92, 76, 25);
		btnRegister.setText("Register");

		CLabel lbl2 = new CLabel(compositeEntrance, SWT.NONE);
		lbl2.setBounds(69, 13, 114, 23);
		lbl2.setText("Login to the server");
		lbl2.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));

		lblStatus = new CLabel(this, SWT.BORDER | SWT.SHADOW_IN);
		lblStatus.setLeftMargin(5);
		lblStatus.setBounds(0, 291, 491, 23);
		lblStatus.setText("Status: Idle");

		compositeNfsw = new Composite(this, SWT.NONE);
		compositeNfsw.setEnabled(false);
		compositeNfsw.setBounds(9, 145, 304, 129);

		CLabel lblStep2 = new CLabel(compositeNfsw, SWT.NONE);
		lblStep2.setText("Step 2.");
		lblStep2.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 16, SWT.NORMAL));
		lblStep2.setBounds(0, 6, 72, 30);
		lblStep2.setEnabled(false);

		CLabel lbl7 = new CLabel(compositeNfsw, SWT.NONE);
		lbl7.setText("Start NFS: World");
		lbl7.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));
		lbl7.setBounds(71, 13, 114, 23);
		lbl7.setEnabled(false);

		Label lbl8 = new Label(compositeNfsw, SWT.NONE);
		lbl8.setBounds(24, 41, 96, 15);
		lbl8.setText("NFS: World Path:");
		lbl8.setEnabled(false);

		CLabel lblNfsWorldPath = new CLabel(compositeNfsw, SWT.BORDER);
		lblNfsWorldPath.setText(UserPreferences.NFSWorldPath);
		lblNfsWorldPath.setBounds(121, 40, 135, 21);
		lblNfsWorldPath.setEnabled(false);

		Button btnNfsWorldPath = new Button(compositeNfsw, SWT.NONE);
		btnNfsWorldPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
						dialog.setFileName("nfsw");
						dialog.setFilterExtensions(new String[] { "*.exe" });
						dialog.setFilterPath("C:\\ProgramData\\Electronic Arts\\Need for Speed World");
						UserPreferences.NFSWorldPath = dialog.open();
						saveSettings();
						lblNfsWorldPath.setText(UserPreferences.NFSWorldPath);
					}
				});
			}
		});
		btnNfsWorldPath.setBounds(258, 38, 33, 25);
		btnNfsWorldPath.setText("...");
		btnNfsWorldPath.setEnabled(false);

		CLabel lblFiller = new CLabel(compositeNfsw, SWT.BORDER | SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		lblFiller.setAlignment(SWT.CENTER);
		lblFiller.setBounds(0, 72, 202, 57);
		lblFiller.setText("FILL THIS PLACE WITH SOMETHING PLEASE THANKS, LOV U BB :*");
		lblFiller.setEnabled(false);

		Button btnLaunchNfsWorld = new Button(compositeNfsw, SWT.NONE);
		btnLaunchNfsWorld.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					if (!UserPreferences.NFSWorldPath.isEmpty()) {
						new ProcessBuilder(UserPreferences.NFSWorldPath,
								"THANKSOBAMA", UserPreferences.ServerURL.concat(":")
										.concat(UserPreferences.ServerHttpPort).concat("/nfsw/Engine.svc"),
								loginToken, userId).start();
						// I'm not managing this shit, ain't nobody got time for that.
						lblStatus.setText("Status: NFS World launched successfully!");
					}
					else
						lblStatus.setText("Launch Error: NFS World path is null.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnLaunchNfsWorld.setBounds(216, 67, 75, 25);
		btnLaunchNfsWorld.setText("Launch");
		btnLaunchNfsWorld.setEnabled(false);

		Group grpServerDetails = new Group(this, SWT.NONE);
		grpServerDetails.setText("Current Server Details");
		grpServerDetails.setBounds(319, 10, 162, 122);

		Label lbl3 = new Label(grpServerDetails, SWT.NONE);
		lbl3.setBounds(10, 23, 27, 15);
		lbl3.setText("URL: ");

		lblServerURL = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerURL.setBounds(40, 20, 112, 21);
		lblServerURL.setText(UserPreferences.ServerURL.replace("http://", ""));

		Label lbl4 = new Label(grpServerDetails, SWT.NONE);
		lbl4.setText("Active Players: ");
		lbl4.setBounds(10, 71, 79, 15);

		CLabel lblServerActiveSessions = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerActiveSessions.setText((String) null);
		lblServerActiveSessions.setBounds(95, 68, 57, 21);

		Label lbl5 = new Label(grpServerDetails, SWT.NONE);
		lbl5.setText("Total Players: ");
		lbl5.setBounds(10, 95, 79, 15);

		CLabel lblServerTotalPlayers = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerTotalPlayers.setText((String) null);
		lblServerTotalPlayers.setBounds(95, 92, 57, 21);

		Label lbl6 = new Label(grpServerDetails, SWT.NONE);
		lbl6.setText("HTTP Port: ");
		lbl6.setBounds(10, 47, 62, 15);

		lblHttpPort = new CLabel(grpServerDetails, SWT.BORDER);
		lblHttpPort.setText(UserPreferences.ServerHttpPort);
		lblHttpPort.setBounds(78, 44, 74, 21);
	}

	private void readSettings() {
		try {
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList("<LauncherSettings>", "	<Server>",
						"		<URL>http://localhost</URL>", "		<Port>1337</Port>", "		<LoginData>",
						"			<Email/>", "			<Password/>", "		</LoginData>", "	</Server>",
						"	<Preferences>", "		<AutoLogin>false</AutoLogin>",
						"		<AutoUpdateServers>false</AutoUpdateServers>",
						"		<KeepServerCache>true</KeepServerCache>", "	</Preferences>", "	<NFSWorld>",
						"		<Path/>", "	</NFSWorld>", "</LauncherSettings>");
				Files.write(Paths.get(dirLauncherSettings.concat(fileLauncherSettings)), defaultSettings,
						StandardCharsets.UTF_8);
			}

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();
			Document doc = dcBuilder.parse(settings);
			doc.getDocumentElement().normalize();

			Boolean autoLogin = Boolean.valueOf(doc.getElementsByTagName("AutoLogin").item(0).getTextContent());
			Boolean autoUpdateServers = Boolean
					.valueOf(doc.getElementsByTagName("AutoUpdateServers").item(0).getTextContent());
			Boolean keepServerCache = Boolean
					.valueOf(doc.getElementsByTagName("KeepServerCache").item(0).getTextContent());
			String serverURL = doc.getElementsByTagName("URL").item(0).getTextContent();
			String serverHttpPort = doc.getElementsByTagName("HTTPPort").item(0).getTextContent();
			String nfswPath = doc.getElementsByTagName("NFSWorld").item(0).getFirstChild().getTextContent();
			UserPreferences.init(autoLogin, autoUpdateServers, keepServerCache, serverURL, serverHttpPort, nfswPath);
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveSettings() {
		try {
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList("<LauncherSettings>", "	<Server>",
						"		<URL>http://localhost</URL>", "		<Port>1337</Port>", "		<LoginData>",
						"			<Email/>", "			<Password/>", "		</LoginData>", "	</Server>",
						"	<Preferences>", "		<AutoLogin>false</AutoLogin>",
						"		<AutoUpdateServers>false</AutoUpdateServers>",
						"		<KeepServerCache>true</KeepServerCache>", "	</Preferences>", "	<NFSWorld>",
						"		<Path/>", "	</NFSWorld>", "</LauncherSettings>");
				Files.write(Paths.get(dirLauncherSettings.concat(fileLauncherSettings)), defaultSettings,
						StandardCharsets.UTF_8);
			}

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();
			Document doc = dcBuilder.parse(settings);
			doc.getDocumentElement().normalize();

			doc.getElementsByTagName("AutoLogin").item(0).setTextContent(String.valueOf(UserPreferences.AutoLogin));
			doc.getElementsByTagName("AutoUpdateServers").item(0)
					.setTextContent(String.valueOf(UserPreferences.AutoUpdateServers));
			doc.getElementsByTagName("KeepServerCache").item(0)
					.setTextContent(String.valueOf(UserPreferences.KeepServerCache));
			doc.getElementsByTagName("URL").item(0).setTextContent(UserPreferences.ServerURL);
			doc.getElementsByTagName("HTTPPort").item(0).setTextContent(UserPreferences.ServerHttpPort);
			doc.getElementsByTagName("NFSWorld").item(0).getFirstChild().setTextContent(UserPreferences.NFSWorldPath);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
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
			lblStatus.setText("Status: Logging in...");
			setEnabled(compositeEntrance, false);
			userId = null;
			loginToken = null;

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(password, StandardCharsets.UTF_8.toString()));

			URL serverAuth = new URL(String.format("%s:%s/nfsw/Engine.svc/User/AuthenticateUser?%s",
					UserPreferences.ServerURL, UserPreferences.ServerHttpPort, param));
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
				setEnabled(compositeNfsw, true);
				return;
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
		} catch (UnknownHostException e) {
			lblStatus.setText("Connection Error: Current server isn't running.");
		} catch (SAXException | ParserConfigurationException e) {
			lblStatus.setText("Login Error: Invalid response data.");
		} catch (IOException e) {
			e.printStackTrace();
			lblStatus.setText(String.format("Error: %s.", e.getCause().getLocalizedMessage()));
		}
		setEnabled(compositeEntrance, true);
	}

	private void doRegister(String email, String password) {
		try {
			lblStatus.setText("Status: Registering email ".concat(txtEmail.getText()));
			setEnabled(compositeEntrance, false);
			userId = null;
			loginToken = null;

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(password, StandardCharsets.UTF_8.toString()));

			URL serverAuth = new URL(String.format("%s:%s/nfsw/Engine.svc/User/CreateUser?%s",
					UserPreferences.ServerURL, UserPreferences.ServerHttpPort, param));
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
				lblStatus.setText("Status: Registered and logged in!");
				setEnabled(compositeNfsw, true);
				return;
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
		} catch (UnknownHostException e) {
			lblStatus.setText("Connection Error: Current server isn't running.");
		} catch (SAXException | ParserConfigurationException e) {
			lblStatus.setText("Login Error: Invalid response data.");
		} catch (IOException e) {
			e.printStackTrace();
			lblStatus.setText(String.format("Error: %s.", e.getCause().getLocalizedMessage()));
		}
		setEnabled(compositeEntrance, true);
	}

	private void setEnabled(Control control, boolean boolTrue) {
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			composite.setEnabled(boolTrue);
			for (Control ctrl : composite.getChildren())
				setEnabled(ctrl, boolTrue);
		} else
			control.setEnabled(boolTrue);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
