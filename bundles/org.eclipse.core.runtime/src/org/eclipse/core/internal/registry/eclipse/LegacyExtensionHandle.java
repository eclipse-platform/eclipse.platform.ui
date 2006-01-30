/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry.eclipse;

import org.eclipse.core.internal.registry.ExtensionHandle;
import org.eclipse.core.internal.registry.IObjectManager;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * Implementation of org.eclipse.core.runtime.IExtension provided for 
 * backward compatibility.
 * 
 * For general use, consider ExtensionHandle class instead.
 * 
 * @since org.eclipse.core.runtime 3.2 
 */
public class LegacyExtensionHandle implements org.eclipse.core.runtime.IExtension {

	protected org.eclipse.equinox.registry.IExtension target;

	public boolean equals(Object object) {
		if (object instanceof LegacyExtensionHandle)
			return target.equals(((LegacyExtensionHandle) object).toEquinox());
		return false;
	}

	public int hashCode() {
		return target.hashCode();
	}

	public LegacyExtensionHandle(IObjectManager objectManager, int id) {
		target = new ExtensionHandle(objectManager, id);
	}

	public LegacyExtensionHandle(org.eclipse.equinox.registry.IExtension extension) {
		target = extension;
	}

	public String getNamespace() {
		return target.getNamespace();
	}

	public String getExtensionPointUniqueIdentifier() {
		return target.getExtensionPointUniqueIdentifier();
	}

	public String getLabel() {
		return target.getLabel();
	}

	public String getSimpleIdentifier() {
		return target.getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return target.getUniqueIdentifier();
	}

	public IConfigurationElement[] getConfigurationElements() {
		return LegacyRegistryConverter.convert(target.getConfigurationElements());
	}

	public boolean isValid() {
		return target.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IExtension toEquinox() {
		return target;
	}

	/**
	 * @deprecated
	 */
	public org.eclipse.core.runtime.IPluginDescriptor getDeclaringPluginDescriptor() {
		String namespace = getNamespace();
		org.eclipse.core.runtime.IPluginDescriptor result = org.eclipse.core.internal.runtime.CompatibilityHelper.getPluginDescriptor(namespace);
		if (result == null) {
			Bundle underlyingBundle = Platform.getBundle(namespace);
			if (underlyingBundle != null) {
				Bundle[] hosts = Platform.getHosts(underlyingBundle);
				if (hosts != null)
					result = org.eclipse.core.internal.runtime.CompatibilityHelper.getPluginDescriptor(hosts[0].getSymbolicName());
			}
		}
		if (org.eclipse.core.internal.runtime.CompatibilityHelper.DEBUG && result == null)
			InternalPlatform.message("Could not obtain plug-in descriptor for bundle " + namespace); //$NON-NLS-1$
		return result;
	}
}
