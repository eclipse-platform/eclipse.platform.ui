/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/*
 * The preference page implementation of Help -> Content.
 */
public class HelpContentPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	private Button checkbox;
	private Group group;
	private Label hostLabel;
	private Text hostText;
	private Label pathLabel;
	private Text pathText;
	private Button radio1;
	private Button radio2;
	private Text portText;
	
	/*
	 * Listens for any change in the UI and checks for valid
	 * input and correct enablement.
	 */
	private Listener changeListener = new Listener() {
		public void handleEvent(Event event) {
			updateEnablement();
			updateValidity();
		}
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP_CONTENT);

		Composite composite = createComposite(parent);
		createCheckbox(composite);
		createGroup(composite);
		
        applyDialogFont(composite);

        // initialize the UI
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		boolean isOn = prefs.getBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON);
		String host = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST);
		String path = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH);
		boolean useDefaultPort = prefs.getBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT);
		int port = prefs.getInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT);
		setValues(isOn, host, path, useDefaultPort, port);
        
        return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// nothing to do here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		boolean isOn = prefs.getDefaultBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON);
		String host = prefs.getDefaultString(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST);
		String path = prefs.getDefaultString(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH);
		boolean useDefaultPort = prefs.getDefaultBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT);
		int port = prefs.getDefaultInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT);
		setValues(isOn, host, path, useDefaultPort, port);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, checkbox.getSelection());
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST, hostText.getText().trim());
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH, pathText.getText().trim());
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT, radio1.getSelection());
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT, portText.getText().trim());
		
		HelpBasePlugin.getDefault().savePluginPreferences();
		RemoteHelp.notifyPreferenceChange();
		return true;
	}
	
	/*
	 * Create the Composite that will hold the entire preference page.
	 */
	private Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        return composite;
	}
	
	/*
	 * Create the top checkbox.
	 */
	private void createCheckbox(Composite parent) {
        checkbox = new Button(parent, SWT.CHECK);
        checkbox.setText(Messages.HelpContentPreferencePage_remote);
        checkbox.addListener(SWT.Selection, changeListener);
	}
	
	/*
	 * Create the "Location" group.
	 */
	private void createGroup(Composite parent) {
        group = new Group(parent, SWT.NONE);
        group.setText(Messages.HelpContentPreferencePage_location);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        createHostSection(group);
        createPathSection(group);
        createPortSection(group);
	}
	
	/*
	 * Create the "Host:" label and text field.
	 */
	private void createHostSection(Composite parent) {
        hostLabel = new Label(parent, SWT.NONE);        
        hostLabel.setText(Messages.HelpContentPreferencePage_host);
        hostText = new Text(parent, SWT.BORDER);
        hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (hostText.getOrientation() == SWT.RIGHT_TO_LEFT)
        	hostText.setOrientation(SWT.LEFT_TO_RIGHT);
        hostText.addListener(SWT.Modify, changeListener);
	}

	/*
	 * Create the "Path:" label and text field.
	 */
	private void createPathSection(Composite parent) {
        pathLabel = new Label(parent, SWT.NONE);        
        pathLabel.setText(Messages.HelpContentPreferencePage_path);
        pathText = new Text(parent, SWT.BORDER);
        pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (pathText.getOrientation() == SWT.RIGHT_TO_LEFT)
        	pathText.setOrientation(SWT.LEFT_TO_RIGHT);
        pathText.addListener(SWT.Modify, changeListener);
	}

	/*
	 * Create the port radio buttons, and text field.
	 */
	private void createPortSection(Composite parent) {
        Composite portComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        portComposite.setLayout(layout);
        portComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        radio1 = new Button(portComposite, SWT.RADIO);
        radio1.setText(Messages.HelpContentPreferencePage_portDefault);
        radio1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        radio1.addListener(SWT.Selection, changeListener);

        radio2 = new Button(portComposite, SWT.RADIO);
        radio2.setText(Messages.HelpContentPreferencePage_port);
        radio2.addListener(SWT.Selection, changeListener);
        
        portText = new Text(portComposite, SWT.BORDER);
        portText.setLayoutData(new GridData(50, SWT.DEFAULT));
        portText.addListener(SWT.Modify, changeListener);
	}
	
	/*
	 * Sets the given values for the UI.
	 */
	private void setValues(boolean isOn, String host, String path, boolean useDefaultPort, int port) {
		checkbox.setSelection(isOn);
		hostText.setText(host);
		pathText.setText(path);
		radio1.setSelection(useDefaultPort);
		radio2.setSelection(!useDefaultPort);
		portText.setText(String.valueOf(port));

		updateEnablement();
		updateValidity();
	}

	/*
	 * Ensures that the correct controls are grayed out and disabled, and the
	 * rest are enabled.
	 */
	private void updateEnablement() {
		boolean isChecked = checkbox.getSelection();
		group.setEnabled(isChecked);
		hostLabel.setEnabled(isChecked);
		hostText.setEnabled(isChecked);
		pathLabel.setEnabled(isChecked);
		pathText.setEnabled(isChecked);
		radio1.setEnabled(isChecked);
		radio2.setEnabled(isChecked);
		portText.setEnabled(isChecked && radio2.getSelection());
	}
	
	/*
	 * Checks for errors in the user input and shows/clears the error message
	 * as appropriate.
	 */
	private void updateValidity() {
		// no checking needed if remote not selected
		if (checkbox.getSelection() == true) {
			// check for empty hostname
			if (hostText.getText().trim().length() == 0) {
				setErrorMessage(Messages.HelpContentPreferencePage_error_host);
				setValid(false);
				return;
			}
			// check for invalid port
			if (radio2.getSelection() == true) {
				try {
					// check port range
					int port = Integer.parseInt(portText.getText());
					if (port < 0 || port > 65535) {
						setErrorMessage(Messages.HelpContentPreferencePage_error_port);
						setValid(false);
						return;
					}
				}
				catch (NumberFormatException e) {
					// not a number
					setErrorMessage(Messages.HelpContentPreferencePage_error_port);
					setValid(false);
					return;
				}
			}
		}
		// no errors
		setErrorMessage(null);
		setValid(true);
	}
}
