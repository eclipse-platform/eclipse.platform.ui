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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
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
	
	private static class PerspectiveDescriptorComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			if (o1 instanceof IPerspectiveDescriptor && o2 instanceof IPerspectiveDescriptor) {
				String id1= ((IPerspectiveDescriptor)o1).getLabel();
				String id2= ((IPerspectiveDescriptor)o2).getLabel();
				return Collator.getInstance().compare(id1, id2);
			}
			return 0;
		}
	}
    
	
	private abstract class Field {
		protected final String fKey;
		public Field(String key) { fFields.add(this); fKey= key; }
		public abstract void initializeValue(IPreferenceStore store);
		public abstract void performOk(IPreferenceStore store);
		public void performDefaults(IPreferenceStore store) {
			store.setToDefault(fKey);
			initializeValue(store);
		}
	}
	
	private class Checkbox extends Field {
		
		private final Button fCheckbox;
		
		public Checkbox(Composite composite, String key, String label, String helpID) {
			super(key);
			fCheckbox= new Button(composite, SWT.CHECK);
			fCheckbox.setText(label);
			WorkbenchHelp.setHelp(fCheckbox, helpID);
		}
		
		public void initializeValue(IPreferenceStore store) {
			fCheckbox.setSelection(store.getBoolean(fKey));
		}
		
		public void performOk(IPreferenceStore store) {
			store.setValue(fKey, fCheckbox.getSelection());
		}
	}

	private abstract class ComboBox extends Field {
		protected final Combo fCombo;
		private final String [] fLabels;
		private final List fValues;
		
		public ComboBox(Composite composite, String key, String text, String helpID, String [] labels, Object [] values) {
			super(key);
			fLabels= labels;
			fValues= Arrays.asList(values);
			
			final Label label= SWTUtils.createLabel(composite, text);
			fCombo= new Combo(composite, SWT.READ_ONLY);
			fCombo.setLayoutData(SWTUtils.createHFillGridData());
			fCombo.setItems(labels);
			
			if (((GridLayout)composite.getLayout()).numColumns > 1) {
				label.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
			}
			
			WorkbenchHelp.setHelp(fCombo, helpID);
		}
		
		public Combo getCombo() {
			return fCombo;
		}
		
		public void initializeValue(IPreferenceStore store) {
			final Object value= getValue(store, fKey);
			final int index= fValues.indexOf(value); 
			if (index >= 0 && index < fLabels.length)
				fCombo.select(index);
			else 
				fCombo.select(0);
		}
		
		public void performOk(IPreferenceStore store) {
			saveValue(store, fKey, fValues.get(fCombo.getSelectionIndex()));
		}
		
		protected abstract void saveValue(IPreferenceStore store, String key, Object object);
		protected abstract Object getValue(IPreferenceStore store, String key);
	}
	
	private class IntegerComboBox extends ComboBox {
		public IntegerComboBox(Composite composite, String key, String label, String helpID, String[] labels, Integer [] values) {
			super(composite, key, label, helpID, labels, values);
		}

		protected void saveValue(IPreferenceStore store, String key, Object object) {
			store.setValue(key, ((Integer)object).intValue());			
		}
		
		protected Object getValue(IPreferenceStore store, String key) {
			return new Integer(store.getInt(key));			
		}
	}
	
	private class StringComboBox extends ComboBox {
		
		public StringComboBox(Composite composite, String key, String label, String helpID, String [] labels, String [] values) {
			super(composite, key, label, helpID, labels, values);
		}

		protected Object getValue(IPreferenceStore store, String key) {
			return store.getString(key);
		}
		
		protected void saveValue(IPreferenceStore store, String key, Object object) {
			store.setValue(key, (String)object);
		}
	}

	private abstract class RadioButtons extends Field {
		protected final Button [] fButtons;
		private final String [] fLabels;
		private final List fValues;
		private final Group fGroup;
		
		public RadioButtons(Composite composite, String key, String label, String helpID, String [] labels, Object [] values) {
			super(key);
			fLabels= labels;
			fValues= Arrays.asList(values);
			
			fGroup= SWTUtils.createHFillGroup(composite, label, SWTUtils.MARGINS_DEFAULT, labels.length);
			
			
			fButtons= new Button [labels.length];
			for (int i = 0; i < fLabels.length; i++) {
				fButtons[i]= new Button(fGroup, SWT.RADIO);
				fButtons[i].setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
				fButtons[i].setText(labels[i]);
			}
			SWTUtils.equalizeControls(SWTUtils.createDialogPixelConverter(composite), fButtons, 0, fButtons.length - 2);
			WorkbenchHelp.setHelp(fGroup, helpID);
		}
		
		public void initializeValue(IPreferenceStore store) {
			final Object value= loadValue(store, fKey);
			final int index= fValues.indexOf(value);
			if (index >= 0 && index < fLabels.length)
				fButtons[index].setSelection(true);
		}

		public void performOk(IPreferenceStore store) {
			for (int i = 0; i < fButtons.length; ++i) {
				if (fButtons[i].getSelection()) {
					saveValue(store, fKey, fValues.get(i));
					return;
				} 
			}
		}
		
		public Control getControl() {
			return fGroup;
		}
		
		protected abstract Object loadValue(IPreferenceStore store, String key);

		protected abstract void saveValue(IPreferenceStore store, String key, Object value);
	}

	private class IntegerRadioButtons extends RadioButtons {

		public IntegerRadioButtons(Composite composite, String key, String label, String helpID, String[] labels, Integer [] values) {
			super(composite, key, label, helpID, labels, values);
		}

		protected Object loadValue(IPreferenceStore store, String key) {
			return new Integer(store.getInt(key));
		}

		protected void saveValue(IPreferenceStore store, String key, Object value) {
			store.setValue(key, ((Integer)value).intValue());
		}
	}
	
	private class StringRadioButtons extends RadioButtons {

		public StringRadioButtons(Composite composite, String key, String label, String helpID, String[] labels, String [] values) {
			super(composite, key, label, helpID, labels, values);
		}

		protected Object loadValue(IPreferenceStore store, String key) {
			return store.getString(key);
		}

		protected void saveValue(IPreferenceStore store, String key, Object value) {
			store.setValue(key, (String)value);
		}
	}
	
	private abstract class TextField extends Field {
		protected final Text fText;
		
		public TextField(Composite composite, String key, String text, String helpID) {
			super(key);
			
			final Label label= new Label(composite, SWT.WRAP);
			label.setText(text);
			label.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
			
			fText= SWTUtils.createText(composite);
			
			WorkbenchHelp.setHelp(fText, helpID);
		}
		
		public Text getControl() {
			return fText;
		}
		
		public void initializeValue(IPreferenceStore store) {
			final String value= store.getString(fKey);
			fText.setText(value);
		}
		
		public void performOk(IPreferenceStore store) {
			store.setValue(fKey, fText.getText());
		}
		
		protected abstract void modifyText(Text text);
	}
	
	private final String [] KSUBST_VALUES;
	private final String [] KSUBST_LABELS;
	
	private final String [] COMPRESSION_LABELS;
	private final Integer [] COMPRESSION_VALUES;
	
	protected final ArrayList fFields;
	private final String [] PERSPECTIVE_VALUES;
	private final String [] PERSPECTIVE_LABELS;
	private final String [] YES_NO_PROMPT;
	
	public CVSPreferencesPage() {
		fFields= new ArrayList();
		
		final KSubstOption[] options = KSubstOption.getAllKSubstOptions();
		final ArrayList KSUBST_MODES= new ArrayList();
		for (int i = 0; i < options.length; i++) {
			final KSubstOption option = options[i];
			if (!option.isBinary()) {
				KSUBST_MODES.add(option);
			}
		}
		Collections.sort(KSUBST_MODES, new Comparator() {
			public int compare(Object a, Object b) {
				final String aKey = ((KSubstOption) a).getLongDisplayText();
				final String bKey = ((KSubstOption) b).getLongDisplayText();
				return aKey.compareTo(bKey);
			}
		});
		
		KSUBST_LABELS= new String[KSUBST_MODES.size()];
		KSUBST_VALUES= new String[KSUBST_MODES.size()];
		int index= 0;
		for (Iterator iter = KSUBST_MODES.iterator(); iter.hasNext();) {
			KSubstOption mod = (KSubstOption) iter.next();
			KSUBST_LABELS[index]= mod.getLongDisplayText();
			final String mode= mod.toMode().trim();
			KSUBST_VALUES[index]= mode.length() != 0 ? mode : "-kkv";    //$NON-NLS-1$
			++index;
		}	
	
		COMPRESSION_LABELS= new String [] { Policy.bind("CVSPreferencesPage.0"), Policy.bind("CVSPreferencesPage.1"), Policy.bind("CVSPreferencesPage.2"), Policy.bind("CVSPreferencesPage.3"), Policy.bind("CVSPreferencesPage.4"), Policy.bind("CVSPreferencesPage.5"), Policy.bind("CVSPreferencesPage.6"), Policy.bind("CVSPreferencesPage.7"), Policy.bind("CVSPreferencesPage.8"), Policy.bind("CVSPreferencesPage.9") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		COMPRESSION_VALUES= new Integer [COMPRESSION_LABELS.length];
		for (int i = 0; i < COMPRESSION_VALUES.length; i++) {
			COMPRESSION_VALUES[i]= new Integer(i);
		}
		
	    final IPerspectiveDescriptor [] perspectives= PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
	    PERSPECTIVE_VALUES= new String[perspectives.length + 1];
	    PERSPECTIVE_LABELS= new String [perspectives.length + 1];
		Arrays.sort(perspectives, new PerspectiveDescriptorComparator());
		PERSPECTIVE_VALUES[0]= ICVSUIConstants.OPTION_NO_PERSPECTIVE;
		PERSPECTIVE_LABELS[0]= Policy.bind("CVSPreferencesPage.10"); //$NON-NLS-1$
		for (int i = 0; i < perspectives.length; i++) {
			PERSPECTIVE_VALUES[i + 1]= perspectives[i].getId();
			PERSPECTIVE_LABELS[i + 1]= perspectives[i].getLabel();
		}
		
		YES_NO_PROMPT= new String [] { Policy.bind("CVSPreferencesPage.11"), Policy.bind("CVSPreferencesPage.12"), Policy.bind("CVSPreferencesPage.13") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		/**
		 * Handle deleted perspectives
		 */
		final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
		final String id= store.getString(ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS);
		if (PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id) == null) {
			store.putValue(ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS, ICVSUIConstants.OPTION_NO_PERSPECTIVE);
		}
	}

	protected Control createContents(Composite parent) {
		
		// create a tab folder for the page
		final TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayoutData(SWTUtils.createHFillGridData());

		createGeneralTab(tabFolder);
		createFilesFoldersTab(tabFolder);
		createConnectionTab(tabFolder);
		createPromptingTab(tabFolder);
		
		return tabFolder;
	}

	private Composite createGeneralTab(final TabFolder tabFolder) {
		final Composite composite = SWTUtils.createHFillComposite(tabFolder, SWTUtils.MARGINS_DEFAULT);
		final TabItem tab= new TabItem(tabFolder, SWT.NONE);
		tab.setText(Policy.bind("CVSPreferencesPage.14")); //$NON-NLS-1$
		tab.setControl(composite);
		new Checkbox(composite, ICVSUIConstants.PREF_DETERMINE_SERVER_VERSION,  Policy.bind("CVSPreferencesPage.15"), IHelpContextIds.PREF_DETERMINE_SERVER_VERSION); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_CONFIRM_MOVE_TAG, Policy.bind("CVSPreferencesPage.16"), IHelpContextIds.PREF_CONFIRM_MOVE_TAG); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_DEBUG_PROTOCOL, Policy.bind("CVSPreferencesPage.17"), IHelpContextIds.PREF_DEBUG_PROTOCOL); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_AUTO_REFRESH_TAGS_IN_TAG_SELECTION_DIALOG, Policy.bind("CVSPreferencesPage.18"), IHelpContextIds.PREF_AUTOREFRESH_TAG); //$NON-NLS-1$
		return composite;
	}
	
	private Composite createConnectionTab(final TabFolder tabFolder) {
		final Composite composite = SWTUtils.createHFillComposite(tabFolder, SWTUtils.MARGINS_DEFAULT);
		final TabItem tab= new TabItem(tabFolder, SWT.NONE);
		tab.setText(Policy.bind("CVSPreferencesPage.19")); //$NON-NLS-1$
		tab.setControl(composite);
		new Checkbox(composite, ICVSUIConstants.PREF_DETERMINE_SERVER_VERSION,  Policy.bind("CVSPreferencesPage.20"), IHelpContextIds.PREF_DETERMINE_SERVER_VERSION); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_DEBUG_PROTOCOL, Policy.bind("CVSPreferencesPage.21"), IHelpContextIds.PREF_DEBUG_PROTOCOL); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_AUTO_REFRESH_TAGS_IN_TAG_SELECTION_DIALOG, Policy.bind("CVSPreferencesPage.22"), IHelpContextIds.PREF_AUTOREFRESH_TAG); //$NON-NLS-1$

		SWTUtils.createPlaceholder(composite, 1);
		final Composite textComposite= SWTUtils.createHFillComposite(composite, SWTUtils.MARGINS_NONE, 2);
		new TextField(
				textComposite, 
				ICVSUIConstants.PREF_TIMEOUT, 
				Policy.bind("CVSPreferencesPage.23"),  //$NON-NLS-1$
				IHelpContextIds.PREF_COMMS_TIMEOUT) {
			protected void modifyText(Text text) {
				// Parse the timeout value
				try {
					final int x = Integer.parseInt(text.getText());
					if (x >= 0) {
						setErrorMessage(null);
						setValid(true);
					} else {
						setErrorMessage(Policy.bind("CVSPreferencesPage.24")); //$NON-NLS-1$
						setValid(false);
					}
				} catch (NumberFormatException ex) {
					setErrorMessage(Policy.bind("CVSPreferencesPage.25")); //$NON-NLS-1$
					setValid(false);
				}
			}
		};
		
		final ComboBox quietnessCombo = new IntegerComboBox(
				textComposite, 
				ICVSUIConstants.PREF_QUIETNESS, 
				Policy.bind("CVSPreferencesPage.26"),  //$NON-NLS-1$
				IHelpContextIds.PREF_QUIET,
				new String [] { Policy.bind("CVSPreferencesPage.27"), Policy.bind("CVSPreferencesPage.28"), Policy.bind("CVSPreferencesPage.29") }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new Integer [] { new Integer(0), new Integer(1), new Integer(2)});
		
		quietnessCombo.getCombo().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (getQuietnessOptionFor(quietnessCombo.getCombo().getSelectionIndex()).equals(Command.SILENT)) {
					MessageDialog.openWarning(getShell(), Policy.bind("CVSPreferencesPage.30"), Policy.bind("CVSPreferencesPage.31"));  //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		new IntegerComboBox(
				textComposite, 
				ICVSUIConstants.PREF_COMPRESSION_LEVEL, 
				Policy.bind("CVSPreferencesPage.32"),  //$NON-NLS-1$
				IHelpContextIds.PREF_COMPRESSION, 
				COMPRESSION_LABELS, COMPRESSION_VALUES);

		return composite;
	}

	
	private Composite createFilesFoldersTab(final TabFolder tabFolder) {
		final Composite composite = SWTUtils.createHFillComposite(tabFolder, SWTUtils.MARGINS_DEFAULT);
		final TabItem tab= new TabItem(tabFolder, SWT.NONE);
		tab.setText(Policy.bind("CVSPreferencesPage.33")); //$NON-NLS-1$
		tab.setControl(composite);
		new Checkbox(composite, ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY, Policy.bind("CVSPreferencesPage.34"), IHelpContextIds.PREF_TREAT_NEW_FILE_AS_BINARY); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_USE_PLATFORM_LINEEND, Policy.bind("CVSPreferencesPage.35"), IHelpContextIds.PREF_LINEEND); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES, Policy.bind("CVSPreferencesPage.36"), IHelpContextIds.PREF_PRUNE); //$NON-NLS-1$
		new Checkbox(composite, ICVSUIConstants.PREF_REPLACE_UNMANAGED, Policy.bind("CVSPreferencesPage.37"), IHelpContextIds.PREF_REPLACE_DELETE_UNMANAGED); //$NON-NLS-1$
		SWTUtils.createPlaceholder(composite, 1);
		final Composite bottom= SWTUtils.createHFillComposite(composite, SWTUtils.MARGINS_NONE, 2);
		new StringComboBox(
				bottom, 
				ICVSUIConstants.PREF_TEXT_KSUBST, 
				Policy.bind("CVSPreferencesPage.38"),  //$NON-NLS-1$
				IHelpContextIds.PREF_KEYWORDMODE, 
				KSUBST_LABELS, KSUBST_VALUES);
		
		return composite;
	}
	
	private Composite createPromptingTab(final TabFolder tabFolder) {

		final Composite composite = SWTUtils.createHFillComposite(tabFolder, SWTUtils.MARGINS_DEFAULT, 1);
		final TabItem tab= new TabItem(tabFolder, SWT.NONE);
		tab.setText(Policy.bind("CVSPreferencesPage.39")); //$NON-NLS-1$
		tab.setControl(composite);
		
		new StringRadioButtons(
				composite,
				ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS,
				Policy.bind("CVSPreferencesPage.40"), //$NON-NLS-1$
				IHelpContextIds.PREF_ALLOW_EMPTY_COMMIT_COMMENTS,
				YES_NO_PROMPT,
				new String [] { MessageDialogWithToggle.ALWAYS, MessageDialogWithToggle.NEVER, MessageDialogWithToggle.PROMPT });
		
		new IntegerRadioButtons(composite, 
				ICVSUIConstants.PREF_SAVE_DIRTY_EDITORS, 
				Policy.bind("CVSPreferencesPage.41"),  //$NON-NLS-1$
				IHelpContextIds.PREF_SAVE_DIRTY_EDITORS, 
	    		YES_NO_PROMPT,
	    		new Integer [] { new Integer(ICVSUIConstants.OPTION_AUTOMATIC),	new Integer(ICVSUIConstants.OPTION_NEVER), 	new Integer(ICVSUIConstants.OPTION_PROMPT)});
				
	    new StringRadioButtons(
	    		composite, 
	    		ICVSUIConstants.PREF_CHANGE_PERSPECTIVE_ON_SHOW_ANNOTATIONS, 
	    		Policy.bind("CVSPreferencesPage.42"), //$NON-NLS-1$
	    		IHelpContextIds.PREF_CHANGE_PERSPECTIVE_ON_SHOW_ANNOTATIONS,
	    		YES_NO_PROMPT,
	    		new String [] { MessageDialogWithToggle.ALWAYS, MessageDialogWithToggle.NEVER,	MessageDialogWithToggle.PROMPT }
	    		);
	    
	    SWTUtils.createPlaceholder(composite, 1);
	    new StringComboBox(
	    		composite, 
	    		ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS,
	    		Policy.bind("CVSPreferencesPage.43"), //$NON-NLS-1$
	    		IHelpContextIds.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS,
	    		PERSPECTIVE_LABELS,
	    		PERSPECTIVE_VALUES);

		initializeValues();

		return composite;
	}
	
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		final IPreferenceStore store = getPreferenceStore();
		for (Iterator iter = fFields.iterator(); iter.hasNext();) {
			((Field)iter.next()).initializeValue(store);
		}
	}

	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {

		final IPreferenceStore store = getPreferenceStore();
		for (Iterator iter = fFields.iterator(); iter.hasNext();) {
			((Field) iter.next()).performOk(store);
		}

		CVSProviderPlugin.getPlugin().setReplaceUnmanaged(store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		CVSProviderPlugin.getPlugin().setQuietness(getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
		CVSProviderPlugin.getPlugin().setCompressionLevel(store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		CVSProviderPlugin.getPlugin().setDebugProtocol(store.getBoolean(ICVSUIConstants.PREF_DEBUG_PROTOCOL));
		CVSProviderPlugin.getPlugin().setRepositoriesAreBinary(store.getBoolean(ICVSUIConstants.PREF_REPOSITORIES_ARE_BINARY));
		KSubstOption oldKSubst = CVSProviderPlugin.getPlugin().getDefaultTextKSubstOption();
		KSubstOption newKSubst = KSubstOption.fromMode(store.getString(ICVSUIConstants.PREF_TEXT_KSUBST));
		CVSProviderPlugin.getPlugin().setDefaultTextKSubstOption(newKSubst);
		CVSProviderPlugin.getPlugin().setUsePlatformLineend(store.getBoolean(ICVSUIConstants.PREF_USE_PLATFORM_LINEEND));
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

	protected void performDefaults() {
		super.performDefaults();
		final IPreferenceStore store = getPreferenceStore();
		for (Iterator iter = fFields.iterator(); iter.hasNext();) {
			((Field) iter.next()).performDefaults(store);
		}
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
