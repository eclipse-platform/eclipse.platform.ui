/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.xml1_0.HelpInfoView;
import org.eclipse.help.internal.util.*;
/**
 * Manages the navigation model. It generates it and it reads it back
 * and instantiates the model for future rendering.
 * There is a model (notifier) for each <views> node.
 */
public class TopicsNavigationManager {
	private Map idToTopics =
		new HashMap(/* of ID to Topics */
	);
	// Ordered List of all infosets available;
	private List topicsIDs;
	/**
	 * HelpNavigationManager constructor.
	 */
	public TopicsNavigationManager() {
		super();
		try {
			build();
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	private void build() {
		try {
			Collection contributedTopicsFiles = getContributedTopicsFiles();
			NavigationBuilder builder = new NavigationBuilder();
			builder.build(contributedTopicsFiles);
			Collection builtTopics = builder.getBuiltTopics();
			for (Iterator it = builtTopics.iterator(); it.hasNext();) {
				Topics topics = (Topics) it.next();
				idToTopics.put(topics.getTopicsID(), topics);
			}
			// 1.0 navigation support
			Collection builtTopics10 = HelpSystem.getNavigationManager().getTopicsIDs();
			for (Iterator it = builtTopics10.iterator(); it.hasNext();) {
				String id= (String) it.next();
				idToTopics.put(id, HelpSystem.getNavigationManager().getTopics(id));
			}
			// eo 1.0 nava support
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	/**
	 * Returns the navigation model for specified topics ID
	 */
	public ITopic getTopics(String id) {
		if (id == null || id.equals(""))
			return null;
		return (ITopic) idToTopics.get(id);
	}
	/**
	 * @return List of Topics hrefs available, not including
	 * ones that do not have navigation
	 * (i.e. require not met)
	 */
	public List getTopicsIDs() {
		if (topicsIDs != null)
			return topicsIDs;
		topicsIDs = new ArrayList(idToTopics.size());
		// obtain unordered hrefs
		List unorderedIDs = new ArrayList(idToTopics.size());
		unorderedIDs.addAll(idToTopics.keySet());
		// Now create ordered list, as specified in product.ini
		List preferedOrder = getPreferedInfosetsOrder();
		// add all infosets that have order specified
		if (preferedOrder != null) {
			for (Iterator it = preferedOrder.iterator(); it.hasNext();) {
				String infosetID = (String) it.next();
				if (unorderedIDs.contains(infosetID))
					topicsIDs.add(infosetID);
				unorderedIDs.remove(infosetID);
				// 1.0 nav support
				for (Iterator it2 = unorderedIDs.iterator(); it2.hasNext();) {
					String infosetDDView = (String) it2.next();
					if (infosetDDView.startsWith(infosetID + "..")) {
						topicsIDs.add(infosetDDView);
						unorderedIDs.remove(infosetDDView);
						// iterator is dirty, start again
						it2 = unorderedIDs.iterator();
						continue;
					}
				}
				// oe 1.0 nav support
			}
		}
		// add the rest of infosets
		topicsIDs.addAll(unorderedIDs);
		return topicsIDs;
	}
	/**
	 * Returns the label for Topics.
	 * This method uses the label from the topics map file
	 * so that the navigation file does not need to be
	 * read in memory
	 */
	public String getTopicsLabel(String href) {
		Object topics = idToTopics.get(href);
		if (topics != null)
			return ((ITopic) topics).getLabel();
		return null;
	}
	/**
	 * Reads product.ini to determine infosets ordering.
	 * It works in current drivers, but will not
	 * if location/name of product.ini change
	 */
	private List getPreferedInfosetsOrder() {
		List infosets = new ArrayList();
		try {
			Plugin p = Platform.getPlugin("org.eclipse.sdk");
			if (p == null)
				return infosets;
			URL pURL = p.getDescriptor().getInstallURL();
			URL productIniURL = new URL(pURL, "product.ini");
			Properties prop = new Properties();
			prop.load(productIniURL.openStream());
			String infosetsList = prop.getProperty("baseInfosets");
			if (infosetsList != null) {
				StringTokenizer suggestdOrderedInfosets =
					new StringTokenizer(infosetsList, " ;,");
				while (suggestdOrderedInfosets.hasMoreElements()) {
					String infoset = (String) suggestdOrderedInfosets.nextElement();
					infosets.add(infoset);
				}
			}
		} catch (Exception e) {
			Logger.logWarning(Resources.getString("W001"));
		}
		return infosets;
	}
	/**
	* Returns a collection of TopicsFile that were not processed.
	*/
	protected Collection getContributedTopicsFiles() {
		Collection contributedTopicsFiles = new ArrayList();
		// find extension point
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint("org.eclipse.help", "topics");
		if (xpt == null)
			return contributedTopicsFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			// add to TopicFiles declared in this extension
			IConfigurationElement[] configElements =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals("topics")) {
					String pluginId =
						configElements[j]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String href = configElements[j].getAttribute("file");
					boolean isTOC = "toc".equals(configElements[j].getAttribute("type"));
					if (href != null)
						contributedTopicsFiles.add(new TopicsFile(pluginId, href, isTOC));
				}
		}
		return contributedTopicsFiles;
	}
}