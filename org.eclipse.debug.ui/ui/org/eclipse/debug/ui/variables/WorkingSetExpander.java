/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.variables;


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * Expands a working set type variable into the desired
 * result format.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class WorkingSetExpander extends DefaultVariableExpander {

	/**
	 * Create an instance
	 */
	public WorkingSetExpander() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IVariableResourceExpander.
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		if (varValue == null || varValue.length() == 0) {
			throwExpansionException(varTag, LaunchConfigurationsMessages.getString("WorkingSetExpander.No_working_set_specified._1")); //$NON-NLS-1$
			return null;
		}

		IWorkingSet set = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(varValue);
		if (set == null) {
			throwExpansionException(varTag, MessageFormat.format(LaunchConfigurationsMessages.getString("WorkingSetExpander.No_working_set_found_with_the_name_{0}._2"), new String[] {varValue})); //$NON-NLS-1$
			return null;
		}
			
		IAdaptable[] elements = set.getElements();
		IResource[] resources = new IResource[elements.length];
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IResource) {
				resources[i] = (IResource) adaptable;
			} else {
				resources[i] = (IResource) adaptable.getAdapter(IResource.class);
			}
		}
		
		return resources;
	}
}
