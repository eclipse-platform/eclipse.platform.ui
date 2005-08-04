/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.internal.resources.mapping.ResourceMapping;
import org.eclipse.core.internal.resources.mapping.ResourceTraversal;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard;
import org.eclipse.team.internal.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.ui.PlatformUI;

/**
 * Action for checking in files to a CVS provider.
 * Prompts the user for a release comment.
 */
public class CommitAction extends WorkspaceTraversalAction {
	
	/*
	 * @see CVSAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		final IResource [][] resources = new IResource[][] { null };
		 
		PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					resources[0] = getDeepResourcesToCommit(monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		});

        run(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
            try {
                CommitWizard.run(shell, resources[0]);

            } catch (CoreException e) {
                throw new InvocationTargetException(e);
            }

            }
        }, false, PROGRESS_BUSYCURSOR);
	}
    
    private IResource[] getDeepResourcesToCommit(IProgressMonitor monitor) throws CoreException {
        ResourceMapping[] mappings = getCVSResourceMappings();
        List roots = new ArrayList();
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            ResourceTraversal[] traversals = mapping.getTraversals(
            		SubscriberResourceMappingContext.getCheckInContext(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber()), 
            		monitor);
            for (int j = 0; j < traversals.length; j++) {
                ResourceTraversal traversal = traversals[j];
                IResource[] resources = traversal.getResources();
                if (traversal.getDepth() == IResource.DEPTH_INFINITE) {
                    roots.addAll(Arrays.asList(resources));
                } else if (traversal.getDepth() == IResource.DEPTH_ZERO) {
                    collectShallowFiles(resources, roots);
                } else if (traversal.getDepth() == IResource.DEPTH_ONE) {
                    collectShallowFiles(resources, roots);
                    for (int k = 0; k < resources.length; k++) {
                        IResource resource = resources[k];
                        if (resource.getType() != IResource.FILE) {
                            collectShallowFiles(members(resource), roots);
                        }
                    }
                }
            }
        }
        return (IResource[]) roots.toArray(new IResource[roots.size()]);
    }

    private IResource[] members(IResource resource) throws CoreException {
        return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().members(resource);
    }

    private void collectShallowFiles(IResource[] resources, List roots) {
        for (int k = 0; k < resources.length; k++) {
            IResource resource = resources[k];
            if (resource.getType() == IResource.FILE)
                roots.add(resource);
        }
    }
    
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return CVSUIMessages.CommitAction_commitFailed; 
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getId()
	 */
	public String getId() {
		return ICVSUIConstants.CMD_COMMIT;
}
}
