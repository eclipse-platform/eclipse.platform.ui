/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.variables;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * The plug-in runtime class for the Core Variables plug-in.
 * @since 3.0
 */
public class VariablesPlugin extends Plugin {

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;		
	
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static VariablesPlugin plugin;

	/**
	 * Unique identifier constant (value <code>"org.eclipse.core.variables"</code>)
	 * for the Core Variables plug-in.
	 */
	public static final String PI_CORE_VARIABLES = "org.eclipse.core.variables"; //$NON-NLS-1$


	/** 
	 * Constructs an instance of this plug-in runtime class.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by the Variables plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   Variables plug-in
	 */
	public VariablesPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns this plug-in instance.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static VariablesPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, PI_CORE_VARIABLES, INTERNAL_ERROR, "Error logged from Core Variables: ", t); //$NON-NLS-1$
		getDefault().getLog().log(status);
	}
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PI_CORE_VARIABLES;
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}
}
