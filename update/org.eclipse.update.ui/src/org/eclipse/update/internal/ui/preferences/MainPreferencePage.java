/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.preferences;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class MainPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	//private Label historySizeLabel;
	private Text historySizeText;
	private Button checkSignatureCheckbox;
	private Button equivalentButton;
	private Button compatibleButton;
	private Text updatePolicyText;
	private Label httpProxyHostLabel;
	private Label httpProxyPortLabel;
	private Text httpProxyHostText;
	private Text httpProxyPortText;
	private Button enableHttpProxy;

	// these two values are for compatibility with old code
	public static final String EQUIVALENT_VALUE = "equivalent"; //$NON-NLS-1$
	public static final String COMPATIBLE_VALUE = "compatible"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public MainPreferencePage() {
		super();
	}

	/**
	 * Insert the method's description here.
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.MainPreferencePage"); //$NON-NLS-1$

		Composite mainComposite = new Composite(parent, SWT.NULL);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label historySizeLabel = new Label(mainComposite, SWT.NONE);
		historySizeLabel.setText(UpdateUI.getString("MainPreferencePage.historySize")); //$NON-NLS-1$
		historySizeText = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		historySizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		checkSignatureCheckbox =
			new Button(mainComposite, SWT.CHECK | SWT.LEFT);
		checkSignatureCheckbox.setText(UpdateUI.getString("MainPreferencePage.checkSignature")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		checkSignatureCheckbox.setLayoutData(gd);
		checkSignatureCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (checkSignatureCheckbox.getSelection() == false) {
					warnSignatureCheck(getShell());
				}
			}
		});

		createSpacer(mainComposite, 2);

		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(UpdateUI.getString("MainPreferencePage.updateVersions")); //$NON-NLS-1$
		group.setLayout(new GridLayout());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		equivalentButton = new Button(group, SWT.RADIO);
		equivalentButton.setText(UpdateUI.getString("MainPreferencePage.updateVersions.equivalent")); //$NON-NLS-1$

		compatibleButton = new Button(group, SWT.RADIO);
		compatibleButton.setText(UpdateUI.getString("MainPreferencePage.updateVersions.compatible")); //$NON-NLS-1$

		createSpacer(mainComposite, 2);

		group = new Group(mainComposite, SWT.NONE);
		group.setText(UpdateUI.getString("MainPreferencePage.updatePolicy")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NULL);
		label.setText(UpdateUI.getString("MainPreferencePage.updatePolicyURL")); //$NON-NLS-1$
		updatePolicyText = new Text(group, SWT.SINGLE | SWT.BORDER);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		updatePolicyText.setLayoutData(gd);

		createSpacer(mainComposite, 2);
		createHttpProxy(mainComposite, 2);
		initialize();
		updatePolicyText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		});
		return mainComposite;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		Dialog.applyDialogFont(getControl());
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}

	protected void createHttpProxy(Composite composite, int columnSpan) {
		Group group = new Group(composite, SWT.NONE);
		group.setText(UpdateUI.getString("MainPreferencePage.proxyGroup")); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = columnSpan;
		group.setLayoutData(gd);

		enableHttpProxy = new Button(group, SWT.CHECK);
		enableHttpProxy.setText(UpdateUI.getString("MainPreferencePage.enableHttpProxy")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		enableHttpProxy.setLayoutData(gd);

		httpProxyHostLabel = new Label(group, SWT.NONE);
		httpProxyHostLabel.setText(UpdateUI.getString("MainPreferencePage.httpProxyHost")); //$NON-NLS-1$

		httpProxyHostText = new Text(group, SWT.SINGLE | SWT.BORDER);
		httpProxyHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		httpProxyPortLabel = new Label(group, SWT.NONE);
		httpProxyPortLabel.setText(UpdateUI.getString("MainPreferencePage.httpProxyPort")); //$NON-NLS-1$

		httpProxyPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
		httpProxyPortText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableHttpProxy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean enable = enableHttpProxy.getSelection();
				httpProxyPortLabel.setEnabled(enable);
				httpProxyHostLabel.setEnabled(enable);
				httpProxyPortText.setEnabled(enable);
				httpProxyHostText.setEnabled(enable);
			}
		});

	}

	private int getHistoryCount() {
		try {
			Integer count = new Integer(historySizeText.getText());
			return count.intValue();
		} catch (NumberFormatException e) {
		}
		return UpdateCore.getPlugin().getPluginPreferences().getDefaultInt(
			UpdateCore.P_HISTORY_SIZE);
	}

	public boolean performOk() {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				try {
					SiteManager.getLocalSite().setMaximumHistoryCount(
						getHistoryCount());
					SiteManager.setHttpProxyInfo(
						enableHttpProxy.getSelection(),
						httpProxyHostText.getText(),
						httpProxyPortText.getText());
				} catch (CoreException e) {
					UpdateUI.logException(e);
				}
			}
		});

		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();
		prefs.setValue(
			UpdateCore.P_CHECK_SIGNATURE,
			checkSignatureCheckbox.getSelection());
		prefs.setValue(UpdateCore.P_HISTORY_SIZE, historySizeText.getText());
		prefs.setValue(
			UpdateCore.P_UPDATE_VERSIONS,
			equivalentButton.getSelection()
				? EQUIVALENT_VALUE
				: COMPATIBLE_VALUE);
		prefs.setValue(
			UpdateUtils.P_UPDATE_POLICY_URL,
			updatePolicyText.getText());

		UpdateCore.getPlugin().savePluginPreferences();
		return super.performOk();
	}


	private void initialize() {
		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();
		
		enableHttpProxy.setSelection(prefs.getBoolean(UpdateCore.HTTP_PROXY_ENABLE));
		String serverValue = prefs.getString(UpdateCore.HTTP_PROXY_HOST);
		if (serverValue != null)
			httpProxyHostText.setText(serverValue);
		String portValue = prefs.getString(UpdateCore.HTTP_PROXY_PORT);
		if (portValue != null)
			httpProxyPortText.setText(portValue);

		httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostText.setEnabled(enableHttpProxy.getSelection());

		checkSignatureCheckbox.setSelection(
			prefs.getBoolean(UpdateCore.P_CHECK_SIGNATURE));

		historySizeText.setText(prefs.getString(UpdateCore.P_HISTORY_SIZE));

		boolean isCompatible =
			UpdateCore.COMPATIBLE_VALUE.equals(
				prefs.getString(UpdateCore.P_UPDATE_VERSIONS));
		equivalentButton.setSelection(!isCompatible);
		compatibleButton.setSelection(isCompatible);

		String text = prefs.getString(UpdateUtils.P_UPDATE_POLICY_URL);
		updatePolicyText.setText(text);
	}

	private void textChanged() {
		String text = updatePolicyText.getText();
		if (text.length() > 0) {
			try {
				new URL(text);
			} catch (MalformedURLException e) {
				setValid(false);
				setErrorMessage(UpdateUI.getString("UpdateSettingsPreferencePage.invalid")); //$NON-NLS-1$
				return;
			}
		}
		setValid(true);
		setErrorMessage(null);
	}

	public void performDefaults() {
		super.performDefaults();
		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();

		enableHttpProxy.setSelection(false);
		httpProxyHostText.setText(""); //$NON-NLS-1$
		httpProxyPortText.setText(""); //$NON-NLS-1$
		httpProxyPortLabel.setEnabled(false);
		httpProxyHostLabel.setEnabled(false);
		httpProxyPortText.setEnabled(false);
		httpProxyHostText.setEnabled(false);

		updatePolicyText.setText(""); //$NON-NLS-1$

		checkSignatureCheckbox.setSelection(true);
		historySizeText.setText(
			prefs.getDefaultString(UpdateCore.P_HISTORY_SIZE));
		equivalentButton.setSelection(true);
		compatibleButton.setSelection(false);
	}

	private void warnSignatureCheck(Shell shell) {
		MessageDialog.openWarning(shell, UpdateUI.getString("MainPreferencePage.digitalSignature.title"), //$NON-NLS-1$
		UpdateUI.getString("MainPreferencePage.digitalSignature.message")); //$NON-NLS-1$
	}
}
