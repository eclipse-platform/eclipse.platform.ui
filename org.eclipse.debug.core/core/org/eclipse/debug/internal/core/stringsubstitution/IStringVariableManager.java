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
	 * Recursively resolves and replaces all variable references in the given
	 * expression with their corresponding values.
	 * 
	 * @param expression expression referencing variables
	 * @return expression with variable references replaced with variable values
	 * @throws CoreException if unable to resolve the value of one or more variables
	 */
	public String performStringSubstitution(String expression) throws CoreException;
	
	/**
	 * Returns a new variable with the given name and description. The variable
	 * will accept a value.
	 * 
	 * @param name variable name, cannot be <code>null</code>
	 * @param description variable description, possibly <code>null</code>
	 * @return a new variable
	 * @exception CoreException if a variable already exists with the given name
	 */
	public IStringVariable newVariable(String name, String description) throws CoreException;
	
	/**
	 * Adds the given variables to the variable registry.
	 * 
	 * @param variables the variables to add
	 * @throws CoreException if one or more variables to add has a name collision with
	 *  an existing variable 
	 */
	public void addVariables(IStringVariable[] variables) throws CoreException;
	
	/**
	 * Removes the given variables from the registry. Has no effect for unregistered
	 * variables.
	 * 
	 * @param variables variables to remove
	 */
	public void removeVariables(IStringVariable[] variables);
	
	/**
	 * Returns the context with the given identifier or <code>null</code> if none.
	 * 
	 * @param id context identifier
	 * @return the context with the given identifier of <code>null</code> if none
	 */
	public IStringVariableContext getContext(String id);
	
	/**
	 * Sets the variable context for the given identifier. A variable
	 * context of <code>null</code> indicates that the context is no longer
	 * defined. 
	 * 
	 * @param id context identifier
	 * @param context variable context
	 */
	public void setContext(String id, IStringVariableContext context);
}
