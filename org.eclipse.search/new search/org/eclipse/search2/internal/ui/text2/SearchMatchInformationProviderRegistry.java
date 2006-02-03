/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.search.ui.text.SearchMatchInformationProvider;

import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search2.internal.ui.SearchMessages;

public class SearchMatchInformationProviderRegistry {
	private static final String EXTENSION_POINT_ID= "org.eclipse.search.textSearchMatchInformationProvider"; //$NON-NLS-1$

	private static final String ATTRIBUTE_SEPARATOR= ","; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILE_EXTENSIONS= "fileExtensions"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT_TYPE_IDS= "contentTypeIDs"; //$NON-NLS-1$
	private static final String ATTRIBUTE_COMMENT_SUPPORT= "supportsComments"; //$NON-NLS-1$
	private static final String ATTRIBUTE_STRING_SUPPORT= "supportsStrings"; //$NON-NLS-1$
	private static final String ATTRIBUTE_INCLUDE_SUPPORT= "supportsIncludes"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PREPROCESSOR_SUPPORT= "supportsPreprocessor"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FUNCTION_SUPPORT= "supportsFunctions"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS= "class"; //$NON-NLS-1$
	private static final String NODE_NAME= "textSearchMatchInformationProvider"; //$NON-NLS-1$
	private static final ScannerProxyChain NULL_CHAIN= new ScannerProxyChain();

	private static class ScannerProxyChain {
		private ArrayList fProxies= new ArrayList();
		private SearchMatchInformationProvider fScanner= null;
		public void sort() {
			Collections.sort(fProxies);
		}
		public void add(ScannerProxy proxy) {
			fProxies.add(proxy);
		}
		public SearchMatchInformationProvider getScanner() {
			return fScanner;
		}
		public SearchMatchInformationProvider computeScanner() {
			if (fScanner == null) {
				for (Iterator iter= fProxies.iterator(); fScanner == null && iter.hasNext();) {
					ScannerProxy proxy= (ScannerProxy) iter.next();
					if (proxy.isDisabled()) {
						iter.remove();
					} else {
						fScanner= proxy.createScanner();
					}
				}
			}
			return fScanner;
		}
	}

	private static class ScannerProxy implements Comparable {
		public static final ScannerProxy NULL_PROXY= new ScannerProxy(null, 0);
		private IConfigurationElement fConfigElem;
		private int fPriority;
		private boolean fIsDisabled= false;

		public ScannerProxy(IConfigurationElement elem, int locationSupport) {
			fConfigElem= elem;
			fPriority= getPriority(locationSupport);

		}

		public SearchMatchInformationProvider createScanner() {
			final SearchMatchInformationProvider[] holder= {null};
			ISafeRunnable code= new SafeRunnable(SearchMessages.TextFileScannerRegistry_error_instanciateScanner) {
				public void run() throws Exception {
					holder[0]= (SearchMatchInformationProvider) fConfigElem.createExecutableExtension(ATTRIBUTE_CLASS);
				}
				public void handleException(Throwable e) {
					disable();
				}
			};
			Platform.run(code);
			return holder[0];
		}

		public int getPriority(int locations) {
			int priority= 0;
			if (supportsLocation(locations, SearchMatchInformationProvider.LOCATION_COMMENT)) {
				priority+= 3;
			}
			if (supportsLocation(locations, SearchMatchInformationProvider.LOCATION_STRING_LITERAL)) {
				priority+= 3;
			}
			if (supportsLocation(locations, SearchMatchInformationProvider.LOCATION_PREPROCESSOR_DIRECTIVE)) {
				priority+= 1;
			}
			if (supportsLocation(locations, SearchMatchInformationProvider.LOCATION_IMPORT_OR_INCLUDE_STATEMENT)) {
				priority+= 2;
			}
			if (supportsLocation(locations, SearchMatchInformationProvider.LOCATION_FUNCTION)) {
				priority+= 1;
			}
			return priority;
		}

		public int compareTo(Object o) {
			return -(fPriority - ((ScannerProxy) o).fPriority);
		}

		public boolean isDisabled() {
			return fIsDisabled;
		}

		public void disable() {
			fIsDisabled= true;
		}
	}

	private HashMap fContentTypeMap= new HashMap();
	private HashMap fExtensionsMap= new HashMap();
	private SearchMatchInformationProvider fLineNumberScanner= new LineNumberScanner();
	private int fAvailableLocations;

	public SearchMatchInformationProviderRegistry() {
		registerExtensions();
	}

