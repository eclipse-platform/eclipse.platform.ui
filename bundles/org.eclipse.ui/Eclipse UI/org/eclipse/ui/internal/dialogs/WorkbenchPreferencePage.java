package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.resources.*;

import org.eclipse.jface.preference.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;

public class WorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener {
	private IWorkbench workbench;
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button linkButton;
	private IntegerFieldEditor recentFilesEditor;

	//Widgets for menu based perspective operation
	private Button openInNewWindowButton;
	private Button openInNewPageButton;
	private Button switchOnNewProjectButton;
	private Button version2PerspectivesButton;
	private Text openInNewWindowText;
	private Text openInNewPageText;

	//Widgets for perspective switching when creating new projects
	private Button openProjectInNewWindowButton;
	private Button openProjectInNewPageButton;
	private Button replaceProjectButton;

	private String currentPerspectiveSetting;
	private String newProjectPerspectiveSetting;

	//Labels
	private static final String NEW_PERSPECTIVE_TITLE = WorkbenchMessages.getString("WorkbenchPreference.openNewPerspective"); //$NON-NLS-1$
	private static final String NEW_PROJECT_PERSPECTIVE_TITLE = WorkbenchMessages.getString("WorkbenchPreference.projectOptionsTitle"); //$NON-NLS-1$

	private static final String OPEN_NEW_WINDOW_LABEL = WorkbenchMessages.getString("WorkbenchPreference.newWindow"); //$NON-NLS-1$
	private static final String OPEN_NEW_PAGE_LABEL = WorkbenchMessages.getString("WorkbenchPreference.sameWindow"); //$NON-NLS-1$
	private static final String OPEN_REPLACE_LABEL = WorkbenchMessages.getString("WorkbenchPreference.replaceCurrent"); //$NON-NLS-1$

	private static final String OPEN_NEW_WINDOW_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.projectNewWindow"); //$NON-NLS-1$
	private static final String OPEN_NEW_PAGE_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.projectSameWindow"); //$NON-NLS-1$
	private static final String OPEN_REPLACE_PROJECT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.replacePerspective"); //$NON-NLS-1$
	private static final String DO_NOT_SWITCH_PERSPECTIVES = WorkbenchMessages.getString("WorkbenchPreference.noSwitch"); //$NON-NLS-1$

