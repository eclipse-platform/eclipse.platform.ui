/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.IContext;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Manages all context-sensitive help data for the help system.
 */
public class ContextManager {

	private static final String EXTENSION_POINT_ID_CONTEXT = HelpPlugin.PLUGIN_ID + ".contexts"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_CONTEXT_PROVIDER = "contextProvider"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_CLASS = "class"; //$NON-NLS-1$
	
	private Map providersByPluginId;
	private List globalProviders;
	
	/*
	 * Returns the Context for the given id and locale.
	 */
	public IContext getContext(String contextId, String locale) {
		if (HelpPlugin.DEBUG_CONTEXT  && contextId != null) {
			System.out.println("ContextManager.getContext(\"" + contextId + "\")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// ask the providers
		int index = contextId.lastIndexOf('.');
		if (index != -1) {
			String pluginId = contextId.substring(0, index);
			Iterator iter = getContextProviders(pluginId).iterator();
			while (iter.hasNext()) {
				AbstractContextProvider provider = (AbstractContextProvider)iter.next();
				try {
					IContext context = provider.getContext(contextId, locale);
					if (context != null) {
						if (HelpPlugin.DEBUG_CONTEXT) {
							System.out.println("ContextManager.getContext found context, description = \"" + context.getText() + '"'); //$NON-NLS-1$
						}
						return new Context(context, contextId);
					}
				}
				catch (Throwable t) {
					// log and skip
					String msg = "Error querying context provider (" + provider.getClass().getName() + ") with context Id: " + contextId; //$NON-NLS-1$ //$NON-NLS-2$
					HelpPlugin.logError(msg, t);
				}
			}
		}
		if (HelpPlugin.DEBUG_CONTEXT) {
			System.out.println("ContextManager.getContext - no context found"); //$NON-NLS-1$
		}
		return null;
	}
	
	/*
	 * Returns all registered context providers (potentially cached) for the
	 * given plug-in id.
	 */
	private List getContextProviders(String pluginId) {
		if (providersByPluginId == null) {
			loadContextProviders();
		}
		List list = new ArrayList();
		List forPlugin = (List)providersByPluginId.get(pluginId);
		if (forPlugin != null) {
			list.addAll(forPlugin);
		}
		list.addAll(globalProviders);
		return list;
	}
	
	/*
	 * Finds and instantiates all registered context-sensitive help providers.
	 */
	private void loadContextProviders() {
		providersByPluginId = new HashMap();
		globalProviders = new ArrayList();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_CONTEXT);
		for (int i=0;i<elements.length;++i) {
			IConfigurationElement elem = elements[i];
			if (elem.getName().equals(ELEMENT_NAME_CONTEXT_PROVIDER)) {
				try {
					AbstractContextProvider provider = (AbstractContextProvider)elem.createExecutableExtension(ATTRIBUTE_NAME_CLASS);
					String[] plugins = provider.getPlugins();
					if (plugins != null) {
						for (int j=0;j<plugins.length;++j) {
							List list = (List)providersByPluginId.get(plugins[j]);
							if (list == null) {
								list = new ArrayList();
								providersByPluginId.put(plugins[j], list);
							}
							list.add(provider);
						}
					}
					else {
						globalProviders.add(provider);
					}
				}
				catch (CoreException e) {
					// log and skip
					String msg = "Error instantiating context-sensitive help provider class \"" + elem.getAttribute(ATTRIBUTE_NAME_CLASS) + '"'; //$NON-NLS-1$
					HelpPlugin.logError(msg, e);
				}
			}
		}
	}
}
