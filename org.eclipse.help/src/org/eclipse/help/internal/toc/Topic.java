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
 * Topic. Visible navigation element. Labeled, contains linik to a document. Can
 * also act as a container for other documents.
 */
public class Topic extends TocNode implements ITopic, ITopicElement {
	private String href;
	private String label;
	private ITopic[] topicArray;
	/**
	 * Constructor.
	 */
	protected Topic(TocFile tocFile, Attributes attrs) {
		if (attrs == null)
			return;
		href = attrs.getValue("href"); //$NON-NLS-1$
		if (href != null && href.length() > 0)
			href = HrefUtil.normalizeHref(tocFile.getPluginID(), href);
		label = attrs.getValue("label"); //$NON-NLS-1$
		if (label == null) {
			throw new RuntimeException("topic label==null"); //$NON-NLS-1$
		}
		tocFile.getToc().registerTopic(this);
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
	 * This public method is to be used after the build of TOCs is finished.
	 * With assumption that TOC model is not modifiable after the build, this
	 * method caches subtopics in an array and releases objects used only during
	 * build.
	 * 
	 * @return ITopic list
	 */
	public ITopic[] getSubtopics() {
		if (topicArray == null) {
			List topics = getChildTopics();
			// create and cache array of children (Topics only)
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
	/**
	 * Obtains shortest path leading to this topic in a given TOC
	 * 
	 * @param toc
	 * @return ITopic[] or null, path excludes TOC and includes this topic
	 */
	public ITopic[] getPathInToc(IToc toc) {
		List /* of TocNode */ancestors = getTopicPathInToc(toc, this);
		if (ancestors == null) {
			return null;
		}
		return (ITopic[]) ancestors.toArray(new ITopic[ancestors.size()]);
	}

	/**
	 * Obtains List of ancestors (TocNodes) leading to specific topic or null
	 * 
	 * @param toc
	 * @param topic
	 * @return List with TocElements: topic1, topic2, topic
	 */
	static List getTopicPathInToc(IToc toc, Topic topic) {
		List topicParents = new ArrayList(topic.getParents());
		for (ListIterator it = topicParents.listIterator(); it.hasNext();) {
			TocNode tocNode = (TocNode) it.next();
			if (!(tocNode instanceof Topic)) {
				// Check if any parent is the needed TOC
				if (tocNode == toc) {
					// success, found the correct TOC
					List ancestors = new ArrayList();
					ancestors.add(topic);
					return ancestors;
				} else {
					// substitute real topics for toc, link, and anchor parent
					// nodes, because we are looking for the shortest path
					List grandParents = tocNode.getParents();
					it.remove();
					for (Iterator it2 = grandParents.iterator(); it2.hasNext();) {
						it.add(it2.next());
						it.previous();
					}
				}

			}
		}

		for (Iterator it = topicParents.iterator(); it.hasNext();) {
			// delegate to ancestors first
			List a = getTopicPathInToc(toc, (Topic) it.next());
			if (a != null) {
				// then add this topic to the path
				a.add(topic);
				return a;
			}
		}

		return null;
	}
}
