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
package org.eclipse.team.examples.model.ui;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.team.examples.model.ModelObject;
import org.eclipse.team.examples.model.ModelWorkspace;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

/**
 * Model content provider for use with the Common Navigator framework.
 * It makes use of an IWorkbenchAdapter to get the children and parent 
 * of model objects.
 */
public class ModelNavigatorContentProvider extends BaseWorkbenchContentProvider
		implements ICommonContentProvider {

	private ICommonContentExtensionSite extensionSite;
	private boolean isWorkspaceRoot;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite aConfig) {
		extensionSite = aConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		// Nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		// Nothing to do
	}

	/**
	 * Return the extension site for this label provider.
	 * @return the extension site for this label provider
	 */
	public ICommonContentExtensionSite getExtensionSite() {
		return extensionSite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		// Since we are used in the project explorer, the root may be 
		// an IWorkspaceRoot. We need to change it to the ModelWorkspace
		if (element instanceof IWorkspaceRoot) {
			isWorkspaceRoot = true;
			return super.getElements(ModelObject.create((IWorkspaceRoot)element));
			
		}
		return super.getElements(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		Object parent = super.getParent(element);
		if (isWorkspaceRoot && parent instanceof ModelWorkspace) {
			return ((ModelWorkspace)parent).getResource();
		}
		return parent;
	}

}
