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
 * The registry of simple variables defined by the user and
 * contributed via extension point. The singleton instance of
 * the simple variable registry can be accessed from
 * <code>org.eclipse.debug.core.DebugPlugin</code>.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.variables.ISimpleLaunchVariable
 * @since 3.0
 */
public interface ISimpleVariableRegistry {
	/**
	 * Adds the given variables to this variable registry
	 * @param variables the variables to add
	 */
	public void addVariables(ISimpleLaunchVariable[] variables);
	/**
	 * Removes the given variables from this registry. Has no effect
	 * if any of the given variables are not in this registry.
	 * @param variables the variables to remove
	 */
	public void removeVariables(ISimpleLaunchVariable[] variables);
	/**
	 * Returns the variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple variables with the given name have
	 * been added to this registry, returns the most recently added variable
	 * with that name.
	 * @param name the name of the variable
	 * @return the launch configuration variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public ISimpleLaunchVariable getVariable(String name);
	/**
	 * Returns all the variables contained in this registry
	 * @return the variables in this registry.
	 */
	public ISimpleLaunchVariable[] getVariables();
}
