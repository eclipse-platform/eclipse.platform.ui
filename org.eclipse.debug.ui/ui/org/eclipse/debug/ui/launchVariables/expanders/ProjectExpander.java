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
package org.eclipse.debug.ui.launchVariables.expanders;


import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.variables.ExpandVariableContext;

/**
 * Expands a resource's project type variable into the desired
 * result format.
 */
public class ProjectExpander extends ResourceExpander {

	/**
	 * Create an instance
	 */
	public ProjectExpander() {
		super();
	}

	/**
	 * @see ResourceExpander#expandUsingContext(ExpandVariableContext)
	 */
	protected IResource expandUsingContext(ExpandVariableContext context) {
		IResource resource = context.getSelectedResource();
		if (resource != null) {
			return resource.getProject();
		}
		return null;
	}
	
	/**
	 * @see ResourceExpander#expandToMember(String)
	 */
	protected IResource expandToMember(String varValue) {
		IResource member = super.expandToMember(varValue);
		if (member != null) {
			return member.getProject();
		}
		return null;
	}
}
