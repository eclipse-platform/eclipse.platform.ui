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
package org.eclipse.team.ui.mapping;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * A label provider wrapper that adds synchronization image and/or text decorations
 * to the image and label obtained from the delegate provider.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class SynchronizationLabelProvider extends AbstractSynchronizeLabelProvider implements ICommonLabelProvider {

	private IResourceMappingScope scope;
	private ISynchronizationContext context;
	
	private void init(IResourceMappingScope input, ISynchronizationContext context) {
		this.scope = input;
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#init(org.eclipse.ui.navigator.IExtensionStateModel, org.eclipse.jface.viewers.ITreeContentProvider)
	 */
	public void init(IExtensionStateModel aStateModel, ITreeContentProvider aContentProvider) {
		init((IResourceMappingScope)aStateModel.getProperty(ISynchronizationConstants.P_RESOURCE_MAPPING_SCOPE), (ISynchronizationContext)aStateModel.getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_CONTEXT));
		ILabelProvider provider = getDelegateLabelProvider();
		if (provider instanceof ICommonLabelProvider) {
			if (aContentProvider instanceof SynchronizationContentProvider) {
				// Assume that there is a similarly wrapped content provider and that the wrapped label provider
				// only knows about that one
				// TODO: This is kind of dangerous to build in. We need to consider alternatives
				SynchronizationContentProvider tacp = (SynchronizationContentProvider) aContentProvider;
				((ICommonLabelProvider) provider).init(aStateModel, tacp.getDelegateContentProvider());
			} else {
				((ICommonLabelProvider) provider).init(aStateModel, aContentProvider);
			}
		}
	}

	/**
	 * Return the synchronization context associated with the view to which
	 * this label provider applies. A <code>null</code> is returned if
	 * no context is available.
	 * @return the synchronization context or <code>null</code>
	 */
	public ISynchronizationContext getContext() {
		return context;
	}

	/**
	 * Return the resource mapping scope associated with the view to which
	 * this label provider applies. A <code>null</code> is returned if
	 * no scope is available.
	 * @return the resource mapping scope or <code>null</code>
	 */
	public IResourceMappingScope getScope() {
		return scope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		ILabelProvider provider = getDelegateLabelProvider();
		if (provider instanceof ICommonLabelProvider) {
			((ICommonLabelProvider) provider).restoreState(aMemento);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		ILabelProvider provider = getDelegateLabelProvider();
		if (provider instanceof ICommonLabelProvider) {
			((ICommonLabelProvider) provider).saveState(aMemento);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object anElement) {
		ILabelProvider provider = getDelegateLabelProvider();
		if (provider instanceof ICommonLabelProvider) {
			return ((ICommonLabelProvider) provider).getDescription(anElement);
		}
		return getDelegateLabelProvider().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.SynchronizationStateLabelProvider#isDecorationEnabled()
	 */
	protected boolean isDecorationEnabled() {
		return getContext() != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.SynchronizationStateLabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image image = super.getImage(element);
		if (image == null && element instanceof ModelProvider) {
			image = super.getImage(getModelRoot());
		}
		return image;
	}
	
	/**
	 * Return the root object for the model. By default, it is the
	 * workspace root. Subclasses may override. This object is used to
	 * obtain an image for the model provider.
	 * @return the root object for the model
	 */
	protected Object getModelRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
