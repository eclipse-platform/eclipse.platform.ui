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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
 
import org.eclipse.core.resources.*;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;

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
	private Combo accelConfigCombo;
	private IntegerFieldEditor reuseEditors;
	private IntegerFieldEditor recentFilesEditor;

	//Widgets for perspective switching when creating new projects
	private Button openProjectInNewWindowButton;
	private Button openProjectInNewPageButton;
	private Button replaceProjectButton;
	private Button switchOnNewProjectButton;
	
	// hashtable mapping accelerator configuration names to accelerator configuration
	private Hashtable namesToConfiguration;
	// the name of the active accelerator configuration
	private String activeAcceleratorConfigurationName;

	private String newProjectPerspectiveSetting;

	//Labels
	private static final String NEW_PROJECT_PERSPECTIVE_TITLE = WorkbenchMessages.getString("WorkbenchPreference.projectOptionsTitle"); //$NON-NLS-1$

	private static final String OPEN_NEW_WINDOW_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.projectNewWindow"); //$NON-NLS-1$
	private static final String OPEN_NEW_PAGE_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.projectSameWindow"); //$NON-NLS-1$
	private static final String OPEN_REPLACE_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.replacePerspective"); //$NON-NLS-1$
	private static final String DO_NOT_SWITCH_PERSPECTIVES = WorkbenchMessages.getString("WorkbenchPreference.noSwitch"); //$NON-NLS-1$

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
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		autoBuildButton = new Button(composite, SWT.CHECK);
		autoBuildButton.setText(WorkbenchMessages.getString("WorkbenchPreference.autobuild")); //$NON-NLS-1$

		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setText(WorkbenchMessages.getString("WorkbenchPreference.savePriorToBuilding")); //$NON-NLS-1$

		linkButton = new Button(composite, SWT.CHECK);
		linkButton.setText(WorkbenchMessages.getString("WorkbenchPreference.linkNavigator")); //$NON-NLS-1$

		createEditorGroup(composite);
		
		createSpace(composite);
		createProjectPerspectiveGroup(composite);
		
		createSpace(composite);
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration"));

		// set initial values
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		linkButton.setSelection(store.getBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));

		return composite;
	}
	/**
	 * Creates a composite that contains a label and combo box specifying the active
	 * accelerator configuration.
	 */
	private void createAcceleratorConfigurationGroup(Composite composite, String label) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);
		
		Label configLabel = createLabel(groupComposite, label);
		accelConfigCombo = createCombo(groupComposite);

		if(namesToConfiguration.size() > 0) { 
			String[] comboItems = new String[namesToConfiguration.size()];
			namesToConfiguration.keySet().toArray(comboItems);
			Arrays.sort(comboItems,Collator.getInstance());
			accelConfigCombo.setItems(comboItems);
		
			if(activeAcceleratorConfigurationName != null)
				accelConfigCombo.select(accelConfigCombo.indexOf(activeAcceleratorConfigurationName));
		} else {
			accelConfigCombo.setEnabled(false);
		}	
	}
	/**
	 * Create a composite that contains entry fields specifying editor preferences.
	 */
	private void createEditorGroup(Composite composite) {

		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);

		reuseEditors = new IntegerFieldEditor(IPreferenceConstants.REUSE_EDITORS, WorkbenchMessages.getString("WorkbenchPreference.reuseEditors"), groupComposite);

		reuseEditors.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		reuseEditors.setPreferencePage(this);
		reuseEditors.setTextLimit(2);
		reuseEditors.setErrorMessage(WorkbenchMessages.getString("WorkbenchPreference.reuseEditorsError"));
		reuseEditors.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		reuseEditors.setValidRange(1, 99);
		reuseEditors.load();

		recentFilesEditor = new IntegerFieldEditor(IPreferenceConstants.RECENT_FILES, WorkbenchMessages.getString("WorkbenchPreference.recentFiles"), groupComposite);

		int recentFilesMax = IPreferenceConstants.MAX_RECENT_FILES_SIZE;
		recentFilesEditor.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		recentFilesEditor.setPreferencePage(this);
		recentFilesEditor.setTextLimit(Integer.toString(recentFilesMax).length());
		recentFilesEditor.setErrorMessage(WorkbenchMessages.format("WorkbenchPreference.recentFilesError", new Object[] { new Integer(recentFilesMax)}));
		recentFilesEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		recentFilesEditor.setValidRange(0, recentFilesMax);
		recentFilesEditor.load();
	}
	/**
	 * Create a composite that contains buttons for selecting the 
	 * preference opening new project selections. 
	 */
	private void createProjectPerspectiveGroup(Composite composite) {

		Group buttonComposite = new Group(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(NEW_PROJECT_PERSPECTIVE_TITLE);

		//Open New Page button
		this.openProjectInNewPageButton = createRadioButton(buttonComposite, OPEN_NEW_PAGE_PROJECT_LABEL);
		this.openProjectInNewPageButton.setSelection(this.newProjectPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));

		this.openProjectInNewPageButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
			}
		});

		//Open New Window button
		this.openProjectInNewWindowButton = createRadioButton(buttonComposite, OPEN_NEW_WINDOW_PROJECT_LABEL);
		this.openProjectInNewWindowButton.setSelection(this.newProjectPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));

		this.openProjectInNewWindowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
			}
		});

		//Replace button
		this.replaceProjectButton = createRadioButton(buttonComposite, OPEN_REPLACE_PROJECT_LABEL);
		this.replaceProjectButton.setSelection(this.newProjectPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));

		this.replaceProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
			}
		});

		//No switch button
		this.switchOnNewProjectButton = createRadioButton(buttonComposite, DO_NOT_SWITCH_PERSPECTIVES);
		this.switchOnNewProjectButton.setSelection(this.newProjectPerspectiveSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE));

		this.switchOnNewProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
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
	private Button createRadioButton(Composite parent, String label) {
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
	private Combo createCombo(Composite parent) {
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
	 * Creates a tab of one horizontal spans.
	 *
	 * @param parent  the parent in which the tab should be created
	 */
	private void createSpace(Composite parent) {
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
		this.workbench = aWorkbench;
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		this.newProjectPerspectiveSetting = store.getString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);

		namesToConfiguration = new Hashtable();
		WorkbenchPlugin plugin = WorkbenchPlugin.getDefault();
		AcceleratorRegistry registry = plugin.getAcceleratorRegistry();
		AcceleratorConfiguration configs[] = registry.getConfigsWithSets();
		for (int i = 0; i < configs.length; i++)
			namesToConfiguration.put(configs[i].getName(), configs[i]);	

		AcceleratorConfiguration config = ((Workbench)aWorkbench).getActiveAcceleratorConfiguration();
		if(config != null)
			activeAcceleratorConfigurationName = config.getName();
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		linkButton.setSelection(store.getDefaultBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));
		reuseEditors.loadDefault();
		recentFilesEditor.loadDefault();
		
		// Sets the accelerator configuration selection to the default configuration
		String id = store.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		AcceleratorConfiguration config = registry.getConfiguration(id);
		String name = null;
		if(config != null) 
			name = config.getName();
		if(name != null)
			accelConfigCombo.select(accelConfigCombo.indexOf(name));

		//Project perspective preferences
		String projectPreference = store.getDefaultString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
		this.newProjectPerspectiveSetting = projectPreference;
		openProjectInNewWindowButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));
		openProjectInNewPageButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));
		replaceProjectButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));
		switchOnNewProjectButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE));

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
		} catch (org.eclipse.core.runtime.CoreException e) {
			// handle the exception here (could not save the new description to disk)
		}
		if (oldAutoBuildSetting != newAutoBuildSetting) {
			// fire off a property change notification so interested
			// parties can know about the auto build setting change
			// since it is not kept in the preference store.
			store.firePropertyChangeEvent(IPreferenceConstants.AUTO_BUILD, new Boolean(oldAutoBuildSetting), new Boolean(newAutoBuildSetting));

			// If auto build is turned on, then do a global incremental
			// build on all the projects.
			if (newAutoBuildSetting) {
				GlobalBuildAction action = new GlobalBuildAction(this.workbench, getShell(), IncrementalProjectBuilder.AUTO_BUILD);
				action.doBuild();
			}
		}

		// store the save all prior to build setting
		store.setValue(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, autoSaveAllButton.getSelection());

		// store the link navigator to editor setting
		store.setValue(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, linkButton.getSelection());

		// store the reuse editors setting
		reuseEditors.store();

		// store the recent files setting
		recentFilesEditor.store();

		// store the open new project perspective settings
		store.setValue(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE, newProjectPerspectiveSetting);

		// store the active accelerator configuration id
		String configName = accelConfigCombo.getText();
		AcceleratorConfiguration config = (AcceleratorConfiguration)namesToConfiguration.get(configName);
		if(config != null) {
			Workbench workbench = (Workbench)PlatformUI.getWorkbench();
			workbench.setActiveAcceleratorConfiguration(config);
			store.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, config.getId());
		}
		return true;
	}
}