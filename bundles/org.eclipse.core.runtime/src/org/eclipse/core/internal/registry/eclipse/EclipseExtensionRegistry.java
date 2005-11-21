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

import java.io.File;
import java.util.Hashtable;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.registry.RegistryFactory;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Backward-compatible Eclipse registry implementation.
 */
public class EclipseExtensionRegistry implements IExtensionRegistry {

	private org.eclipse.equinox.registry.IExtensionRegistry theEquinoxHandle;
	private Object registryKey = new Object();
	ServiceRegistration registrationNew;
	ServiceRegistration registrationOld;

	public EclipseExtensionRegistry() {
		Location configuration = InternalPlatform.getDefault().getConfigurationLocation();
		File theStorageDir = new File(configuration.getURL().getPath() + '/' + Platform.PI_RUNTIME);
		EclipseRegistryStrategy registryStrategy = new EclipseRegistryStrategy(theStorageDir, configuration.isReadOnly(), registryKey);
		theEquinoxHandle = RegistryFactory.createExtensionRegistry(registryStrategy, registryKey);

		// Register this registry both under old and new names
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		registrationNew = context.registerService(org.eclipse.equinox.registry.IExtensionRegistry.class.getName(), theEquinoxHandle, new Hashtable());
		registrationOld = context.registerService(IExtensionRegistry.class.getName(), this, new Hashtable());
	}

	public void stop() {
		theEquinoxHandle.stop(registryKey);
		registrationNew.unregister();
		registrationOld.unregister();
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener, String namespace) {
		theEquinoxHandle.addRegistryChangeListener(listener, namespace);
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener) {
		theEquinoxHandle.addRegistryChangeListener(listener);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getConfigurationElementsFor(extensionPointId));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getConfigurationElementsFor(namespace, extensionPointName));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName, String extensionId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getConfigurationElementsFor(namespace, extensionPointName, extensionId));

	}

	public IExtension getExtension(String extensionId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtension(extensionId));
	}

	public IExtension getExtension(String extensionPointId, String extensionId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtension(extensionPointId, extensionId));
	}

	public IExtension getExtension(String namespace, String extensionPointName, String extensionId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtension(namespace, extensionPointName, extensionId));
	}

	public IExtensionPoint getExtensionPoint(String extensionPointId) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensionPoint(extensionPointId));
	}

	public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensionPoint(namespace, extensionPointName));
	}

	public IExtensionPoint[] getExtensionPoints() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensionPoints());
	}

	public IExtensionPoint[] getExtensionPoints(String namespace) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensionPoints(namespace));
	}

	public IExtension[] getExtensions(String namespace) {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensions(namespace));
	}

	public String[] getNamespaces() {
		return theEquinoxHandle.getNamespaces();
	}

	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
		theEquinoxHandle.removeRegistryChangeListener(listener);
	}
}
