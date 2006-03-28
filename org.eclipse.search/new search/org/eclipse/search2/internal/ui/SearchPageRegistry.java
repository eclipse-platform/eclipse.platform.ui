/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;

import org.eclipse.search.internal.ui.SearchPlugin;

public class SearchPageRegistry {
	
	public static final String ID_EXTENSION_POINT= "org.eclipse.search.searchResultViewPages"; //$NON-NLS-1$
	public static final String ATTRIB_SEARCH_RESULT_CLASS= "searchResultClass"; //$NON-NLS-1$
	public static final String ATTRIB_ID= "id"; //$NON-NLS-1$

	public static final String ATTRIB_LABEL= "label"; //$NON-NLS-1$
	public static final String ATTRIB_ICON= "icon"; //$NON-NLS-1$
	
	private final Map fResultClassNameToExtension;
	private final Map fExtensionToInstance;
	private final IConfigurationElement[] fExtensions;
	
	public SearchPageRegistry() {
		fExtensionToInstance= new HashMap();
		fResultClassNameToExtension= new HashMap();
		fExtensions= Platform.getExtensionRegistry().getConfigurationElementsFor(ID_EXTENSION_POINT);
		for (int i= 0; i < fExtensions.length; i++) {
			fResultClassNameToExtension.put(fExtensions[i].getAttribute(ATTRIB_SEARCH_RESULT_CLASS), fExtensions[i]);
		}
	}

	public ISearchResultPage findPageForSearchResult(ISearchResult result, boolean create) {
		Class resultClass= result.getClass();
		IConfigurationElement configElement= findConfigurationElement(resultClass);
		if (configElement != null) {
			return getSearchResultPage(configElement, create);
		}
		return null;
	}
	
	public ISearchResultPage findPageForPageId(String pageId, boolean create) {
		IConfigurationElement configElement= findConfigurationElement(pageId);
		if (configElement != null) {
			return getSearchResultPage(configElement, create);
		}
		return null;
	}

	public String findLabelForPageId(String pageId) {
		IConfigurationElement configElement= findConfigurationElement(pageId);
		if (configElement != null) {
			return configElement.getAttribute(ATTRIB_LABEL);
		}
		return null;
	}

	private ISearchResultPage getSearchResultPage(final IConfigurationElement configElement, boolean create) {
		ISearchResultPage instance= (ISearchResultPage) fExtensionToInstance.get(configElement);
		if (instance == null && create) {
			final Object[] result= new Object[1];

			ISafeRunnable safeRunnable= new SafeRunnable(SearchMessages.SearchPageRegistry_error_creating_extensionpoint) {
				public void run() throws Exception {
					result[0]= configElement.createExecutableExtension("class"); //$NON-NLS-1$
				}
				public void handleException(Throwable e) {
					// invalid contribution
					SearchPlugin.log(e);
				}
			};
			SafeRunner.run(safeRunnable);
			if (result[0] instanceof ISearchResultPage) {
				instance= (ISearchResultPage) result[0];
				instance.setID(configElement.getAttribute(ATTRIB_ID));
				fExtensionToInstance.put(configElement, instance);
			}
		}
		return instance;
	}
	
	private IConfigurationElement findConfigurationElement(String pageId) {
		for (int i= 0; i < fExtensions.length; i++) {
			IConfigurationElement curr= fExtensions[i];
			if (pageId.equals(curr.getAttribute(ATTRIB_ID))) {
				return curr;
			}
		}
		return null;
	}
	
	private IConfigurationElement findConfigurationElement(Class resultClass) {
		String className= resultClass.getName();
		IConfigurationElement configElement= (IConfigurationElement) fResultClassNameToExtension.get(className);
		if (configElement != null) {
			return configElement;
		}
		Class superclass= resultClass.getSuperclass();
		if (superclass != null) {
			IConfigurationElement foundExtension= findConfigurationElement(superclass);
			if (foundExtension != null) {
				fResultClassNameToExtension.put(className, configElement);
				return foundExtension;
			}
		}
			
		Class[] interfaces= resultClass.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			IConfigurationElement foundExtension= findConfigurationElement(interfaces[i]);
			if (foundExtension != null) {
				fResultClassNameToExtension.put(className, configElement);
				return foundExtension;
			}
		}
		return null;
	}



}
