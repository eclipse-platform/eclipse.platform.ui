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

// XXX Eclipse* classes should be renamed to be Legacy or some such
// XXX EclipseRegistryAdaptor should be renamed to *Converter and the adapt() => convert() or some such

public class EclipseExtensionRegistry implements IExtensionRegistry {
	ServiceRegistration registrationOld;
	// XXX this field name should be updated.  Something like "target" or "wrappee" :-)
	private org.eclipse.equinox.registry.IExtensionRegistry theEquinoxHandle;

	public EclipseExtensionRegistry() {
		theEquinoxHandle = RegistryFactory.getRegistry();
		if (theEquinoxHandle instanceof ExtensionRegistry) {
			((ExtensionRegistry) theEquinoxHandle).setCompatibilityStrategy(new EclipseRegistryCompatibility());
		}

		// XXX Did we used to register the registry as a service?  If so, bummer.  If not, we should consider 
		//    Dropping this.  If we do have to register it, there should be a distinguishing property set so
		//    someone can ensure they are getting the legacy registry.
		// For compatibility, register this registry under old name as well
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		registrationOld = context.registerService(IExtensionRegistry.class.getName(), this, new Hashtable());
	}

	public void stop() {
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
