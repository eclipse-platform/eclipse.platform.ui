/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
 * 
 * For general use, consider ExtensionHandle class instead.
 */
public class LegacyExtensionPointHandle implements IExtensionPoint {

	protected org.eclipse.equinox.registry.IExtensionPoint target;

	public boolean equals(Object object) {
		if (object instanceof LegacyExtensionPointHandle)
			return target.equals(((LegacyExtensionPointHandle) object).getInternalHandle());
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

	public IExtension[] getExtensions() {
		return LegacyRegistryConverter.convert(target.getExtensions());
	}

	public String getNamespace() {
		return target.getNamespace();
	}

	public IExtension getExtension(String extensionId) {
		return LegacyRegistryConverter.convert(target.getExtension(extensionId));
	}

	public IConfigurationElement[] getConfigurationElements() {
		return LegacyRegistryConverter.convert(target.getConfigurationElements());
	}

	public String getLabel() {
		return target.getLabel();
	}

	public String getSchemaReference() {
		return target.getSchemaReference();
	}

	public String getSimpleIdentifier() {
		return target.getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return target.getUniqueIdentifier();
	}

	public boolean isValid() {
		return target.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IExtensionPoint getInternalHandle() {
		return target;
	}

	/**
	 * @deprecated
	 */
	public org.eclipse.core.runtime.IPluginDescriptor getDeclaringPluginDescriptor() {
		return org.eclipse.core.internal.runtime.CompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}
