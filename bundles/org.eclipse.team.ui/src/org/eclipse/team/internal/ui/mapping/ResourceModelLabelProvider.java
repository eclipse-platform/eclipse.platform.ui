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
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;

/**
 * Resource label provider that can decorate using sync state.
 */
public class ResourceModelLabelProvider extends
		SynchronizationLabelProvider implements IFontProvider {

	private ILabelProvider provider = new ResourceMappingLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.SynchronizationOperationLabelProvider#getBaseLabelProvider()
	 */
	protected ILabelProvider getDelegateLabelProvider() {
		return provider ;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.AbstractSynchronizationLabelProvider#getSyncDelta(java.lang.Object)
	 */
	protected IDiffNode getDiff(Object element) {
		IResource resource = getResource(element);
		if (resource != null) {
			ISynchronizationContext context = getContext();
			if (context != null) {
				IDiffNode delta = context.getDiffTree().getDiff(resource.getFullPath());
				return delta;
			}
		}
			
		return null;
	}

	private IResource getResource(Object element) {
		if (element instanceof IResource) {
			return (IResource) element;
		}
		return null;
	}
	
	protected boolean isBusy(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			ISynchronizationContext context = getContext();
			if (context != null)
				return context.getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_BUSY_HINT);
		}
		return super.isBusy(element);
	}
	
	protected boolean hasDecendantConflicts(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			ISynchronizationContext context = getContext();
			if (context != null)
				return context.getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_HAS_DESCENDANT_CONFLICTS);
		}
		return super.hasDecendantConflicts(element);
	}
}
