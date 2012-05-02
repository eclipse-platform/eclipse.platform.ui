/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM - Initial API and implementation
 * 		Remy Chi Jian Suen <remy.suen@gmail.com>
 * 			- Fix for Bug 155436 [IDE] Project>Clean dialog should not use a question-mark icon
 * 		Mark Melvin <mark_melvin@amis.com>
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.IProgressConstants2;

/**
 * Dialog that asks the user to confirm a clean operation, and to configure
 * settings in relation to the clean. Clicking ok in the dialog will perform the
 * clean operation.
 * 
 * @since 3.0
 */
public class CleanDialog extends MessageDialog {
    private class ProjectSubsetBuildAction extends BuildAction {
        private IProject[] projectsToBuild = new IProject[0];
        public ProjectSubsetBuildAction(IShellProvider shellProvider, int type, IProject[] projects) {
            super(shellProvider, type);
            this.projectsToBuild = projects;
        }

        protected List getSelectedResources() {
            return Arrays.asList(this.projectsToBuild);
        }
	}

    private static final String DIALOG_SETTINGS_SECTION = "CleanDialogSettings"; //$NON-NLS-1$
    private static final String DIALOG_ORIGIN_X = "DIALOG_X_ORIGIN"; //$NON-NLS-1$
    private static final String DIALOG_ORIGIN_Y = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$
    private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
    private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
    private static final String TOGGLE_SELECTED = "TOGGLE_SELECTED"; //$NON-NLS-1$
    private static final String BUILD_NOW = "BUILD_NOW"; //$NON-NLS-1$
    private static final String BUILD_ALL = "BUILD_ALL"; //$NON-NLS-1$
    
    private Button allButton, selectedButton, buildNowButton, globalBuildButton, projectBuildButton;

    private CheckboxTableViewer projectNames;

    private Object[] selection;

    private IWorkbenchWindow window;

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
        super(
                window.getShell(),
                IDEWorkbenchMessages.CleanDialog_title, null, getQuestion(), NONE, new String[] {
                IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
        this.window = window;
        this.selection = selection;
        if (this.selection == null) {
            this.selection = new Object[0];
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        final boolean cleanAll = allButton.getSelection();
        final boolean buildAll = buildNowButton != null
                && buildNowButton.getSelection();
        final boolean globalBuild = globalBuildButton != null
                && globalBuildButton.getSelection();
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
            public boolean belongsTo(Object family) {
                return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
            }
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
        cleanJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
                .buildRule());
        cleanJob.setUser(true);
        cleanJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
        cleanJob.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createCustomArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        area.setLayout(layout);
        area.setLayoutData(new GridData(GridData.FILL_BOTH));
        SelectionListener updateEnablement = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateEnablement();
            }
        };

        IDialogSettings settings = getDialogSettings(DIALOG_SETTINGS_SECTION);
        boolean selectSelectedButton= settings.getBoolean(TOGGLE_SELECTED);
        //first row
        allButton = new Button(area, SWT.RADIO);
        allButton.setText(IDEWorkbenchMessages.CleanDialog_cleanAllButton);
        allButton.setSelection(!selectSelectedButton);
        allButton.addSelectionListener(updateEnablement);
        selectedButton = new Button(area, SWT.RADIO);
        selectedButton.setText(IDEWorkbenchMessages.CleanDialog_cleanSelectedButton);
        selectedButton.setSelection(selectSelectedButton);
        selectedButton.addSelectionListener(updateEnablement);

        //second row
        createProjectSelectionTable(area);
        
