package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.w3c.dom.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributors.xml.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.util.*;

/**
 * Maintains the list of contexts
 * and performs look-ups.
 */
public class HelpContextManager implements ContextManager {
	/** contexts are indexed by each plugin */
	Map pluginsContexts = new Hashtable(/*of Map of Context*/);
	/**
	 * HelpContextManager constructor.
	 */
	public HelpContextManager() {
		super();
	}
	private void addMap(String plugin, Element map) {
		/*
		if (map == null) return;
		
		Map pluginContexts = (Map)pluginsContexts.get(plugin);
		if (pluginContexts == null)
		{
			pluginContexts = new Hashtable();
			pluginsContexts.put(plugin, pluginContexts);
		}
		
		NodeList children = map.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element contextElement = (Element)child;
			String id = contextElement.getAttribute(ContextContributor.ID_ATTR);
			// Try merging an existing context with the same id
			// with the new one. If none exists, just add it.
			Context oldContext = (Context)pluginContexts.get(id);
			if (oldContext == null)
			{
				Context newContext = new Context(contextElement);
				pluginContexts.put(id, newContext);
			}
			else
			{
				oldContext.merge(contextElement);
			}
		}
		*/
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
	 * Get contributions of some type, like contexts, topics, actions
	 * or views from a specified plugin. If no contributors are found, or
	 * if errors are encountered, return an empty Iterator. 
	 * @return java.util.Iterator
	 * @param typeName java.lang.String
	 * @param pluginId java.lang.String
	 */
	public Iterator getContributionsOfType(String pluginId, String typeName) {
		List typedContributors = new ArrayList();

		try {
			// Get the help contributions for this plugin
			IPluginDescriptor plugin =
				Platform.getPluginRegistry().getPluginDescriptor(pluginId);
			IExtension[] extensions = plugin.getExtensions();
			IExtension helpContributions = null;
			// Loop through all the contributions and find the help ones.
			// Then, get the one specified by the typeName
			for (int i = 0; extensions != null && i < extensions.length; i++) {
				if (!ContextManager
					.CONTEXT_EXTENSION
					.equals(extensions[i].getExtensionPointUniqueIdentifier()))
					continue;

				helpContributions = extensions[i];
				IConfigurationElement[] pluginContributions =
					helpContributions.getConfigurationElements();
				for (int j = 0; j < pluginContributions.length; j++) {
					if (pluginContributions[j].getName().equals(typeName)) {
						Contributor contributor =
							ContributorFactory.getFactory().createContributor(
								plugin,
								pluginContributions[j]);
						if (contributor != null)
							typedContributors.add(contributor);
					}
				}
			}
			return typedContributors.iterator();
		} catch (Exception e) {
			// log failure.
			String msg = Resources.getString("E011", pluginId);
			Logger.logError(msg, e);
			// this puts an empty Map in the pluginsContexts HashMap, which forces
			// looking into nested Context objects.  
			return typedContributors.iterator();
		}

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

		String contextId = (String) context;
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

		IContext contextNode = (IContext) contexts.get(id);
		if (contextNode != null)
			return contextNode.getText();
		else
			// no context defined for this object. return null to enable 
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
		if(description==null)
			return description;
		// if description starts with %, need to translate.
		if (description.indexOf('%') == 0) {
			// strip off the leading %
			description = description.substring(1);
			// now translate
			description = DocResources.getPluginString(pluginID, description);
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

		// Scan the list and return the related topics from the first context containing data
		/*for (int i = 0; i < contexts.length; i++) {
			IHelpTopic[] topics = getRelatedTopics(contexts[i]);
			if (topics != null)
				return topics;
		}*/

		// return the related topics from the first context
		if(contexts.length>=1){
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

		String contextId = (String) context;
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
		IContext contextNode = (IContext) contexts.get(id);
		if (contextNode != null)
			return contextNode.getRelatedTopics();
		else
			// no context defined for this object. return null to enable 
			// lookup of description from other Contexts. 
			return null;
	}
	/**
	 * Finds the context to display (text and related topics), given
	 * a an ordered list of (nested) context objects
	 */
	private Map loadContext(String plugin) {
		Map contexts = (Map) pluginsContexts.get(plugin);
		if (contexts == null) {
			// read the context info from the XML contributions
			Iterator contextsContributors =
				getContributionsOfType(plugin, ContextContributor.CONTEXTS_ELEM);
			contexts = new HashMap();
			pluginsContexts.put(plugin, contexts);
			// iterator over all contexts contributors and add all the contexts
			// defined by this plugin
			// NOTE: if performance is a problem, this can be improved by a better
			//       use of the SAX parser
			while (contextsContributors.hasNext()) {
				Contributor contextContributor = (Contributor) contextsContributors.next();
				Contribution contrib = contextContributor.getContribution();

				// contrib could be null if there was an error parsing the manifest file!
				if (contrib == null);
				// do nothing here because we need to load other Context files.
				else {
					for (Iterator contextIterator = contrib.getChildren();
						contextIterator.hasNext();
						) {
						HelpContext contextNode = (HelpContext) contextIterator.next();
						String description = contextNode.getDescription();
						description = getNLdescription(plugin, description);
						contextNode.setDescription(description);
						contexts.put(contextNode.getID(), contextNode);
					}
				}
			}
		}
		return contexts;
	}
}
