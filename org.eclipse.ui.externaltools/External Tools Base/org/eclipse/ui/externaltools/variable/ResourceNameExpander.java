package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IResource;

/**
 * Extracts the resource name from a variable context
 */
public class ResourceNameExpander extends DefaultVariableExpander {

	/**
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		IResource resource= context.getSelectedResource();
		if (resource != null) {
			return resource.getName();
		}
		return null;
	}
}
