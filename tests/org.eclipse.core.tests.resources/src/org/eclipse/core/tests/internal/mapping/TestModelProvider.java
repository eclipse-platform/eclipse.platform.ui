/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public ResourceMapping[] getMappings(IResource resource, ResourceMappingContext context, IProgressMonitor monitor) {
		return new ResourceMapping[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ModelProvider#validateChange(org.eclipse.core.resources.IResourceDelta, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus validateChange(IResourceDelta rootDelta, IProgressMonitor monitor) {
		if (!enabled)
			return new ModelStatus(IStatus.OK, ResourcesPlugin.PI_RESOURCES, ID, Status.OK_STATUS.getMessage());
		final ChangeDescription description = new ChangeDescription();
		try {
			rootDelta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					return description.recordChange(delta);
				}
			});
		} catch (CoreException e) {
			description.addError(e);
		}
		return description.asStatus();
	}
}
