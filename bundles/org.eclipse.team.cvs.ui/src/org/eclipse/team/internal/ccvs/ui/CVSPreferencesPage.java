package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
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
	private Button historyTracksSelectionButton;
	private Button considerContentsInCompare;
	
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

		pruneEmptyDirectoriesField = createCheckBox(composite, Policy.bind("CVSPreferencePage.pruneEmptyDirectories"));

		createLabel(composite, Policy.bind("CVSPreferencePage.timeoutValue"));
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
		
		// Quietness preference removed: bug 11036
		//createLabel(composite, Policy.bind("CVSPreferencePage.quietness"));
		//quietnessCombo = createCombo(composite);
		
		historyTracksSelectionButton = createCheckBox(composite, Policy.bind("CVSPreferencePage.historyTracksSelection"));
		
		considerContentsInCompare = createCheckBox(composite, Policy.bind("CVSPreferencePage.considerContentsInCompare"));
		considerContentsInCompare.setToolTipText(Policy.bind("CVSPreferencePage.considerContentsInCompareTooltip"));
		
		initializeValues();

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
		// Quietness preference removed: bug 11036
		//quietnessCombo.add(Policy.bind("CVSPreferencePage.notquiet"));
		//quietnessCombo.add(Policy.bind("CVSPreferencePage.somewhatquiet"));
		//quietnessCombo.add(Policy.bind("CVSPreferencePage.reallyquiet"));
		//quietnessCombo.select(store.getInt(ICVSUIConstants.PREF_QUIETNESS));
		historyTracksSelectionButton.setSelection(store.getBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION));
		considerContentsInCompare.setSelection(store.getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS));
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
		// Quietness preference removed: bug 11036
		//store.setValue(ICVSUIConstants.PREF_QUIETNESS, quietnessCombo.getSelectionIndex());
		store.setValue(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION, historyTracksSelectionButton.getSelection());
		store.setValue(ICVSUIConstants.PREF_CONSIDER_CONTENTS, considerContentsInCompare.getSelection());
		
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(
			store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(
			store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		// Quietness preference removed: bug 11036
		//CVSProviderPlugin.getPlugin().setQuietness(
		//	getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));

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
		// Quietness preference removed: bug 11036
		//quietnessCombo.select(store.getDefaultInt(ICVSUIConstants.PREF_QUIETNESS));
		historyTracksSelectionButton.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION));
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
	
	// Quietness preference removed: bug 11036
	/*protected static QuietOption getQuietnessOptionFor(int option) {
		switch (option) {
			case 0: return Command.VERBOSE;
			case 1: return Command.PARTLY_QUIET;
			case 2: return Command.SILENT;
		}
		return null;
	}*/
}