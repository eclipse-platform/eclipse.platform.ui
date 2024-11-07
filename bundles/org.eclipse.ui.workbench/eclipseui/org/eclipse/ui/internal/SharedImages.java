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
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

/**
 * Common images used by the workbench which may be useful to other plug-ins.
 */
public class SharedImages implements ISharedImages {
	/**
	 * Retrieves the specified image from the workbench plugin's image registry.
	 *
	 * @see ISharedImages
	 */
	@Override
	public Image getImage(String symbolicName) {
		Image image = WorkbenchImages.getImage(symbolicName);
		if (image != null) {
			return image;
		}

		// if there is a descriptor for it, add the image to the registry.
		ImageDescriptor desc = WorkbenchImages.getImageDescriptor(symbolicName);
		if (desc != null) {
			WorkbenchImages.getImageRegistry().put(symbolicName, desc);
			return WorkbenchImages.getImageRegistry().get(symbolicName);
		}
		return null;
	}

	/**
	 * Retrieves the specified image descriptor from the workbench plugin's image
	 * registry.
	 *
	 * @see ISharedImages
	 */
	@Override
	public ImageDescriptor getImageDescriptor(String symbolicName) {
		return WorkbenchImages.getImageDescriptor(symbolicName);
	}
}
