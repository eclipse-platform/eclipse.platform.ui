/*******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.FrameworkUtil;

class ImageResourceManager {
	private LocalResourceManager manager;

	/**
	 * The constructor
	 *
	 * @param owner
	 *            control that owns the images, images will be disposed when owner
	 *            is disposed
	 */
	ImageResourceManager(Control owner) {
		manager = new LocalResourceManager(JFaceResources.getResources(), owner);
	}

	/**
	 * Get image descriptor composed from folder and image
	 *
	 * @param path
	 *            path to folder the image
	 * @return image descriptor
	 */
	static ImageDescriptor getImageDescriptor(String path) {
		URL url = FileLocator.find(FrameworkUtil.getBundle(ImageResourceManager.class), new Path(path), null);
		return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Get image
	 *
	 * @param descriptor
	 *            the image descriptor
	 * @return the image
	 */
	Image getImage(ImageDescriptor descriptor) {
		return (Image) manager.get(descriptor);
	}
}

