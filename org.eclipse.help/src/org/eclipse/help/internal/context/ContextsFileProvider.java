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
package org.eclipse.help.internal.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.IContext;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Provides context help data from context XML files to the help system.
 */
public class ContextsFileProvider extends AbstractContextProvider {

	private static final String CONTEXTS_XP_NAME = "contexts"; //$NON-NLS-1$
	private static final String CONTEXTS_XP_FULLNAME = HelpPlugin.PLUGIN_ID + "." + CONTEXTS_XP_NAME; //$NON-NLS-1$
	
	private Map pluginsContextsByLocale = new HashMap();
	private Map contextsFiles = new HashMap();

	public ContextsFileProvider() {
		createContextsFiles();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractContextProvider#getContext(java.lang.String, java.lang.String)
	 */
	public IContext getContext(String contextId, String locale) {
		if (contextId != null) {
			int dot = contextId.lastIndexOf('.');
			if (dot <= 0 || dot >= contextId.length() - 1) {
				// no dot in the middle of context ID
				return null;
			}
			String plugin = contextId.substring(0, dot);
			String id = contextId.substring(dot + 1);
			PluginsContexts pluginsContexts = (PluginsContexts)pluginsContextsByLocale.get(locale);
			if (pluginsContexts == null) {
				pluginsContexts = new PluginsContexts();
				pluginsContextsByLocale.put(locale, pluginsContexts);
			}
			PluginContexts contexts = pluginsContexts.get(plugin);
			if (contexts == null) {
				contexts = loadPluginContexts(plugin, pluginsContexts, locale);
			}
			return contexts.get(id);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractContextProvider#getPlugins()
	 */
	public String[] getPlugins() {
		Set plugins = new HashSet();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(CONTEXTS_XP_FULLNAME);
		for (int i=0;i<elements.length;++i) {
			IConfigurationElement element = (IConfigurationElement)elements[i];
			if ("contexts".equals(element.getName())) { //$NON-NLS-1$
				plugins.add(element.getContributor().getName());
			}
			String plugin = element.getAttribute("plugin"); //$NON-NLS-1$
			if (plugin != null) {
				plugins.add(plugin);
			}
		}
		return (String[])plugins.toArray(new String[plugins.size()]);
	}
	
	/**
	 * Loads context.xml with context for a specified plugin, creates context
	 * nodes and adds to pluginContext map.
	 */
	public synchronized PluginContexts loadPluginContexts(String plugin, PluginsContexts pluginsContexts, String locale) {
		PluginContexts contexts = pluginsContexts.get(plugin);
		if (contexts == null) {
			contexts = new PluginContexts();
			// read the context info from the XML contributions
			List pluginContextsFiles = (List)contextsFiles.get(plugin);
			if (pluginContextsFiles == null) {
				pluginContextsFiles = new ArrayList();
			}
			ContextsBuilder builder = new ContextsBuilder(contexts);
			builder.build(pluginContextsFiles, locale);
			pluginsContexts.put(plugin, contexts);
		}
		return contexts;
	}
	
	/**
	 * Creates a list of context files.
	 */
	private void createContextsFiles() {
		// read extension point and retrieve all context contributions
		IExtensionPoint xpt = Platform.getExtensionRegistry()
				.getExtensionPoint(CONTEXTS_XP_FULLNAME);
		if (xpt == null)
			return; // no contributions...
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			createContextFile(extensions[i]);
		}
	}
	
	/**
	 * @param extension
	 * @return Collection of String (plugin IDs that have new contexts
	 *         contributed)
	 */
	private Collection createContextFile(IExtension extension) {
		Collection plugins = new HashSet();
		String definingPlugin = extension.getContributor().getName();
		IConfigurationElement[] contextContributions = extension
				.getConfigurationElements();
		for (int j = 0; j < contextContributions.length; j++) {
			if ("contexts".equals(contextContributions[j].getName())) { //$NON-NLS-1$
				String plugin = contextContributions[j].getAttribute("plugin"); //$NON-NLS-1$
				if (plugin == null || "".equals(plugin)) //$NON-NLS-1$
					plugin = definingPlugin;
				String fileName = contextContributions[j].getAttribute("file"); //$NON-NLS-1$
				// in v1 file attribute was called name
				if (fileName == null)
					fileName = contextContributions[j].getAttribute("name"); //$NON-NLS-1$
				if (fileName == null) {
					String msg = "\"context\" element in extension of " //$NON-NLS-1$
							+ CONTEXTS_XP_FULLNAME
							+ ", contributed in plug-in " + definingPlugin //$NON-NLS-1$
							+ ", is missing required \"file\" attribute."; //$NON-NLS-1$
					HelpPlugin.logError(msg, null);
					continue;
				}
				List pluginContextsFiles = (List) contextsFiles.get(plugin);
				if (pluginContextsFiles == null) {
					pluginContextsFiles = new ArrayList();
					contextsFiles.put(plugin, pluginContextsFiles);
				}
				pluginContextsFiles.add(new ContextsFile(definingPlugin,
						fileName, plugin));
				plugins.add(plugin);
			}
		}
		return plugins;
	}
}
