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
import java.net.*;
import java.text.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
/**
 * Uses a resource bundle to load images and strings from a property file. This
 * class needs to properly use the desired locale.
 */
public class HelpUIResources {
	private static ResourceBundle resBundle;
	static {
		resBundle = ResourceBundle.getBundle(HelpUIResources.class.getName());
	}
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
		IPath path = new Path("icons/").append(name); //$NON-NLS-1$
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
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name) {
		try {
			return resBundle.getString(name);
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0, String replace1) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3, String replace4) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3, replace4});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3, String replace4,
			String replace5) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3, replace4, replace5});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
}
