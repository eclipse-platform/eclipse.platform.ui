/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;

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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The Editors preference page of the workbench.
 */
public class EditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench workbench;
	
	// State for encoding group
	private String defaultEnc;
	private Button defaultEncodingButton;
	private Button otherEncodingButton;
	private Combo encodingCombo;

	private Button editorListPullDown;
	private Composite editorReuseGroup;
	private Button reuseEditors;
	private Button closeEditorsOnExit;
	private Composite editorReuseIndentGroup;
	private Composite editorReuseThresholdGroup;
	private IntegerFieldEditor reuseEditorsThreshold;	
	private Group dirtyEditorReuseGroup;		
	private Button openNewEditor;
	private Button promptToReuseEditor;

	// TODO: editor tabs 
//	private IntegerFieldEditor numberEditorTabs;
	private Button editorTabSpanMultipleLines;
	
	// editor tab appearance
	private Group editorTabCompressionGroup;
	private Button editorTabCompressionNone;
	private Button editorTabCompressionLow;
	private Button editorTabCompressionMedium;
	private Button editorTabCompressionHigh;
	private int editorTabCompression = EDITOR_TAB_COMPRESSION_HIGH;
	
	// multiply height of tab by these to calculate width   //3,9,15,21
	private static final int EDITOR_TAB_COMPRESSION_NONE = 30;
	private static final int EDITOR_TAB_COMPRESSION_LOW = 9;
	private static final int EDITOR_TAB_COMPRESSION_MEDIUM = 6;
	private static final int EDITOR_TAB_COMPRESSION_HIGH = 3;
	
	private static final int REUSE_INDENT = 10;

	private IntegerFieldEditor recentFilesEditor;

	// hashtable mapping accelerator configuration names to accelerator configuration
	private Hashtable namesToConfiguration;
	// the name of the active accelerator configuration
	private String activeAcceleratorConfigurationName;
			
	private IPropertyChangeListener validityChangeListener =
		new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					updateValidState();
			}
		};
			
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		createEditorHistoryGroup(composite);
		
		WorkbenchPreferencePage.createSpace(composite);
		
		IPreferenceStore store = getPreferenceStore();
		editorListPullDown = new Button(composite, SWT.CHECK);
		editorListPullDown.setText(WorkbenchMessages.getString("WorkbenchPreference.editorsListButton")); //$NON-NLS-1$
		editorListPullDown.setFont(composite.getFont());
		editorListPullDown.setSelection(store.getBoolean(IPreferenceConstants.EDITOR_LIST_PULLDOWN_ACTIVE));
		setButtonLayoutData(editorListPullDown);

		closeEditorsOnExit = new Button(composite, SWT.CHECK);
		closeEditorsOnExit.setText(WorkbenchMessages.getString("WorkbenchPreference.closeEditorsButton")); //$NON-NLS-1$
		closeEditorsOnExit.setFont(composite.getFont());
		closeEditorsOnExit.setSelection(store.getBoolean(IPreferenceConstants.CLOSE_EDITORS_ON_EXIT));
		setButtonLayoutData(closeEditorsOnExit);
		
		createEditorReuseGroup(composite);
		
		WorkbenchPreferencePage.createSpace(composite);		
		createEditorTabCompressionGroup(composite);
		
		// TODO: editor tabs
