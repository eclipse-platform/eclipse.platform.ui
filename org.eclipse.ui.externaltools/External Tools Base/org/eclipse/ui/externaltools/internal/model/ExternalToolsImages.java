package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * The images provided by the external tools plugin.
 */
public class ExternalToolsImages {

	/** 
	 * The image registry containing <code>Image</code>s.
	 */
	private static ImageRegistry imageRegistry;
	
	/**
	 * A table of all the <code>ImageDescriptor</code>s.
	 */
	private static Map imageDescriptors;

	/* Declare Common paths */
	private static URL ICON_BASE_URL= null;

	static {
		String pathSuffix = "icons/full/"; //$NON-NLS-1$
			
		try {
			ICON_BASE_URL= new URL(ExternalToolsPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix);
		} catch (MalformedURLException e) {
			// do nothing
		}
	}

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String CTOOL= "ctool16/"; //basic colors - size 16x16 //$NON-NLS-1$
	private final static String LOCALTOOL= "clcl16/"; //basic colors - size 16x16 //$NON-NLS-1$
	private final static String DLCL= "dlcl16/"; //disabled - size 16x16 //$NON-NLS-1$
	private final static String ELCL= "elcl16/"; //enabled - size 16x16 //$NON-NLS-1$
	private final static String OBJECT= "obj16/"; //basic colors - size 16x16 //$NON-NLS-1$
	private final static String WIZBAN= "wizban/"; //basic colors - size 16x16 //$NON-NLS-1$
	private final static String OVR= "ovr16/"; //basic colors - size 7x8 //$NON-NLS-1$
	private final static String VIEW= "cview16/"; // views //$NON-NLS-1$
	
	/**
	 * Declare all images
	 */
	private static void declareImages() {
		// Ant View Actions
		declareRegistryImage(IExternalToolsUIConstants.IMG_REMOVE, LOCALTOOL + "remove_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_REMOVE_ALL, LOCALTOOL + "removeAll_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ADD, LOCALTOOL + "add_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_RUN, LOCALTOOL + "run_tool.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_SEARCH, LOCALTOOL + "search.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_MOVE_UP, LOCALTOOL + "moveUp.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_MOVE_DOWN, LOCALTOOL + "moveDown.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ACTIVATE, LOCALTOOL + "activate.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_DEACTIVATE, LOCALTOOL + "deactivate.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_GO_TO_FILE, LOCALTOOL + "gotoobj_tsk.gif"); //$NON-NLS-1$
		// Ant View Labels
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_PROJECT, OBJECT + "file_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_PROJECT_ERROR, LOCALTOOL + "ant_project_err.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_TARGET, LOCALTOOL + "ant_target.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ERROR, LOCALTOOL + "error.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ELEMENTS, LOCALTOOL + "elements.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ELEMENT, LOCALTOOL + "element.gif"); //$NON-NLS-1$
		// Wizards
		declareRegistryImage(IExternalToolConstants.IMG_WIZBAN_EXTERNAL_TOOLS, WIZBAN + "ext_tools_wiz.gif"); //$NON-NLS-1$
		
		// Actions
		declareRegistryImage(IExternalToolConstants.IMG_ACTION_REFRESH, LOCALTOOL + "refresh.gif"); //$NON-NLS-1$
		
		// Objects
		declareRegistryImage(IExternalToolConstants.IMG_TAB_MAIN, OBJECT + "main_tab.gif"); //$NON-NLS-1$
		declareRegistryImage(IExternalToolConstants.IMG_TAB_OPTIONS, OBJECT + "options_tab.gif"); //$NON-NLS-1$
		
		//ANT object
		declareRegistryImage(IExternalToolConstants.IMG_TAB_ANT_TARGETS, LOCALTOOL + "ant_tsk_check.gif"); //$NON-NLS-1$
	}

	/**
	 * Declare an Image in the registry table.
	 * @param key 	The key to use when registering the image
	 * @param path	The path where the image can be found. This path is relative to where
	 *				this plugin class is found (i.e. typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
		try {
			desc= ImageDescriptor.createFromURL(makeIconFileURL(path));
		} catch (MalformedURLException me) {
			//ExternalToolsPlugin.log(me);
		}
		imageRegistry.put(key, desc);
		imageDescriptors.put(key, desc);
	}
	
	/**
	 * Returns the ImageRegistry.
	 */
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			initializeImageRegistry();
		}
		return imageRegistry;
	}

	/**
	 *	Initialize the image registry by declaring all of the required
	 *	graphics. This involves creating JFace image descriptors describing
	 *	how to create/find the image should it be needed.
	 *	The image is not actually allocated until requested.
	 *
	 * 	Prefix conventions
	 *		Wizard Banners			WIZBAN_
	 *		Preference Banners		PREF_BAN_
	 *		Property Page Banners	PROPBAN_
	 *		Color toolbar			CTOOL_
	 *		Enable toolbar			ETOOL_
	 *		Disable toolbar			DTOOL_
	 *		Local enabled toolbar	ELCL_
	 *		Local Disable toolbar	DLCL_
	 *		Object large			OBJL_
	 *		Object small			OBJS_
	 *		View 					VIEW_
	 *		Product images			PROD_
	 *		Misc images				MISC_
	 *
	 *	Where are the images?
	 *		The images (typically gifs) are found in the same location as this plugin class.
	 *		This may mean the same package directory as the package holding this class.
	 *		The images are declared using this.getClass() to ensure they are looked up via
	 *		this plugin class.
	 *	@see JFace's ImageRegistry
	 */
	public static ImageRegistry initializeImageRegistry() {
		imageRegistry= new ImageRegistry(ExternalToolsPlugin.getStandardDisplay());
		imageDescriptors = new HashMap(30);
		declareImages();
		return imageRegistry;
	}

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	
	/**
	 * Returns the <code>ImageDescriptor<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		if (imageDescriptors == null) {
			initializeImageRegistry();
		}
		return (ImageDescriptor)imageDescriptors.get(key);
	}
	
	private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}
			
		return new URL(ICON_BASE_URL, iconPath);
	}
}


