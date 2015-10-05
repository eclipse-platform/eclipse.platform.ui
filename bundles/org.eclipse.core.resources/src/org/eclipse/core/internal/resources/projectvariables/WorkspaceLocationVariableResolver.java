/*******************************************************************************
 * Copyright (c) 2008, 2014 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.projectvariables;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

/**
 *
 */
public class WorkspaceLocationVariableResolver extends PathVariableResolver {

	public static String NAME = "WORKSPACE_LOC"; //$NON-NLS-1$

	public WorkspaceLocationVariableResolver() {
		// nothing to do
	}

	@Override
	public String[] getVariableNames(String variable, IResource resource) {
		return new String[] {NAME};
	}

	@Override
	public String getValue(String variable, IResource resource) {
		return resource.getWorkspace().getRoot().getLocationURI().toASCIIString();
	}
}
