/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

public class ResourceMgr {

	private static ResourceBundle resourceBundle = null;
	private static Hashtable imageMap = null;
	private static Hashtable imageDescriptorMap = null;
	private static URL imageURLBase = null;

	public static void init() {
		if (imageURLBase != null) {
			// The resource manager has already been initialized
			return;
		}
		URL urlBase = null;
		try {
			urlBase = new URL(ExternalToolsPlugin.getDefault().getDescriptor().getInstallURL(), IAntViewConstants.IMAGE_DIR);
		} catch (MalformedURLException ex) {
			urlBase = null;
		}

		ResourceMgr.setImageBase(urlBase);
		ResourceMgr.setResourceBase(IAntViewConstants.RESOURCE_BASE_NAME);

		ResourceMgr.setImage(IAntViewConstants.IMAGE_PROJECT);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_PROJECT_ERROR);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_TARGET_SELECTED);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_TARGET_DESELECTED);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_ELEMENT);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_ELEMENTS);
		ResourceMgr.setImage(IAntViewConstants.IMAGE_ERROR);

		ResourceMgr.setImageDescriptor(IAntViewConstants.IMAGE_RUN);
		ResourceMgr.setImageDescriptor(IAntViewConstants.IMAGE_REMOVE);
		ResourceMgr.setImageDescriptor(IAntViewConstants.IMAGE_CLEAR);
		ResourceMgr.setImageDescriptor(IAntViewConstants.IMAGE_REFRESH);
	}

	/**
	 * Method setResourceBase.
	 * @param resourceBaseName
	 */
	public static void setResourceBase(String resourceBaseName) {
		try {
			resourceBundle = ResourceBundle.getBundle(resourceBaseName);
		} catch (MissingResourceException ex) {
			resourceBundle = null;
		}
	}

	/**
	 * Method setImageBase.
	 * @param imageBase
	 */
	public static void setImageBase(URL imageBase) {
		imageMap = new Hashtable();
		imageDescriptorMap = new Hashtable();
		imageURLBase = imageBase;
	}

	/**
	 * Method getString.
	 * @param key
	 * @return String
	 */
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException ex) {
			return key;
		}
	}

	/**
	 * Method getResourceBundle.
	 * @return ResourceBundle
	 */
	public static ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Method setImageDescriptor.
	 * @param key
	 */
	public static void setImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = null;
		try {
			URL imageLocation = new URL(imageURLBase, key);
			imageDescriptor = ImageDescriptor.createFromURL(imageLocation);
		} catch (MalformedURLException ex) {
			imageDescriptor = null;
		}
		imageDescriptorMap.put(key, imageDescriptor);
	}

	/**
	 * Method setImageDescriptor.
	 * @param key
	 * @param imageDescriptor
	 */
	public static void setImageDescriptor(String key, ImageDescriptor imageDescriptor) {
		imageDescriptorMap.put(key, imageDescriptor);
	}

	/**
	 * Method setImage.
	 * @param key
	 */
	public static void setImage(String key) {
		Image image = null;
		try {
			URL imageLocation = new URL(imageURLBase, key);
			image = new Image(null, imageLocation.openStream());
		} catch (MalformedURLException ex) {
			image = null;
		} catch (IOException ex) {
			image = null;
		}
		imageMap.put(key, image);
	}

	/**
	 * Method setImage.
	 * @param key
	 * @param image
	 */
	public static void setImage(String key, Image image) {
		imageMap.put(key, image);
	}

	/**
	 * Method getImageDescriptor.
	 * @param key
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return (ImageDescriptor) imageDescriptorMap.get(key);
	}

	/**
	 * Method getImage.
	 * @param key
	 * @return Image
	 */
	public static Image getImage(String key) {
		return (Image) imageMap.get(key);
	}

	/**
	 * Method dispose.
	 */
	public static void dispose() {
		Enumeration e = imageMap.elements();
		while (e.hasMoreElements()) {
			 ((Image) e.nextElement()).dispose();
		}
		imageMap.clear();
	}
}