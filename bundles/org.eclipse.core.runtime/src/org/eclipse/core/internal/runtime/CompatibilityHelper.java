/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.lang.reflect.Method;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

//TODO Some comments could be welcomed here.
public class CompatibilityHelper {
	public static void setPlugin(IPluginDescriptor descriptor, Plugin plugin) {
		//Here we use reflection so the runtime code can run without the compatibility
		Bundle compatibility = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle(IPlatform.PI_RUNTIME_COMPATIBILITY);
		if (compatibility == null)
			return;

		try {
			Method setPlugin = descriptor.getClass().getMethod("setPlugin", new Class[] { Plugin.class }); //$NON-NLS-1$
			setPlugin.invoke(descriptor, new Object[] { plugin });
		} catch (Exception e) {
			//Ignore the exceptions, return false
		}
	}
	public static IPluginDescriptor getPluginDescriptor(String pluginId) {
		//Here we use reflection so the runtime code can run without the compatibility
		Bundle compatibility = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle(IPlatform.PI_RUNTIME_COMPATIBILITY);
		if (compatibility == null)
			return null;

		Class oldInternalPlatform = null;
		try {
			oldInternalPlatform = compatibility.loadClass("org.eclipse.core.internal.plugins.InternalPlatform"); //$NON-NLS-1$
			Method getPluginDescriptor = oldInternalPlatform.getMethod("getPluginDescriptor", new Class[] { String.class }); //$NON-NLS-1$
			return (IPluginDescriptor) getPluginDescriptor.invoke(oldInternalPlatform, new Object[] { pluginId });
		} catch (Exception e) {
			//Ignore the exceptions, return false
		}
		return null;
	}
	
	public static void setActive(IPluginDescriptor descriptor) {
		Bundle compatibility = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle(IPlatform.PI_RUNTIME_COMPATIBILITY);
		if (compatibility == null)
			return;

		try {
			Method setPlugin = descriptor.getClass().getMethod("setActive", null); //$NON-NLS-1$
			setPlugin.invoke(descriptor, null);
		} catch (Exception e) {
			//Ignore the exceptions, return false
		}
	}
}