//		editorTabSpanMultipleLines = new Button(composite, SWT.CHECK);
//		editorTabSpanMultipleLines.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabSpanMultipleLine")); //$NON-NLS-1$
//		editorTabSpanMultipleLines.setFont(composite.getFont());
//		editorTabSpanMultipleLines.setSelection(store.getBoolean(IPreferenceConstants.EDITOR_TABS_SPAN_MULTIPLE_LINES));
//		editorTabSpanMultipleLines.setEnabled(false);
//		setButtonLayoutData(editorTabSpanMultipleLines);
//		
//		createNumberOfEditorTabGroup(composite);		
		WorkbenchPreferencePage.createSpace(composite);
		createEncodingGroup(composite);

		updateValidState();
		
		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);

		return composite;
	}
	
	public void init(IWorkbench aWorkbench) {
		workbench = aWorkbench;
	}
	
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		updateEncodingState(true);
		editorListPullDown.setSelection(store.getDefaultBoolean(IPreferenceConstants.EDITOR_LIST_PULLDOWN_ACTIVE));
		closeEditorsOnExit.setSelection(store.getDefaultBoolean(IPreferenceConstants.CLOSE_EDITORS_ON_EXIT));
		reuseEditors.setSelection(store.getDefaultBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());
		openNewEditor.setSelection(!store.getDefaultBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
		openNewEditor.setEnabled(reuseEditors.getSelection());
		promptToReuseEditor.setSelection(store.getDefaultBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
		promptToReuseEditor.setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.loadDefault();
		reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		recentFilesEditor.loadDefault();
		//TODO: editor tabs
//		numberEditorTabs.loadDefault();
//		editorTabSpanMultipleLines.setSelection(store.getDefaultBoolean(IPreferenceConstants.EDITOR_TABS_SPAN_MULTIPLE_LINES));
		updateEditorTabCompressionState(store.getDefaultInt(IPreferenceConstants.EDITOR_TAB_WIDTH_SCALAR));
	}
	
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();	
		Preferences resourcePrefs = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (defaultEncodingButton.getSelection()) {
			resourcePrefs.setToDefault(ResourcesPlugin.PREF_ENCODING);
		}
		else {
			String enc = encodingCombo.getText();
			resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, enc);
		}
		
		ResourcesPlugin.getPlugin().savePluginPreferences();

		store.setValue(IPreferenceConstants.EDITOR_LIST_PULLDOWN_ACTIVE,editorListPullDown.getSelection());
		store.setValue(IPreferenceConstants.CLOSE_EDITORS_ON_EXIT,closeEditorsOnExit.getSelection());

		// store the reuse editors setting
		store.setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN,reuseEditors.getSelection());
		store.setValue(IPreferenceConstants.REUSE_DIRTY_EDITORS,promptToReuseEditor.getSelection());
		reuseEditorsThreshold.store();

		// store the recent files setting
		recentFilesEditor.store();

		// TODO: editor tabs
		// store the editor tab settings
