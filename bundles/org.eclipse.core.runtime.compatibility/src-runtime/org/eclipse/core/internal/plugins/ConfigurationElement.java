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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;

public class ConfigurationElement implements IConfigurationElement {

	private org.eclipse.core.runtime.registry.IConfigurationElement configElt;

	public ConfigurationElement(org.eclipse.core.runtime.registry.IConfigurationElement toAdapt) {
		configElt = toAdapt;
	}

	public Object createExecutableExtension(String propertyName) throws CoreException {
		((org.eclipse.core.internal.registry.ConfigurationElement) configElt).setOldStyleConfigurationElement(this);
		return configElt.createExecutableExtension(propertyName);
	}

	public String getAttribute(String name) {
		return configElt.getAttribute(name);
	}

	public String getAttributeAsIs(String name) {
		return configElt.getAttributeAsIs(name);
	}

	public String[] getAttributeNames() {
		return configElt.getAttributeNames();
	}

	public IConfigurationElement[] getChildren() {
		return Utils.convertConfigurationElements((org.eclipse.core.runtime.registry.IConfigurationElement[]) configElt.getChildren());
	}

	public IConfigurationElement[] getChildren(String name) {
		return Utils.convertConfigurationElements((org.eclipse.core.runtime.registry.IConfigurationElement[]) configElt.getChildren(name));
	}

	public IExtension getDeclaringExtension() {
		return new Extension(configElt.getDeclaringExtension());
	}

	public String getName() {
		return configElt.getName();
	}

	public String getValue() {
		return configElt.getValue();
	}

	public String getValueAsIs() {
		return configElt.getValueAsIs();
	}

	public org.eclipse.core.runtime.registry.IConfigurationElement getUnderlyingElement() {
		return configElt;
	}

	public void runOldExecutableExtension(Object result, String propertyName, Object initData) throws CoreException {
		if (result instanceof IExecutableExtension) {
			// make the call even if the initialization string is null
			 ((IExecutableExtension) result).setInitializationData(this, propertyName, initData);
		}
	}

	public static Boolean implementsIExecutableExtension(Object o) {
		return new Boolean(o instanceof IExecutableExtension);
	}

	public Object getParent() {
		return configElt.getParent();
	}
}
