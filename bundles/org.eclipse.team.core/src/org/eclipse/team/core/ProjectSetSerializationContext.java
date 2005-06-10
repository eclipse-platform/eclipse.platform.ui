/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dan Rubel - initial API and implementation
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/

package org.eclipse.team.core;

import org.eclipse.core.resources.IProject;

/**
 * The context in which project serialization occurs.
 * The class may be subclassed to represent different serialization contexts.
 * 
 * @since 3.0
 */
public class ProjectSetSerializationContext {
	
	private final String filename;

	/**
	 * Create a serialization context with no filename
	 */
	public ProjectSetSerializationContext() {
		this(null);
	}
	
	/**
	 * Create a serialization context and set the filename of the file 
	 * that does or is to contain the project set.
	 * @param filename a filename or <code>null</code>
	 */
	public ProjectSetSerializationContext(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Given an array of projects that currently exist in the workspace
	 * determine which of those projects should be overwritten.
	 * <p>
	 * This default implementation always returns an empty array
	 * indicating that no existing projects should be overwritten.
	 * Subclasses may override this as appropriate.
	 * 
	 * @param projects 
	 * 		an array of projects currently existing in the workspace
	 * 		that are desired to be overwritten.
	 * 		(not <code>null</code>, contains no <code>null</code>s)
	 * @return
	 * 		an array of zero or more projects that should be overwritten
	 * 		or <code>null</code> if the operation is to be canceled
	 */
	public IProject[] confirmOverwrite(IProject[] projects) throws TeamException {
		return new IProject[0];
	}

	/**
	 * Return a org.eclipse.swt.Shell if there is a UI context 
	 * or <code>null</code> if executing headless.
	 *
	 * @return the shell or <code>null</code>
	 */
	public Object getShell() {
		return null;
	}
	
	/**
	 * Return the name of the file to or from which the project set is being loaded or saved. 
	 * This may be <code>null</code>.
	 * @return the filename or <code>null</code>
	 */
	public String getFilename() {
		return filename;
	}
}
