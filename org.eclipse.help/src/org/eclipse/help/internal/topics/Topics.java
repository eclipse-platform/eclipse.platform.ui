/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.net.URLEncoder;
import java.util.*;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.internal.util.TString;
import org.xml.sax.*;
import org.eclipse.help.*;
/** 
 * Root of navigation TopicsFile
 * Can be linked with other Topics objects.
 */
public class Topics extends NavigationElement implements ITopic {
	private final static String defaultSplash =
		"/org.eclipse.help/" + Resources.getString("splash_location");
	private String link_to;
	private String href;
	private String label;
	private TopicsFile topicsFile;
	private String topicsID;
	private ITopic[] topicArray;
	
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Topics(TopicsFile topicsFile, Attributes attrs)
	{
		if (attrs == null)
			return;
		this.topicsFile = topicsFile;
		this.label = attrs.getValue("label");
		this.link_to = attrs.getValue("link_to");
		this.topicsID=attrs.getValue("topicsID");
		this.link_to = HrefUtil.normalizeHref(topicsFile.getPluginID(), link_to);
		this.topicsID=HrefUtil.normalizeHref(topicsFile.getPluginID(),topicsFile.getHref());
		this.href =
			defaultSplash
				+ "?title="
				+ URLEncoder.encode(TString.getUnicodeNumbers(label));
	}

	/**
	 * Implements abstract method.
	 */
	public void build(NavigationBuilder builder) {
		builder.buildTopics(this);
	}
	/**
	 * Returns the topics file. 
	 * Returns null when the topic is read from a temp file.
	 */
	public TopicsFile getTopicsFile() {
		return topicsFile;
	}

	/**
	 * Gets the link_to
	 * @return Returns a String
	 */
	protected String getLink_to() {
		return link_to;
	}
	/**
	 * Gets the href
	 * @return Returns a String
	 */
	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns a topic with the specified href.
	 * <br> It is possible that multiple topics have 
	 * the same href, in which case there is no guarantee 
	 * which one is returned.
	 * @param href The topic's href value.
	 */
	public ITopic getTopic(String href)
	{
		// At some point we may want to cache the topics
		// by href, but for now let's just traverse the
		// tree and find the topic.
		Stack stack = new Stack();
		ITopic[] topics = getSubtopics();
		for (int i=0; i<topics.length; i++)
			stack.push(topics[i]);
		
		while(!stack.isEmpty())
		{
			ITopic topic = (ITopic)stack.pop();
			if (topic.getHref().equals(href))
				return topic;
		}
		return null;
	}
			
	/**
	 * Note: assumes the topics have been built....
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
	
	/**
	 * Used by debugger
	 */
	public String toString() {
		return topicsID != null ? topicsID : super.toString();
	}
	/**
	 * Gets the topicsID.
	 * @return Returns a String
	 */
	public String getTopicsID() {
		return topicsID;
	}
}