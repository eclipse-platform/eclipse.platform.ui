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
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;

public class ExtensionPoint implements IExtensionPoint {
	private org.eclipse.core.runtime.registry.IExtensionPoint extensionPoint;

	public ExtensionPoint(org.eclipse.core.runtime.registry.IExtensionPoint xp) {
		extensionPoint = xp;
	}

	public IConfigurationElement[] getConfigurationElements() {
		return Utils.convertConfigurationElements(InternalPlatform.getDefault().getRegistry().getConfigurationElementsFor(extensionPoint.getParentIdentifier(), extensionPoint.getSimpleIdentifier()));
	}

	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return org.eclipse.core.internal.plugins.InternalPlatform.getPluginRegistry().getPluginDescriptor(extensionPoint.getParentIdentifier());
	}

	public IExtension getExtension(String extensionId) {
		return (IExtension) ((org.eclipse.core.internal.registry.Extension) extensionPoint.getExtension(extensionId)).getAdapter(IExtension.class);
	}

	public IExtension[] getExtensions() {
		return Utils.convertExtensions(extensionPoint.getExtensions());
	}

	public String getLabel() {
		return extensionPoint.getLabel();
	}

	public String getSchemaReference() {
		return ((org.eclipse.core.internal.registry.ExtensionPoint) extensionPoint).getSchemaReference();
	}

	public String getSimpleIdentifier() {
		return extensionPoint.getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return extensionPoint.getUniqueIdentifier();
	}

}
