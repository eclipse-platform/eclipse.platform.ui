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


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IVariableExpander {
	/**
	 * Returns the path location to a file or directory
	 * for the given variable tag and value. The path does
	 * not need to exist.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the <code>IPath</code> to a file/directory
	 * @throws CoreException if the given variable could not be expanded
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) throws CoreException;
	/**
	 * Returns the <code>IResource</code> list
	 * for the given variable tag and value.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the list of <code>IResource</code> or <code>null</code> if not
	 * @throws CoreException if the given variable could not be expanded
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException;
	/**
	 * Returns the expanded text for the given variable
	 * tag and value.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the text of the expanded variable
	 * @throws CoreException if the given variable could not be expanded
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException;
}
