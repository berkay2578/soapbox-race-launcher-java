package br.com.soapboxrace.launcher.forms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class ServerSelection extends Dialog {

	protected String[] server;
	protected Shell shlServerSelection;

	private List listServer;
	private CLabel lblStatus;
	private Button btnSelect;

	private String dirServerList = "launcher/";
	private String fileServerList = "serverlist.txt";

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ServerSelection(Shell parent, int style) {
		super(parent, style);
		setText("Server Selection");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public String[] open() {
		createContents();
		downloadServerList(false);
		shlServerSelection.open();
		shlServerSelection.layout();
		Display display = getParent().getDisplay();
		while (!shlServerSelection.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return server;
	}

	private void downloadServerList(boolean deleteExistingList) {
		lblStatus.setText("Status: Refreshing server list...");
		new File(dirServerList).mkdir();
		File list = new File(dirServerList.concat(fileServerList));
		try {
			if (deleteExistingList)
				if (list.exists())
					list.delete();
			if (list.createNewFile()) {
				URL soapboxServers = new URL(
						"https://raw.githubusercontent.com/nilzao/soapbox-race-hill/master/serverlist.txt"); // temporary
				try (ReadableByteChannel rbc = Channels.newChannel(soapboxServers.openStream())) {
					try (FileOutputStream fos = new FileOutputStream(list)) {
						fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
					}
				}
			}
			listServer.setItems(new String[] {});
			btnSelect.setEnabled(false);
			try (BufferedReader br = new BufferedReader(new FileReader(list))) {
				String serverURL = br.readLine();
				while (serverURL != null && !serverURL.isEmpty()) {
					listServer.add(serverURL);
					serverURL = br.readLine();
				}
			}
			lblStatus.setText("Status: Idle");
		} catch (IOException e) {
			lblStatus.setText("Error: Couldn't download server list.");
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlServerSelection = new Shell(getParent(), getStyle());
		shlServerSelection.setSize(441, 283);
		shlServerSelection.setText("Server Selection");
		shlServerSelection.setLayout(null);

		listServer = new List(shlServerSelection, SWT.BORDER | SWT.V_SCROLL);
		listServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(listServer.getSelection()[0]);
				btnSelect.setEnabled(true);
			}
		});
		listServer.setItems(new String[] {});
		listServer.setBounds(3, 6, 231, 218);

		lblStatus = new CLabel(shlServerSelection, SWT.BORDER | SWT.SHADOW_IN);
		lblStatus.setLeftMargin(5);
		lblStatus.setBounds(0, 233, 435, 21);
		lblStatus.setText("Status: Idle");

		Canvas canvasServerImage = new Canvas(shlServerSelection, SWT.BORDER);
		canvasServerImage.setBounds(240, 6, 185, 68);

		CLabel lblServerDescription = new CLabel(shlServerSelection, SWT.BORDER | SWT.SHADOW_IN | SWT.SHADOW_OUT);
		lblServerDescription.setAlignment(SWT.CENTER);
		lblServerDescription.setBounds(240, 80, 185, 114);
		lblServerDescription.setText("");

		Button btnRefresh = new Button(shlServerSelection, SWT.NONE);
		btnRefresh.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				downloadServerList(true);
			}
		});
		btnRefresh.setBounds(240, 200, 90, 25);
		btnRefresh.setText("Refresh");

		btnSelect = new Button(shlServerSelection, SWT.NONE);
		btnSelect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				String[] serverData = new String[2];
				String[] tempArr = listServer.getSelection()[0].split(":");
				serverData[0] = (tempArr[0].startsWith("http") ? tempArr[0].concat(":").concat(tempArr[1])
						: tempArr[0]);
				serverData[1] = (tempArr[0].startsWith("http") ? tempArr[2] : tempArr[1]);
				server = serverData;
				shlServerSelection.close();
			}
		});
		btnSelect.setEnabled(false);
		btnSelect.setBounds(336, 200, 89, 25);
		btnSelect.setText("Select");
	}
}
