/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog that asks the user to confirm a clean operation, and to configure
 * settings in relation to the clean. Clicking ok in the dialog will perform the
 * clean operation.
 * 
 * @since 3.0
 */
public class CleanDialog extends MessageDialog {
    private Button allButton, selectedButton, buildNowButton;

    private Text projectName;

    private Object[] selection;

    private IWorkbenchWindow window;

    /**
     * Gets the text of the clean dialog, depending on whether the
     * workspace is currently in autobuild mode.
     * @return String the question the user will be asked.
     */
    private static String getQuestion() {
        boolean autoBuilding = ResourcesPlugin.getWorkspace().isAutoBuilding();
        if (autoBuilding)
            return IDEWorkbenchMessages.getString("CleanDialog.buildCleanAuto"); //$NON-NLS-1$
        return IDEWorkbenchMessages.getString("CleanDialog.buildCleanManual"); //$NON-NLS-1$
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
                IDEWorkbenchMessages.getString("CleanDialog.title"), null, getQuestion(), QUESTION, new String[] { //$NON-NLS-1$
                IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
        this.window = window;
        this.selection = selection;
        if (this.selection == null)
            this.selection = new Object[0];
    }

    protected void browsePressed() {
        ILabelProvider labelProvider = new WorkbenchLabelProvider();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                getShell(), labelProvider);
        dialog.setMultipleSelection(true);
        dialog.setTitle("Project Selection"); //$NON-NLS-1$
        dialog.setMessage("Chose projects to clean:"); //$NON-NLS-1$
        dialog.setElements(ResourcesPlugin.getWorkspace().getRoot()
                .getProjects());
        dialog.setInitialSelections(new Object[] { selection });
        if (dialog.open() == Window.OK) {
            selection = dialog.getResult();
            if (selection == null)
                selection = new Object[0];
        }
        setProjectName();
        updateEnablement();
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
        super.buttonPressed(buttonId);
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                //batching changes ensures that autobuild runs after cleaning

                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                        new WorkspaceModifyOperation() {
                            protected void execute(IProgressMonitor monitor)
                                    throws CoreException {
                                doClean(cleanAll, monitor);
                            }
                        });
                //see if a build was requested
                if (buildAll) {
                    //start an immediate workspace build
                    GlobalBuildAction build = new GlobalBuildAction(window,
                            IncrementalProjectBuilder.INCREMENTAL_BUILD);
                    build.run();
                }
            } catch (InvocationTargetException e) {
                //we only throw CoreException above
                Throwable target = e.getTargetException();
                if (target instanceof CoreException)
                    ErrorDialog.openError(getShell(), null, null,
                            ((CoreException) target).getStatus());
            } catch (InterruptedException e) {
                //cancelation
            }
        }

    }

    protected void createButtonsForButtonBar(Composite parent) {
        //only need to prompt for immediate build if autobuild is off
        if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
            // increment the number of columns in the button bar
            GridLayout layout = ((GridLayout) parent.getLayout());
            layout.numColumns += 2;
            layout.makeColumnsEqualWidth = false;
            buildNowButton = new Button(parent, SWT.CHECK);
            buildNowButton.setText(IDEWorkbenchMessages
                    .getString("CleanDialog.buildNowButton")); //$NON-NLS-1$
            buildNowButton.setSelection(true);
            buildNowButton.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_BEGINNING));
            //create some horizontal space before the ok and cancel buttons
            Label spacer = new Label(parent, SWT.NONE);
            GridData data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.widthHint = 200;
            spacer.setLayoutData(data);
        }
        super.createButtonsForButtonBar(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createCustomArea(Composite parent) {
        Composite radioGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 3;
        radioGroup.setLayout(layout);
        radioGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        SelectionListener updateEnablement = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateEnablement();
            }
        };

        //first row
        allButton = new Button(radioGroup, SWT.RADIO);
        allButton.setText(IDEWorkbenchMessages
                .getString("CleanDialog.cleanAllButton")); //$NON-NLS-1$
        allButton.setSelection(true);
        allButton.addSelectionListener(updateEnablement);
        //empty label to fill rest of grid row
        new Label(radioGroup, SWT.NONE);
        new Label(radioGroup, SWT.NONE);

        //second row
        selectedButton = new Button(radioGroup, SWT.RADIO);
        selectedButton.setText(IDEWorkbenchMessages
                .getString("CleanDialog.cleanSelectedButton")); //$NON-NLS-1$
        selectedButton.addSelectionListener(updateEnablement);
        projectName = new Text(radioGroup, SWT.READ_ONLY | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        projectName.setLayoutData(data);
        setProjectName();
        Button browse = new Button(radioGroup, SWT.PUSH);
        browse.setText(IDEWorkbenchMessages.getString("CleanDialog.browse")); //$NON-NLS-1$
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                browsePressed();
            }
        });
        return radioGroup;
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
        if (cleanAll)
            ResourcesPlugin.getWorkspace().build(
                    IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        else {
            try {
                monitor.beginTask(IDEWorkbenchMessages
                        .getString("CleanDialog.taskName"), //$NON-NLS-1$
                        selection.length);
                for (int i = 0; i < selection.length; i++)
                    ((IProject) selection[i]).build(
                            IncrementalProjectBuilder.CLEAN_BUILD,
                            new SubProgressMonitor(monitor, 1));
            } finally {
                monitor.done();
            }
        }
    }

    /**
     * Fills in the name of the project in the text area.
     */
    private void setProjectName() {
        if (selection.length == 0)
            projectName.setText(IDEWorkbenchMessages
                    .getString("CleanDialog.noSelection")); //$NON-NLS-1$
        else {
            StringBuffer names = new StringBuffer(((IProject) selection[0])
                    .getName());
            for (int i = 1; i < selection.length; i++) {
                names.append(',');
                names.append(((IProject) selection[i]).getName());
            }
            projectName.setText(names.toString());
        }
    }

    /**
     * Updates the enablement of the dialog's ok button based
     * on the current choices in the dialog.
     */
    protected void updateEnablement() {
        boolean enabled = allButton.getSelection() || selection.length > 0;
        getButton(OK).setEnabled(enabled);
    }
}