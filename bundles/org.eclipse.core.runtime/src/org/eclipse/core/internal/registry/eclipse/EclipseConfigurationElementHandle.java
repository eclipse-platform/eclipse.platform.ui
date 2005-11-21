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
public class EclipseConfigurationElementHandle implements org.eclipse.core.runtime.IConfigurationElement {

	protected org.eclipse.equinox.registry.IConfigurationElement theEquinoxHandle;

	public boolean equals(Object object) {
		if (object instanceof EclipseConfigurationElementHandle)
			return theEquinoxHandle.equals(((EclipseConfigurationElementHandle) object).getInternalHandle());
		return false;
	}

	public int hashCode() {
		return theEquinoxHandle.hashCode();
	}

	public EclipseConfigurationElementHandle(IObjectManager objectManager, int id) {
		theEquinoxHandle = new ConfigurationElementHandle(objectManager, id);
	}

	public EclipseConfigurationElementHandle(org.eclipse.equinox.registry.IConfigurationElement element) {
		theEquinoxHandle = element;
	}

	public String getAttribute(String propertyName) {
		return theEquinoxHandle.getAttribute(propertyName);
	}

	public String[] getAttributeNames() {
		return theEquinoxHandle.getAttributeNames();
	}

	public IConfigurationElement[] getChildren() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getChildren());
	}

	public Object createExecutableExtension(String propertyName) throws CoreException {
		return theEquinoxHandle.createExecutableExtension(propertyName);

	}

	public String getAttributeAsIs(String name) {
		return theEquinoxHandle.getAttributeAsIs(name);
	}

	public IConfigurationElement[] getChildren(String name) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getChildren(name));
	}

	public IExtension getDeclaringExtension() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getDeclaringExtension());
	}

	public String getName() {
		return theEquinoxHandle.getName();
	}

	public Object getParent() {
		Object internalResult = theEquinoxHandle.getParent();
		if (internalResult instanceof org.eclipse.equinox.registry.IConfigurationElement)
			return EclipseRegistryAdaptor.adapt((org.eclipse.equinox.registry.IConfigurationElement)internalResult);
		else if (internalResult instanceof org.eclipse.equinox.registry.IExtensionPoint)
			return EclipseRegistryAdaptor.adapt((org.eclipse.equinox.registry.IExtensionPoint)internalResult);
		else if (internalResult instanceof org.eclipse.equinox.registry.IExtension)
			return EclipseRegistryAdaptor.adapt((org.eclipse.equinox.registry.IExtension)internalResult);
		else 
			return internalResult;
	}

	public String getValue() {
		return theEquinoxHandle.getValue();
	}

	public String getValueAsIs() {
		return theEquinoxHandle.getValueAsIs();
	}

	public String getNamespace() {
		return theEquinoxHandle.getNamespace();
	}

	public boolean isValid() {
		return theEquinoxHandle.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IConfigurationElement getInternalHandle() {
		return theEquinoxHandle;
	}
}
