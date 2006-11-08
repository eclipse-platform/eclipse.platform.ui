/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.Node;
import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.NodeAdapter;
import org.eclipse.help.internal.Topic;

/*
 * Adapts a "toc" Node as an IToc. All methods operate on the
 * underlying adapted Node.
 */
public class Toc extends NodeAdapter implements IToc {

	public static final String NAME = "toc"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$
	public static final String ATTRIBUTE_HREF = "href"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TOPIC = "topic"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LINK_TO = "link_to"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	private ITopic topic;
	private Map href2TopicMap;

	/*
	 * Constructs a new toc adapter for an empty toc node.
	 */
	public Toc() {
		super();
		setNodeName(NAME);
	}

	/*
	 * Constructs a new toc adapter for the given toc node.
	 */
	public Toc(Node node) {
		super(node);
	}
	
	/*
	 * Creates a mapping of all topic hrefs to ITopics.
	 */
	private Map createHref2TopicMap() {
		Map map = new HashMap();
		if (topic != null) {
			map.put(topic.getHref(), topic);
		}
		ITopic[] topics = getTopics();
		for (int i=0;i<topics.length;++i) {
			createHref2TopicMapAux(map, topics[i]);
		}
		return map;
	}

	/*
	 * Creates a mapping of all topic hrefs to ITopics under the given
	 * ITopic and stores in the given Map.
	 */
	private void createHref2TopicMapAux(Map map, ITopic topic) {
		map.put(topic.getHref(), topic);
		ITopic[] subtopics = topic.getSubtopics();
		if (subtopics != null) {
			for (int i=0;i<subtopics.length;++i) {
				if (subtopics[i] != null) {
					createHref2TopicMapAux(map, subtopics[i]);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IHelpResource#getHref()
	 */
	public String getHref() {
		Node parent = node.getParentNode();
		if (parent != null) {
			return parent.getAttribute(ATTRIBUTE_ID);
		}
		return null;
	}

	/*
	 * Returns a mapping of all topic hrefs to ITopics.
	 */
	public Map getHref2TopicMap() {
		if (href2TopicMap == null) {
			href2TopicMap = createHref2TopicMap();
		}
		return href2TopicMap;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IHelpResource#getLabel()
	 */
	public String getLabel() {
		return node.getAttribute(ATTRIBUTE_LABEL);
	}
	
	/*
	 * Returns the path to the toc and anchor to link this toc into.
	 */
	public String getLinkTo() {
		return node.getAttribute(ATTRIBUTE_LINK_TO);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IToc#getTocContribution()
	 */
	public TocContribution getTocContribution() {
		return (TocContribution)node.getParentNode();
	}
	
	/*
	 * Returns the toc's own topic href.
	 */
	public String getTopic() {
		return node.getAttribute(ATTRIBUTE_TOPIC);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IToc#getTopic(java.lang.String)
	 */
	public ITopic getTopic(String href) {
		if (href == null) {
			if (topic == null) {
				topic = new ITopic() {
					public String getHref() {
						return getTopic();
					}
					public String getLabel() {
						return Toc.this.getLabel();
					}
					public ITopic[] getSubtopics() {
						return getTopics();
					}
				};
			}
			return topic;
		}
		else {
			return (ITopic)getHref2TopicMap().get(href);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IToc#getTopics()
	 */
	public ITopic[] getTopics() {
		return (Topic[])getChildren(Topic.NAME, Topic.class);
	}
	
	/*
	 * Sets the toc's label.
	 */
	public void setLabel(String label) {
		node.setAttribute(ATTRIBUTE_LABEL, label);
	}

	/*
	 * Sets the toc's link_to target.
	 */
	public void setLinkTo(String linkTo) {
		node.setAttribute(ATTRIBUTE_LINK_TO, linkTo);
	}

	/*
	 * Sets the toc's own topic href.
	 */
	public void setTopic(String href) {
		node.setAttribute(ATTRIBUTE_TOPIC, href);
	}
}
