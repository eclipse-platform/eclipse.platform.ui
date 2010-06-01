/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for copying the currently selected resources elsewhere
 * in the workspace. All resources being copied as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyResourceAction extends SelectionListenerAction implements
        ISelectionValidator {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".CopyResourceAction"; //$NON-NLS-1$

    /**
     * The IShellProvider in which to show any dialogs.
     */
    protected IShellProvider shellProvider;

    /**
     * The operation to run.  This is created only during the life-cycle of the
     * run method.
     */
    protected CopyFilesAndFoldersOperation operation;

	private String[] modelProviderIds;

    /**
     * Returns a new name for a copy of the resource at the given path in the given
     * workspace. This name could be determined either automatically or by querying
     * the user. This name will <b>not</b> be verified by the caller, so it must be
     * valid and unique.
     * <p>
     * Note this method is for internal use only.
     * </p>
     *
     * @param originalName the full path of the resource
     * @param workspace the workspace
     * @return the new full path for the copy, or <code>null</code> if the resource
     *   should not be copied
     */
    public static IPath getNewNameFor(IPath originalName, IWorkspace workspace) {
        return CopyFilesAndFoldersOperation.getAutoNewNameFor(originalName,
                workspace);
    }

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * 
     * @deprecated {@link #CopyResourceAction(IShellProvider)}
     */
    public CopyResourceAction(Shell shell) {
        this(shell, IDEWorkbenchMessages.CopyResourceAction_title);
    }
    
    /**
     * Creates a new action
     * 
     * @param provider the shell for any dialogs
     * @since 3.4
     */
    public CopyResourceAction(IShellProvider provider){
    	this(provider, IDEWorkbenchMessages.CopyResourceAction_title);
    }

    /**
     * Creates a new action with the given text.
     *
     * @param shell the shell for any dialogs
     * @param name the string used as the name for the action, 
     *   or <code>null</code> if there is no name
     *   
     * @deprecated {@link #CopyResourceAction(IShellProvider, String)}
     */
    CopyResourceAction(final Shell shell, String name) {
        super(name);
        Assert.isNotNull(shell);
        shellProvider = new IShellProvider(){
        	public Shell getShell(){
        		return shell;
        	}
        };
        initAction();
    }
    
    /**
     * Creates a new action with the given text
     * 
     * @param provider the shell for any dialogs
     * @param name the string used as the name for the action, 
     *   or <code>null</code> if there is no name
     */
    CopyResourceAction(IShellProvider provider, String name){
    	super(name);
        Assert.isNotNull(provider);
        shellProvider = provider;
        initAction();
    }

    /**
     * Returns the operation to perform when this action runs.
     * 
     * @return the operation to perform when this action runs.
     */
    protected CopyFilesAndFoldersOperation createOperation() {
        return new CopyFilesAndFoldersOperation(getShell());
    }
    
    private void initAction(){
    	setToolTipText(IDEWorkbenchMessages.CopyResourceAction_toolTip);
        setId(CopyResourceAction.ID);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.COPY_RESOURCE_ACTION);
    }

    /**
     * Returns the path of the container to initially select in the container
     * selection dialog, or <code>null</code> if there is no initial selection
     * @return The initial container; <code>null</code> if none.
     */
    IContainer getInitialContainer() {
        List resources = getSelectedResources();
        if (resources.size() > 0) {
            IResource resource = (IResource) resources.get(0);
            return resource.getParent();
        }
        return null;
    }

    /**
     * Returns an array of resources to use for the operation from 
     * the provided list.
     * 
     * @param resourceList The list of resources to converted into an array.
     * @return an array of resources to use for the operation
     */
    protected IResource[] getResources(List resourceList) {
        return (IResource[]) resourceList.toArray(new IResource[resourceList
                .size()]);
    }

    /**
     * Returns the shell in which to show any dialogs
     * @return The shell for parenting dialogs; never <code>null</code>.
     */
    Shell getShell() {
        return shellProvider.getShell();
    }

    /**
     * The <code>CopyResourceAction</code> implementation of this 
     * <code>ISelectionValidator</code> method checks whether the given path
     * is a good place to copy the selected resources.
     */
    public String isValid(Object destination) {
        IWorkspaceRoot root = IDEWorkbenchPlugin.getPluginWorkspace().getRoot();
        IContainer container = (IContainer) root
                .findMember((IPath) destination);

        if (container != null) {
            // create a new operation here. 
            // isValid is API and may be called in any context.
            CopyFilesAndFoldersOperation newOperation = createOperation();
            List sources = getSelectedResources();
            IResource[] resources = (IResource[]) sources
                    .toArray(new IResource[sources.size()]);
            return newOperation.validateDestination(container, resources);
        }
        return null;
    }

    /**
     * Asks the user for the destination of this action.
     *
     * @return the path on an existing or new resource container, or 
     *  <code>null</code> if the operation should be abandoned
     */
    IPath queryDestinationResource() {
        // start traversal at root resource, should probably start at a
        // better location in the tree
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(shellProvider.getShell(),
                getInitialContainer(), true, IDEWorkbenchMessages.CopyResourceAction_selectDestination);
        dialog.setValidator(this);
        dialog.showClosedProjects(false);
        dialog.open();
        Object[] result = dialog.getResult();
        if (result != null && result.length == 1) {
            return (IPath) result[0];
        }
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        try {
            operation = createOperation();
            operation.setModelProviderIds(getModelProviderIds());

            // WARNING: do not query the selected resources more than once
            // since the selection may change during the run, 
            // e.g. due to window activation when the prompt dialog is dismissed.
            // For more details, see Bug 60606 [Navigator] (data loss) Navigator deletes/moves the wrong file
            List sources = getSelectedResources();

            IPath destination = queryDestinationResource();
            if (destination == null) {
				return;
			}

            IWorkspaceRoot root = IDEWorkbenchPlugin.getPluginWorkspace()
                    .getRoot();
            IContainer container = (IContainer) root.findMember(destination);
            if (container == null) {
                return;
            }

            runOperation(getResources(sources), container);
        } finally {
            operation = null;
        }
    }

    /**
     * Runs the operation created in <code>createOperation</code>
     * 
     * @param resources source resources to pass to the operation
     * @param destination destination container to pass to the operation
     */
    protected void runOperation(IResource[] resources, IContainer destination) {
        operation.copyResources(resources, destination);
    }

    /**
     * The <code>CopyResourceAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables this action only if 
     * all of the one or more selections are sibling resources which are 
     * local (depth infinity).
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        if (!super.updateSelection(selection)) {
            return false;
        }
        if (getSelectedNonResources().size() > 0) {
            return false;
        }

        // to enable this command all selected resources must be siblings
        List selectedResources = getSelectedResources();
        if (selectedResources.size() == 0) {
			return false;
		}
        IContainer firstParent = ((IResource) selectedResources.get(0))
                .getParent();
        if (firstParent == null) {
            return false;
        }
        Iterator resourcesEnum = selectedResources.iterator();
        while (resourcesEnum.hasNext()) {
            IResource currentResource = (IResource) resourcesEnum.next();
            if (!currentResource.exists()) {
                return false;
            }
            if (currentResource.getType() == IResource.PROJECT) {
                return false;
            }
            IContainer parent = currentResource.getParent();
            if ((parent != null) && (!parent.equals(firstParent))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the model provider ids that are known to the client
     * that instantiated this operation.
     * 
     * @return the model provider ids that are known to the client
     * that instantiated this operation.
     * @since 3.2
     */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
     * Sets the model provider ids that are known to the client
     * that instantiated this operation. Any potential side effects
     * reported by these models during validation will be ignored.
     * 
	 * @param modelProviderIds the model providers known to the client
	 * who is using this operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}
}