	private void registerExtensions() {
		fAvailableLocations= 0;
		IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement elem= extensions[i];
			if (NODE_NAME.equals(elem.getName())) {
				String contentTypeIDs= elem.getAttribute(ATTRIBUTE_CONTENT_TYPE_IDS);
				String fileExtensions= elem.getAttribute(ATTRIBUTE_FILE_EXTENSIONS);
				int locationSupport= getLocationSupport(elem);
				fAvailableLocations|= locationSupport;
				ScannerProxy proxy= new ScannerProxy(elem, locationSupport);
				registerProxy(contentTypeIDs, proxy, fContentTypeMap);
				registerProxy(fileExtensions, proxy, fExtensionsMap);
			}
		}
		sortProxyChains(fContentTypeMap.values());
		sortProxyChains(fExtensionsMap.values());
	}

	private void sortProxyChains(Collection chains) {
		for (Iterator iter= chains.iterator(); iter.hasNext();) {
			ScannerProxyChain chain= (ScannerProxyChain) iter.next();
			chain.sort();
		}
	}

	private int getLocationSupport(IConfigurationElement elem) {
		int result= 0;
		result|= hasSupport(elem, ATTRIBUTE_COMMENT_SUPPORT, 1 << SearchMatchInformationProvider.LOCATION_COMMENT);
		result|= hasSupport(elem, ATTRIBUTE_STRING_SUPPORT, 1 << SearchMatchInformationProvider.LOCATION_STRING_LITERAL);
		result|= hasSupport(elem, ATTRIBUTE_INCLUDE_SUPPORT, 1 << SearchMatchInformationProvider.LOCATION_IMPORT_OR_INCLUDE_STATEMENT);
		result|= hasSupport(elem, ATTRIBUTE_PREPROCESSOR_SUPPORT, 1 << SearchMatchInformationProvider.LOCATION_PREPROCESSOR_DIRECTIVE);
		result|= hasSupport(elem, ATTRIBUTE_FUNCTION_SUPPORT, 1 << SearchMatchInformationProvider.LOCATION_FUNCTION);
		return result;
	}

	private int hasSupport(IConfigurationElement elem, String attrib, int value) {
		String aval= elem.getAttribute(attrib);
		if (aval != null && Boolean.valueOf(aval).booleanValue()) {
			return value;
		}
		return 0;
	}

	private void registerProxy(String idList, ScannerProxy proxy, HashMap map) {
		if (idList != null) {
			String[] ids= idList.split(ATTRIBUTE_SEPARATOR);
			for (int k= 0; k < ids.length; k++) {
				String id= ids[k].trim();
				if (id.length() > 0) {
					ScannerProxyChain chain= (ScannerProxyChain) map.get(id);
					if (chain == null) {
						chain= new ScannerProxyChain();
						map.put(id, chain);
					}
					chain.add(proxy);
				}
			}
		}
	}

	public SearchMatchInformationProvider findScanner(IFile file) {
		ScannerProxyChain chain= NULL_CHAIN;
		if (file != null) {
			// first try to find a scanner with the content type
			IProject prj= file.getProject();
			IContentType ct;
			try {
				ct= prj.getContentTypeMatcher().findContentTypeFor(file.getName());
				if (ct != null) {
					chain= findScanner(ct);
				}
			} catch (CoreException e) {
				SearchPlugin.log(e.getStatus());
			}

			// try to find scanner by file extension
			if (chain == NULL_CHAIN) {
				chain= findScanner(file.getName());
			}
		}
		return chain.getScanner();
	}

	private ScannerProxyChain findScanner(IContentType ct) {
		String id= ct.getId();
		ScannerProxyChain chain= (ScannerProxyChain) fContentTypeMap.get(id);
		if (chain != null) {
			if (chain == NULL_CHAIN || chain.computeScanner() != null) {
				return chain;
			}
		}

		// no cached element
		ct= ct.getBaseType();
		chain= ct == null ? NULL_CHAIN : findScanner(ct);

		fContentTypeMap.put(id, chain);
		return chain;
	}

	private ScannerProxyChain findScanner(String name) {
		int idx= 0;
		while (true) {
			ScannerProxyChain chain= (ScannerProxyChain) fExtensionsMap.get(name);
			if (chain != null) {
				if (chain.computeScanner() != null) {
					return chain;
				}
				fExtensionsMap.remove(name);
			}

			idx= name.indexOf('.', 1);
			if (idx < 0) {
				return NULL_CHAIN;
			}
			name= name.substring(idx);
		}
	}

	public SearchMatchInformationProvider getLineNumberScanner() {
		return fLineNumberScanner;
	}

	private static boolean supportsLocation(int locations, int location) {
		return (locations & (1 << location)) != 0;
	}

	public boolean hasPreprocessorSupport() {
		return supportsLocation(fAvailableLocations, SearchMatchInformationProvider.LOCATION_PREPROCESSOR_DIRECTIVE);
	}

	public boolean hasFunctionSupport() {
		return supportsLocation(fAvailableLocations, SearchMatchInformationProvider.LOCATION_FUNCTION);
	}
}
