/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *     font should be activated and used by other components.
 *******************************************************************************/

package org.eclipse.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.CreateLinkedResourceGroup;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * The NewFolderDialog is used to create a new folder.
 * The folder can optionally be linked to a file system folder.
 * <p>
 * NOTE: 
 * A linked folder can only be created at the project 
 * level. The widgets used to specify a link target are disabled 
 * if the supplied container is not a project.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewFolderDialog extends SelectionStatusDialog {
    // widgets
    private Text folderNameField;

    private Button advancedButton;

    private CreateLinkedResourceGroup linkedResourceGroup;

    private IContainer container;

    private boolean firstLinkCheck = true;

    /**
     * Parent composite of the advanced widget group for creating 
     * linked resources.
     */
    private Composite linkedResourceParent;

    /**
     * Linked resources widget group. Null if advanced section is not visible.
     */
    private Composite linkedResourceComposite;

    /**
     * Height of the dialog without the "advanced" linked resource group. 
     * Set when the advanced group is first made visible. 
     */
    private int basicShellHeight = -1;

    /**
     * Creates a NewFolderDialog
     * 
     * @param parentShell parent of the new dialog
     * @param container parent of the new folder
     */
    public NewFolderDialog(Shell parentShell, IContainer container) {
        super(parentShell);
        this.container = container;
        setTitle(IDEWorkbenchMessages.getString("NewFolderDialog.title")); //$NON-NLS-1$
        setShellStyle(getShellStyle() | SWT.RESIZE);
        setStatusLineAboveButtons(true);
    }

    /**
     * Creates the folder using the name and link target entered
     * by the user.
     * Sets the dialog result to the created folder.  
     */
    protected void computeResult() {
        String linkTarget = linkedResourceGroup.getLinkTarget();
        IFolder folder = createNewFolder(folderNameField.getText(), linkTarget);
        if (folder == null)
            return;

        setSelectionResult(new IFolder[] { folder });
    }

    /* (non-Javadoc)
     * Method declared in Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        WorkbenchHelp.setHelp(shell, IHelpContextIds.NEW_FOLDER_DIALOG);
    }

    /**
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create() {
        super.create();
        // initially disable the ok button since we don't preset the
        // folder name field
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /**
     * Creates the widget for advanced options.
     *  
     * @param parent the parent composite
     */
    protected void createAdvancedControls(Composite parent) {
        Preferences preferences = ResourcesPlugin.getPlugin()
                .getPluginPreferences();

        if (preferences.getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING) == false
                && isValidContainer()) {
            linkedResourceParent = new Composite(parent, SWT.NONE);
            linkedResourceParent.setFont(parent.getFont());
            linkedResourceParent.setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            linkedResourceParent.setLayout(layout);

            advancedButton = new Button(linkedResourceParent, SWT.PUSH);
            advancedButton.setFont(linkedResourceParent.getFont());
            advancedButton.setText(IDEWorkbenchMessages
                    .getString("showAdvanced")); //$NON-NLS-1$
            setButtonLayoutData(advancedButton);
            GridData data = (GridData) advancedButton.getLayoutData();
            data.horizontalAlignment = GridData.BEGINNING;
            advancedButton.setLayoutData(data);
            advancedButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    handleAdvancedButtonSelect();
                }
            });
        }
        linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FOLDER,
                new Listener() {
                    public void handleEvent(Event e) {
                        validateLinkedResource();
                        firstLinkCheck = false;
                    }
                });
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createFolderNameGroup(composite);
        createAdvancedControls(composite);
        return composite;
    }

    /**
     * Creates the folder name specification controls.
     *
     * @param parent the parent composite
     */
    private void createFolderNameGroup(Composite parent) {
        Font font = parent.getFont();
        // project specification group
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // new project label
        Label folderLabel = new Label(folderGroup, SWT.NONE);
        folderLabel.setFont(font);
        folderLabel.setText(IDEWorkbenchMessages
                .getString("NewFolderDialog.nameLabel")); //$NON-NLS-1$

        // new project name entry field
        folderNameField = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        folderNameField.setLayoutData(data);
        folderNameField.setFont(font);
        folderNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                validateLinkedResource();
            }
        });
    }

    /**
     * Creates a folder resource handle for the folder with the given name.
     * The folder handle is created relative to the container specified during 
     * object creation. 
     *
     * @param folderName the name of the folder resource to create a handle for
     * @return the new folder resource handle
     */
    private IFolder createFolderHandle(String folderName) {
        IWorkspaceRoot workspaceRoot = container.getWorkspace().getRoot();
        IPath folderPath = container.getFullPath().append(folderName);
        IFolder folderHandle = workspaceRoot.getFolder(folderPath);

        return folderHandle;
    }

    /**
     * Creates a new folder with the given name and optionally linking to
     * the specified link target.
     * 
     * @param folderName name of the new folder
     * @param linkTargetName name of the link target folder. may be null.
     * @return IFolder the new folder
     */
    private IFolder createNewFolder(String folderName,
            final String linkTargetName) {
        final IFolder folderHandle = createFolderHandle(folderName);

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask(IDEWorkbenchMessages
                            .getString("NewFolderDialog.progress"), 2000); //$NON-NLS-1$
                    if (monitor.isCanceled())
                        throw new OperationCanceledException();
                    if (linkTargetName == null)
                        folderHandle.create(false, true, monitor);
                    else
                        folderHandle.createLink(new Path(linkTargetName),
                                IResource.ALLOW_MISSING_LOCAL, monitor);
                    if (monitor.isCanceled())
                        throw new OperationCanceledException();
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            new ProgressMonitorJobsDialog(getShell())
                    .run(true, true, operation);
        } catch (InterruptedException exception) {
            return null;
        } catch (InvocationTargetException exception) {
            if (exception.getTargetException() instanceof CoreException) {
                ErrorDialog.openError(getShell(), IDEWorkbenchMessages
                        .getString("NewFolderDialog.errorTitle"), //$NON-NLS-1$
                        null, // no special message
                        ((CoreException) exception.getTargetException())
                                .getStatus());
            } else {
                // CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
                IDEWorkbenchPlugin.log(MessageFormat.format(
                        "Exception in {0}.createNewFolder(): {1}", //$NON-NLS-1$
                        new Object[] { getClass().getName(),
                                exception.getTargetException() }));
                MessageDialog.openError(getShell(), IDEWorkbenchMessages
                        .getString("NewFolderDialog.errorTitle"), //$NON-NLS-1$
                        IDEWorkbenchMessages.format(
                                "NewFolderDialog.internalError", //$NON-NLS-1$
                                new Object[] { exception.getTargetException()
                                        .getMessage() }));
            }
            return null;
        }
        return folderHandle;
    }

    /**
     * Shows/hides the advanced option widgets. 
     */
    protected void handleAdvancedButtonSelect() {
        Shell shell = getShell();
        Point shellSize = shell.getSize();
        Composite composite = (Composite) getDialogArea();

        if (linkedResourceComposite != null) {
            linkedResourceComposite.dispose();
            linkedResourceComposite = null;
            composite.layout();
            shell.setSize(shellSize.x, basicShellHeight);
            advancedButton.setText(IDEWorkbenchMessages
                    .getString("showAdvanced")); //$NON-NLS-1$
        } else {
            if (basicShellHeight == -1) {
                basicShellHeight = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                        true).y;
            }
            linkedResourceComposite = linkedResourceGroup
                    .createContents(linkedResourceParent);
            shellSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            shell.setSize(shellSize);
            composite.layout();
            advancedButton.setText(IDEWorkbenchMessages
                    .getString("hideAdvanced")); //$NON-NLS-1$
        }
    }

    /**
     * Returns whether the container specified in the constructor is
     * a valid parent for creating linked resources.
     * 
     * @return boolean <code>true</code> if the container specified in 
     * 	the constructor is a valid parent for creating linked resources.
     * 	<code>false</code> if no linked resources may be created with the
     * 	specified container as a parent. 
     */
    private boolean isValidContainer() {
        if (container.getType() != IResource.PROJECT)
            return false;

        try {
            IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
            IProject project = (IProject) container;
            String[] natureIds = project.getDescription().getNatureIds();

            for (int i = 0; i < natureIds.length; i++) {
                IProjectNatureDescriptor descriptor = workspace
                        .getNatureDescriptor(natureIds[i]);
                if (descriptor != null
                        && descriptor.isLinkingAllowed() == false)
                    return false;
            }
        } catch (CoreException exception) {
            // project does not exist or is closed
            return false;
        }
        return true;
    }

    /**
     * Update the dialog's status line to reflect the given status. It is safe to call
     * this method before the dialog has been opened.
     */
    protected void updateStatus(IStatus status) {
        if (firstLinkCheck && status != null) {
            // don't show the first validation result as an error.
            // fixes bug 29659
            Status newStatus = new Status(IStatus.OK, status.getPlugin(),
                    status.getCode(), status.getMessage(), status
                            .getException());
            super.updateStatus(newStatus);
        } else {
            super.updateStatus(status);
        }
    }

    /**
     * Update the dialog's status line to reflect the given status. It is safe to call
     * this method before the dialog has been opened.
     */
    private void updateStatus(int severity, String message) {
        updateStatus(new Status(severity, IDEWorkbenchPlugin.getDefault()
                .getDescriptor().getUniqueIdentifier(), severity, message, null));
    }

    /**
     * Checks whether the folder name and link location are valid.
     *
     * @return null if the folder name and link location are valid.
     * 	a message that indicates the problem otherwise.
     */
    private void validateLinkedResource() {
        boolean valid = validateFolderName();

        if (valid) {
            IFolder linkHandle = createFolderHandle(folderNameField.getText());
            IStatus status = linkedResourceGroup
                    .validateLinkLocation(linkHandle);

            if (status.getSeverity() != IStatus.ERROR)
                getOkButton().setEnabled(true);
            else
                getOkButton().setEnabled(false);

            if (status.isOK() == false)
                updateStatus(status);
        } else
            getOkButton().setEnabled(false);
    }

    /**
     * Checks if the folder name is valid.
     *
     * @return null if the new folder name is valid.
     * 	a message that indicates the problem otherwise.
     */
    private boolean validateFolderName() {
        String name = folderNameField.getText();
        IWorkspace workspace = container.getWorkspace();
        IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

        if ("".equals(name)) { //$NON-NLS-1$
            updateStatus(IStatus.ERROR, IDEWorkbenchMessages
                    .getString("NewFolderDialog.folderNameEmpty")); //$NON-NLS-1$
            return false;
        }
        if (nameStatus.isOK() == false) {
            updateStatus(nameStatus);
            return false;
        }
        IPath path = new Path(name);
        if (container.getFolder(path).exists()
                || container.getFile(path).exists()) {
            updateStatus(IStatus.ERROR, IDEWorkbenchMessages.format(
                    "NewFolderDialog.alreadyExists", new Object[] { name })); //$NON-NLS-1$
            return false;
        }
        updateStatus(IStatus.OK, ""); //$NON-NLS-1$
        return true;
    }
}