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
	IProxyData[] proxyData;

	Entry[] entryList;

	Button directConnectionToButton;

	private Button manualProxyConfigurationButton;

	Button useSameProxyButton;

	private Text nonHostLabel;

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
		proxyData = proxyService.getProxyData();
		entryList = new Entry[proxyData.length];

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

		addProtocolEntry(manualProxyConfigurationComposite, 0);

		new Label(manualProxyConfigurationComposite, SWT.NONE);
		useSameProxyButton = new Button(manualProxyConfigurationComposite,
				SWT.CHECK);
		useSameProxyButton.setText(NetUIMessages.ProxyPreferencePage_5);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		useSameProxyButton.setLayoutData(gridData);
		useSameProxyButton
				.setToolTipText(NetUIMessages.ProxyPreferencePage_23);
		useSameProxyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleUseSameProtocol(false);
			}
		});

		for (int index = 1; index < proxyData.length; index++) {
			addProtocolEntry(manualProxyConfigurationComposite, index);
		}

		Label separator = new Label(manualProxyConfigurationComposite,
				SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(newGridData(4, 5, true, false));
		nonHostLabel = new Text(manualProxyConfigurationComposite,
				SWT.READ_ONLY);
		nonHostLabel.setText(NetUIMessages.ProxyPreferencePage_6);
		nonHostLabel
				.setToolTipText(NetUIMessages.ProxyPreferencePage_24);
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
		enableProxyAuth
				.setToolTipText(NetUIMessages.ProxyPreferencePage_25);
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

    ModifyListener modifyListener = new ModifyListener()
    {
      public void modifyText(ModifyEvent e)
      {
        if( useSameProxyButton.getSelection() )
        {
          Entry  httpEntry    = entryList[0];
          String httpHostname = httpEntry.hostname.getText();
          String httpPort     = httpEntry.port.getText();
          
          for( int index = 1; index < entryList.length; index++ )
          {
            Entry entry = entryList[index];
            
            entry.hostname.setText( httpHostname );
            entry.port.setText( httpPort );
          }
        }
      }
    };
    
    entryList[0].hostname.addModifyListener( modifyListener );
    entryList[0].port.addModifyListener( modifyListener );
    
		restoreState(proxyService.isProxiesEnabled());
		applyDialogFont(composite);

		return composite;
	}

	protected void performApply() {
		if (proxyService == null)
			return;
		boolean proxiesEnabled = manualProxyConfigurationButton.getSelection();

		// Save the contents of the text fields to the proxy data.
		for (int index = 0; index < entryList.length; index++) {
			entryList[index].applyValues();
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
			Entry entry = entryList[index];

			entry.hostname.setText(""); //$NON-NLS-1$
			entry.port.setText(""); //$NON-NLS-1$
			entry.prevHostname = ""; //$NON-NLS-1$
			entry.prevPort = -1;
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
	private void restoreState(boolean proxiesEnabled) {

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
			boolean useSameProtocol = true;
			for (int i = 1; i < proxyData.length; i++) {
				IProxyData data = proxyData[i];
				boolean same = (hostsEqual(data.getHost(), proxyData[0]
						.getHost()) && portsEqual(data.getPort(), proxyData[0]
						.getPort()));
				if (!same) {
					useSameProtocol = false;
					break;
				}
			}
			this.useSameProxyButton.setSelection(useSameProtocol);
			IProxyData data = entryList[0].getProxy();
			this.enableProxyAuth.setSelection(data.isRequiresAuthentication());
			this.userid.setText(data.getUserId() == null ? "" : data //$NON-NLS-1$
					.getUserId());
			this.password.setText(data.getPassword() == null ? "" : data //$NON-NLS-1$
					.getPassword());

			// TODO: Why does this loop from 1 and not 0?
			for (int index = 1; index < entryList.length; index++) {
				entryList[index].restoreValues();
			}
		}

		enableControls(proxiesEnabled);
		handleUseSameProtocol(true);
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

	void enableControls(boolean enabled) {
		for (int index = 0; index < entryList.length; index++) {
			Entry entry = entryList[index];

			entry.hostname.setEnabled(enabled);
			entry.nameLabel.setEnabled(enabled);
			entry.portLabel.setEnabled(enabled);
			entry.port.setEnabled(enabled);
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

	void handleUseSameProtocol(boolean init) {
		String httpHostname = entryList[0].hostname.getText();
		String httpPort = entryList[0].port.getText();
		boolean proxiesEnabled = manualProxyConfigurationButton.getSelection();
		boolean useSameProtocol = useSameProxyButton.getSelection();
		boolean controlEnabled = proxiesEnabled && !useSameProtocol;

		for (int index = 1; index < entryList.length; index++) {
			Entry entry = entryList[index];

			entry.hostname.setEnabled(controlEnabled);
			entry.nameLabel.setEnabled(controlEnabled);
			entry.portLabel.setEnabled(controlEnabled);
			entry.port.setEnabled(controlEnabled);

			if (useSameProtocol) {
				if (!init) {
					entry.prevHostname = entry.hostname.getText();
					try {
						entry.prevPort = Integer.parseInt(entry.port.getText());
					} catch (NumberFormatException e) {
						entry.prevPort = -1;
					}
				}

				entry.hostname.setText(httpHostname);
				entry.port.setText(httpPort);
			} else {
				if (!init) {
					entry.hostname.setText(entry.prevHostname);
					entry.port.setText(entry.prevPort == -1 ? "" : String //$NON-NLS-1$
							.valueOf(entry.prevPort));
				}
			}
		}
	}

	private void addProtocolEntry(Composite parent, int index) {
		Entry entry = new Entry(index);
		GridData gridData = null;
		IProxyData proxy = entry.getProxy();
		String name = getLabel(proxy);

		String hostname = proxy.getHost();
		if (hostname == null) {
			hostname = ""; //$NON-NLS-1$
		}

		int portInt = proxy.getPort();
		String port;
		if (portInt == -1) {
			port = ""; //$NON-NLS-1$
		} else {
			port = String.valueOf(portInt);
		}

		entry.nameLabel = new Label(parent, SWT.NONE);
		entry.nameLabel.setText(name);

		entry.hostname = new Text(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 120;
		entry.hostname.setLayoutData(gridData);
		entry.hostname.setText(hostname);

		entry.portLabel = new Label(parent, SWT.NONE);
		entry.portLabel.setText(NetUIMessages.ProxyPreferencePage_22);

		entry.port = new Text(parent, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 50;
		entry.port.setLayoutData(gridData);
		entry.port.setText(port);
		entryList[index] = entry;
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

		String prevHostname;

		Label portLabel;

		Text port;

		int prevPort;

		private int proxyIndex;

		public Entry(int index) {
			this.proxyIndex = index;
		}

		public IProxyData getProxy() {
			return proxyData[proxyIndex];
		}

		public void restoreValues() {
			IProxyData proxy = getProxy();
			prevHostname = proxy.getHost();
			prevPort = proxy.getPort();
		}

		public void applyValues() {
			IProxyData proxy = getProxy();
			boolean enableAuth = enableProxyAuth.getSelection();

			String hostString;
			String portString;
			if (useSameProxyButton.getSelection()) {
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
	}
}
