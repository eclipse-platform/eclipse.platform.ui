/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of an extension
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */

public class ConfigurationElement extends NestedRegistryModelObject implements IConfigurationElement {
	int PLUGIN_ERROR = 1;

	// DTD properties (included in plug-in manifest)
	private String value = null;
	private ConfigurationProperty[] properties = null;
	private IConfigurationElement[] children = null;

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
				pluginName = element.getAttribute("plugin"); //$NON-NLS-1$
				className = element.getAttribute("class"); //$NON-NLS-1$
				parms = element.getChildren("parameter"); //$NON-NLS-1$
				if (parms != null) {
					initParms = new Hashtable(parms.length + 1);
					for (i = 0; i < parms.length; i++) {
						pname = parms[i].getAttribute("name"); //$NON-NLS-1$
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
				IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, PLUGIN_ERROR, message, null); //$NON-NLS-1$
				InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundleContext().getBundle()).log(status); //$NON-NLS-1$
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
			String message = Policy.bind("plugin.extDefNoClass", attributeName); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, PLUGIN_ERROR, message, null); //$NON-NLS-1$ 
			InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundleContext().getBundle()).log(status); //$NON-NLS-1$

			throw new CoreException(status);
		}

		return createExecutableExtension(InternalPlatform.getDefault().getBundle(getDeclaringExtension().getNamespace()), pluginName, className, initData, this, attributeName);
	}

	Object createExecutableExtension(Bundle bundle, String pluginName, String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
		String id = bundle.getSymbolicName(); // this plugin id
		// check if we need to delegate to some other plugin
		if (pluginName != null && !pluginName.equals("") && !pluginName.equals(id)) { //$NON-NLS-1$
			Bundle otherBundle = null;
			otherBundle = InternalPlatform.getDefault().getBundle(pluginName);
			return createExecutableExtension(otherBundle, className, initData, cfig, propertyName);
		}
		return createExecutableExtension(bundle, className, initData, cfig, propertyName);
	}

	public Object createExecutableExtension(Bundle bundle, String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
		// load the requested class from this plugin
		Class classInstance = null;
		try {
			classInstance = bundle.loadClass(className);
		} catch (Exception e1) {
			throwException(Policy.bind("plugin.loadClassError", bundle.getSymbolicName(), className), e1); //$NON-NLS-1$
		} catch (LinkageError e) {
			throwException(Policy.bind("plugin.loadClassError", bundle.getSymbolicName(), className), e); //$NON-NLS-1$
		}

		// create a new instance
		Object result = null;
		try {
			result = classInstance.newInstance();
		} catch (Exception e) {
			throwException(Policy.bind("plugin.instantiateClassError", bundle.getSymbolicName(), className), e); //$NON-NLS-1$
		}

		// check if we have extension adapter and initialize
		if (result instanceof IExecutableExtension) {
			try {
				// make the call even if the initialization string is null
				((IExecutableExtension) result).setInitializationData(cfig, propertyName, initData);
			} catch (CoreException ce) {
				// user code threw exception
				InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundleContext().getBundle()).log(ce.getStatus());
				throw new CoreException(ce.getStatus());
			} catch (Exception te) {
				// user code caused exception
				throwException(Policy.bind("policy.initObjectError", bundle.getSymbolicName(), className), te); //$NON-NLS-1$
			}
		}
		return result;
	}

	private void throwException(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, PLUGIN_ERROR, message, exception);
		InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundleContext().getBundle()).log(status);
		throw new CoreException(status);
	}

	/**
	 * Returns the extension in which this configuration element is declared.
	 * If this element is a top-level child of an extension, the returned value
	 * is equivalent to <code>getParent</code>.
	 *
	 * @return the extension in which this configuration element is declared
	 *  or <code>null</code>
	 */
	public Extension getParentExtension() {
		Object p = getParent();
		while (p != null && p instanceof ConfigurationElement)
			p = ((ConfigurationElement) p).getParent();
		return (Extension) p;
	}

	public IExtension getDeclaringExtension() {
		return getParentExtension();
	}

	/**
	 * Returns the properties associated with this element.
	 *
	 * @return the properties associated with this element
	 *  or <code>null</code>
	 */
	public ConfigurationProperty[] getProperties() {
		return properties;
	}

	/**
	 * Returns the value of this element.
	 * 
	 * @return the value of this element or <code>null</code>
	 */
	public String getValue() {
		String s = getValueAsIs();
		if (s == null)
			return null;
		return s;
	}

	public String getValueAsIs() {
		return value;
	}

	/**
	 * Returns this element's sub-elements.
	 *
	 * @return the sub-elements of this element or <code>null</code>
	 */
	public IConfigurationElement[] getChildren() {
		return children == null ? new IConfigurationElement[0] : children;
	}

	public IConfigurationElement[] getChildren(String name) {
		IConfigurationElement[] list = getChildren();
		if (list == null)
			return new IConfigurationElement[0];
		List children = new ArrayList();
		for (int i = 0; i < list.length; i++) {
			IConfigurationElement element = list[i];
			if (name.equals(element.getName()))
				children.add(list[i]);
		}
		return (IConfigurationElement[]) children.toArray(new IConfigurationElement[children.size()]);
	}

	public String getAttribute(String name) {
		ConfigurationProperty[] list = getProperties();
		if (list == null)
			return null;

		ConfigurationProperty found = null;
		for (int i = 0; i < list.length; i++)
			if (name.equals(list[i].getName())) {
				found = list[i];
				break;
			}
		return found == null ? null : found.getValue();
	}

	public String getAttributeAsIs(String name) {
		ConfigurationProperty[] list = getProperties();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++)
			if (name.equals(list[i].getName()))
				return list[i].getValue();
		return null;
	}

	public String[] getAttributeNames() {
		ConfigurationProperty[] list = getProperties();
		if (list == null)
			return new String[0];
		String[] result = new String[list.length];
		for (int i = 0; i < list.length; i++)
			result[i] = list[i].getName();
		return result;
	}

	/**
	 * Sets the properties associated with this element. 
	 *
	 * @param value the properties to associate with this element.  May be <code>null</code>.
	 */
	public void setProperties(ConfigurationProperty[] value) {
		properties = value;
	}

	/**
	 * Sets configuration elements contained by this element
	 *
	 * @param value the configuration elements to be associated with this element.  
	 *		May be <code>null</code>.
	 */
	public void setChildren(IConfigurationElement[] value) {
		children = value;
	}

	/**
	 * Sets the value of this element.  
	 * 
	 * @param value the new value of this element.  May be <code>null</code>.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.  Avoids having
	 * to access resource bundles for further lookups.
	 */
	public void setLocalizedValue(String value) {
		this.value = value;
		((ExtensionRegistry) InternalPlatform.getDefault().getRegistry()).setDirty(true);
	}
}