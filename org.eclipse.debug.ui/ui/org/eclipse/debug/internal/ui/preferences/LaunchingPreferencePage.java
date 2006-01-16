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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * A preference page for configuring launching preferences.
 */
public class LaunchingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * a list of the field editors
	 * @since 3.2
	 */
	private List fFieldEditors;
	
	/**
	 * The default contsructor
	 */
	public LaunchingPreferencePage() {
		super();
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCHING_PREFERENCE_PAGE);
	}

	/**
	 * creates a composite to place tab controls on
	 * @param parent the parent to create to composite for
	 * @return a composite for settgin as a tabitem control
	 * @since 3.2
	 */
	private Composite createComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fFieldEditors = new ArrayList();
		Composite comp = createComposite(parent);
		//save dirty editors
		FieldEditor edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_2, 3,  
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_3, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_4, MessageDialogWithToggle.NEVER},
				 {DebugPreferencesMessages.LaunchingPreferencePage_5, MessageDialogWithToggle.PROMPT}}, 
				 comp,
				 true);	
		fFieldEditors.add(edit);
		
		//wait for build
		edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, 
				 DebugPreferencesMessages.LaunchingPreferencePage_6, 3,
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_7, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_8, MessageDialogWithToggle.NEVER}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_9, MessageDialogWithToggle.PROMPT}}, 
				 comp,
				 true);
		fFieldEditors.add(edit);
		
		//relaunch in debug mode
		edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE,
				 DebugPreferencesMessages.LaunchingPreferencePage_15, 3, 
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_16, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_17, MessageDialogWithToggle.NEVER}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_18, MessageDialogWithToggle.PROMPT}}, 
				 comp,
				 true);
		fFieldEditors.add(edit);
		
		//continue with compile errors
		edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR,
				 DebugPreferencesMessages.LaunchingPreferencePage_21, 2, 
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_22, MessageDialogWithToggle.ALWAYS},  
				 {DebugPreferencesMessages.LaunchingPreferencePage_23, MessageDialogWithToggle.PROMPT}},  
				 comp,
				 true);
		fFieldEditors.add(edit);
		
		//filtering options
		Group group = createGroupComposite(comp, DebugPreferencesMessages.LaunchingPreferencePage_36);
		Composite spacer = createComposite(group);
		edit = new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_1, SWT.NONE, spacer);
		edit.fillIntoGrid(spacer, 2);
		fFieldEditors.add(edit);
		edit = new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.LaunchingPreferencePage_10, SWT.NONE, spacer);
		edit.fillIntoGrid(spacer, 2);
		fFieldEditors.add(edit);
		
		//history list size pref
		final IntegerFieldEditor editor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.DebugPreferencePage_10, spacer);
		editor.fillIntoGrid(spacer, 2);
		fFieldEditors.add(editor);
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
		
		//init the field editors
		initFieldEditors();
		return comp;
	}

	/**
	 * Creates a standard grouping for this pref page
	 * @param parent the parent to add the group to
	 * @param title text the test for the group
	 * @return the new group
	 * @since 3.2
	 */
	private Group createGroupComposite(Composite parent, String text) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(text);
		group.setFont(parent.getFont());
		return group;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}
	
	/**
	 * Initializes the field editors to their values
	 * @since 3.2
	 */
	private void initFieldEditors() {
		FieldEditor editor;
		for(int i = 0; i < fFieldEditors.size(); i++) {
			editor = (FieldEditor)fFieldEditors.get(i);
			editor.setPreferenceStore(getPreferenceStore());
			editor.load();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for(int i = 0; i < fFieldEditors.size(); i++) {
			((FieldEditor)fFieldEditors.get(i)).loadDefault();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		for(int i = 0; i < fFieldEditors.size(); i++) {
			((FieldEditor)fFieldEditors.get(i)).store();
		}
		return super.performOk();
	}
}
