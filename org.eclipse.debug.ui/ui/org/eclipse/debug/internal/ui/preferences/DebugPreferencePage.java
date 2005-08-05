/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.PlatformUI;

/**
 * The page for setting debugger preferences.  Built on the 'field editor' infrastructure.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {
	
	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugPreferencesMessages.DebugPreferencePage_1); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.DEBUG_PREFERENCE_PAGE);
	}
	
	/**
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_REUSE_EDITOR, DebugPreferencesMessages.DebugPreferencePage_2, SWT.NONE, getFieldEditorParent())); 
		
		createSpacer(getFieldEditorParent(), 2);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, DebugPreferencesMessages.DebugPreferencePage_3, SWT.NONE, getFieldEditorParent())); 
		addField(new BooleanFieldEditor(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, DebugPreferencesMessages.DebugPreferencePage_26, SWT.NONE, getFieldEditorParent())); 
		createSwitchPerspectiveOnSuspendEditor();

		createSpacer(getFieldEditorParent(), 2);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, DebugPreferencesMessages.DebugPreferencePage_25, SWT.NONE, getFieldEditorParent())); 
		
		createSpacer(getFieldEditorParent(), 2);
		ColorFieldEditor mem= new ColorFieldEditor(IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR, DebugPreferencesMessages.DebugPreferencePage_4, getFieldEditorParent()); 
		addField(mem);
		mem= new ColorFieldEditor(IDebugPreferenceConstants.MEMORY_VIEW_UNBUFFERED_LINE_COLOR, DebugPreferencesMessages.DebugPreferencePage_0, getFieldEditorParent()); 
		addField(mem);
		mem= new ColorFieldEditor(IDebugPreferenceConstants.MEMORY_VIEW_BUFFERED_LINE_COLOR, DebugPreferencesMessages.DebugPreferencePage_27, getFieldEditorParent()); 
		addField(mem);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	private void createSwitchPerspectiveOnSuspendEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND,
				DebugPreferencesMessages.DebugPreferencePage_21, 3, 
				new String[][] {{DebugPreferencesMessages.DebugPreferencePage_22, MessageDialogWithToggle.ALWAYS}, 
								{DebugPreferencesMessages.DebugPreferencePage_23, MessageDialogWithToggle.NEVER}, 
								{DebugPreferencesMessages.DebugPreferencePage_24, MessageDialogWithToggle.PROMPT}}, 
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
