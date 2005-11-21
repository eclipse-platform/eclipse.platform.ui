/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.*;
import org.eclipse.core.runtime.*;

/**
 * Portions of the AdapterManager that deal with the Eclipse extension registry
 * were moved into this class.
 * 
 * @since org.eclipse.core.runtime 3.2
 */
public final class AdapterManagerListener implements IRegistryChangeListener {

	private AdapterManager theAdapterManager;

	/**
	 * Constructs a new adapter manager.
	 */
	public AdapterManagerListener() {
		theAdapterManager = AdapterManager.getDefault();
		registerFactoryProxies();
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	/**
	 * Loads adapters registered with the adapters extension point from
	 * the plug-in registry.  Note that the actual factory implementations
	 * are loaded lazily as they are needed.
	 */
	private void registerFactoryProxies() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_ADAPTERS);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; i++)
			registerExtension(extensions[i]);
	}

	private void registerExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int j = 0; j < elements.length; j++) {
			AdapterFactoryProxy proxy = AdapterFactoryProxy.createProxy(elements[j]);
			if (proxy != null)
				theAdapterManager.registerFactory(proxy, proxy.getAdaptableType());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public synchronized void registryChanged(IRegistryChangeEvent event) {
		//find the set of changed adapter extensions
		HashSet toRemove = null;
		IExtensionDelta[] deltas = event.getExtensionDeltas();
		String adapterId = Platform.PI_RUNTIME + '.' + Platform.PT_ADAPTERS;
		boolean found = false;
		for (int i = 0; i < deltas.length; i++) {
			//we only care about extensions to the adapters extension point
			if (!adapterId.equals(deltas[i].getExtensionPoint().getUniqueIdentifier()))
				continue;
			found = true;
			if (deltas[i].getKind() == IExtensionDelta.ADDED)
				registerExtension(deltas[i].getExtension());
			else {
				//create the hash set lazily
				if (toRemove == null)
					toRemove = new HashSet();
				toRemove.add(deltas[i].getExtension().getUniqueIdentifier());
			}
		}
		//need to discard cached state for the changed extensions
		if (found)
			theAdapterManager.flushLookup();
		if (toRemove == null)
			return;
		//remove any factories belonging to extensions that are going away
		for (Iterator it = theAdapterManager.getFactories().values().iterator(); it.hasNext();) {
			for (Iterator it2 = ((List) it.next()).iterator(); it2.hasNext();) {
				IAdapterFactory factory = (IAdapterFactory) it2.next();
				if (factory instanceof AdapterFactoryProxy) {
					String ext = ((AdapterFactoryProxy) factory).getOwnerId();
					if (toRemove.contains(ext))
						it2.remove();
				}
			}
		}
	}

	/*
	 * Shuts down the listener by removing the registry change listener. Should only be
	 * invoked during platform shutdown.
	 */
	public synchronized void stop() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

}
