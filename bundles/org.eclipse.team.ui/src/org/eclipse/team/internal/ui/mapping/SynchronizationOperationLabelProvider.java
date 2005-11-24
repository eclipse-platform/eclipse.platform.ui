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

import org.eclipse.jface.viewers.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.IResourceMappingScope;
import org.eclipse.team.ui.mapping.ISynchronizationContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

public abstract class SynchronizationOperationLabelProvider extends SynchronizationStateLabelProvider implements ICommonLabelProvider {

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
		init((IResourceMappingScope)aStateModel.getProperty(TeamUI.RESOURCE_MAPPING_SCOPE), (ISynchronizationContext)aStateModel.getProperty(TeamUI.SYNCHRONIZATION_CONTEXT));
		ILabelProvider provider = getDelegateLabelProvider();
		if (provider instanceof ICommonLabelProvider) {
			if (aContentProvider instanceof AbstractTeamAwareContentProvider) {
				// Assume that there is a similary wrapped content provider and that the wrapped label provider
				// only knows about that one
				// TODO: This is kind of dangerous to build in. We need to consider alternatives
				AbstractTeamAwareContentProvider tacp = (AbstractTeamAwareContentProvider) aContentProvider;
				((ICommonLabelProvider) provider).init(aStateModel, tacp.getDelegateContentProvider());
			} else {
				((ICommonLabelProvider) provider).init(aStateModel, aContentProvider);
			}
		}
	}

	/**
	 * Return the synchronization context associated with the view to which
	 * this label provider applies.
	 * @return the synchronization context
	 */
	public ISynchronizationContext getContext() {
		return context;
	}

	/**
	 * Return the resource mapping scope associated with the view to which
	 * this label provider applies.
	 * @return the esource mapping scope
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
}
