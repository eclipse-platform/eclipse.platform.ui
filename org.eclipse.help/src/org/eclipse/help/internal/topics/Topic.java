/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
package org.eclipse.help.internal.topics;

import java.util.List;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.topics.ITopic;
import org.xml.sax.*;
/**
 * Topic.  Visible navigation element.
 * Labeled, contains linik to a document.
 * Can also act as a container for other documents.
 */
class Topic extends NavigationElement implements ITopic {
	private String href;
	private String label;
	private ITopic[] topicArray;
	
	/**
	 * Contstructor.  
	 */
	protected Topic(TopicsFile topicsFile, Attributes attrs) throws SAXException {
		if (attrs == null)
			return;
		href = attrs.getValue("href");
		if (href != null && topicsFile != null)
			href = HrefUtil.normalizeHref(topicsFile.getPluginID(), href);
		label = attrs.getValue("label");
	}

	/**
	 * Implements abstract method.
	 */
	public void build(NavigationBuilder builder) {
		builder.buildTopic(this);
	}

	/////////////////
	//  ITopic
	/////////////////
	
	public String getHref() {
		return href;
	}
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return ITopic list
	 */
	public ITopic[] getSubtopics() {
		if (topicArray == null)
		{
			List topics = getChildTopics();
			topicArray = new ITopic[topics.size()];
			topics.toArray(topicArray);
		}
		return topicArray;
	}
}