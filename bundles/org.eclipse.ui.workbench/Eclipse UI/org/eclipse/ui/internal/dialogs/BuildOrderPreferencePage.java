/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**	
 * Page used to determine what order projects will be built in 
 * by the workspace.
 */
public class BuildOrderPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

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

	private final String UP_LABEL = WorkbenchMessages.getString("BuildOrderPreference.up"); //$NON-NLS-1$
	private final String DOWN_LABEL = WorkbenchMessages.getString("BuildOrderPreference.down"); //$NON-NLS-1$
	private final String ADD_LABEL = WorkbenchMessages.getString("BuildOrderPreference.add"); //$NON-NLS-1$
	private final String REMOVE_LABEL = WorkbenchMessages.getString("BuildOrderPreference.remove"); //$NON-NLS-1$
	private final String PROJECT_SELECTION_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.selectOtherProjects"); //$NON-NLS-1$
	private final String DEFAULTS_LABEL = WorkbenchMessages.getString("BuildOrderPreference.useDefaults"); //$NON-NLS-1$
	private final String LIST_LABEL = WorkbenchMessages.getString("BuildOrderPreference.projectBuildOrder"); //$NON-NLS-1$
	private final String NOTE_LABEL = WorkbenchMessages.getString("Preference.note"); //$NON-NLS-1$
	private final String REMOVE_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.removeNote"); //$NON-NLS-1$

	// whether or not the use defaults option was selected when Apply (or OK) was last pressed
	// (or when the preference page was opened). This represents the most recent applied state.
	private boolean defaultOrderInitiallySelected;

	private IPropertyChangeListener validityChangeListener =
		new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
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
			public String getText(Object element) {
				return (String) element;
			}
		};

		SimpleListContentProvider contentsProvider =
			new SimpleListContentProvider();
		contentsProvider.setElements(
			sortedDifference(allProjects, currentItems));

		ListSelectionDialog dialog =
			new ListSelectionDialog(
				this.getShell(),
				this,
				contentsProvider,
				labelProvider,
				PROJECT_SELECTION_MESSAGE);

		if (dialog.open() != Dialog.OK)
			return;

		Object[] result = dialog.getResult();

		int currentItemsLength = currentItems.length;
		int resultLength = result.length;
		String[] newItems = new String[currentItemsLength + resultLength];

		System.arraycopy(currentItems, 0, newItems, 0, currentItemsLength);
		System.arraycopy(
			result,
			0,
			newItems,
			currentItemsLength,
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
	 * @param - enabled - the boolean that indcates if the list will be sensitive initially or not
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

		this.buildList =
			new List(
				composite,
				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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
	protected Control createContents(Composite parent) {

		WorkbenchHelp.setHelp(
			parent,
			IHelpContextIds.BUILD_ORDER_PREFERENCE_PAGE);

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

		String[] buildOrder = getCurrentBuildOrder();
		boolean useDefault = (buildOrder == null);

		createDefaultPathButton(composite, useDefault);
		// List always enabled so user can scroll list.
		// Only the buttons need to be disabled.
		createBuildOrderList(composite, true);
		createListButtons(composite, !useDefault);

		Composite noteComposite =
			createNoteComposite(font, composite, NOTE_LABEL, REMOVE_MESSAGE);
		GridData noteData = new GridData();
		noteData.horizontalSpan = 2;
		noteComposite.setLayoutData(noteData);

		createSpacer(composite);

		createMaxIterationsField(composite);

		createSpacer(composite);

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
	private void createDefaultPathButton(
		Composite composite,
		boolean selected) {

		defaultOrderInitiallySelected = selected;

		this.defaultOrderButton = new Button(composite, SWT.LEFT | SWT.CHECK);
		this.defaultOrderButton.setSelection(selected);
		this.defaultOrderButton.setText(DEFAULTS_LABEL);
		SelectionListener listener = new SelectionAdapter() {
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
	private void createListButtons(
		Composite composite,
		boolean enableComposite) {

		Font font = composite.getFont();

		//Create an intermeditate composite to keep the buttons in the same column
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

		Button upButton =
			new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
		upButton.setText(UP_LABEL);
		upButton.setEnabled(enableComposite);
		upButton.setFont(font);
		setButtonLayoutData(upButton);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveSelectionUp();
			}
		};
		upButton.addSelectionListener(listener);

		Button downButton =
			new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
		downButton.setText(DOWN_LABEL);
		downButton.setEnabled(enableComposite);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveSelectionDown();
			}
		};
		downButton.addSelectionListener(listener);
		downButton.setFont(font);
		setButtonLayoutData(downButton);

		Button addButton =
			new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
		addButton.setText(ADD_LABEL);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addProject();
			}
		};
		addButton.addSelectionListener(listener);
		addButton.setEnabled(enableComposite);
		addButton.setFont(font);
		setButtonLayoutData(addButton);

		Button removeButton =
			new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
		removeButton.setText(REMOVE_LABEL);
		listener = new SelectionAdapter() {
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
		Composite maxItersComposite = new Composite(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		maxItersComposite.setLayoutData(gd);
		maxItersComposite.setFont(composite.getFont());

		maxItersField = new IntegerFieldEditor("", WorkbenchMessages.getString("BuildOrderPreference.maxIterationsLabel"), maxItersComposite) { //$NON-NLS-1$ //$NON-NLS-2$
			protected void doLoad() {
				Text text = getTextControl();
				if (text != null) {
					int value =
						getWorkspace().getDescription().getMaxBuildIterations();
					text.setText(Integer.toString(value));
				}
			}
			protected void doLoadDefault() {
				Text text = getTextControl();
				if (text != null) {
					int value =
						ResourcesPlugin
							.getPlugin()
							.getPluginPreferences()
							.getDefaultInt(
							ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS);
					text.setText(Integer.toString(value));
				}
				valueChanged();
			}
			protected void doStore() {
				// handled specially in performOK()
				throw new UnsupportedOperationException();
			}
		};
		maxItersField.setValidRange(1, Integer.MAX_VALUE);
		maxItersField.setPreferencePage(this);
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
			if (buildOrder == null)
				buildList.setItems(getDefaultProjectOrder());
			else
				buildList.setItems(buildOrder);
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
			IWorkspace.ProjectOrder projectOrder =
				getWorkspace().computeProjectOrder(
					workspace.getRoot().getProjects());
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

		for (int i = 0; i < testArray.length; i++) {
			if (searchElement.equals(testArray[i]))
				return true;
		}
		return false;

	}
	/**
	 * See IWorkbenchPreferencePage. This class does nothing with he Workbench.
	 */
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		setPreferenceStore(workbench.getPreferenceStore());
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
				this.buildList.select(currentIndex + 1);
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
			this.buildList.select(currentIndex - 1);
		}
	}
	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 * In this case change the defaultOrderButton to have it's selection set to true.
	 */
	protected void performDefaults() {
		this.defaultOrderButton.setSelection(true);
		defaultsButtonSelected(true);
		maxItersField.loadDefault();
		super.performDefaults();
	}
	/** 
	 * OK has been pressed. If the defualt button is pressed then reset the build order to false;
	 * otherwise set it to the contents of the list.
	 */
	public boolean performOk() {

		String[] buildOrder = null;
		boolean useDefault = defaultOrderButton.getSelection();

		// if use defaults is turned off
		if (!useDefault)
			buildOrder = buildList.getItems();

		//Get a copy of the description from the workspace, set the build order and then
		//apply it to the workspace.
		IWorkspaceDescription description = getWorkspace().getDescription();
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
				GlobalBuildAction action =
					new GlobalBuildAction(
						workbench.getActiveWorkbenchWindow(),
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
	 * Set the widgets that select build order to be enabled or diabled.
	 * @param value boolean
	 */
	private void setBuildOrderWidgetsEnablement(boolean value) {

		// Only change enablement of buttons. Leave list alone
		// because you can't scroll it when disabled.
		Control[] children = this.buttonComposite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setEnabled(value);
		}
	}

	/**
	 * Return a sorted array of the names of the projects that are already in the currently 
	 * displayed names.
	 * @return String[]
	 * @param allProjects - all of the projects in the workspace 
	 * @param currentlyDisplayed - the names of the projects already being displayed
	 */
	private String[] sortedDifference(
		IProject[] allProjects,
		String[] currentlyDisplayed) {

		TreeSet difference = new TreeSet();

		for (int i = 0; i < allProjects.length; i++) {
			if (!includes(currentlyDisplayed, allProjects[i].getName()))
				difference.add(allProjects[i].getName());
		}

		String[] returnValue = new String[difference.size()];
		difference.toArray(returnValue);
		return returnValue;
	}
}
