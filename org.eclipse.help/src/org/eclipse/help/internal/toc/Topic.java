/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.util.List;

import org.eclipse.help.ITopic;
import org.xml.sax.*;
/**
 * Topic.  Visible navigation element.
 * Labeled, contains linik to a document.
 * Can also act as a container for other documents.
 */
class Topic extends TocNode implements ITopic {
	private String href;
	private String label;
	private ITopic[] topicArray;
	/**
	 * Constructor.  
	 */
	protected Topic(TocFile tocFile, Attributes attrs) throws SAXException {
		if (attrs == null)
			return;
		href = attrs.getValue("href");
		if (href != null && href.length() > 0)
			href = HrefUtil.normalizeHref(tocFile.getPluginID(), href);
		label = attrs.getValue("label");
	}
	/**
	 * Implements abstract method.
	 */
	public final void build(TocBuilder builder) {
		builder.buildTopic(this);
	}
	public String getHref() {
		return href;
	}
	public String getLabel() {
		return label;
	}
	/**
	 * This public method is to be used after the build of TOCs
	 * is finished.
	 * With assumption that TOC model is not modifiable
	 * after the build, this method caches subtopics in an array
	 * and releases objects used only during build.
	 * @return ITopic list
	 */
	public ITopic[] getSubtopics() {
		if (topicArray == null) {
			List topics = getChildTopics();
			// create and cache array of children (Topics only)
			topicArray = new ITopic[topics.size()];
			topics.toArray(topicArray);
			// for memory foot print, release list of child
			// and parent nodes.
			children = null;
			parents = null;
		}
		return topicArray;
	}

	void setLabel(String label) {
		this.label = label;
	}

	void setHref(String href) {
		this.href = href;
	}
}