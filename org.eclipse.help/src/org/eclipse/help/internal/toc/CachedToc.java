package org.eclipse.help.internal.toc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;

/*
 * Wraps an existing IToc, pre-fetches all the data in constructor (calls all
 * operations) and returns cached values only.
 */
public class CachedToc implements IToc {

	private static final ITopic[] EMPTY_ARRAY = new ITopic[0];

	private Map href2TopicMap;
	private ITopic topic;
	private ITopic[] topics;
	private String href;
	private String label;
	
	/*
	 * Constructs a wrapper for the given IToc. All data is fetched
	 * in constructor.
	 */
	public CachedToc(IToc original) {
		topics = original.getTopics();
		if (topics == null || topics.length == 0) {
			topics = EMPTY_ARRAY;
		}
		else {
			for (int i=0;i<topics.length;++i) {
				topics[i] = new CachedTopic(topics[i]);
			}
		}
		topic = new CachedTopic(original.getTopic(null));
		href = original.getHref();
		label = original.getLabel();
		href2TopicMap = createHref2TopicMap();
	}
	
	public ITopic getTopic(String href) {
		return (ITopic)href2TopicMap.get(href);
	}

	public ITopic[] getTopics() {
		return topics;
	}

	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}
	
	private Map createHref2TopicMap() {
		Map map = new HashMap();
		// create a topic for the overall toc
		map.put(null, topic);
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
