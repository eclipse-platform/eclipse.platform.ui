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
package org.eclipse.debug.internal.core.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.variables.ExpandVariableContext;

/**
 * Expands a workspace variable into the desired
 * result format.
 */
public class WorkspaceExpander extends ResourceExpander {

	/**
	 * Create an instance
	 */
	public WorkspaceExpander() {
		super();
	}

	/**
	 * @see ResourceExpander#expandUsingContext(ExpandVariableContext)
	 */
	protected IResource expandUsingContext(ExpandVariableContext context) {
		return getWorkspaceRoot();
	}
}
