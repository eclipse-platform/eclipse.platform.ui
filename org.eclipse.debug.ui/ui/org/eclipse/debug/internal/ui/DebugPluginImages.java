package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * The images provided by the debug plugin.
 */
public class DebugPluginImages {

	/** 
	 * The image registry containing <code>Image</code>s.
	 */
	private static ImageRegistry imageRegistry;
	
	/**
	 * A table of all the <code>ImageDescriptor</code>s.
	 */
	private static HashMap imageDescriptors;

	/* Declare Common paths */
	private static URL ICON_BASE_URL= null;
	static {
		try {
			ICON_BASE_URL= new URL(DebugUIPlugin.getDefault().getDescriptor().getInstallURL(), "icons/");
		} catch (MalformedURLException e) {
			// do nothing
		}
	}

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String CTOOL= "full/ctool16/"; //basic colors - size 16x16
	private final static String LOCALTOOL= "full/clcl16/"; //basic colors - size 16x16
	private final static String DLCL= "full/dlcl16/"; //disabled - size 16x16
	private final static String ELCL= "full/elcl16/"; //enabled - size 16x16
	private final static String OBJECT= "full/obj16/"; //basic colors - size 16x16
	private final static String WIZBAN= "full/wizban/"; //basic colors - size 16x16
	
	/**
	 * Declare all images
	 */
	private static void declareImages() {
		// Actions
		declareRegistryImage(IDebugUIConstants.IMG_ACT_DEBUG, CTOOL + "debug_exc.gif");
		declareRegistryImage(IDebugUIConstants.IMG_ACT_RUN, CTOOL + "run_exc.gif");

		//menus

		//Local toolbars
		declareRegistryImage(IDebugUIConstants.IMG_LCL_RESUME, LOCALTOOL + "resume_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_SUSPEND, LOCALTOOL + "suspend_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TERMINATE, LOCALTOOL + "terminate_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_DISCONNECT, LOCALTOOL + "disconnect_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPINTO, LOCALTOOL + "stepinto_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPOVER, LOCALTOOL + "stepover_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPRETURN, LOCALTOOL + "stepreturn_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_CLEAR, LOCALTOOL + "clear_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED, LOCALTOOL + "rem_all_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_QUALIFIED_NAMES, LOCALTOOL + "qnames_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TYPE_NAMES, LOCALTOOL + "tnames_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE, LOCALTOOL + "rem_co.gif");
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE_ALL, LOCALTOOL + "rem_all_co.gif");

		// disabled local toolbars
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_RESUME, DLCL + "resume_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_SUSPEND, DLCL + "suspend_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TERMINATE, DLCL + "terminate_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT, DLCL + "disconnect_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPINTO, DLCL + "stepinto_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPOVER, DLCL + "stepover_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPRETURN, DLCL + "stepreturn_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_CLEAR, DLCL + "clear_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED, DLCL + "rem_all_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_QUALIFIED_NAMES, DLCL + "qnames_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TYPE_NAMES, DLCL + "tnames_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE, DLCL + "rem_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL, DLCL + "rem_all_co.gif");

		// enabled local toolbars
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_RESUME, ELCL + "resume_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_SUSPEND, ELCL + "suspend_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TERMINATE, ELCL + "terminate_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_DISCONNECT, ELCL + "disconnect_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPINTO, ELCL + "stepinto_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPOVER, ELCL + "stepover_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPRETURN, ELCL + "stepreturn_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_CLEAR, ELCL + "clear_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED, ELCL + "rem_all_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_QUALIFIED_NAMES, ELCL + "qnames_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TYPE_NAMES, ELCL + "tnames_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE, ELCL + "rem_co.gif");
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL, ELCL + "rem_all_co.gif");


		//Object
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_LAUNCH_DEBUG, OBJECT + "ldebug_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_LAUNCH_RUN, OBJECT + "lrun_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET, OBJECT + "debugt_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED, OBJECT + "debugtt_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING, OBJECT + "thread_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED, OBJECT + "threads_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED, OBJECT + "threadt_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_STACKFRAME, OBJECT + "stckframe_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT, OBJECT + "brkp_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED, OBJECT + "brkpd_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_OS_PROCESS, OBJECT + "osprc_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED, OBJECT + "osprct_obj.gif");
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_EXPRESSION, OBJECT + "expression_obj.gif");
		
		//Wizard Banners
		declareRegistryImage(IDebugUIConstants.IMG_WIZBAN_DEBUG, WIZBAN + "debug_wiz.gif");
		declareRegistryImage(IDebugUIConstants.IMG_WIZBAN_RUN, WIZBAN + "run_wiz.gif");
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
		imageRegistry= new ImageRegistry();
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


