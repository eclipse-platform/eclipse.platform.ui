/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 219273
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * The CategorizedPageRegistryReader is the abstract super class of registry
 * readers for page that have categorization.
 */
public abstract class CategorizedPageRegistryReader extends RegistryReader {

	public static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	static final String PREFERENCE_SEPARATOR = "/"; //$NON-NLS-1$

	List<Object> topLevelNodes;

	/**
	 * Internal class used to sort all the preference page nodes based on the
	 * category.
	 */
	abstract static class CategoryNode {
		/**
		 * Comment for <code>reader</code>
		 */
		private final CategorizedPageRegistryReader reader;

		// private WorkbenchPreferenceNode node;

		private String flatCategory;

		/**
		 * Default constructor
		 */
		public CategoryNode(CategorizedPageRegistryReader reader) {
			this.reader = reader;
		}

		/**
		 * Return the flatten category
		 */
		public String getFlatCategory() {
			if (flatCategory == null) {
				initialize();
				if (flatCategory == null) {
					flatCategory = getLabelText();
				}
			}
			return flatCategory;
		}

		/**
		 * Get the label text for this node.
		 *
		 * @return String
		 */
		abstract String getLabelText();

		/*
		 * Initialize the flat category to include the parents' category names and the
		 * current node's label
		 */
		private void initialize() {
			String category = reader.getCategory(getNode());
			if (category == null) {
				return;
			}

			StringBuilder sb = new StringBuilder();
			StringTokenizer stok = new StringTokenizer(category, PREFERENCE_SEPARATOR);
			Object immediateParent = null;
			while (stok.hasMoreTokens()) {
				String pathID = stok.nextToken();
				immediateParent = this.reader.findNode(pathID);
				if (immediateParent == null) {
					return;
				}
				if (sb.length() > 0) {
					sb.append(PREFERENCE_SEPARATOR);
				}
				sb.append(getLabelText(immediateParent));
			}

			if (sb.length() > 0) {
				sb.append(PREFERENCE_SEPARATOR);
			}
			sb.append(getLabelText());
			flatCategory = sb.toString();
		}

		/**
		 * Return the label text for the passed element.
		 *
		 * @return String
		 */
		abstract String getLabelText(Object element);

		/**
		 * Get the node the receiver represents.
		 *
		 * @return Object
		 */
		abstract Object getNode();
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public CategorizedPageRegistryReader() {
		super();
	}

	/**
	 * Process the preference page nodes.
	 */
	void processNodes() {
		topLevelNodes = new ArrayList<>();
		// root nodes (which contain subnodes)

		// Add root nodes to the contributions vector
		StringTokenizer tokenizer;
		String currentToken;

		CategoryNode[] nodes = createCategoryNodes(getNodes());
		// flag to indicate that some work was done in the inner loop over the nodes
		boolean workDone;
		do {
			// reset the flag
			workDone = false;
			List<CategoryNode> deferred = new ArrayList<>();
			for (CategoryNode categoryNode : nodes) {
				Object node = categoryNode.getNode();

				String category = getCategory(node);
				if (category == null) {
					topLevelNodes.add(node);
					continue;
				}
				// has category
				tokenizer = new StringTokenizer(category, PREFERENCE_SEPARATOR);
				Object parent = null;
				while (tokenizer.hasMoreElements()) {
					currentToken = tokenizer.nextToken();
					Object child = null;
					if (parent == null) {
						child = findNode(currentToken);
					} else {
						child = findNode(parent, currentToken);
					}

					if (child == null) {
						parent = null;
						break;
					}
					parent = child;
				}
				if (parent != null) {
					// we've done some work - the number of nodes to process has decreased
					workDone = true;
					add(parent, node);
				} else {
					// we haven't done any work - the parent for this node has not been found.
					deferred.add(categoryNode);
				}
			}
			// reset the nodes to all that have yet to find their proper parent
			nodes = deferred.toArray(new CategoryNode[deferred.size()]);
		} while (nodes.length > 0 && workDone); // loop while we still have nodes to work on and the list is shrinking

		// log anything left over.
		for (CategoryNode categoryNode : nodes) {
			// Could not find the parent - log
			WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.WARNING, invalidCategoryNodeMessage(categoryNode), null));
			topLevelNodes.add(categoryNode.getNode());
		}
	}

	/**
	 * Return a warning message for an invalid category path.
	 *
	 * @param categoryNode the unknown category node
	 * @return an english string suitable for logging
	 */
	protected abstract String invalidCategoryNodeMessage(CategoryNode categoryNode);

	/**
	 * Get the category for the node if there is one. If there isn't return
	 * <code>null</code>.
	 *
	 * @return String or <code>null</code>.
	 */
	abstract String getCategory(Object node);

	/**
	 * Add the node to the parent.
	 */
	abstract void add(Object parent, Object node);

	/**
	 * Get the nodes for the receiver.
	 *
	 * @return Collection of Object
	 */
	abstract Collection<?> getNodes();

	/**
	 * Sort the nodes based on full category + name. Category used for sorting is
	 * created by substituting node IDs with labels of the referenced nodes.
	 * workbench node is excluded from sorting because it always appears first in
	 * the dialog.
	 */
	CategoryNode[] createCategoryNodes(Collection<?> nodesToCategorize) {
		// sort by categories
		List<CategoryNode> nodes = new ArrayList<>();

		Iterator<?> nodesIterator = nodesToCategorize.iterator();
		while (nodesIterator.hasNext()) {
			nodes.add(createCategoryNode(this, nodesIterator.next()));
		}

		return nodes.toArray(new CategoryNode[nodes.size()]);
	}

	/**
	 * Create a node for categorization from the reader and the supplied object.
	 *
	 * @return CategoryNode
	 */
	abstract CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object);

	/**
	 * Searches for the top-level node with the given id.
	 *
	 * @return Object of the type being categorized or <code>null</code>
	 */
	abstract Object findNode(String id);

	/**
	 * Find the node with the given parent with the id of currentToken.
	 */
	abstract Object findNode(Object parent, String currentToken);

}
