package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * Expands a working set type variable into the desired
 * result format.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class WorkingSetExpander implements IVariableResourceExpander {

	/**
	 * Create an instance
	 */
	public WorkingSetExpander() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IVariableResourceExpander.
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) {
		if (varValue == null || varValue.length() == 0)
			return null;

		IWorkingSet set = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(varValue);
		if (set == null)
			return null;
			
		IAdaptable[] elements = set.getElements();
		IResource[] resources = new IResource[elements.length];
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IResource)
				resources[i] = (IResource) adaptable;
			else
				resources[i] = (IResource) adaptable.getAdapter(IResource.class);
		}
		
		return resources;
	}
}
