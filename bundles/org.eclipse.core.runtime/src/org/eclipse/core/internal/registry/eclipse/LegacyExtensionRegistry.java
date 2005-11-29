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

import java.util.Hashtable;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.registry.RegistryFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Backward-compatible Eclipse registry implementation.
 */
public class LegacyExtensionRegistry implements IExtensionRegistry {
	ServiceRegistration registrationOld;
	private org.eclipse.equinox.registry.IExtensionRegistry target;

	public LegacyExtensionRegistry() {
		target = RegistryFactory.getRegistry();
		if (target instanceof ExtensionRegistry) {
			((ExtensionRegistry) target).setCompatibilityStrategy(new LegacyRegistryCompatibility());
		}

		// For compatibility, continue to register this registry under 
		// the org.eclipse.core.runtime.IExtentionRegistry name
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		registrationOld = context.registerService(IExtensionRegistry.class.getName(), this, new Hashtable());
	}

	public void stop() {
		registrationOld.unregister();
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener, String namespace) {
		target.addRegistryChangeListener(listener, namespace);
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		target.addRegistryChangeListener(listener);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		return LegacyRegistryConverter.convert(target.getConfigurationElementsFor(extensionPointId));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName) {
		return LegacyRegistryConverter.convert(target.getConfigurationElementsFor(namespace, extensionPointName));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName, String extensionId) {
		return LegacyRegistryConverter.convert(target.getConfigurationElementsFor(namespace, extensionPointName, extensionId));

	}

	public IExtension getExtension(String extensionId) {
		return LegacyRegistryConverter.convert(target.getExtension(extensionId));
	}

	public IExtension getExtension(String extensionPointId, String extensionId) {
		return LegacyRegistryConverter.convert(target.getExtension(extensionPointId, extensionId));
	}

	public IExtension getExtension(String namespace, String extensionPointName, String extensionId) {
		return LegacyRegistryConverter.convert(target.getExtension(namespace, extensionPointName, extensionId));
	}

	public IExtensionPoint getExtensionPoint(String extensionPointId) {
		return LegacyRegistryConverter.convert(target.getExtensionPoint(extensionPointId));
	}

	public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName) {
		return LegacyRegistryConverter.convert(target.getExtensionPoint(namespace, extensionPointName));
	}

	public IExtensionPoint[] getExtensionPoints() {
		return LegacyRegistryConverter.convert(target.getExtensionPoints());
	}

	public IExtensionPoint[] getExtensionPoints(String namespace) {
		return LegacyRegistryConverter.convert(target.getExtensionPoints(namespace));
	}

	public IExtension[] getExtensions(String namespace) {
		return LegacyRegistryConverter.convert(target.getExtensions(namespace));
	}

	public String[] getNamespaces() {
		return target.getNamespaces();
	}

	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		target.removeRegistryChangeListener(listener);
	}
}
