package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IPath;

/**
 * Responsible for expanding a variable into the location
 * path of a file or directory.
 * <p>
 * Implementation of this interface will be treated like
 * a singleton. That is, only one instance will be created
 * per variable extension.
 * </p><p>
 * This interface is not to be extended by clients. Clients
 * may implement this interface.
 * </p>
 */
public interface IVariableLocationExpander {
	/**
	 * Returns the path location to a file or directory
	 * for the given variable tag and value. The path does
	 * not need to exist.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the <code>IPath</code> to a file/directory
	 * 		or <code>null</code> if not possible
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context);
}
