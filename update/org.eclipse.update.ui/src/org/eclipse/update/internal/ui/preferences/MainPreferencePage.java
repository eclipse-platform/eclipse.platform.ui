/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;

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
	private Button automaticallyChooseMirrorCheckbox;
	private Button equivalentButton;
	private Button compatibleButton;
	private Text updatePolicyText;

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
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.update.ui.MainPreferencePage"); //$NON-NLS-1$

		Composite mainComposite = new Composite(parent, SWT.NULL);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label historySizeLabel = new Label(mainComposite, SWT.NONE);
		historySizeLabel.setText(UpdateUIMessages.MainPreferencePage_historySize); 
		historySizeText = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		historySizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		historySizeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				historySizeChanged();
			}
		});

		checkSignatureCheckbox =
			new Button(mainComposite, SWT.CHECK | SWT.LEFT);
		checkSignatureCheckbox.setText(UpdateUIMessages.MainPreferencePage_checkSignature); 
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
		
		automaticallyChooseMirrorCheckbox =
			new Button(mainComposite, SWT.CHECK | SWT.LEFT);
		automaticallyChooseMirrorCheckbox.setText(UpdateUIMessages.MainPreferencePage_automaticallyChooseMirror);
		gd = new GridData();
		gd.horizontalSpan = 2;
		automaticallyChooseMirrorCheckbox.setLayoutData(gd);

		createSpacer(mainComposite, 2);

		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(UpdateUIMessages.MainPreferencePage_updateVersions); 
		group.setLayout(new GridLayout());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		equivalentButton = new Button(group, SWT.RADIO);
		equivalentButton.setText(UpdateUIMessages.MainPreferencePage_updateVersions_equivalent); 

		compatibleButton = new Button(group, SWT.RADIO);
		compatibleButton.setText(UpdateUIMessages.MainPreferencePage_updateVersions_compatible); 

		createSpacer(mainComposite, 2);

		group = new Group(mainComposite, SWT.NONE);
		group.setText(UpdateUIMessages.MainPreferencePage_updatePolicy); 
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NULL);
		label.setText(UpdateUIMessages.MainPreferencePage_updatePolicyURL); 
		updatePolicyText = new Text(group, SWT.SINGLE | SWT.BORDER);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		updatePolicyText.setLayoutData(gd);

		//createSpacer(mainComposite, 2);
		//createHttpProxy(mainComposite, 2);
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
		group.setText(UpdateUIMessages.MainPreferencePage_proxyGroup); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = columnSpan;
		group.setLayoutData(gd);
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
	
	private void historySizeChanged() {
		
		try {
			int historySize = Integer.parseInt(historySizeText.getText());
			if (historySize < 0) {
				setValid(false);
				setErrorMessage(UpdateUIMessages.MainPreferencePage_invalidHistorySize); 
				return;
			}
		} catch (NumberFormatException e) {
			setValid(false);
			setErrorMessage(UpdateUIMessages.MainPreferencePage_invalidHistorySize); 
			return;
		}
		setValid(true);
		setErrorMessage(null);	
	}

	public boolean performOk() {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				try {
					SiteManager.getLocalSite().setMaximumHistoryCount(
						getHistoryCount());
				} catch (CoreException e) {
					UpdateUI.logException(e);
				}
			}
		});

		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();
		prefs.setValue(
			UpdateCore.P_CHECK_SIGNATURE,
			checkSignatureCheckbox.getSelection());
		prefs.setValue(UpdateCore.P_AUTOMATICALLY_CHOOSE_MIRROR, automaticallyChooseMirrorCheckbox.getSelection());
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

		checkSignatureCheckbox.setSelection(
			prefs.getBoolean(UpdateCore.P_CHECK_SIGNATURE));
		automaticallyChooseMirrorCheckbox.setSelection(
				prefs.getBoolean(UpdateCore.P_AUTOMATICALLY_CHOOSE_MIRROR));

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
				setErrorMessage(UpdateUIMessages.UpdateSettingsPreferencePage_invalid); 
				return;
			}
		}
		setValid(true);
		setErrorMessage(null);
	}

	public void performDefaults() {
		super.performDefaults();
		Preferences prefs = UpdateCore.getPlugin().getPluginPreferences();

		updatePolicyText.setText(""); //$NON-NLS-1$

		checkSignatureCheckbox.setSelection(true);
		automaticallyChooseMirrorCheckbox.setSelection(false);
		historySizeText.setText(
			prefs.getDefaultString(UpdateCore.P_HISTORY_SIZE));
		
		equivalentButton.setSelection(true);
		compatibleButton.setSelection(false);
	}

	private void warnSignatureCheck(Shell shell) {
		MessageDialog.openWarning(shell, UpdateUIMessages.MainPreferencePage_digitalSignature_title, 
		UpdateUIMessages.MainPreferencePage_digitalSignature_message); 
	}
}
