/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

public class ConsolePreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private ColorFieldEditor commandColorEditor;
	private ColorFieldEditor messageColorEditor;
	private ColorFieldEditor errorColorEditor;
	private WorkbenchChainedTextFontFieldEditor fontEditor;
	private Button autoOpenCheckBox;

	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		//GridLayout
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginHeight = 0;
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent, 2);
		createLabel(composite, Policy.bind("ConsolePreferencePage.consoleColorSettings")); //$NON-NLS-1$

		commandColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR,
			Policy.bind("ConsolePreferencePage.commandColor"), composite); //$NON-NLS-1$
		messageColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR,
			Policy.bind("ConsolePreferencePage.messageColor"), composite); //$NON-NLS-1$
		errorColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR,
			Policy.bind("ConsolePreferencePage.errorColor"), composite); //$NON-NLS-1$

		fontEditor = new WorkbenchChainedTextFontFieldEditor(ICVSUIConstants.PREF_CONSOLE_FONT,
			Policy.bind("ConsolePreferencePage.font"), composite); //$NON-NLS-1$
		fontEditor.setPreferencePage(this);
		fontEditor.setPreferenceStore(getPreferenceStore());
		
		autoOpenCheckBox = createCheckBox(composite, Policy.bind("ConsolePreferencePage.autoOpen")); //$NON-NLS-1$
		
		initializeValues();
		WorkbenchHelp.setHelp(composite, IHelpContextIds.CONSOLE_PREFERENCE_PAGE);
		return composite;
	}
	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Creates a new color field editor.
	 */
	private ColorFieldEditor createColorFieldEditor(String preferenceName, String label, Composite parent) {
		ColorFieldEditor editor = new ColorFieldEditor(preferenceName, label, parent);
		editor.setPreferencePage(this);
		editor.setPreferenceStore(getPreferenceStore());
		new Label(parent, SWT.NONE); // fill in 3rd column
		return editor;
	}
	
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		commandColorEditor.load();
		messageColorEditor.load();
		errorColorEditor.load();
		fontEditor.load();
		autoOpenCheckBox.setSelection(store.getBoolean(ICVSUIConstants.PREF_CONSOLE_AUTO_OPEN));
	}

	/**
	* @see IWorkbenchPreferencePage#init(IWorkbench)
	*/
	public void init(IWorkbench workbench) {
	}

	/**
	 * OK was clicked. Store the CVS preferences.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		commandColorEditor.store();
		messageColorEditor.store();
		errorColorEditor.store();
		fontEditor.store();
		store.setValue(ICVSUIConstants.PREF_CONSOLE_AUTO_OPEN, autoOpenCheckBox.getSelection());
		return true;
	}

	/**
	 * Defaults was clicked. Restore the CVS preferences to
	 * their default values
	 */
	protected void performDefaults() {
		super.performDefaults();
		IPreferenceStore store = getPreferenceStore();
		commandColorEditor.loadDefault();
		messageColorEditor.loadDefault();
		errorColorEditor.loadDefault();
		fontEditor.loadDefault();
		autoOpenCheckBox.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_CONSOLE_AUTO_OPEN));
	}

	/**
	* Returns preference store that belongs to the our plugin.
	* This is important because we want to store
	* our preferences separately from the desktop.
	*
	* @return the preference store for this plugin
	*/
	protected IPreferenceStore doGetPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}
}
