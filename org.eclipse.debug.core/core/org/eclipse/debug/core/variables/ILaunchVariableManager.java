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
 * The launch variable manager provides access to registered simple,
 * context, and refresh launch variables. Simple variables are loaded
 * via extensions, added programatically, and persisted in the preferences.
 * Context and refresh variables are loaded via extensions.
 * <p>
 * The singleton instance of the launch variable manager is accessible
 * from <code>org.eclipse.debug.core.DebugPlugin</code>.
 * <p>
 * Clients are not intended to implement this interface.
 * <p>
 * @since 3.0
 * @see org.eclipse.debug.core.variables.ISimpleLaunchVariable
 * @see org.eclipse.debug.core.variables.IContextLaunchVariable
 */
public interface ILaunchVariableManager {
	
	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to the workspace directory (value <code>workspace_loc</code>).
	 */
	public static final String VAR_WORKSPACE_LOC = "workspace_loc"; //$NON-NLS-1$
	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a project's directory (value <code>project_loc</code>).
	 */
	public static final String VAR_PROJECT_LOC = "project_loc"; //$NON-NLS-1$
	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a project (value <code>project_path</code>).
	 */
	public static final String VAR_PROJECT_PATH = "project_path"; //$NON-NLS-1$
	/**
	 * Variable that expands to the name of a project (value <code>project_name</code>).
	 */
	public static final String VAR_PROJECT_NAME = "project_name"; //$NON-NLS-1$
	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a resource's location (value <code>resource_loc</code>).
	 */
	public static final String VAR_RESOURCE_LOC = "resource_loc"; //$NON-NLS-1$
	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a resource (value <code>resource_path</code>).
	 */
	public static final String VAR_RESOURCE_PATH = "resource_path"; //$NON-NLS-1$
	/**
	 * Variable that expands to the name of a resource (value <code>resource_name</code>).
	 */
	public static final String VAR_RESOURCE_NAME = "resource_name"; //$NON-NLS-1$
	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a resource's containing directory (value <code>container_loc</code>).
	 */
	public static final String VAR_CONTAINER_LOC = "container_loc"; //$NON-NLS-1$
	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a resource's parent (value <code>container_path</code>).
	 */
	public static final String VAR_CONTAINER_PATH = "container_path"; //$NON-NLS-1$
	/**
	 * Variable that expands to the name of a resource's parent (value <code>container_name</code>).
	 */
	public static final String VAR_CONTAINER_NAME = "container_name"; //$NON-NLS-1$
		
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
	
	/**
	 * Creates and returns a new simple launch variable with the given name,
	 * description, and value. The variable is <b>not</b> registered with the
	 * launch variable manager.
	 * 
	 * @param name variable name, cannot be <code>null</code>
	 * @param value variable value, may be <code>null</code>
	 * @param description variable description, may be <code>null</code>
	 * @return launch variable
	 */
	public ISimpleLaunchVariable newSimpleVariable(String name, String value, String description);
}
