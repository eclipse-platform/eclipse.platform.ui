/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractContentExtensionProvider;
import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Manages content extensions (contributions into anchors and element
 * replacements) for user assistance documents.
 */
public class ContentExtensionManager {

	private static final String EXTENSION_POINT_ID_CONTENT_EXTENSION = HelpPlugin.PLUGIN_ID + ".contentExtension"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_CONTENT_EXTENSION_PROVIDER = "contentExtensionProvider"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_CLASS = "class"; //$NON-NLS-1$
	private static final ContentExtension[] EMPTY_ARRAY = new ContentExtension[0];
	
	private AbstractContentExtensionProvider[] contentExtensionProviders;
	private Map extensionsByPath;
	private Map replacesByPath;
	
	/*
	 * Returns all known extensions for the given locale.
	 */
	public ContentExtension[] getExtensions(String locale) {
		if (extensionsByPath == null) {
			loadExtensions(locale);
		}
		List extensions = new ArrayList();
		Iterator iter = extensionsByPath.values().iterator();
		while (iter.hasNext()) {
			extensions.addAll((Collection)iter.next());
		}
		iter = replacesByPath.values().iterator();
		while (iter.hasNext()) {
			extensions.addAll((Collection)iter.next());
		}
		return (ContentExtension[])extensions.toArray(new ContentExtension[extensions.size()]);
	}
	
	/*
	 * Get all extensions of the given type whose target matches the given path.
	 */
	public ContentExtension[] getExtensions(String path, int type, String locale) {
		if (extensionsByPath == null) {
			loadExtensions(locale);
		}
		Map map = (type == ContentExtension.CONTRIBUTION ? extensionsByPath : replacesByPath);
		List extensions = (List)map.get(path);
		if (extensions != null) {
			return (ContentExtension[])extensions.toArray(new ContentExtension[extensions.size()]);
		}
		return EMPTY_ARRAY;
	}
	
	/*
	 * Clears all cached data, forcing the manager to query the
	 * providers again next time a request is made.
	 */
	public void clearCache() {
		if (extensionsByPath != null) {
			extensionsByPath.clear();
		}
		if (replacesByPath != null) {
			replacesByPath.clear();
		}
	}

	/*
	 * Retrieves all extensions from all providers and organizes them by
	 * type.
	 */
	private void loadExtensions(String locale) {
		extensionsByPath = new HashMap();
		replacesByPath = new HashMap();
		contentExtensionProviders = getContentExtensionProviders();
		for (int i=0;i<contentExtensionProviders.length;++i) {
			ContentExtension[] extensions;
			try {
				Node[] nodes = contentExtensionProviders[i].getContentExtensions(locale);
				extensions = new ContentExtension[nodes.length];
				for (int j=0;j<nodes.length;++j) {
					ContentExtension ext = new ContentExtension(nodes[j]);
					ext.setContent(normalizePath(ext.getContent()));
					ext.setPath(normalizePath(ext.getPath()));
					extensions[j] = ext;
				}
			}
			catch (Throwable t) {
				String msg = "An error occured while querying one of the user assistance content extension providers (" + contentExtensionProviders[i] + ')'; //$NON-NLS-1$
				HelpPlugin.logError(msg, t);
				continue;
			}
			for (int j=0;j<extensions.length;++j) {
				try {
					ContentExtension ext = extensions[j];
					Map map = (ext.getType() == ContentExtension.CONTRIBUTION ? extensionsByPath : replacesByPath);
					List list = (List)map.get(ext.getPath());
					if (list == null) {
						list = new ArrayList();
						map.put(ext.getPath(), list);
					}
					list.add(ext);
				}
				catch (Throwable t) {
					String msg = "An error occured while querying one of the user assistance content extension providers (" + contentExtensionProviders[i] + ')'; //$NON-NLS-1$
					HelpPlugin.logError(msg, t);					
				}
			}
		}		
	}
	
