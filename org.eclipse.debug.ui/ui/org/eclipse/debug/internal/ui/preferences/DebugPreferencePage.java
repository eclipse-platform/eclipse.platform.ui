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
package org.eclipse.debug.internal.ui.preferences;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The page for setting debugger preferences.  Built on the 'field editor' infrastructure.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {
	
	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugPreferencesMessages.getString("DebugPreferencePage.1")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.DEBUG_PREFERENCE_PAGE);
	}
	
	/**
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_REUSE_EDITOR, DebugPreferencesMessages.getString("DebugPreferencePage.2"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 2);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, DebugPreferencesMessages.getString("DebugPreferencePage.3"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, DebugPreferencesMessages.getString("DebugPreferencePage.26"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		createSwitchPerspectiveOnSuspendEditor();

		createSpacer(getFieldEditorParent(), 2);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, DebugPreferencesMessages.getString("DebugPreferencePage.25"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 2);
		ColorFieldEditor mem= new ColorFieldEditor(IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR, DebugPreferencesMessages.getString("DebugPreferencePage.4"), getFieldEditorParent()); //$NON-NLS-1$
		addField(mem);
		mem= new ColorFieldEditor(IDebugPreferenceConstants.MEMORY_VIEW_UNBUFFERED_LINE_COLOR, DebugPreferencesMessages.getString("DebugPreferencePage.0"), getFieldEditorParent()); //$NON-NLS-1$
		addField(mem);
		mem= new ColorFieldEditor(IDebugPreferenceConstants.MEMORY_VIEW_BUFFERED_LINE_COLOR, DebugPreferencesMessages.getString("DebugPreferencePage.27"), getFieldEditorParent()); //$NON-NLS-1$
		addField(mem);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	private void createSwitchPerspectiveOnSuspendEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND,
				DebugPreferencesMessages.getString("DebugPreferencePage.21"), 3, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.22"), MessageDialogWithToggle.ALWAYS}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.23"), MessageDialogWithToggle.NEVER}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.24"), MessageDialogWithToggle.PROMPT}}, //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}
		
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		DebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}			
}