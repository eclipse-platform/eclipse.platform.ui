package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;

/**
 * Responsible for expanding a variable into a list of
 * <code>IResource</code>.
 * <p>
 * Implementation of this interface will be treated like
 * a singleton. That is, only one instance will be created
 * per variable extension.
 * </p><p>
 * This interface is not to be extended by clients. Clients
 * may implement this interface.
 * </p>
 */
public interface IVariableResourceExpander {
	/**
	 * Returns the <code>IResource</code> list
	 * for the given variable tag and value.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the list of <code>IResource</code> or <code>null</code> if not
	 * 		possible (note, elements of the list can be <code>null</code>)
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context);
}
