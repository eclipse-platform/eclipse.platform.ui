package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;

public class WorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench workbench;
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button refreshButton;
	private Button showTasks;
	private IntegerFieldEditor saveInterval;

	private Button doubleClickButton;
	private Button singleClickButton;
	private Button selectOnHoverButton;
	private Button openAfterDelayButton;
	private boolean openOnSingleClick;
	private boolean selectOnHover;
	private boolean openAfterDelay;

	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		// GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}
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
		autoBuildButton.setText(WorkbenchMessages.getString("WorkbenchPreference.autobuild")); //$NON-NLS-1$
		autoBuildButton.setFont(font);

		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setText(WorkbenchMessages.getString("WorkbenchPreference.savePriorToBuilding")); //$NON-NLS-1$
		autoSaveAllButton.setFont(font);

		refreshButton = new Button(composite, SWT.CHECK);
		refreshButton.setText(WorkbenchMessages.getString("WorkbenchPreference.refreshButton")); //$NON-NLS-1$
		refreshButton.setFont(font);
		
		showTasks = new Button(composite, SWT.CHECK);
		showTasks.setText(WorkbenchMessages.getString("WorkbenchPreference.showTasks")); //$NON-NLS-1$
		showTasks.setFont(font);

		createSpace(composite);
		createSaveIntervalGroup(composite);
		
		createSpace(composite);
		createSingleClickGroup(composite);

		// set initial values
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		refreshButton.setSelection(store.getBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));
		showTasks.setSelection(store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD));
		
		return composite;
	}
	
	private void createSingleClickGroup(Composite composite) {
		
		Font font = composite.getFont();
		
		Group buttonComposite = new Group(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(WorkbenchMessages.getString("WorkbenchPreference.openMode")); //$NON-NLS-1$
		buttonComposite.setFont(font);
		

		String label = WorkbenchMessages.getString("WorkbenchPreference.doubleClick"); //$NON-NLS-1$	
		doubleClickButton = createRadioButton(buttonComposite,label);
		doubleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		doubleClickButton.setSelection(!openOnSingleClick);

		label = WorkbenchMessages.getString("WorkbenchPreference.singleClick"); //$NON-NLS-1$
		singleClickButton = createRadioButton(buttonComposite,label);
		singleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		singleClickButton.setSelection(openOnSingleClick);
		
		label = WorkbenchMessages.getString("WorkbenchPreference.singleClick_SelectOnHover"); //$NON-NLS-1$				
		selectOnHoverButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		selectOnHoverButton.setText(label);
		selectOnHoverButton.setFont(font);
		selectOnHoverButton.setEnabled(openOnSingleClick);
		selectOnHoverButton.setSelection(selectOnHover);
		selectOnHoverButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectOnHover = selectOnHoverButton.getSelection();
			}
		});
		data = new GridData();
		data.horizontalIndent = 20;
		selectOnHoverButton.setLayoutData(data);
		
		
		label = WorkbenchMessages.getString("WorkbenchPreference.singleClick_OpenAfterDelay"); //$NON-NLS-1$				
		openAfterDelayButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		openAfterDelayButton.setText(label);
		openAfterDelayButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setSelection(openAfterDelay);
		openAfterDelayButton.setFont(font);
		openAfterDelayButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openAfterDelay = openAfterDelayButton.getSelection();
			}
		});		
		data = new GridData();
		data.horizontalIndent = 20;
		openAfterDelayButton.setLayoutData(data);
		
		createNoteComposite(
			font,
			buttonComposite,
			WorkbenchMessages.getString("Preference.note"),//$NON-NLS-1$
			WorkbenchMessages.getString("WorkbenchPreference.noEffectOnAllViews")); //$NON-NLS-1$
	}
	
	private void selectClickMode(boolean singleClick) {
		openOnSingleClick = singleClick;
		selectOnHoverButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setEnabled(openOnSingleClick);
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
		
		saveInterval = new IntegerFieldEditor(IPreferenceConstants.SAVE_INTERVAL, WorkbenchMessages.getString("WorkbenchPreference.saveInterval"), groupComposite); //$NON-NLS-1$

		saveInterval.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		saveInterval.setPreferencePage(this);
		saveInterval.setTextLimit(Integer.toString(IPreferenceConstants.MAX_SAVE_INTERVAL).length());
		saveInterval.setErrorMessage(WorkbenchMessages.format("WorkbenchPreference.saveIntervalError", new Object[] { new Integer(IPreferenceConstants.MAX_SAVE_INTERVAL)})); //$NON-NLS-1$
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
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		workbench = aWorkbench;
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		openOnSingleClick = store.getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK); //$NON-NLS-1$
		selectOnHover = store.getBoolean(IPreferenceConstants.SELECT_ON_HOVER); //$NON-NLS-1$
		openAfterDelay = store.getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY); //$NON-NLS-1$
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.AUTO_BUILD));
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		refreshButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));
		showTasks.setSelection(store.getBoolean(IPreferenceConstants.SHOW_TASKS_ON_BUILD));
		saveInterval.loadDefault();
		
		openOnSingleClick = store.getDefaultBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK); //$NON-NLS-1$
		selectOnHover = store.getDefaultBoolean(IPreferenceConstants.SELECT_ON_HOVER); //$NON-NLS-1$
		openAfterDelay = store.getDefaultBoolean(IPreferenceConstants.OPEN_AFTER_DELAY); //$NON-NLS-1$
		singleClickButton.setSelection(openOnSingleClick);
		doubleClickButton.setSelection(!openOnSingleClick);
		selectOnHoverButton.setSelection(selectOnHover);
		openAfterDelayButton.setSelection(openAfterDelay);
		selectOnHoverButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setEnabled(openOnSingleClick);		
		
		super.performDefaults();
	}
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		// inform the workbench of whether it should do autobuilds or not
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		boolean coreAutoBuildSetting = description.isAutoBuilding();
		boolean preferenceStoreCurrentSetting = store.getBoolean(IPreferenceConstants.AUTO_BUILD);
		boolean newAutoBuildSetting = autoBuildButton.getSelection();
		
		//As older versions of Eclipse did not use the preference store for this
		//setting it is possible that the setting in Core will be false 
		//and the preference store will have the default value of true.
		//Do a second setValue if required here to synch them up
		if(preferenceStoreCurrentSetting && !coreAutoBuildSetting)		
			store.setValue(IPreferenceConstants.AUTO_BUILD, coreAutoBuildSetting);
		
		// store the auto build in the preference store so that we can enable import and export
		store.setValue(IPreferenceConstants.AUTO_BUILD, newAutoBuildSetting);

		// store the save all prior to build setting
		store.setValue(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, autoSaveAllButton.getSelection());

		// store the link navigator to editor setting
		store.setValue(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP, refreshButton.getSelection());

		//store the preference for bringing task view to front on build
		store.setValue(IPreferenceConstants.SHOW_TASKS_ON_BUILD, showTasks.getSelection());

		long oldSaveInterval = description.getSnapshotInterval() / 60000;
		long newSaveInterval = new Long(saveInterval.getStringValue()).longValue();
		if(oldSaveInterval != newSaveInterval) {
			try {
				description.setSnapshotInterval(newSaveInterval * 60000);
				ResourcesPlugin.getWorkspace().setDescription(description);
				store.firePropertyChangeEvent(IPreferenceConstants.SAVE_INTERVAL, new Integer((int)oldSaveInterval), new Integer((int)newSaveInterval));
			} catch (CoreException e) {
				WorkbenchPlugin.log("Error changing save interval preference", e.getStatus()); //$NON-NLS-1$
			}
		}

		store.setValue(IPreferenceConstants.OPEN_ON_SINGLE_CLICK,openOnSingleClick); //$NON-NLS-1$
		store.setValue(IPreferenceConstants.SELECT_ON_HOVER,selectOnHover); //$NON-NLS-1$
		store.setValue(IPreferenceConstants.OPEN_AFTER_DELAY,openAfterDelay); //$NON-NLS-1$
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK : OpenStrategy.DOUBLE_CLICK;
		if(openOnSingleClick) {
			if(selectOnHover)
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			if(openAfterDelay)
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
		}
		OpenStrategy.setOpenMethod(singleClickMethod);

		WorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}
}