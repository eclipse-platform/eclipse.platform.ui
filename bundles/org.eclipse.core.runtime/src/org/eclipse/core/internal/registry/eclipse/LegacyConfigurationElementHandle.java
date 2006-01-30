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

import org.eclipse.core.internal.registry.ConfigurationElementHandle;
import org.eclipse.core.internal.registry.IObjectManager;
import org.eclipse.core.runtime.*;

/**
 * Implementation of org.eclipse.core.runtime.IConfigurationElement provided for 
 * backward compatibility.
 * 
 * For general use, consider ConfigurationElementHandle class instead.
 * 
 * @since org.eclipse.core.runtime 3.2 
 */
public class LegacyConfigurationElementHandle implements org.eclipse.core.runtime.IConfigurationElement {

	protected org.eclipse.equinox.registry.IConfigurationElement target;

	public boolean equals(Object object) {
		if (object instanceof LegacyConfigurationElementHandle)
			return target.equals(((LegacyConfigurationElementHandle) object).toEquinox());
		return false;
	}

	public int hashCode() {
		return target.hashCode();
	}

	public LegacyConfigurationElementHandle(IObjectManager objectManager, int id) {
		target = new ConfigurationElementHandle(objectManager, id);
	}

	public LegacyConfigurationElementHandle(org.eclipse.equinox.registry.IConfigurationElement element) {
		target = element;
	}

	public String getAttribute(String propertyName) throws InvalidRegistryObjectException {
		try {
			return target.getAttribute(propertyName);
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String[] getAttributeNames() throws InvalidRegistryObjectException {
		try {
			return target.getAttributeNames();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getChildren());
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public Object createExecutableExtension(String propertyName) throws CoreException {
		return target.createExecutableExtension(propertyName);

	}

	public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
		try {
			return target.getAttributeAsIs(name);
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getChildren(name));
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
		try {
			return LegacyRegistryConverter.convert(target.getDeclaringExtension());
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getName() throws InvalidRegistryObjectException {
		try {
			return target.getName();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public Object getParent() throws InvalidRegistryObjectException {
		try {
			Object internalResult = target.getParent();
			if (internalResult instanceof org.eclipse.equinox.registry.IConfigurationElement)
				return LegacyRegistryConverter.convert((org.eclipse.equinox.registry.IConfigurationElement)internalResult);
			else if (internalResult instanceof org.eclipse.equinox.registry.IExtensionPoint)
				return LegacyRegistryConverter.convert((org.eclipse.equinox.registry.IExtensionPoint)internalResult);
			else if (internalResult instanceof org.eclipse.equinox.registry.IExtension)
				return LegacyRegistryConverter.convert((org.eclipse.equinox.registry.IExtension)internalResult);
			else 
				return internalResult;
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getValue() throws InvalidRegistryObjectException {
		try {
			return target.getValue();
		} catch (org.eclipse.equinox.registry.InvalidRegistryObjectException e) {
			throw LegacyRegistryConverter.convert(e);
		}
	}

	public String getValueAsIs() throws InvalidRegistryObjectException {
		try {
			return target.getValueAsIs();
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

	public boolean isValid() {
		return target.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IConfigurationElement toEquinox() {
		return target;
	}
}
