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

import org.eclipse.core.resources.IPathVariable;
import org.eclipse.core.resources.IPathVariableManager;

/**
 * Implements the IPathVariable interface
 */
public class PathVariable implements IPathVariable {

	private String variableName;
	IPathVariableManager manager;
	
	public PathVariable(IPathVariableManager manager, String name) {
		variableName = name;
		this.manager = manager;
	}

	/**
	 * @see IPathVariable#getExtensions()
	 */
	public String[] getExtensions() {
		ProjectVariableProviderManager.Descriptor descriptor = ProjectVariableProviderManager.getDefault().findDescriptor(variableName);
		if (descriptor != null) {
			if (manager instanceof ProjectPathVariableManager)
				return descriptor.getExtensions(variableName, ((ProjectPathVariableManager) manager).getResource());
			if (manager instanceof PathVariableManager)
				return descriptor.getExtensions(variableName, null);
		}
		return null;
	}


	/**
	 * @see IPathVariable#isReadOnly()
	 */
	public boolean isReadOnly() {
		ProjectVariableProviderManager.Descriptor descriptor = ProjectVariableProviderManager.getDefault().findDescriptor(variableName);
		return descriptor != null;
	}
}
