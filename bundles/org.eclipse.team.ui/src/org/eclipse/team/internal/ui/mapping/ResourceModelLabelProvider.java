/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.team.core.delta.IDelta;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Resource label provider that can decorate using sync state.
 */
public class ResourceModelLabelProvider extends
		SynchronizationLabelProvider {

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
	protected IDelta getSyncDelta(Object element) {
		IResource resource = getResource(element);
		if (resource != null) {
			ISynchronizationContext context = getContext();
			if (context != null) {
				IDelta delta = context.getDeltaTree().getDelta(resource.getFullPath());
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object anElement) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.SynchronizationOperationLabelProvider#init(org.eclipse.ui.navigator.IExtensionStateModel, org.eclipse.jface.viewers.ITreeContentProvider)
	 */
	public void init(IExtensionStateModel aStateModel, ITreeContentProvider aContentProvider) {
		// TODO Auto-generated method stub
		super.init(aStateModel, aContentProvider);
	}
}
