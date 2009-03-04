/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences.legacy;

import org.eclipse.core.internal.preferences.exchange.ILegacyPreferences;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * Provides initialization of the legacy preferences as described in
 * the Plugin class.
 */
public class InitLegacyPreferences implements ILegacyPreferences {
	/**
	 * The method tries to initialize the preferences using the legacy Plugin method.
	 * 
	 * @param object - plugin to initialize
	 * @param name - ID of the plugin to be initialized
	 * 
	 * @see Plugin#initializeDefaultPluginPreferences
	 * 
	 * @since org.eclipse.core.runtime 3.2
	 * 
	 * @deprecated Marked deprecated to suppress warnings. This class is added to support
	 * backward compatibility only and should not be used in any new code.
	 */
	public Object init(Object object, String name) {
		Plugin plugin = null;
		if (object instanceof Plugin)
			plugin = (Plugin) object;
		// No extension exists. Get the plug-in object and call #initializeDefaultPluginPreferences().
		// We can only call this if the runtime compatibility layer is installed.
		if (plugin == null && InternalPlatform.getDefault().getBundle(org.eclipse.core.internal.runtime.CompatibilityHelper.PI_RUNTIME_COMPATIBILITY) != null)
			plugin = Platform.getPlugin(name);
		if (plugin == null) {
			if (InternalPlatform.DEBUG_PLUGIN_PREFERENCES)
				InternalPlatform.message("No plug-in object available to set plug-in default preference overrides for:" + name); //$NON-NLS-1$
			return null;
		}
		if (InternalPlatform.DEBUG_PLUGIN_PREFERENCES)
			InternalPlatform.message("Applying plug-in default preference overrides for plug-in: " + plugin.getDescriptor().getUniqueIdentifier()); //$NON-NLS-1$

		plugin.internalInitializeDefaultPluginPreferences();
		return plugin;
	}

}