//		numberEditorTabs.store();
//		store.setValue(IPreferenceConstants.EDITOR_TABS_SPAN_MULTIPLE_LINES, editorTabSpanMultipleLines.getSelection());
		store.setValue(IPreferenceConstants.EDITOR_TAB_WIDTH_SCALAR, editorTabCompression);		
		
		return super.performOk();
	}
	/**
	 * Returns preference store that belongs to the our plugin.
	 *
	 * @return the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
		
	private void createEncodingGroup(Composite parent) {
		
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(WorkbenchMessages.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		group.setFont(font);
		
		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				updateValidState();
			}
		};
		
		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = System.getProperty("file.encoding", "UTF-8");  //$NON-NLS-1$  //$NON-NLS-2$
		defaultEncodingButton.setText(WorkbenchMessages.format("WorkbenchPreference.defaultEncoding", new String[] { defaultEnc })); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);
		
		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(WorkbenchMessages.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
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
			n = Integer.parseInt(WorkbenchMessages.getString("WorkbenchPreference.numDefaultEncodings")); //$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			// Ignore;
		}
		for (int i = 0; i < n; ++i) {
			String enc = WorkbenchMessages.getString("WorkbenchPreference.defaultEncoding" + (i+1), null); //$NON-NLS-1$
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
		if (!recentFilesEditor.isValid()) {
			setErrorMessage(recentFilesEditor.getErrorMessage());
			setValid(false);
		}
		else if (!reuseEditorsThreshold.isValid()) {
			setErrorMessage(reuseEditorsThreshold.getErrorMessage());
			setValid(false);
		}
//		else if (!numberEditorTabs.isValid()) {
//			setErrorMessage(numberEditorTabs.getErrorMessage());
//			setValid(false);
//		}
		else if (!isEncodingValid()) {
			setErrorMessage(WorkbenchMessages.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			setValid(false);
		}
		else {
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
	 * Create a composite that contains entry fields specifying editor reuse preferences.
	 */
	private void createEditorReuseGroup(Composite composite) {
		
		Font font = composite.getFont();
		
		editorReuseGroup = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		// Line up with other entries in preference page
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		editorReuseGroup.setLayout(layout);
		editorReuseGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));	
		editorReuseGroup.setFont(font);	
		
		reuseEditors = new Button(editorReuseGroup, SWT.CHECK);
		reuseEditors.setText(WorkbenchMessages.getString("WorkbenchPreference.reuseEditors")); //$NON-NLS-1$
		reuseEditors.setLayoutData(new GridData());
		reuseEditors.setFont(font);
		
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		reuseEditors.setSelection(store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		reuseEditors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
				reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
				dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());
				openNewEditor.setEnabled(reuseEditors.getSelection());
				promptToReuseEditor.setEnabled(reuseEditors.getSelection());
			}
		});
		
		editorReuseIndentGroup = new Composite(editorReuseGroup, SWT.LEFT);
		GridLayout indentLayout = new GridLayout();
		indentLayout.marginWidth = REUSE_INDENT;
		editorReuseIndentGroup.setLayout(indentLayout);
		editorReuseIndentGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));	
		
		editorReuseThresholdGroup = new Composite(editorReuseIndentGroup, SWT.LEFT);
		editorReuseThresholdGroup.setLayout(new GridLayout());
		editorReuseThresholdGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		editorReuseThresholdGroup.setFont(font);	
		
		reuseEditorsThreshold = new IntegerFieldEditor(IPreferenceConstants.REUSE_EDITORS, WorkbenchMessages.getString("WorkbenchPreference.reuseEditorsThreshold"), editorReuseThresholdGroup); //$NON-NLS-1$
		
		reuseEditorsThreshold.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		reuseEditorsThreshold.setPreferencePage(this);
		reuseEditorsThreshold.setTextLimit(2);
		reuseEditorsThreshold.setErrorMessage(WorkbenchMessages.getString("WorkbenchPreference.reuseEditorsThresholdError")); //$NON-NLS-1$
		reuseEditorsThreshold.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		reuseEditorsThreshold.setValidRange(1, 99);
		reuseEditorsThreshold.load();
		reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.setPropertyChangeListener(validityChangeListener);		
		
		dirtyEditorReuseGroup = new Group(editorReuseIndentGroup, SWT.NONE);
		dirtyEditorReuseGroup.setLayout(new GridLayout());		
		dirtyEditorReuseGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirtyEditorReuseGroup.setText(WorkbenchMessages.getString("WorkbenchPreference.reuseDirtyEditorGroupTitle")); //$NON-NLS-1$
		dirtyEditorReuseGroup.setFont(font);
		dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());
		
		promptToReuseEditor = new Button(dirtyEditorReuseGroup, SWT.RADIO);
		promptToReuseEditor.setText(WorkbenchMessages.getString("WorkbenchPreference.promptToReuseEditor")); //$NON-NLS-1$
		promptToReuseEditor.setFont(font);	
		promptToReuseEditor.setSelection(store.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
		promptToReuseEditor.setEnabled(reuseEditors.getSelection());	

		openNewEditor = new Button(dirtyEditorReuseGroup, SWT.RADIO);
		openNewEditor.setText(WorkbenchMessages.getString("WorkbenchPreference.openNewEditor")); //$NON-NLS-1$
		openNewEditor.setFont(font);	
		openNewEditor.setSelection(!store.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
		openNewEditor.setEnabled(reuseEditors.getSelection());		

	}
	/**
	 * Create a composite that contains entry fields specifying editor history preferences.
	 */
	private void createEditorHistoryGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);	
		groupComposite.setFont(composite.getFont());
		
		recentFilesEditor = new IntegerFieldEditor(IPreferenceConstants.RECENT_FILES, WorkbenchMessages.getString("WorkbenchPreference.recentFiles"), groupComposite); //$NON-NLS-1$

		int recentFilesMax = IPreferenceConstants.MAX_RECENT_FILES_SIZE;
		recentFilesEditor.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		recentFilesEditor.setPreferencePage(this);
		recentFilesEditor.setTextLimit(Integer.toString(recentFilesMax).length());
		recentFilesEditor.setErrorMessage(WorkbenchMessages.format("WorkbenchPreference.recentFilesError", new Object[] { new Integer(recentFilesMax)})); //$NON-NLS-1$
		recentFilesEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		recentFilesEditor.setValidRange(0, recentFilesMax);
		recentFilesEditor.load();
		recentFilesEditor.setPropertyChangeListener(validityChangeListener);
		
	}
	
	/**
	 * Create a composite that contains entry fields specifying number of editor
	 * tabs preferences.
	 */
