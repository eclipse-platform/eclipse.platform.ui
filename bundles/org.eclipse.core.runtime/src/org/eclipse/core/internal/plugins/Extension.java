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

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.ConfigurationElementModel;
import org.eclipse.core.runtime.model.ExtensionModel;

public class Extension extends ExtensionModel implements IExtension {
	// this extension's elements data offset in the registry cache
	private int subElementsCacheOffset;
	// is this extension already fully loaded?
	private boolean fullyLoaded = true;

public IConfigurationElement[] getConfigurationElements() {
	ConfigurationElementModel[] list = getSubElements();
	if (list == null)
		return new IConfigurationElement[0];
	IConfigurationElement[] newValues = new IConfigurationElement[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
public IPluginDescriptor getDeclaringPluginDescriptor() {
	return (IPluginDescriptor) getParentPluginDescriptor();
}
public String getExtensionPointUniqueIdentifier() {
	return getExtensionPoint();
}
public String getLabel() {
	String s = getName();
	return s == null ? "" : ((PluginDescriptor) getDeclaringPluginDescriptor()).getResourceString(s); //$NON-NLS-1$
}
public String getSimpleIdentifier() {
	return getId();
}
public String getUniqueIdentifier() {
	String simple = getSimpleIdentifier();
	if (simple == null)
		return null;
	return getParentPluginDescriptor().getId() + "." + simple; //$NON-NLS-1$
}
public String toString() {
	return getParent().getPluginId() + "." + getSimpleIdentifier(); //$NON-NLS-1$
}
int getSubElementsCacheOffset() {
	return subElementsCacheOffset;
}
void setSubElementsCacheOffset(int subElementsCacheOffset) {
	this.subElementsCacheOffset = subElementsCacheOffset;
}
public boolean isFullyLoaded() {
	return fullyLoaded;
}
public void setFullyLoaded(boolean fullyLoaded) {
	this.fullyLoaded = fullyLoaded;
}
public ConfigurationElementModel[] getSubElements() {
	// maybe it was lazily loaded
	if (!fullyLoaded)
		((PluginRegistry)this.getParent().getRegistry()).loadConfigurationElements(this);
	return super.getSubElements();
}
/**
 * Overridden to relax read-only contraints and allow lazy loading of read-only
 * extensions.
 * @see org.eclipse.core.runtime.model.PluginModelObject#assertIsWriteable
 */
protected void assertIsWriteable() {
	if (fullyLoaded)
		super.assertIsWriteable();
}
}
