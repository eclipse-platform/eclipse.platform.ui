package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A page to set the preferences for the variables
 */
public class VariableViewsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {
	/**
	 * Create the variables page.
	 */
	public VariableViewsPreferencePage() {
		super(GRID);
		setDescription(DebugUIMessages.getString("VariabeViewsPreferencePage.Debug_Variable_Views_Settings_1"));  //$NON-NLS-1$
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			IDebugHelpContextIds.VARIABLE_VIEWS_PREFERENCE_PAGE);
	}
	
	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {
		
		addField(new ColorFieldEditor(CHANGED_VARIABLE_RGB, DebugUIMessages.getString("VariableViewsPreferencePage.&Changed_variable_value_color__3"), getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 1);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SHOW_DETAIL_PANE, DebugUIMessages.getString("VariableViewPreferencePage.&Show_detail_pane_by_default_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SHOW_TYPE_NAMES, DebugUIMessages.getString("VariableViewPreferencePage.Show_type_&names_by_default_2"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 1);
		
		addField(new RadioGroupFieldEditor(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION,
					DebugUIMessages.getString("VariableViewsPreferencePage.Orientation_of_detail_pane_in_variables_view_1"), //$NON-NLS-1$
					1,
					new String[][] {
						{DebugUIMessages.getString("VariableViewsPreferencePage.To_the_right_of_variables_tree_pane_2"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT}, //$NON-NLS-1$
						{DebugUIMessages.getString("VariableViewsPreferencePage.Underneath_the_variables_tree_pane_3"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH} //$NON-NLS-1$
					},
					getFieldEditorParent(), true));
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		store.setDefault(IDebugUIConstants.PREF_SHOW_DETAIL_PANE, false);
		store.setDefault(IDebugUIConstants.PREF_SHOW_TYPE_NAMES, false);
		
		PreferenceConverter.setDefault(store, CHANGED_VARIABLE_RGB, new RGB(255, 0, 0));
	}
	
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
}