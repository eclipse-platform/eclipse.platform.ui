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

/**
 * Resolver for the <code>${container_*}</code> variables. Accepts an optional
 * argument that is interpretted as a full path to a container in the workspace.  
 * 
 * @since 3.0
 */
public class ContainerResolver extends ResourceResolver {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.AbstractResolver#translateSelectedResource(org.eclipse.core.resources.IResource)
	 */
	protected IResource translateSelectedResource(IResource resource) {
		return resource.getParent();
	}

}
