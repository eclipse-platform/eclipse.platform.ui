/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IAdapterFactory;

public class RegistryAdapters {
	private static IAdapterFactory extensionPoint;
	private static IAdapterFactory extension;
	private static IAdapterFactory configElt;

	public static void registerFactories() {
		extensionPoint = new ExtensionPointModelFactory();
		extension = new ExtensionModelFactory();
		configElt = new ConfigurationElementFactory();

		InternalPlatform.getDefault().getAdapterManager().registerAdapters(extensionPoint, org.eclipse.core.internal.registry.ExtensionPoint.class);
		InternalPlatform.getDefault().getAdapterManager().registerAdapters(extension, org.eclipse.core.internal.registry.Extension.class);
		InternalPlatform.getDefault().getAdapterManager().registerAdapters(configElt, org.eclipse.core.internal.registry.ConfigurationElement.class);
	}

	public static void unregisterFactories() {
		InternalPlatform.getDefault().getAdapterManager().unregisterAdapters(extensionPoint);
		InternalPlatform.getDefault().getAdapterManager().unregisterAdapters(extension);
		InternalPlatform.getDefault().getAdapterManager().unregisterAdapters(configElt);
	}

	static class ExtensionPointModelFactory implements IAdapterFactory {
		public Object getAdapter(Object adaptableObject, Class adapterType) {
			org.eclipse.core.internal.registry.ExtensionPoint toAdapt = null;
			try {
				toAdapt = (org.eclipse.core.internal.registry.ExtensionPoint) adaptableObject;
				return new org.eclipse.core.internal.plugins.ExtensionPoint(toAdapt);
			} catch (ClassCastException e) {
				return null;
			}
		}
		public Class[] getAdapterList() {
			return new Class[] { org.eclipse.core.internal.plugins.ExtensionPoint.class, org.eclipse.core.runtime.IExtensionPoint.class };
		}
	}

	static class ExtensionModelFactory implements IAdapterFactory {
		public Object getAdapter(Object adaptableObject, Class adapterType) {
			org.eclipse.core.internal.registry.Extension toAdapt = null;
			try {
				toAdapt = (org.eclipse.core.internal.registry.Extension) adaptableObject;
				return new org.eclipse.core.internal.plugins.Extension(toAdapt);
			} catch (ClassCastException e) {
				return null;
			}
		}
		public Class[] getAdapterList() {
			return new Class[] { org.eclipse.core.internal.plugins.Extension.class, org.eclipse.core.runtime.IExtension.class };
		}
	}

	static class ConfigurationElementFactory implements IAdapterFactory {
		public Object getAdapter(Object adaptableObject, Class adapterType) {
			org.eclipse.core.internal.registry.ConfigurationElement toAdapt = null;
			try {
				toAdapt = (org.eclipse.core.internal.registry.ConfigurationElement) adaptableObject;
				return new org.eclipse.core.internal.plugins.ConfigurationElement(toAdapt);
			} catch (ClassCastException e) {
				return null;
			}
		}
		public Class[] getAdapterList() {
			return new Class[] { org.eclipse.core.internal.plugins.ConfigurationElement.class, org.eclipse.core.runtime.IConfigurationElement.class };
		}
	}

}
