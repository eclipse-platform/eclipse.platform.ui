/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 *
 *
 * @since 3.2
 */
public class NavigatorImages {

	// Create image registry
	private static final ImageRegistry NAVIGATOR_PLUGIN_REGISTRY = NavigatorPlugin
			.getDefault().getImageRegistry();

	private static URL ICONS_LOCATION;
	static {
		ICONS_LOCATION = FileLocator.find(NavigatorPlugin.getDefault()
				.getBundle(), new Path("icons/full/"), Collections.EMPTY_MAP); //$NON-NLS-1$
	}

	/**
	 * Gets the current image.
	 *
	 * @param key
	 *            Name of the icon.
	 * @return Image
	 */
	public static Image get(String key) {
		return NAVIGATOR_PLUGIN_REGISTRY.get(key);
	}

	/**
	 * Create and returns a image descriptor and adds the image to the registry.
	 *
	 * @param prefix
	 *            Icon dir structure.
	 * @param name
	 *            The name of the icon.
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor createManaged(String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(
				prefix, name));
		NAVIGATOR_PLUGIN_REGISTRY.put(name, result);
		return result;
	}

	/**
	 * Creates the icon url
	 *
	 * @param prefix
	 *            Icon dir structure.
	 * @param name
	 *            The name of the icon.
	 * @return URL
	 */
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuilder buffer = new StringBuilder(prefix);
		buffer.append(name);
		try {
			return new URL(ICONS_LOCATION, buffer.toString());
		} catch (MalformedURLException ex) {
			return null;
		}
	}
}
