package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

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

	private static final String ATTR_LAUNCH_CONFIG_TYPE_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_LAUNCH_CONFIG_TYPE_ID = "configTypeID"; //$NON-NLS-1$
	
	/* Declare Common paths */
	private static URL ICON_BASE_URL= null;

	static {
		String pathSuffix = "icons/full/"; //$NON-NLS-1$
			
		try {
			ICON_BASE_URL= new URL(DebugUIPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix);
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
	
	/**
	 * Declare all images
	 */
	private static void declareImages() {
		// Actions
		declareRegistryImage(IDebugUIConstants.IMG_ACT_DEBUG, CTOOL + "debug_exc.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_ACT_RUN, CTOOL + "run_exc.gif"); //$NON-NLS-1$

		//menus

		//Local toolbars
		declareRegistryImage(IDebugUIConstants.IMG_LCL_RESUME, LOCALTOOL + "resume_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_SUSPEND, LOCALTOOL + "suspend_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TERMINATE, LOCALTOOL + "terminate_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TERMINATE_ALL, LOCALTOOL + "terminate_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TERMINATE_AND_REMOVE, LOCALTOOL + "terminate_rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_DISCONNECT, LOCALTOOL + "disconnect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPINTO, LOCALTOOL + "stepinto_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPOVER, LOCALTOOL + "stepover_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_STEPRETURN, LOCALTOOL + "stepreturn_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_CLEAR, LOCALTOOL + "clear_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED, LOCALTOOL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_TYPE_NAMES, LOCALTOOL + "tnames_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE, LOCALTOOL + "rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_REMOVE_ALL, LOCALTOOL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_INSPECT, LOCALTOOL + "inspect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_RELAUNCH, LOCALTOOL + "relaunch_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_LCL_COPY, LOCALTOOL + "copy_edit.gif"); //$NON-NLS-1$
			
		// disabled local toolbars
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_RESUME, DLCL + "resume_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_SUSPEND, DLCL + "suspend_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TERMINATE, DLCL + "terminate_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_ALL, DLCL + "terminate_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_REMOVE, DLCL + "terminate_rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT, DLCL + "disconnect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPINTO, DLCL + "stepinto_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPOVER, DLCL + "stepover_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_STEPRETURN, DLCL + "stepreturn_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_CLEAR, DLCL + "clear_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED, DLCL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_TYPE_NAMES, DLCL + "tnames_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE, DLCL + "rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL, DLCL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_INSPECT, DLCL + "inpsect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_RELAUNCH, DLCL + "relaunch_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_DLCL_COPY, DLCL + "copy_edit.gif"); //$NON-NLS-1$

		// enabled local toolbars
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_RESUME, ELCL + "resume_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_SUSPEND, ELCL + "suspend_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TERMINATE, ELCL + "terminate_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL, ELCL + "terminate_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE, ELCL + "terminate_rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_DISCONNECT, ELCL + "disconnect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPINTO, ELCL + "stepinto_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPOVER, ELCL + "stepover_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_STEPRETURN, ELCL + "stepreturn_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_CLEAR, ELCL + "clear_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED, ELCL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_TYPE_NAMES, ELCL + "tnames_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE, ELCL + "rem_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL, ELCL + "rem_all_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_INSPECT, ELCL + "inpsect_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_RELAUNCH, ELCL + "relaunch_co.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_COPY, ELCL + "copy_edit.gif"); //$NON-NLS-1$
		declareRegistryImage(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE, ELCL + "hidenav.gif"); //$NON-NLS-1$


		//Object
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_LAUNCH_DEBUG, OBJECT + "ldebug_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_LAUNCH_RUN, OBJECT + "lrun_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET, OBJECT + "debugt_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED, OBJECT + "debugtt_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING, OBJECT + "thread_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED, OBJECT + "threads_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED, OBJECT + "threadt_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_STACKFRAME, OBJECT + "stckframe_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING, OBJECT + "stckframe_running_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT, OBJECT + "brkp_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED, OBJECT + "brkpd_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_OS_PROCESS, OBJECT + "osprc_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED, OBJECT + "osprct_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_EXPRESSION, OBJECT + "expression_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_OBJS_LAUNCH_CONFIGURATION, OBJECT + "launch_config.gif"); //$NON-NLS-1$
		
		//Wizard Banners
		declareRegistryImage(IDebugUIConstants.IMG_WIZBAN_DEBUG, WIZBAN + "debug_wiz.gif"); //$NON-NLS-1$
		declareRegistryImage(IDebugUIConstants.IMG_WIZBAN_RUN, WIZBAN + "run_wiz.gif"); //$NON-NLS-1$
		
		// launchers
		ILauncher[] launchers = DebugPlugin.getDefault().getLaunchManager().getLaunchers();
		for (int i = 0; i < launchers.length; i++) {
			ILauncher launcher = launchers[i];
			String iconPath = launcher.getIconPath();
			if (iconPath != null) {
				URL iconURL = launcher.getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL();
				ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
				try {
					iconURL = new URL(iconURL, iconPath);			
					desc= ImageDescriptor.createFromURL(iconURL);
				} catch (MalformedURLException e) {
				} 
				imageRegistry.put(launcher.getIdentifier(), desc);				
				imageDescriptors.put(launcher.getIdentifier(), desc);
			}
		}
		
		// launch configuration types
		IPluginDescriptor pluginDescriptor = DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= pluginDescriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPE_IMAGES);
		IConfigurationElement[] configElements= extensionPoint.getConfigurationElements();
		for (int i = 0; i < configElements.length; i++) {
			IConfigurationElement configElement = configElements[i];
			URL iconURL = configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL();
			String iconPath = configElement.getAttribute(ATTR_LAUNCH_CONFIG_TYPE_ICON);
			ImageDescriptor imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
			try {
				iconURL = new URL(iconURL, iconPath);
				imageDescriptor = ImageDescriptor.createFromURL(iconURL);
			} catch (MalformedURLException mue) {
			}
			String configTypeID = configElement.getAttribute(ATTR_LAUNCH_CONFIG_TYPE_ID);			
			imageRegistry.put(configTypeID, imageDescriptor);				
			imageDescriptors.put(configTypeID, imageDescriptor);
		}
		
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
		imageRegistry= new ImageRegistry(DebugUIPlugin.getStandardDisplay());
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


