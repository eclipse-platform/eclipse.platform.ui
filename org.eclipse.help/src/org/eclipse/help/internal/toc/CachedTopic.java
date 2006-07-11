package org.eclipse.help.internal.toc;

import org.eclipse.help.ITopic;

/*
 * Wraps an existing ITopic, pre-fetches all the data in constructor (calls all
 * operations) and returns cached values only.
 */
public class CachedTopic implements ITopic {

	private static final ITopic[] EMPTY_ARRAY = new ITopic[0];
	
	private ITopic[] subtopics;
	private String href;
	private String label;
	
	/*
	 * Constructs a wrapper for the given ITopic. All data is fetched
	 * in constructor.
	 */
	public CachedTopic(ITopic original) {
		subtopics = original.getSubtopics();
		if (subtopics == null || subtopics.length == 0) {
			subtopics = EMPTY_ARRAY;
		}
		else {
			for (int i=0;i<subtopics.length;++i) {
				subtopics[i] = new CachedTopic(subtopics[i]);
			}
		}
		href = original.getHref();
		label = original.getLabel();
	}
	
	public ITopic[] getSubtopics() {
		return subtopics;
	}

	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}
}
