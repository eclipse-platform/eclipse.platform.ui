/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.variables;

import org.eclipse.core.resources.IResource;

/**
 * Resolver for the <code>${project_*}</code> variables. Accepts an optional argument
 * that is interpretted as the name of a project.  
 * <p>
 * Moved to debug core in 3.5, existed in debug.iu since 3.0.
 * </p>
 * @since 3.5
 */
public class ProjectResolver extends ResourceResolver {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.ResourceResolver#translateSelectedResource(org.eclipse.core.resources.IResource)
	 */
	protected IResource translateSelectedResource(IResource resource) {
		return resource.getProject();
	}
}
