/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

import java.text.MessageFormat;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
		setDescription(DebugPreferencesMessages.LaunchingPreferencePage_20); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_1, SWT.NONE, getFieldEditorParent())); 
		createSaveBeforeLaunchEditors();
		createWaitForBuildEditor();
		
		createSpacer(getFieldEditorParent(), 2);

		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.LaunchingPreferencePage_10, SWT.NONE, getFieldEditorParent())); 
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
		final IntegerFieldEditor editor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.DebugPreferencePage_10, getFieldEditorParent()); 
		int historyMax = IDebugPreferenceConstants.MAX_LAUNCH_HISTORY_SIZE;
		editor.setTextLimit(Integer.toString(historyMax).length());
		editor.setErrorMessage(MessageFormat.format(DebugPreferencesMessages.DebugPreferencePage_11, new Object[] { new Integer(1), new Integer(historyMax)})); 
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
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_2, 3,  
										new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_3, MessageDialogWithToggle.ALWAYS}, 
											{DebugPreferencesMessages.LaunchingPreferencePage_4, MessageDialogWithToggle.NEVER}, 
											{DebugPreferencesMessages.LaunchingPreferencePage_5, MessageDialogWithToggle.PROMPT}}, 
										getFieldEditorParent(),
										true));	
	}	
	
	private void createWaitForBuildEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, 
						DebugPreferencesMessages.LaunchingPreferencePage_6, 3,  
						new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_7, MessageDialogWithToggle.ALWAYS}, 
							{DebugPreferencesMessages.LaunchingPreferencePage_8, MessageDialogWithToggle.NEVER}, 
							{DebugPreferencesMessages.LaunchingPreferencePage_9, MessageDialogWithToggle.PROMPT}}, 
						getFieldEditorParent(),
						true));
	}
	
	private void createSwitchPerspectiveEditor() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE,
				DebugPreferencesMessages.LaunchingPreferencePage_11, 3, 
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_12, MessageDialogWithToggle.ALWAYS}, 
								{DebugPreferencesMessages.LaunchingPreferencePage_13, MessageDialogWithToggle.NEVER}, 
								{DebugPreferencesMessages.LaunchingPreferencePage_14, MessageDialogWithToggle.PROMPT}}, 
				getFieldEditorParent(),
				true));
	}
	
	private void createRelaunchInDebugMode() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE,
				DebugPreferencesMessages.LaunchingPreferencePage_15, 3, 
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_16, MessageDialogWithToggle.ALWAYS}, 
					{DebugPreferencesMessages.LaunchingPreferencePage_17, MessageDialogWithToggle.NEVER}, 
					{DebugPreferencesMessages.LaunchingPreferencePage_18, MessageDialogWithToggle.PROMPT}}, 
				getFieldEditorParent(),
				true));
	}	

	private void createContinueWithCompileErrors() {
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR,
				DebugPreferencesMessages.LaunchingPreferencePage_21, 2, 
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_22, MessageDialogWithToggle.ALWAYS},  
					{DebugPreferencesMessages.LaunchingPreferencePage_23, MessageDialogWithToggle.PROMPT}},  
				getFieldEditorParent(),
				true));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}
