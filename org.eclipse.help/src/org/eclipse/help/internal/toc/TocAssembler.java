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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.help.ITocContribution;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.Anchor;
import org.eclipse.help.internal.Filter;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Include;
import org.eclipse.help.internal.Node;

/*
 * Assembles toc contributions (toc fragments) into complete, linked, and
 * filtered books.
 */
public class TocAssembler {

	private List workingCopy;
	private List includes;
	private List filters;
	private Map id2AnchorMap;
	private Map id2ContributionMap;
	
	/*
	 * Assembles the given toc contributions into complete, linked, and filtered
	 * books. The originals are not modified.
	 */
	public List assemble(List contributions) {
		workingCopy = createWorkingCopy(contributions);
		discoverNodes();
		processLinkTos();
		processIncludes();
		processFilters();
		processOrphans();
		return workingCopy;
	}
	
	/*
	 * Creates an identical copy of the given contributions. The list and all
	 * its contents are duplicated (deep copy).
	 */
	private List createWorkingCopy(List contributions) {
		List workingCopy = new ArrayList(contributions.size());
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			ITocContribution contribution = (ITocContribution)iter.next();
			workingCopy.add(TocPrefetcher.prefetch(contribution));
		}
		return workingCopy;
	}
	
	/*
	 * Traverse through all toc contributions to find and make note of any
	 * nodes that require processing, specifically anchors, includes, and
	 * filters.
	 */
	private void discoverNodes() {
		includes = new ArrayList();
		filters = new ArrayList();
		id2AnchorMap = new HashMap();
		id2ContributionMap = new HashMap();
		Iterator iter = workingCopy.iterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			id2ContributionMap.put(contrib.getId(), contrib);
			discover((Toc)contrib.getToc(), contrib);
		}
	}
	
	/*
	 * Traverse the given node from the given contribution to find nodes
	 * that require processing.
	 */
	private void discover(Node node, TocContribution contrib) {
		// is it one of the nodes that need processing?
		if (node instanceof Anchor) {
			String id = contrib.getId() + '#' + ((Anchor)node).getId();
			id2AnchorMap.put(id, node);
		}
		else if (node instanceof Include) {
			includes.add(node);
		}
		else if (node instanceof Filter) {
			filters.add(node);
		}
		
		// recursively discover all descendants
		Node[] children = node.getChildrenInternal();
		for (int i=0;i<children.length;++i) {
			discover(children[i], contrib);
		}
	}
	
	private Toc getOwningToc(Node node) {
		while (node != null && !(node instanceof Toc)) {
			node = node.getParentInternal();
		}
		return (Toc)node;
	}
	
	/*
	 * Processes all link_tos. Any contribution may specify that it should be
	 * inserted at a specific anchor in another toc contribution.
	 */
	private void processLinkTos() {
		ListIterator iter = workingCopy.listIterator();
		while (iter.hasNext()) {
			TocContribution contribution = (TocContribution)iter.next();
			String target = contribution.getLinkTo();
			if (target != null) {
				// find the target anchor				
				Anchor anchor = (Anchor)id2AnchorMap.get(target);
				if (anchor != null) {
					Toc toc = (Toc)contribution.getToc();
					anchor.addChildren(toc.getChildrenInternal());
					
					// combine the extra documents into the larger contribution
					int numberSignIndex = target.indexOf('#');
					String contributionId = target.substring(0, numberSignIndex);
					TocContribution targetContribution = (TocContribution)id2ContributionMap.get(contributionId);
					targetContribution.addExtraDocuments(contribution.getExtraDocuments());
				}
				else {
					String msg = "TOC contribution \"" + contribution.getId() + "\"'s link_to target anchor could not be found: " + target;  //$NON-NLS-1$//$NON-NLS-2$
					HelpPlugin.logError(msg, null);
				}
				// this is no longer a top-level toc, so remove it from book list
				iter.remove();
			}
		}
		// anchors are no longer needed; remove them
		removeIntermediateNodes(id2AnchorMap.values());
	}
	
	/*
	 * Process all include directives by replacing the include node with
	 * the target toc.
	 */
	private void processIncludes() {
		Iterator iter = includes.iterator();
		while (iter.hasNext()) {
			Include include = (Include)iter.next();
			TocContribution contrib = (TocContribution)id2ContributionMap.get(include.getTarget());
			if (contrib != null) {
				Toc toc = (Toc)contrib.getToc();
				include.getParentInternal().replaceChild(include, toc.getChildrenInternal());
				
				// no longer a top-level toc, so remove it from book list
				workingCopy.remove(contrib);
				
				// combine the extra documents into the larger toc
				Toc owningToc = getOwningToc(include);
				TocContribution owningContribution = (TocContribution)owningToc.getTocContribution();
				owningContribution.addExtraDocuments(contrib.getExtraDocuments());
			}
			else {
				String msg = "TOC contribution include target could not be found: " + include.getTarget(); //$NON-NLS-1$
				HelpPlugin.logError(msg, null);
			}
		}
	}
	
	/*
	 * Processes all filters. If the filter passes (i.e. it should be kept in),
	 * only the filter node itself is removed. Otherwise, the filter node and
	 * all its children are removed.
	 */
	private void processFilters() {
		Iterator iter = filters.iterator();
		while (iter.hasNext()) {
			Filter filter = (Filter)iter.next();
			if (UAContentFilter.isFiltered(filter.getExpression())) {
				removeIntermediateNode(filter);
			}
			else {
				filter.getParentInternal().removeChild(filter);
			}
		}
	}
	
	/*
	 * Processes all remaining contribution that are not primary and have not
	 * been linked under any other toc (orphan tocs).
	 */
	private void processOrphans() {
		ListIterator iter = workingCopy.listIterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			if (!contrib.isPrimary()) {
				iter.remove();
			}
		}
	}
	
	/*
	 * Removes the given node from the tree, but not its children. The children
	 * are "pulled up" one level.
	 */
	private void removeIntermediateNode(Node node) {
		Node parent = (Node)node.getParent();
		if (parent != null) {
			Node[] children = node.getChildrenInternal();
			parent.replaceChild(node, children);
		}
	}
	
	/*
	 * Removes all the given nodes from the tree, but not its children. The
	 * children are "pulled up" one level.
	 */
	private void removeIntermediateNodes(Collection nodes) {
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			removeIntermediateNode((Node)iter.next());
		}
	}
}
