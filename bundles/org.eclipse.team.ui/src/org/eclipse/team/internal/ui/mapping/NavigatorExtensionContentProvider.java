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

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * This content provider acts as the root of a team view that can contain
 * content from multiple models. It return all the model providers in the current scope 
 * as the children of the worksace root.
 */
public class NavigatorExtensionContentProvider extends WorkbenchContentProvider implements ICommonContentProvider {

	private IExtensionStateModel stateModel;
	private IResourceMappingScope scope;

	public NavigatorExtensionContentProvider() {
		// Initialization done in the init method
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.ICommonContentProvider#init(org.eclipse.ui.navigator.IExtensionStateModel, org.eclipse.ui.IMemento)
	 */
	public void init(IExtensionStateModel aStateModel, IMemento aMemento) {
		stateModel = aStateModel;
		Object o = stateModel.getProperty(TeamUI.RESOURCE_MAPPING_SCOPE);
		if (o instanceof IResourceMappingScope) {
			scope = (IResourceMappingScope) o;
		}
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (element instanceof IWorkspaceRoot && scope != null) {
			return scope.getModelProviders();
		}
		return super.getChildren(element);
	}

}
