/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.commands;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * A command parameter value converter to convert between IResources and strings
 * encoding the path of a resource.
 * 
 * @since 3.2
 */
public final class ResourcePathConverter extends
		AbstractParameterValueConverter {

	public final Object convertToObject(final String parameterValue)
			throws ParameterValueConversionException {
		final Path path = new Path(parameterValue);
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
				.getRoot();
		final IResource resource = workspaceRoot.findMember(path);

		if ((resource == null) || (!resource.exists())) {
			throw new ParameterValueConversionException(
					"parameterValue must be the path of an existing resource"); //$NON-NLS-1$
		}

		return resource;
	}

	public final String convertToString(final Object parameterValue)
			throws ParameterValueConversionException {
		if (!(parameterValue instanceof IResource)) {
			throw new ParameterValueConversionException(
					"parameterValue must be an IResource"); //$NON-NLS-1$
		}
		final IResource resource = (IResource) parameterValue;
		return resource.getFullPath().toString();
	}
}
