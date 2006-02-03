/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.osgi.framework.Bundle;

/**
 * This class contains only compatibility-specific code.
 * 
 * @deprecated marked as deprecated to supress warnings
 */
public class ExtensionHandle extends BaseExtensionHandle {

	public ExtensionHandle(IObjectManager objectManager, int id) {
		super(objectManager, id);
	}

	public IPluginDescriptor getDeclaringPluginDescriptor() throws InvalidRegistryObjectException {
		String namespace = getNamespace();
		IPluginDescriptor result = RegistryCompatibilityHelper.getPluginDescriptor(namespace);
		if (result == null) {
			Bundle underlyingBundle = BundleHelper.getDefault().getBundle(namespace);
			if (underlyingBundle != null) {
				Bundle[] hosts = BundleHelper.getDefault().getHosts(underlyingBundle);
				if (hosts != null)
					result = RegistryCompatibilityHelper.getPluginDescriptor(hosts[0].getSymbolicName());
			}
		}

		return result;
	}
}
