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
 * Variable constants defining the name of variables contributed
 * by the debug plug-in.
 * 
 * @since 3.0
 */
public interface ILaunchVariableConstants {

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
}
