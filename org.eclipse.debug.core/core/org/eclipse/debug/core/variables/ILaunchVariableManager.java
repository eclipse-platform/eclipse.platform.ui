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

public interface ILaunchVariableManager {
	/**
	 * Returns the context variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple context variables with the given name
	 * have been added to this registry, returns the most recently added variable
	 * with that name.
	 * 
	 * @param name the name of the context variable
	 * @return the context launch configuration variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public IContextLaunchVariable getContextVariable(String name);
	/**
	 * Returns all the context variables in the registry.
	 * 
	 * @return the context launch variables in this registry
	 */
	public IContextLaunchVariable[] getContextVariables();
	/**
	 * Returns the refresh variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple refresh variables with the given name
	 * have been added to this registry, returns the most recently added variable
	 * with that name.
	 * 
	 * @param name the name of the refresh variable
	 * @return the refresh launch configuration variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public IContextLaunchVariable getRefreshVariable(String name);
	/**
	 * Returns all the refresh variables in the registry.
	 * 
	 * @return the refresh launch variables in this registry
	 */
	public IContextLaunchVariable[] getRefreshVariables();
	/**
	 * Adds the given simple variables to this variable registry
	 * 
	 * @param variables the simple launch variables to add
	 */
	public void addSimpleVariables(ISimpleLaunchVariable[] variables);
	/**
	 * Removes the given simple variables from this registry. Has no effect
	 * if any of the given variables are not in this registry.
	 * 
	 * @param variables the simple launch variables to remove
	 */
	public void removeSimpleVariables(ISimpleLaunchVariable[] variables);
	/**
	 * Returns the simple variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple variables with the given name have
	 * been added to this registry, returns the most recently added variable
	 * with that name.
	 * 
	 * @param name the name of the variable
	 * @return the simple launch variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public ISimpleLaunchVariable getSimpleVariable(String name);
	/**
	 * Returns all the simple variables contained in this registry
	 * 
	 * @return the simple launch variables in this registry.
	 */
	public ISimpleLaunchVariable[] getSimpleVariables();
}
