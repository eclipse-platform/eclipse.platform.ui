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
package org.eclipse.core.internal.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.registry.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of an extension
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */

public class ConfigurationElement extends RegistryModelObject implements IConfigurationElement {

	// DTD properties (included in plug-in manifest)
	private String value = null;
	private ConfigurationProperty[] properties = null;
	private IConfigurationElement[] children = null;

	//This field is only used when doing createExecutableExtension 
	private Object oldStyleConfigurationElement = null;

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
				IStatus status = new Status(IStatus.ERROR, "org.eclipse.core.runtime", IRegistryConstants.PLUGIN_ERROR, message, null); //$NON-NLS-1$
				InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(status); //$NON-NLS-1$
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
			IStatus status = new Status(IStatus.ERROR, "org.eclipse.core.runtime", IRegistryConstants.PLUGIN_ERROR, message, null); //$NON-NLS-1$ 
			InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(status); //$NON-NLS-1$

			throw new CoreException(status);
		}

		return createExecutableExtension(InternalPlatform.getDefault().getBundle(getDeclaringExtension().getParentIdentifier()), pluginName, className, initData, this, attributeName);
	}

	Object createExecutableExtension(Bundle bundle, String pluginName, String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
		String id = bundle.getGlobalName(); // this plugin id
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
			if (bundle.getState() != Bundle.STARTING && bundle.getState() != Bundle.ACTIVE) //This is necessary to support recursive call on createExecutableExtension on the same bundle
				bundle.start();
			//classInstance = getPluginClassLoader(true).loadClass(className);z
			classInstance = bundle.loadClass(className);
		} catch (Exception e1) {
			throwException(Policy.bind("plugin.loadClassError", bundle.getGlobalName(), className), e1); //$NON-NLS-1$
		}

		// create a new instance
		Object result = null;
		try {
			result = classInstance.newInstance();
		} catch (Exception e) {
			throwException(Policy.bind("plugin.instantiateClassError", bundle.getGlobalName(), className), e); //$NON-NLS-1$
		}

		// check if we have extension adapter and initialize
		if (result instanceof IExecutableExtension) {
			try {
				// make the call even if the initialization string is null
				 ((IExecutableExtension) result).setInitializationData(cfig, propertyName, initData);
			} catch (CoreException ce) {
				// user code threw exception
				InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(ce.getStatus());
				throw new CoreException(ce.getStatus());
			} catch (Exception te) {
				// user code caused exception
				throwException(Policy.bind("policy.initObjectError", bundle.getGlobalName(), className), te); //$NON-NLS-1$
			}
			return result;
		}

		//Backward compatibility handling of configurationElement
		if (oldStyleConfigurationElement != null || implementsIExecutableExtension(result)) {
			Class oldConfigurationElement = null;
			Method executableExtension = null;
			try {
				oldConfigurationElement = Class.forName("org.eclipse.core.internal.plugins.ConfigurationElement");
				try {
					executableExtension = oldConfigurationElement.getMethod("runOldExecutableExtension", new Class[] { Object.class, String.class, Object.class });
					try {
						if (oldStyleConfigurationElement == null)
							oldStyleConfigurationElement = createOldStyleConfigurationElement(this);
						executableExtension.invoke(oldStyleConfigurationElement, new Object[] { result, propertyName, initData });
					} catch (Exception e) {
						if (e instanceof CoreException) {
							CoreException ce = (CoreException) e;
							InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(ce.getStatus());
							throw new CoreException(ce.getStatus());
						}
						throwException(Policy.bind("policy.initObjectError", bundle.getGlobalName(), className), e); //$NON-NLS-1$
					}
				} catch (SecurityException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (NoSuchMethodException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			} catch (ClassNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return result;
		}

		return result;
	}

	private void throwException(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "org.eclipse.core.runtime", IRegistryConstants.PLUGIN_ERROR, message, exception); //$NON-NLS-1$
		InternalPlatform.getDefault().getLog(InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime")).log(status); //$NON-NLS-1$
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
		return (IExtension) getParentExtension();
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
		BundleModel bundleModel = (BundleModel) ((Extension) getDeclaringExtension()).getParent();
		return bundleModel.getResourceString(s);
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
		String s;
		if (found == null || (s = found.getValue()) == null)
			return null;
		//replace the key with its localized value
		BundleModel bundleModel = (BundleModel) ((Extension) getDeclaringExtension()).getParent();
		String localized = bundleModel.getResourceString(s);
		if (localized != s)
			found.setLocalizedValue(localized);
		return localized;
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
	 * Sets this model object and all of its descendents to be read-only.
	 * Subclasses may extend this implementation.
	 *
	 * @see #isReadOnly
	 */
	public void markReadOnly() {
		super.markReadOnly();
		if (children != null)
			for (int i = 0; i < children.length; i++)
				 ((ConfigurationElement) children[i]).markReadOnly();
		if (properties != null)
			for (int i = 0; i < properties.length; i++)
				properties[i].markReadOnly();
	}
	/**
	 * Sets the properties associated with this element.  This object must not be read-only.
	 *
	 * @param value the properties to associate with this element.  May be <code>null</code>.
	 */
	public void setProperties(ConfigurationProperty[] value) {
		assertIsWriteable();
		properties = value;
	}
	/**
	 * Sets configuration elements contained by this element
	 * This object must not be read-only.
	 *
	 * @param value the configuration elements to be associated with this element.  
	 *		May be <code>null</code>.
	 */
	public void setChildren(IConfigurationElement[] value) {
		assertIsWriteable();
		children = value;
	}
	/**
	 * Sets the value of this element.  This object must not be read-only.
	 * 
	 * @param value the new value of this element.  May be <code>null</code>.
	 */
	public void setValue(String value) {
		assertIsWriteable();
		this.value = value;
	}

	public void setOldStyleConfigurationElement(Object ce) {
		oldStyleConfigurationElement = ce;
	}

	private boolean implementsIExecutableExtension(Object o) {
		Bundle compatibility = InternalPlatform.getDefault().getBundle("org.eclipse.core.runtime.compatibility");
		if (compatibility != null) {
			Class oldConfigurationElement = null;
			try {
				oldConfigurationElement = compatibility.loadClass("org.eclipse.core.internal.plugins.ConfigurationElement");
				Method testExecutableExtensionType = oldConfigurationElement.getMethod("implementsIExecutableExtension", new Class[] { Object.class });
				return ((Boolean) testExecutableExtensionType.invoke(oldConfigurationElement, new Object[] { o })).booleanValue();
			} catch (Exception e) {
				//Ignore the exceptions, return false 
			}
		}
		return false;
	}

	/* create an instance of the old configurationElement using reflection. Reflection is used so the runtime code can run without the compatibility fragment*/
	private Object createOldStyleConfigurationElement(Object o) {
		Class oldConfigurationElement = null;
		try {
			oldConfigurationElement = Class.forName("org.eclipse.core.internal.plugins.ConfigurationElement");
			Constructor constructor = oldConfigurationElement.getConstructor(new Class[] { org.eclipse.core.runtime.registry.IConfigurationElement.class });
			return constructor.newInstance(new Object[] { o });
		} catch (Exception e) {
			e.printStackTrace();
			//Ignore the exceptions, return the 
		}
		return null;
	}
}
