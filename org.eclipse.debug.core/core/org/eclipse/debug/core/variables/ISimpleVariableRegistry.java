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
	 * Adds the given variable to this variable registry.
	 * @param variable the variable to add
	 */
	public void addVariable(ISimpleLaunchVariable variable);
	/**
	 * Adds the given variables to this variable registry
	 * @param variables the variables to add
	 */
	public void addVariables(ISimpleLaunchVariable[] variables);
	/**
	 * Removes the given variable from this registry. Has no effect
	 * if the given variable is not in this registry.
	 * @param variable the variable to remove
	 */
	public void removeVariable(ISimpleLaunchVariable variable);
	/**
	 * Clears this registry, removing all variables.
	 */
	public void clear();
	/**
	 * Stores the variables in this registry in a file in the metadata.
	 */
	public void storeVariables();
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
