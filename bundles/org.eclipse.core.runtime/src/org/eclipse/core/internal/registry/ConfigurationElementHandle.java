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

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;

/**
 * @since 3.1 
 */
public class ConfigurationElementHandle extends Handle implements IConfigurationElement {
	static final ConfigurationElementHandle[] EMPTY_ARRAY = new ConfigurationElementHandle[0];

	public ConfigurationElementHandle(IObjectManager objectManager, int id) {
		super(objectManager, id);
	}

	protected ConfigurationElement getConfigurationElement() {
		return (ConfigurationElement) objectManager.getObject(getId(), RegistryObjectManager.CONFIGURATION_ELEMENT);
	}

	public String getAttribute(String propertyName) {
		return getConfigurationElement().getAttribute(propertyName);
	}

	public String[] getAttributeNames() {
		return getConfigurationElement().getAttributeNames();
	}

	public IConfigurationElement[] getChildren() {
		ConfigurationElement actualCe = getConfigurationElement();
		if (actualCe.extraDataOffset == -1) {
			return (IConfigurationElement[]) objectManager.getHandles(actualCe.getRawChildren(), RegistryObjectManager.CONFIGURATION_ELEMENT);
		}
		return (IConfigurationElement[]) objectManager.getHandles(actualCe.getRawChildren(), RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
	}

	public Object createExecutableExtension(String propertyName) throws CoreException {
		try {
			return getConfigurationElement().createExecutableExtension(propertyName);
		} catch (InvalidRegistryObjectException e) {
			Status status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, ConfigurationElement.PLUGIN_ERROR, e.getMessage(), e);
			InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundleContext().getBundle()).log(status);
			throw new CoreException(status);
		}
	}

	public String getAttributeAsIs(String name) {
		return getConfigurationElement().getAttributeAsIs(name);
	}

	public IConfigurationElement[] getChildren(String name) {
		ConfigurationElement actualCE = getConfigurationElement();
		ConfigurationElement[] children = (ConfigurationElement[]) objectManager.getObjects(actualCE.getRawChildren(), actualCE.extraDataOffset == -1 ? RegistryObjectManager.CONFIGURATION_ELEMENT : RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
		if (children.length == 0)
			return ConfigurationElementHandle.EMPTY_ARRAY;

		IConfigurationElement[] result = new IConfigurationElement[1];
		int idx = 0;
		for (int i = 0; i < children.length; i++) {
			if (children[i].getName().equals(name)) {
				if (idx != 0) {
					IConfigurationElement[] copy = new IConfigurationElement[result.length + 1];
					System.arraycopy(result, 0, copy, 0, result.length);
					result = copy;
				}
				result[idx++] = (IConfigurationElement) objectManager.getHandle(children[i].getObjectId(), actualCE.extraDataOffset == -1 ? RegistryObjectManager.CONFIGURATION_ELEMENT : RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
			}
		}
		if (idx == 0)
			return ConfigurationElementHandle.EMPTY_ARRAY;
		return result;
	}

	public IExtension getDeclaringExtension() {
		Object result = this;
		while (!((result = ((ConfigurationElementHandle) result).getParent()) instanceof ExtensionHandle)) { /*do nothing*/
		}
		return (IExtension) result;
	}

	public String getName() {
		return getConfigurationElement().getName();
	}

	public Object getParent() {
		ConfigurationElement actualCe = getConfigurationElement();
		return objectManager.getHandle(actualCe.parentId, actualCe.parentType);
	}

	public String getValue() {
		return getConfigurationElement().getValue();
	}

	public String getValueAsIs() {
		return getConfigurationElement().getValueAsIs();
	}

	RegistryObject getObject() {
		return getConfigurationElement();
	}

	public String getNamespace() {
		String result = getConfigurationElement().getNamespace();
		if (result == null)
			return getDeclaringExtension().getNamespace();
		return result;
	}
	
	public boolean isValid() {
		try {
			getConfigurationElement();
		} catch (InvalidRegistryObjectException e) {
			return false;
		}
		return true;
	}
}
