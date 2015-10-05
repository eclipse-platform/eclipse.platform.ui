/*******************************************************************************
 * Copyright (c) 2008, 2014 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources.projectvariables;

import java.net.URI;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

/**
 * Returns the location of the parent resource
 */
public class WorkspaceParentLocationVariableResolver extends PathVariableResolver {

	public static String NAME = "PARENT_LOC"; //$NON-NLS-1$

	public WorkspaceParentLocationVariableResolver() {
		// nothing
	}

	@Override
	public String[] getVariableNames(String variable, IResource resource) {
		return new String[] {NAME};
	}

	@Override
	public String getValue(String variable, IResource resource) {
		IContainer parent = resource.getParent();
		if (parent != null) {
			URI locationURI = parent.getLocationURI();
			if (locationURI != null)
				return locationURI.toASCIIString();
		}
		return null;
	}
}
