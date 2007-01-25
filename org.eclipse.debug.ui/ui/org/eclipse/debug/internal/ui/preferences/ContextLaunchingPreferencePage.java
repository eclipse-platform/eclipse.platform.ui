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
package org.eclipse.debug.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * A preference page for configuring launching preferences.
 * 
 * @since 3.3
 */
public class ContextLaunchingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * a list of the field editors
	 */
	private List fFieldEditors;
	
	/**
	 * The default constructor
	 */
	public ContextLaunchingPreferencePage() {
		super();
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.CONTEXTUAL_LAUNCHING_PREFERENCE_PAGE);
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fFieldEditors = new ArrayList();
		Composite comp = SWTUtil.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		
		SWTUtil.createWrapLabel(comp, DebugPreferencesMessages.ContextualLaunchPreferencePage_0, 1, 300);
		SWTUtil.createVerticalSpacer(comp, 2);
	//use contextual launch
		FieldEditor edit = new BooleanFieldEditor(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH, DebugPreferencesMessages.ContextualLaunchPreferencePage_1, comp);	
		fFieldEditors.add(edit);
			
		edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_ALWAYS_RUN_LAST_LAUNCH, DebugPreferencesMessages.ContextLaunchingPreferencePage_0, 3, 
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_3, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_5, MessageDialogWithToggle.PROMPT}},
				 comp, true);	
		fFieldEditors.add(edit);
		edit = new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_ALWAYS_RUN_PROJECT_CONFIGURATION, DebugPreferencesMessages.ContextLaunchingPreferencePage_1, 3, 
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_7, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_9, MessageDialogWithToggle.PROMPT}},
				 comp, true);	
		fFieldEditors.add(edit);
	//init the field editors
		FieldEditor editor;
		for(int i = 0; i < fFieldEditors.size(); i++) {
			editor = (FieldEditor)fFieldEditors.get(i);
			editor.setPreferenceStore(getPreferenceStore());
			editor.load();
		}
		return comp;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for(int i = 0; i < fFieldEditors.size(); i++) {
			((FieldEditor)fFieldEditors.get(i)).loadDefault();
		}
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		for(int i = 0; i < fFieldEditors.size(); i++) {
			((FieldEditor)fFieldEditors.get(i)).store();
		}
		return super.performOk();
	}
}
