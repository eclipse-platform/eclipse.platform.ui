/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page for configuring launching preferences.
 */
public class LaunchingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public LaunchingPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugPreferencesMessages.getString("LaunchingPreferencePage.20")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugPreferencesMessages.getString("LaunchingPreferencePage.1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		createSaveBeforeLaunchEditors();
		createWaitForBuildEditor();
		
		createSpacer(getFieldEditorParent(), 2);

		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.getString("LaunchingPreferencePage.10"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		createSwitchPerspectiveEditor();
		createRelaunchInDebugMode();
		createContinueWithCompileErrors();
		
		createLaunchHistoryEditor();
	}
	

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	private void createLaunchHistoryEditor() {
		final IntegerFieldEditor editor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.getString("DebugPreferencePage.10"), getFieldEditorParent()); //$NON-NLS-1$
		int historyMax = IDebugPreferenceConstants.MAX_LAUNCH_HISTORY_SIZE;
		editor.setTextLimit(Integer.toString(historyMax).length());
		editor.setErrorMessage(MessageFormat.format(DebugPreferencesMessages.getString("DebugPreferencePage.11"), new Object[] { new Integer(1), new Integer(historyMax)})); //$NON-NLS-1$
		editor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		editor.setValidRange(1, historyMax);		
		editor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(editor.isValid());
			}
		});
		addField(editor);
	}
	
	private void createSaveBeforeLaunchEditors() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, DebugPreferencesMessages.getString("LaunchingPreferencePage.2"), 3,  //$NON-NLS-1$
										new String[][] {{DebugPreferencesMessages.getString("LaunchingPreferencePage.3"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
											{DebugPreferencesMessages.getString("LaunchingPreferencePage.4"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
											{DebugPreferencesMessages.getString("LaunchingPreferencePage.5"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
										getFieldEditorParent(),
										true));	
	}	
	
	private void createWaitForBuildEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, 
						DebugPreferencesMessages.getString("LaunchingPreferencePage.6"), 3,  //$NON-NLS-1$
						new String[][] {{DebugPreferencesMessages.getString("LaunchingPreferencePage.7"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
							{DebugPreferencesMessages.getString("LaunchingPreferencePage.8"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
							{DebugPreferencesMessages.getString("LaunchingPreferencePage.9"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
						getFieldEditorParent(),
						true));
	}
	
	private void createSwitchPerspectiveEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE,
				DebugPreferencesMessages.getString("LaunchingPreferencePage.11"), 3, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("LaunchingPreferencePage.12"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("LaunchingPreferencePage.13"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("LaunchingPreferencePage.14"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}
	
	private void createRelaunchInDebugMode() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE,
				DebugPreferencesMessages.getString("LaunchingPreferencePage.15"), 3, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("LaunchingPreferencePage.16"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
					{DebugPreferencesMessages.getString("LaunchingPreferencePage.17"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
					{DebugPreferencesMessages.getString("LaunchingPreferencePage.18"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}	

	private void createContinueWithCompileErrors() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR,
				DebugPreferencesMessages.getString("LaunchingPreferencePage.21"), 2, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("LaunchingPreferencePage.22"), AlwaysNeverDialog.ALWAYS},  //$NON-NLS-1$
					{DebugPreferencesMessages.getString("LaunchingPreferencePage.23"), AlwaysNeverDialog.PROMPT}},  //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}
