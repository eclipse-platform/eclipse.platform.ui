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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.toc.TocManager;

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
	public Index assemble(List contributions) {
		this.contributions = contributions;
		processMerge();
		processTopics();
		processOrder();
		return index;
	}
	
	/*
	 * Merge all indexes into one large index, not sorted.
	 */
	private void processMerge() {
		index = new Index();
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			IndexContribution contribution = (IndexContribution)iter.next();
			mergeChildren(index, contribution.getIndex());
		}
	}
	
	/*
	 * Merges the children of nodes a and b, and stores them into a. If the two
	 * contain the same keyword, only one is kept but its children are merged,
	 * recursively. If multiple topics exist with the same href, only the
	 * first one found is kept.
	 */
	private void mergeChildren(Node a, Node b) {
		// create data structures for fast lookup
		Map entriesByKeyword = new HashMap();
		Set topicHrefs = new HashSet();
		Node[] childrenA = a.getChildren();
		for (int i=0;i<childrenA.length;++i) {
			Node childA = childrenA[i];
			if (IndexEntry.NAME.equals(childA.getName())) {
				entriesByKeyword.put(childA.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD), childA);
			}
			else if (Topic.NAME.equals(childA.getName())) {
				topicHrefs.add(childA.getAttribute(Topic.ATTRIBUTE_HREF));
			}
		}
		
		// now do the merge
		Node[] childrenB = b.getChildren();
		for (int i=0;i<childrenB.length;++i) {
			Node childB = childrenB[i];
			if (IndexEntry.NAME.equals(childB.getName())) {
				String keyword = childB.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD);
				if (entriesByKeyword.containsKey(keyword)) {
					// duplicate keyword; merge children
					mergeChildren((Node)entriesByKeyword.get(keyword), childB);
				}
				else {
					// wasn't a duplicate
					a.appendChild(childB);
					entriesByKeyword.put(keyword, childB);
				}
			}
			else if (Topic.NAME.equals(childB.getName())) {
				String href = childB.getAttribute(Topic.ATTRIBUTE_HREF);
				if (!topicHrefs.contains(href)) {
					// add topic only if href doesn't exist yet
					a.appendChild(childB);
					topicHrefs.add(href);
				}
			}
		}
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
				int c1 = getCategory((Node)o1);
				int c2 = getCategory((Node)o2);
				if (c1 == c2) {
					// same type of object; compare alphabetically
					String s1 = getLabel((Node)o1).toLowerCase();
					String s2 = getLabel((Node)o2).toLowerCase();
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
	 * Returns the category of the node. The order is:
	 * 1. topics
	 * 2. entries starting with non-alphanumeric
	 * 3. entries starting with digit
	 * 4. entries starting with alpha
	 * 5. other
	 */
	private static int getCategory(Node node) {
		if (Topic.NAME.equals(node.getName())) {
			return 0;
		}
		else if (IndexEntry.NAME.equals(node.getName())) {
			String keyword = node.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD);
			if (keyword != null && keyword.length() > 0) {
				char c = keyword.charAt(0);
				if (Character.isDigit(c)) {
					return 2;
				}
				else if (Character.isLetter(c)) {
					return 3;
				}
				return 1;
			}
			return 4;
		}
		else {
			return 5;
		}
	}
	
	/*
	 * Returns the string that will be displayed for the given object,
	 * used for sorting.
	 */
	private static String getLabel(Node node) {
		if (Topic.NAME.equals(node.getName())) {
			return node.getAttribute(Topic.ATTRIBUTE_LABEL);
		}
		else if (IndexEntry.NAME.equals(node.getName())) {
			return node.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD);
		}
		else {
			return node.getName();
		}
	}
	
	/*
	 * Fills in missing topic labels and removes topics that only exist in
	 * ignored tocs.
	 */
	private void processTopics() {
		processTopics(index.getChildren());
	}
	
	/*
	 * Fills in missing topic labels and removes topics that only exist in
	 * ignored tocs, starting with the given nodes.
	 */
	private void processTopics(Node[] nodes) {
		for (int i=0;i<nodes.length;++i) {
			if (Topic.NAME.equals(nodes[i].getName())) {
				Topic topic = nodes[i] instanceof Topic ? (Topic)nodes[i] : new Topic(nodes[i]);
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
					topic.getParent().removeChild(nodes[i]);
				}
			}
			Node[] children = nodes[i].getChildren();
			processTopics(children);
		}
	}
	
	/*
	 * Sort the given node's descendants recursively using the given
	 * Comparator.
	 */
	private void sort(Node node, Comparator c) {
		// sort children
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			node.removeChild(children[i]);
		}
		Arrays.sort(children, c);
		for (int i=0;i<children.length;++i) {
			node.appendChild(children[i]);
		}
		
		// sort children's children
		for (int i=0;i<children.length;++i) {
			sort(children[i], c);
		}
	}
}
