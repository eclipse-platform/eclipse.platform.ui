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

/**
 * The registry of context launch variables contributed via
 * extension point. The singleton instance of the context
 * variable registry can be accessed from
 * <code>org.eclipse.debug.core.DebugPlugin</code>.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.variables.IContextLaunchVariable
 * @since 3.0
 */
public interface IContextLaunchVariableRegistry {
	/**
	 * Returns the variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple variables with the given name have
	 * been added to this registry, returns the most recently added variable
	 * with that name.
	 * 
	 * @param name the name of the variable
	 * @return the launch configuration variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public IContextLaunchVariable getVariable(String name);
	/**
	 * Returns all the context variables in the registry.
	 * 
	 * @return the variables in this registry
	 */
	public IContextLaunchVariable[] getVariables();
}