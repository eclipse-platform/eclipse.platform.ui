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
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffChangeEvent;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.CompareInputChangeNotifier;

/**
 * A change notifier for resource-based compare inputs.
 */
public class ResourceCompareInputChangeNotifier extends
		CompareInputChangeNotifier {

	/**
	 * Create a notifier
	 * @param context a synchronization context
	 */
	public ResourceCompareInputChangeNotifier(ISynchronizationContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		List changedInputs = new ArrayList();
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource resource = getResource(input);
			if (resource != null) {
				IResourceDelta delta = event.getDelta().findMember(resource.getFullPath());
				if (delta != null) {
					if ((delta.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) > 0
							|| (delta.getKind() & (IResourceDelta.CHANGED)) > 0 
								&& (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.REPLACED)) > 0) {
						changedInputs.add(input);
					}
				}
			}
		}
		if (!changedInputs.isEmpty())
			handleInputChanges((ICompareInput[]) changedInputs.toArray(new ICompareInput[changedInputs.size()]));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#diffsChanged(org.eclipse.team.core.diff.IDiffChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		Set changedInputs = new HashSet();
		IDiff[] added = event.getAdditions();
		for (int i = 0; i < added.length; i++) {
			IDiff diff = added[i];
			ICompareInput input = findInput(ResourceDiffTree.getResourceFor(diff));
			if (input != null)
				changedInputs.add(input);
		}
		IDiff[] changed = event.getChanges();
		for (int i = 0; i < changed.length; i++) {
			IDiff diff = changed[i];
			ICompareInput input = findInput(ResourceDiffTree.getResourceFor(diff));
			if (input != null)
				changedInputs.add(input);
		}
		IPath[] paths = event.getRemovals();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			ICompareInput input = findInput(path);
			if (input != null)
				changedInputs.add(input);
		}
		
		if (!changedInputs.isEmpty())
			handleInputChanges((ICompareInput[]) changedInputs.toArray(new ICompareInput[changedInputs.size()]));
	}
	
	private IResource getResource(ICompareInput input) {
		if (input instanceof IResourceProvider) {
			IResourceProvider rp = (IResourceProvider) input;
			return rp.getResource();
		}
		return Utils.getResource(input);
	}
	
	private ICompareInput findInput(IPath path) {
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource inputResource = getResource(input);
			if (inputResource != null && inputResource.getFullPath().equals(path)) {
				return input;
			}
		}
		return null;
	}

	private ICompareInput findInput(IResource resource) {
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource inputResource = getResource(input);
			if (inputResource != null && inputResource.equals(resource)) {
				return input;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationCompareInputChangeNotifier#isInSync(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	protected boolean isInSync(ICompareInput input) {
		IResource resource = getResource(input);
		if (resource != null) {
			return getContext().getDiffTree().getDiff(resource) == null;
		}
		return false;
	}

}
