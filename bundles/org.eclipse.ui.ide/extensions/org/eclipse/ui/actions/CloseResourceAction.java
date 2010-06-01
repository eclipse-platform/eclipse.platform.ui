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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for closing the currently selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CloseResourceAction extends WorkspaceAction implements
        IResourceChangeListener {
    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".CloseResourceAction"; //$NON-NLS-1$
	private String[] modelProviderIds;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @deprecated See {@link #CloseResourceAction(IShellProvider)}
     */
    public CloseResourceAction(Shell shell) {
        super(shell, IDEWorkbenchMessages.CloseResourceAction_text);
        initAction();
    }

    /**
	 * Override super constructor to allow subclass to 
	 * override with unique text.
	 * @deprecated See {@link #CloseResourceAction(IShellProvider, String)}
	 */
    protected CloseResourceAction(Shell shell, String text) {
    	super(shell, text);
    }
    
    /**
	 * Create the new action.
	 * 
	 * @param provider
	 *            the shell provider for any dialogs
	 * @since 3.4
	 */
    public CloseResourceAction(IShellProvider provider) {
    	super(provider, IDEWorkbenchMessages.CloseResourceAction_text);
        initAction();
    }
    
    /**
	 * Provide text to the action.
	 * 
	 * @param provider
	 *            the shell provider for any dialogs
	 * @param text
	 *            label
	 * @since 3.4
	 */
    protected CloseResourceAction(IShellProvider provider, String text) {
    	super(provider, text);
    }

	private void initAction() {
		setId(ID);
        setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.CLOSE_RESOURCE_ACTION);
	}
    
    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getOperationMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_operationMessage;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_problemMessage;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsTitle() {
        return IDEWorkbenchMessages.CloseResourceAction_title;
    }

    protected void invokeOperation(IResource resource, IProgressMonitor monitor)
	        throws CoreException {
	    ((IProject) resource).close(monitor);
	}

    /** 
     * The implementation of this <code>WorkspaceAction</code> method
     * method saves and closes the resource's dirty editors before closing 
     * it.
     */
    public void run() {
        // Get the items to close.
        List projects = getSelectedResources();
        if (projects == null || projects.isEmpty()) {
			// no action needs to be taken since no projects are selected
            return;
		}

		IResource[] projectArray = (IResource[]) projects
				.toArray(new IResource[projects.size()]);

		if (!IDE.saveAllEditors(projectArray, true)) {
			return;
		}
        if (!validateClose()) {
        	return;
        }
        //be conservative and include all projects in the selection - projects
        //can change state between now and when the job starts
    	ISchedulingRule rule = null;
    	IResourceRuleFactory factory = ResourcesPlugin.getWorkspace().getRuleFactory();
        Iterator resources = getSelectedResources().iterator();
        while (resources.hasNext()) {
            IProject project = (IProject) resources.next();
       		rule = MultiRule.combine(rule, factory.modifyRule(project));
        }
        runInBackground(rule);
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected boolean shouldPerformResourcePruning() {
        return false;
    }

    /**
     * The <code>CloseResourceAction</code> implementation of this
     * <code>SelectionListenerAction</code> method ensures that this action is
     * enabled only if one of the selections is an open project.
     */
    protected boolean updateSelection(IStructuredSelection s) {
        // don't call super since we want to enable if open project is selected.
        if (!selectionIsOfType(IResource.PROJECT)) {
			return false;
		}

        Iterator resources = getSelectedResources().iterator();
        while (resources.hasNext()) {
            IProject currentResource = (IProject) resources.next();
            if (currentResource.isOpen()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a resource changed event by updating the enablement
     * if one of the selected projects is opened or closed.
     */
    public synchronized void resourceChanged(IResourceChangeEvent event) {
        // Warning: code duplicated in OpenResourceAction
        List sel = getSelectedResources();
        // don't bother looking at delta if selection not applicable
        if (selectionIsOfType(IResource.PROJECT)) {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
                IResourceDelta[] projDeltas = delta
                        .getAffectedChildren(IResourceDelta.CHANGED);
                for (int i = 0; i < projDeltas.length; ++i) {
                    IResourceDelta projDelta = projDeltas[i];
                    if ((projDelta.getFlags() & IResourceDelta.OPEN) != 0) {
                        if (sel.contains(projDelta.getResource())) {
                            selectionChanged(getStructuredSelection());
                            return;
                        }
                    }
                }
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionListenerAction#getSelectedResources()
     */
    protected synchronized List getSelectedResources() {
    	return super.getSelectedResources();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionListenerAction#getSelectedNonResources()
     */
    protected synchronized List getSelectedNonResources() {
    	return super.getSelectedNonResources();
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
	
	/**
	 * Validates the operation against the model providers.
	 * 
	 * @return whether the operation should proceed
	 */
    private boolean validateClose() {
    	IResourceChangeDescriptionFactory factory = ResourceChangeValidator.getValidator().createDeltaFactory();
    	List resources = getActionResources();
    	for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				factory.close(project);
			}
		}
    	String message;
    	if (resources.size() == 1) {
    		message = NLS.bind(IDEWorkbenchMessages.CloseResourceAction_warningForOne, ((IResource)resources.get(0)).getName());
    	} else {
    		message = IDEWorkbenchMessages.CloseResourceAction_warningForMultiple;
    	}
		return IDE.promptToConfirm(getShell(), IDEWorkbenchMessages.CloseResourceAction_confirm, message, factory.getDelta(), getModelProviderIds(), false /* no need to syncExec */);
	}
}
