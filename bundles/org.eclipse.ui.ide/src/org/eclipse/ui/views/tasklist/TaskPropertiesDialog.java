/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;

/**
 * Shows the properties of a new or existing task, or a problem.
 */
public class TaskPropertiesDialog extends Dialog {

    /**
     * The task or problem being shown, or <code>null</code> for a new task.
     */
    private IMarker marker = null;

    /**
     * The resource on which to create a new task.
     */
    private IResource resource = null;

    /**
     * The initial attributes to use when creating a new task.
     */
    private Map initialAttributes = null;

    /**
     * The text control for the Description field.
     */
    private Text descriptionText;

    /**
     * The control for the Creation Time field.
     */
    private Label creationTime;

    /**
     * The combo box control for the Priority field.
     */
    private Combo priorityCombo;

    /**
     * The checkbox button for the Completed field.
     */
    private Button completedCheckbox;

    /**
     * The control for the Severity field.
     */
    private Label severityLabel;

    /**
     * The text control for the Resource field.
     */
    private Text resourceText;

    /**
     * The text control for the Folder field.
     */
    private Text folderText;

    /**
     * The text control for the Location field.
     */
    private Text locationText;

    /**
     * Dirty flag.  True if any changes have been made.
     */
    private boolean dirty;

    /**
     * Creates the dialog.  By default this dialog creates a new task.
     * To set the resource and initial attributes for the new task, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing task, use <code>setMarker</code>.
     * 
     * @param parentShell the parent shell
     */
    public TaskPropertiesDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Sets the marker to show or modify.
     * 
     * @param marker the marker, or <code>null</code> to create a new marker
     */
    public void setMarker(IMarker marker) {
        this.marker = marker;
    }

    /**
     * Returns the marker being created or modified.
     * For a new marker, this returns <code>null</code> until
     * the dialog returns, but is non-null after.
     */
    public IMarker getMarker() {
        return marker;
    }

    /**
     * Sets the resource to use when creating a new task.
     * If not set, the new task is created on the workspace root.
     */
    public void setResource(IResource resource) {
        this.resource = resource;
    }

    /**
     * Returns the resource to use when creating a new task,
     * or <code>null</code> if none has been set.
     * If not set, the new task is created on the workspace root.
     */
    public IResource getResource() {
        return resource;
    }

    /**
     * Sets initial attributes to use when creating a new task.
     * If not set, the new task is created with default attributes.
     */
    public void setInitialAttributes(Map initialAttributes) {
        this.initialAttributes = initialAttributes;
    }

