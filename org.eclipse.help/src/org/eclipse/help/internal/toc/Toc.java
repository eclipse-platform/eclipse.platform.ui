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
import java.util.List;
import java.util.Map;

import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.Node;

public class Toc extends Node implements IToc {

	private String label;
	private ITopic topic;
	private ITopic[] topics;
	private Map href2TopicMap;
	private TocContribution contribution;
	
	public Toc(String label, String topic) {
		this.label = label;
		this.topic = new Topic(topic, label) {
			public ITopic[] getSubtopics() {
				return getTopics();
			}
		};
	}
	
	public String getHref() {
		return contribution.getId();
	}

	public String getLabel() {
		return label;
	}
	
	public ITocContribution getTocContribution() {
		return contribution;
	}
	
	public ITopic getTopic(String href) {
		if (href == null) {
			return topic;
		}
		else {
			return (ITopic)getHref2TopicMap().get(href);
		}
	}
	
	public Map getHref2TopicMap() {
		if (href2TopicMap == null) {
			href2TopicMap = createHref2TopicMap();
		}
		return href2TopicMap;
	}
	
	public ITopic[] getTopics() {
		if (topics == null) {
			List list = getChildren(ITopic.class);
			topics = (ITopic[])list.toArray(new ITopic[list.size()]);
		}
		return topics;
	}
	
	public void setTocContribution(TocContribution contribution) {
		this.contribution = contribution;
	}
	
	private Map createHref2TopicMap() {
		Map map = new HashMap();
		map.put(topic.getHref(), topic);
		ITopic[] topics = getTopics();
		for (int i=0;i<topics.length;++i) {
			createHref2TopicMapAux(map, topics[i]);
		}
		return map;
	}
	
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
}
