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
		if (href != null && !"".equals(href))
			href = HrefUtil.normalizeHref(tocFile.getPluginID(), href);
		label = attrs.getValue("label");
	}
	/**
	 * Implements abstract method.
	 */
	public void build(TocBuilder builder) {
		builder.buildTopic(this);
	}
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
		if (topicArray == null) {
			List topics = getChildTopics();
			topicArray = new ITopic[topics.size()];
			topics.toArray(topicArray);
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