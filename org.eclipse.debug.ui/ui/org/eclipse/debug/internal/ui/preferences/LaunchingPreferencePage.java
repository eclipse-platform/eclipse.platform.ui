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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * A preference page for configuring launching preferences.
 */
public class LaunchingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private ProgressMonitorPart fMonitor;
	
	/**
	 * The default contsructor
	 */
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
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_1, SWT.NONE, parent)); 
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, DebugPreferencesMessages.LaunchingPreferencePage_2, 3,  
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_3, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_4, MessageDialogWithToggle.NEVER},
				 {DebugPreferencesMessages.LaunchingPreferencePage_5, MessageDialogWithToggle.PROMPT}}, 
				 parent,
				 true));	
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, 
				 DebugPreferencesMessages.LaunchingPreferencePage_6, 3,
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_7, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_8, MessageDialogWithToggle.NEVER}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_9, MessageDialogWithToggle.PROMPT}}, 
				 parent,
				 true));
		createSpacer(parent, 2);
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.LaunchingPreferencePage_10, SWT.NONE, parent)); 
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE,
				 DebugPreferencesMessages.LaunchingPreferencePage_15, 3, 
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_16, MessageDialogWithToggle.ALWAYS}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_17, MessageDialogWithToggle.NEVER}, 
				 {DebugPreferencesMessages.LaunchingPreferencePage_18, MessageDialogWithToggle.PROMPT}}, 
				 parent,
				 true));
		addField(new RadioGroupFieldEditor(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR,
				 DebugPreferencesMessages.LaunchingPreferencePage_21, 2, 
				 new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_22, MessageDialogWithToggle.ALWAYS},  
				 {DebugPreferencesMessages.LaunchingPreferencePage_23, MessageDialogWithToggle.PROMPT}},  
				 parent,
				 true));
		createLaunchHistoryEditor(parent);
		createSpacer(parent, 1);
		createMigrationEditor(parent);
		createSpacer(parent, 2);
	}

	/**
	 * handles the Migrate button being clicked
	 *
	 * @since 3.2
	 */
	private void handleMigrateNowSelected() {
		try {
			ILaunchManager lmanager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfiguration[] configurations = lmanager.getMigrationCandidates();
			if(configurations.length == 0) {
				MessageDialog.openInformation(getShell(), DebugPreferencesMessages.LaunchingPreferencePage_29, DebugPreferencesMessages.LaunchingPreferencePage_30);
				return;
			}
			ListSelectionDialog listd = new ListSelectionDialog(getShell(), new AdaptableList(configurations), 
											new WorkbenchContentProvider(),	DebugUITools.newDebugModelPresentation(), 
											DebugPreferencesMessages.LaunchingPreferencePage_0);
			listd.setTitle(DebugPreferencesMessages.LaunchingPreferencePage_28);
			listd.setInitialSelections(configurations);
			if(listd.open() == IDialogConstants.OK_ID) {
				Object[] objs = listd.getResult();
				fMonitor.beginTask(DebugPreferencesMessages.LaunchingPreferencePage_31, objs.length);
				for(int i = 0; i < objs.length; i++) {
					if(objs[i] instanceof ILaunchConfiguration) {
						((ILaunchConfiguration)objs[i]).migrate();
					}
					fMonitor.worked(i);
				}
				fMonitor.done();
			}
		}
		catch (CoreException e) {DebugUIPlugin.log(e);}
	}
	
	
	/**
	 * Creates a horozontal spacer in a composite which is as wide as the specified column span 
	 * @param composite the parent to add the spacer to
	 * @param columnSpan the number of columns to add the spacer to.
	 */
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/**
	 * Create the section that handles migration
	 * 
	 * @since 3.2
	 */
	private void createMigrationEditor(Composite parent) {
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		GridData gd = new GridData();
		gd.widthHint = 450;
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setText(DebugPreferencesMessages.LaunchingPreferencePage_26);
		Button msabod = SWTUtil.createPushButton(parent, DebugPreferencesMessages.LaunchingPreferencePage_27, null);
		msabod.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleMigrateNowSelected();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}			
		});
		new Label(parent, SWT.NONE);
		fMonitor = new ProgressMonitorPart(parent, new GridLayout());
	}
	
	
	/**
	 * Creates the launch history section of the page
	 */
	private void createLaunchHistoryEditor(Composite parent) {
		final IntegerFieldEditor editor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.DebugPreferencePage_10, parent); 
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}
}
