/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.IContext;
/**
 * Maintains the list of contexts
 * and performs look-ups.
 */
public class ContextManager {
	public static final String CONTEXTS_EXTENSION = "org.eclipse.help.contexts";
	/**
	 * Map of Contexts, indexed by each locale+plugin 
	 */
	Map pluginsContexts = new HashMap(/*of Map of Context indexed by plugin*/
	);
	/**
	 * Map of of List ContextsFile index by locale+plugin
	 */
	Map contextsFiles = new HashMap(/* of List ContextsFile index by plugin */
	);
	/**
	 * HelpContextManager constructor.
	 */
	public ContextManager() {
		super();
		createContextsFiles(Locale.getDefault().toString());
	}
	/**
	 * Finds the context, given context ID.
	 */
	public IContext getContext(String contextId, String locale) {
		if (contextId == null)
			return null;
		String plugin = contextId;
		String id = contextId;
		int dot = contextId.lastIndexOf('.');
		if (dot <= 0 || dot >= contextId.length() - 1) {
			// no dot in the middle of context ID
			return (IContext) null;
		}
		plugin = contextId.substring(0, dot);
		id = contextId.substring(dot + 1);
		Map contexts = (Map) pluginsContexts.get(locale+plugin);
		if (contexts == null) {
			contexts = loadPluginContexts(plugin, locale);
		}
		return (IContext) contexts.get(id);
	}
	/**
	 * Loads context.xml with context for a specified plugin,
	 * creates context nodes and adds to pluginContext map.
	 */
	private synchronized Map loadPluginContexts(String plugin, String locale) {
		Map contexts = (Map) pluginsContexts.get(locale+plugin);
		if (contexts == null) {
			// read the context info from the XML contributions
			List pluginContextsFiles = (List) contextsFiles.get(locale+plugin);
			if (pluginContextsFiles == null) {
				pluginContextsFiles = new ArrayList();
			}
			ContextsBuilder builder = new ContextsBuilder();
			builder.build(pluginContextsFiles);
			contexts = builder.getBuiltContexts();
			pluginsContexts.put(locale+plugin, contexts);
		}
		return contexts;
	}
	/**
	 * Creates a list of context files. 
	 */
	private void createContextsFiles(String locale) {
		// read extension point and retrieve all context contributions
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint(CONTEXTS_EXTENSION);
		if (xpt == null)
			return; // no contributions...
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			String definingPlugin =
				extensions[i].getDeclaringPluginDescriptor().getUniqueIdentifier();
			IConfigurationElement[] contextContributions =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < contextContributions.length; j++) {
				if ("contexts".equals(contextContributions[j].getName())) {
					String plugin = contextContributions[j].getAttribute("plugin");
					if (plugin == null || "".equals(plugin))
						plugin = definingPlugin;
					String fileName = contextContributions[j].getAttribute("file");
					// in v1 file attribute was called name
					if (fileName == null)
						fileName = contextContributions[j].getAttribute("name");
					List pluginContextsFiles = (List) contextsFiles.get(locale+plugin);
					if (pluginContextsFiles == null) {
						pluginContextsFiles = new ArrayList();
						contextsFiles.put(locale+plugin, pluginContextsFiles);
					}
					pluginContextsFiles.add(new ContextsFile(definingPlugin, fileName, plugin, locale));
				}
			}
		}
	}
	/**
	 * Registers context in the manager.
	 * @return context ID
	 */
	public void addContext(String contextId, IContext context, String locale) {
		if (contextId == null)
			return;
		if (getContext(contextId, locale) != null)
			return;
		String plugin = contextId;
		String id = contextId;
		int dot = contextId.lastIndexOf('.');
		if (dot != -1) {
			plugin = contextId.substring(0, dot);
			id = contextId.substring(dot + 1);
		}
		Map contexts = (Map) pluginsContexts.get(locale+plugin);
		if (contexts == null) {
			contexts = new HashMap();
			pluginsContexts.put(locale+plugin, contexts);
		}
		contexts.put(id, context);
	}
}