/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
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

import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

import org.eclipse.core.resources.IResource;

/**
 * 
 */
public class ProjectLocationVariableResolver extends PathVariableResolver {

	public static String NAME = "PROJECT_LOC"; //$NON-NLS-1$

	public ProjectLocationVariableResolver() {
		// nothing
	}

	public String getValue(String variable, IResource resource) {
		if (resource.getProject().getLocationURI() != null)
			return resource.getProject().getLocationURI().toASCIIString();
		return null;
	}
}
