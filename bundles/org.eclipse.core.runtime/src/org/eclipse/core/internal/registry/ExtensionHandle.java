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

import org.eclipse.core.runtime.*;

/**
 * @since 3.1 
 */
public class ExtensionHandle extends Handle implements IExtension {
	static final ExtensionHandle[] EMPTY_ARRAY = new ExtensionHandle[0];

	public ExtensionHandle(IObjectManager objectManager, int id) {
		super(objectManager, id);
	}

	private Extension getExtension() {
		return (Extension) objectManager.getObject(getId(), RegistryObjectManager.EXTENSION);
	}
	
	/**
	 * @deprecated
	 */
	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return getExtension().getDeclaringPluginDescriptor();
	}

	public String getNamespace() {
		return getExtension().getNamespace();
	}

	public String getExtensionPointUniqueIdentifier() {
		return getExtension().getExtensionPointIdentifier();
	}

	public String getLabel() {
		return getExtension().getLabel();
	}

	public String getSimpleIdentifier() {
		return getExtension().getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return getExtension().getUniqueIdentifier();
	}
	
	public IConfigurationElement[] getConfigurationElements() {
		return (IConfigurationElement[]) objectManager.getHandles(getExtension().getRawChildren(), RegistryObjectManager.CONFIGURATION_ELEMENT);
	}
	 
	RegistryObject getObject() {
		return getExtension();
	}
}
