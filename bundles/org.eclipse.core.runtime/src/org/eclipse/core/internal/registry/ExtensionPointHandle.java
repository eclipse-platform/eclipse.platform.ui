/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.core.runtime.*;

/**
 * @since 3.1 
 */
public class ExtensionPointHandle extends Handle implements IExtensionPoint {
	static final ExtensionPointHandle[] EMPTY_ARRAY = new ExtensionPointHandle[0];

	public ExtensionPointHandle(IObjectManager objectManager, int id) {
		super(objectManager, id);
	}

	public IExtension[] getExtensions() {
		return (IExtension[]) objectManager.getHandles(getExtensionPoint().getRawChildren(), RegistryObjectManager.EXTENSION);
	}

	/**
	 * @deprecated
	 */
	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return getExtensionPoint().getDeclaringPluginDescriptor();
	}

	public String getNamespace() {
		return getExtensionPoint().getNamespace();
	}

	public IExtension getExtension(String extensionId) {
		if (extensionId == null)
			return null;
		int[] children = getExtensionPoint().getRawChildren();
		for (int i = 0; i < children.length; i++) {
			//	Here we directly get the object because it avoids the creation of garbage and because we'll need the object anyway to compare the value
			if (extensionId.equals(((Extension) objectManager.getObject(children[i], RegistryObjectManager.EXTENSION)).getUniqueIdentifier()))
				return (ExtensionHandle) objectManager.getHandle(children[i], RegistryObjectManager.EXTENSION);
		}
		return null;
	}

	public IConfigurationElement[] getConfigurationElements() {
		//get the actual extension objects since we'll need to get the configuration elements information.
		Extension[] tmpExtensions = (Extension[]) objectManager.getObjects(getExtensionPoint().getRawChildren(), RegistryObjectManager.EXTENSION);
		if (tmpExtensions.length == 0)
			return ConfigurationElementHandle.EMPTY_ARRAY;

		ArrayList result = new ArrayList();
		for (int i = 0; i < tmpExtensions.length; i++) {
			result.addAll(Arrays.asList(objectManager.getHandles(tmpExtensions[i].getRawChildren(), RegistryObjectManager.CONFIGURATION_ELEMENT)));
		}
		return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
	}

	public String getLabel() {
		return getExtensionPoint().getLabel();
	}

	public String getSchemaReference() {
		return getExtensionPoint().getSchemaReference();
	}

	public String getSimpleIdentifier() {
		return getExtensionPoint().getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return getExtensionPoint().getUniqueIdentifier();
	}

	RegistryObject getObject() {
		return getExtensionPoint();
	}

	private ExtensionPoint getExtensionPoint() {
		return (ExtensionPoint) objectManager.getObject(getId(), RegistryObjectManager.EXTENSION_POINT);
	}

}
