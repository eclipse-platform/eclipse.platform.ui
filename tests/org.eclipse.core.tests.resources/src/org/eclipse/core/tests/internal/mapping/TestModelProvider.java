/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.core.tests.internal.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;

/**
 * Test model provider. This model provider will match
 * all resources for the purposes of validation.
 */
public class TestModelProvider extends ModelProvider {
	/**
	 * Validation enablement flag to prevent validation when the
	 * tests are not running.
	 */
	public static boolean enabled = false;

	public static final String ID = "org.eclipse.core.tests.resources.modelProvider";

	@Override
	public ResourceMapping[] getMappings(IResource resource, ResourceMappingContext context, IProgressMonitor monitor) {
		return new ResourceMapping[0];
	}

	@Override
	public IStatus validateChange(IResourceDelta rootDelta, IProgressMonitor monitor) {
		if (!enabled) {
			return new ModelStatus(IStatus.OK, ResourcesPlugin.PI_RESOURCES, ID, Status.OK_STATUS.getMessage());
		}
		final ChangeDescription description = new ChangeDescription();
		try {
			rootDelta.accept(delta -> description.recordChange(delta));
		} catch (CoreException e) {
			description.addError(e);
		}
		return description.asStatus();
	}
}
