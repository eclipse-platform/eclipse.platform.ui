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
package org.eclipse.debug.internal.ui.views.variables;


import org.eclipse.core.runtime.IPath;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState {

	// paths to expanded variables
	private IPath[] fExpandedElements = null;
	// paths to selected variables	
	private IPath[] fSelection = null;
	
	/**
	 * Constructs a memento with the given name paths to 
	 * expanded and selected elements.
	 * 
	 * @param expandedElements name paths to expanded elements,
	 *  or <code>null</code>
	 * @param selection name paths to selected elements, or
	 *  <code>null</code>
	 */
	public ViewerState(IPath[] expandedElements, IPath[] selection) {
		fExpandedElements = expandedElements;
		fSelection = selection;
	}

	/**
	 * Returns a collection of name paths to expanded elements.
	 * 
	 * @return a collection of name paths to exepanded elements,
	 *  possibly <code>null</code>
	 */
	public IPath[] getExpandedElements() {
		return fExpandedElements;
	}
	
	/**
	 * Returns a collection of name paths to selected elements.
	 * 
	 * @return a collection of name paths to selected elements,
	 *  possibly <code>null</code>
	 */
	public IPath[] getSelection() {
		return fSelection;
	}
}
