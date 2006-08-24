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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Provides context help data from context XML files to the help system.
 */
public class ContextsFileProvider implements IContextProvider, IRegistryChangeListener {

	private static final String CONTEXTS_XP_NAME = "contexts"; //$NON-NLS-1$
	private static final String CONTEXTS_XP_FULLNAME = HelpPlugin.PLUGIN_ID + "." + CONTEXTS_XP_NAME; //$NON-NLS-1$
	
	private PluginsContexts pluginsContexts = new PluginsContexts();
	private Map contextsFiles = new HashMap();

	public ContextsFileProvider() {
		createContextsFiles();
		Platform.getExtensionRegistry().addRegistryChangeListener(this,
				HelpPlugin.PLUGIN_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getContext(java.lang.Object)
	 */
	public IContext getContext(Object target) {
		if (target instanceof String) {
			return getContext((String)target);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getContextChangeMask()
	 */
	public int getContextChangeMask() {
		return NONE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
	 */
	public String getSearchExpression(Object target) {
		return null;
	}
	
	/**
	 * Finds the context, given context ID.
	 */
	public IContext getContext(String contextId) {
		if (HelpPlugin.DEBUG_CONTEXT) {
			System.out.println("ContextManager.getContext(" + contextId + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (contextId == null)
			return null;
		String plugin = contextId;
		String id = contextId;
		int dot = contextId.lastIndexOf('.');
		if (dot <= 0 || dot >= contextId.length() - 1) {
			// no dot in the middle of context ID
			return null;
		}
		plugin = contextId.substring(0, dot);
		id = contextId.substring(dot + 1);
		PluginContexts contexts = pluginsContexts.get(plugin);
		if (contexts == null) {
			contexts = loadPluginContexts(plugin);
		}
		return contexts.get(id);
	}
	
	/**
	 * Loads context.xml with context for a specified plugin, creates context
	 * nodes and adds to pluginContext map.
	 */
	public synchronized PluginContexts loadPluginContexts(String plugin) {
		PluginContexts contexts = pluginsContexts.get(plugin);
		if (contexts == null) {
			contexts = new PluginContexts();
			// read the context info from the XML contributions
			List pluginContextsFiles = (List) contextsFiles.get(plugin);
			if (pluginContextsFiles == null) {
				pluginContextsFiles = new ArrayList();
			}
			ContextsBuilder builder = new ContextsBuilder(contexts);
			builder.build(pluginContextsFiles);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public synchronized void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(
				HelpPlugin.PLUGIN_ID, CONTEXTS_XP_NAME);
		for (int i = 0; i < deltas.length; i++) {
			if (deltas[i].getKind() == IExtensionDelta.ADDED) {
				IExtension extension = deltas[i].getExtension();
				Collection affectedPlugins = createContextFile(extension);
				// reset contexts for affected plugins,
				// they will be recreated on demand
				for (Iterator it = affectedPlugins.iterator(); it.hasNext();) {
					String pluginId = (String) it.next();
					pluginsContexts.remove(pluginId);
				}
			}
		}
	}

}
