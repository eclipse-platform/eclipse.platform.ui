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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.team.ui.mapping.IResourceMappingScope;
import org.eclipse.team.ui.operations.ScopeGenerator;
import org.eclipse.ui.PlatformUI;


/**
 * A specialized workspace actions that operates on resource traversals
 * instead of resources/
 */
public abstract class WorkspaceTraversalAction extends WorkspaceAction {

    /**
     * Return the selected mappings that contain resources 
     * within a CVS managed project.
     * @return the selected mappings that contain resources 
     * within a CVS managed project
     * 
     * @deprecated should use getOperationInput() instead
     */
    protected ResourceMapping[] getCVSResourceMappings() {
        ResourceMapping[] selectedMappings = getSelectedResourceMappings(CVSProviderPlugin.getTypeId());
        try {
			IResourceMappingScope scope = new ScopeGenerator().prepareScope("CVS Operation", selectedMappings, getResourceMappingContext(), new NullProgressMonitor());
			if (scope.hasAdditionalMappings()) {
				return showAllMappings(scope);
			}
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
		}
		return selectedMappings;
    }
    
    private ResourceMapping[] showAllMappings(final IResourceMappingScope scope) {
//        final boolean[] canceled = new boolean[] { false };
//        getShell().getDisplay().syncExec(new Runnable() {
//            public void run() {
//                AdditionalMappingsDialog dialog = new AdditionalMappingsDialog(getShell(), "Participating Elements", scope);
//                int result = dialog.open();
//                canceled[0] = result != Window.OK;
//            }
//        
//        });
//        
//        if (canceled[0]) {
//            return new ResourceMapping[0];
//        }
        return scope.getMappings();
    }

    protected static IResource[] getRootTraversalResources(ResourceMapping[] mappings, ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
        List result = new ArrayList();
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            ResourceTraversal[] traversals = mapping.getTraversals(context, monitor);
            for (int j = 0; j < traversals.length; j++) {
                ResourceTraversal traversal = traversals[j];
                IResource[] resources = traversal.getResources();
                for (int k = 0; k < resources.length; k++) {
                    IResource resource = resources[k];
                    if (RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) != null) {
                        result.add(resource);
                    }
                }
            }
        }
        return (IResource[]) result.toArray(new IResource[result.size()]);
    }

    protected Subscriber getWorkspaceSubscriber() {
        return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
    }
    
    protected IResource[] getResourcesToCompare(final Subscriber subscriber) throws InvocationTargetException {
        return getResourcesToCompare(getCVSResourceMappings(), subscriber);
    }
    
    protected ResourceMappingContext getResourceMappingContext() {
		return SubscriberResourceMappingContext.getCompareContext(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber());
	}

	public static IResource[] getResourcesToCompare(final ResourceMapping[] mappings, final Subscriber subscriber) throws InvocationTargetException {
        // Determine what resources need to be synchronized.
        // Use a resource mapping context to include any relevant remote resources
        final IResource[][] resources = new IResource[][] { null };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        resources[0] = getRootTraversalResources(
                                mappings, 
                                SubscriberResourceMappingContext.getCompareContext(subscriber), 
                                monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            
            });
        } catch (InterruptedException e) {
            // Canceled
            return null;
        }
        return resources[0];
    }
    
    public static IResource[] getProjects(IResource[] resources) {
        Set projects = new HashSet();
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            projects.add(resource.getProject());
        }
        return (IResource[]) projects.toArray(new IResource[projects.size()]);
    }
    
    public static boolean isLogicalModel(ResourceMapping[] mappings) {
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            if (! (mapping.getModelObject() instanceof IResource) ) {
                return true;
            }
        }
        return false;
    }
}
