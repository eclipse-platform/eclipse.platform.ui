package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.core.SiteManager;

import java.io.File;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateManagerPlugin extends Plugin {
	
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_SHOW_INSTALL = false;
	public static boolean DEBUG_SHOW_PARSING = false;
	public static boolean DEBUG_SHOW_WARNINGS = false;
	public static boolean DEBUG_SHOW_CONFIGURATION = false;	
	public static boolean DEBUG_SHOW_TYPE = false;

	
	//The shared instance.
	private static UpdateManagerPlugin plugin;
	//Resource bundle.
	
	/**
	 * The constructor.
	 */
	public UpdateManagerPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateManagerPlugin getPlugin() {
		return plugin;
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
				result = Platform.getDebugOption("org.eclipse.update.core/debug/warning");
				DEBUG_SHOW_WARNINGS = result.trim().equalsIgnoreCase("true");
				
				result = Platform.getDebugOption("org.eclipse.update.core/debug/parsing");
				DEBUG_SHOW_PARSING = result.trim().equalsIgnoreCase("true");
				
				result = Platform.getDebugOption("org.eclipse.update.core/debug/install");
				DEBUG_SHOW_INSTALL = result.trim().equalsIgnoreCase("true");

				result = Platform.getDebugOption("org.eclipse.update.core/debug/configuration");
				DEBUG_SHOW_CONFIGURATION = result.trim().equalsIgnoreCase("true");			
				
				result = Platform.getDebugOption("org.eclipse.update.core/debug/type");
				DEBUG_SHOW_TYPE = result.trim().equalsIgnoreCase("true");						
				
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
