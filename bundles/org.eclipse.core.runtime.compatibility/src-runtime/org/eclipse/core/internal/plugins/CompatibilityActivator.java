/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.runtime.CompatibilityHelper;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CompatibilityActivator implements BundleActivator {
	public void start(BundleContext context) throws Exception {
		IPluginDescriptor descriptor = CompatibilityHelper.getPluginDescriptor(Platform.PI_RUNTIME);
		CompatibilityHelper.setPlugin(descriptor, InternalPlatform.getDefault().getRuntimeInstance());
		CompatibilityHelper.setActive(descriptor);
	}

	public void stop(BundleContext context) throws Exception {
		IPluginDescriptor descriptor = CompatibilityHelper.getPluginDescriptor(Platform.PI_RUNTIME);
		CompatibilityHelper.setPlugin(descriptor, null);
		((PluginRegistry) org.eclipse.core.internal.plugins.InternalPlatform.getPluginRegistry()).close();
		CompatibilityHelper.nullCompatibility();
	}

}
