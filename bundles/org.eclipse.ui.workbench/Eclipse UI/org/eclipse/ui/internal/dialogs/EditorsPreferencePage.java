/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 97667 [Preferences] Pref Page General/Editors - problems
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorHistory;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The Editors preference page of the workbench.
 */
public class EditorsPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    private static final int REUSE_INDENT = 10;

    protected Composite editorReuseGroup;

    private Button reuseEditors;

    protected Button showMultipleEditorTabs;

    protected Button useIPersistableEditor;

    private Composite editorReuseIndentGroup;

    private Composite editorReuseThresholdGroup;

    private IntegerFieldEditor reuseEditorsThreshold;

    private Group dirtyEditorReuseGroup;

    private Button openNewEditor;

    private Button promptToReuseEditor;

    private IntegerFieldEditor recentFilesEditor;

    private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
        }
    };

	private Button promptWhenStillOpenEditor;

	private Button allowInplaceEditor;

	protected Control createContents(Composite parent) {
        Composite composite = createComposite(parent);

        createEditorHistoryGroup(composite);

        createSpace(composite);
        createShowMultipleEditorTabsPref(composite);
        createAllowInplaceEditorPref(composite);
        createUseIPersistablePref(composite);
        createPromptWhenStillOpenPref(composite);
		createEditorReuseGroup(composite);
		((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).setPreferenceVisibility(editorReuseGroup, showMultipleEditorTabs);

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

    protected void createShowMultipleEditorTabsPref(Composite composite) {
        showMultipleEditorTabs = new Button(composite, SWT.CHECK);
        showMultipleEditorTabs.setText(WorkbenchMessages.WorkbenchPreference_showMultipleEditorTabsButton);
        showMultipleEditorTabs.setSelection(getPreferenceStore().getBoolean(
                IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        setButtonLayoutData(showMultipleEditorTabs);
    }

    protected void createAllowInplaceEditorPref(Composite composite) {
    	allowInplaceEditor = new Button(composite, SWT.CHECK);
    	allowInplaceEditor.setText(WorkbenchMessages.WorkbenchPreference_allowInplaceEditingButton);
    	allowInplaceEditor.setSelection(!getAPIPreferenceStore().getBoolean(
    			IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE));
    	setButtonLayoutData(allowInplaceEditor);
    }
    
    protected void createUseIPersistablePref(Composite composite) {
        useIPersistableEditor = new Button(composite, SWT.CHECK);
        useIPersistableEditor.setText(WorkbenchMessages.WorkbenchPreference_useIPersistableEditorButton);
        useIPersistableEditor.setSelection(getPreferenceStore().getBoolean(
                IPreferenceConstants.USE_IPERSISTABLE_EDITORS));
        setButtonLayoutData(useIPersistableEditor);
    }
    
    protected void createPromptWhenStillOpenPref(Composite composite) {
    	promptWhenStillOpenEditor = new Button(composite, SWT.CHECK);
    	promptWhenStillOpenEditor.setText(WorkbenchMessages.WorkbenchPreference_promptWhenStillOpenButton);
    	promptWhenStillOpenEditor.setSelection(getAPIPreferenceStore().getBoolean(
    			IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN));
    	setButtonLayoutData(promptWhenStillOpenEditor);
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

    public void init(IWorkbench workbench) {
        // do nothing
    }

    protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        showMultipleEditorTabs
				.setSelection(store
						.getDefaultBoolean(IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        allowInplaceEditor
        		.setSelection(!getAPIPreferenceStore()
        				.getDefaultBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE));
		useIPersistableEditor
				.setSelection(store
						.getDefaultBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS));
		promptWhenStillOpenEditor
				.setSelection(getAPIPreferenceStore()
						.getDefaultBoolean(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN));
        reuseEditors.setSelection(store
                .getDefaultBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
        dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());
        openNewEditor.setSelection(!store
                .getDefaultBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
        openNewEditor.setEnabled(reuseEditors.getSelection());
        promptToReuseEditor.setSelection(store
                .getDefaultBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
        promptToReuseEditor.setEnabled(reuseEditors.getSelection());
        reuseEditorsThreshold.loadDefault();
        reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup)
                .setEnabled(reuseEditors.getSelection());
        reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup)
                .setEnabled(reuseEditors.getSelection());
        recentFilesEditor.loadDefault();
    }

    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS,
                showMultipleEditorTabs.getSelection());
        getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE,
        		!allowInplaceEditor.getSelection());
        store.setValue(IPreferenceConstants.USE_IPERSISTABLE_EDITORS,
                useIPersistableEditor.getSelection());
        getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN,
        		promptWhenStillOpenEditor.getSelection());
        
        // store the reuse editors setting
        store.setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, reuseEditors
                .getSelection());
        store.setValue(IPreferenceConstants.REUSE_DIRTY_EDITORS,
                promptToReuseEditor.getSelection());
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
     * Create a composite that contains entry fields specifying editor reuse preferences.
     */
    protected void createEditorReuseGroup(Composite composite) {
        editorReuseGroup = new Composite(composite, SWT.LEFT);
        GridLayout layout = new GridLayout();
        // Line up with other entries in preference page
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        editorReuseGroup.setLayout(layout);
        editorReuseGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        reuseEditors = new Button(editorReuseGroup, SWT.CHECK);
        reuseEditors.setText(WorkbenchMessages.WorkbenchPreference_reuseEditors);
        reuseEditors.setLayoutData(new GridData());

        IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
        reuseEditors.setSelection(store
                .getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
        reuseEditors.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                reuseEditorsThreshold
                        .getLabelControl(editorReuseThresholdGroup).setEnabled(
                                reuseEditors.getSelection());
                reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup)
                        .setEnabled(reuseEditors.getSelection());
                dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());
                openNewEditor.setEnabled(reuseEditors.getSelection());
                promptToReuseEditor.setEnabled(reuseEditors.getSelection());
            }
        });

        editorReuseIndentGroup = new Composite(editorReuseGroup, SWT.LEFT);
        GridLayout indentLayout = new GridLayout();
        indentLayout.marginLeft = REUSE_INDENT;
        indentLayout.marginRight = 0;
        editorReuseIndentGroup.setLayout(indentLayout);
        editorReuseIndentGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        editorReuseThresholdGroup = new Composite(editorReuseIndentGroup,
                SWT.LEFT);
        layout = new GridLayout();
        layout.marginWidth = 0;
        editorReuseThresholdGroup.setLayout(layout);
        editorReuseThresholdGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        reuseEditorsThreshold = new IntegerFieldEditor(
                IPreferenceConstants.REUSE_EDITORS,
                WorkbenchMessages.WorkbenchPreference_reuseEditorsThreshold, editorReuseThresholdGroup); 

        reuseEditorsThreshold.setPreferenceStore(WorkbenchPlugin.getDefault()
                .getPreferenceStore());
        reuseEditorsThreshold.setPage(this);
        reuseEditorsThreshold.setTextLimit(2);
        reuseEditorsThreshold.setErrorMessage(WorkbenchMessages.WorkbenchPreference_reuseEditorsThresholdError); 
        reuseEditorsThreshold
                .setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        reuseEditorsThreshold.setValidRange(1, 99);
        reuseEditorsThreshold.load();
        reuseEditorsThreshold.getLabelControl(editorReuseThresholdGroup)
                .setEnabled(reuseEditors.getSelection());
        reuseEditorsThreshold.getTextControl(editorReuseThresholdGroup)
                .setEnabled(reuseEditors.getSelection());
        reuseEditorsThreshold.setPropertyChangeListener(validityChangeListener);

        dirtyEditorReuseGroup = new Group(editorReuseIndentGroup, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        dirtyEditorReuseGroup.setLayout(layout);
        dirtyEditorReuseGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dirtyEditorReuseGroup.setText(WorkbenchMessages.WorkbenchPreference_reuseDirtyEditorGroupTitle); 
        dirtyEditorReuseGroup.setEnabled(reuseEditors.getSelection());

        promptToReuseEditor = new Button(dirtyEditorReuseGroup, SWT.RADIO);
        promptToReuseEditor.setText(WorkbenchMessages.WorkbenchPreference_promptToReuseEditor); 
        promptToReuseEditor.setSelection(store
                .getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
        promptToReuseEditor.setEnabled(reuseEditors.getSelection());

        openNewEditor = new Button(dirtyEditorReuseGroup, SWT.RADIO);
        openNewEditor.setText(WorkbenchMessages.WorkbenchPreference_openNewEditor); 
        openNewEditor.setSelection(!store
                .getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS));
        openNewEditor.setEnabled(reuseEditors.getSelection());
    }

    /**
     * Create a composite that contains entry fields specifying editor history preferences.
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
        
        recentFilesEditor = new IntegerFieldEditor(
                IPreferenceConstants.RECENT_FILES,
                WorkbenchMessages.WorkbenchPreference_recentFiles, groupComposite); 

        recentFilesEditor.setPreferenceStore(WorkbenchPlugin.getDefault()
                .getPreferenceStore());
        recentFilesEditor.setPage(this);
        recentFilesEditor.setTextLimit(Integer.toString(EditorHistory.MAX_SIZE)
                .length());
        recentFilesEditor
                .setErrorMessage(NLS.bind(WorkbenchMessages.WorkbenchPreference_recentFilesError, new Integer(EditorHistory.MAX_SIZE) )); 
        recentFilesEditor
                .setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        recentFilesEditor.setValidRange(0, EditorHistory.MAX_SIZE);
        recentFilesEditor.load();
        recentFilesEditor.setPropertyChangeListener(validityChangeListener);
    }
}