	/*
	 * Returns all registered content extension providers (potentially cached).
	 */
	private AbstractContentExtensionProvider[] getContentExtensionProviders() {
		if (contentExtensionProviders == null) {
			List providers = new ArrayList();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_CONTENT_EXTENSION);
			for (int i=0;i<elements.length;++i) {
				IConfigurationElement elem = elements[i];
				try {
					if (elem.getName().equals(ELEMENT_NAME_CONTENT_EXTENSION_PROVIDER)) {
						String className = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
						if (className != null) {
							try {
								AbstractContentExtensionProvider provider = (AbstractContentExtensionProvider)elem.createExecutableExtension(ATTRIBUTE_NAME_CLASS);
								providers.add(provider);
							}
							catch (CoreException e) {
								// log and skip
								String msg = "Error instantiating " + ELEMENT_NAME_CONTENT_EXTENSION_PROVIDER + " class"; //$NON-NLS-1$ //$NON-NLS-2$
								HelpPlugin.logError(msg, e);
							}
							catch (ClassCastException e) {
								// log and skip
								String msg = ELEMENT_NAME_CONTENT_EXTENSION_PROVIDER + " class must implement " + AbstractContentExtensionProvider.class.getName(); //$NON-NLS-1$
								HelpPlugin.logError(msg, e);
							}
						}
						else {
							// log the missing class attribute and skip
							String msg = ELEMENT_NAME_CONTENT_EXTENSION_PROVIDER + " element of extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " must specify a " + ATTRIBUTE_NAME_CLASS + " attribute"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							try {
								msg += " (declared from plug-in " + elem.getNamespaceIdentifier() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							}
							catch (InvalidRegistryObjectException e) {
								// skip the declaring plugin part
							}
							HelpPlugin.logError(msg, null);
						}
					}
				}
				catch (InvalidRegistryObjectException e) {
					// no longer valid; skip it
				}
			}
			contentExtensionProviders = (AbstractContentExtensionProvider[])providers.toArray(new AbstractContentExtensionProvider[providers.size()]);
		}
		return contentExtensionProviders;
	}
	
	/*
	 * Normalizes the given path by adding a leading slash if one doesn't
	 * exist, and converting the final slash into a '#' if it is thought to
	 * separate the end of the document with the element (legacy form).
	 */
	private String normalizePath(String path) {
		int bundleStart, bundleEnd;
		int pathStart, pathEnd;
		int elementStart, elementEnd;
		
		bundleStart = path.charAt(0) == '/' ? 1 : 0;
		bundleEnd = path.indexOf('/', bundleStart);
		
		pathStart = bundleEnd + 1;
		pathEnd = path.indexOf('#', pathStart);
		if (pathEnd == -1) {
			int lastSlash = path.lastIndexOf('/');
			if (lastSlash > 0) {
				int secondLastSlash = path.lastIndexOf('/', lastSlash - 1);
				if (secondLastSlash != -1) {
					String secondLastToken = path.substring(secondLastSlash, lastSlash);
					boolean hasDot = (secondLastToken.indexOf('.') != -1);
					if (hasDot) {
						pathEnd = lastSlash;
					}
				}
			}
			if (pathEnd == -1) {
				pathEnd = path.length();
			}
		}
		
		elementStart = Math.min(pathEnd + 1, path.length());
		elementEnd = path.length();
		
		if (bundleEnd > bundleStart && pathStart > bundleEnd && pathEnd > pathStart && elementStart >= pathEnd && elementEnd >= elementStart) {
			String bundleId = path.substring(bundleStart, bundleEnd);
			String relativePath = path.substring(pathStart, pathEnd);
			String elementId = path.substring(elementStart, elementEnd);
			path = '/' + bundleId + '/' + relativePath;
			if (elementId.length() > 0) {
				path += '#' + elementId;
			}
		}
		return path;
	}
}
