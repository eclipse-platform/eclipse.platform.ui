/**********************************************************************
 Copyright (c) 2004 Dan Rubel and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:

 Dan Rubel - initial API and implementation

 **********************************************************************/

package org.eclipse.team.core;

import org.eclipse.core.resources.IProject;

/**
 * The context in which project serialization occurs.
 * The class may be subclasses to represent different serialization contexts.
 * 
 * @since 3.0
 */
public class ProjectSetSerializationContext {
	
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
	 * Return a {@link org.eclipse.swt.Shell} if there is a UI context 
	 * or <code>null</code> if executing headless.
	 *
	 * @return the shell or <code>null</code>
	 */
	public Object getShell() {
		return null;
	}
	
}
