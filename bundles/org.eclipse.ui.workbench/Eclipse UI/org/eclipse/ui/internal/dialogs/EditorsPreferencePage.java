/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 97667 [Preferences] Pref Page General/Editors - problems
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 489891
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorHistory;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The Editors preference page of the workbench.
 */
public class EditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Composite editorReuseGroup;

	private Button reuseEditors;

	protected Button useIPersistableEditor;

	private Composite editorReuseIndentGroup;

	private Composite editorReuseThresholdGroup;

	private IntegerFieldEditor reuseEditorsThreshold;

	private IntegerFieldEditor recentFilesEditor;

	private IPropertyChangeListener validityChangeListener = event -> {
		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			updateValidState();
		}
	};

	private Button promptWhenStillOpenEditor;

	private Button allowInplaceEditor;

	private ComboFieldEditor multiPageEditorTabPositionComboField;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);

		createEditorHistoryGroup(composite);

		createSpace(composite);
		createAllowInplaceEditorPref(composite);
		createUseIPersistablePref(composite);
		createPromptWhenStillOpenPref(composite);
		createEditorReuseGroup(composite);
		// ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).setPreferenceVisibility(editorReuseGroup,
		// showMultipleEditorTabs);

		updateValidState();

		applyDialogFont(composite);

		setHelpContext(parent);

		return composite;
	}

	protected void setHelpContext(Composite parent) {
		// @issue the IDE subclasses this, but should provide its own help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);
	}

	protected void createSpace(Composite parent) {
		WorkbenchPreferencePage.createSpace(parent);
	}


	protected void createAllowInplaceEditorPref(Composite composite) {
		allowInplaceEditor = new Button(composite, SWT.CHECK);
		allowInplaceEditor.setText(WorkbenchMessages.WorkbenchPreference_allowInplaceEditingButton);
		allowInplaceEditor.setSelection(
				!getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE));
		setButtonLayoutData(allowInplaceEditor);
	}

	protected void createUseIPersistablePref(Composite composite) {
		useIPersistableEditor = new Button(composite, SWT.CHECK);
		useIPersistableEditor.setText(WorkbenchMessages.WorkbenchPreference_useIPersistableEditorButton);
		useIPersistableEditor
				.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS));
		setButtonLayoutData(useIPersistableEditor);
	}

	protected void createPromptWhenStillOpenPref(Composite composite) {
		promptWhenStillOpenEditor = new Button(composite, SWT.CHECK);
		promptWhenStillOpenEditor.setText(WorkbenchMessages.WorkbenchPreference_promptWhenStillOpenButton);
		promptWhenStillOpenEditor.setSelection(
				getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN));
		setButtonLayoutData(promptWhenStillOpenEditor);
	}

	protected void createAlignMultiPageEditorTabs(Composite parent) {
		Composite comboComposite = new Composite(parent, SWT.NONE);
		comboComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		comboComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		String name = IWorkbenchPreferenceConstants.ALIGN_MULTI_PAGE_EDITOR_TABS;
		String label = WorkbenchMessages.WorkbenchPreference_AlignMultiPageEditorTabs;
		String[][] namesAndValues = {
				{ Action.removeMnemonics(WorkbenchMessages.WorkbenchPreference_AlignMultiPageEditorTabs_Top),
						String.valueOf(SWT.TOP) },
				{ Action.removeMnemonics(WorkbenchMessages.WorkbenchPreference_AlignMultiPageEditorTabs_Bottom),
						String.valueOf(SWT.BOTTOM) } };
		multiPageEditorTabPositionComboField = new ComboFieldEditor(name, label, namesAndValues, comboComposite);
		multiPageEditorTabPositionComboField.setPreferenceStore(getAPIPreferenceStore());
		multiPageEditorTabPositionComboField.setPage(this);
		multiPageEditorTabPositionComboField.load();
	}

	protected Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		allowInplaceEditor.setSelection(
				!getAPIPreferenceStore().getDefaultBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE));
		useIPersistableEditor.setSelection(store.getDefaultBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS));
		promptWhenStillOpenEditor.setSelection(getAPIPreferenceStore()
				.getDefaultBoolean(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN));
		reuseEditors.setSelection(store.getDefaultBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		reuseEditorsThreshold.loadDefault();
		reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		recentFilesEditor.loadDefault();
		multiPageEditorTabPositionComboField.loadDefault();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		multiPageEditorTabPositionComboField.store();
		getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE,
				!allowInplaceEditor.getSelection());
		store.setValue(IPreferenceConstants.USE_IPERSISTABLE_EDITORS, useIPersistableEditor.getSelection());
		getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN,
				promptWhenStillOpenEditor.getSelection());

		// store the reuse editors setting
		store.setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, reuseEditors.getSelection());
		reuseEditorsThreshold.store();

		// store the recent files setting
		recentFilesEditor.store();

		PrefUtil.savePrefs();
		return super.performOk();
	}

	/**
	 * Returns preference store that belongs to the our plugin.
	 *
	 * @return the preference store for this plugin
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	protected IPreferenceStore getAPIPreferenceStore() {
		return PrefUtil.getAPIPreferenceStore();
	}

	protected void updateValidState() {
		if (!recentFilesEditor.isValid()) {
			setErrorMessage(recentFilesEditor.getErrorMessage());
			setValid(false);
		} else if (!reuseEditorsThreshold.isValid()) {
			setErrorMessage(reuseEditorsThreshold.getErrorMessage());
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	/**
	 * Create a composite that contains entry fields specifying editor reuse
	 * preferences.
	 */
	protected void createEditorReuseGroup(Composite composite) {
		editorReuseGroup = new Composite(composite, SWT.LEFT);
		editorReuseGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).spacing(0, 0).create());
		editorReuseGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		reuseEditors = new Button(editorReuseGroup, SWT.CHECK);
		reuseEditors.setText(WorkbenchMessages.WorkbenchPreference_reuseEditors);
		reuseEditors.setLayoutData(new GridData());

		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		reuseEditors.setSelection(store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		reuseEditors.addSelectionListener(widgetSelectedAdapter(e -> {
			reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
			reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		}));

		editorReuseIndentGroup = new Composite(editorReuseGroup, SWT.LEFT);
		editorReuseIndentGroup.setLayout(GridLayoutFactory.fillDefaults().create());
		editorReuseIndentGroup
				.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());

		editorReuseThresholdGroup = new Composite(editorReuseIndentGroup, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		editorReuseThresholdGroup.setLayout(layout);
		GridData gridData = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData.widthHint = 35;
		editorReuseThresholdGroup
				.setLayoutData(gridData);

		reuseEditorsThreshold = new IntegerFieldEditor(IPreferenceConstants.REUSE_EDITORS, "", //$NON-NLS-1$
				editorReuseThresholdGroup);
		reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).dispose();

		reuseEditorsThreshold.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		reuseEditorsThreshold.setPage(this);
		reuseEditorsThreshold.setTextLimit(2);
		reuseEditorsThreshold.setErrorMessage(WorkbenchMessages.WorkbenchPreference_reuseEditorsThresholdError);
		reuseEditorsThreshold.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		reuseEditorsThreshold.setValidRange(1, 99);
		reuseEditorsThreshold.load();
		reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.setPropertyChangeListener(validityChangeListener);

		LabelFactory.newLabel(SWT.NONE).text(WorkbenchMessages.WorkbenchPreference_reuseEditors_closing)
				.create(editorReuseGroup);
	}

	/**
	 * Create a composite that contains entry fields specifying editor history
	 * preferences.
	 */
	protected void createEditorHistoryGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);

		recentFilesEditor = new IntegerFieldEditor(IPreferenceConstants.RECENT_FILES,
				WorkbenchMessages.WorkbenchPreference_recentFiles, groupComposite);

		recentFilesEditor.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		recentFilesEditor.setPage(this);
		recentFilesEditor.setTextLimit(Integer.toString(EditorHistory.MAX_SIZE).length());
		recentFilesEditor.setErrorMessage(NLS.bind(WorkbenchMessages.WorkbenchPreference_recentFilesError,
				Integer.valueOf(EditorHistory.MAX_SIZE)));
		recentFilesEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		recentFilesEditor.setValidRange(0, EditorHistory.MAX_SIZE);
		recentFilesEditor.load();
		recentFilesEditor.setPropertyChangeListener(validityChangeListener);
	}
}
