package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
			// iterator over all contexts contributors and add all the contexts
			// defined for this plugin
			// NOTE: if performance is a problem, this can be improved by a better
			//       use of the SAX parser
			for (Iterator contextContributors = contributors.iterator();
				contextContributors.hasNext();
				) {
				ContextContributor contextContributor =
					(ContextContributor) contextContributors.next();
				IContextContributionNode contrib = contextContributor.getContribution();
				// contrib could be null if there was an error parsing the manifest file!
				if (contrib == null);
				// do nothing here because we need to load other Context files.
				else {
					for (Iterator contextIterator = contrib.getChildren();
						contextIterator.hasNext();
						) {
						ContextContribution contextNode = (ContextContribution) contextIterator.next();
						String description = contextNode.getDescription();
						// NOTE: a context can be defined in another plugin, so we'd better
						//       make sure we use the contributor plugin for properties files
						//description = getNLdescription(plugin, description);
						description =
							getNLdescription(
								contextNode.getContributor().getPlugin().getUniqueIdentifier(),
								description);
						contextNode.setDescription(description);
						// Set the plugin ID, so the context knows its full ID
						contextNode.setPluginID(plugin);
						contexts.put(contextNode.getShortId(), contextNode);
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