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
package org.eclipse.debug.internal.core.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.variables.ExpandVariableContext;

/**
 * Extracts the container name from a variable context.
 * @since 3.0
 */
public class ContainerNameExpander extends ContainerExpander {

	/**
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IResource resource= expand(varValue, context);
		if (resource != null) {
			return resource.getName();
		}
		throwExpansionException(varTag, LaunchVariableMessages.getString("ContainerNameExpander.0")); //$NON-NLS-1$
		return null;
	}

}
