/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.io.*;
import java.util.*;
import org.eclipse.help.internal.util.ResourceLocator;
import org.xml.sax.SAXException;

public class TopicsFile {

	protected Topics topics;
	protected boolean topicsParsed = false;

	protected String plugin;
	protected String href;
	protected boolean toc;


	// used for fast access to anchors
	protected Map anchors;

	/**
	 * Topics File Contstructor
	 */
	protected TopicsFile(String plugin, String href, boolean isTOC) {
		this.plugin = plugin;
		this.href = href;
		this.toc = isTOC;
	}

	/**
	 * Gets the href
	 * @return Returns a String
	 */
	protected String getHref() {
		return href;
	}

	/**
	 * Gets the pluginID
	 * @return Returns a String
	 */
	public String getPluginID() {
		return plugin;
	}

	protected InputStream getInputStream() {
		InputStream stream = null;
		try {
			if (plugin != null)
				stream = ResourceLocator.openFromPlugin(plugin, href);
			else
				stream = new FileInputStream(href);
		} catch (IOException e) {
		}
		return stream;
	}

	/**
	 * Parses file and gets the topics
	 * @return Returns a Topics
	 */
	public Topics getTopics() {
		return topics;
	}

	/**
	 * Sets the topics on this file. It should happen during parsin
	 */
	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	/**
	 * Registers a new anchor.
	 */
	public void addAnchor(Anchor a) {
		if (anchors == null)
			anchors = new HashMap();

		anchors.put(a.getID(), a);
	}

	/** 
	 * Returns anchor by id
	 */
	public Anchor getAnchor(String id) {
		if (anchors == null || anchors.get(id) == null)
			return null;
		else
			return (Anchor) anchors.get(id);
	}

	/**
	 * Builds the topics file if needed
	 */
	public void build(NavigationBuilder builder) {
		builder.buildTopicsFile(this);
	}


	/**
	 * Used by debugger
	 */
	public String toString() {
		return plugin + "/" + href;
	}
	/**
	 * Checks if this file specifies a TOC.
	 * @return Returns a boolean
	 */
	public boolean isTOC() {
		return toc;
	}

}