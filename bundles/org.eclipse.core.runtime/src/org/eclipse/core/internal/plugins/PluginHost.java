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
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.registry.*;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionPoint;

public class PluginHost implements IHost {
	private IPluginDescriptor pluginDescriptor;

	public PluginHost(IPluginDescriptor pluginDescriptor) {
		this.pluginDescriptor = pluginDescriptor;
	}

	public IExtension[] getExtensions() {
		return Utils.convertExtensions(pluginDescriptor.getExtensions());
	}

	public IExtensionPoint getExtensionPoint(String xpt) {
		org.eclipse.core.runtime.IExtensionPoint extensioPoint = pluginDescriptor.getExtensionPoint(xpt);
		if (extensioPoint == null)
			return null;
		return new ExtensionPointWrapper(extensioPoint);
	}

	public String getHostId() {
		return pluginDescriptor.getUniqueIdentifier();
	}

	public IExtensionPoint[] getExtensionPoints() {
		return Utils.convertExtensionPoints(pluginDescriptor.getExtensionPoints());		
	}
}
