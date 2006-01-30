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

import org.eclipse.core.internal.registry.ExtensionPointHandle;
import org.eclipse.core.internal.registry.IObjectManager;
import org.eclipse.core.runtime.*;

/**
 * Implementation of org.eclipse.core.runtime.IExtensionPoint provided for 
 * backward compatibility.
 * For general use, consider ExtensionHandle class instead.
 *
 * @since org.eclipse.core.runtime 3.2 
 */
public class LegacyExtensionPointHandle implements IExtensionPoint {

	protected org.eclipse.equinox.registry.IExtensionPoint target;

	public boolean equals(Object object) {
		if (object instanceof LegacyExtensionPointHandle)
			return target.equals(((LegacyExtensionPointHandle) object).toEquinox());
		return false;
	}

	public int hashCode() {
		return target.hashCode();
	}

	public LegacyExtensionPointHandle(IObjectManager objectManager, int id) {
		target = new ExtensionPointHandle(objectManager, id);
	}

	public LegacyExtensionPointHandle(org.eclipse.equinox.registry.IExtensionPoint extensionPoint) {
		target = extensionPoint;
	}

	public IExtension[] getExtensions() throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getExtensions());
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getNamespace() throws InvalidRegistryObjectException {
		try {
			return target.getNamespace();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public IExtension getExtension(String extensionId) throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getExtension(extensionId));
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public IConfigurationElement[] getConfigurationElements() throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getConfigurationElements());
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getLabel() throws InvalidRegistryObjectException {
		try {
			return target.getLabel();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getSchemaReference() throws InvalidRegistryObjectException {
		try {
			return target.getSchemaReference();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getSimpleIdentifier() throws InvalidRegistryObjectException {
		try {
			return target.getSimpleIdentifier();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getUniqueIdentifier() throws InvalidRegistryObjectException {
		try {
			return target.getUniqueIdentifier();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public boolean isValid() {
		return target.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IExtensionPoint toEquinox() {
		return target;
	}

	/**
	 * @deprecated
	 */
	public org.eclipse.core.runtime.IPluginDescriptor getDeclaringPluginDescriptor() {
		return org.eclipse.core.internal.runtime.CompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}
