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

import java.util.Hashtable;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of an extension
 * in a plug-in manifest.
 */
public class ConfigurationElement extends RegistryObject {
	static final ConfigurationElement[] EMPTY_ARRAY = new ConfigurationElement[0];
	static final int PLUGIN_ERROR = 1;

	//The id of the parent element. It can be a configuration element or an extension
	int parentId;
	byte parentType; //This value is only interesting when running from cache.

	//Store the properties and the value of the configuration element.
	//The format is the following: 
	//	[p1, v1, p2, v2, configurationElementValue]
	//If the array size is even, there is no "configurationElementValue (ie getValue returns null)".
	//The properties and their values are alternated (v1 is the value of p1). 
	private String[] propertiesAndValue;

	//The name of the configuration element
	private String name;

	//The bundle from which classes will be loaded. It is never a fragment
	//This value can be null when the element is loaded from disk and the bundle has been uninstalled.
	//This happens when the configuration is obtained from a delta containing removed extension.
	private Bundle contributingBundle;

	ConfigurationElement() {
		//Nothing to do
	}

	ConfigurationElement(int self, Bundle bundle, String name, String[] propertiesAndValue, int[] children, int extraDataOffset, int parent, byte parentType) {
		setObjectId(self);
		contributingBundle = bundle;
		this.name = name;
		this.propertiesAndValue = propertiesAndValue;
		setRawChildren(children);
		this.extraDataOffset = extraDataOffset;
		parentId = parent;
		this.parentType = parentType;
	}

	Object createExecutableExtension(String attributeName) throws CoreException {
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
			ConfigurationElement[] exec;
			ConfigurationElement[] parms;
			ConfigurationElement element;
			Hashtable initParms;
			String pname;

			exec = getChildren(attributeName);
			if (exec.length != 0) {
				element = exec[0]; // assumes single definition
				pluginName = element.getAttribute("plugin"); //$NON-NLS-1$
				className = element.getAttribute("class"); //$NON-NLS-1$
				parms = element.getChildren("parameter"); //$NON-NLS-1$
				if (parms.length != 0) {
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

		return createExecutableExtension(pluginName, className, initData, this, attributeName);
	}

	private Object createExecutableExtension(String pluginName, String className, Object initData, ConfigurationElement cfig, String propertyName) throws CoreException {
		if(contributingBundle==null) {
			throwException(Policy.bind("plugin.loadClassError", "UNKNOWN BUNDLE", className), new InvalidRegistryObjectException());  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
		String id = contributingBundle.getSymbolicName(); // this plugin id check if we need to delegate to some other plugin
		if (pluginName != null && !pluginName.equals("") && !pluginName.equals(id)) { //$NON-NLS-1$
			Bundle otherBundle = null;
			otherBundle = InternalPlatform.getDefault().getBundle(pluginName);
			return createExecutableExtension(otherBundle, className, initData, cfig, propertyName);
		}
		return createExecutableExtension(contributingBundle, className, initData, cfig, propertyName);
	}

	private Object createExecutableExtension(Bundle bundle, String className, Object initData, ConfigurationElement cfig, String propertyName) throws CoreException {
		if(contributingBundle==null) {
			throwException(Policy.bind("plugin.loadClassError", "UNKNOWN BUNDLE", className), new InvalidRegistryObjectException());  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
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
				//TODO Need to change here the access to the registry manager
				((IExecutableExtension) result).setInitializationData(new ConfigurationElementHandle(((ExtensionRegistry)InternalPlatform.getDefault().getRegistry()).getObjectManager(),cfig.getObjectId()), propertyName, initData);
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

	String getValue() {
		return getValueAsIs();
	}

	String getValueAsIs() {
		if (propertiesAndValue.length != 0 && propertiesAndValue.length % 2 == 1)
			return propertiesAndValue[propertiesAndValue.length - 1];
		return null;
	}

	String getAttribute(String attrName) {
		return getAttributeAsIs(attrName);
	}

	String getAttributeAsIs(String attrName) {
		if (propertiesAndValue.length <= 1)
			return null;
		int size = propertiesAndValue.length - (propertiesAndValue.length % 2);
		for (int i = 0; i < size; i += 2) {
			if (propertiesAndValue[i].equals(attrName))
				return propertiesAndValue[i + 1];
		}
		return null;
	}

	String[] getAttributeNames() {
		if (propertiesAndValue.length <= 1)
			return RegistryObjectManager.EMPTY_STRING_ARRAY;

		int size = propertiesAndValue.length / 2;
		String[] result = new String[size];
		for (int i = 0; i < size; i++) {
			result[i] = propertiesAndValue[i * 2];
		}
		return result;
	}

	void setProperties(String[] value) {
		propertiesAndValue = value;
	}

	String[] getPropertiesAndValue() {
		return propertiesAndValue;
	}

	void setValue(String value) {
		if (propertiesAndValue.length == 0) {
			propertiesAndValue = new String[] {value};
			return;
		}
		if (propertiesAndValue.length % 2 == 1) {
			propertiesAndValue[propertiesAndValue.length - 1] = value;
			return;
		}
		String[] newPropertiesAndValue = new String[propertiesAndValue.length + 1];
		System.arraycopy(propertiesAndValue, 0, newPropertiesAndValue, 0, propertiesAndValue.length);
		newPropertiesAndValue[propertiesAndValue.length] = value;
		propertiesAndValue = newPropertiesAndValue;
	}

	void setContributingBundle(Bundle b) {
		contributingBundle = b;
	}

	Bundle getContributingBundle() {
		return contributingBundle;
	}

	ConfigurationElement[] getChildren(String childrenName) {
		if (getRawChildren().length == 0)
			return ConfigurationElement.EMPTY_ARRAY;

		ConfigurationElement[] result = new ConfigurationElement[1]; //Most of the time there is only one match
		int idx = 0;
		RegistryObjectManager objectManager = ((ExtensionRegistry)InternalPlatform.getDefault().getRegistry()).getObjectManager(); //TODO To change
		for (int i = 0; i < children.length; i++) {
			ConfigurationElement toTest = (ConfigurationElement) objectManager.getObject(children[i], extraDataOffset == -1 ? RegistryObjectManager.CONFIGURATION_ELEMENT : RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
			if (toTest.name.equals(childrenName)) {
				if (idx != 0) {
					ConfigurationElement[] copy = new ConfigurationElement[result.length + 1];
					System.arraycopy(result, 0, copy, 0, result.length);
					result = copy;
				}
				result[idx++] = toTest;
			}
		}
		if (idx == 0)
			result = ConfigurationElement.EMPTY_ARRAY;
		return result;
	}

	void setParentId(int objectId) {
		parentId = objectId;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	void setParentType(byte type) {
		parentType = type;
	}
	
	String getNamespace() {
		return contributingBundle == null ? null : contributingBundle.getSymbolicName();
	}
}