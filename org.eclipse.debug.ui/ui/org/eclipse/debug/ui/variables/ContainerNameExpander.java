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


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;

/**
 * Extracts the container name from a variable context
 */
public class ContainerNameExpander extends DefaultVariableExpander {

	/**
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IResource resource= context.getSelectedResource();
		if (resource != null) {
			IContainer parent= resource.getParent();
			if (parent != null) {
				return parent.getName();
			}
			throwExpansionException(varTag, LaunchConfigurationsMessages.getString("ContainerNameExpander.No_container_could_be_determined_for_the_selected_resource._1")); //$NON-NLS-1$
		}
		throwExpansionException(varTag, LaunchConfigurationsMessages.getString("ContainerNameExpander.No_resource_selected._2")); //$NON-NLS-1$
		return null;
	}

}
