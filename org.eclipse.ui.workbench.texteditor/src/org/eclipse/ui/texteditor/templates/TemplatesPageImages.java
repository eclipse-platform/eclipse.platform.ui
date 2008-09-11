/*******************************************************************************
 * Copyright (c) 2007, 2008 Dakshinamurthy Karra, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import java.net.URL;

import org.osgi.framework.Bundle;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Bundle of the images used by the {@link AbstractTemplatesPage}.
 *
 * @since 3.4
 */
class TemplatesPageImages {

	static final String PREFIX_ELCL= TextEditorPlugin.PLUGIN_ID + ".elcl."; //$NON-NLS-1$

	static final String PREFIX_DLCL= TextEditorPlugin.PLUGIN_ID + ".dlcl."; //$NON-NLS-1$

	static final String PREFIX_OBJ= TextEditorPlugin.PLUGIN_ID + ".obj."; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_NEW= PREFIX_ELCL + "new_template.gif"; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_DELETE= PREFIX_ELCL + "delete_template.gif"; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_EDIT= PREFIX_ELCL + "edit_template.gif"; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_INSERT= PREFIX_ELCL + "insert_template.gif"; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_LINK= PREFIX_ELCL + "link_to_editor.gif"; //$NON-NLS-1$

	public static final String IMG_ELCL_TEMPLATE_COLLAPSE_ALL= PREFIX_ELCL + "collapseall.gif"; //$NON-NLS-1$

	public static final String IMG_DLCL_TEMPLATE_DELETE= PREFIX_DLCL + "delete_template.gif"; //$NON-NLS-1$

	public static final String IMG_DLCL_TEMPLATE_EDIT= PREFIX_DLCL + "edit_template.gif"; //$NON-NLS-1$

	public static final String IMG_DLCL_TEMPLATE_INSERT= PREFIX_DLCL + "insert_template.gif"; //$NON-NLS-1$

	public static final String IMG_OBJ_PREVIEW= PREFIX_OBJ + "preview.gif"; //$NON-NLS-1$

	public static final String IMG_OBJ_CONTEXT= PREFIX_OBJ + "context.gif"; //$NON-NLS-1$

	public static final String IMG_OBJ_TEMPLATE= PREFIX_OBJ + "template_obj.gif"; //$NON-NLS-1$

	/**
	 * The image registry containing {@link Image images}.
	 */
	private static ImageRegistry fgImageRegistry;

	private static String ICONS_PATH= "$nl$/icons/full/"; //$NON-NLS-1$

	// Use IPath and toOSString to build the names to ensure they have the
	// slashes correct
	private final static String ELCL= ICONS_PATH + "elcl16/"; //$NON-NLS-1$

	private final static String DLCL= ICONS_PATH + "dlcl16/"; //$NON-NLS-1$

	private final static String OBJ= ICONS_PATH + "obj16/"; //$NON-NLS-1$

	/**
	 * Declare all images
	 */
	private static void declareImages() {
		// Ant Editor images
		declareRegistryImage(IMG_ELCL_TEMPLATE_NEW, ELCL + "new_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_ELCL_TEMPLATE_INSERT, ELCL + "insert_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_DLCL_TEMPLATE_INSERT, DLCL + "insert_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_ELCL_TEMPLATE_DELETE, ELCL + "delete_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_DLCL_TEMPLATE_DELETE, DLCL + "delete_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_ELCL_TEMPLATE_EDIT, ELCL + "edit_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_DLCL_TEMPLATE_EDIT, DLCL + "edit_template.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_ELCL_TEMPLATE_LINK, ELCL + "link_to_editor.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_ELCL_TEMPLATE_COLLAPSE_ALL, ELCL + "collapseall.gif"); //$NON-NLS-1$

		declareRegistryImage(IMG_OBJ_PREVIEW, OBJ + "preview.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OBJ_CONTEXT, OBJ + "context.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OBJ_TEMPLATE, OBJ + "template_obj.gif"); //$NON-NLS-1$
	}

	/**
	 * Declare an Image in the registry table.
	 *
	 * @param key the key to use when registering the image
	 * @param path the path where the image can be found. This path is relative to where this plugin
	 *            class is found (i.e. typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle= Platform.getBundle(TextEditorPlugin.PLUGIN_ID);
		URL url= null;
		if (bundle != null) {
			url= FileLocator.find(bundle, new Path(path), null);
			desc= ImageDescriptor.createFromURL(url);
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
	 * Initialize the image registry by declaring all of the required graphics. This involves
	 * creating JFace image descriptors describing how to create/find the image should it be needed.
	 * The image is not actually allocated until requested.
	 *
	 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_ Property Page Banners
	 * PROPBAN_ Color toolbar CTOOL_ Enable toolbar ETOOL_ Disable toolbar DTOOL_ Local enabled
	 * toolbar ELCL_ Local Disable toolbar DLCL_ Object large OBJL_ Object small OBJS_ View VIEW_
	 * Product images PROD_ Misc images MISC_
	 *
	 * Where are the images? The images (typically gifs) are found in the same location as this
	 * plugin class. This may mean the same package directory as the package holding this class. The
	 * images are declared using this.getClass() to ensure they are looked up via this plugin class.
	 *
	 * @return the image registry
	 * @see org.eclipse.jface.resource.ImageRegistry
	 */
	public static ImageRegistry initializeImageRegistry() {
		fgImageRegistry= TextEditorPlugin.getDefault().getImageRegistry();
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
