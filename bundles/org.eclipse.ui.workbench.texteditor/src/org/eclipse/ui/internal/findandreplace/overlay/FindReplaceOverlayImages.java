/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.net.URL;

import org.osgi.framework.Bundle;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * Provides Icons for the editor overlay used for performing
 * find/replace-operations.
 */
class FindReplaceOverlayImages {
	private static final String PREFIX_ELCL = TextEditorPlugin.PLUGIN_ID + ".elcl."; //$NON-NLS-1$
	static final String KEY_CLOSE = PREFIX_ELCL + "close"; //$NON-NLS-1$
	static final String KEY_FIND_NEXT = PREFIX_ELCL + "select_next"; //$NON-NLS-1$
	static final String KEY_FIND_PREV = PREFIX_ELCL + "select_prev"; //$NON-NLS-1$
	static final String KEY_FIND_REGEX = PREFIX_ELCL + "regex"; //$NON-NLS-1$
	static final String KEY_REPLACE = PREFIX_ELCL + "replace"; //$NON-NLS-1$
	static final String KEY_REPLACE_ALL = PREFIX_ELCL + "replace_all"; //$NON-NLS-1$
	static final String KEY_WHOLE_WORD = PREFIX_ELCL + "whole_word"; //$NON-NLS-1$
	static final String KEY_CASE_SENSITIVE = PREFIX_ELCL + "case_sensitive"; //$NON-NLS-1$
	static final String KEY_SEARCH_ALL = PREFIX_ELCL + "search_all"; //$NON-NLS-1$
	static final String KEY_SEARCH_IN_AREA = PREFIX_ELCL + "search_in_selection"; //$NON-NLS-1$
	static final String KEY_OPEN_REPLACE_AREA = PREFIX_ELCL + "open_replace"; //$NON-NLS-1$
	static final String KEY_CLOSE_REPLACE_AREA = PREFIX_ELCL + "close_replace"; //$NON-NLS-1$
	static final String KEY_OPEN_HISTORY = "open_history"; //$NON-NLS-1$

	/**
	 * The image registry containing {@link Image images}.
	 */
	private static ImageRegistry fgImageRegistry;

	private static String ICONS_PATH = "$nl$/icons/full/"; //$NON-NLS-1$

	private final static String ELCL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$

	/**
	 * Declare all images
	 */
	private static void declareImages() {
		declareRegistryImage(KEY_CLOSE, ELCL + "close.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_FIND_NEXT, ELCL + "select_next.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_FIND_PREV, ELCL + "select_prev.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_FIND_REGEX, ELCL + "regex.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_REPLACE_ALL, ELCL + "replace_all.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_REPLACE, ELCL + "replace.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_WHOLE_WORD, ELCL + "whole_word.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_CASE_SENSITIVE, ELCL + "case_sensitive.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_SEARCH_ALL, ELCL + "search_all.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_SEARCH_IN_AREA, ELCL + "search_in_area.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_OPEN_REPLACE_AREA, ELCL + "open_replace.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_CLOSE_REPLACE_AREA, ELCL + "close_replace.png"); //$NON-NLS-1$
		declareRegistryImage(KEY_OPEN_HISTORY, ELCL + "open_history.png"); //$NON-NLS-1$
	}

	/**
	 * Declare an Image in the registry table.
	 *
	 * @param key  the key to use when registering the image
	 * @param path the path where the image can be found. This path is relative to
	 *             where this plugin class is found (i.e. typically the packages
	 *             directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle = Platform.getBundle(TextEditorPlugin.PLUGIN_ID);
		URL url = null;
		if (bundle != null) {
			url = FileLocator.find(bundle, IPath.fromOSString(path), null);
			desc = ImageDescriptor.createFromURL(url);
		}
		fgImageRegistry.put(key, desc);
	}

	/**
	 * Returns the ImageRegistry.
	 *
	 * @return image registry
	 */
	public static ImageRegistry getImageRegistry() {
		if (fgImageRegistry == null) {
			initializeImageRegistry();
		}
		return fgImageRegistry;
	}

	/**
	 * Initialize the image registry by declaring all of the required graphics. This
	 * involves creating JFace image descriptors describing how to create/find the
	 * image should it be needed. The image is not actually allocated until
	 * requested.
	 *
	 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
	 * Property Page Banners PROPBAN_ Color toolbar CTOOL_ Enable toolbar ETOOL_
	 * Disable toolbar DTOOL_ Local enabled toolbar ELCL_ Local Disable toolbar
	 * DLCL_ Object large OBJL_ Object small OBJS_ View VIEW_ Product images PROD_
	 * Misc images MISC_
	 *
	 * Where are the images? The images (typically pngs) are found in the same
	 * location as this plugin class. This may mean the same package directory as
	 * the package holding this class. The images are declared using this.getClass()
	 * to ensure they are looked up via this plugin class.
	 *
	 * @return the image registry
	 * @see org.eclipse.jface.resource.ImageRegistry
	 */
	public static ImageRegistry initializeImageRegistry() {
		fgImageRegistry = TextEditorPlugin.getDefault().getImageRegistry();
		declareImages();
		return fgImageRegistry;
	}

	/**
	 * Returns the image managed under the given key in this registry.
	 *
	 * @param key the image's key
	 * @return the image managed under the given key
	 */
	public static Image get(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the image descriptor for the given key in this registry.
	 *
	 * @param key the image's key
	 * @return the image descriptor for the given key
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
}
