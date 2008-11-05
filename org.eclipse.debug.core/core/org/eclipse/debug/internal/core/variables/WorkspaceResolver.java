/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.variables;

import java.io.File;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.*;
import org.eclipse.osgi.util.NLS;

/**
 * Resolves the <code>${workspace_loc}</code> variable. The variable resolves to the
 * location of the workspace. If an argument is provided, it is interpreted as a
 * workspace relative path to a specific resource.
 */
public final class WorkspaceResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource;

		if (argument == null) {
			resource = root;
		} else {
			resource = root.findMember(new Path(argument));
		}

		if (resource != null && resource.exists()) {
			URI uri = resource.getLocationURI();

			if (uri != null) {
				File file = EFS.getStore(uri).toLocalFile(EFS.NONE, null);

				if (file != null) {
					return file.getAbsolutePath();
				}
			}
		}

		String expression = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(variable.getName(), argument);
		String message = NLS.bind(Messages.WorkspaceResolver_0, expression);

		throw new CoreException(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.RESOURCE_NOT_FOUND, message, null));
	}
}
