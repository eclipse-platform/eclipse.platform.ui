/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.console;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class ConsolePreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ConsolePreferencesPage() {
		super(GRID);
		setPreferenceStore(CVSUIPlugin.getPlugin().getPreferenceStore());
	}
	private ColorFieldEditor commandColorEditor;
	private ColorFieldEditor messageColorEditor;
	private ColorFieldEditor errorColorEditor;
	private BooleanFieldEditor showOnMessage;
	private BooleanFieldEditor restrictOutput;
	private BooleanFieldEditor wrap;
	private IntegerFieldEditor highWaterMark;
	private IntegerFieldEditor width;

	protected void createFieldEditors() {
		final Composite composite = getFieldEditorParent();
		createLabel(composite, Policy.bind("ConsolePreferencesPage.9")); //$NON-NLS-1$
		IPreferenceStore store = getPreferenceStore();
		
		// ** WRAP
		wrap = new BooleanFieldEditor(ICVSUIConstants.PREF_CONSOLE_WRAP, Policy.bind("ConsolePreferencesPage.6"), composite); //$NON-NLS-1$
		addField(wrap);
		
		width = new IntegerFieldEditor(ICVSUIConstants.PREF_CONSOLE_WIDTH, Policy.bind("ConsolePreferencesPage.7"), composite); //$NON-NLS-1$)
		width.setValidRange(80, Integer.MAX_VALUE - 1);
		addField(width);
		width.setEnabled(store.getBoolean(ICVSUIConstants.PREF_CONSOLE_WRAP), composite);
		
		// ** RESTRICT OUTPUT
		restrictOutput = new BooleanFieldEditor(ICVSUIConstants.PREF_CONSOLE_LIMIT_OUTPUT, Policy.bind("ConsolePreferencesPage.5"), composite); //$NON-NLS-1$
		addField(restrictOutput);
		
		highWaterMark = new IntegerFieldEditor(ICVSUIConstants.PREF_CONSOLE_HIGH_WATER_MARK, Policy.bind("ConsolePreferencesPage.8"), composite); //$NON-NLS-1$)
		highWaterMark.setValidRange(1000, Integer.MAX_VALUE - 1);
		addField(highWaterMark);
		highWaterMark.setEnabled(store.getBoolean(ICVSUIConstants.PREF_CONSOLE_LIMIT_OUTPUT), composite);
		
		// ** SHOW AUTOMATICALLY
		showOnMessage = new BooleanFieldEditor(ICVSUIConstants.PREF_CONSOLE_SHOW_ON_MESSAGE, Policy.bind("ConsolePreferencesPage.4"), composite); //$NON-NLS-1$
		addField(showOnMessage);
		
		createLabel(composite, Policy.bind("ConsolePreferencePage.consoleColorSettings")); //$NON-NLS-1$
		
		//	** COLORS AND FONTS
		commandColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR,
			Policy.bind("ConsolePreferencePage.commandColor"), composite); //$NON-NLS-1$
		addField(commandColorEditor);
		
		messageColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR,
			Policy.bind("ConsolePreferencePage.messageColor"), composite); //$NON-NLS-1$
		addField(messageColorEditor);
		
		errorColorEditor = createColorFieldEditor(ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR,
			Policy.bind("ConsolePreferencePage.errorColor"), composite); //$NON-NLS-1$
		addField(errorColorEditor);
		
		Dialog.applyDialogFont(composite);
		WorkbenchHelp.setHelp(composite, IHelpContextIds.CONSOLE_PREFERENCE_PAGE);
	}
	
	
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		highWaterMark.setEnabled(restrictOutput.getBooleanValue(), getFieldEditorParent());
		width.setEnabled(wrap.getBooleanValue(), getFieldEditorParent());
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
		data.horizontalSpan = 2;
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
		return editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		CVSUIPlugin.getPlugin().savePluginPreferences();
		return super.performOk();
	}
}
