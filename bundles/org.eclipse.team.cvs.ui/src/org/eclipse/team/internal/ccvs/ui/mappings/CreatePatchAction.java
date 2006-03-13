/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

public class CreatePatchAction extends CVSModelProviderAction {

	public CreatePatchAction(ISynchronizePageConfiguration configuration) {
		super(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		try {
			IResource[] resources = getVisibleResources(selection);
			return resources.length > 0;
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
			return false;
		}
	}
	
    private IResource[] getVisibleResources(IStructuredSelection selection) throws CoreException {
    	final Set resources = new HashSet();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			// Only enable if all elements map directly to a resource
			IResource resource = Utils.getResource(element);
			if (resource == null)
			return new IResource[0];
			final IResourceDiffTree diffTree = getSynchronizationContext().getDiffTree();
			IDiff[] diffs = diffTree.getDiffs(resource, IResource.DEPTH_INFINITE);
			for (int i = 0; i < diffs.length; i++) {
				IDiff diff = diffs[i];
				IResource child = diffTree.getResource(diff);
				if (child.getType() == IResource.FILE && diff instanceof IThreeWayDiff) {
					IThreeWayDiff twd = (IThreeWayDiff) diff;
					IDiff local = twd.getLocalChange();
					if (local != null && local.getKind() != IDiff.NO_CHANGE) {
						resources.add(child);
					}
				}
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.mappings.CVSModelProviderAction#getBundleKeyPrefix()
     */
    protected String getBundleKeyPrefix() {
    	return "GenerateDiffFileAction."; //$NON-NLS-1$
    }
    
    public void run() {
    	final ResourceTraversal [][] traversals = new ResourceTraversal[][] { null };
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						traversals[0] = getResourceTraversals(getStructuredSelection(), monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handleError(getConfiguration().getSite().getShell(), e, null, null);
		} catch (InterruptedException e) {
			// Ignore
		}
		if (traversals[0] != null) {
			IResource[] resources = getVisibleResources(traversals[0]);
			GenerateDiffFileWizard.run(getConfiguration().getSite().getPart(), resources, false);
		}
    }

	private IResource[] getVisibleResources(ResourceTraversal[] traversals) {
		final Set resources = new HashSet();
		final IResourceDiffTree diffTree = getSynchronizationContext().getDiffTree();
		IDiff[] diffs = diffTree.getDiffs(traversals);
		for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource child = diffTree.getResource(diff);
			if (child.getType() == IResource.FILE && diff instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) diff;
				IDiff local = twd.getLocalChange();
				if (local != null && local.getKind() != IDiff.NO_CHANGE) {
					resources.add(child);
				}
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

}
