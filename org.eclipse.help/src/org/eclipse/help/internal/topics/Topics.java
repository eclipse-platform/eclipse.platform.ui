/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.util.*;
import org.eclipse.help.internal.util.Resources;
import org.eclipse.help.topics.ITopics;
import org.xml.sax.*;
import sun.java2d.pipe.NullPixelPipe;
/** 
 * Root of navigation TopicsFile
 * Can be attached to and/or include other
 * Topics objects.
 */
class Topics extends NavigationElement implements ITopics {
	protected String attach_to;
	protected String href;
	protected String label;
	protected TopicsFile topicsFile;
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Topics(TopicsFile topicsFile, Attributes attrs)
	{
		if (attrs == null)
			return;

		this.topicsFile = topicsFile;
		this.label = attrs.getValue("label");
		this.attach_to = attrs.getValue("attach_to");
		this.href = attrs.getValue("href");
		if ((this.href == null || this.href.equals("")) && topicsFile != null )
			// use the value of the topics file
			this.href = topicsFile.getHref();


		if (topicsFile != null) {
			this.attach_to = HrefUtil.normalizeHref(topicsFile.getPluginID(), attach_to);
			this.href = HrefUtil.normalizeHref(topicsFile.getPluginID(), href);
		}
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
	 * Gets the atach_to
	 * @return Returns a String
	 */
	protected String getAttach_to() {
		return attach_to;
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
	 * Used by debugger
	 */
	public String toString() {
		return href != null ? href : super.toString();
	}
}