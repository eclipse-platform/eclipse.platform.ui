/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448060
 *******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Page used to determine what order projects will be built in
 * by the workspace.
 */
public class BuildOrderPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private IWorkbench workbench;

	private Button defaultOrderButton;

	private Label buildLabel;

	private List buildList;

	private Composite buttonComposite;

	private IntegerFieldEditor maxItersField;

	private String[] defaultBuildOrder;

	private String[] customBuildOrder;

	//Boolean to indicate if we have looked it up
	private boolean notCheckedBuildOrder = true;

	private final String UP_LABEL = IDEWorkbenchMessages.BuildOrderPreference_up;

	private final String DOWN_LABEL = IDEWorkbenchMessages.BuildOrderPreference_down;

	private final String ADD_LABEL = IDEWorkbenchMessages.BuildOrderPreference_add;

	private final String REMOVE_LABEL = IDEWorkbenchMessages.BuildOrderPreference_remove;

	private final String PROJECT_SELECTION_MESSAGE = IDEWorkbenchMessages.BuildOrderPreference_selectOtherProjects;

	private final String DEFAULTS_LABEL = IDEWorkbenchMessages.BuildOrderPreference_useDefaults;

	private final String LIST_LABEL = IDEWorkbenchMessages.BuildOrderPreference_projectBuildOrder;

	private final String NOTE_LABEL = IDEWorkbenchMessages.Preference_note;

	private final String REMOVE_MESSAGE = IDEWorkbenchMessages.BuildOrderPreference_removeNote;

	// whether or not the use defaults option was selected when Apply (or OK) was last pressed
	// (or when the preference page was opened). This represents the most recent applied state.
	private boolean defaultOrderInitiallySelected;

	private Button autoBuildButton;

	private IntegerFieldEditor maxSimultaneousBuilds;

	private Button autoSaveAllButton;

	private IPropertyChangeListener validityChangeListener = event -> {
		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			updateValidState();
		}
	};

	/**
	 * Add another project to the list at the end.
	 */
	private void addProject() {

		String[] currentItems = this.buildList.getItems();

		IProject[] allProjects = getWorkspace().getRoot().getProjects();

		ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		};

		ListSelectionDialog dialog = new ListSelectionDialog(this.getShell(), sortedDifference(allProjects,
				currentItems), ArrayContentProvider.getInstance(), labelProvider,
				PROJECT_SELECTION_MESSAGE) {
			@Override
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.SHEET;
			}
		};

		if (dialog.open() != Window.OK) {
			return;
		}

		Object[] result = dialog.getResult();

		int currentItemsLength = currentItems.length;
		int resultLength = result.length;
		String[] newItems = new String[currentItemsLength + resultLength];

		System.arraycopy(currentItems, 0, newItems, 0, currentItemsLength);
		System
				.arraycopy(result, 0, newItems, currentItemsLength,
						result.length);
		this.buildList.setItems(newItems);
	}

	/**
	 * Updates the valid state of the page.
	 */
	private void updateValidState() {
		setValid(maxItersField.isValid());
	}

	/**
	 * Create the list of build paths. If the current build order is empty make the list empty
	 * and disable it.
	 * @param composite - the parent to create the list in
	 * @param enabled - the boolean that indcates if the list will be sensitive initially or not
	 */
	private void createBuildOrderList(Composite composite, boolean enabled) {

		Font font = composite.getFont();

		this.buildLabel = new Label(composite, SWT.NONE);
		this.buildLabel.setText(LIST_LABEL);
		this.buildLabel.setEnabled(enabled);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		this.buildLabel.setLayoutData(gridData);
		this.buildLabel.setFont(font);

		this.buildList = new List(composite, SWT.BORDER | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		this.buildList.setEnabled(enabled);
		GridData data = new GridData();
		//Set heightHint with a small value so the list size will be defined by
		//the space available in the dialog instead of resizing the dialog to
		//fit all the items in the list.
		data.heightHint = buildList.getItemHeight();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		this.buildList.setLayoutData(data);
		this.buildList.setFont(font);
	}

	/**
	 * Create the widgets that are used to determine the build order.
	 *
	 * @param parent the parent composite
	 * @return the new control
	 */
	@Override
	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IIDEHelpContextIds.BUILD_ORDER_PREFERENCE_PAGE);

		Font font = parent.getFont();

		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		composite.setFont(font);

		createSaveAllBeforeBuildPref(composite);
		createAutoBuildPref(composite);
		createSpacer(composite);

		String[] buildOrder = getCurrentBuildOrder();
		boolean useDefault = (buildOrder == null);

		createDefaultPathButton(composite, useDefault);
		// List always enabled so user can scroll list.
		// Only the buttons need to be disabled.
		createBuildOrderList(composite, true);
		createListButtons(composite, !useDefault);

		Composite noteComposite = createNoteComposite(font, composite,
				NOTE_LABEL, REMOVE_MESSAGE);
		GridData noteData = new GridData();
		noteData.horizontalSpan = 2;
		noteComposite.setLayoutData(noteData);

		createSpacer(composite);

		Composite intFieldsComposite = new Composite(composite, SWT.NONE);
		intFieldsComposite.setLayout(new GridLayout(2, false));
		intFieldsComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		createMaxIterationsField(intFieldsComposite);
		createMaxSimultaneousBuildsGroup(intFieldsComposite);

		if (useDefault) {
			this.buildList.setItems(getDefaultProjectOrder());
		} else {
			this.buildList.setItems(buildOrder);
		}

		return composite;

	}

	/**
	 * Adds in a spacer.
	 *
	 * @param composite the parent composite
	 */
	private void createSpacer(Composite composite) {
		Label spacer = new Label(composite, SWT.NONE);
		GridData spacerData = new GridData();
		spacerData.horizontalSpan = 2;
		spacer.setLayoutData(spacerData);
	}

	/**
	 * Create the default path button. Set it to selected based on the current workspace
	 * build path.
	 * @param composite org.eclipse.swt.widgets.Composite
	 * @param selected - the boolean that indicates the buttons initial state
	 */
	private void createDefaultPathButton(Composite composite, boolean selected) {

		defaultOrderInitiallySelected = selected;

		this.defaultOrderButton = new Button(composite, SWT.LEFT | SWT.CHECK);
		this.defaultOrderButton.setSelection(selected);
		this.defaultOrderButton.setText(DEFAULTS_LABEL);
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				defaultsButtonSelected(defaultOrderButton.getSelection());
			}
		};
		this.defaultOrderButton.addSelectionListener(listener);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		this.defaultOrderButton.setLayoutData(gridData);
		this.defaultOrderButton.setFont(composite.getFont());
	}

	/**
	 * Create the buttons used to manipulate the list. These Add, Remove and Move Up or Down
	 * the list items.
	 * @param composite the parent of the buttons
	 * @param enableComposite - boolean that indicates if a composite should be enabled
	 */
	private void createListButtons(Composite composite, boolean enableComposite) {

		Font font = composite.getFont();

		// Create an intermediate composite to keep the buttons in the same column
		this.buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.buttonComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		this.buttonComposite.setLayoutData(gridData);
		this.buttonComposite.setFont(font);

		Button upButton = new Button(this.buttonComposite, SWT.CENTER
				| SWT.PUSH);
		upButton.setText(UP_LABEL);
		upButton.setEnabled(enableComposite);
		upButton.setFont(font);
		setButtonLayoutData(upButton);

		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectionUp();
			}
		};
		upButton.addSelectionListener(listener);

		Button downButton = new Button(this.buttonComposite, SWT.CENTER
				| SWT.PUSH);
		downButton.setText(DOWN_LABEL);
		downButton.setEnabled(enableComposite);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectionDown();
			}
		};
		downButton.addSelectionListener(listener);
		downButton.setFont(font);
		setButtonLayoutData(downButton);

		Button addButton = new Button(this.buttonComposite, SWT.CENTER
				| SWT.PUSH);
		addButton.setText(ADD_LABEL);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addProject();
			}
		};
		addButton.addSelectionListener(listener);
		addButton.setEnabled(enableComposite);
		addButton.setFont(font);
		setButtonLayoutData(addButton);

		Button removeButton = new Button(this.buttonComposite, SWT.CENTER
				| SWT.PUSH);
		removeButton.setText(REMOVE_LABEL);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelection();
			}
		};
		removeButton.addSelectionListener(listener);
		removeButton.setEnabled(enableComposite);
		removeButton.setFont(font);
		setButtonLayoutData(removeButton);

	}

	/**
	 * Create the field for the maximum number of iterations in the presence
	 * of cycles.
	 */
	private void createMaxIterationsField(Composite composite) {
		maxItersField = new IntegerFieldEditor(
				"", IDEWorkbenchMessages.BuildOrderPreference_maxIterationsLabel, composite) { //$NON-NLS-1$
			@Override
			protected void doLoad() {
				Text text = getTextControl();
				if (text != null) {
					int value = getWorkspace().getDescription()
							.getMaxBuildIterations();
					text.setText(Integer.toString(value));
				}
			}

			@Override
			protected void doLoadDefault() {
				Text text = getTextControl();
				if (text != null) {
					IEclipsePreferences def = DefaultScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
					int defaultValue = 10; // org.eclipse.core.internal.resources.PreferenceInitializer.PREF_MAX_BUILD_ITERATIONS_DEFAULT
					int value = def.getInt(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS, defaultValue);
					text.setText(Integer.toString(value));
				}
				valueChanged();
			}

			@Override
			protected void doStore() {
				// handled specially in performOK()
				throw new UnsupportedOperationException();
			}
		};
		maxItersField.setValidRange(1, Integer.MAX_VALUE);
		maxItersField.setPage(this);
		maxItersField.setPreferenceStore(getPreferenceStore());
		maxItersField.setPropertyChangeListener(validityChangeListener);
		maxItersField.load();
	}

	/**
	 * The defaults button has been selected - update the other widgets as required.
	 * @param selected - whether or not the defaults button got selected
	 */
	private void defaultsButtonSelected(boolean selected) {
		if (selected) {
			setBuildOrderWidgetsEnablement(false);
			//Cache the current value as the custom order
			customBuildOrder = buildList.getItems();
			buildList.setItems(getDefaultProjectOrder());

		} else {
			setBuildOrderWidgetsEnablement(true);
			String[] buildOrder = getCurrentBuildOrder();
			if (buildOrder == null) {
				buildList.setItems(getDefaultProjectOrder());
			} else {
				buildList.setItems(buildOrder);
			}
		}
	}

	/**
	 * Get the project names for the current custom build
	 * order stored in the workspace description.
	 *
	 * @return java.lang.String[] or null if there is no setting
	 */
	private String[] getCurrentBuildOrder() {
		if (notCheckedBuildOrder) {
			customBuildOrder = getWorkspace().getDescription().getBuildOrder();
			notCheckedBuildOrder = false;
		}

		return customBuildOrder;
	}

	/**
	 * Get the project names in the default build order
	 * based on the current Workspace settings.
	 *
	 * @return java.lang.String[]
	 */
	private String[] getDefaultProjectOrder() {
		if (defaultBuildOrder == null) {
			IWorkspace workspace = getWorkspace();
			IWorkspace.ProjectOrder projectOrder = getWorkspace()
					.computeProjectOrder(workspace.getRoot().getProjects());
			IProject[] foundProjects = projectOrder.projects;
			defaultBuildOrder = new String[foundProjects.length];
			int foundSize = foundProjects.length;
			for (int i = 0; i < foundSize; i++) {
				defaultBuildOrder[i] = foundProjects[i].getName();
			}
		}

		return defaultBuildOrder;
	}

	/**
	 * Return the Workspace the build order is from.
	 * @return org.eclipse.core.resources.IWorkspace
	 */
	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Return whether or not searchElement is in testArray.
	 */
	private boolean includes(String[] testArray, String searchElement) {

		for (String currentSearchElement : testArray) {
			if (searchElement.equals(currentSearchElement)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * See IWorkbenchPreferencePage. This class does nothing with he Workbench.
	 */
	@Override
	public void init(IWorkbench currentWorkbench) {
		this.workbench = currentWorkbench;
		setPreferenceStore(PrefUtil.getInternalPreferenceStore());
	}

	/**
	 * Move the current selection in the build list down.
	 */
	private void moveSelectionDown() {

		//Only do this operation on a single selection
		if (this.buildList.getSelectionCount() == 1) {
			int currentIndex = this.buildList.getSelectionIndex();
			if (currentIndex < this.buildList.getItemCount() - 1) {
				String elementToMove = this.buildList.getItem(currentIndex);
				this.buildList.remove(currentIndex);
				this.buildList.add(elementToMove, currentIndex + 1);
				this.buildList.setSelection(currentIndex + 1);
			}
		}
	}

	/**
	 * Move the current selection in the build list up.
	 */
	private void moveSelectionUp() {

		int currentIndex = this.buildList.getSelectionIndex();

		//Only do this operation on a single selection
		if (currentIndex > 0 && this.buildList.getSelectionCount() == 1) {
			String elementToMove = this.buildList.getItem(currentIndex);
			this.buildList.remove(currentIndex);
			this.buildList.add(elementToMove, currentIndex - 1);
			this.buildList.setSelection(currentIndex - 1);
		}
	}

	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 * In this case change the defaultOrderButton to have it's selection set to true.
	 */
	@Override
	protected void performDefaults() {
		this.defaultOrderButton.setSelection(true);
		defaultsButtonSelected(true);
		maxItersField.loadDefault();

		// core holds onto this preference.
		IEclipsePreferences def = DefaultScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
		boolean autoBuild = def.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING, true);
		autoBuildButton.setSelection(autoBuild);

		int simultaneousBuilds = def.getInt(ResourcesPlugin.PREF_MAX_CONCURRENT_BUILDS, 1);
		maxSimultaneousBuilds.setStringValue(Integer.toString(simultaneousBuilds));

		IPreferenceStore store = getIDEPreferenceStore();
		autoSaveAllButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
		super.performDefaults();
	}

	/**
	 * OK has been pressed. If the default button is pressed then reset the build
	 * order to false; otherwise set it to the contents of the list.
	 */
	@Override
	public boolean performOk() {

		String[] buildOrder = null;
		boolean useDefault = defaultOrderButton.getSelection();

		// if use defaults is turned off
		if (!useDefault) {
			buildOrder = buildList.getItems();
		}

		//Get a copy of the description from the workspace, set the build order and then
		//apply it to the workspace.
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (autoBuildButton.getSelection() != getWorkspace().isAutoBuilding()) {
			try {
				description.setAutoBuilding(autoBuildButton.getSelection());
				getWorkspace().setDescription(description);
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log(
						"Error changing auto build workspace setting.", e//$NON-NLS-1$
								.getStatus());
			}
		}
		if (maxSimultaneousBuilds.getIntValue() != description.getMaxConcurrentBuilds()) {
			try {
				description.setMaxConcurrentBuilds(maxSimultaneousBuilds.getIntValue());
				getWorkspace().setDescription(description);
			} catch (CoreException e) {
				IDEWorkbenchPlugin.log("Error changing max cucrrent builds workspace setting.", e//$NON-NLS-1$
						.getStatus());
			}
		}

		IPreferenceStore store = getIDEPreferenceStore();

		// store the save all prior to build setting
		store.setValue(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD,
				autoSaveAllButton.getSelection());

		description.setBuildOrder(buildOrder);
		description.setMaxBuildIterations(maxItersField.getIntValue());
		try {
			getWorkspace().setDescription(description);
		} catch (CoreException exception) {
			//failed - return false
			return false;
		}

		// Perform auto-build if use default is off (because
		// order could have changed) or if use default setting
		// was changed.
		if (!useDefault || (useDefault != defaultOrderInitiallySelected)) {
			defaultOrderInitiallySelected = useDefault;
			// If auto build is turned on, then do a global incremental
			// build on all the projects.
			if (ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				GlobalBuildAction action = new GlobalBuildAction(workbench
						.getActiveWorkbenchWindow(),
						IncrementalProjectBuilder.INCREMENTAL_BUILD);
				action.doBuild();
			}
		}

		// Clear the custom build order cache
		customBuildOrder = null;

		return true;
	}

	/**
	 * Remove the current selection in the build list.
	 */
	private void removeSelection() {

		this.buildList.remove(this.buildList.getSelectionIndices());
	}

	/**
	 * Set the widgets that select build order to be enabled or disabled.
	 *
	 * @param value boolean
	 */
	private void setBuildOrderWidgetsEnablement(boolean value) {

		// Only change enablement of buttons. Leave list alone
		// because you can't scroll it when disabled.
		for (Control child : this.buttonComposite.getChildren()) {
			child.setEnabled(value);
		}
	}

	/**
	 * Return a sorted array of the names of the projects that are already in the currently
	 * displayed names.
	 * @return String[]
	 * @param allProjects - all of the projects in the workspace
	 * @param currentlyDisplayed - the names of the projects already being displayed
	 */
	private String[] sortedDifference(IProject[] allProjects,
			String[] currentlyDisplayed) {

		TreeSet<String> difference = new TreeSet<>();

		for (IProject allProject : allProjects) {
			if (!includes(currentlyDisplayed, allProject.getName())) {
				difference.add(allProject.getName());
			}
		}

		String[] returnValue = new String[difference.size()];
		difference.toArray(returnValue);
		return returnValue;
	}

	/**
	 * Create a composite that contains entry fields specifying save interval
	 * preference.
	 *
	 * @param composite the Composite the group is created in.
	 */
	private void createMaxSimultaneousBuildsGroup(Composite composite) {
		maxSimultaneousBuilds = new IntegerFieldEditor(IDEInternalPreferences.MAX_SIMULTANEOUS_BUILD,
				IDEWorkbenchMessages.WorkbenchPreference_maxSimultaneousBuilds, composite);

		// @issue we should drop our preference constant and let clients use
		// core'zs pref. ours is not up-to-date anyway if someone changes this
		// interval directly through core api.
		maxSimultaneousBuilds.setPreferenceStore(getIDEPreferenceStore());
		maxSimultaneousBuilds.setPage(this);
		maxSimultaneousBuilds
				.setTextLimit(Integer.toString(IDEInternalPreferences.MAX_MAX_SIMULTANEOUS_BUILD).length());
		maxSimultaneousBuilds
				.setErrorMessage(NLS.bind(IDEWorkbenchMessages.WorkbenchPreference_maxSimultaneousBuildIntervalError,
						Integer.valueOf(IDEInternalPreferences.MAX_MAX_SIMULTANEOUS_BUILD)));
		maxSimultaneousBuilds.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		maxSimultaneousBuilds.setValidRange(1, IDEInternalPreferences.MAX_MAX_SIMULTANEOUS_BUILD);

		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		maxSimultaneousBuilds.setStringValue(Integer.toString(description.getMaxConcurrentBuilds()));

		maxSimultaneousBuilds.setPropertyChangeListener(event -> {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(maxSimultaneousBuilds.isValid());
			}
		});
	}

	protected void createSaveAllBeforeBuildPref(Composite composite) {
		autoSaveAllButton = new Button(composite, SWT.CHECK);
		autoSaveAllButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		autoSaveAllButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_savePriorToBuilding);
		autoSaveAllButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_savePriorToBuildingToolTip);
		autoSaveAllButton
				.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
	}

	protected void createAutoBuildPref(Composite composite) {
		autoBuildButton = new Button(composite, SWT.CHECK);
		autoBuildButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		autoBuildButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_autobuild);
		autoBuildButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_autobuildToolTip);
		autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
	}

	protected IPreferenceStore getIDEPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}

}
