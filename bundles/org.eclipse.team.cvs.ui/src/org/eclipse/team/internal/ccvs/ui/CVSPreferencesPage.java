/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

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
public class CVSPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button pruneEmptyDirectoriesField;
	private Text timeoutValue;
	private Combo quietnessCombo;
	private Combo compressionLevelCombo;
	private Combo ksubstCombo;
	private Button usePlatformLineend;
	private List ksubstOptions;
	private Button replaceUnmanaged;
	private Button repositoriesAreBinary;
	private Button determineVersionEnabled;
	private Button confirmMoveTag;
	private Button debugProtocol;
	private Button autoRefreshTags;
	
	private Button never;
	private Button prompt;
	private Button auto;

	public CVSPreferencesPage() {
		// sort the options by display text
		setDescription(Policy.bind("CVSPreferencePage.description")); //$NON-NLS-1$
		KSubstOption[] options = KSubstOption.getAllKSubstOptions();
		this.ksubstOptions = new ArrayList();
		for (int i = 0; i < options.length; i++) {
			KSubstOption option = options[i];
			if (! option.isBinary()) {
				ksubstOptions.add(option);
			}
		}
		Collections.sort(ksubstOptions, new Comparator() {
			public int compare(Object a, Object b) {
				String aKey = ((KSubstOption) a).getLongDisplayText();
				String bKey = ((KSubstOption) b).getLongDisplayText();
				return aKey.compareTo(bKey);
			}
		});
	}

	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent, int widthChars) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		GC gc = new GC(combo);
	 	gc.setFont(combo.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();		
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, widthChars);
		gc.dispose();
		combo.setLayoutData(data);
		return combo;
	}

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
		layout.marginWidth = 0;
		layout.marginHeight = 0;
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

		pruneEmptyDirectoriesField = createCheckBox(composite, Policy.bind("CVSPreferencePage.pruneEmptyDirectories")); //$NON-NLS-1$	
		replaceUnmanaged = createCheckBox(composite, Policy.bind("CVSPreferencePage.replaceUnmanaged")); //$NON-NLS-1$
		repositoriesAreBinary = createCheckBox(composite, Policy.bind("CVSPreferencePage.repositoriesAreBinary")); //$NON-NLS-1$
		determineVersionEnabled = createCheckBox(composite, Policy.bind("CVSPreferencePage.determineVersionEnabled")); //$NON-NLS-1$
		confirmMoveTag = createCheckBox(composite, Policy.bind("CVSPreferencePage.confirmMoveTag")); //$NON-NLS-1$
		debugProtocol = createCheckBox(composite, Policy.bind("CVSPreferencePage.debugProtocol")); //$NON-NLS-1$
		usePlatformLineend = createCheckBox(composite, Policy.bind("CVSPreferencePage.lineend")); //$NON-NLS-1$
		autoRefreshTags = createCheckBox(composite, Policy.bind("CVSPreferencePage.autoRefreshTags")); //$NON-NLS-1$
			
		createLabel(composite, ""); createLabel(composite, ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		createLabel(composite, Policy.bind("CVSPreferencePage.timeoutValue")); //$NON-NLS-1$
		timeoutValue = createTextField(composite);
		timeoutValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Parse the timeout value
				try {
					int x = Integer.parseInt(timeoutValue.getText());
					if (x >= 0) {
						setErrorMessage(null);
						setValid(true);
					} else {
						setErrorMessage(Policy.bind("CVSPreferencesPage.Timeout_must_not_be_negative_1")); //$NON-NLS-1$
						setValid(false);
					}
				} catch (NumberFormatException ex) {
					setErrorMessage(Policy.bind("CVSPreferencesPage.Timeout_must_be_a_number_2")); //$NON-NLS-1$
					setValid(false);
				}
			}
		});
		
		createLabel(composite, Policy.bind("CVSPreferencePage.quietness")); //$NON-NLS-1$
		quietnessCombo = createCombo(composite);

		createLabel(composite, Policy.bind("CVSPreferencePage.compressionLevel")); //$NON-NLS-1$
		compressionLevelCombo = createCombo(composite);
		
		createLabel(composite, Policy.bind("CVSPreferencePage.defaultTextKSubst")); //$NON-NLS-1$
		int chars = 0;
		for (Iterator it = ksubstOptions.iterator(); it.hasNext();) {
			KSubstOption option = (KSubstOption) it.next();
			int c = option.getLongDisplayText().length();
			if(c > chars) {
				chars = c;
			}
		}
		ksubstCombo = createCombo(composite, chars);
		
		createLabel(composite, ""); createLabel(composite, ""); //$NON-NLS-1$ //$NON-NLS-2$
				
		createSaveCombo(composite);
				
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

		WorkbenchHelp.setHelp(pruneEmptyDirectoriesField, IHelpContextIds.PREF_PRUNE);
		WorkbenchHelp.setHelp(compressionLevelCombo, IHelpContextIds.PREF_COMPRESSION);
		WorkbenchHelp.setHelp(quietnessCombo, IHelpContextIds.PREF_QUIET);
		WorkbenchHelp.setHelp(ksubstCombo, IHelpContextIds.PREF_KEYWORDMODE);
		WorkbenchHelp.setHelp(usePlatformLineend, IHelpContextIds.PREF_LINEEND);
		WorkbenchHelp.setHelp(timeoutValue, IHelpContextIds.PREF_COMMS_TIMEOUT);
		WorkbenchHelp.setHelp(replaceUnmanaged, IHelpContextIds.PREF_REPLACE_DELETE_UNMANAGED);
		WorkbenchHelp.setHelp(repositoriesAreBinary, IHelpContextIds.PREF_TREAT_NEW_FILE_AS_BINARY);
		WorkbenchHelp.setHelp(determineVersionEnabled, IHelpContextIds.PREF_DETERMINE_SERVER_VERSION);
		WorkbenchHelp.setHelp(confirmMoveTag, IHelpContextIds.PREF_CONFIRM_MOVE_TAG);
		WorkbenchHelp.setHelp(autoRefreshTags, IHelpContextIds.PREF_AUTOREFRESH_TAG);
		Dialog.applyDialogFont(parent);
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
		repositoriesAreBinary.setSelection(store.getBoolean(ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY));
		quietnessCombo.add(Policy.bind("CVSPreferencePage.notquiet")); //$NON-NLS-1$
		quietnessCombo.add(Policy.bind("CVSPreferencePage.somewhatquiet")); //$NON-NLS-1$
		quietnessCombo.add(Policy.bind("CVSPreferencePage.reallyquiet")); //$NON-NLS-1$
		quietnessCombo.select(store.getInt(ICVSUIConstants.PREF_QUIETNESS));
		for (int i = 0; i < 10; ++i) {
			compressionLevelCombo.add(Policy.bind("CVSPreferencePage.level" + i)); //$NON-NLS-1$
		}
		compressionLevelCombo.select(store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		for (Iterator it = ksubstOptions.iterator(); it.hasNext();) {
			KSubstOption option = (KSubstOption) it.next();
			ksubstCombo.add(option.getLongDisplayText());
		}
		ksubstCombo.select(getKSubstComboIndexFor(store.getString(ICVSUIConstants.PREF_TEXT_KSUBST)));
		usePlatformLineend.setSelection(store.getBoolean(ICVSUIConstants.PREF_USE_PLATFORM_LINEEND));
		replaceUnmanaged.setSelection(store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
		determineVersionEnabled.setSelection(store.getBoolean(ICVSUIConstants.PREF_DETERMINE_SERVER_VERSION));
		confirmMoveTag.setSelection(store.getBoolean(ICVSUIConstants.PREF_CONFIRM_MOVE_TAG));
		debugProtocol.setSelection(store.getBoolean(ICVSUIConstants.PREF_DEBUG_PROTOCOL));
		autoRefreshTags.setSelection(store.getBoolean(ICVSUIConstants.PREF_AUTO_REFRESH_TAGS_IN_TAG_SELECTION_DIALOG));
		
		initializeSaveRadios(store.getInt(ICVSUIConstants.PREF_SAVE_DIRTY_EDITORS));		
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
		
		// set the provider preferences first because the preference change
		// listeners invoked from the preference store change may need these
		// values
		
		store.setValue(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES, pruneEmptyDirectoriesField.getSelection());
		store.setValue(ICVSUIConstants.PREF_TIMEOUT, timeout);
		store.setValue(ICVSUIConstants.PREF_QUIETNESS, quietnessCombo.getSelectionIndex());
		store.setValue(ICVSUIConstants.PREF_COMPRESSION_LEVEL, compressionLevelCombo.getSelectionIndex());
		// Text mode processed separately to avoid empty string in properties file.
		String mode =((KSubstOption)ksubstOptions.get(ksubstCombo.getSelectionIndex())).toMode();
		if (mode.length() == 0) {
			mode = "-kkv"; //$NON-NLS-1$
		}
		store.setValue(ICVSUIConstants.PREF_TEXT_KSUBST, mode);
		store.setValue(ICVSUIConstants.PREF_USE_PLATFORM_LINEEND, usePlatformLineend.getSelection());
		store.setValue(ICVSUIConstants.PREF_REPLACE_UNMANAGED, replaceUnmanaged.getSelection());
		store.setValue(ICVSUIConstants.PREF_SAVE_DIRTY_EDITORS, getSaveRadio());
		store.setValue(ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY, repositoriesAreBinary.getSelection());
		store.setValue(ICVSUIConstants.PREF_DETERMINE_SERVER_VERSION, determineVersionEnabled.getSelection());
		store.setValue(ICVSUIConstants.PREF_CONFIRM_MOVE_TAG, confirmMoveTag.getSelection());
		store.setValue(ICVSUIConstants.PREF_DEBUG_PROTOCOL, debugProtocol.getSelection());
		store.setValue(ICVSUIConstants.PREF_AUTO_REFRESH_TAGS_IN_TAG_SELECTION_DIALOG, autoRefreshTags.getSelection());
		
		CVSProviderPlugin.getPlugin().setReplaceUnmanaged(
			store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(
			store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(
			store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		CVSProviderPlugin.getPlugin().setQuietness(
			getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
		CVSProviderPlugin.getPlugin().setCompressionLevel(
			store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		CVSProviderPlugin.getPlugin().setDebugProtocol(
					store.getBoolean(ICVSUIConstants.PREF_DEBUG_PROTOCOL));
		CVSProviderPlugin.getPlugin().setRepositoriesAreBinary(store.getBoolean(ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY));
		KSubstOption oldKSubst = CVSProviderPlugin.getPlugin().getDefaultTextKSubstOption();
		KSubstOption newKSubst = KSubstOption.fromMode(store.getString(ICVSUIConstants.PREF_TEXT_KSUBST));
		CVSProviderPlugin.getPlugin().setDefaultTextKSubstOption(newKSubst);
		CVSProviderPlugin.getPlugin().setUsePlatformLineend(
				store.getBoolean(ICVSUIConstants.PREF_USE_PLATFORM_LINEEND));
		CVSProviderPlugin.getPlugin().setDetermineVersionEnabled(store.getBoolean(ICVSUIConstants.PREF_DETERMINE_SERVER_VERSION));
		CVSProviderPlugin.getPlugin().setConfirmMoveTagEnabled(store.getBoolean(ICVSUIConstants.PREF_CONFIRM_MOVE_TAG));
		
		// changing the default keyword substitution mode for text files may affect
		// information displayed in the decorators
		if (! oldKSubst.equals(newKSubst)) {
			CVSUIPlugin.broadcastPropertyChange(new PropertyChangeEvent(this, CVSUIPlugin.P_DECORATORS_CHANGED, null, null));
		}
		
		CVSUIPlugin.getPlugin().savePluginPreferences();
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
		ksubstCombo.select(getKSubstComboIndexFor(store.getDefaultString(ICVSUIConstants.PREF_TEXT_KSUBST)));
		usePlatformLineend.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_USE_PLATFORM_LINEEND));
		replaceUnmanaged.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
		initializeSaveRadios(store.getDefaultInt(ICVSUIConstants.PREF_SAVE_DIRTY_EDITORS));
		repositoriesAreBinary.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY));
		confirmMoveTag.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_CONFIRM_MOVE_TAG));
		debugProtocol.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_DEBUG_PROTOCOL));
		autoRefreshTags.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_AUTO_REFRESH_TAGS_IN_TAG_SELECTION_DIALOG));
	}

   private void createSaveCombo(Composite composite) {
		Group group = new Group(composite, SWT.NULL);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		group.setText(Policy.bind("CVSPreferencePage.Save_dirty_editors_before_CVS_operations_1")); //$NON-NLS-1$
		
		never = new Button(group, SWT.RADIO | SWT.LEFT);
		never.setLayoutData(new GridData());
		never.setText(Policy.bind("CVSPreferencePage.&Never_2")); //$NON-NLS-1$
		
		prompt = new Button(group, SWT.RADIO | SWT.LEFT);
		prompt.setLayoutData(new GridData());
		prompt.setText(Policy.bind("CVSPreferencePage.&Prompt_3")); //$NON-NLS-1$
		
		auto = new Button(group, SWT.RADIO | SWT.LEFT);
		auto.setLayoutData(new GridData());
		auto.setText(Policy.bind("CVSPreferencePage.Auto-&save_4")); //$NON-NLS-1$
		
		WorkbenchHelp.setHelp(group, IHelpContextIds.PREF_SAVE_DIRTY_EDITORS);
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
	
	protected int getKSubstComboIndexFor(String mode) {
		KSubstOption ksubst = KSubstOption.fromMode(mode);
		int i = 0;
		for (Iterator it = ksubstOptions.iterator(); it.hasNext();) {
			KSubstOption option = (KSubstOption) it.next();
			if (ksubst.equals(option)) return i;
			i++;
		}
		// unknown option, add it to the list
		ksubstOptions.add(ksubst);
		ksubstCombo.add(ksubst.getLongDisplayText());
		return i;
	}
	
	protected void initializeSaveRadios(int option) {
		auto.setSelection(false);
		never.setSelection(false);
		prompt.setSelection(false);
		switch(option) {
			case ICVSUIConstants.OPTION_AUTOMATIC:
				auto.setSelection(true); break;
			case ICVSUIConstants.OPTION_NEVER:
				never.setSelection(true); break;
			case ICVSUIConstants.OPTION_PROMPT:
				prompt.setSelection(true); break;
		}
	}
	
	protected int getSaveRadio() {
		if(auto.getSelection()) {
			return ICVSUIConstants.OPTION_AUTOMATIC;
		} else if(never.getSelection()) {
			return ICVSUIConstants.OPTION_NEVER;
		} else {
			return ICVSUIConstants.OPTION_PROMPT;
		}
	}
}
