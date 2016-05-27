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

import br.com.soapboxrace.launcher.variables.ServerData;
import br.com.soapboxrace.launcher.variables.UserPreferences;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

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
		readSettings();
		setLayout(new FormLayout());

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
		FormData fd_compositeEntrance = new FormData();
		fd_compositeEntrance.bottom = new FormAttachment(0, 139);
		fd_compositeEntrance.right = new FormAttachment(0, 313);
		fd_compositeEntrance.top = new FormAttachment(0, 10);
		fd_compositeEntrance.left = new FormAttachment(0, 9);
		compositeEntrance.setLayoutData(fd_compositeEntrance);
		compositeEntrance.setLayout(new FormLayout());

		CLabel lblStep1 = new CLabel(compositeEntrance, SWT.NONE);
		FormData fd_lblStep1 = new FormData();
		fd_lblStep1.bottom = new FormAttachment(0, 36);
		fd_lblStep1.top = new FormAttachment(0, 6);
		fd_lblStep1.left = new FormAttachment(0);
		lblStep1.setLayoutData(fd_lblStep1);
		lblStep1.setText("Step 1.");
		lblStep1.setFont(new Font(lblStep1.getDisplay(), new FontData("Segoe UI Semibold", 16, SWT.NONE)));

		Label lblEmail = new Label(compositeEntrance, SWT.NONE);
		FormData fd_lblEmail = new FormData();
		fd_lblEmail.top = new FormAttachment(0, 41);
		fd_lblEmail.left = new FormAttachment(0, 24);
		lblEmail.setLayoutData(fd_lblEmail);
		lblEmail.setText("Email: ");

		Label lblPassword = new Label(compositeEntrance, SWT.NONE);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.top = new FormAttachment(0, 68);
		fd_lblPassword.left = new FormAttachment(0, 24);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText("Password: ");

		txtEmail = new Text(compositeEntrance, SWT.BORDER);
		FormData fd_txtEmail = new FormData();
		fd_txtEmail.right = new FormAttachment(0, 291);
		fd_txtEmail.top = new FormAttachment(0, 38);
		fd_txtEmail.left = new FormAttachment(0, 92);
		txtEmail.setLayoutData(fd_txtEmail);
		txtEmail.setTextLimit(254);

		txtPassword = new Text(compositeEntrance, SWT.BORDER | SWT.PASSWORD);
		FormData fd_txtPassword = new FormData();
		fd_txtPassword.right = new FormAttachment(0, 291);
		fd_txtPassword.top = new FormAttachment(0, 65);
		fd_txtPassword.left = new FormAttachment(0, 92);
		txtPassword.setLayoutData(fd_txtPassword);
		txtPassword.setTextLimit(64);

		Button btnLogin = new Button(compositeEntrance, SWT.NONE);
		FormData fd_btnLogin = new FormData();
		fd_btnLogin.right = new FormAttachment(0, 187);
		fd_btnLogin.top = new FormAttachment(0, 92);
		fd_btnLogin.left = new FormAttachment(0, 131);
		btnLogin.setLayoutData(fd_btnLogin);
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
		btnLogin.setText("Login");

		Label lbl1 = new Label(compositeEntrance, SWT.NONE);
		FormData fd_lbl1 = new FormData();
		fd_lbl1.right = new FormAttachment(0, 209);
		fd_lbl1.top = new FormAttachment(0, 97);
		fd_lbl1.left = new FormAttachment(0, 193);
		lbl1.setLayoutData(fd_lbl1);
		lbl1.setText("or");

		Button btnRegister = new Button(compositeEntrance, SWT.NONE);
		FormData fd_btnRegister = new FormData();
		fd_btnRegister.right = new FormAttachment(0, 291);
		fd_btnRegister.top = new FormAttachment(0, 92);
		fd_btnRegister.left = new FormAttachment(0, 215);
		btnRegister.setLayoutData(fd_btnRegister);
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
		btnRegister.setText("Register");

		CLabel lbl2 = new CLabel(compositeEntrance, SWT.NONE);
		FormData fd_lbl2 = new FormData();
		fd_lbl2.right = new FormAttachment(0, 291);
		fd_lbl2.top = new FormAttachment(0, 13);
		fd_lbl2.left = new FormAttachment(0, 69);
		lbl2.setLayoutData(fd_lbl2);
		lbl2.setText("Login to the server");
		lbl2.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));

		lblStatus = new CLabel(this, SWT.BORDER | SWT.SHADOW_IN);
		FormData fd_lblStatus = new FormData();
		fd_lblStatus.bottom = new FormAttachment(0, 314);
		fd_lblStatus.right = new FormAttachment(0, 491);
		fd_lblStatus.top = new FormAttachment(0, 291);
		fd_lblStatus.left = new FormAttachment(0);
		lblStatus.setLayoutData(fd_lblStatus);
		lblStatus.setLeftMargin(5);
		lblStatus.setText("Status: Idle");

		compositeNfsw = new Composite(this, SWT.NONE);
		FormData fd_compositeNfsw = new FormData();
		fd_compositeNfsw.right = new FormAttachment(0, 313);
		fd_compositeNfsw.top = new FormAttachment(0, 145);
		fd_compositeNfsw.left = new FormAttachment(0, 9);
		compositeNfsw.setLayoutData(fd_compositeNfsw);
		compositeNfsw.setEnabled(false);
		compositeNfsw.setLayout(new FormLayout());

		CLabel lblStep2 = new CLabel(compositeNfsw, SWT.NONE);
		FormData fd_lblStep2 = new FormData();
		fd_lblStep2.bottom = new FormAttachment(0, 36);
		fd_lblStep2.right = new FormAttachment(0, 72);
		fd_lblStep2.top = new FormAttachment(0, 6);
		fd_lblStep2.left = new FormAttachment(0);
		lblStep2.setLayoutData(fd_lblStep2);
		lblStep2.setText("Step 2.");
		lblStep2.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 16, SWT.NORMAL));
		lblStep2.setEnabled(false);

		CLabel lbl7 = new CLabel(compositeNfsw, SWT.NONE);
		FormData fd_lbl7 = new FormData();
		fd_lbl7.right = new FormAttachment(0, 291);
		fd_lbl7.top = new FormAttachment(0, 13);
		fd_lbl7.left = new FormAttachment(0, 71);
		lbl7.setLayoutData(fd_lbl7);
		lbl7.setText("Start NFS: World");
		lbl7.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));
		lbl7.setEnabled(false);

		Label lbl8 = new Label(compositeNfsw, SWT.NONE);
		FormData fd_lbl8 = new FormData();
		fd_lbl8.right = new FormAttachment(0, 120);
		fd_lbl8.top = new FormAttachment(0, 41);
		fd_lbl8.left = new FormAttachment(0, 24);
		lbl8.setLayoutData(fd_lbl8);
		lbl8.setText("NFS: World Path:");
		lbl8.setEnabled(false);

		CLabel lblNfsWorldPath = new CLabel(compositeNfsw, SWT.BORDER);
		FormData fd_lblNfsWorldPath = new FormData();
		fd_lblNfsWorldPath.bottom = new FormAttachment(0, 63);
		fd_lblNfsWorldPath.right = new FormAttachment(0, 256);
		fd_lblNfsWorldPath.top = new FormAttachment(0, 40);
		fd_lblNfsWorldPath.left = new FormAttachment(0, 121);
		lblNfsWorldPath.setLayoutData(fd_lblNfsWorldPath);
		lblNfsWorldPath.setText(UserPreferences.NFSWorldPath);
		lblNfsWorldPath.setEnabled(false);

		Button btnNfsWorldPath = new Button(compositeNfsw, SWT.NONE);
		FormData fd_btnNfsWorldPath = new FormData();
		fd_btnNfsWorldPath.right = new FormAttachment(0, 291);
		fd_btnNfsWorldPath.top = new FormAttachment(0, 38);
		fd_btnNfsWorldPath.left = new FormAttachment(0, 258);
		btnNfsWorldPath.setLayoutData(fd_btnNfsWorldPath);
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
		btnNfsWorldPath.setText("...");
		btnNfsWorldPath.setEnabled(false);

		CLabel lblFiller = new CLabel(compositeNfsw, SWT.BORDER | SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		FormData fd_lblFiller = new FormData();
		fd_lblFiller.bottom = new FormAttachment(0, 129);
		fd_lblFiller.right = new FormAttachment(0, 202);
		fd_lblFiller.top = new FormAttachment(0, 72);
		fd_lblFiller.left = new FormAttachment(0);
		lblFiller.setLayoutData(fd_lblFiller);
		lblFiller.setAlignment(SWT.CENTER);
		lblFiller.setText("FILL THIS PLACE WITH SOMETHING PLEASE THANKS, LOV U BB :*");
		lblFiller.setEnabled(false);

		Button btnLaunchNfsWorld = new Button(compositeNfsw, SWT.NONE);
		FormData fd_btnLaunchNfsWorld = new FormData();
		fd_btnLaunchNfsWorld.right = new FormAttachment(0, 291);
		fd_btnLaunchNfsWorld.top = new FormAttachment(0, 67);
		fd_btnLaunchNfsWorld.left = new FormAttachment(0, 216);
		btnLaunchNfsWorld.setLayoutData(fd_btnLaunchNfsWorld);
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
		btnLaunchNfsWorld.setText("Launch");
		btnLaunchNfsWorld.setEnabled(false);

		Group grpServerDetails = new Group(this, SWT.NONE);
		FormData fd_grpServerDetails = new FormData();
		fd_grpServerDetails.bottom = new FormAttachment(0, 132);
		fd_grpServerDetails.right = new FormAttachment(0, 481);
		fd_grpServerDetails.top = new FormAttachment(0, 10);
		fd_grpServerDetails.left = new FormAttachment(0, 319);
		grpServerDetails.setLayoutData(fd_grpServerDetails);
		grpServerDetails.setText("Current Server Details");
		grpServerDetails.setLayout(new FormLayout());

		Label lbl3 = new Label(grpServerDetails, SWT.NONE);
		FormData fd_lbl3 = new FormData();
		fd_lbl3.top = new FormAttachment(0, 8);
		fd_lbl3.left = new FormAttachment(0, 7);
		lbl3.setLayoutData(fd_lbl3);
		lbl3.setText("URL: ");

		lblServerURL = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerURL.setAlignment(SWT.RIGHT);
		FormData fd_lblServerURL = new FormData();
		fd_lblServerURL.right = new FormAttachment(0, 149);
		fd_lblServerURL.top = new FormAttachment(0, 5);
		fd_lblServerURL.left = new FormAttachment(0, 37);
		lblServerURL.setLayoutData(fd_lblServerURL);
		lblServerURL.setText(UserPreferences.ServerURL.replace("http://", ""));

		Label lbl4 = new Label(grpServerDetails, SWT.NONE);
		FormData fd_lbl4 = new FormData();
		fd_lbl4.top = new FormAttachment(0, 56);
		fd_lbl4.left = new FormAttachment(0, 7);
		lbl4.setLayoutData(fd_lbl4);
		lbl4.setText("Active Players: ");

		CLabel lblServerActiveSessions = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerActiveSessions.setAlignment(SWT.RIGHT);
		FormData fd_lblServerActiveSessions = new FormData();
		fd_lblServerActiveSessions.right = new FormAttachment(0, 149);
		fd_lblServerActiveSessions.top = new FormAttachment(0, 53);
		fd_lblServerActiveSessions.left = new FormAttachment(0, 92);
		lblServerActiveSessions.setLayoutData(fd_lblServerActiveSessions);
		lblServerActiveSessions.setText((String) null);

		Label lbl5 = new Label(grpServerDetails, SWT.NONE);
		FormData fd_lbl5 = new FormData();
		fd_lbl5.right = new FormAttachment(0, 86);
		fd_lbl5.top = new FormAttachment(0, 80);
		fd_lbl5.left = new FormAttachment(0, 7);
		lbl5.setLayoutData(fd_lbl5);
		lbl5.setText("Total Players: ");

		CLabel lblServerTotalPlayers = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerTotalPlayers.setAlignment(SWT.RIGHT);
		FormData fd_lblServerTotalPlayers = new FormData();
		fd_lblServerTotalPlayers.right = new FormAttachment(0, 149);
		fd_lblServerTotalPlayers.top = new FormAttachment(0, 77);
		fd_lblServerTotalPlayers.left = new FormAttachment(0, 92);
		lblServerTotalPlayers.setLayoutData(fd_lblServerTotalPlayers);
		lblServerTotalPlayers.setText((String) null);

		Label lbl6 = new Label(grpServerDetails, SWT.NONE);
		FormData fd_lbl6 = new FormData();
		fd_lbl6.right = new FormAttachment(0, 69);
		fd_lbl6.top = new FormAttachment(0, 32);
		fd_lbl6.left = new FormAttachment(0, 7);
		lbl6.setLayoutData(fd_lbl6);
		lbl6.setText("HTTP Port: ");

		lblHttpPort = new CLabel(grpServerDetails, SWT.BORDER);
		lblHttpPort.setAlignment(SWT.RIGHT);
		FormData fd_lblHttpPort = new FormData();
		fd_lblHttpPort.right = new FormAttachment(0, 149);
		fd_lblHttpPort.top = new FormAttachment(0, 29);
		fd_lblHttpPort.left = new FormAttachment(0, 75);
		lblHttpPort.setLayoutData(fd_lblHttpPort);
		lblHttpPort.setText(UserPreferences.ServerHttpPort);
		
		if (UserPreferences.AutoLogin && (!ServerData.Email.isEmpty() & !ServerData.PasswordHash.isEmpty())) {
			txtEmail.setText(ServerData.Email);
			txtPassword.setText(ServerData.PasswordHash);
			doLogin(txtEmail.getText(), txtPassword.getText());
		}
	}

	private void readSettings() {
		try {
			new File(dirLauncherSettings).mkdirs();
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList("<LauncherSettings>", "	<Server>",
						"		<URL>http://localhost</URL>", "		<HTTPPort>1337</HTTPPort>", "		<LoginData>",
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
			
			String email = doc.getElementsByTagName("Email").item(0).getTextContent();
			String passwordHash = doc.getElementsByTagName("Password").item(0).getTextContent();
			ServerData.init(email, passwordHash);
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveSettings() {
		try {
			new File(dirLauncherSettings).mkdirs();
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList("<LauncherSettings>", "	<Server>",
						"		<URL>http://localhost</URL>", "		<HTTPPort>1337</HTTPPort>", "		<LoginData>",
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
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(settings);

			transformer.transform(source, result);
		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveCredentials(String email, String passwordHash) {
		try {
			new File(dirLauncherSettings).mkdirs();
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile()) {
				List<String> defaultSettings = Arrays.asList("<LauncherSettings>", "	<Server>",
						"		<URL>http://localhost</URL>", "		<HTTPPort>1337</HTTPPort>", "		<LoginData>",
						"			<Email/>", "			<Password/>", "		</LoginData>", "	</Server>",
						"	<Preferences>", "		<AutoLogin>false</AutoLogin>",
						"		<AutoUpdateServers>false</AutoUpdateServers>",
						"		<KeepServerCache>true</KeepServerCache>", "	</Preferences>", "	<NFSWorld>",
						"		<Path/>", "	</NFSWorld>", "</LauncherSettings>");
				Files.write(Paths.get(dirLauncherSettings.concat(fileLauncherSettings)), defaultSettings,
						StandardCharsets.UTF_8);
			}

			ServerData.init(email, passwordHash);
			
			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();
			Document doc = dcBuilder.parse(settings);
			doc.getDocumentElement().normalize();

			doc.getElementsByTagName("Email").item(0).setTextContent(ServerData.Email);
			doc.getElementsByTagName("Password").item(0).setTextContent(ServerData.PasswordHash);

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

	private void doLogin(String email, String passwordHash) {
		try {
			lblStatus.setText("Status: Logging in...");
			setEnabled(compositeEntrance, false);
			userId = null;
			loginToken = null;

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(passwordHash, StandardCharsets.UTF_8.toString()));

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
				saveCredentials(email, passwordHash);
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

	private void doRegister(String email, String passwordHash) {
		try {
			lblStatus.setText("Status: Registering email ".concat(txtEmail.getText()));
			setEnabled(compositeEntrance, false);
			userId = null;
			loginToken = null;

			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(passwordHash, StandardCharsets.UTF_8.toString()));

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
				saveCredentials(email, passwordHash);
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
