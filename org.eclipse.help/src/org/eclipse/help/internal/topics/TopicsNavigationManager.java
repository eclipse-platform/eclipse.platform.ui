/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import com.ibm.jvm.format.Util;
import java.io.File;
import java.util.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.topics.ITopics;
import org.eclipse.core.runtime.*;
import java.net.*;
/**
 * Manages the navigation model. It generates it and it reads it back
 * and instantiates the model for future rendering.
 * There is a model (notifier) for each <views> node.
 */
public class TopicsNavigationManager {
	private Map topicsNavigationModels =
		new HashMap(/* of href to TopicsNavigationModel */
	);
	// Ordered List of all infosets available;
	private List topicsHrefs;
	// Map String href to String label that keeps track of all the topics_ available
	private TopicsMap topicsMap = null;
	/**
	 * HelpNavigationManager constructor.
	 */
	public TopicsNavigationManager() {
		super();
		try {
			TopicsContributorsManager tcmgr = HelpSystem.getTopicsContributorsManager();
			if (tcmgr.hasNewContributors()) {
				buildContributions(tcmgr.getContributedTopicsFiles());
				// will initialize infosetsIds, as byproduct
				tcmgr.versionContributors();
			} else {
				// read persited Topics
				getTopicsHrefs();
			}
			// 1.0 navigation support
			HelpSystem.getNavigationManager();
			// eo 1.0 nava support
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	
	
	private void buildContributions(Collection contributedTopicsFiles) {
		try {
			// Keep track of all the topics available
			topicsMap = new TopicsMap();
			NavigationBuilder builder = new NavigationBuilder();
			builder.build(contributedTopicsFiles);
			Collection builtTopics = builder.getBuiltTopics();
			for (Iterator it = builtTopics.iterator(); it.hasNext();) {
				ITopics topics = (ITopics) it.next();
				topicsNavigationModels.put(topics.getHref(), topics);
				// generate navigation file for each topics
				File navOutFile = new File(HrefUtil.getStateLocation(topics.getHref()));
				File navOutDir = navOutFile.getParentFile();
				if (!navOutDir.exists()) {
					navOutDir.mkdirs();
				}
				new NavigationWriter(topics, navOutFile).generate();
				topicsMap.put(topics.getHref(), topics.getLabel());
			}
			// Save a file with all the infosets ids and labels
			topicsMap.save();
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}

	/**
	 * Returns the navigation model for specified topics href
	 */
	public ITopics getTopics(String href) {
		if (href == null || href.equals(""))
			return null;
			
		ITopics topics = (ITopics) topicsNavigationModels.get(href);
		if (topics == null && topicsMap.containsKey(href)) 
			topics = loadSavedTopics(href);
		
		if (topics != null)
			return topics;
		// 1.0 nav support
		else
			return HelpSystem.getNavigationManager().getTopics(href);
		// eo 1.0 nav support
	}
	/**
	 * @return List of Topics hrefs available, not including
	 * ones that do not have navigation
	 * (i.e. require not met)
	 */
	public List getTopicsHrefs() {
		if (topicsHrefs != null)
			return topicsHrefs;
		// first call to this method, prepare topicsHrefs and mapping to labels
		if (topicsMap == null) {
			topicsMap = new TopicsMap();
			topicsMap.restore();
		}
		topicsHrefs = new ArrayList(topicsMap.size());
		// obtain unordered hrefs
		List unorderedHrefs = new ArrayList(topicsMap.size());
		unorderedHrefs.addAll(topicsMap.keySet());
		// 1.0 nav support
		unorderedHrefs.addAll(HelpSystem.getNavigationManager().getTopicsHrefs());
		// eo 1.0 nav support
		// Now create ordered list, as specified in product.ini
		List preferedOrder = getPreferedInfosetsOrder();
		// add all infosets that have order specified
		if (preferedOrder != null) {
			for (Iterator it = preferedOrder.iterator(); it.hasNext();) {
				String infosetHref = (String) it.next();
				if (unorderedHrefs.contains(infosetHref))
					topicsHrefs.add(infosetHref);
				unorderedHrefs.remove(infosetHref);
				// 1.0 nav support
				for (Iterator it2 = unorderedHrefs.iterator(); it2.hasNext();) {
					String infosetDDView = (String) it2.next();
					if (infosetDDView.startsWith(infosetHref + "..")) {
						topicsHrefs.add(infosetDDView);
						unorderedHrefs.remove(infosetDDView);
						// iterator is dirty, start again
						it2 = unorderedHrefs.iterator();
						continue;
					}
				}
				// oe 1.0 nav support
			}
		}
		// add the rest of infosets
		topicsHrefs.addAll(unorderedHrefs);
		return topicsHrefs;
	}
	/**
	 * Returns the label for Topics.
	 * This method uses the label from the topics map file
	 * so that the navigation file does not need to be
	 * read in memory
	 */
	public String getTopicsLabel(String href) {
		// 1.0 nav support
		if (!topicsMap.containsKey(href))
			return HelpSystem.getNavigationManager().getTopicsLabel(href);
		// eo 1.0 nav support
		return (String) topicsMap.get(href);
	}

	/**
	 * Loads a saved topics file with the specified original href
	 */
	private ITopics loadSavedTopics(String href)
	{
		// Need to build from a generated topics file
		String realHref = HrefUtil.getStateLocation(href);
		TopicsFile topicsFile = new TopicsFile(null, realHref);
		NavigationBuilder builder = new NavigationBuilder();
		builder.buildTopicsFile(topicsFile);
		ITopics topics = topicsFile.getTopics();
		topicsNavigationModels.put(href, topics);
		
		return topics;
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
}