package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.contributions1_0.Contribution;
import org.eclipse.help.internal.contributors.xml1_0.ContributorFactory;
import org.eclipse.help.internal.contributors1_0.Contributor;
import org.eclipse.help.internal.util.*;
/**
 * Maintains the list of contexts
 * and performs look-ups.
 */
public class ContextManager implements IContextManager {
	/** contexts are indexed by each plugin */
	Map pluginsContexts = new HashMap(/*of Map of Context indexed by plugin*/
	);
	/** Context contributors */
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
		return (IContext) contexts.get(id);
	}
	/**
	 * Finds the context to display (text and related topics), given
	 * a an ordered list of (nested) context objects
	 */
	public String getDescription(Object[] contexts) {
		if (contexts == null)
			return null;
		// Scan the list and return the description from the first context containing data
		for (int i = 0; i < contexts.length; i++) {
			String description = getDescription(contexts[i]);
			if (description != null)
				return description;
		}
		// worst case scenario. Could not find description from any context object. 
		return null;
	}
	/**
	 * Finds the context description to display, given
	 * a an ordered list of (nested) context objects.
	 */
	private String getDescription(Object context) {
		if (context instanceof IContext)
			return ((IContext) context).getText();
		if (!(context instanceof String))
			return null;
		IContext contextNode = getContext((String) context);
		if (contextNode != null)
			return contextNode.getText();
		else			// no context defined for this object. return null to enable 
			// lookup of description from other Contexts. 
			return null;
	}
	/**
	 * Finds the context related topics to display, given
	 * a an ordered list of (nested) context objects.
	 * Finds rest of the topics not returned by
	 * getRelatedTopics(Object[]).  May take long to execute.
	 */
	public IHelpTopic[] getMoreRelatedTopics(Object[] contexts) {
		if (contexts == null)
			return null;
		ArrayList relatedTopics = new ArrayList();
		// Skip the first one the list and return the related topics from the other contexts
		for (int i = 1; i < contexts.length; i++) {
			IHelpTopic[] topics = getRelatedTopics(contexts[i]);
			if (topics != null) {
				for (int j = 0; j < topics.length; j++)
					relatedTopics.add(topics[j]);
			}
		}
		IHelpTopic[] moreRelated = new IHelpTopic[relatedTopics.size()];
		return (IHelpTopic[]) relatedTopics.toArray(moreRelated);
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
	 * Finds the context to display (text and related topics), given
	 * a an ordered list of (nested) context objects
	 */
	public IHelpTopic[] getRelatedTopics(Object[] contexts) {
		if (contexts == null)
			return null;
		// return the related topics from the first context
		if (contexts.length >= 1) {
			IHelpTopic[] topics = getRelatedTopics(contexts[0]);
			return topics;
		}
		// worst case scenario. Could not find related Topics in any context object. 
		return null;
	}
	/**
	 * Finds the context related topics to display, given
	 * a an ordered list of (nested) context objects
	 * Finds only some of the topics.
	 */
	private IHelpTopic[] getRelatedTopics(Object context) {
		if (context instanceof IContext)
			return ((IContext) context).getRelatedTopics();
		if (!(context instanceof String))
			return null;
		IContext contextNode = getContext((String) context);
		if (contextNode != null)
			return contextNode.getRelatedTopics();
		else			// no context defined for this object. return null to enable 
			// lookup of description from other Contexts. 
			return null;
	}
	/**
	 * Finds the context to display (text and related topics), given
	 * a an ordered list of (nested) context objects
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
			// iterator over all contexts contributors and add all the contexts
			// defined for this plugin
			// NOTE: if performance is a problem, this can be improved by a better
			//       use of the SAX parser
			for (Iterator contextContributors = contributors.iterator();
				contextContributors.hasNext();
				) {
				Contributor contextContributor = (Contributor) contextContributors.next();
				Contribution contrib = contextContributor.getContribution();
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
						contexts.put(contextNode.getID(), contextNode);
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
				Contributor contributor =
					ContributorFactory.getFactory().createContributor(
						plugin,
						contextContributions[j]);
				if (contributor != null) {
					// add this contributors to the map of contributors
					// and index it by the plugin id specified.
					// Use the current plugin if none is specified
					String contextPlugin =
						contextContributions[j].getAttribute(Contributor.PLUGIN_ATTR);
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
}