        //third row
        //only prompt for immediate build if autobuild is off
        if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
            buildNowButton = new Button(parent, SWT.CHECK);
            buildNowButton.setText(IDEWorkbenchMessages.CleanDialog_buildNowButton);
            String buildNow = settings.get(BUILD_NOW);
            buildNowButton.setSelection(buildNow == null || Boolean.valueOf(buildNow).booleanValue());
            buildNowButton.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_BEGINNING));
            buildNowButton.addSelectionListener(updateEnablement);

            globalBuildButton = new Button(parent, SWT.RADIO);
            globalBuildButton.setText(IDEWorkbenchMessages.CleanDialog_globalBuildButton);
            String buildAll = settings.get(BUILD_ALL);
            globalBuildButton.setSelection(buildAll == null || Boolean.valueOf(buildAll).booleanValue());
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


            SelectionListener buildRadioSelected = new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    updateBuildRadioEnablement();
                }
            };
            globalBuildButton.addSelectionListener(buildRadioSelected);
            projectBuildButton.addSelectionListener(buildRadioSelected);
        }

        return area;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
    	Control contents= super.createContents(parent);
    	updateEnablement();
    	return contents;
    }

    private void createProjectSelectionTable(Composite radioGroup) {
        projectNames = CheckboxTableViewer.newCheckList(radioGroup, SWT.BORDER);
        projectNames.setContentProvider(new WorkbenchContentProvider());
        projectNames.setLabelProvider(new WorkbenchLabelProvider());
        projectNames.setComparator(new ResourceComparator(ResourceComparator.NAME));
        projectNames.addFilter(new ViewerFilter() {
            private final IProject[] projectHolder = new IProject[1];
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (!(element instanceof IProject)) {
                    return false;
                }
                IProject project = (IProject) element;
                if (!project.isAccessible()) {
                    return false;
                }
                projectHolder[0] = project;
                return BuildUtilities.isEnabled(projectHolder, IncrementalProjectBuilder.CLEAN_BUILD);
            }
        });
        projectNames.setInput(ResourcesPlugin.getWorkspace().getRoot());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        data.heightHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        projectNames.getTable().setLayoutData(data);
        projectNames.setCheckedElements(selection);
        //table is disabled to start because all button is selected
        projectNames.getTable().setEnabled(selectedButton.getSelection());
        projectNames.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                selection = projectNames.getCheckedElements();
                updateEnablement();
            }
        });
    }

    /**
     * Performs the actual clean operation.
     * @param cleanAll if <code>true</true> clean all projects
     * @param monitor The monitor that the build will report to
     * @throws CoreException thrown if there is a problem from the
     * core builder.
     */
    protected void doClean(boolean cleanAll, IProgressMonitor monitor)
            throws CoreException {
        if (cleanAll) {
            ResourcesPlugin.getWorkspace().build(
                    IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        } else {
            try {
                monitor.beginTask(IDEWorkbenchMessages.CleanDialog_cleanSelectedTaskName, selection.length);
                for (int i = 0; i < selection.length; i++) {
                    ((IProject) selection[i]).build(
                            IncrementalProjectBuilder.CLEAN_BUILD,
                            new SubProgressMonitor(monitor, 1));
                }
            } finally {
                monitor.done();
            }
        }
    }

    /**
     * Updates the enablement of the dialog's ok button based
     * on the current choices in the dialog.
     */
    protected void updateEnablement() {
        projectNames.getTable().setEnabled(selectedButton.getSelection());
        boolean enabled = allButton.getSelection() || selection.length > 0;
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        persistDialogSettings(getShell(), DIALOG_SETTINGS_SECTION);
        return super.close();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    protected Point getInitialLocation(Point initialSize) {
        Point p = getInitialLocation(DIALOG_SETTINGS_SECTION);
        return p != null ? p : super.getInitialLocation(initialSize);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialSize()
     */
    protected Point getInitialSize() {
        Point p = super.getInitialSize();
        return getInitialSize(DIALOG_SETTINGS_SECTION, p);
    }
    
    /**
     * Returns the initial location which is persisted in the IDE Plugin dialog settings
     * under the provided dialog setttings section name.
     * If location is not persisted in the settings, the <code>null</code> is returned.
     * 
     * @param dialogSettingsSectionName The name of the dialog settings section
     * @return The initial location or <code>null</code>
     */
    public Point getInitialLocation(String dialogSettingsSectionName) {
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        try {
            int x= settings.getInt(DIALOG_ORIGIN_X);
            int y= settings.getInt(DIALOG_ORIGIN_Y);
            return new Point(x,y);
        } catch (NumberFormatException e) {
        }
        return null;
    }
    
    private IDialogSettings getDialogSettings(String dialogSettingsSectionName) {
        IDialogSettings settings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(dialogSettingsSectionName);
        if (section == null) {
            section = settings.addNewSection(dialogSettingsSectionName);
        }
        return section;
    }

    /**
     * Persists the location and dimensions of the shell and other user settings in the
     * plugin's dialog settings under the provided dialog settings section name
     * 
     * @param shell The shell whose geometry is to be stored
     * @param dialogSettingsSectionName The name of the dialog settings section
     */
    private void persistDialogSettings(Shell shell, String dialogSettingsSectionName) {
        Point shellLocation = shell.getLocation();
        Point shellSize = shell.getSize();
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        settings.put(DIALOG_ORIGIN_X, shellLocation.x);
        settings.put(DIALOG_ORIGIN_Y, shellLocation.y);
        settings.put(DIALOG_WIDTH, shellSize.x);
        settings.put(DIALOG_HEIGHT, shellSize.y);

        if (buildNowButton != null) {
            settings.put(BUILD_NOW, buildNowButton.getSelection());
        }
        if (globalBuildButton != null) {
            settings.put(BUILD_ALL, globalBuildButton.getSelection());
        }
        settings.put(TOGGLE_SELECTED, selectedButton.getSelection());
    }

    /**
     * Returns the initial size which is the larger of the <code>initialSize</code> or
     * the size persisted in the IDE UI Plugin dialog settings under the provided dialog setttings section name.
     * If no size is persisted in the settings, the <code>initialSize</code> is returned.
     * 
     * @param initialSize The initialSize to compare against
     * @param dialogSettingsSectionName The name of the dialog settings section
     * @return the initial size
     */
    private Point getInitialSize(String dialogSettingsSectionName, Point initialSize) {
        IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
        try {
            int x, y;
            x = settings.getInt(DIALOG_WIDTH);
            y = settings.getInt(DIALOG_HEIGHT);
            return new Point(Math.max(x, initialSize.x), Math.max(y, initialSize.y));
        } catch (NumberFormatException e) {
        }
        return initialSize;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
        return true;
    }
}
