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

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.CacheBaseContentsOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CacheRemoteContentsOperation;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action that runs an update without prompting the user for a tag.
 * 
 * @since 3.1
 */
public class UpdateModelAction extends WorkspaceTraversalAction {
    
	private class CVSMergeOperation extends ResourceMappingMergeOperation {

		protected CVSMergeOperation(IWorkbenchPart part, IResourceMappingOperationInput input) {
			super(part, input);
		}

		protected IMergeContext buildMergeContext(IProgressMonitor monitor) {
			monitor.beginTask(null, 100);
			IMergeContext context = CVSMergeContext.createContext(getInput(), Policy.subMonitorFor(monitor, 50));
			// cache the base and remote contents
			// TODO: Refreshing and caching now takes 3 round trips.
			// OPTIMIZE: remote state and contents could be obtained in 1
			// OPTIMIZE: Based could be avoided if we always cached base locally
			try {
				new CacheBaseContentsOperation(getPart(), getInput().getInputMappings(), context.getSyncInfoTree(), true).run(Policy.subMonitorFor(monitor, 25));
				new CacheRemoteContentsOperation(getPart(), getInput().getInputMappings(), context.getSyncInfoTree()).run(Policy.subMonitorFor(monitor, 25));
			} catch (InvocationTargetException e) {
				CVSUIPlugin.log(CVSException.wrapException(e));
			} catch (InterruptedException e) {
				// Ignore
			}
			monitor.done();
			return context;
		}

		protected void requiresManualMerge(ModelProvider[] providers, IMergeContext context) throws CoreException {
			// TODO Auto-generated method stub
		}

	}
	
    /*
     * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
     */
    protected boolean isEnabledForAddedResources() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
     */
    protected boolean isEnabledForNonExistantResources() {
        return true;
    }
    
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		new CVSMergeOperation(getTargetPart(), getOperationInput()).run();
	}

	public String getId() {
		return "org.eclipse.team.cvs.ui.modelupdate";
	}
}
