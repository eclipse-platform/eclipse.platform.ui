package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.IContext;
import org.eclipse.help.internal.util.*;
/**
 * Maintains the list of contexts
 * and performs look-ups.
 */
public class ContextManager {
	public static final String CONTEXT_EXTENSION = "org.eclipse.help.contexts";
	/**
	 * Contexts, indexed by each plugin 
	 */
	Map pluginsContexts = new HashMap(/*of Map of Context indexed by plugin*/
	);
	/**
	 * Context contributors
	 */
	Map contextContributors =
		new HashMap(/* of List of Contributors index by plugin */
	);
	/**
	 * HelpContextManager constructor.
	 */
	public ContextManager() {
		super();
		createContextContributors();
	}
	/**
	 * Finds the context, given context ID.
	 */
	public IContext getContext(String contextId) {
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
		Map contexts = (Map) pluginsContexts.get(plugin);
		if (contexts == null) {
			// parse the xml context contribution files and load the context
			// defintion (NOTE: the side-effect is that all the contexts defined
			// by this plugin get loaded)
			contexts = loadContext(plugin);
		}
		return (IContext) contexts.get(id);
	}
	// NL enables a description string. 
	private String getNLdescription(String pluginID, String description) {
		if (description == null)
			return description;
		// if description starts with %, need to translate.
		if (description.indexOf('%') == 0) {
			// strip off the leading %
			description = description.substring(1);
			// now translate
			description = ContextResources.getPluginString(pluginID, description);
		}
		return description;
	}
	/**
	 * Loads context.xml with context for a specified plugin,
	 * creates context nodes and adds to pluginContext map.
	 */
	private synchronized Map loadContext(String plugin) {
		Map contexts = (Map) pluginsContexts.get(plugin);
		if (contexts == null) {
			contexts = new HashMap();
			pluginsContexts.put(plugin, contexts);
			// read the context info from the XML contributions
			List contributors = (List) contextContributors.get(plugin);
			if (contributors == null) {
				// log failure.
				String msg = Resources.getString("E011", plugin);
				Logger.logInfo(msg);
				return contexts;
			}
			// iterate over all contexts contributors
			// and add all the contexts defined for this plugin
			for (Iterator contextContributors = contributors.iterator();
				contextContributors.hasNext();
				) {
				ContextContributor contextContributor =
					(ContextContributor) contextContributors.next();
				IContextContributionNode contrib = contextContributor.getContribution();
				// contrib could be null if there was an error parsing the manifest file!
				if (contrib == null) {
					// go to the next context file.
					continue;
				}
				for (Iterator contextIterator = contrib.getChildren().iterator();
					contextIterator.hasNext();
					) {
					ContextContribution context = (ContextContribution) contextIterator.next();
					// nl description handling
					String description = context.getText();
					// NOTE: a context can be defined in another plugin, so we'd better
					//       make sure we use the contributor plugin for properties files
					//description = getNLdescription(plugin, description);
					description =
						getNLdescription(
							context.getContributor().getPlugin().getUniqueIdentifier(),
							description);
					context.setText(description);
					// end of nl description handling
					// Set the plugin ID, so the context knows its full ID
					context.setPluginID(plugin);
					// Check if context already exists for this ID,
					// merge them if id does, or add this context
					// to the context map if not.
					ContextContribution existingContext =
						(ContextContribution) contexts.get(context.getShortId());
					if (existingContext != null) {
						existingContext.merge(context);
					} else {
						contexts.put(context.getShortId(), context);
					}
				}
			}
		}
		return contexts;
	}
	/**
	 * Creates the list of context contributors 
	 */
	private void createContextContributors() {
		// read extension point and retrieve all context contributions
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint(CONTEXT_EXTENSION);
		if (xpt == null)
			return; // no contributions...
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IPluginDescriptor plugin = extensions[i].getDeclaringPluginDescriptor();
			IConfigurationElement[] contextContributions =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < contextContributions.length; j++) {
				if (ContextContributor
					.CONTEXTS_ELEM
					.equals(contextContributions[j].getName())) {
					ContextContributor contributor =
						new ContextContributor(plugin, contextContributions[j]);
					// add this contributors to the map of contributors
					// and index it by the plugin id specified.
					// Use the current plugin if none is specified
					String contextPlugin =
						contextContributions[j].getAttribute(ContextContributor.PLUGIN_ATTR);
					if (contextPlugin == null || contextPlugin.length() == 0)
						contextPlugin = plugin.getUniqueIdentifier();
					List contributors = (List) contextContributors.get(contextPlugin);
					if (contributors == null) {
						contributors = new ArrayList();
						contextContributors.put(contextPlugin, contributors);
					}
					contributors.add(contributor);
				}
			}
		}
	}
	/**
	 * Registers context in the manager.
	 * @return context ID
	 */
	public void addContext(String contextId, IContext context) {
		if (contextId == null)
			return;
		String plugin = contextId;
		String id = contextId;
		int dot = contextId.lastIndexOf('.');
		if (dot != -1) {
			plugin = contextId.substring(0, dot);
			id = contextId.substring(dot + 1);
		}
		Map contexts = (Map) pluginsContexts.get(plugin);
		if (contexts == null) {
			// parse the xml context contribution files and load the context
			// defintion (NOTE: the side-effect is that all the contexts defined
			// by this plugin get loaded)
			contexts = loadContext(plugin);
		}
		if (contexts.get(id) != null)
			return;
		contexts.put(contextId, context);
	}
}