/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20070123   154100 eu@md.pp.ru - Eugene Kuleshov, Initial UI coding
 * 20070201   154100 pmoogk@ca.ibm.com - Peter Moogk, Port internet code from WTP to Eclipse base.
 * 20070219   174674 pmoogk@ca.ibm.com - Peter Moogk
 * 20070402   180622 brockj@tpg.com.au - Brock Janiczak, Inconsistent enablement states in network preference page
 * 20070416   177897 ymnk@jcraft.com - Atsuhiko Yamanaka, Improve UI of Proxy Preferences Page
 *******************************************************************************/
package org.eclipse.ui.internal.net;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProxyPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	Entry[] entryList;
	Button directConnectionToButton;
	private Button manualProxyConfigurationButton;
	Button useSameProxyButton;
	private Label nonHostLabel;
	private NonProxyHostsComposite nonHostComposite;
	Button enableProxyAuth;
	private Label useridLabel;
	private Label passwordLabel;
	Text userid;
	Text password;
	private IProxyService proxyService;

	public ProxyPreferencePage() {
		super(NetUIMessages.ProxyPreferencePage_2);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
		// Nothing to do
	}

	protected Control createContents(Composite parent) {

		proxyService = Activator.getDefault().getProxyService();
		if (proxyService == null) {
			Label l = new Label(parent, SWT.NONE);
			l.setText(NetUIMessages.ProxyPreferencePage_40);
			return l;
		}

		IProxyData[] proxyData = proxyService.getProxyData();
		entryList = new Entry[proxyData.length];
		for (int i = 0; i < proxyData.length; i++) {
			IProxyData pd = proxyData[i];
			entryList[i] = new Entry(pd);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 5;
		layout.marginTop = 5;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		directConnectionToButton = new Button(composite, SWT.RADIO);
		directConnectionToButton.setLayoutData(new GridData());
		directConnectionToButton.setText(NetUIMessages.ProxyPreferencePage_3);
		directConnectionToButton
				.setToolTipText(NetUIMessages.ProxyPreferencePage_1);
		directConnectionToButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableControls(!directConnectionToButton.getSelection());
			}
		});

		manualProxyConfigurationButton = new Button(composite, SWT.RADIO);
		manualProxyConfigurationButton
				.setText(NetUIMessages.ProxyPreferencePage_4);
		manualProxyConfigurationButton
				.setToolTipText(NetUIMessages.ProxyPreferencePage_0);

		final Composite manualProxyConfigurationComposite = new Composite(
				composite, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 4;
		manualProxyConfigurationComposite.setLayout(gridLayout);
		final GridData gridData_2 = new GridData();
		gridData_2.horizontalIndent = 17;
		manualProxyConfigurationComposite.setLayoutData(gridData_2);

		entryList[0].addProtocolEntry(manualProxyConfigurationComposite);

		new Label(manualProxyConfigurationComposite, SWT.NONE);
		useSameProxyButton = new Button(manualProxyConfigurationComposite,
				SWT.CHECK);
		useSameProxyButton.setText(NetUIMessages.ProxyPreferencePage_5);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		useSameProxyButton.setLayoutData(gridData);
		useSameProxyButton.setToolTipText(NetUIMessages.ProxyPreferencePage_23);
		useSameProxyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toggleUseSameProtocol();
			}
		});

		for (int index = 1; index < entryList.length; index++) {
			entryList[index]
					.addProtocolEntry(manualProxyConfigurationComposite);
		}

		Label separator = new Label(manualProxyConfigurationComposite,
				SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(newGridData(4, 5, true, false));
		nonHostLabel = new Label(manualProxyConfigurationComposite, SWT.NONE);
		nonHostLabel.setText(NetUIMessages.ProxyPreferencePage_6);
		nonHostLabel.setToolTipText(NetUIMessages.ProxyPreferencePage_24);
		nonHostLabel.setLayoutData(newGridData(4, 5, true, false));
		nonHostComposite = new NonProxyHostsComposite(
				manualProxyConfigurationComposite, SWT.NONE);
		nonHostComposite.setLayoutData(newGridData(4, -1, true, false));

		Label separator2 = new Label(manualProxyConfigurationComposite,
				SWT.HORIZONTAL | SWT.SEPARATOR);
		separator2.setLayoutData(newGridData(4, 5, true, false));
		enableProxyAuth = new Button(manualProxyConfigurationComposite,
				SWT.CHECK);
		enableProxyAuth.setText(NetUIMessages.ProxyPreferencePage_7);
		enableProxyAuth.setLayoutData(newGridData(4, 5, true, false));
		enableProxyAuth.setToolTipText(NetUIMessages.ProxyPreferencePage_25);
		enableProxyAuth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableUseridPassword(enableProxyAuth.getSelection());
			}
		});

		final Composite userIdPassword = new Composite(
				manualProxyConfigurationComposite, SWT.NONE);
		GridLayout layout2 = new GridLayout(2, false);
		layout2.marginWidth = 0;
		layout2.marginHeight = 0;
		userIdPassword.setLayoutData(newGridData(4, -1, true, false));
		userIdPassword.setLayout(layout2);

		useridLabel = new Label(userIdPassword, SWT.NONE);
		useridLabel.setText(NetUIMessages.ProxyPreferencePage_8);
		userid = new Text(userIdPassword, SWT.BORDER);
		passwordLabel = new Label(userIdPassword, SWT.NONE);
		passwordLabel.setText(NetUIMessages.ProxyPreferencePage_9);
		password = new Text(userIdPassword, SWT.BORDER);

		userid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userid.setToolTipText(NetUIMessages.ProxyPreferencePage_26);
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password.setToolTipText(NetUIMessages.ProxyPreferencePage_27);
		password.setEchoChar('*');

		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (useSameProxyButton.getSelection()) {
					for (int index = 1; index < entryList.length; index++) {
						entryList[index].copyValuesFrom(entryList[0]);
					}
				}
			}
		};

		entryList[0].hostname.addModifyListener(modifyListener);
		entryList[0].port.addModifyListener(modifyListener);

		initializeValues(proxyService.isProxiesEnabled());
		applyDialogFont(composite);

		return composite;
	}

	protected void performApply() {
		if (proxyService == null)
			return;
		boolean proxiesEnabled = manualProxyConfigurationButton.getSelection();

		// Save the contents of the text fields to the proxy data.
		IProxyData[] proxyData = new IProxyData[entryList.length];
		for (int index = 0; index < entryList.length; index++) {
			entryList[index].applyValues();
			proxyData[index] = entryList[index].getProxy();
		}

		proxyService.setProxiesEnabled(proxiesEnabled);
		if (proxiesEnabled) {
			try {
				proxyService.setProxyData(proxyData);
				proxyService.setNonProxiedHosts(
						nonHostComposite.getList());
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), null, null, e.getStatus());
			}
		}
		Activator.getDefault().savePluginPreferences();
	}

	protected void performDefaults() {
		directConnectionToButton.setSelection(true);
		manualProxyConfigurationButton.setSelection(false);
		useSameProxyButton.setSelection(false);
		enableProxyAuth.setSelection(false);

		for (int index = 0; index < entryList.length; index++) {
			entryList[index].reset();
		}

		nonHostComposite.setList(new String[] { "localhost", "127.0.0.1" }); //$NON-NLS-1$ //$NON-NLS-2$
		userid.setText(""); //$NON-NLS-1$
		password.setText(""); //$NON-NLS-1$
		enableControls(false);
	}

	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	/**
	 * This method is run once when when this preference page is displayed. It
	 * will restore the state of this page using previously saved preferences.
	 * 
	 * @param proxiesEnabled indicates if manual proxies are enabled or not.
	 */
	private void initializeValues(boolean proxiesEnabled) {

		directConnectionToButton.setSelection(!proxiesEnabled);
		manualProxyConfigurationButton.setSelection(proxiesEnabled);

		String[] nonHostLists = null;
		if (proxyService != null)
			nonHostLists = proxyService.getNonProxiedHosts();
		this.nonHostComposite.setList(nonHostLists == null ? new String[] {
				"localhost", "127.0.0.1" } : nonHostLists); //$NON-NLS-1$ //$NON-NLS-2$
		if (!proxiesEnabled) {
			this.useSameProxyButton.setSelection(false);
			this.enableProxyAuth.setSelection(false);
			this.userid.setText(""); //$NON-NLS-1$
			this.password.setText(""); //$NON-NLS-1$
		} else {
			IProxyData data = entryList[0].getProxy();
			this.enableProxyAuth.setSelection(data.isRequiresAuthentication());
			this.userid.setText(data.getUserId() == null ? "" : data //$NON-NLS-1$
					.getUserId());
			this.password.setText(data.getPassword() == null ? "" : data //$NON-NLS-1$
					.getPassword());

		}
		boolean useSameProtocol = true;
		for (int i = 1; i < entryList.length; i++) {
			Entry entry = entryList[i];
			if (!entry.isUseSameProtocol(entryList[0])) {
				useSameProtocol = false;
				break;
			}
		}
		this.useSameProxyButton.setSelection(useSameProtocol);
		for (int index = 1; index < entryList.length; index++) {
			Entry entry = entryList[index];
			entry.loadPreviousValues();
			entry.updateEnablement(proxiesEnabled, useSameProtocol);
		}

		enableControls(proxiesEnabled);
	}

	void enableControls(boolean enabled) {
		for (int index = 0; index < entryList.length; index++) {
			entryList[index].updateEnablement(enabled, useSameProxyButton.getSelection());
		}

		useSameProxyButton.setEnabled(enabled);
		nonHostLabel.setEnabled(enabled);
		nonHostComposite.enableComposite(enabled);
		enableProxyAuth.setEnabled(enabled);

		enableUseridPassword(enableProxyAuth.getSelection() && enabled);
	}

	void enableUseridPassword(boolean enabled) {
		useridLabel.setEnabled(enabled);
		userid.setEnabled(enabled);
		passwordLabel.setEnabled(enabled);
		password.setEnabled(enabled);
	}

	protected void toggleUseSameProtocol() {
		boolean useSameProtocol = useSameProxyButton.getSelection();
		for (int index = 1; index < entryList.length; index++) {
			entryList[index].setUseSameProtocol(useSameProtocol, entryList[0]);
		}
	}
	
	public void updateErrorMessage() {
		for (int index = 0; index < entryList.length; index++) {
			String message = entryList[index].getErrorMessage();
			if (message != null) {
				setErrorMessage(message);
				return;
			}
		}
		setErrorMessage(null);
	}

	private GridData newGridData(int span, int verticalIndent,
			boolean horizontal, boolean vertical) {
		int style = 0;

		style |= horizontal ? GridData.FILL_HORIZONTAL : 0;
		style |= vertical ? GridData.FILL_VERTICAL : 0;

		GridData gridData = new GridData(style);

		if (span != -1) {
			gridData.horizontalSpan = span;
		}

		if (verticalIndent != -1) {
			gridData.verticalIndent = verticalIndent;
		}

		return gridData;
	}

	private class Entry {
		Label nameLabel;
		Text hostname;
		Label portLabel;
		Text port;
		String prevHostname;
		int prevPort;
		private final IProxyData proxyData;
		private String errorMessage;

		public Entry(IProxyData proxyData) {
			this.proxyData = proxyData;
		}

		public void updateEnablement(boolean proxiesEnabled, boolean useSameProtocol) {
			if (isHttpProxy()) {
				setEnabled(proxiesEnabled);
			} else {
				if (proxiesEnabled) {
					setUseSameProtocol(useSameProtocol);
				} else {
					setEnabled(false);
				}
			}
			updateMessage();
		}

		public void setUseSameProtocol(boolean useSameProtocol, Entry httpEntry) {
			setUseSameProtocol(useSameProtocol);
			if (isSocksProxy() || isHttpProxy())
				return;
			if (useSameProtocol) {
				recordPreviousValues();
				copyValuesFrom(entryList[0]);
			} else {
				restorePreviousValues();
			}
		}
		
		public void setUseSameProtocol(boolean useSameProtocol) {
			if (isSocksProxy()) {
				setEnabled(true);
			} else {
				setEnabled(!useSameProtocol);
			}
		}

		public void loadPreviousValues() {
			IProxyData proxy = getProxy();
			prevHostname = proxy.getHost();
			prevPort = proxy.getPort();
		}
		
		public void restorePreviousValues() {
			hostname.setText(prevHostname);
			port.setText(prevPort == -1 ? "" : String //$NON-NLS-1$
					.valueOf(prevPort));
		}

		public void recordPreviousValues() {
			prevHostname = hostname.getText();
			try {
				prevPort = Integer.parseInt(port.getText());
			} catch (NumberFormatException e) {
				prevPort = -1;
			}
		}

		/**
		 * The user has chosen to use the same entry for all protocols
		 * @param entry the entry (the http entry to be exact)
		 */
		public void copyValuesFrom(Entry entry) {
			// For SOCKS, don't use the general entry
			if (!isSocksProxy()){
	            hostname.setText( entry.hostname.getText() );
	            port.setText( entry.port.getText() );
			}
		}
		
		public boolean isUseSameProtocol(Entry entry) {
			// Always answer true for the SOCKS proxy since we never disable
			if (isSocksProxy())
				return true;
			return (hostsEqual(proxyData.getHost(), entry.getProxy().getHost()) 
					&& portsEqual(proxyData.getPort(), entry.getProxy().getPort()));
		}

		private boolean portsEqual(int port1, int port2) {
			return port1 == port2;
		}

		private boolean hostsEqual(String host1, String host2) {
			if (host1 == host2)
				// If there are no hosts, don't enable the use same hosts button
				return false;
			if (host1 == null || host2 == null)
				return false;
			return host1.equals(host2);
		}
		
		boolean isSocksProxy() {
			return getProxy().getType().equals(IProxyData.SOCKS_PROXY_TYPE);
		}
		
		boolean isHttpProxy() {
			return getProxy().getType().equals(IProxyData.HTTP_PROXY_TYPE);
		}
		
		boolean isHttpsProxy() {
			return getProxy().getType().equals(IProxyData.HTTPS_PROXY_TYPE);
		}

		public IProxyData getProxy() {
			return proxyData;
		}

		public boolean updateMessage() {
			if (hostname.isEnabled()) {
				if (isSocksProxy() || isHttpProxy()|| !useSameProxyButton.getSelection()) {
					String hostString = hostname.getText();
					if (hostString.startsWith(" ") || hostString.endsWith(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
						setErrorMessage(NetUIMessages.ProxyPreferencePage_41);
						return false;
					}
					String portString = port.getText();
					if (portString.length() > 0) {
						try {
							int port = Integer.valueOf(portString).intValue();
							if (port < 0) {
								setErrorMessage(NetUIMessages.ProxyPreferencePage_42);
								return false;
							}
						} catch (NumberFormatException e) {
							setErrorMessage(NetUIMessages.ProxyPreferencePage_43);
							return false;
						}
					}
				}
			}
			setErrorMessage(null);
			return true;
		}
		
		private void setErrorMessage(String message) {
			errorMessage = message;
			updateErrorMessage();
		}
		
		public void applyValues() {
			IProxyData proxy = getProxy();
			boolean enableAuth = enableProxyAuth.getSelection();

			String hostString;
			String portString;
			if (useSameProxyButton.getSelection() && !isSocksProxy()) {
				hostString = entryList[0].hostname.getText();
				portString = entryList[0].port.getText();
			} else {
				hostString = hostname.getText();
				portString = port.getText();
			}
			
			proxy.setHost(hostString);
			try {
				int port = Integer.valueOf(portString).intValue();
				proxy.setPort(port);
			} catch (NumberFormatException e) {
				proxy.setPort(-1);
			}
			proxy.setUserid(enableAuth ? userid.getText() : null);
			proxy.setPassword(enableAuth ? password.getText() : null);
		}
		
		public void addProtocolEntry(Composite parent) {
			GridData gridData;
			IProxyData proxy = getProxy();

			nameLabel = new Label(parent, SWT.NONE);
			nameLabel.setText(getLabel(proxy));

			hostname = new Text(parent, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = 120;
			hostname.setLayoutData(gridData);
			hostname.setText(getHostName(proxy));
			hostname.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Entry.this.updateMessage();
				}
			});

			portLabel = new Label(parent, SWT.NONE);
			portLabel.setText(NetUIMessages.ProxyPreferencePage_22);

			port = new Text(parent, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = 50;
			port.setLayoutData(gridData);
			port.setText(getPortString(proxy));
			port.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Entry.this.updateMessage();
				}
			});
		}

		private String getLabel(IProxyData data) {
			if (data.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
				return NetUIMessages.ProxyPreferencePage_37;
			}
			if (data.getType().equals(IProxyData.HTTPS_PROXY_TYPE)) {
				return NetUIMessages.ProxyPreferencePage_38;
			}
			if (data.getType().equals(IProxyData.SOCKS_PROXY_TYPE)) {
				return NetUIMessages.ProxyPreferencePage_39;
			}
			return ""; //$NON-NLS-1$
		}
		
		private String getPortString(IProxyData proxy) {
			int portInt = proxy.getPort();
			String portString;
			if (portInt == -1) {
				portString = ""; //$NON-NLS-1$
			} else {
				portString = String.valueOf(portInt);
			}
			return portString;
		}

		private String getHostName(IProxyData proxy) {
			String hostnameString = proxy.getHost();
			if (hostnameString == null) {
				hostnameString = ""; //$NON-NLS-1$
			}
			return hostnameString;
		}
		
		public void reset() {
			hostname.setText(""); //$NON-NLS-1$
			port.setText(""); //$NON-NLS-1$
			prevHostname = ""; //$NON-NLS-1$
			prevPort = -1;
		}
		
		private void setEnabled(boolean enabled) {
			hostname.setEnabled(enabled);
			nameLabel.setEnabled(enabled);
			portLabel.setEnabled(enabled);
			port.setEnabled(enabled);
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
