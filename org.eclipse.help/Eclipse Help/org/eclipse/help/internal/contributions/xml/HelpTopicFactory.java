package org.eclipse.help.internal.contributions.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Factory for topic contributions.
 * Each topic is also registered with the specified view,
 * as topics are created for a view only.
 */
public class HelpTopicFactory {
	protected static final HelpTopicFactory instance = new HelpTopicFactory();
	protected Map topicPool = new HashMap();
	protected static InfoView view;

	/**
	 * HelpTopicFactory constructor comment.
	 */
	protected HelpTopicFactory() {
		super();
	}
	/**
	 */
	public static Topic createTopic(Topic topicNode) {
		Topic topic = new HelpTopicRef(topicNode);
		if (view != null)
			 ((HelpInfoView) view).registerTopic(topic);
		return topic;
	}
	/**
	 */
	public static HelpTopicFactory getFactory() {
		return instance;
	}
	/**
	 */
	public Topic getTopic(Topic topicNode) {
		Topic topic = (Topic) topicPool.get(topicNode);
		if (topic == null) {
			topic = new HelpTopicRef(topicNode);
			topicPool.put(topicNode, topic);
		}
		return topic;
	}
	/**
	 */
	public static void setView(InfoView v) {
		view = v;
	}
	/**
	 */
	public static void setView(HelpInfoView v) {
		view = v;
	}
}
