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

import org.eclipse.core.resources.IResource;

/**
 * Represents the context in which a variable will be expanded.
 * @since 3.0
 */
public class ExpandVariableContext {
	
	private IResource selectedResource = null;
	
	/**
	 * Create a context for a launch configuration running
	 * with the given resource selected.
	 * 
	 * @param selectedResource the <code>IResource</code> selected
	 * 		or <code>null</code> if none.
	 */
	public ExpandVariableContext(IResource selectedResource) {
		super();
		if (selectedResource != null) {
			this.selectedResource = selectedResource;
		}
	}
	
	/**
	 * Returns the resource selected at the time the tool
	 * is run, or <code>null</code> if none selected.
	 * 
	 * @return the <code>IResource</code> selected, or <code>null</code> if none
	 */
	public IResource getSelectedResource() {
		return selectedResource;
	}
}
