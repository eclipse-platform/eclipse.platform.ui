package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateManagerPlugin extends Plugin {
	
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_SHOW_INSTALL = true;
	public static boolean DEBUG_SHOW_PARSING = true;
	public static boolean DEBUG_SHOW_WARNINGS = true;
	public static boolean 	DEBUG_SHOW_CONFIGURATION = true;	

	
	//The shared instance.
	private static UpdateManagerPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public UpdateManagerPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.update.core.UpdateManagerPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateManagerPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= UpdateManagerPlugin.getPlugin().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();

		String result = null;
		result = Platform.getDebugOption("org.eclipse.update.core/debug");
		if (result != null) {
			DEBUG = result.trim().equalsIgnoreCase("true");

			if (DEBUG) {
				result = Platform.getDebugOption("org.eclipse.update.core/debug/warnings");
				DEBUG_SHOW_WARNINGS = result.trim().equalsIgnoreCase("true");
				
				result = Platform.getDebugOption("org.eclipse.update.core/debug/parsing");
				DEBUG_SHOW_PARSING = result.trim().equalsIgnoreCase("true");
				
				result = Platform.getDebugOption("org.eclipse.update.core/debug/install");
				DEBUG_SHOW_INSTALL = result.trim().equalsIgnoreCase("true");

				result = Platform.getDebugOption("org.eclipse.update.core/debug/configurations");
				DEBUG_SHOW_CONFIGURATION = result.trim().equalsIgnoreCase("true");				
				
			}
		}
	}

	/**
	 * dumps a String in the trace
	 */
	public void debug(String s){
		System.out.println(toString()+"^"+Integer.toHexString(Thread.currentThread().hashCode())+" "+s);
	}

}
