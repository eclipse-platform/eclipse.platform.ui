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
package org.eclipse.debug.internal.ui.preferences;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A page to set the preferences for the variables
 */
public class VariableViewsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	/**
	 * Create the variables page.
	 */
	public VariableViewsPreferencePage() {
		super(GRID);
		setDescription(DebugPreferencesMessages.getString("VariabeViewsPreferencePage.Debug_Variable_Views_Settings_1"));  //$NON-NLS-1$
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			getControl(),
			IDebugHelpContextIds.VARIABLE_VIEWS_PREFERENCE_PAGE);
	}
	
	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {
		
		addField(new ColorFieldEditor(IDebugPreferenceConstants.CHANGED_VARIABLE_RGB, DebugPreferencesMessages.getString("VariableViewsPreferencePage.&Changed_variable_value_color__3"), getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 1);
		
		addField(new RadioGroupFieldEditor(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION,
					DebugPreferencesMessages.getString("VariableViewsPreferencePage.Orientation_of_detail_pane_in_variables_view_1"), //$NON-NLS-1$
					1,
					new String[][] {
						{DebugPreferencesMessages.getString("VariableViewsPreferencePage.To_the_right_of_variables_tree_pane_2"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT}, //$NON-NLS-1$
						{DebugPreferencesMessages.getString("VariableViewsPreferencePage.Underneath_the_variables_tree_pane_3"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH} //$NON-NLS-1$
					},
					getFieldEditorParent(), true));
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		DebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}
}
