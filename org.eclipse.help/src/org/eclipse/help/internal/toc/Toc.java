/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.net.URLEncoder;
import java.util.*;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.internal.util.TString;
import org.xml.sax.*;
import org.eclipse.help.*;
/** 
 * Root of navigation TocFile
 * Can be linked with other Toc objects.
 */
public class Toc extends TocNode implements ITopic {
	private final static String defaultSplash =
		"/org.eclipse.help/" + Resources.getString("splash_location");
	private String link_to;
	private String href;
	private String label;
	private TocFile tocFile;
	private String tocID;
	private ITopic[] topicArray;
	
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Toc(TocFile tocFile, Attributes attrs)
	{
		if (attrs == null)
			return;
		this.tocFile = tocFile;
		this.label = attrs.getValue("label");
		this.link_to = attrs.getValue("link_to");
		this.tocID=attrs.getValue("tocID");
		this.link_to = HrefUtil.normalizeHref(tocFile.getPluginID(), link_to);
		this.tocID=HrefUtil.normalizeHref(tocFile.getPluginID(),tocFile.getHref());
		this.href =
			defaultSplash
				+ "?title="
				+ URLEncoder.encode(TString.getUnicodeNumbers(label));
	}

	/**
	 * Implements abstract method.
	 */
	public void build(TocBuilder builder) {
		builder.buildToc(this);
	}
	/**
	 * Returns the toc file. 
	 * Returns null when the topic is read from a temp file.
	 */
	public TocFile getTocFile() {
		return tocFile;
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
	 * <br> It is possible that multiple tocs have 
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
	 * Note: assumes the toc have been built....
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
		return tocID != null ? tocID : super.toString();
	}
	/**
	 * Gets the topicsID.
	 * @return Returns a String
	 */
	public String getTocID() {
		return tocID;
	}
}