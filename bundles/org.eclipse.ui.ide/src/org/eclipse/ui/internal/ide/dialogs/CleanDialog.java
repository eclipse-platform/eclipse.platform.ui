/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - [IDE] Project>Clean dialog should not use a question-mark icon - http://bugs.eclipse.org/155436
 *     Mark Melvin <mark_melvin@amis.com>
 *     Christian Georgi <christian.georgi@sap.com> -  [IDE] Clean dialog should scroll to reveal selected projects - http://bugs.eclipse.org/415522
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 462760
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472784
 *     David Weiser <David.Weiser@vogella.com> - Bug 500598
 *     Conrad Groth <info@conrad-groth.de> - Bug 514694
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.IProgressConstants2;
import org.osgi.framework.FrameworkUtil;

/**
 * Dialog that asks the user to confirm a clean operation, and to configure
 * settings in relation to the clean. Clicking ok in the dialog will perform the
 * clean operation.
 *
 * @since 3.0
 */
public class CleanDialog extends MessageDialog {

	private static class ProjectSubsetBuildAction extends BuildAction {

		private IProject[] projectsToBuild = new IProject[0];

		public ProjectSubsetBuildAction(IShellProvider shellProvider, int type, IProject[] projects) {
			super(shellProvider, type);
			this.projectsToBuild = projects;
		}

		@Override
		protected List<? extends IResource> getSelectedResources() {
			return Arrays.asList(this.projectsToBuild);
		}
	}

	private static final String DIALOG_SETTINGS_SECTION = "CleanDialogSettings"; //$NON-NLS-1$
	private static final String TOGGLE_SELECTED = "TOGGLE_SELECTED"; //$NON-NLS-1$
	private static final String BUILD_NOW = "BUILD_NOW"; //$NON-NLS-1$
	private static final String BUILD_ALL = "BUILD_ALL"; //$NON-NLS-1$

	private Button alwaysCleanButton, buildNowButton, globalBuildButton,
			projectBuildButton;

	private CheckboxTableViewer projectNames;

	private Object[] selection;

	private IWorkbenchWindow window;

	private Text filterText;
	private SearchPattern searchPattern = new SearchPattern();

	/**
	 * Gets the text of the clean dialog, depending on whether the
	 * workspace is currently in autobuild mode.
	 * @return String the question the user will be asked.
	 */
	private static String getQuestion() {
		boolean autoBuilding = ResourcesPlugin.getWorkspace().isAutoBuilding();
		if (autoBuilding) {
			return IDEWorkbenchMessages.CleanDialog_buildCleanAuto;
		}
		return IDEWorkbenchMessages.CleanDialog_buildCleanManual;
	}

	/**
	 * Creates a new clean dialog.
	 *
	 * @param window the window to create it in
	 * @param selection the currently selected projects (may be empty)
	 */
	public CleanDialog(IWorkbenchWindow window, IProject[] selection) {
		super(window.getShell(), IDEWorkbenchMessages.CleanDialog_title, null, getQuestion(), NONE, 0,
				IDEWorkbenchMessages.CleanDialog_clean_button_label, IDialogConstants.CANCEL_LABEL);
		this.window = window;
		this.selection = selection;
		if (this.selection == null) {
			this.selection = new Object[0];
		}
		searchPattern.setPattern(""); //$NON-NLS-1$
	}

