/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.*;

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
	private Label httpProxyHostLabel;
	private Label httpProxyPortLabel;
	private Text httpProxyHostText;
	private Text httpProxyPortText;
	private Button enableHttpProxy;

	// these two values are for compatibility with old code
	public static final String EQUIVALENT_VALUE = "equivalent";
	public static final String COMPATIBLE_VALUE = "compatible";

	/**
	 * The constructor.
	 */
	public MainPreferencePage() {
		super();
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(
			parent,
			"org.eclipse.update.ui.MainPreferencePage");

		Composite mainComposite =
			new Composite(parent, SWT.NULL);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label historySizeLabel = new Label(mainComposite, SWT.NONE);
		historySizeLabel.setText(UpdateUI.getString("MainPreferencePage.historySize"));
		historySizeText = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		historySizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		checkSignatureCheckbox =
			new Button(mainComposite, SWT.CHECK | SWT.LEFT);
		checkSignatureCheckbox.setText(UpdateUI.getString("MainPreferencePage.checkSignature"));
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
		group.setText(UpdateUI.getString("MainPreferencePage.updateVersions"));
		group.setLayout(new GridLayout());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		equivalentButton = new Button(group, SWT.RADIO);
		equivalentButton.setText(
			UpdateUI.getString("MainPreferencePage.updateVersions.equivalent"));

		compatibleButton = new Button(group, SWT.RADIO);
		compatibleButton.setText(
			UpdateUI.getString("MainPreferencePage.updateVersions.compatible"));

		createSpacer(mainComposite, 2);
		createHttpProxy(mainComposite, 2);
		performDefaults();
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
		group.setText(UpdateUI.getString("MainPreferencePage.proxyGroup"));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = columnSpan;
		group.setLayoutData(gd);

		enableHttpProxy = new Button(group, SWT.CHECK);
		enableHttpProxy.setText(UpdateUI.getString("MainPreferencePage.enableHttpProxy"));
		gd = new GridData();
		gd.horizontalSpan = 2;
		enableHttpProxy.setLayoutData(gd);

		httpProxyHostLabel = new Label(group, SWT.NONE);
		httpProxyHostLabel.setText(UpdateUI.getString("MainPreferencePage.httpProxyHost"));

		httpProxyHostText = new Text(group, SWT.SINGLE | SWT.BORDER);
		httpProxyHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		httpProxyPortLabel = new Label(group, SWT.NONE);
		httpProxyPortLabel.setText(UpdateUI.getString("MainPreferencePage.httpProxyPort"));

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
		return UpdateCore.getPlugin().getPluginPreferences().getDefaultInt(UpdateCore.P_HISTORY_SIZE);
	}
	
	public boolean performOk() {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				try {
					SiteManager.getLocalSite().setMaximumHistoryCount(getHistoryCount());
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
			equivalentButton.getSelection() ? EQUIVALENT_VALUE : COMPATIBLE_VALUE);

		UpdateCore.getPlugin().savePluginPreferences();
		return super.performOk();
	}
	
	public void performApply() {
		super.performApply();
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				SiteManager.setHttpProxyInfo(
					enableHttpProxy.getSelection(),
					httpProxyHostText.getText(),
					httpProxyPortText.getText());
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
				
		UpdateCore.getPlugin().savePluginPreferences();
	}
	
	public void performDefaults() {
		super.performDefaults();

		enableHttpProxy.setSelection(SiteManager.isHttpProxyEnable());
		String serverValue = SiteManager.getHttpProxyServer();
		if (serverValue != null)
			httpProxyHostText.setText(serverValue);
		String portValue = SiteManager.getHttpProxyPort();
		if (portValue != null)
			httpProxyPortText.setText(portValue);

		httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostText.setEnabled(enableHttpProxy.getSelection());

		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();
		checkSignatureCheckbox.setSelection(
			prefs.getBoolean(UpdateCore.P_CHECK_SIGNATURE));
		historySizeText.setText(prefs.getString(UpdateCore.P_HISTORY_SIZE));
		boolean isCompatible =
			UpdateCore.COMPATIBLE_VALUE.equals(
				prefs.getString(UpdateCore.P_UPDATE_VERSIONS));
		equivalentButton.setSelection(!isCompatible);
		compatibleButton.setSelection(isCompatible);
	}

	private void warnSignatureCheck(Shell shell) {
		MessageDialog.openWarning(
			shell,
			UpdateUI.getString("MainPreferencePage.digitalSignature.title"),
			UpdateUI.getString("MainPreferencePage.digitalSignature.message"));
	}
}