    /**
     * Returns the initial attributes to use when creating a new task,
     * or <code>null</code> if not set.
     * If not set, the new task is created with default attributes.
     */
    public Map getInitialAttributes() {
        return initialAttributes;
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        if (marker == null) {
            newShell.setText(TaskListMessages
                    .getString("TaskProp.newTaskTitle")); //$NON-NLS-1$
        } else {
            String kind = MarkerUtil.getKindText(marker);
            newShell.setText(TaskListMessages.format(
                    "TaskProp.propertiesTitle", new Object[] { kind })); //$NON-NLS-1$
        }

        WorkbenchHelp.setHelp(newShell,
                ITaskListHelpContextIds.PROPERTIES_DIALOG);
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);
        createDescriptionArea(composite);
        if (marker != null)
            createCreationTimeArea(composite);
        if (isTask()) {
            createPriorityAndStatusArea(composite);
        } else {
            createSeverityArea(composite);
        }
        createResourceArea(composite);
        updateDialogFromMarker();
        return composite;
    }

    /**
     * Method createCreationTimeArea.
     * @param composite
     */
    private void createCreationTimeArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(TaskListMessages.getString("TaskProp.creationTime")); //$NON-NLS-1$
        label.setFont(font);

        creationTime = new Label(composite, SWT.NONE);
        creationTime.setFont(font);
    }

    /**
     * Creates only the OK button if showing problem properties, otherwise creates
     * both OK and Cancel buttons.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        if (isEditable()) {
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        }
    }

    /**
     * Creates the area for the Description field.
     */
    private void createDescriptionArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gridData);
        
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(TaskListMessages.getString("TaskProp.description")); //$NON-NLS-1$
        label.setFont(font);
        int style = SWT.SINGLE | SWT.BORDER;
        if (!isEditable()) {
            style |= SWT.READ_ONLY;
        }
        descriptionText = new Text(composite, style);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = convertHorizontalDLUsToPixels(400);
        descriptionText.setLayoutData(gridData);
        descriptionText.setFont(font);
    }

    /**
     * Creates the area for the Priority and Status fields.
     */
    private void createPriorityAndStatusArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(TaskListMessages.getString("TaskProp.priority")); //$NON-NLS-1$
        label.setFont(font);
        priorityCombo = new Combo(composite, SWT.READ_ONLY);
        priorityCombo.setItems(new String[] {
                TaskListMessages.getString("TaskList.high"), //$NON-NLS-1$
                TaskListMessages.getString("TaskList.normal"), //$NON-NLS-1$
                TaskListMessages.getString("TaskList.low") //$NON-NLS-1$
                });
        // Prevent Esc and Return from closing the dialog when the combo is active.
        priorityCombo.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        priorityCombo.setFont(font);

        completedCheckbox = new Button(composite, SWT.CHECK);
        completedCheckbox.setText(TaskListMessages
                .getString("TaskProp.completed")); //$NON-NLS-1$
        GridData gridData = new GridData();
        gridData.horizontalIndent = convertHorizontalDLUsToPixels(20);
        completedCheckbox.setLayoutData(gridData);
        completedCheckbox.setFont(font);

        if (!isEditable()) {
            priorityCombo.setEnabled(false);
            completedCheckbox.setEnabled(false);
        }
    }

    /**
     * Creates the area for the Severity field.
     */
    private void createSeverityArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(TaskListMessages.getString("TaskProp.severity")); //$NON-NLS-1$
        label.setFont(font);
        // workaround for bug 11078: Can't get a read-only combo box
        severityLabel = new Label(composite, SWT.NONE);
        severityLabel.setFont(font);
        /*
         severityCombo = new Combo(composite, SWT.READ_ONLY);
         severityCombo.setItems(new String[] {
         TaskListMessages.getString("TaskList.error"), //$NON-NLS-1$
         TaskListMessages.getString("TaskList.warning"), //$NON-NLS-1$
         TaskListMessages.getString("TaskList.info") //$NON-NLS-1$
         });
         */
    }

    /**
     * Creates the area for the Resource field.
     */
    private void createResourceArea(Composite parent) {
        IResource resource = getResource();
        if (marker == null) {
            if (resource == null)
                return;
            if ((resource.getType() & (IResource.FILE | IResource.FOLDER | IResource.PROJECT)) == 0)
                return;
        }

        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gridData);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label resourceLabel = new Label(composite, SWT.NONE);
        resourceLabel
                .setText(TaskListMessages.getString("TaskProp.onResource")); //$NON-NLS-1$
        resourceLabel.setFont(font);
        resourceText = new Text(composite, SWT.SINGLE | SWT.WRAP
                | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        resourceText.setLayoutData(gridData);
        resourceText.setFont(font);

        Label folderLabel = new Label(composite, SWT.NONE);
        folderLabel.setText(TaskListMessages.getString("TaskProp.inFolder")); //$NON-NLS-1$
        folderLabel.setFont(font);
        folderText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY
                | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        folderText.setLayoutData(gridData);
        folderText.setFont(font);

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText(TaskListMessages.getString("TaskProp.location")); //$NON-NLS-1$
        locationLabel.setFont(font);
        locationText = new Text(composite, SWT.SINGLE | SWT.WRAP
                | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        locationText.setLayoutData(gridData);
        locationText.setFont(font);
    }

    /**
     * Updates the dialog from the marker state.
     */
    private void updateDialogFromMarker() {
        if (marker == null) {
            updateDialogForNewMarker();
            return;
        }
        descriptionText.setText(MarkerUtil.getMessage(marker));
        descriptionText.selectAll();
        creationTime.setText(MarkerUtil.getCreationTime(marker));
        if (isTask()) {
            priorityCombo.clearSelection();
            priorityCombo.select(IMarker.PRIORITY_HIGH
                    - MarkerUtil.getPriority(marker));
            completedCheckbox.setSelection(MarkerUtil.isComplete(marker));
            markDirty();
        } else {
            /* 	workaround for bug 11078: Can't get a read-only combo box
             severityCombo.clearSelection();
             severityCombo.select(IMarker.SEVERITY_ERROR - MarkerUtil.getSeverity(marker));
             */
            String sev = ""; //$NON-NLS-1$
            switch (MarkerUtil.getSeverity(marker)) {
            case IMarker.SEVERITY_ERROR:
                sev = TaskListMessages.getString("TaskList.error"); //$NON-NLS-1$
                break;
            case IMarker.SEVERITY_WARNING:
                sev = TaskListMessages.getString("TaskList.warning"); //$NON-NLS-1$
                break;
            case IMarker.SEVERITY_INFO:
                sev = TaskListMessages.getString("TaskList.info"); //$NON-NLS-1$
                break;
            }
            severityLabel.setText(sev);

        }
        resourceText.setText(MarkerUtil.getResourceName(marker));
        folderText.setText(MarkerUtil.getContainerName(marker));
        locationText.setText(MarkerUtil.getLineAndLocation(marker));
    }

    /**
     * Updates the dialog to reflect the state for a new marker.
     */
    private void updateDialogForNewMarker() {
        Map attrs = getInitialAttributes();

        String desc = ""; //$NON-NLS-1$
        if (attrs != null) {
            Object o = attrs.get(IMarker.MESSAGE);
            if (o instanceof String) {
                desc = (String) o;
            }
        }
        descriptionText.setText(desc);
        descriptionText.selectAll();

        int pri = IMarker.PRIORITY_NORMAL;
        if (attrs != null) {
            Object o = attrs.get(IMarker.PRIORITY);
            if (o instanceof Integer) {
                int val = ((Integer) o).intValue();
                if (val >= IMarker.PRIORITY_LOW && val <= IMarker.PRIORITY_HIGH) {
                    pri = val;
                }
            }
        }
        priorityCombo.deselectAll();
        priorityCombo.select(IMarker.PRIORITY_HIGH - pri);

        boolean completed = false;
        if (attrs != null) {
            Object o = attrs.get(IMarker.DONE);
            if (o instanceof Boolean) {
                completed = ((Boolean) o).booleanValue();
            }
        }
        completedCheckbox.setSelection(completed);

        IResource resource = getResource();
        if (resource != null && resourceText != null) {
            resourceText.setText(resource.getName());
            IResource parent = resource.getParent();
            folderText
                    .setText(parent == null ? "" : parent.getFullPath().toString().substring(1)); //$NON-NLS-1$
        }

        int line = -1;
        String loc = ""; //$NON-NLS-1$
        if (attrs != null) {
            Object o = attrs.get(IMarker.LINE_NUMBER);
            if (o instanceof Integer) {
                line = ((Integer) o).intValue();
            }
            o = attrs.get(IMarker.LOCATION);
            if (o instanceof String) {
                loc = (String) o;
            }
        }
        if (locationText != null)
            locationText.setText(MarkerUtil.getLineAndLocation(line, loc));

        markDirty();
        return;

    }

    /* (non-Javadoc)
     * Method declared on Dialog
     */
    protected void okPressed() {
        saveChanges();
        super.okPressed();
    }

    private void markDirty() {
        dirty = true;
    }

    private boolean isDirty() {
        return dirty;
    }

    /**
     * Returns whether the marker is editable.
     */
    private boolean isEditable() {
        return marker == null || MarkerUtil.isEditable(marker);
    }

    /**
     * Returns <code>true</code> if a task is being created or modified.
     * Returns <code>false</code> if a problem is being shown.
     */
    private boolean isTask() {
        return marker == null || MarkerUtil.isMarkerType(marker, IMarker.TASK);
    }

    /**
     * Saves the changes made in the dialog if needed.
     * Creates a new task if needed.
     * Updates the existing task only if there have been changes.
     * Does nothing for problems, since they cannot be modified.
     */
    private void saveChanges() {
        if (!isEditable() || !isDirty())
            return;

        final CoreException[] coreExceptions = new CoreException[1];
        final Map attrs = getMarkerAttributesFromDialog();
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                    new IRunnableWithProgress() {
                        /* (non-Javadoc)
                         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
                         */
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            try {
                                IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                                    /* (non-Javadoc)
                                     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
                                     */
                                    public void run(IProgressMonitor monitor)
                                            throws CoreException {
                                        createOrUpdateMarker(monitor, attrs);
                                    }
                                };
                                ResourcesPlugin.getWorkspace().run(runnable,
                                        monitor);
                            } catch (CoreException e) {
                                coreExceptions[0] = e;
                            }
                        }
                    });
        } catch (InvocationTargetException e) {
            IDEWorkbenchPlugin.log(e.getMessage(), StatusUtil.newStatus(
                    IStatus.ERROR, e.getMessage(), e));
            return;
        }

        catch (InterruptedException e) {
            IDEWorkbenchPlugin.log(e.getMessage(), StatusUtil.newStatus(
                    IStatus.ERROR, e.getMessage(), e));
            return;
        }

        if (coreExceptions[0] != null)
            ErrorDialog.openError(getShell(), TaskListMessages
                    .getString("TaskProp.errorMessage"), //$NON-NLS-1$
                    null, coreExceptions[0].getStatus());
    }

    /**
     * Creates or updates the marker.  Must be called within a workspace runnable.
     * @param monitor The monitor to report to.
     * @param attrs The atrributes entered from the dialog.
     * @throws CoreException
     */
    private void createOrUpdateMarker(IProgressMonitor monitor, Map attrs)
            throws CoreException {

        monitor.beginTask(TaskListMessages
                .getString("TaskPropertiesDialog.WorkingOnMarker"), 100); //$NON-NLS-1$
        if (marker == null) {
            monitor.subTask(TaskListMessages
                    .getString("TaskPropertiesDialog.CreatingMarker")); //$NON-NLS-1$
            IResource resource = getResource();
            if (resource == null) {
                resource = ResourcesPlugin.getWorkspace().getRoot();
            }
            monitor.worked(25);

            marker = resource.createMarker(IMarker.TASK);
            Map initialAttrs = getInitialAttributes();
            if (initialAttrs != null) {
                marker.setAttributes(initialAttrs);
            }
            monitor.worked(25);
        } else
            monitor.worked(50);

        // Set the marker attributes from the current dialog field values.
        // Do not use setAttributes(Map) as that overwrites any attributes
        // not covered by the dialog.

        monitor.subTask(TaskListMessages
                .getString("TaskPropertiesDialog.UpdatingAttributes")); //$NON-NLS-1$
        int increment = 50 / attrs.keySet().size();
        for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Object val = attrs.get(key);
            marker.setAttribute(key, val);
            monitor.worked(increment);
        }

        monitor.done();
    }

    /**
     * Returns the marker attributes to save back to the marker, 
     * based on the current dialog fields.
     */
    private Map getMarkerAttributesFromDialog() {
        Map attribs = new HashMap(11);
        if (isTask()) {
            attribs.put(IMarker.MESSAGE, descriptionText.getText());
            int i = priorityCombo.getSelectionIndex();
            if (i != -1) {
                attribs.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH
                        - i));
            }
            attribs.put(IMarker.DONE,
                    completedCheckbox.getSelection() ? Boolean.TRUE
                            : Boolean.FALSE);
        }
        return attribs;
    }
}