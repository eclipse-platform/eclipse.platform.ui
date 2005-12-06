/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
/**
 * Uses a resource bundle to load images and strings from a property file. This
 * class needs to properly use the desired locale.
 */
public class HelpUIResources {
	/**
	 * WorkbenchResources constructor comment.
	 */
	public HelpUIResources() {
		super();
	}
	/**
	 * Returns a string from a property file
	 */
	public static URL getImagePath(String name) {
		IPath path = new Path("$nl$/icons/").append(name); //$NON-NLS-1$
		return Platform.find(HelpUIPlugin.getDefault().getBundle(), path);
	}
	
	/**
	 * Returns an image descriptor from a property file
	 * @param name simple image file name
	 * @return the descriptor
	 */

	public static ImageDescriptor getImageDescriptor(String name) {
		URL imagePath = getImagePath(name);
		ImageRegistry registry = HelpUIPlugin.getDefault().getImageRegistry();
		ImageDescriptor desc = registry.getDescriptor(name);
		if (desc==null) {
			desc = ImageDescriptor.createFromURL(imagePath);
			registry.put(name, desc);
		}
		return desc;
	}
	
	public static ImageDescriptor getImageDescriptor(String bundleId, String name) {
		ImageRegistry registry = HelpUIPlugin.getDefault().getImageRegistry();		
		ImageDescriptor desc = registry.getDescriptor(name);
		if (desc==null) {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle==null) return null;
			URL url = Platform.find(bundle, new Path(name));			
			desc = ImageDescriptor.createFromURL(url);
			registry.put(name, desc);
		}
		return desc;
	}
	
	/**
	 * Returns an image from a property file
	 * @param name simple image file name
	 * @return the new image or <code>null</code> if image
	 * could not be created
	 */

	public static Image getImage(String name) {
		ImageRegistry registry = HelpUIPlugin.getDefault().getImageRegistry();
		//Ensure we have the descriptor
		getImageDescriptor(name);
		return registry.get(name);
	}
	
	public static Image getImage(URL url) {
		ImageRegistry registry = HelpUIPlugin.getDefault().getImageRegistry();	
		String name = url.toString();
		ImageDescriptor desc = registry.getDescriptor(name);
		if (desc==null) {
			desc = ImageDescriptor.createFromURL(url);
			registry.put(name, desc);
		}
		return registry.get(name);
	}
}
