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
package org.eclipse.debug.core.variables;

public interface IContextLaunchVariableRegistry {
	/**
	 * Returns the variable for the given tag or <code>null</code> if none.
	 */
	public abstract IContextLaunchVariable getVariable(String tag);
	/**
	 * Returns the list of argument variables in the registry.
	 */
	public abstract IContextLaunchVariable[] getVariables();
}