/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceCategory;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Instances access the registry that is provided at creation time in order to
 * determine the contributed preference pages
 */
public class PreferencePageRegistryReader extends RegistryReader {
	public static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	public static final String ATT_CLASS = "class"; //$NON-NLS-1$

	public static final String ATT_NAME = "name"; //$NON-NLS-1$

	public static final String ATT_ID = "id"; //$NON-NLS-1$

	public static final String TAG_PAGE = "page"; //$NON-NLS-1$

	public static final String ATT_ICON = "icon"; //$NON-NLS-1$

	public static final String PREFERENCE_SEPARATOR = "/"; //$NON-NLS-1$

	public static final String TAG_CATEGORY = "category"; //$NON-NLS-1$

	public static final String ATT_PARENT_CATEGORY = "parent"; //$NON-NLS-N$

	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((CategoryNode) arg0).getFlatCategory();
			String s2 = ((CategoryNode) arg1).getFlatCategory();
			return collator.compare(s1, s2);
		}
	};

	private List nodes;

	private Hashtable categories;

	private IWorkbench workbench;

	/**
	 * Internal class used to sort all the preference page nodes based on the
	 * category.
	 */
	class CategoryNode {
		private WorkbenchPreferenceNode node;

		private String flatCategory;

		/**
		 * Default constructor
		 */
		public CategoryNode(WorkbenchPreferenceNode node) {
			this.node = node;
		}

		/**
		 * Return the preference node this category represents
		 */
		public WorkbenchPreferenceNode getNode() {
			return node;
		}

		/**
		 * Return the flatten category
		 */
		public String getFlatCategory() {
			if (flatCategory == null) {
				initialize();
				if (flatCategory == null)
					flatCategory = node.getLabelText();
			}
			return flatCategory;
		}

		/*
		 * Initialize the flat category to include the parents' category names
		 * and the current node's label
		 */
		private void initialize() {
			String category = node.getCategory();
			if (category == null)
				return;

			StringBuffer sb = new StringBuffer();
			StringTokenizer stok = new StringTokenizer(category,
					PREFERENCE_SEPARATOR);
			WorkbenchPreferenceNode immediateParent = null;
			while (stok.hasMoreTokens()) {
				String pathID = stok.nextToken();
				immediateParent = findNode(pathID);
				if (immediateParent == null)
					return;
				if (sb.length() > 0)
					sb.append(PREFERENCE_SEPARATOR);
				sb.append(immediateParent.getLabelText());
			}

			if (sb.length() > 0)
				sb.append(PREFERENCE_SEPARATOR);
			sb.append(node.getLabelText());
			flatCategory = sb.toString();
		}
	}

	/**
	 * Create a new instance configured with the workbench
	 */
	public PreferencePageRegistryReader(IWorkbench newWorkbench) {
		workbench = newWorkbench;
	}

	/**
	 * Searches for the top-level node with the given id.
	 */
	private WorkbenchPreferenceNode findNode(String id) {
		for (int i = 0; i < nodes.size(); i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes
					.get(i);
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}

	/**
	 * Searches for the child node with the given ID in the provided parent
	 * node. If not found, null is returned.
	 */
	private WorkbenchPreferenceNode findNode(WorkbenchPreferenceNode parent,
			String id) {
		IPreferenceNode[] subNodes = parent.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) subNodes[i];
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}

	/**
	 * Load the preference page contirbutions from the registry and organize
	 * preference node contributions by category into hierarchies If there is no
	 * page for a given node in the hierarchy then a blank page will be created.
	 * If no category has been specified or category information is incorrect,
	 * page will appear at the root level. workbench log entry will be created
	 * for incorrect category information.
	 * 
	 * After the nodes are processed add the top level categories at the end.
	 * 
	 * @return List of WorkbenchPreferenceNode and WorkbenchCategoryNode
	 */
	public List getPreferenceContributions(IExtensionRegistry registry) {
		loadNodesFromRegistry(registry); // all nodes keyed on category
		List contributions = new ArrayList();
		// root nodes (which contain subnodes)

		// Add root nodes to the contributions vector
		StringTokenizer tokenizer;
		String currentToken;

		// Make the advisor's favorite the first category
		IPreferenceNode favorite = null;
		String favoriteId = ((Workbench) workbench).getMainPreferencePageId();
		if (favoriteId != null) {
			favorite = findNode(favoriteId);
		}
		if (favorite != null) {
			contributions.add(favorite);
		}

		// Sort nodes based on flattened display path composed of
		// actual labels of nodes referenced in category attribute.
		Object[] sortedNodes = sortByCategories(nodes);
		for (int i = 0; i < sortedNodes.length; i++) {
			// Iterate through all the nodes
			CategoryNode categoryNode = (CategoryNode) sortedNodes[i];
			WorkbenchPreferenceNode node = categoryNode.getNode();
			if (node == favorite) {
				// skip it - favorite already at the top of the list
				continue;
			}
			String category = node.getCategory();
			if (category == null) {
				contributions.add(node);
				continue;
			}
			// has category
			tokenizer = new StringTokenizer(category, PREFERENCE_SEPARATOR);
			WorkbenchPreferenceNode parent = null;
			while (tokenizer.hasMoreElements()) {
				currentToken = tokenizer.nextToken();
				WorkbenchPreferenceNode child = null;
				if (parent == null)
					child = findNode(currentToken);
				else
					child = findNode(parent, currentToken);
				if (child == null) {
					parent = null;
					break;
				} else {
					parent = child;
				}
			}
			if (parent != null) {
				parent.add(node);
			} else {
				if (!searchCategories(node)) {
					// Could not find the parent - log
					WorkbenchPlugin
							.log("Invalid preference page path: " + categoryNode.getFlatCategory()); //$NON-NLS-1$
					contributions.add(node);
				}
			}
		}

		// Add all of the categories
		contributions.addAll(getOrganizedCategories());
		return contributions;
	}

	/**
	 * Search the defined categories for nodes category. If its category
	 * is valid add it to the category and return <code>true</code>. 
	 * If not then return <code>false</code>.
	 * @param node
	 * @return boolean
	 */
	private boolean searchCategories(WorkbenchPreferenceNode node) {
		String category = node.getCategory();
		if(categories.containsKey(category)){
			WorkbenchPreferenceCategory categoryNode = (WorkbenchPreferenceCategory) categories.get(category);
			categoryNode.addNode(node);
			return true;
		}
		return false;
	}

	/**
	 * Return the categories in sorted and in tree order.
	 * 
	 * @return
	 */
	private Collection getOrganizedCategories() {
		Collection topCategories = new ArrayList();

		Iterator allCategories = categories.values().iterator();

		while (allCategories.hasNext()) {
			WorkbenchPreferenceCategory category = (WorkbenchPreferenceCategory) allCategories
					.next();
			String parentId = category.getParent();
			if (parentId == null)
				topCategories.add(category);
			else {
				Object parent = categories.get(parentId);
				if (parent == null) {
					WorkbenchPlugin.log("Invalid category path: " + parentId); //$NON-NLS-1$
					topCategories.add(category);
				} else {
					((WorkbenchPreferenceCategory) parent).addChild(category);
				}
			}

		}
		return topCategories;

	}

	/**
	 * Get the preference nodes that are defined in the registry
	 */
	protected void loadNodesFromRegistry(IExtensionRegistry registry) {
		nodes = new ArrayList();
		categories = new Hashtable();
		readRegistry(registry, PlatformUI.PLUGIN_ID,
				IWorkbenchConstants.PL_PREFERENCES);
	}

	/**
	 * Read preference page element.
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_PAGE) == false)
			return checkForCategory(element);
		WorkbenchPreferenceNode node = createNode(workbench, element);
		if (node != null)
			nodes.add(node);
		readElementChildren(element);
		return true;
	}

	/**
	 * Check to see if there is a category defined here.
	 * 
	 * @param element
	 * @return
	 */
	private boolean checkForCategory(IConfigurationElement element) {
		if (element.getName().equals(TAG_CATEGORY) == false)
			return false;

		String name = element.getAttribute(ATT_NAME);
		String id = element.getAttribute(ATT_ID);
		String icon = element.getAttribute(ATT_ICON);
		String parent = element.getAttribute(ATT_PARENT_CATEGORY);

		ImageDescriptor descriptor = null;

		if (icon != null) {
			String contributingPluginId = element.getDeclaringExtension()
					.getNamespace();
			descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
					contributingPluginId, icon);
		}

		categories.put(id, new WorkbenchPreferenceCategory(id, name, parent,
				descriptor));
		return true;
	}

	public static WorkbenchPreferenceNode createNode(IWorkbench workbench,
			IConfigurationElement element) {
		String name = element.getAttribute(ATT_NAME);
		String id = element.getAttribute(ATT_ID);
		String category = element.getAttribute(ATT_CATEGORY);
		String imageName = element.getAttribute(ATT_ICON);
		String className = element.getAttribute(ATT_CLASS);
		if (name == null) {
			logMissingAttribute(element, ATT_NAME);
		}
		if (id == null) {
			logMissingAttribute(element, ATT_ID);
		}
		if (className == null) {
			logMissingAttribute(element, ATT_CLASS);
		}
		if (name == null || id == null || className == null) {
			return null;
		}
		ImageDescriptor image = null;
		if (imageName != null) {
			String contributingPluginId = element.getDeclaringExtension()
					.getNamespace();
			image = AbstractUIPlugin.imageDescriptorFromPlugin(
					contributingPluginId, imageName);
		}
		WorkbenchPreferenceNode node = new WorkbenchPreferenceNode(id, name,
				category, image, element, workbench);
		return node;
	}

	/**
	 * Sort the nodes based on full category + name. Category used for sorting
	 * is created by substituting node IDs with labels of the referenced nodes.
	 * workbench node is excluded from sorting because it always appears first
	 * in the dialog.
	 */
	private Object[] sortByCategories(List categoryNodes) {
		// sort by categories
		CategoryNode[] nodeArray = new CategoryNode[categoryNodes.size()];

		for (int i = 0; i < categoryNodes.size(); i++) {
			nodeArray[i] = new CategoryNode(
					(WorkbenchPreferenceNode) categoryNodes.get(i));
		}

		Collections.sort(Arrays.asList(nodeArray), comparer);
		return nodeArray;
	}
}