	@Override
	protected void buttonPressed(int buttonId) {
		final boolean cleanAll = alwaysCleanButton.getSelection();
		final boolean buildAll = buildNowButton != null && buildNowButton.getSelection();
		final boolean globalBuild = globalBuildButton != null && globalBuildButton.getSelection();
		super.buttonPressed(buttonId);
		if (buttonId != IDialogConstants.OK_ID) {
			return;
		}
		//save all dirty editors
		BuildUtilities.saveEditors(null);
		//batching changes ensures that autobuild runs after cleaning
		WorkspaceJob cleanJob = new WorkspaceJob(
				cleanAll ? IDEWorkbenchMessages.CleanDialog_cleanAllTaskName
						: IDEWorkbenchMessages.CleanDialog_cleanSelectedTaskName) {
			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				doClean(cleanAll, monitor);
				//see if a build was requested
				if (buildAll) {
					// Only build what was requested
					if (globalBuild) {
						//start an immediate workspace build
						GlobalBuildAction build = new GlobalBuildAction(window,
								IncrementalProjectBuilder.INCREMENTAL_BUILD);
						build.doBuild();
					} else {
						// Only build what was cleaned
						IProject[] projects = new IProject[selection.length];
						for (int i = 0; i < selection.length; i++) {
							projects[i] = (IProject) selection[i];
						}

						ProjectSubsetBuildAction projectBuild =
							new ProjectSubsetBuildAction(window,
								IncrementalProjectBuilder.INCREMENTAL_BUILD,
								projects);
						projectBuild.runInBackground(
								ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
					}
				}
				return Status.OK_STATUS;
			}
		};
		cleanJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		cleanJob.setUser(true);
		cleanJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		cleanJob.schedule();
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout areaLayout = new GridLayout();
		areaLayout.marginWidth = areaLayout.marginHeight = 0;
		areaLayout.numColumns = 1;
		areaLayout.makeColumnsEqualWidth = false;
		area.setLayout(areaLayout);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));

		IDialogSettings settings = getDialogSettings();

		alwaysCleanButton = new Button(area, SWT.CHECK);
		alwaysCleanButton.setText(IDEWorkbenchMessages.CleanDialog_alwaysCleanAllButton);
		alwaysCleanButton.setSelection(!settings.getBoolean(TOGGLE_SELECTED));
		alwaysCleanButton.addSelectionListener(widgetSelectedAdapter(e -> {
			updateEnablement();
			if (!alwaysCleanButton.getSelection()) {
				setInitialFilterText();
			} else {
				filterText.setText(""); //$NON-NLS-1$
			}
		}));

		filterText = new Text(area, SWT.BORDER | SWT.SINGLE | SWT.SEARCH | SWT.ICON_CANCEL);

		filterText.setMessage(IDEWorkbenchMessages.CleanDialog_typeFilterText);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filterText.setLayoutData(gd);
		filterText.addModifyListener(e -> {
			String filter = filterText.getText();
			if (filter.startsWith("*") || filter.startsWith("?")) { //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern.setPattern(filter);
			} else {
				searchPattern.setPattern("*" + filter); //$NON-NLS-1$
			}

			if (filter.isEmpty()) {
				filterText.setMessage(IDEWorkbenchMessages.CleanDialog_typeFilterText);
			}

			projectNames.refresh();
		});

		filterText.addFocusListener(FocusListener.focusLostAdapter(e -> {
			if (filterText.getText().equals(IDEWorkbenchMessages.CleanDialog_typeFilterText)) {
				filterText.setText(""); //$NON-NLS-1$
			}
		}));

		createProjectSelectionTable(area);
		if (!alwaysCleanButton.getSelection()) {
			setInitialFilterText();
		}

		//only prompt for immediate build if autobuild is off
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			SelectionListener updateEnablement = widgetSelectedAdapter(e -> updateEnablement());

			buildNowButton = new Button(parent, SWT.CHECK);
			buildNowButton.setText(IDEWorkbenchMessages.CleanDialog_buildNowButton);
			String buildNow = settings.get(BUILD_NOW);
			buildNowButton.setSelection(buildNow == null || Boolean.parseBoolean(buildNow));
			buildNowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			buildNowButton.addSelectionListener(updateEnablement);

			globalBuildButton = new Button(parent, SWT.RADIO);
			globalBuildButton.setText(IDEWorkbenchMessages.CleanDialog_globalBuildButton);
			String buildAll = settings.get(BUILD_ALL);
			globalBuildButton.setSelection(buildAll == null || Boolean.parseBoolean(buildAll));
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			data.horizontalIndent = 10;
			globalBuildButton.setLayoutData(data);
			globalBuildButton.setEnabled(buildNowButton.getSelection());

			projectBuildButton = new Button(parent, SWT.RADIO);
			projectBuildButton.setText(IDEWorkbenchMessages.CleanDialog_buildSelectedProjectsButton);
			projectBuildButton.setSelection(!globalBuildButton.getSelection());
			data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			data.horizontalIndent = 10;
			projectBuildButton.setLayoutData(data);
			projectBuildButton.setEnabled(buildNowButton.getSelection());

			SelectionListener buildRadioSelected = widgetSelectedAdapter(e -> updateBuildRadioEnablement());

			globalBuildButton.addSelectionListener(buildRadioSelected);
			projectBuildButton.addSelectionListener(buildRadioSelected);
		}
		return area;
	}

	private void setInitialFilterText() {
		filterText.setText(IDEWorkbenchMessages.CleanDialog_typeFilterText);
		filterText.selectAll();
		filterText.setFocus();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents= super.createContents(parent);
		updateEnablement();
		return contents;
	}

	private void checkAllItemsIfSelectAllEventIsFired(SelectionEvent e) {
		if (e.item == null) {
			projectNames.setAllChecked(true);
			checkStateChanged();
		}
	}

	private void createProjectSelectionTable(Composite parent) {
		projectNames = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		projectNames.getTable().addSelectionListener(SelectionListener.widgetSelectedAdapter(this::checkAllItemsIfSelectAllEventIsFired));
		projectNames.setContentProvider(new WorkbenchContentProvider());
		projectNames.setLabelProvider(new WorkbenchLabelProvider());
		projectNames.setComparator(new ResourceComparator(ResourceComparator.NAME));
		projectNames.addFilter(new ViewerFilter() {
			private final IProject[] projectHolder = new IProject[1];
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (!(element instanceof IProject)) {
					return false;
				}
				IProject project = (IProject) element;
				boolean isProjectNameMatchingPattern = searchPattern.matches(project.getName());
				if (!project.isAccessible() || !isProjectNameMatchingPattern) {
					if (!filterText.getText().equals(IDEWorkbenchMessages.CleanDialog_typeFilterText)) {
						return false;
					}
				}
				projectHolder[0] = project;
				return BuildUtilities.isEnabled(projectHolder, IncrementalProjectBuilder.CLEAN_BUILD);
			}
		});
		projectNames.setInput(ResourcesPlugin.getWorkspace().getRoot());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		projectNames.getTable().setLayoutData(data);
		projectNames.setCheckedElements(selection);
		Object[] checked = projectNames.getCheckedElements();
		// reveal first checked project
		if (checked.length > 0) {
			projectNames.reveal(checked[0]);
		}
		projectNames.addCheckStateListener(event -> checkStateChanged());
	}

	private void checkStateChanged() {
		selection = projectNames.getCheckedElements();
		updateEnablement();
	}

	/**
	 * Performs the actual clean operation.
	 * @param cleanAll if <code>true</code> clean all projects
	 * @param monitor The monitor that the build will report to
	 * @throws CoreException thrown if there is a problem from the
	 * core builder.
	 */
	protected void doClean(boolean cleanAll, IProgressMonitor monitor)
			throws CoreException {
		if (cleanAll) {
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
		} else {
			SubMonitor subMonitor = SubMonitor.convert(monitor, IDEWorkbenchMessages.CleanDialog_cleanSelectedTaskName,
					selection.length);
			for (Object currentSelection : selection) {
				((IProject) currentSelection).build(IncrementalProjectBuilder.CLEAN_BUILD, subMonitor.split(1));
			}
		}
	}

	/**
	 * Updates the enablement of the dialog elements based on the current
	 * choices in the dialog.
	 */
	protected void updateEnablement() {
		projectNames.getTable().setEnabled(!alwaysCleanButton.getSelection());
		filterText.setEnabled(!alwaysCleanButton.getSelection());

		boolean enabled = selection.length > 0 || alwaysCleanButton.getSelection();
		getButton(OK).setEnabled(enabled);
		if (globalBuildButton != null) {
			globalBuildButton.setEnabled(buildNowButton.getSelection());
		}
		if (projectBuildButton != null) {
			projectBuildButton.setEnabled(buildNowButton.getSelection());
		}
	}

	/**
	 * Updates the enablement of the dialog's build selection radio
	 * buttons.
	 */
	protected void updateBuildRadioEnablement() {
		projectBuildButton.setSelection(!globalBuildButton.getSelection());
	}

	@Override
	public boolean close() {
		persistDialogSettings();
		return super.close();
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(CleanDialog.class))
				.getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	private void persistDialogSettings() {
		IDialogSettings settings = getDialogSettings();

		if (buildNowButton != null) {
			settings.put(BUILD_NOW, buildNowButton.getSelection());
		}
		if (globalBuildButton != null) {
			settings.put(BUILD_ALL, globalBuildButton.getSelection());
		}

		settings.put(TOGGLE_SELECTED, !alwaysCleanButton.getSelection());
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return getDialogSettings();
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}
}
