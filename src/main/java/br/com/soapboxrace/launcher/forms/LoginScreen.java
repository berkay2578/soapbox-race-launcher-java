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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.soapboxrace.launcher.jaxb.LauncherSettingsType;
import br.com.soapboxrace.launcher.jaxb.LoginDataType;
import br.com.soapboxrace.launcher.jaxb.ServerDataType;
import br.com.soapboxrace.launcher.jaxb.util.MarshalUtil;
import br.com.soapboxrace.launcher.variables.Settings;

public class LoginScreen extends Shell {
	private Text txtPassword;
	private Text txtEmail;
	private CLabel lblStatus;
	private CLabel lblServerURL;
	private CLabel lblHttpPort;
	private Button btnLogout;
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
		setSize(537, 401);
		readSettings();
		setLayout(null);

		LauncherSettingsType loadDelegate = Settings.getLauncherSettings();

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
				try {
					Shell a = getShell();
					ServerSelection dialog = new ServerSelection(a, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					String result[] = dialog.open();
					Settings.getLauncherSettings().getServerData().setLiteralURL(
							new URL("http", result[0].replace("http://", ""), Integer.valueOf(result[1]), ""));
					Settings.getLauncherSettings().getServerData().setUrl(result[0].replace("http://", ""));
					Settings.getLauncherSettings().getServerData().setHttpPort(Integer.valueOf(result[1]));
					saveSettings();
					lblServerURL.setText(result[0].replace("http://", ""));
					lblHttpPort.setText(result[1].toString());
				} catch (IOException e) {
					lblStatus.setText("Error: ".concat(e.getMessage()));
				}
			}
		});
		mntmServerSelect.setText("Select a server...");

		MenuItem mntmKeepServerCache = new MenuItem(menu_1, SWT.CHECK);
		mntmKeepServerCache.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Settings.getLauncherSettings().getPreferences()
						.setKeepServerDataCache(mntmKeepServerCache.getSelection());
				saveSettings();
			}
		});
		mntmKeepServerCache.setToolTipText(
				"When server data is retrieved, keep them for later use\r\n(Note: this will cause old data to be shown unless manually refreshed in the launcher)\r\n(Note-2: disabling this will also delete your current saved server data cache)");
		mntmKeepServerCache.setText("Keep server cache");
		mntmKeepServerCache.setSelection(loadDelegate.getPreferences().isKeepServerDataCache());

		MenuItem mntmAutoUpdateServers = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoUpdateServers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Settings.getLauncherSettings().getPreferences()
						.setAutoUpdateServersList(mntmAutoUpdateServers.getSelection());
				saveSettings();
			}
		});
		mntmAutoUpdateServers.setToolTipText(
				"Whether should the launcher auto-retrieve latest list of servers and also download their latest data\r\n(Note: do not check this if your PC or your internet is slow)");
		mntmAutoUpdateServers.setText("Auto-Update servers on start");
		mntmAutoUpdateServers.setSelection(loadDelegate.getPreferences().isAutoUpdateServersList());

		MenuItem mntmAutoLogin = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Settings.getLauncherSettings().getPreferences().setAutoLogin(mntmAutoLogin.getSelection());
				saveSettings();
			}
		});
		mntmAutoLogin.setToolTipText("Whether should the launcher auto-log you in to the current server");
		mntmAutoLogin.setText("Auto-Login on start");
		mntmAutoLogin.setSelection(loadDelegate.getPreferences().isAutoLogin());

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.setText("About");

		compositeEntrance = new Composite(this, SWT.NONE);
		compositeEntrance.setBounds(9, 10, 329, 161);
		compositeEntrance.setLayout(null);

		CLabel lblStep1 = new CLabel(compositeEntrance, SWT.NONE);
		lblStep1.setBounds(7, 7, 80, 30);
		lblStep1.setText("Step 1.");
		lblStep1.setFont(new Font(lblStep1.getDisplay(), new FontData("Segoe UI Semibold", 16, SWT.NONE)));

		Label lblEmail = new Label(compositeEntrance, SWT.NONE);
		lblEmail.setBounds(30, 47, 35, 15);
		lblEmail.setText("Email: ");

		Label lblPassword = new Label(compositeEntrance, SWT.NONE);
		lblPassword.setBounds(30, 82, 56, 15);
		lblPassword.setText("Password: ");

		txtEmail = new Text(compositeEntrance, SWT.BORDER);
		txtEmail.setBounds(109, 44, 199, 21);
		txtEmail.setTextLimit(254);

		txtPassword = new Text(compositeEntrance, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setBounds(109, 79, 199, 21);
		txtPassword.setTextLimit(64);

		Button btnLogin = new Button(compositeEntrance, SWT.NONE);
		btnLogin.setBounds(142, 111, 56, 25);
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
		lbl1.setAlignment(SWT.CENTER);
		lbl1.setBounds(204, 116, 22, 15);
		lbl1.setText("or");

		Button btnRegister = new Button(compositeEntrance, SWT.NONE);
		btnRegister.setBounds(232, 111, 76, 25);
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
		lbl2.setBounds(92, 15, 215, 21);
		lbl2.setText("Login to the server");
		lbl2.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));

		btnLogout = new Button(compositeEntrance, SWT.NONE);
		btnLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				setEnabled(compositeNfsw, false);
				setEnabled(compositeEntrance, true);
				btnLogout.setVisible(false);
				btnLogout.setEnabled(false);
				userId = null;
				loginToken = null;
				lblStatus.setText("Status: Logged-out successfully!");
			}
		});
		btnLogout.setBounds(12, 111, 75, 25);
		btnLogout.setText("Logout");
		btnLogout.setVisible(false);
		btnLogout.setEnabled(false);

		lblStatus = new CLabel(this, SWT.BORDER | SWT.SHADOW_IN);
		lblStatus.setBounds(3, 323, 526, 26);
		lblStatus.setLeftMargin(5);
		lblStatus.setText("Status: Idle");

		compositeNfsw = new Composite(this, SWT.NONE);
		compositeNfsw.setBounds(9, 177, 512, 129);
		compositeNfsw.setLayout(null);
		compositeNfsw.setEnabled(false);

		CLabel lblStep2 = new CLabel(compositeNfsw, SWT.NONE);
		lblStep2.setBounds(7, 7, 80, 30);
		lblStep2.setText("Step 2.");
		lblStep2.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 16, SWT.NORMAL));
		lblStep2.setEnabled(false);

		CLabel lbl7 = new CLabel(compositeNfsw, SWT.NONE);
		lbl7.setBounds(92, 15, 218, 21);
		lbl7.setText("Start NFS: World");
		lbl7.setFont(SWTResourceManager.getFont("Segoe UI Semilight", 10, SWT.NORMAL));
		lbl7.setEnabled(false);

		Label lbl8 = new Label(compositeNfsw, SWT.NONE);
		lbl8.setBounds(28, 50, 96, 15);
		lbl8.setText("NFS: World Path:");
		lbl8.setEnabled(false);

		CLabel lblNfsWorldPath = new CLabel(compositeNfsw, SWT.BORDER);
		lblNfsWorldPath.setBounds(132, 46, 178, 23);
		lblNfsWorldPath.setText(loadDelegate.getClientData().getPath());
		lblNfsWorldPath.setEnabled(false);

		Button btnNfsWorldPath = new Button(compositeNfsw, SWT.NONE);
		btnNfsWorldPath.setBounds(327, 45, 33, 25);
		btnNfsWorldPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
						dialog.setFileName("nfsw");
						dialog.setFilterExtensions(new String[] { "*.exe" });
						dialog.setFilterPath("C:\\ProgramData\\Electronic Arts\\Need for Speed World");
						String result = dialog.open();
						if (result != null && !result.isEmpty()) {
							Settings.getLauncherSettings().getClientData().setPath(result);
							Settings.getLauncherSettings().getClientData().setModuleName(dialog.getFileName());
							saveSettings();
							lblNfsWorldPath.setText(result);
						} else {
							lblStatus.setText("Error: There was a problem setting the client path.");
						}
					}
				});
			}
		});
		btnNfsWorldPath.setText("...");
		btnNfsWorldPath.setEnabled(false);

		Button btnLaunchNfsWorld = new Button(compositeNfsw, SWT.NONE);
		btnLaunchNfsWorld.setBounds(427, 94, 75, 25);
		btnLaunchNfsWorld.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					LauncherSettingsType lDelegate = Settings.getLauncherSettings();
					if (lDelegate.getClientData().getPath() != null)
						if (!lDelegate.getClientData().getPath().isEmpty()) {
							new ProcessBuilder(lDelegate.getClientData().getPath(), "THANKSOBAMA",
									new URL(lDelegate.getServerData().getLiteralURL(), "nfsw/Engine.svc").toString(),
									loginToken, userId).start();
							// I'm not managing this shit, ain't nobody got time
							// for
							// that.
							lblStatus.setText("Status: NFS World launched successfully!");
						} else
							lblStatus.setText("Launch Error: NFS World path is null.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnLaunchNfsWorld.setText("Launch");
		btnLaunchNfsWorld.setEnabled(false);

		Group grpServerDetails = new Group(this, SWT.NONE);
		grpServerDetails.setBounds(344, 10, 177, 161);
		grpServerDetails.setText("Current Server Details");
		grpServerDetails.setLayout(null);

		Label lbl3 = new Label(grpServerDetails, SWT.NONE);
		lbl3.setBounds(10, 23, 27, 15);
		lbl3.setText("URL: ");

		lblServerURL = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerURL.setBounds(56, 20, 112, 21);
		lblServerURL.setAlignment(SWT.RIGHT);
		lblServerURL.setText(loadDelegate.getServerData().getUrl());

		Label lbl4 = new Label(grpServerDetails, SWT.NONE);
		lbl4.setBounds(10, 71, 79, 15);
		lbl4.setText("Active Players: ");

		CLabel lblServerActiveSessions = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerActiveSessions.setBounds(111, 68, 57, 21);
		lblServerActiveSessions.setAlignment(SWT.RIGHT);
		lblServerActiveSessions.setText((String) null);

		Label lbl5 = new Label(grpServerDetails, SWT.NONE);
		lbl5.setBounds(10, 95, 79, 15);
		lbl5.setText("Total Players: ");

		CLabel lblServerTotalPlayers = new CLabel(grpServerDetails, SWT.BORDER);
		lblServerTotalPlayers.setBounds(111, 92, 57, 21);
		lblServerTotalPlayers.setAlignment(SWT.RIGHT);
		lblServerTotalPlayers.setText((String) null);

		Label lbl6 = new Label(grpServerDetails, SWT.NONE);
		lbl6.setBounds(10, 47, 62, 15);
		lbl6.setText("HTTP Port: ");

		lblHttpPort = new CLabel(grpServerDetails, SWT.BORDER);
		lblHttpPort.setBounds(94, 44, 74, 21);
		lblHttpPort.setAlignment(SWT.RIGHT);
		lblHttpPort.setText(loadDelegate.getServerData().getHttpPort().toString());

		if (loadDelegate.getPreferences().isAutoLogin()) {
			LoginDataType lDelegate = loadDelegate.getServerData().getLoginData();
			if (lDelegate.getEmail() != null & lDelegate.getPasswordHash() != null) {
				if (!lDelegate.getEmail().isEmpty() & !lDelegate.getPasswordHash().isEmpty()) {
					txtEmail.setText(lDelegate.getEmail());
					txtPassword.setText("");
					doLogin(lDelegate.getEmail(), lDelegate.getPasswordHash());
				}
			}
		}
	}

	private void readSettings() {
		try {
			new File(dirLauncherSettings).mkdirs();
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile())
				MarshalUtil.marshalToFile(new LauncherSettingsType(), settings);

			LauncherSettingsType launcherSettings = MarshalUtil.unMarshal(settings);

			ServerDataType sDelegate = launcherSettings.getServerData();
			launcherSettings.getServerData()
					.setLiteralURL(new URL("http", sDelegate.getUrl(), sDelegate.getHttpPort(), ""));
			Settings.setLauncherSettings(launcherSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveSettings() {
		try {
			new File(dirLauncherSettings).mkdirs();
			File settings = new File(dirLauncherSettings.concat(fileLauncherSettings));
			if (settings.createNewFile())
				MarshalUtil.marshalToFile(new LauncherSettingsType(), settings);

			MarshalUtil.marshalToFile(Settings.getLauncherSettings(), settings);
		} catch (IOException e) {
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

			String param = String.format("?email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(passwordHash, StandardCharsets.UTF_8.toString()));

			URL serverAuth = new URL(Settings.getLauncherSettings().getServerData().getLiteralURL(),
					"nfsw/Engine.svc/User/AuthenticateUser".concat(param));

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
				Settings.getLauncherSettings().getServerData().getLoginData().setEmail(email);
				Settings.getLauncherSettings().getServerData().getLoginData().setPasswordHash(passwordHash);
				saveSettings();
				lblStatus.setText("Status: Logged in!");
				setEnabled(compositeNfsw, true);
				btnLogout.setVisible(true);
				btnLogout.setEnabled(true);
				compositeEntrance.setEnabled(true);
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

			String param = String.format("?email=%s&password=%s",
					URLEncoder.encode(email, StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(passwordHash, StandardCharsets.UTF_8.toString()));

			URL serverAuth = new URL(Settings.getLauncherSettings().getServerData().getLiteralURL(),
					"nfsw/Engine.svc/User/CreateUser".concat(param));
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
				Settings.getLauncherSettings().getServerData().getLoginData().setEmail(email);
				Settings.getLauncherSettings().getServerData().getLoginData().setPasswordHash(passwordHash);
				saveSettings();
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
