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
import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;

public class ConfigurationElementWrapper implements IConfigurationElement {
	private org.eclipse.core.runtime.IConfigurationElement toAdapt;
	public ConfigurationElementWrapper(org.eclipse.core.runtime.IConfigurationElement toAdapt) {
		this.toAdapt = toAdapt;
	}
	public Object createExecutableExtension(String propertyName) throws CoreException {
		return toAdapt.createExecutableExtension(propertyName);
	}
	public String getAttribute(String name) {
		return toAdapt.getAttribute(name);
	}
	public String getAttributeAsIs(String name) {
		return toAdapt.getAttributeAsIs(name);
	}
	public String[] getAttributeNames() {
		return toAdapt.getAttributeNames();
	}
	public IConfigurationElement[] getChildren() {
		return Utils.convertConfigurationElements(toAdapt.getChildren());
	}
	public IConfigurationElement[] getChildren(String name) {
		return Utils.convertConfigurationElements(toAdapt.getChildren(name));
	}
	public IExtension getDeclaringExtension() {
		return new ExtensionWrapper(toAdapt.getDeclaringExtension());
	}
	public String getName() {
		return toAdapt.getName();
	}
	public Object getParent() {
		 Object parent = ((ConfigurationElement)toAdapt).getParent();
		 if (parent == null)
		 	return null;
		 if (parent instanceof org.eclipse.core.runtime.IConfigurationElement)
		 	return new ConfigurationElementWrapper((org.eclipse.core.runtime.IConfigurationElement) parent);
		else
		return new ExtensionWrapper((org.eclipse.core.runtime.IExtension) parent);
	}
	public String getValue() {
		return toAdapt.getValue();
	}
	public String getValueAsIs() {
		return toAdapt.getValueAsIs();
	}
	public org.eclipse.core.runtime.IConfigurationElement getAdapted() {
		return toAdapt;
	}	
}