	private static final String SHIFT_LABEL = WorkbenchMessages.getString("WorkbenchPreference.shift"); //$NON-NLS-1$
	private static final String ALT_LABEL = getAlternateString();
	/**
	 * Get the values for the alt perspective setting. It will be replace unless replace is selected.
	 */
	private String altPerspectiveSetting() {
		return shiftPerspectiveSetting();
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

		WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IHelpContextIds.WORKBENCH_PREFERENCE_PAGE));

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

		createPerspectiveGroup(composite);

		createProjectPerspectiveGroup(composite);

		// set initial values
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		linkButton.setSelection(store.getBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));

		return composite;
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
	 * Create a composite that contains buttons for selecting the preference opening selections. 
	 */
	private void createPerspectiveGroup(Composite composite) {

		Group buttonComposite = new Group(composite, SWT.LEFT);
		buttonComposite.setText(NEW_PERSPECTIVE_TITLE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(data);

		//Open New Window button
		this.openInNewWindowButton = createRadioButton(buttonComposite, OPEN_NEW_WINDOW_LABEL);
		this.openInNewWindowButton.setSelection(this.currentPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));

		this.openInNewWindowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				currentPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
				setTextValuesForPerspective();
			}
		});

		this.openInNewWindowText = new Text(buttonComposite, SWT.NONE);
		this.openInNewWindowText.setEditable(false);

		//Open New Page button
		this.openInNewPageButton = createRadioButton(buttonComposite, OPEN_NEW_PAGE_LABEL);
		this.openInNewPageButton.setSelection(this.currentPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));

		this.openInNewPageButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				currentPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
				setTextValuesForPerspective();
			}
		});

		this.openInNewPageText = new Text(buttonComposite, SWT.NONE);
		this.openInNewPageText.setEditable(false);

		version2PerspectivesButton = new Button(composite, SWT.CHECK);
		version2PerspectivesButton.setText(WorkbenchMessages.getString("WorkbenchPreference.reusePerspectives")); //$NON-NLS-1$
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		version2PerspectivesButton.setSelection(store.getBoolean(IPreferenceConstants.REUSE_PERSPECTIVES));

		setTextValuesForPerspective();
	}
	/**
	 * Create a composite that contains buttons for selecting the 
	 * preference opening new project selections. 
	 */
	private void createProjectPerspectiveGroup(Composite composite) {

		Group buttonComposite = new Group(composite, SWT.LEFT);
		buttonComposite.setText(NEW_PROJECT_PERSPECTIVE_TITLE);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(data);

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
		button.addListener(SWT.Selection, this);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
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
	 * Get the label for the alternate setting for this platform - either Control for Windows
	 * or Shift-Alt for Motif.
	 * @return java.lang.String
	 */
	private static String getAlternateString() {
		if (SWT.getPlatform().equals("win32")) //$NON-NLS-1$
			return WorkbenchMessages.getString("WorkbenchPreference.control"); //$NON-NLS-1$
		else
			return WorkbenchMessages.getString("WorkbenchPreference.shiftAlt"); //$NON-NLS-1$
	}
	/**
	 * Handles events generated by controls on this page.
	 *
	 * @param e  the event to handle
	 */
	public void handleEvent(Event e) {
		// get widget that generates the event
		Widget source = e.widget;

		// add the code that should react to
		// some widget event
	}
	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		this.currentPerspectiveSetting = store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
		this.newProjectPerspectiveSetting = store.getString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
		linkButton.setSelection(store.getDefaultBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));
		recentFilesEditor.loadDefault();

		//Perspective preferences
		String defaultPreference = store.getDefaultString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
		this.currentPerspectiveSetting = defaultPreference;
		openInNewWindowButton.setSelection(defaultPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));
		openInNewPageButton.setSelection(defaultPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));
		version2PerspectivesButton.setSelection(store.getDefaultBoolean(IPreferenceConstants.REUSE_PERSPECTIVES));

		//Project perspective preferences
		String projectPreference = store.getDefaultString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
		this.newProjectPerspectiveSetting = projectPreference;
		openProjectInNewWindowButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));
		openProjectInNewPageButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));
		replaceProjectButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));
		switchOnNewProjectButton.setSelection(projectPreference.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE));

		setTextValuesForPerspective();
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

		// store the recent files setting
		recentFilesEditor.store();

		// store reuse perspctives setting
		store.setValue(IPreferenceConstants.REUSE_PERSPECTIVES, version2PerspectivesButton.getSelection());

		// store the open in new window settings
		store.setValue(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE, currentPerspectiveSetting);

		// store the open in new window shift settings
		store.setValue(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE, shiftPerspectiveSetting());

		// store the open in new window alt settings
		store.setValue(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE, altPerspectiveSetting());

		// store the open in new project settings
		store.setValue(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE, newProjectPerspectiveSetting);

		return true;
	}
	/**
	 * Set the values for the text based on the current setting.
	 */
	private void setTextValuesForPerspective() {
		if (this.currentPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE)) {
			this.openInNewWindowText.setText(SHIFT_LABEL);
			this.openInNewPageText.setText(""); //$NON-NLS-1$
		} else {
			if (this.currentPerspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW)) {
				this.openInNewPageText.setText(SHIFT_LABEL);
				this.openInNewWindowText.setText(""); //$NON-NLS-1$
			}

		}
	}
	/**
	 * Get the values for the shift perspective setting. It will be window unless window is selected.
	 */
	private String shiftPerspectiveSetting() {
		if (this.currentPerspectiveSetting == IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW)
			return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
		else
			return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
	}
}