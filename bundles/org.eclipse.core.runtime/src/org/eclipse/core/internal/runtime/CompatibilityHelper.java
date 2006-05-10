/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.lang.reflect.Method;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * This class isolates calls to the backward compatibility layer.
 * It uses reflection so it can be loaded with success even in the absence of
 * the compatibility plugin.
 * 
 * @deprecated Marked as deprecated to suppress deprecation warnings.
 */
public class CompatibilityHelper {
	private static final String OPTION_DEBUG_COMPATIBILITY = Platform.PI_RUNTIME + "/compatibility/debug"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.TRUE.toString().equalsIgnoreCase(InternalPlatform.getDefault().getOption(OPTION_DEBUG_COMPATIBILITY));
	public static final String PI_RUNTIME_COMPATIBILITY = "org.eclipse.core.runtime.compatibility"; //$NON-NLS-1$
	private static Bundle compatibility = null;

	public synchronized static void nullCompatibility() {
		compatibility = null;
	}
	
	public synchronized static Bundle initializeCompatibility() {
		// if compatibility is stale (has been uninstalled or unresolved) 
		// then we try to get a new resolved compatibility bundle
		if (compatibility == null || (compatibility.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED | Bundle.STOPPING)) != 0)
			compatibility = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle(PI_RUNTIME_COMPATIBILITY);
		return compatibility;
	}

	public static void setPlugin(IPluginDescriptor descriptor, Plugin plugin) {
		//Here we use reflection so the runtime code can run without the compatibility plugin
		if (initializeCompatibility() == null)
			throw new IllegalStateException();

		try {
			Method setPlugin = descriptor.getClass().getMethod("setPlugin", new Class[] {Plugin.class}); //$NON-NLS-1$
			setPlugin.invoke(descriptor, new Object[] {plugin});
		} catch (Exception e) {
			//Ignore the exceptions
		}
	}

	public synchronized static IPluginDescriptor getPluginDescriptor(String pluginId) {
		//Here we use reflection so the runtime code can run without the compatibility
		initializeCompatibility();
		if (compatibility == null)
			throw new IllegalStateException();

		Class oldInternalPlatform = null;
		try {
			oldInternalPlatform = compatibility.loadClass("org.eclipse.core.internal.plugins.InternalPlatform"); //$NON-NLS-1$
			Method getPluginDescriptor = oldInternalPlatform.getMethod("getPluginDescriptor", new Class[] {String.class}); //$NON-NLS-1$
			return (IPluginDescriptor) getPluginDescriptor.invoke(oldInternalPlatform, new Object[] {pluginId});
		} catch (Exception e) {
			if (DEBUG) {
				String msg = "Error running compatibility code"; //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 1, msg, e);
				InternalPlatform.getDefault().log(error);
			}
			//Ignore the exceptions, return null			
		}
		return null;
	}

	public synchronized static void setActive(IPluginDescriptor descriptor) {
		initializeCompatibility();
		if (compatibility == null)
			throw new IllegalStateException();

		try {
			Method setPlugin = descriptor.getClass().getMethod("setActive", null); //$NON-NLS-1$
			setPlugin.invoke(descriptor, null);
		} catch (Exception e) {
			//Ignore the exceptions
		}
	}

	public synchronized static boolean hasPluginObject(IPluginDescriptor descriptor) {
		initializeCompatibility();
		if (compatibility == null)
			throw new IllegalStateException();

		Boolean result = Boolean.FALSE;
		try {
			Method setPlugin = descriptor.getClass().getMethod("hasPluginObject", null); //$NON-NLS-1$
			result = (Boolean) setPlugin.invoke(descriptor, null);
		} catch (Exception e) {
			//Ignore the exceptions			
		}
		return result.booleanValue();
	}
}
