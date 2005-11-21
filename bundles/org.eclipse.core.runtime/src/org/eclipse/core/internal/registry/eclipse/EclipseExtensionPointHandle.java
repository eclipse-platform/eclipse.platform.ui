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
public class EclipseExtensionPointHandle implements IExtensionPoint {

	protected org.eclipse.equinox.registry.IExtensionPoint theEquinoxHandle;

	public boolean equals(Object object) {
		if (object instanceof EclipseExtensionPointHandle)
			return theEquinoxHandle.equals(((EclipseExtensionPointHandle) object).getInternalHandle());
		return false;
	}

	public int hashCode() {
		return theEquinoxHandle.hashCode();
	}

	public EclipseExtensionPointHandle(IObjectManager objectManager, int id) {
		theEquinoxHandle = new ExtensionPointHandle(objectManager, id);
	}

	public EclipseExtensionPointHandle(org.eclipse.equinox.registry.IExtensionPoint extensionPoint) {
		theEquinoxHandle = extensionPoint;
	}

	public IExtension[] getExtensions() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensions());
	}

	public String getNamespace() {
		return theEquinoxHandle.getNamespace();
	}

	public IExtension getExtension(String extensionId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtension(extensionId));
	}

	public IConfigurationElement[] getConfigurationElements() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getConfigurationElements());
	}

	public String getLabel() {
		return theEquinoxHandle.getLabel();
	}

	public String getSchemaReference() {
		return theEquinoxHandle.getSchemaReference();
	}

	public String getSimpleIdentifier() {
		return theEquinoxHandle.getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return theEquinoxHandle.getUniqueIdentifier();
	}

	public boolean isValid() {
		return theEquinoxHandle.isValid();
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IExtensionPoint getInternalHandle() {
		return theEquinoxHandle;
	}

	/**
	 * @deprecated
	 */
	public org.eclipse.core.runtime.IPluginDescriptor getDeclaringPluginDescriptor() {
		return org.eclipse.core.internal.runtime.CompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}
