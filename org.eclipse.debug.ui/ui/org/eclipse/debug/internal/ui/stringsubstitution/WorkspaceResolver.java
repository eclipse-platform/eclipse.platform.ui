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
 * Resolves the <code>${workspace_loc}</code> variable. The variable resolves to the
 * location of the workspace. If an argument is provided, it is interpretted as a
 * workspace relative path to a specific resource.
 * 
 * @since 3.0
 */
public class WorkspaceResolver extends ResourceResolver {

	/**
	 * The <code>${workspace_loc}</code> variable does not use the selected resource.
	 * 
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.ResourceResolver#getSelectedResource(org.eclipse.debug.internal.core.stringsubstitution.IContextVariable)
	 */
	protected IResource getSelectedResource(IContextVariable variable) throws CoreException {
		return getWorkspaceRoot();
	}

}
