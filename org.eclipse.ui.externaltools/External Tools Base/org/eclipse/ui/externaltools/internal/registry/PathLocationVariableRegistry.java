package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Registry of all available path location variables.
 */
public class PathLocationVariableRegistry extends ExternalToolVariableRegistry {

	/**
	 * Creates the registry and loads the variables.
	 */
	public PathLocationVariableRegistry(String extensionPointId) {
		super(extensionPointId);
	}

	/**
	 * Returns the path location variable for the given tag
	 * or <code>null</code> if none.
	 */
	public PathLocationVariable getPathLocationVariable(String tag) {
		return (PathLocationVariable) findVariable(tag);
	}
	
	/**
	 * Returns the list of path location variables in the registry.
	 */
	public PathLocationVariable[] getPathLocationVariables() {
		PathLocationVariable[] results = new PathLocationVariable[getVariableCount()];
		copyVariables(results);
		return results;
	}
	
	/* (non-Javadoc)
	 * Method declared on ExternalToolVariableRegistry.
	 */
	protected ExternalToolVariable newVariable(String tag, String description, IConfigurationElement element) {
		return new PathLocationVariable(tag, description, element);
	}
}
