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
package org.eclipse.ui.internal.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IPreferenceConstants;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * The IDE workbench main preference page.
 */
public class IDEWorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button refreshButton;
	private Button showTasks;
	private Button exitPromptButton;
	private IntegerFieldEditor saveInterval;

	// State for encoding group
	private String defaultEnc;
	private Button defaultEncodingButton;
	private Button otherEncodingButton;
	private Combo encodingCombo;

	/**
	 *	Create this page's visual contents
	 *
	 *	@return org.eclipse.swt.widgets.Control
	 *	@param parent org.eclipse.swt.widgets.Composite
	 */
	protected Control createContents(Composite parent) {
		
		Font font = parent.getFont();

		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_PREFERENCE_PAGE);

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(font);

		autoBuildButton = new Button(composite, SWT.CHECK);
		autoBuildButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.autobuild")); //$NON-NLS-1$
		autoBuildButton.setFont(font);

		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.savePriorToBuilding")); //$NON-NLS-1$
		autoSaveAllButton.setFont(font);

		refreshButton = new Button(composite, SWT.CHECK);
		refreshButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.refreshButton")); //$NON-NLS-1$
		refreshButton.setFont(font);
		
		exitPromptButton = new Button(composite, SWT.CHECK);
		exitPromptButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.exitPromptButton")); //$NON-NLS-1$
		exitPromptButton.setFont(font);
		
		showTasks = new Button(composite, SWT.CHECK);
		showTasks.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.showTasks")); //$NON-NLS-1$
		showTasks.setFont(font);

		createSpace(composite);
		createSaveIntervalGroup(composite);
		
		createSpace(composite);
		createEncodingGroup(composite);

		// set initial values
		IPreferenceStore store = getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		refreshButton.setSelection(store.getBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));
		exitPromptButton.setSelection(store.getBoolean(IPreferenceConstants.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));
		showTasks.setSelection(store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD));
		
		return composite;
	}

	/**
	 * Create a composite that contains entry fields specifying save interval preference.
	 */
	private void createSaveIntervalGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);	
		groupComposite.setFont(composite.getFont());
		
		saveInterval = new IntegerFieldEditor(IPreferenceConstants.SAVE_INTERVAL, IDEWorkbenchMessages.getString("WorkbenchPreference.saveInterval"), groupComposite); //$NON-NLS-1$

		// @issue we should drop our preference constant and let clients use core's pref. ours is not up-to-date anyway if someone changes this interval directly thru core api.
		saveInterval.setPreferenceStore(getPreferenceStore());
		saveInterval.setPreferencePage(this);
		saveInterval.setTextLimit(Integer.toString(IPreferenceConstants.MAX_SAVE_INTERVAL).length());
		saveInterval.setErrorMessage(IDEWorkbenchMessages.format("WorkbenchPreference.saveIntervalError", new Object[] { new Integer(IPreferenceConstants.MAX_SAVE_INTERVAL)})); //$NON-NLS-1$
		saveInterval.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		saveInterval.setValidRange(1, IPreferenceConstants.MAX_SAVE_INTERVAL);

		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		long interval = description.getSnapshotInterval() / 60000;
		saveInterval.setStringValue(Long.toString(interval));

		saveInterval.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(saveInterval.isValid());
			}
		});
		
	}	

	private void createEncodingGroup(Composite parent) {
		
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		group.setFont(font);
		
		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};
		
		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = System.getProperty("file.encoding", "UTF-8");  //$NON-NLS-1$  //$NON-NLS-2$
		defaultEncodingButton.setText(IDEWorkbenchMessages.format("WorkbenchPreference.defaultEncoding", new String[] { defaultEnc })); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);
		
		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
		otherEncodingButton.addSelectionListener(buttonListener);
		otherEncodingButton.setFont(font);
		
		encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(15);
		encodingCombo.setFont(font);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValidState();
			}
		});

		ArrayList encodings = new ArrayList();
		int n = 0;
		try {
			n = Integer.parseInt(IDEWorkbenchMessages.getString("WorkbenchPreference.numDefaultEncodings")); //$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			// Ignore;
		}
		for (int i = 0; i < n; ++i) {
			String enc = IDEWorkbenchMessages.getString("WorkbenchPreference.defaultEncoding" + (i+1), null); //$NON-NLS-1$
			if (enc != null) {
				encodings.add(enc);
			}
		}
		
		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		String enc = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
		boolean isDefault = enc == null || enc.length() == 0;

		if (!isDefault && !encodings.contains(enc)) {
			encodings.add(enc);
		}
		Collections.sort(encodings);
		for (int i = 0; i < encodings.size(); ++i) {
			encodingCombo.add((String) encodings.get(i));
		}

		encodingCombo.setText(isDefault ? defaultEnc : enc);
		
		updateEncodingState(isDefault);
	}

	private void updateValidState() {
		if (!isEncodingValid()) {
			setErrorMessage(IDEWorkbenchMessages.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	private boolean isEncodingValid() {
		return defaultEncodingButton.getSelection() ||
			isValidEncoding(encodingCombo.getText());
	}
	
	private boolean isValidEncoding(String enc) {
		try {
			new String(new byte[0], enc);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	private void updateEncodingState(boolean useDefault) {
		defaultEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		encodingCombo.setEnabled(!useDefault);
		updateValidState();
	}		

	/**
	 * Utility method that creates a radio button instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new button
	 * @return the newly-created button
	 */
	protected static Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		button.setFont(parent.getFont());
		return button;
	}

	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected static Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		combo.setFont(parent.getFont());
		return combo;
	}

	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected static Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		label.setFont(parent.getFont());
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a tab of one horizontal spans.
	 *
	 * @param parent  the parent in which the tab should be created
	 */
	protected static void createSpace(Composite parent) {
		Label vfiller = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		vfiller.setLayoutData(gridData);
	}

	/**
	 * Returns preference store that belongs to the our plugin.
	 *
	 * @return the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		// do nothing
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		updateEncodingState(true);

		// core holds onto this preference.
		boolean autoBuild = ResourcesPlugin.getPlugin().getPluginPreferences().getDefaultBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
		autoBuildButton.setSelection(autoBuild);
		
		IPreferenceStore store = getPreferenceStore();
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		refreshButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));
		exitPromptButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));
		showTasks.setSelection(store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD));
		saveInterval.loadDefault();
		
		super.performDefaults();
	}

	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		// set the workspace auto-build flag
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		if (autoBuildButton.getSelection() != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			try {
				description.setAutoBuilding(autoBuildButton.getSelection());
				ResourcesPlugin.getWorkspace().setDescription(description);
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Error changing auto build workspace setting.", e.getStatus()); //$NON-NLS-1$
			}
		}

		// set the workspace text file encoding
		Preferences resourcePrefs = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (defaultEncodingButton.getSelection()) {
			resourcePrefs.setToDefault(ResourcesPlugin.PREF_ENCODING);
		}
		else {
			String enc = encodingCombo.getText();
			resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, enc);
		}
		ResourcesPlugin.getPlugin().savePluginPreferences();

		IPreferenceStore store = getPreferenceStore();

		// store the save all prior to build setting
		store.setValue(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, autoSaveAllButton.getSelection());

		// store the refresh workspace on startup setting
		store.setValue(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP, refreshButton.getSelection());

		// store the exit prompt on last window close setting
		store.setValue(IPreferenceConstants.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, exitPromptButton.getSelection());

		// store the preference for bringing task view to front on build
		store.setValue(IPreferenceConstants.SHOW_TASKS_ON_BUILD, showTasks.getSelection());

		// store the workspace save interval
		// @issue we should drop our preference constant and let clients use core's pref. ours is not up-to-date anyway if someone changes this interval directly thru core api.
		long oldSaveInterval = description.getSnapshotInterval() / 60000;
		long newSaveInterval = new Long(saveInterval.getStringValue()).longValue();
		if (oldSaveInterval != newSaveInterval) {
			try {
				description.setSnapshotInterval(newSaveInterval * 60000);
				ResourcesPlugin.getWorkspace().setDescription(description);
				store.firePropertyChangeEvent(IPreferenceConstants.SAVE_INTERVAL, new Integer((int)oldSaveInterval), new Integer((int)newSaveInterval));
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Error changing save interval preference", e.getStatus()); //$NON-NLS-1$
			}
		}

		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}
}
