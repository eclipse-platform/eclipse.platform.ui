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
package org.eclipse.debug.internal.core.stringsubstitution;

import org.eclipse.core.runtime.CoreException;

/**
 * Regisitry for string variables and contexts.
 * 
 * @since 3.0
 */
public interface IStringVariableManager {

	/**
	 * Returns all registered variables.
	 * 
	 * @return a collection of all registered variables
	 */
	public IStringVariable[] getVariables();
	
	/**
	 * Returns all registered value variables.
	 * 
	 * @return a collection of all registered value variables
	 */
	public IValueVariable[] getValueVariables();
	
	/**
	 * Returns the value variable with the given name, or <code>null</code>
	 * if none.
	 * 
	 * @param name variable name
	 * @return the value variable with the given name, or <code>null</code>
	 * if none
	 */
	public IValueVariable getValueVariable(String name);
	
	/**
	 * Returns all registered context variables.
	 * 
	 * @return a collection of all registered context variables
	 */
	public IContextVariable[] getContextVariables();	
	
	/**
	 * Returns the context variable with the given name or <code>null</code>
	 * if none.
	 * 
	 * @param name variable name
	 * @return the context variable with the given name or <code>null</code>
	 * if none
	 */
	public IContextVariable getContextVariable(String name);
	
	/**
	 * Recursively resolves and replaces all variable references in the given
	 * expression with their corresponding values.
	 * 
	 * @param expression expression referencing variables
	 * @return expression with variable references replaced with variable values
	 * @throws CoreException if unable to resolve the value of one or more variables
	 */
	public String performStringSubstitution(String expression) throws CoreException;
	
	/**
	 * Returns a new value variable with the given name and description.
	 * 
	 * @param name variable name, cannot be <code>null</code>
	 * @param description variable description, possibly <code>null</code>
	 * @return a new variable
	 * @exception CoreException if a variable already exists with the given name
	 */
	public IValueVariable newValueVariable(String name, String description) throws CoreException;
	
	/**
	 * Adds the given variables to the variable registry.
	 * 
	 * @param variables the variables to add
	 * @throws CoreException if one or more variables to add has a name collision with
	 *  an existing variable 
	 */
	public void addVariables(IValueVariable[] variables) throws CoreException;
	
	/**
	 * Removes the given variables from the registry. Has no effect for unregistered
	 * variables.
	 * 
	 * @param variables variables to remove
	 */
	public void removeVariables(IValueVariable[] variables);
	
	/**
	 * Registers the given listener for value variable notifications. Has no effect
	 * if an identical listener is already registered.
	 *   
	 * @param listener value variable listener to add
	 */
	public void addValueVariableListener(IValueVariableListener listener);
	
	/**
	 * Removes the given listener from the list of registered value variable
	 * listeners. Has no effect if an identical listener is not already registered.
	 * 
	 * @param listener value variable listener to remove
	 */
	public void removeValueVariableListener(IValueVariableListener listener);
	
}
