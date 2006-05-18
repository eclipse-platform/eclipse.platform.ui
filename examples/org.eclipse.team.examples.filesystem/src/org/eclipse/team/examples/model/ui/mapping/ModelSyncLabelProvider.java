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
package org.eclipse.team.examples.model.ui.mapping;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.examples.model.ModelResource;
import org.eclipse.team.examples.model.ui.ModelNavigatorLabelProvider;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

/**
 * The label provider that is used for synchronizations.
 */
public class ModelSyncLabelProvider extends SynchronizationLabelProvider {
	
	private ModelNavigatorLabelProvider delegate;

	public ModelSyncLabelProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationLabelProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		delegate = new ModelNavigatorLabelProvider();
		delegate.init(site);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (delegate != null)
			delegate.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#getDelegateLabelProvider()
	 */
	protected ILabelProvider getDelegateLabelProvider() {
		return delegate;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#getDiff(java.lang.Object)
	 */
	protected IDiff getDiff(Object element) {
		if (element instanceof ModelResource) {
			ModelResource mr = (ModelResource) element;
			return getContext().getDiffTree().getDiff(mr.getResource());
		}
		return super.getDiff(element);
	}

}
