/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.UIConstants;

/**
 * TeamImages provides convenience methods for accessing shared images
 * provided by the org.eclipse.team.ui plug-in.
 */
public class TeamImages {
	private static Hashtable imageDescriptors = new Hashtable(20);
	
	final static String ICON_PATH;
	static {
		final String iconPath[] = new String[1];
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				if (display.getIconDepth() > 4) {
					iconPath[0] = UIConstants.ICON_PATH_FULL;
				} else {
					iconPath[0] = UIConstants.ICON_PATH_BASIC;
				}
			}
		});
		ICON_PATH = iconPath[0];
		
		initializeImages();
	}
	/**
	 * Creates an image and places it in the image registry.
	 * 
	 * @param id  the identifier for the image
	 * @param baseURL  the base URL for the image
	 */
	protected static void createImageDescriptor(String id, URL baseURL) {
		URL url = null;
		try {
			url = new URL(baseURL, UIConstants.ICON_PATH_FULL + id);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		imageDescriptors.put(id, desc);
	}
	/**
	 * Returns the image descriptor for the given image ID.
	 * Returns null if there is no such image.
	 * 
	 * @param id  the identifier for the image to retrieve
	 * @return the image associated with the given ID
	 */
	public static ImageDescriptor getImageDescriptor(String id) {
		return (ImageDescriptor)imageDescriptors.get(id);
	}	
	/**
	 * Convenience method to get an image descriptor for an extension
	 * 
	 * @param extension  the extension declaring the image
	 * @param subdirectoryAndFilename  the path to the image
	 * @return the image
	 */
	public static ImageDescriptor getImageDescriptorFromExtension(IExtension extension, String subdirectoryAndFilename) {
		IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
		URL path = pluginDescriptor.getInstallURL();
		URL fullPathString = null;
		try {
			fullPathString = new URL(path,subdirectoryAndFilename);
			return ImageDescriptor.createFromURL(fullPathString);
		} catch (MalformedURLException e) {
		}
		return null;
	}
	/**
	 * Initializes the table of images used in this plugin.
	 */
	private static void initializeImages() {
		URL baseURL = TeamUIPlugin.getPlugin().getDescriptor().getInstallURL();

		// View decoration overlays
		createImageDescriptor(ISharedImages.IMG_DIRTY_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR, baseURL);
		
		// Target Management Icons
		createImageDescriptor(ISharedImages.IMG_SITE_VIEW, baseURL);
		createImageDescriptor(ISharedImages.IMG_SITE_ELEMENT, baseURL);
		
		// Sync View Icons
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_INCOMING, baseURL);
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_OUTGOING, baseURL);
		createImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING, baseURL);
		createImageDescriptor(UIConstants.IMG_REFRESH, baseURL);
		createImageDescriptor(UIConstants.IMG_IGNORE_WHITESPACE, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE, baseURL);
		createImageDescriptor(UIConstants.IMG_SYNC_MODE_FREE, baseURL);
		createImageDescriptor(UIConstants.IMG_WIZBAN_SHARE, baseURL);
		createImageDescriptor(UIConstants.IMG_WIZBAN_NEW_CONNECTION, baseURL);
		createImageDescriptor(UIConstants.IMG_CONTENTS, baseURL);
	}
}
