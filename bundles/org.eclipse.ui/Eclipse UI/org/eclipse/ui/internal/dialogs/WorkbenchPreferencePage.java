package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
 
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;

public class WorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench workbench;
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button linkButton;
	private Button refreshButton;

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

		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_PREFERENCE_PAGE);

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		autoBuildButton = new Button(composite, SWT.CHECK);
		autoBuildButton.setText(WorkbenchMessages.getString("WorkbenchPreference.autobuild")); //$NON-NLS-1$

		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setText(WorkbenchMessages.getString("WorkbenchPreference.savePriorToBuilding")); //$NON-NLS-1$

		linkButton = new Button(composite, SWT.CHECK);
		linkButton.setText(WorkbenchMessages.getString("WorkbenchPreference.linkNavigator")); //$NON-NLS-1$

		refreshButton = new Button(composite, SWT.CHECK);
		refreshButton.setText(WorkbenchMessages.getString("WorkbenchPreference.refreshButton")); //$NON-NLS-1$

		createSpace(composite);
		createSingleClickGroup(composite);

		// set initial values
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		linkButton.setSelection(store.getBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));
		refreshButton.setSelection(store.getBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));

		return composite;
	}
	
	private void createSingleClickGroup(Composite composite) {
		
		Group buttonComposite = new Group(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(WorkbenchMessages.getString("WorkInProgressPreference.openMode")); //$NON-NLS-1$
		

		String label = WorkbenchMessages.getString("WorkInProgressPreference.doubleClick"); //$NON-NLS-1$	
		doubleClickButton = createRadioButton(buttonComposite,label);
		doubleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		doubleClickButton.setSelection(!openOnSingleClick);

		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick"); //$NON-NLS-1$
		singleClickButton = createRadioButton(buttonComposite,label);
		singleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		singleClickButton.setSelection(openOnSingleClick);
		
		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick_SelectOnHover"); //$NON-NLS-1$				
		selectOnHoverButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		selectOnHoverButton.setText(label);
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
		
		
		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick_OpenAfterDelay"); //$NON-NLS-1$				
		openAfterDelayButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		openAfterDelayButton.setText(label);
		openAfterDelayButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setSelection(openAfterDelay);
		openAfterDelayButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openAfterDelay = openAfterDelayButton.getSelection();
			}
		});		
		data = new GridData();
		data.horizontalIndent = 20;
		openAfterDelayButton.setLayoutData(data);
		
		Label note = new Label(buttonComposite, SWT.NONE);
		note.setText(WorkbenchMessages.getString("WorkInProgressPreference.noEffectOnAllViews")); //$NON-NLS-1$
	}
	
	private void selectClickMode(boolean singleClick) {
		openOnSingleClick = singleClick;
		selectOnHoverButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setEnabled(openOnSingleClick);
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
		GridData data = new GridData();
		button.setLayoutData(data);
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
		linkButton.setSelection(store.getDefaultBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));
		refreshButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP));
		
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
		boolean newAutoBuildSetting = autoBuildButton.getSelection();
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		boolean oldAutoBuildSetting = description.isAutoBuilding();
		description.setAutoBuilding(newAutoBuildSetting);
		try {
			ResourcesPlugin.getWorkspace().setDescription(description);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Error changing autobuild pref", e.getStatus());
		}
		if (oldAutoBuildSetting != newAutoBuildSetting) {
			// fire off a property change notification so interested
			// parties can know about the auto build setting change
			// since it is not kept in the preference store.
			store.firePropertyChangeEvent(IPreferenceConstants.AUTO_BUILD, new Boolean(oldAutoBuildSetting), new Boolean(newAutoBuildSetting));

			// If auto build is turned on, then do a global incremental
			// build on all the projects.
			if (newAutoBuildSetting) {
				GlobalBuildAction action = new GlobalBuildAction(this.workbench, getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
				action.doBuild();
			}
		}

		// store the save all prior to build setting
		store.setValue(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, autoSaveAllButton.getSelection());

		// store the link navigator to editor setting
		store.setValue(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, linkButton.getSelection());

		// store the link navigator to editor setting
		store.setValue(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP, refreshButton.getSelection());

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