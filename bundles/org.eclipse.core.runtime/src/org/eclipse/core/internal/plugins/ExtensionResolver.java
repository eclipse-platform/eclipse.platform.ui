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

import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;

/**
 * The class <code>ExtensionResolver</code> exports a static method 
 * <code>resolve</code> which links ExtensionPointModels and their 
 * corresponding ExtensionModels.
 */

public final class ExtensionResolver {

	private PluginRegistryModel registry = null;
	private MultiStatus status = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, "", null); //$NON-NLS-1$

	/**
	 * Creates links between ExtensionPointModels and their 
	 * corresponding ExtensionModels.
	 *
	 * @param registry the registry containing plugins whose 
	 * ExtensionPointModels and ExtensionModels to be linked.
	 */

static public MultiStatus resolve(PluginRegistryModel registry) {
	ExtensionResolver resolver = new ExtensionResolver(registry);
	return resolver.resolve();
}

private ExtensionResolver() {
}

private ExtensionResolver(PluginRegistryModel registry) {
    super();
	this.registry = registry;
}

private  MultiStatus resolve() {
	PluginDescriptorModel [] plugins = registry.getPlugins();
	for (int i = 0; i < plugins.length; i++) {
		PluginDescriptorModel pd = plugins[i];
		if (!pd.getEnabled())
			continue;
		ExtensionModel[] list = pd.getDeclaredExtensions();
		if (list == null)
			continue;
		for (int j = 0; j < list.length; j++)
			resolveExtension((ExtensionModel) list[j]);
	}
	return status;
}

private void resolveExtension(ExtensionModel ext) {
	
	String target = ext.getExtensionPoint();
	int ix = target.lastIndexOf("."); //$NON-NLS-1$
	String pluginId = target.substring(0, ix);
	String extPtId = target.substring(ix + 1);

	PluginDescriptorModel plugin = registry.getPlugin(pluginId);
	if (plugin == null) {
		String message = Policy.bind("parse.extPointUnknown", target, ext.getParentPluginDescriptor().getId()); //$NON-NLS-1$
		error(message);
		return;
	}
	if (!plugin.getEnabled()) {
		String message = Policy.bind("parse.extPointDisabled", target, ext.getParentPluginDescriptor().getId()); //$NON-NLS-1$
		error(message);
		return;
	}

	ExtensionPointModel extPt = (ExtensionPointModel) getExtensionPoint(plugin, extPtId);
	if (extPt == null) {
		String message = Policy.bind("parse.extPointUnknown", target, ext.getParentPluginDescriptor().getId()); //$NON-NLS-1$
		error(message);
		return;
	}

	ExtensionModel[] oldValues = extPt.getDeclaredExtensions();
	ExtensionModel[] newValues = null;
	if (oldValues == null)
		newValues = new ExtensionModel[1];
	else {
		newValues = new ExtensionModel[oldValues.length + 1];
		System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
	}
	newValues[newValues.length - 1] = ext;
	extPt.setDeclaredExtensions(newValues);
}

private IExtensionPoint getExtensionPoint(PluginDescriptorModel plugin, String extensionPointId) {
	if (plugin == null || extensionPointId == null) 
		return null;
	ExtensionPointModel[] list = plugin.getDeclaredExtensionPoints();
	if (list == null || list.length == 0)
		return null;
	for (int i = 0; i < list.length; i++) {
		if (extensionPointId.equals(list[i].getId()))
			return (IExtensionPoint) list[i];
	}
	return null;
}

private void error(String message) {
	Status error = new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, message, null);
	status.add(error);
}
}
