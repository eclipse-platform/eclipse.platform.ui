package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.IModel;
import org.eclipse.core.internal.runtime.Policy;
import java.util.*;

public class Extension extends ExtensionModel implements IExtension {
  public Extension()
  {
	super();
  }  
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
	return s == null ? "" : ((PluginDescriptor) getDeclaringPluginDescriptor()).getResourceString(s);
}
public String getSimpleIdentifier() {
	return getId();
}
public String getUniqueIdentifier() {
	String simple = getSimpleIdentifier();
	if (simple == null)
		return null;
	return getParentPluginDescriptor().getId() + "." + simple;
}
public String toString() {
	return getParent().getPluginId() + "." + getSimpleIdentifier();
}
}
