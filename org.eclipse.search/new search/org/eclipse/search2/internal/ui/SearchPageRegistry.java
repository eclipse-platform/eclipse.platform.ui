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
package org.eclipse.search2.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.search.ui.ISearchResultPage;

import org.eclipse.search.internal.ui.SearchPlugin;

public class SearchPageRegistry {
	private Map fClassToInstance;
	private Map fTargetClassNameToExtension;
	private Map fExtensionToInstance;
	private String fIdAttribute;
	
	public SearchPageRegistry(String extensionPoint, String targetClassAttribute, String idAttribute) {
		super();
		fExtensionToInstance= new HashMap();
		fClassToInstance= new HashMap();
		initializeExtensionCache(extensionPoint, targetClassAttribute);
		fIdAttribute= idAttribute;
	}

	private void initializeExtensionCache(String extensionPoint, String targetClassAttribute) {
		fTargetClassNameToExtension= new HashMap();
		IConfigurationElement[] extensions=
			Platform.getExtensionRegistry().getConfigurationElementsFor(
				extensionPoint);
		for (int i= 0; i < extensions.length; i++) {
			fTargetClassNameToExtension.put(extensions[i].getAttribute(targetClassAttribute), extensions[i]);
		}

	}

	public ISearchResultPage getExtensionObject(Object element, Class expectedType) {
		ISearchResultPage page= (ISearchResultPage) fClassToInstance.get(element.getClass());
		if (page != null)
			return page;
		if (fClassToInstance.containsKey(element.getClass()))
			return null;
		page= internalGetExtensionObject(element, expectedType);
		if (page != null)
		fClassToInstance.put(element.getClass(), page);
		return page;
	}
	
	private ISearchResultPage internalGetExtensionObject(Object element, Class expectedType) {
		IConfigurationElement configElement= (IConfigurationElement) fTargetClassNameToExtension.get(element.getClass().getName());
		if (configElement == null) {
			if (fTargetClassNameToExtension.containsKey(element.getClass().getName()))
				return null;
			configElement= getConfigElement(element.getClass());
			if (configElement != null)
			fTargetClassNameToExtension.put(element.getClass().getName(), configElement);
		}
		
		if (configElement != null) {
			ISearchResultPage lp= (ISearchResultPage) fExtensionToInstance.get(configElement);
			if (lp == null) {
				if (fExtensionToInstance.containsKey(configElement))
					return null;
				ISearchResultPage instance;
				try {
					instance= (ISearchResultPage) configElement.createExecutableExtension("class"); //$NON-NLS-1$
					String id= configElement.getAttribute(fIdAttribute);
					instance.setID(id);
					if (expectedType.isAssignableFrom(instance.getClass())) {
						fExtensionToInstance.put(configElement, instance);
						return instance;
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
			foundExtension= (IConfigurationElement) fTargetClassNameToExtension.get(superclass.getName());
		if (foundExtension != null)
			return foundExtension;
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			foundExtension= (IConfigurationElement) fTargetClassNameToExtension.get(interfaces[i].getName());
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
