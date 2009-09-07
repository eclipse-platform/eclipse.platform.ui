/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     
 *******************************************************************************/

package org.eclipse.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateGroupOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup;

/**
 * Standard main page for a wizard that creates a group resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 * @since 3.6
 */
public class WizardNewGroupMainPage extends WizardPage implements Listener {
    private static final int SIZING_CONTAINER_GROUP_HEIGHT = 250;

    private IStructuredSelection currentSelection;

    private IFolder newGroup;

    // widgets
    private ResourceAndContainerGroup resourceGroup;

    /**
     * Creates a new group creation wizard page. If the initial resource selection 
     * contains exactly one container resource then it will be used as the default
     * container resource.
     *
     * @param pageName the name of the page
     * @param selection the current resource selection
     */
    public WizardNewGroupMainPage(String pageName,
            IStructuredSelection selection) {
        super("newGroupPage1");//$NON-NLS-1$
        setTitle(pageName);
        setDescription(IDEWorkbenchMessages.WizardNewGroupMainPage_description);
        this.currentSelection = selection;
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        // top level group
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(composite, IIDEHelpContextIds.NEW_GROUP_WIZARD_PAGE);

        resourceGroup = new ResourceAndContainerGroup(
                composite,
                this,
                IDEWorkbenchMessages.WizardNewGroupMainPage_groupName, 
                IDEWorkbenchMessages.WizardNewGroupMainPage_groupLabel, 
                false, SIZING_CONTAINER_GROUP_HEIGHT);
        resourceGroup.setAllowExistingResources(false);
        initializePage();
        validatePage();
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    /**
     * Creates a group resource handle for the group with the given workspace path.
     * This method does not create the group resource; this is the responsibility
     * of <code>createGroup</code>.
     *
     * @param groupPath the path of the group resource to create a handle for
     * @return the new group resource handle
     */
    protected IFolder createGroupHandle(IPath groupPath) {
    	return IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getFolder(groupPath);
    }

	/**
	 * Creates a new group resource in the selected container and with the
	 * selected name. Creates any missing resource containers along the path;
	 * does nothing if the container resources already exist.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish
	 * on the wizard; the enablement of the Finish button implies that all
	 * controls on this page currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this page caches the new group once it has been successfully
	 * created; subsequent invocations of this method will answer the same
	 * group resource without attempting to create it again.
	 * </p>
	 * <p>
	 * This method should be called within a workspace modify operation since it
	 * creates resources.
	 * </p>
	 * 
	 * @return the created group resource, or <code>null</code> if the group 
	 *         was not created
	 */
	public IFolder createNewGroup() {
		if (newGroup != null) {
			return newGroup;
		}

		// create the new folder and cache it if successful
		final IPath containerPath = resourceGroup.getContainerFullPath();
		IPath newFolderPath = containerPath.append(resourceGroup.getResource());
		final IFolder newFolderHandle = createGroupHandle(newFolderPath);

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				CreateGroupOperation op = new CreateGroupOperation(newFolderHandle,
						IDEWorkbenchMessages.WizardNewGroupCreationPage_title);
				try {
					// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
					// directly execute the operation so that the undo state is
					// not preserved.  Making this undoable can result in accidental
					// folder (and file) deletions.
					op.execute(monitor, WorkspaceUndoUtil
						.getUIInfoAdapter(getShell()));
				} catch (final ExecutionException e) {
					getContainer().getShell().getDisplay().syncExec(
							new Runnable() {
								public void run() {
									if (e.getCause() instanceof CoreException) {
										ErrorDialog
												.openError(
														getContainer()
																.getShell(), // Was Utilities.getFocusShell()
														IDEWorkbenchMessages.WizardNewGroupCreationPage_errorTitle,
														null, // no special message
														((CoreException) e
																.getCause())
																.getStatus());
									} else {
										IDEWorkbenchPlugin
												.log(
														getClass(),
														"createNewGroup()", e.getCause()); //$NON-NLS-1$
										MessageDialog
												.openError(
														getContainer()
																.getShell(),
														IDEWorkbenchMessages.WizardNewGroupCreationPage_internalErrorTitle,
														NLS
																.bind(
																		IDEWorkbenchMessages.WizardNewGroup_internalError,
																		e
																				.getCause()
																				.getMessage()));
									}
								}
							});
				}
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// ExecutionExceptions are handled above, but unexpected runtime
			// exceptions and errors may still occur.
			IDEWorkbenchPlugin.log(getClass(),
					"createNewGroup()", e.getTargetException()); //$NON-NLS-1$
			MessageDialog
					.openError(
							getContainer().getShell(),
							IDEWorkbenchMessages.WizardNewGroupCreationPage_internalErrorTitle,
							NLS
									.bind(
											IDEWorkbenchMessages.WizardNewGroup_internalError,
											e.getTargetException().getMessage()));
			return null;
		}

		newGroup = newFolderHandle;

		return newGroup;
	}

	/**
     * The <code>WizardNewGroupCreationPage</code> implementation of this 
     * <code>Listener</code> method handles all events and enablements for controls
     * on this page. Subclasses may extend.
     */
    public void handleEvent(Event ev) {
        setPageComplete(validatePage());
    }

    /**
     * Initializes this page's controls.
     */
    protected void initializePage() {
        Iterator it = currentSelection.iterator();
        if (it.hasNext()) {
            Object next = it.next();
            IResource selectedResource = null;
            if (next instanceof IResource) {
                selectedResource = (IResource) next;
            } else if (next instanceof IAdaptable) {
                selectedResource = (IResource) ((IAdaptable) next)
                        .getAdapter(IResource.class);
            }
            if (selectedResource != null) {
                if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}
                if (selectedResource.isAccessible()) {
					resourceGroup.setContainerFullPath(selectedResource.getFullPath());
				}
            }
        }
        setPageComplete(false);
    }

    /*
     * @see DialogPage.setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
			resourceGroup.setFocus();
		}
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    protected boolean validatePage() {
        boolean valid = true;

        if (!resourceGroup.areAllValuesValid()) {
            // if blank name then fail silently
            if (resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_RESOURCE_EMPTY
                    || resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_CONTAINER_EMPTY) {
                setMessage(resourceGroup.getProblemMessage());
                setErrorMessage(null);
            } else {
                setErrorMessage(resourceGroup.getProblemMessage());
            }
            valid = false;
        }
        // validateLinkedResource sets messages itself
        if (valid) {
            setMessage(null);
            setErrorMessage(null);
        }
        return valid;
    }

}

