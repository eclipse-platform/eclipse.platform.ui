package org.eclipse.help.internal.contributions.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.xml.sax.*;
import org.eclipse.help.internal.contributors.TopicContributor;
import org.eclipse.help.internal.contributions.*;

/**
 * Default implementation for a topic contribution
 */
public class HelpTopicRef extends HelpTopic implements TopicRef {
	protected Topic topic;
	public HelpTopicRef(Topic topic) {
		super(null);
		this.topic = topic;
		id = topic.getID();
	}
	public String getHref() {
		return topic.getHref();
	}
	public String getID() {
		return id;
	}
	public String getLabel() {
		return topic.getLabel();
	}
	public String getRawLabel() {
		return ((HelpTopic) topic).getRawLabel();
	}
	public Topic getTopic() {
		return topic;
	}
}
