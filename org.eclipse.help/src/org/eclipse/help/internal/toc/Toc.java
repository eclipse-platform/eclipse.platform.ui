/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;
import java.util.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.model.*;
import org.xml.sax.*;
/**
 * Root of navigation TocFile Can be linked with other Toc objects.
 */
public class Toc extends TocNode implements IToc, ITocElement {
	private static final int SIZE_UNINITIALIZED = -1;
	private String link_to;
	private String href;
	private String label;
	private TocFile tocFile;
	private ITopic[] topicArray;
	private Topic descriptionTopic;
	/**
	 * Collection of Toc
	 */
	private List childrenTocs;
	private DirectoryToc directoryToc;
	/**
	 * Map of all topics contained by this TOC by href. Description topic is not
	 * in the map.
	 */
	private Map topicMap = new HashMap();
	private int size = SIZE_UNINITIALIZED;
	/**
	 * Constructor. Used when parsing help contributions.
	 */
	protected Toc(TocFile tocFile, Attributes attrs) {
		if (attrs == null)
			return;
		this.tocFile = tocFile;
		this.label = attrs.getValue("label"); //$NON-NLS-1$
		if (label == null) {
			throw new RuntimeException("toc label==null"); //$NON-NLS-1$
		}
		this.link_to = attrs.getValue("link_to"); //$NON-NLS-1$
		this.link_to = HrefUtil.normalizeHref(tocFile.getPluginID(), link_to);
		this.href = HrefUtil.normalizeHref(tocFile.getPluginID(), tocFile
				.getHref());

		try {
			// create the description topic
			this.descriptionTopic = new Topic(tocFile, null);
			this.descriptionTopic.setLabel(this.label);
			String topic = attrs.getValue("topic"); //$NON-NLS-1$
			if (topic != null && topic.trim().length() > 0)
				this.descriptionTopic.setHref(HrefUtil.normalizeHref(tocFile
						.getPluginID(), topic));
			else
				this.descriptionTopic.setHref(""); //$NON-NLS-1$
		} catch (Exception e) {
		}

		childrenTocs = new ArrayList();
		directoryToc = new DirectoryToc(tocFile);
	}
	/**
	 * Implements abstract method.
	 */
	public void build(TocBuilder builder) {
		builder.buildToc(this);
	}
	/**
	 * Returns the toc file. Returns null when the topic is read from a temp
	 * file.
	 */
	public TocFile getTocFile() {
		return tocFile;
	}
	/**
	 * Gets the link_to
	 * 
	 * @return Returns a String
	 */
	protected String getLink_to() {
		return link_to;
	}
	/**
	 * Gets the href
	 * 
	 * @return Returns a String
	 */
	public String getHref() {
		return href;
	}
	public String getLabel() {
		return label;
	}
	/**
	 * Returns a topic with the specified href defined by this TOC. <br>
	 * If the TOC contains multiple topics with the same href only of them
	 * (arbitrarily chosen) will be returned.
	 * <p>
	 * If no topic is specified, then the TOC description topic is returned, or
	 * null if there is no description topic for the TOC.
	 * </p>
	 * 
	 * @param href
	 *            the topic's URL or null
	 * @return ITopic or null
	 */
	public ITopic getTopic(String href) {
		if (href == null || href.equals(descriptionTopic.getHref())) {
			return descriptionTopic;
		}
		return getTopicNoDescr(href);
	}
	/**
	 * Similar to ITopic getTopic(String) but does not match and return
	 * description Topic
	 * 
	 * @param href
	 *            the topic's URL
	 * @return ITopic or null
	 */
	private ITopic getTopicNoDescr(String href) {
		ITopic result = getOwnedTopic(href);
		if (result != null) {
			return result;
		}

		// check inside children TOCs
		for (Iterator it = getChildrenTocs().iterator(); it.hasNext();) {
			Toc childToc = (Toc) it.next();
			// must not return description topic from children TOCs
			result = childToc.getTopicNoDescr(href);
			if (result != null) {
				break;
			}
		}
		return result;
	}
	/**
	 * This public method is to be used after the build of TOCs is finished.
	 * With assumption that TOC model is not modifiable after the build, this
	 * method caches subtopics in an array and releases objects used only during
	 * build.
	 * 
	 * @return ITopic[]
	 */
	public ITopic[] getTopics() {
		if (topicArray == null) {
			List topics = getChildTopics();
			// create and cache array of children (Topics only)
			topicArray = new ITopic[topics.size()];
			topics.toArray(topicArray);
			// after TOC is build, TocFile no longer needed
			tocFile = null;
		}
		return topicArray;
	}
	/**
	 * @return ITopic or null;
	 */
	public String getTocTopicHref() {
		if (descriptionTopic != null) {
			return descriptionTopic.getHref();
		}
		return null;
	}
	/**
	 * Returns a topic with the specified href defined by this TOC, without
	 * looking in children TOCs <br>
	 * If the TOC contains multiple topics with the same href only of them
	 * (arbitrarily chosen) will be returned. TOC Descritpion topic is ignored.
	 * 
	 * @param href
	 *            the topic's URL.
	 * @return ITopic or null
	 */
	public ITopic getOwnedTopic(String href) {
		return (ITopic) topicMap.get(href);
	}
	/**
	 * @return ITopic[]
	 */
	public ITopic[] getExtraTopics() {
		Collection dirTopicCollection = directoryToc.getExtraTopics().values();
		ITopic[] dirTopics = (ITopic[]) dirTopicCollection
				.toArray(new ITopic[dirTopicCollection.size()]);

		// add extra topics from children TOCs.
		for (Iterator it = childrenTocs.iterator(); it.hasNext();) {
			IToc toc = (IToc) it.next();
			if (toc instanceof Toc) {
				ITopic[] moreDirTopics = ((Toc) toc).getExtraTopics();
				if (moreDirTopics.length > 0) {
					ITopic[] newDirTopics = new ITopic[dirTopics.length
							+ moreDirTopics.length];
					System.arraycopy(dirTopics, 0, newDirTopics, 0,
							dirTopics.length);
					System.arraycopy(moreDirTopics, 0, newDirTopics,
							dirTopics.length, moreDirTopics.length);
					dirTopics = newDirTopics;
				}
			}
		}

		return dirTopics;
	}
	/**
	 * Returns a topic with the specified href found in extra dir defined by
	 * this TOC, without looking in children TOCs
	 * 
	 * @param href
	 *            the topic's URL.
	 * @return ITopic or null
	 */
	public ITopic getOwnedExtraTopic(String href) {
		return (ITopic) directoryToc.getExtraTopics().get(href);

	}
	/**
	 * Used by debugger
	 */
	public String toString() {
		return href != null ? href : super.toString();
	}

	/**
	 * Gets the childrenTocs.
	 * 
	 * @return Returns a Collection of Toc
	 */
	public List getChildrenTocs() {
		return childrenTocs;
	}
	public int size() {
		if (size == SIZE_UNINITIALIZED) {
			size = topicMap.size();
			for (Iterator it = childrenTocs.iterator(); it.hasNext();) {
				size += ((Toc) it.next()).size();
			}
		}
		return size;
	}
	void registerTopic(ITopic topic) {
		String topicHref = topic.getHref();
		if (topicHref != null) {
			topicMap.put(topicHref, topic);
		}
	}
}
