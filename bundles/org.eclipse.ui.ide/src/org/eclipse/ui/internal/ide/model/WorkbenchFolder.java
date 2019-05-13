/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * An IWorkbenchAdapter that represents IFolders.
 */
public class WorkbenchFolder extends WorkbenchResource {
	/**
	 *	Answer the appropriate base image to use for the passed resource, optionally
	 *	considering the passed open status as well iff appropriate for the type of
	 *	passed resource
	 */
	@Override
	protected ImageDescriptor getBaseImage(IResource resource) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJ_FOLDER);
	}

	/**
	 * Returns the children of this container.
	 */
	@Override
	public Object[] getChildren(Object o) {
		try {
			return ((IContainer) o).members();
		} catch (CoreException e) {
			return NO_CHILDREN;
		}
	}
}
