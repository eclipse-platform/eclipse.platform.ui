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
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.stringsubstitution.IContextVariable;



/**
 * Resolver for the <code>${project_*}</code> variables. Accepts an optional argument
 * that is interpretted as the name of a project.  
 * 
 * @since 3.0
 */
public class ProjectResolver extends ResourceResolver {
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.AbstractResolver#getResource(org.eclipse.debug.internal.core.stringsubstitution.IContextVariable, java.lang.String)
	 */
	protected IResource getResource(IContextVariable variable, String argument) throws CoreException {
		return getWorkspaceRoot().getProject(argument);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.AbstractResolver#translateSelectedResource(org.eclipse.core.resources.IResource)
	 */
	protected IResource translateSelectedResource(IResource resource) {
		return resource.getProject();
	}

}
