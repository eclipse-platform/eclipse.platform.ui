/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.help.ITopic;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.UAElement;

import com.ibm.icu.text.Collator;

/*
 * Handles the "sort" attribute on topics and tocs
 */
public class TopicSorter {
	
	private Comparator comparator;
	
	public void sortChildren(Toc toc) {
		if (comparator == null) {
			comparator = new TopicComparator();
		}
		if (toc.isSorted()) { 
			sort(toc, toc.getTopics());
	    }
		ITopic[] childTopics = toc.getTopics();
		for (int i = 0; i < childTopics.length; i++) {
			sortChildren((Topic)childTopics[i]);
		}
	}
	
	private void sortChildren(Topic topic) {
		if (topic.isSorted()) { 
			sort(topic, topic.getSubtopics());
	    }
		ITopic[] childTopics = topic.getSubtopics();
		for (int i = 0; i < childTopics.length; i++) {
			sortChildren((Topic)childTopics[i]);
		}
	}

	private class TopicComparator implements Comparator {
		Collator collator = Collator.getInstance();
		
		public int compare(Object o1, Object o2) {
			String label1 = ((ITopic)o1).getLabel();
			String label2 = ((ITopic)o2).getLabel();
			return collator.compare(label1, label2);
		}	
	}
	
	/*
	 * Sort the given node's descendants recursively using the given
	 * Comparator.
	 */
	private void sort(UAElement element, ITopic[] children) {
		// sort children
		if (children.length > 1) {
			for (int i=0;i<children.length;++i) {
				element.removeChild((UAElement)children[i]);
			}
			Arrays.sort(children, comparator);
			for (int i=0;i<children.length;++i) {
				element.appendChild((UAElement)children[i]);
			}
		}
	}
}

	