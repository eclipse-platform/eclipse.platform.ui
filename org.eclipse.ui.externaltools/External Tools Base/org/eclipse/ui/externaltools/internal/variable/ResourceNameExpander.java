package org.eclipse.ui.externaltools.internal.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Extracts the resource name from a variable context
 */
public class ResourceNameExpander extends DefaultVariableExpander {

	/**
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IResource resource= context.getSelectedResource();
		if (resource != null) {
			return resource.getName();
		}
		throwExpansionException(varTag, "No resource selected.");
		return null;
	}
}