/*	private void createNumberOfEditorTabGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);
		groupComposite.setFont(composite.getFont());
		
		numberEditorTabs = new IntegerFieldEditor(IPreferenceConstants.NUMBER_EDITOR_TABS, WorkbenchMessages.getString("WorkbenchPreference.numberEditorTabs"), groupComposite); //$NON-NLS-1$
	
		int numberEditorTabsMax = IPreferenceConstants.NUMBER_EDITOR_TABS_MAXIMUM;
		numberEditorTabs.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		numberEditorTabs.setPreferencePage(this);
		numberEditorTabs.setTextLimit(Integer.toString(numberEditorTabsMax).length());
		numberEditorTabs.setErrorMessage(WorkbenchMessages.format("WorkbenchPreference.numberEditorTabsError", new Object[] { new Integer(numberEditorTabsMax)})); //$NON-NLS-1$
		numberEditorTabs.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		numberEditorTabs.setValidRange(0, numberEditorTabsMax);
		numberEditorTabs.load();
		numberEditorTabs.setPropertyChangeListener(validityChangeListener);
	
	}
*/
	private void updateEditorTabCompressionState(int scalar) {
		editorTabCompression = scalar;
		editorTabCompressionNone.setSelection(scalar==EDITOR_TAB_COMPRESSION_NONE);
		editorTabCompressionLow.setSelection(scalar==EDITOR_TAB_COMPRESSION_LOW);
		editorTabCompressionMedium.setSelection(scalar==EDITOR_TAB_COMPRESSION_MEDIUM);
		editorTabCompressionHigh.setSelection(scalar==EDITOR_TAB_COMPRESSION_HIGH);
	}
		
	private void createEditorTabCompressionGroup(Composite composite) {
		/* Create the group */
		Font font = composite.getFont();
		
		editorTabCompressionGroup = new Group(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		editorTabCompressionGroup.setLayout(gridLayout);
		editorTabCompressionGroup.setLayoutData(new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		editorTabCompressionGroup.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabCompression"));
		editorTabCompressionGroup.setFont(font);
	
		/* Create the buttons */
		editorTabCompressionNone = new Button (editorTabCompressionGroup, SWT.RADIO);
		editorTabCompressionNone.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabCompressionNone"));
		editorTabCompressionNone.setFont(font);
		editorTabCompressionLow = new Button (editorTabCompressionGroup, SWT.RADIO);
		editorTabCompressionLow.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabCompressionLow"));
		editorTabCompressionLow.setFont(font);
		editorTabCompressionMedium = new Button(editorTabCompressionGroup, SWT.RADIO);
		editorTabCompressionMedium.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabCompressionMedium"));
		editorTabCompressionMedium.setFont(font);
		editorTabCompressionHigh = new Button (editorTabCompressionGroup, SWT.RADIO);
		editorTabCompressionHigh.setText(WorkbenchMessages.getString("WorkbenchPreference.editorTabCompressionHigh"));
		editorTabCompressionHigh.setFont(font);
	
		/* Add the listeners */
		SelectionAdapter selectionListener = new SelectionAdapter () {
			public void widgetSelected (SelectionEvent event) {
				if (!((Button) event.widget).getSelection ()) {
					return;
				}
				if (editorTabCompressionNone == null) {
					editorTabCompression = EDITOR_TAB_COMPRESSION_HIGH;
					return;
				}
				if (editorTabCompressionNone.getSelection()) {
					editorTabCompression = EDITOR_TAB_COMPRESSION_NONE;
					return;
				}
				if (editorTabCompressionLow.getSelection()) {
					editorTabCompression = EDITOR_TAB_COMPRESSION_LOW;
					return;
				}
				if (editorTabCompressionMedium.getSelection()) {
					editorTabCompression = EDITOR_TAB_COMPRESSION_MEDIUM;
					return;
				}
				if (editorTabCompressionHigh.getSelection()) {
					editorTabCompression = EDITOR_TAB_COMPRESSION_HIGH;
					return;
				}
			};
		};
	
		editorTabCompressionNone.addSelectionListener(selectionListener);
		editorTabCompressionLow.addSelectionListener(selectionListener);
		editorTabCompressionMedium.addSelectionListener(selectionListener);
		editorTabCompressionHigh.addSelectionListener(selectionListener);
	
		/* Set the default state */
		IPreferenceStore store = getPreferenceStore();
		updateEditorTabCompressionState(store.getInt(IPreferenceConstants.EDITOR_TAB_WIDTH_SCALAR));
	}
}

