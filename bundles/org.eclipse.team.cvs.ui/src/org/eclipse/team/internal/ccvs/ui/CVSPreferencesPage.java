/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * CVS Preference Page
 * 
 * Allows the configuration of CVS specific options.
 * The currently supported options are:
 *  - Allow loading of CVS administration directory (CVSROOT)
 * 
 * There are currently a couple of deficiencies:
 *  1. The Repository view is not refreshed when the show CVSROOT option is changed
 *  2. There is no help associated with the page
 */
public class CVSPreferencesPage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button pruneEmptyDirectoriesField;
	private Text timeoutValue;
	private Combo quietnessCombo;
	private Combo compressionLevelCombo;
	private Button historyTracksSelectionButton;
	private Button considerContentsInCompare;
	private Button promptOnFileDelete;
	private Button promptOnFolderDelete;
	private Button showMarkers;
	private Button replaceUnmanaged;
	
	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
	}
	
	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent, 2);

		// set F1 help
//		WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, ICVSHelpContextIds.CVS_PREFERENCE_PAGE));

		pruneEmptyDirectoriesField = createCheckBox(composite, Policy.bind("CVSPreferencePage.pruneEmptyDirectories")); //$NON-NLS-1$

		createLabel(composite, Policy.bind("CVSPreferencePage.timeoutValue")); //$NON-NLS-1$
		timeoutValue = createTextField(composite);
		timeoutValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Parse the timeout value
				try {
					Integer.parseInt(timeoutValue.getText());
					setValid(true);
				} catch (NumberFormatException ex) {
					setValid(false);
				}
			}
		});
		
		createLabel(composite, Policy.bind("CVSPreferencePage.quietness")); //$NON-NLS-1$
		quietnessCombo = createCombo(composite);

		createLabel(composite, Policy.bind("CVSPreferencePage.compressionLevel")); //$NON-NLS-1$
		compressionLevelCombo = createCombo(composite);
		
		historyTracksSelectionButton = createCheckBox(composite, Policy.bind("CVSPreferencePage.historyTracksSelection")); //$NON-NLS-1$
		
		considerContentsInCompare = createCheckBox(composite, Policy.bind("CVSPreferencePage.considerContentsInCompare")); //$NON-NLS-1$
		considerContentsInCompare.setToolTipText(Policy.bind("CVSPreferencePage.considerContentsInCompareTooltip")); //$NON-NLS-1$
		
		promptOnFileDelete = createCheckBox(composite, Policy.bind("CVSPreferencePage.promptOnFileDelete")); //$NON-NLS-1$
		promptOnFileDelete.setToolTipText(Policy.bind("CVSPreferencePage.promptOnFileDeleteTooltip")); //$NON-NLS-1$

		promptOnFolderDelete = createCheckBox(composite, Policy.bind("CVSPreferencePage.promptOnFolderDelete")); //$NON-NLS-1$
		promptOnFolderDelete.setToolTipText(Policy.bind("CVSPreferencePage.promptOnFolderDeleteTooltip")); //$NON-NLS-1$

		showMarkers = createCheckBox(composite, Policy.bind("CVSPreferencePage.showAddRemoveMarkers")); //$NON-NLS-1$
		showMarkers.setToolTipText(Policy.bind("CVSPreferencePage.showAddRemoveMarkersTooltip")); //$NON-NLS-1$
		
		replaceUnmanaged = createCheckBox(composite, Policy.bind("CVSPreferencePage.replaceUnmanaged")); //$NON-NLS-1$
		replaceUnmanaged.setToolTipText(Policy.bind("CVSPreferencePage.replaceUnmanagedTooltip")); //$NON-NLS-1$
		
		initializeValues();
		
		quietnessCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (getQuietnessOptionFor(quietnessCombo.getSelectionIndex()).equals(Command.SILENT)) {
					MessageDialog.openWarning(getShell(), Policy.bind("CVSPreferencePage.silentWarningTitle"), Policy.bind("CVSPreferencePage.silentWarningMessage")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		return composite;
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Creates an new text widget and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @return the new text widget
	 */ 
	private Text createTextField(Composite group) {
		Text text = new Text(group, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		pruneEmptyDirectoriesField.setSelection(store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		timeoutValue.setText(new Integer(store.getInt(ICVSUIConstants.PREF_TIMEOUT)).toString());
		quietnessCombo.add(Policy.bind("CVSPreferencePage.notquiet")); //$NON-NLS-1$
		quietnessCombo.add(Policy.bind("CVSPreferencePage.somewhatquiet")); //$NON-NLS-1$
		quietnessCombo.add(Policy.bind("CVSPreferencePage.reallyquiet")); //$NON-NLS-1$
		quietnessCombo.select(store.getInt(ICVSUIConstants.PREF_QUIETNESS));
		for (int i = 0; i < 10; ++i) {
			compressionLevelCombo.add(Policy.bind("CVSPreferencePage.level" + i)); //$NON-NLS-1$
		}
		compressionLevelCombo.select(store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		historyTracksSelectionButton.setSelection(store.getBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION));
		considerContentsInCompare.setSelection(store.getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS));
		promptOnFileDelete.setSelection(store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_FILE_DELETE));
		promptOnFolderDelete.setSelection(store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE));
		showMarkers.setSelection(store.getBoolean(ICVSUIConstants.PREF_SHOW_MARKERS));
		replaceUnmanaged.setSelection(store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
	}

	/**
	* @see IWorkbenchPreferencePage#init(IWorkbench)
	*/
	public void init(IWorkbench workbench) {
	}

	/**
	 * OK was clicked. Store the CVS preferences.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		
		// Parse the timeout value
		int timeout = Integer.parseInt(timeoutValue.getText());
		
		IPreferenceStore store = getPreferenceStore();
		
		store.setValue(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES, pruneEmptyDirectoriesField.getSelection());
		store.setValue(ICVSUIConstants.PREF_TIMEOUT, timeout);
		store.setValue(ICVSUIConstants.PREF_QUIETNESS, quietnessCombo.getSelectionIndex());
		store.setValue(ICVSUIConstants.PREF_COMPRESSION_LEVEL, compressionLevelCombo.getSelectionIndex());
		store.setValue(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION, historyTracksSelectionButton.getSelection());
		store.setValue(ICVSUIConstants.PREF_CONSIDER_CONTENTS, considerContentsInCompare.getSelection());
		store.setValue(ICVSUIConstants.PREF_PROMPT_ON_FILE_DELETE, promptOnFileDelete.getSelection());
		store.setValue(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE, promptOnFolderDelete.getSelection());
		store.setValue(ICVSUIConstants.PREF_SHOW_MARKERS, showMarkers.getSelection());
		store.setValue(ICVSUIConstants.PREF_REPLACE_UNMANAGED, replaceUnmanaged.getSelection());
		
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(
			store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(
			store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		CVSProviderPlugin.getPlugin().setQuietness(
			getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
		CVSProviderPlugin.getPlugin().setCompressionLevel(
			store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		CVSProviderPlugin.getPlugin().setPromptOnFileDelete(
			store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_FILE_DELETE));
		CVSProviderPlugin.getPlugin().setPromptOnFolderDelete(
			store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE));
		CVSProviderPlugin.getPlugin().setShowTasksOnAddAndDelete(
			store.getBoolean(ICVSUIConstants.PREF_SHOW_MARKERS));
		CVSProviderPlugin.getPlugin().setReplaceUnmanaged(
			store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));

		return true;
	}

	/**
	 * Defaults was clicked. Restore the CVS preferences to
	 * their default values
	 */
	protected void performDefaults() {
		super.performDefaults();
		IPreferenceStore store = getPreferenceStore();
		pruneEmptyDirectoriesField.setSelection(
			store.getDefaultBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		timeoutValue.setText(new Integer(store.getDefaultInt(ICVSUIConstants.PREF_TIMEOUT)).toString());
		quietnessCombo.select(store.getDefaultInt(ICVSUIConstants.PREF_QUIETNESS));
		compressionLevelCombo.select(store.getDefaultInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		historyTracksSelectionButton.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION));
		promptOnFileDelete.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_PROMPT_ON_FILE_DELETE));
		promptOnFolderDelete.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE));
		showMarkers.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_SHOW_MARKERS));
		replaceUnmanaged.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
	}

	/**
	* Returns preference store that belongs to the our plugin.
	* This is important because we want to store
	* our preferences separately from the desktop.
	*
	* @return the preference store for this plugin
	*/
	protected IPreferenceStore doGetPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}
	
	protected static QuietOption getQuietnessOptionFor(int option) {
		switch (option) {
			case 0: return Command.VERBOSE;
			case 1: return Command.PARTLY_QUIET;
			case 2: return Command.SILENT;
		}
		return null;
	}
}