/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.util.*;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.topics.ITopics;

/**
 * Domain notifier for the help model.
 * It holds the model (views and topic elements).
 */
public class TopicsNavigationModel {
	private Map urlToTopicMap = new HashMap();
	private ITopics topics;
	private String topicsHref = null;
	/**
	 * HelpDomainNotifier constructor comment.
	 */
	public TopicsNavigationModel(String topicsHref) {
		super();
		this.topicsHref = topicsHref;
		load();
	}
	/**
	 * HelpDomainNotifier constructor comment.
	 */
	public TopicsNavigationModel(ITopics topics) {
		super();
		this.topics = topics;
	}
	public Set getAllURLs() {
		return urlToTopicMap.keySet();
	}
	public ITopics getRootElement() {
		return topics;
	}
	/**
	 * Loads the persisted xml model
	 */
	private void load() {
		String realHref = 
			HelpPlugin
				.getDefault()
				.getStateLocation()
				.addTrailingSeparator()
				.append("nl")
				.addTrailingSeparator()
				.append(Locale.getDefault().toString())
				.addTrailingSeparator()
				.append(topicsHref).toOSString();
				
		TopicsFile topicsFile = new TopicsFile(null, realHref);
		ArrayList list = new ArrayList(1);
		NavigationBuilder builder = new NavigationBuilder();
		builder.buildTopicsFile(topicsFile);
		topics = topicsFile.getTopics();
		//PersistedNavParser persistedTopicsParser = new PersistedNavParser(topicsHref);
		//topics = persistedTopicsParser.getTopics();
	}
}