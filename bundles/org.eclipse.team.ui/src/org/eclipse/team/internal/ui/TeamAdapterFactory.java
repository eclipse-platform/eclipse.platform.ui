/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.internal.ui.mapping.INavigatorContentExtensionFactory;
import org.eclipse.team.internal.ui.synchronize.DiffNodeWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class TeamAdapterFactory implements IAdapterFactory {

	private DiffNodeWorkbenchAdapter diffNodeAdapter = new DiffNodeWorkbenchAdapter();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof DiffNode && adapterType == IWorkbenchAdapter.class) {
			return diffNodeAdapter;
		}
		if (adaptableObject instanceof ModelProvider && adapterType == INavigatorContentExtensionFactory.class) {
			return new ResourceNavigatorContentExtensionFactory();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		// TODO Auto-generated method stub
		return new Class[] {IWorkbenchAdapter.class, INavigatorContentExtensionFactory.class};
	}
}
