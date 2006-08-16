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
package org.eclipse.help.internal.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.help.internal.toc.Topic;

/*
 * Assembles individual keyword index contributions into a complete, fully
 * sorted master index.
 */
public class IndexAssembler {

	private List contributions;
	private Index index;
    private TocManager tocManager = HelpPlugin.getTocManager();
    private IToc[] tocs = HelpSystem.getTocs();

	/*
	 * Assembles the given index contributions into a complete, sorted index.
	 * The originals are not modified.
	 */
	public IIndex assemble(List contributions) {
		this.contributions = contributions;
		processMerge();
		processTopics();
		processOrder();
		return index;
	}
	
	/*
	 * Merge all indexes into one large index, not necessarily sorted.
	 */
	private void processMerge() {
		List rootEntries = new ArrayList();
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			IndexContribution contribution = (IndexContribution)iter.next();
			IIndexEntry[] entries = contribution.getIndex().getEntries();
			rootEntries.addAll(Arrays.asList(entries));
		}
		index = new Index();
		index.addChildren((Node[])rootEntries.toArray(new Node[rootEntries.size()]));
	}
	
	/*
	 * Sort the master index by keyword and topic label at all levels of
	 * the keyword index.
	 */
	private void processOrder() {
		Comparator c = new Comparator() {
			public int compare(Object o1, Object o2) {
				/*
				 * First separate the objects into different groups by type;
				 * topics first, then entries, etc. Then within each
				 * group, sort alphabetically.
				 */
				int c1 = getCategory(o1);
				int c2 = getCategory(o2);
				if (c1 == c2) {
					// same type of object; compare alphabetically
					String s1 = getLabel(o1);
					String s2 = getLabel(o2);
					return s1.compareTo(s2);
				}
				else {
					// different types; compare by type
					return c1 - c2;
				}
			}
		};
		sort(index, c);
	}
	
	/*
	 * Returns the category of the object. The order is:
	 * topics, entries, other.
	 */
	private static int getCategory(Object o) {
		if (o instanceof ITopic) {
			return 0;
		}
		else if (o instanceof IIndexEntry) {
			return 1;
		}
		else {
			return 2;
		}
	}
	
	/*
	 * Returns the string that will be displayed for the given object,
	 * used for sorting.
	 */
	private static String getLabel(Object o) {
		if (o instanceof ITopic) {
			return ((ITopic)o).getLabel();
		}
		else if (o instanceof IIndexEntry) {
			return ((IIndexEntry)o).getKeyword();
		}
		else {
			return o.toString();
		}
	}
	
	/*
	 * Fills in missing topic labels and removes topics that only exist in
	 * ignored tocs.
	 */
	private void processTopics() {
		processTopics(index.getChildrenInternal());
	}
	
	/*
	 * Fills in missing topic labels and removes topics that only exist in
	 * ignored tocs, starting with the given nodes.
	 */
	private void processTopics(Node[] nodes) {
		for (int i=0;i<nodes.length;++i) {
			if (nodes[i] instanceof Topic) {
				Topic topic = (Topic)nodes[i];
				String label = topic.getLabel();
				String href = topic.getHref();
				boolean isLabelEmpty = (label == null || label.length() == 0);
		        if (isLabelEmpty) {
					for (int j=0;j<tocs.length;j++) {
			            ITopic t = tocs[j].getTopic(href);
			            if (t != null) {
							if(isLabelEmpty) {
								topic.setLabel(t.getLabel());
								isLabelEmpty = false;
							}
			            }
			        }
		        }
				if(isLabelEmpty) {
					topic.setLabel(""); //$NON-NLS-1$
				}
				if (tocManager.isTopicIgnored(href)) {
					topic.getParentInternal().removeChild(topic);
				}
			}
			Node[] children = nodes[i].getChildrenInternal();
			processTopics(children);
		}
	}
	
	/*
	 * Sort the given node's descendants recursively using the given
	 * Comparator.
	 */
	private void sort(Node node, Comparator c) {
		node.sortChildren(c);
		Node[] children = node.getChildrenInternal();
		for (int i=0;i<children.length;++i) {
			sort(children[i], c);
		}
	}
}
