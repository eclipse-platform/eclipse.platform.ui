/*******************************************************************************
 * Copyright (c) 2009, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.resources.projectvariables.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IPathVariable;

/**
 * Implements the IPathVariable interface
 */
public class PathVariable implements IPathVariable {

	private String variableName;
	
	public PathVariable(String name) {
		variableName = name;
	}

	/**
	 * @see IPathVariable#getExtensions(String, IResource)
	 */
	public Object[] getExtensions(String variable, IResource resource) {
		ProjectVariableProviderManager.Descriptor descriptor = ProjectVariableProviderManager.getDefault().findDescriptor(variableName);
		if (descriptor != null)
			return descriptor.getExtensions(variable, resource);
		return null;
	}


	/**
	 * @see IPathVariable#isReadOnly()
	 */
	public boolean isReadOnly() {
		ProjectVariableProviderManager.Descriptor descriptor = ProjectVariableProviderManager.getDefault().findDescriptor(variableName);
		return descriptor != null;
	}

	public boolean isPreferred() {
		return !(variableName.equals(WorkspaceLocationVariableResolver.NAME) ||
				variableName.equals(WorkspaceParentLocationVariableResolver.NAME) ||
				variableName.equals(ParentVariableResolver.NAME));
	}
}
