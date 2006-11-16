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

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeProcessor;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.dynamic.ValidationHandler;
import org.eclipse.help.internal.toc.HrefUtil;

/*
 * Assembles individual keyword index contributions into a complete, fully
 * sorted master index.
 */
public class IndexAssembler {

	private NodeProcessor processor;
	private Comparator comparator;
	private String locale;
	private Map requiredAttributes;

	/*
	 * Assembles the given index contributions into a complete, sorted index.
	 * The originals are not modified.
	 */
	public Index assemble(List contributions, String locale) {
		this.locale = locale;
		process(contributions);
		Index index = merge(contributions);
		sort(index);
		return index;
	}
	
	/*
	 * Merge all index contributions into one large index, not sorted.
	 */
	private Index merge(List contributions) {
		Index index = new Index();
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			IndexContribution contribution = (IndexContribution)iter.next();
			mergeChildren(index, contribution.getIndex());
		}
		return index;
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
		Node[] childrenA = a.getChildNodes();
		for (int i=0;i<childrenA.length;++i) {
			Node childA = childrenA[i];
			if (IndexEntry.NAME.equals(childA.getNodeName())) {
				entriesByKeyword.put(childA.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD), childA);
			}
			else if (Topic.NAME.equals(childA.getNodeName())) {
				topicHrefs.add(childA.getAttribute(Topic.ATTRIBUTE_HREF));
			}
		}
		
		// now do the merge
		Node[] childrenB = b.getChildNodes();
		for (int i=0;i<childrenB.length;++i) {
			Node childB = childrenB[i];
			if (IndexEntry.NAME.equals(childB.getNodeName())) {
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
			else if (Topic.NAME.equals(childB.getNodeName())) {
				String href = childB.getAttribute(Topic.ATTRIBUTE_HREF);
				if (!topicHrefs.contains(href)) {
					// add topic only if href doesn't exist yet
					a.appendChild(childB);
					topicHrefs.add(href);
				}
			}
		}
	}
	
	private void process(List contributions) {
		if (processor == null) {
			NodeReader reader = new NodeReader();
			reader.setIgnoreWhitespaceNodes(true);
			processor = new NodeProcessor(new NodeHandler[] {
				new ValidationHandler(getRequiredAttributes()),
				new NormalizeHandler(),
				new LabelHandler(),
				new IncludeHandler(reader, locale),
				new ExtensionHandler(reader, locale),
			});
		}
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			IndexContribution contribution = (IndexContribution)iter.next();
			processor.process(contribution.getIndex(), contribution.getId());
		}
	}

	/*
	 * Sort the given node's descendants recursively.
	 */
	private void sort(Node node) {
		if (comparator == null) {
			comparator = new IndexComparator();
		}
		sort(node, comparator);
	}
	
	/*
	 * Sort the given node's descendants recursively using the given
	 * Comparator.
	 */
	private void sort(Node node, Comparator comparator) {
		// sort children
		Node[] children = node.getChildNodes();
		for (int i=0;i<children.length;++i) {
			node.removeChild(children[i]);
		}
		Arrays.sort(children, comparator);
		for (int i=0;i<children.length;++i) {
			node.appendChild(children[i]);
		}
		// sort children's children
		for (int i=0;i<children.length;++i) {
			sort(children[i], comparator);
		}
	}

	private Map getRequiredAttributes() {
		if (requiredAttributes == null) {
			requiredAttributes = new HashMap();
			requiredAttributes.put(IndexEntry.NAME, new String[] { IndexEntry.ATTRIBUTE_KEYWORD });
			requiredAttributes.put(Topic.NAME, new String[] { Topic.ATTRIBUTE_HREF });
			requiredAttributes.put("anchor", new String[] { "id" }); //$NON-NLS-1$ //$NON-NLS-2$
			requiredAttributes.put("include", new String[] { "path" }); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return requiredAttributes;
	}
	
	/*
	 * Normalizes topic hrefs, by prepending the plug-in id to form an href.
	 * e.g. "path/myfile.html" -> "/my.plugin/path/myfile.html"
	 */
	private class NormalizeHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (Topic.NAME.equals(node.getNodeName())) {
				String href = node.getAttribute(Topic.ATTRIBUTE_HREF);
				if (href != null) {
					int index = id.indexOf('/', 1);
					if (index != -1) {
						String pluginId = id.substring(1, index);
						node.setAttribute(Topic.ATTRIBUTE_HREF, HrefUtil.normalizeHref(pluginId, href));
					}
				}
			}
			return UNHANDLED;
		}
	}

	private class LabelHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (Topic.NAME.equals(node.getNodeName())) {
				Topic topic = node instanceof Topic ? (Topic)node : new Topic(node);
				String label = topic.getLabel();
				String href = topic.getHref();
				boolean isLabelEmpty = (label == null || label.length() == 0);
		        if (isLabelEmpty) {
		        	IToc[] tocs = HelpPlugin.getTocManager().getTocs(locale);
					for (int j=0;j<tocs.length;j++) {
			            ITopic t = tocs[j].getTopic(href);
			            if (t != null) {
							topic.setLabel(t.getLabel());
							isLabelEmpty = false;
							break;
			            }
			        }
		        }
				if(isLabelEmpty) {
					Node parent = node.getParentNode();
					if (parent != null) {
						parent.removeChild(node);
					}
					String msg = "Unable to look up label for help keyword index topic \"" + href + "\" with missing \"" + Topic.ATTRIBUTE_LABEL + "\" attribute (topic does not exist in table of contents; skipping)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					HelpPlugin.logError(msg);
				}
			}
			return UNHANDLED;
		}
	}

	private static class IndexComparator implements Comparator {
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

		/*
		 * Returns the category of the node. The order is:
		 * 1. topics
		 * 2. entries starting with non-alphanumeric
		 * 3. entries starting with digit
		 * 4. entries starting with alpha
		 * 5. other
		 */
		private static int getCategory(Node node) {
			if (Topic.NAME.equals(node.getNodeName())) {
				return 0;
			}
			else if (IndexEntry.NAME.equals(node.getNodeName())) {
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
			if (Topic.NAME.equals(node.getNodeName())) {
				return node.getAttribute(Topic.ATTRIBUTE_LABEL);
			}
			else if (IndexEntry.NAME.equals(node.getNodeName())) {
				return node.getAttribute(IndexEntry.ATTRIBUTE_KEYWORD);
			}
			else {
				return node.getNodeName();
			}
		}
	};
}
