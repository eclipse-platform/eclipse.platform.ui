/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.registry.*;

/*
 * An implementation of IExtensionRegistry that wraps an IPluginRegistry. 
 */
public class PluginRegistryWrapper implements IExtensionRegistry {

	private IPluginRegistry pluginRegistry;

	public PluginRegistryWrapper(IPluginRegistry pluginRegistry) {
		this.pluginRegistry = pluginRegistry;
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener, String elementId) {
	}

	public void addRegistryChangeListener(IRegistryChangeListener listener) {
	}

	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
		return Utils.convertConfigurationElements(pluginRegistry.getConfigurationElementsFor(extensionPointId));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String elementId, String extensionPointName) {
		return Utils.convertConfigurationElements(pluginRegistry.getConfigurationElementsFor(elementId, extensionPointName));
	}

	public IConfigurationElement[] getConfigurationElementsFor(String elementId, String extensionPointName, String extensionId) {
		return Utils.convertConfigurationElements(pluginRegistry.getConfigurationElementsFor(elementId, extensionPointName, extensionId));
	}

	public IExtension getExtension(String extensionPointId, String extensionId) {
		org.eclipse.core.runtime.IExtension oldExtension = pluginRegistry.getExtension(extensionPointId, extensionId);
		if (oldExtension == null)
			return null;
		return new ExtensionWrapper(oldExtension);
	}

	public IExtension getExtension(String elementId, String extensionPointName, String extensionId) {
		org.eclipse.core.runtime.IExtension oldExtension = pluginRegistry.getExtension(elementId, extensionPointName, extensionId);
		if (oldExtension == null)
			return null;
		return new ExtensionWrapper(oldExtension);
	}

	public IExtensionPoint getExtensionPoint(String extensionPointId) {
		org.eclipse.core.runtime.IExtensionPoint oldExtensionPoint = pluginRegistry.getExtensionPoint(extensionPointId);
		if (oldExtensionPoint == null)
			return null;
		return new ExtensionPointWrapper(oldExtensionPoint);
	}

	public IExtensionPoint getExtensionPoint(String elementId, String extensionPointName) {
		org.eclipse.core.runtime.IExtensionPoint oldExtensionPoint = pluginRegistry.getExtensionPoint(elementId, extensionPointName);
		if (oldExtensionPoint == null)
			return null;
		return new ExtensionPointWrapper(oldExtensionPoint);
	}

	public IExtensionPoint[] getExtensionPoints() {
		return Utils.convertExtensionPoints(pluginRegistry.getExtensionPoints());
	}

	public IExtensionPoint[] getExtensionPoints(String elementId) {
		IPluginDescriptor pluginDescriptor = pluginRegistry.getPluginDescriptor(elementId);
		if (pluginDescriptor == null)
			return new IExtensionPoint[0];
		return Utils.convertExtensionPoints(pluginDescriptor.getExtensionPoints());
	}

	public IExtension[] getExtensions(String elementId) {
		IPluginDescriptor pluginDescriptor = pluginRegistry.getPluginDescriptor(elementId);
		if (pluginDescriptor == null)
			return new IExtension[0];
		return Utils.convertExtensions(pluginDescriptor.getExtensions());
	}

	public String[] getElementIdentifiers() {
		IPluginDescriptor[] pluginDescriptors = pluginRegistry.getPluginDescriptors();
		String[] elementIds = new String[pluginDescriptors.length];
		for (int i = 0; i < pluginDescriptors.length; i++)
			elementIds[i] = pluginDescriptors[i].getUniqueIdentifier();
		return elementIds;
	}

	public void removeRegistryChangeListener(IRegistryChangeListener listener) {
	}

}
