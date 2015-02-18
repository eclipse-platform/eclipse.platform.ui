/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.lang.reflect.Method;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.osgi.framework.Bundle;

/**
 * This class isolates calls to the backward compatibility layer.
 * It uses reflection so it can be loaded with success even in the absence of
 * the compatibility plugin.
 * 
 * @deprecated Marked as deprecated to suppress deprecation warnings.
 */
public class RegistryCompatibilityHelper {
	public static final String PI_RUNTIME_COMPATIBILITY = "org.eclipse.core.runtime.compatibility"; //$NON-NLS-1$
	private static Bundle compatibility = null;

	public synchronized static Bundle initializeCompatibility() {
		// if compatibility is stale (has been uninstalled or unresolved) 
		// then we try to get a new resolved compatibility bundle
		if (compatibility == null || (compatibility.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED | Bundle.STOPPING)) != 0) {
			compatibility = BundleHelper.getDefault().getBundle(PI_RUNTIME_COMPATIBILITY);
		}
		return compatibility;
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
			//Ignore the exceptions, return null			
		}
		return null;
	}
}
