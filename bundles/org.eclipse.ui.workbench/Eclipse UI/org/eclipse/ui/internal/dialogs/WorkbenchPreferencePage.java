/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tristan Hume - <trishume@gmail.com> -
 *     		Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 *     		Implemented workbench auto-save to correctly restore state in case of crash.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Generic workbench main preference page.
 */
public class WorkbenchPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    private Button stickyCycleButton;

    private Button doubleClickButton;

    private Button singleClickButton;

    private Button selectOnHoverButton;

    private Button openAfterDelayButton;

    private Button showUserDialogButton;

	private IntegerFieldEditor saveInterval;

    private boolean openOnSingleClick;

    private boolean selectOnHover;

    private boolean openAfterDelay;

	private Button showHeapStatusButton;

	protected static int MAX_SAVE_INTERVAL = 9999;

    @Override
	protected Control createContents(Composite parent) {

        // @issue if the product subclasses this page, then it should provide
        // the help content
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.WORKBENCH_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

		createSettings(composite);

        createSpace(composite);
        createOpenModeGroup(composite);

        applyDialogFont(composite);

        return composite;
    }

    /**
     * Create the buttons at the top of the preference page.
     * @param composite
     */
	protected void createSettings(Composite composite) {
		createShowUserDialogPref(composite);
        createStickyCyclePref(composite);
        createHeapStatusPref(composite);
		createSaveIntervalGroup(composite);
	}

    /**
     * Create the widget for the user dialog preference.
     *
     * @param composite
     */
    protected void createShowUserDialogPref(Composite composite) {
        showUserDialogButton = new Button(composite, SWT.CHECK);
        showUserDialogButton.setText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundButton);
        showUserDialogButton.setToolTipText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundToolTip);
        showUserDialogButton.setSelection(WorkbenchPlugin.getDefault()
                .getPreferenceStore().getBoolean(
                        IPreferenceConstants.RUN_IN_BACKGROUND));
    }

    /**
     * Create the widget for the heap status preference.
     *
     * @param composite
     */
    protected void createHeapStatusPref(Composite composite) {
        showHeapStatusButton = new Button(composite, SWT.CHECK);
        showHeapStatusButton.setText(WorkbenchMessages.WorkbenchPreference_HeapStatusButton);
        showHeapStatusButton.setToolTipText(WorkbenchMessages.WorkbenchPreference_HeapStatusButtonToolTip);

        showHeapStatusButton.setSelection(PrefUtil.getAPIPreferenceStore().getBoolean(
                        IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR));
    }

    /**
     * Creates the composite which will contain all the preference controls for
     * this page.
     *
     * @param parent
     *            the parent composite
     * @return the composite for this page
     */
    protected Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        return composite;
    }

    protected void createStickyCyclePref(Composite composite) {
        stickyCycleButton = new Button(composite, SWT.CHECK);
        stickyCycleButton.setText(WorkbenchMessages.WorkbenchPreference_stickyCycleButton);
        stickyCycleButton.setSelection(getPreferenceStore().getBoolean(
                IPreferenceConstants.STICKY_CYCLE));
    }

	/**
	 * Create a composite that contains entry fields specifying save interval
	 * preference.
	 *
	 * @param composite
	 *            the Composite the group is created in.
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

		saveInterval = new IntegerFieldEditor(IPreferenceConstants.WORKBENCH_SAVE_INTERVAL,
				WorkbenchMessages.WorkbenchPreference_workbenchSaveInterval, groupComposite);

		// @issue we should drop our preference constant and let clients use
		// core's pref. ours is not up-to-date anyway if someone changes this
		// interval directly thru core api.
		saveInterval.setPreferenceStore(getPreferenceStore());
		saveInterval.setPage(this);
		saveInterval.setTextLimit(Integer.toString(MAX_SAVE_INTERVAL).length());
		saveInterval.setErrorMessage(NLS.bind(
				WorkbenchMessages.WorkbenchPreference_workbenchSaveIntervalError, new Integer(
						MAX_SAVE_INTERVAL)));
		saveInterval.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		saveInterval.setValidRange(0, MAX_SAVE_INTERVAL);

		saveInterval.load();

		saveInterval.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) {
					setValid(saveInterval.isValid());
				}
			}
		});

	}

    protected void createOpenModeGroup(Composite composite) {

        Font font = composite.getFont();

        Group buttonComposite = new Group(composite, SWT.LEFT);
        GridLayout layout = new GridLayout();
        buttonComposite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        buttonComposite.setLayoutData(data);
        buttonComposite.setText(WorkbenchMessages.WorkbenchPreference_openMode);

        String label = WorkbenchMessages.WorkbenchPreference_doubleClick;
        doubleClickButton = createRadioButton(buttonComposite, label);
        doubleClickButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                selectClickMode(singleClickButton.getSelection());
            }
        });
        doubleClickButton.setSelection(!openOnSingleClick);

        label = WorkbenchMessages.WorkbenchPreference_singleClick;
        singleClickButton = createRadioButton(buttonComposite, label);
        singleClickButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                selectClickMode(singleClickButton.getSelection());
            }
        });
        singleClickButton.setSelection(openOnSingleClick);

        label = WorkbenchMessages.WorkbenchPreference_singleClick_SelectOnHover;
        selectOnHoverButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
        selectOnHoverButton.setText(label);
        selectOnHoverButton.setEnabled(openOnSingleClick);
        selectOnHoverButton.setSelection(selectOnHover);
        selectOnHoverButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                selectOnHover = selectOnHoverButton.getSelection();
            }
        });
        data = new GridData();
		data.horizontalIndent = LayoutConstants.getIndent();
        selectOnHoverButton.setLayoutData(data);

        label = WorkbenchMessages.WorkbenchPreference_singleClick_OpenAfterDelay;
        openAfterDelayButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
        openAfterDelayButton.setText(label);
        openAfterDelayButton.setEnabled(openOnSingleClick);
        openAfterDelayButton.setSelection(openAfterDelay);
        openAfterDelayButton.addSelectionListener(new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                openAfterDelay = openAfterDelayButton.getSelection();
            }
        });
        data = new GridData();
		data.horizontalIndent = LayoutConstants.getIndent();
        openAfterDelayButton.setLayoutData(data);

        createNoteComposite(font, buttonComposite, WorkbenchMessages.Preference_note,
                WorkbenchMessages.WorkbenchPreference_noEffectOnAllViews);
    }

    private void selectClickMode(boolean singleClick) {
        openOnSingleClick = singleClick;
        selectOnHoverButton.setEnabled(openOnSingleClick);
        openAfterDelayButton.setEnabled(openOnSingleClick);
    }

    /**
     * Utility method that creates a radio button instance and sets the default
     * layout data.
     *
     * @param parent
     *            the parent for the new button
     * @param label
     *            the label for the new button
     * @return the newly-created button
     */
    protected static Button createRadioButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
        button.setText(label);
        return button;
    }

    /**
     * Utility method that creates a combo box
     *
     * @param parent
     *            the parent for the new label
     * @return the new widget
     */
    protected static Combo createCombo(Composite parent) {
        Combo combo = new Combo(parent, SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        combo.setLayoutData(data);
        return combo;
    }

    /**
     * Utility method that creates a label instance and sets the default layout
     * data.
     *
     * @param parent
     *            the parent for the new label
     * @param text
     *            the text for the new label
     * @return the new label
     */
    protected static Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        return label;
    }

    /**
     * Creates a tab of one horizontal spans.
     *
     * @param parent
     *            the parent in which the tab should be created
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
    @Override
	protected IPreferenceStore doGetPreferenceStore() {
        return WorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /**
     * @see IWorkbenchPreferencePage
     */
    @Override
	public void init(IWorkbench aWorkbench) {
        IPreferenceStore store = getPreferenceStore();
        openOnSingleClick = store
                .getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK);
        selectOnHover = store.getBoolean(IPreferenceConstants.SELECT_ON_HOVER);
        openAfterDelay = store
                .getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY);
    }

    /**
     * The default button has been pressed.
     */
    @Override
	protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
		saveInterval.loadDefault();
        stickyCycleButton.setSelection(store
                .getBoolean(IPreferenceConstants.STICKY_CYCLE));
        openOnSingleClick = store
                .getDefaultBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK);
        selectOnHover = store
                .getDefaultBoolean(IPreferenceConstants.SELECT_ON_HOVER);
        openAfterDelay = store
                .getDefaultBoolean(IPreferenceConstants.OPEN_AFTER_DELAY);
        singleClickButton.setSelection(openOnSingleClick);
        doubleClickButton.setSelection(!openOnSingleClick);
        selectOnHoverButton.setSelection(selectOnHover);
        openAfterDelayButton.setSelection(openAfterDelay);
        selectOnHoverButton.setEnabled(openOnSingleClick);
        openAfterDelayButton.setEnabled(openOnSingleClick);
        stickyCycleButton.setSelection(store
                .getDefaultBoolean(IPreferenceConstants.STICKY_CYCLE));
        showUserDialogButton.setSelection(store.getDefaultBoolean(
                IPreferenceConstants.RUN_IN_BACKGROUND));
        showHeapStatusButton.setSelection(PrefUtil.getAPIPreferenceStore().getDefaultBoolean(
                IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR));

        super.performDefaults();
    }

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    @Override
	public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();

        // store the keep cycle part dialogs sticky preference
        store.setValue(IPreferenceConstants.STICKY_CYCLE, stickyCycleButton
                .getSelection());
        store.setValue(IPreferenceConstants.OPEN_ON_SINGLE_CLICK,
                openOnSingleClick);
        store.setValue(IPreferenceConstants.SELECT_ON_HOVER, selectOnHover);
        store.setValue(IPreferenceConstants.OPEN_AFTER_DELAY, openAfterDelay);
        store.setValue(IPreferenceConstants.RUN_IN_BACKGROUND,
                showUserDialogButton.getSelection());
		store.setValue(IPreferenceConstants.WORKBENCH_SAVE_INTERVAL, saveInterval.getIntValue());
        PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, showHeapStatusButton.getSelection());
        updateHeapStatus(showHeapStatusButton.getSelection());

        int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK
                : OpenStrategy.DOUBLE_CLICK;
        if (openOnSingleClick) {
            if (selectOnHover) {
                singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
            }
            if (openAfterDelay) {
                singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
            }
        }
        OpenStrategy.setOpenMethod(singleClickMethod);

        PrefUtil.savePrefs();
        return true;
    }

	/**
	 * Show or hide the heap status based on selection.
	 * @param selection
	 */
	private void updateHeapStatus(boolean selection) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchWindow window = windows[i];
			if(window instanceof WorkbenchWindow){
				((WorkbenchWindow) window).showHeapStatus(selection);
			}
		}

	}
}
