package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.xml.sax.*;
import org.eclipse.help.internal.contributors.TopicContributor;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.util.*;

/**
 * Default implementation for a topic contribution
 */
public class HelpTopicRef extends HelpTopic implements TopicRef {
	protected Topic topic;
	protected String label;
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
	public void setLabel(String label) {
		this.label=label;
	}
	public String getLabel() {
		if(label!=null)
			return label;
		return ((HelpTopic) topic).getLabel();
	}
	public Topic getTopic() {
		return topic;
	}
}
