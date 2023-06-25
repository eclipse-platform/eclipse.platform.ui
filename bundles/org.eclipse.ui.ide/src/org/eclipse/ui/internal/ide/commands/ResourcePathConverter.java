/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.runtime.IPath;

/**
 * A command parameter value converter to convert between IResources and strings
 * encoding the path of a resource.
 *
 * @since 3.2
 */
public final class ResourcePathConverter extends
		AbstractParameterValueConverter {

	@Override
	public final Object convertToObject(final String parameterValue)
			throws ParameterValueConversionException {
		final IPath path = IPath.fromOSString(parameterValue);
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
				.getRoot();
		final IResource resource = workspaceRoot.findMember(path);

		if ((resource == null) || (!resource.exists())) {
			throw new ParameterValueConversionException(
					"parameterValue must be the path of an existing resource"); //$NON-NLS-1$
		}

		return resource;
	}

	@Override
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
