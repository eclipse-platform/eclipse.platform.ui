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
package org.eclipse.search2.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.search.internal.ui.SearchPlugin;

/**
 * @author Thomas Mäder
 *
 */
public class ExtensionService {
	private Map fClassToInstance;
	private Map fAttributeToExtension;
	private Map fExtensionToInstance;
	
	public ExtensionService(String extensionPoint, String targetClassAttribute) {
		super();
		fExtensionToInstance= new HashMap();
		fClassToInstance= new HashMap();
		initializeExtensionCache(extensionPoint, targetClassAttribute);
	}

	private void initializeExtensionCache(String extensionPoint, String targetClassAttribute) {
		fAttributeToExtension= new HashMap();
		IConfigurationElement[] extensions=
			Platform.getPluginRegistry().getConfigurationElementsFor(
				extensionPoint);
		for (int i= 0; i < extensions.length; i++) {
			fAttributeToExtension.put(extensions[i].getAttribute(targetClassAttribute), extensions[i]);
		}

	}

	public Object getExtensionObject(Object element, Class expectedType) {
		Object obj= fClassToInstance.get(element.getClass());
		if (obj != null)
			return obj;
		if (fClassToInstance.containsKey(element.getClass()))
			return null;
		obj= internalGetExtensionObject(element, expectedType);
		if (obj != null)
		fClassToInstance.put(element.getClass(), obj);
		return obj;
	}

	private Object internalGetExtensionObject(Object element, Class expectedType) {
		IConfigurationElement configElement= (IConfigurationElement) fAttributeToExtension.get(element.getClass().getName());
		if (configElement == null) {
			if (fAttributeToExtension.containsKey(element.getClass().getName()))
				return null;
			configElement= getConfigElement(element.getClass());
			if (configElement != null)
			fAttributeToExtension.put(element.getClass().getName(), configElement);
		}
		
		if (configElement != null) {
			Object lp= fExtensionToInstance.get(configElement);
			if (lp == null) {
				if (fExtensionToInstance.containsKey(configElement))
					return null;
				Object extension;
				try {
					extension= configElement.createExecutableExtension("class"); //$NON-NLS-1$
					if (expectedType.isAssignableFrom(extension.getClass())) {
						fExtensionToInstance.put(configElement, extension);
						return extension;
					}
				} catch (CoreException e) {
					// programming error. Log it.
					SearchPlugin.getDefault().getLog().log(e.getStatus());
				}
			} else {
				return lp;
			}
		}
		return null;
	}

	private IConfigurationElement getConfigElement(Class clazz) {
		return searchInSupertypes(clazz);
	}

	private IConfigurationElement searchInSupertypes(Class clazz) {
		IConfigurationElement foundExtension= null;
		Class superclass= clazz.getSuperclass();
		if (superclass != null)
			foundExtension= (IConfigurationElement) fAttributeToExtension.get(superclass.getName());
		if (foundExtension != null)
			return foundExtension;
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			foundExtension= (IConfigurationElement) fAttributeToExtension.get(interfaces[i].getName());
			if (foundExtension != null)
				return foundExtension;
		}
		if (superclass != null)
			foundExtension= searchInSupertypes(superclass);
		if (foundExtension != null)
			return foundExtension;
		for (int i= 0; i < interfaces.length; i++) {
			foundExtension= searchInSupertypes(interfaces[i]);
			if (foundExtension != null)
				return foundExtension;
		}
		return null;
	}

}
