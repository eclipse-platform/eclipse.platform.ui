/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import java.util.*;

public class ConfigurationElement extends ConfigurationElementModel implements IConfigurationElement {
  public ConfigurationElement()
  {
	super();
  }  
public Object createExecutableExtension(String attributeName) throws CoreException {
	String prop = null;
	String executable;
	String pluginName = null;
	String className = null;
	Object initData = null;
	int i;

	if (attributeName != null)
		prop = getAttribute(attributeName);
	else {
		// property not specified, try as element value
		prop = getValue();
		if (prop != null) {
			prop = prop.trim();
			if (prop.equals("")) //$NON-NLS-1$
				prop = null;
		}
	}

	if (prop == null) {
		// property not defined, try as a child element
		IConfigurationElement[] exec;
		IConfigurationElement[] parms;
		IConfigurationElement element;
		Hashtable initParms;
		String pname;

		exec = getChildren(attributeName);
		if (exec.length != 0) {
			element = exec[0]; // assumes single definition
			pluginName = (String) element.getAttribute("plugin"); //$NON-NLS-1$
			className = (String) element.getAttribute("class"); //$NON-NLS-1$
			parms = element.getChildren("parameter"); //$NON-NLS-1$
			if (parms != null) {
				initParms = new Hashtable(parms.length + 1);
				for (i = 0; i < parms.length; i++) {
					pname = (String) parms[i].getAttribute("name"); //$NON-NLS-1$
					if (pname != null)
						initParms.put(pname, parms[i].getAttribute("value")); //$NON-NLS-1$
				}
				if (!initParms.isEmpty())
					initData = initParms;
			}
		}

		// specified name is not a simple attribute nor child element
		else {
			String message = Policy.bind("plugin.extDefNotFound", attributeName); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, null);
			throw new CoreException(status);
		}
	} else {
		// simple property or element value, parse it into its components
		i = prop.indexOf(':');
		if (i != -1) {
			executable = prop.substring(0, i).trim();
			initData = prop.substring(i + 1).trim();
		} else
			executable = prop;

		i = executable.indexOf('/');
		if (i != -1) {
			pluginName = executable.substring(0, i).trim();
			className = executable.substring(i + 1).trim();
		} else
			className = executable;
	}

	if (className == null || className.equals("")) { //$NON-NLS-1$
		String message = Policy.bind("plugin.extDefNoClass", attributeName ); //$NON-NLS-1$
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, null);
		logError(status);
		throw new CoreException(status);
	}

	IPluginDescriptor plugin = getDeclaringExtension().getDeclaringPluginDescriptor();
	return ((PluginDescriptor) plugin).createExecutableExtension(pluginName, className, initData, this, attributeName);
}
public String getAttribute(String name) {

	String s = getAttributeAsIs(name);	
	return s==null ? null : getDeclaringExtension().getDeclaringPluginDescriptor().getResourceString(s);
}
public String getAttributeAsIs(String name) {
	ConfigurationPropertyModel[] list = (ConfigurationPropertyModel[]) getProperties();
	if (list == null)
		return null;
	for (int i = 0; i < list.length; i++)
		if (name.equals(list[i].getName()))
			return list[i].getValue();
	return null;
}
public String[] getAttributeNames() {
	ConfigurationPropertyModel[] list = getProperties();
	if (list == null)
		return new String[0];
	String[] result = new String[list.length];
	for (int i = 0; i < list.length; i++)
		result[i] = list[i].getName();
	return result;
}
public IConfigurationElement[] getChildren() {
	ConfigurationElementModel[] list = getSubElements();
	if (list == null)
		return new IConfigurationElement[0];
	IConfigurationElement[] newValues = new IConfigurationElement[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
public IConfigurationElement[] getChildren(String name) {
	ConfigurationElementModel[] list = getSubElements();
	if (list == null)
		return new IConfigurationElement[0];
	ArrayList children = new ArrayList();
	for (int i = 0; i < list.length; i++) {
		ConfigurationElementModel	element = list[i];
		if (name.equals(element.getName()))
			children.add(list[i]);
	}
	return (IConfigurationElement[]) children.toArray(new IConfigurationElement[children.size()]);
}
public IExtension getDeclaringExtension() {
	return (IExtension) getParentExtension();
}
public String getValue() {
	String s = getValueAsIs();
	return s == null ? null : getDeclaringExtension().getDeclaringPluginDescriptor().getResourceString(s);
}
private void logError(IStatus status) {
	InternalPlatform.getRuntimePlugin().getLog().log(status);
	if (InternalPlatform.DEBUG)
		System.out.println(status.getMessage());
}
}
