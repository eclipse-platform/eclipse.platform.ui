/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;


/** 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorImages {

	//Create image registry
	private final static ImageRegistry NAVIGATOR_PLUGIN_REGISTRY = NavigatorPlugin.getDefault().getImageRegistry();

	private static URL ICON_LOCATION;
	static {
		try {
			ICON_LOCATION = new URL(NavigatorPlugin.getDefault().getDescriptor().getInstallURL(), "icons/full/"); //$NON-NLS-1$ 
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Gets the current image.
	 * 
	 * @param String
	 *            key - Name of the icon.
	 * @return Image
	 */
	public static Image get(String key) {
		return NAVIGATOR_PLUGIN_REGISTRY.get(key);
	}

	/**
	 * Create and returns a image descriptor and adds the image to the registery.
	 * 
	 * @param String
	 *            prefix - Icon dir structure.
	 * @param String
	 *            name - The name of the icon.
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor createManaged(String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		NAVIGATOR_PLUGIN_REGISTRY.put(name, result);
		return result;
	}

	/**
	 * Creates the icon url
	 * 
	 * @param String
	 *            prefix - Icon dir structure.
	 * @param String
	 *            name - The name of the icon.
	 * @return URL
	 */
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(ICON_LOCATION, buffer.toString());
		} catch (MalformedURLException ex) {
			return null;
		}
	}
}
