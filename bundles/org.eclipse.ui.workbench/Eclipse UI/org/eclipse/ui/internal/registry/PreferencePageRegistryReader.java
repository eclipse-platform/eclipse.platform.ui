/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceGroup;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;

/**
 *  Instances access the registry that is provided at creation time in order
 *  to determine the contributed preference pages
 */
public class PreferencePageRegistryReader extends RegistryReader {
	public static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	public static final String ATT_GROUP = "group"; //$NON-NLS-1$

	public static final String ATT_CLASS = "class"; //$NON-NLS-1$

	public static final String ATT_NAME = "name"; //$NON-NLS-1$

	public static final String ATT_ID = "id"; //$NON-NLS-1$

	public static final String TAG_PAGE = "page"; //$NON-NLS-1$

	public static final String ATT_ICON = "icon"; //$NON-NLS-1$

	public static final String PREFERENCE_SEPARATOR = "/"; //$NON-NLS-1$

	public static final String TAG_GROUP = "group"; //$NON-NLS-1$

	public static final String ATT_PARENT_GROUP = "parent"; //$NON-NLS-1$
	
	public static final String ADVANCED_ID = "org.eclipse.ui.advanced"; //$NON-NLS-1$

	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((CategoryNode) arg0).getFlatCategory();
			String s2 = ((CategoryNode) arg1).getFlatCategory();
			return collator.compare(s1, s2);
		}
	};

	private List nodes;

	private List topLevelNodes;

	private Collection topGroups;

	private Hashtable groups;

	private IWorkbench workbench;

	/**
	 * Internal class used to sort all the preference page nodes
	 * based on the category.
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
		 * Initialize the flat category to include the parents'
		 * category names and the current node's label
		 */
		private void initialize() {
			String category = node.getCategory();
			if (category == null)
				return;

			StringBuffer sb = new StringBuffer();
			StringTokenizer stok = new StringTokenizer(category, PREFERENCE_SEPARATOR);
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
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes.get(i);
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}

	/**
	 * Searches for the child node with the given ID in the provided parent node.
	 * If not found, null is returned.
	 */
	private WorkbenchPreferenceNode findNode(WorkbenchPreferenceNode parent, String id) {
		IPreferenceNode[] subNodes = parent.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) subNodes[i];
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}

	/**
	 * Load the preference page contirbutions from the registry and
	 * organize preference node contributions by category into hierarchies
	 * If there is no page for a given node in the hierarchy then a blank
	 * page will be created.
	 * If no category has been specified or category information
	 * is incorrect, page will appear at the root level. workbench
	 * log entry will be created for incorrect category information.
	 */
	public void loadFromRegistry(IExtensionRegistry registry) {
		nodes = new ArrayList();
		groups = new Hashtable();

		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PREFERENCES);

		processNodes();
		processGroups();

	}

	/**
	 * Process the preference page nodes.
	 */
	private void processNodes() {
		topLevelNodes = new ArrayList();
		//root nodes (which contain subnodes)

		//Add root nodes to the contributions vector	
		StringTokenizer tokenizer;
		String currentToken;

		// Make the advisor's favorite the first category
		IPreferenceNode favorite = null;
		String favoriteId = ((Workbench) workbench).getMainPreferencePageId();
		if (favoriteId != null) {
			favorite = findNode(favoriteId);
		}
		if (favorite != null) {
			topLevelNodes.add(favorite);
		}

		// Sort nodes based on flattened display path composed of
		// actual labels of nodes referenced in category attribute.
		Object[] sortedNodes = sortByCategories(nodes);
		for (int i = 0; i < sortedNodes.length; i++) {
			//Iterate through all the nodes
			CategoryNode categoryNode = (CategoryNode) sortedNodes[i];
			WorkbenchPreferenceNode node = categoryNode.getNode();
			if (node == favorite) {
				// skip it - favorite already at the top of the list
				continue;
			}
			String category = node.getCategory();
			if (category == null) {
				topLevelNodes.add(node);
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
				//Could not find the parent - log
				WorkbenchPlugin
						.log("Invalid preference page path: " + categoryNode.getFlatCategory()); //$NON-NLS-1$
				topLevelNodes.add(node);
			}
		}
	}

	/**
	 * Read preference page element.
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_GROUP) == true)
			return readGroupElement(element);
		if (element.getName().equals(TAG_PAGE) == false)
			return false;
		WorkbenchPreferenceNode node = createNode(workbench, element);
		if (node != null)
			nodes.add(node);
		readElementChildren(element);
		return true;
	}

	/**
	 * Read an element that is a group.
	 * @param element
	 * @return boolean
	 */
	private boolean readGroupElement(IConfigurationElement element) {

		String name = element.getAttribute(ATT_NAME);
		String id = element.getAttribute(ATT_ID);
		String icon = element.getAttribute(ATT_ICON);
		String parent = element.getAttribute(ATT_PARENT_GROUP);

		Collection pageIds = readPages(element);

		ImageDescriptor descriptor = null;

		if (icon != null) {
			String contributingPluginId = element.getDeclaringExtension().getNamespace();
			descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(contributingPluginId, icon);
		}

		groups.put(id, new WorkbenchPreferenceGroup(id, name, parent, pageIds, descriptor));
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
			String contributingPluginId = element.getDeclaringExtension().getNamespace();
			image = AbstractUIPlugin.imageDescriptorFromPlugin(contributingPluginId, imageName);
		}
		WorkbenchPreferenceNode node = new WorkbenchPreferenceNode(id, name, category,
				image, element, workbench);
		return node;
	}

	/**
	 * Sort the nodes based on full category + name. Category used for sorting
	 * is created by substituting node IDs with labels of the referenced
	 * nodes. workbench node is excluded from sorting because it always
	 * appears first in the dialog.
	 */
	private Object[] sortByCategories(List categoryNodes) {
		//sort by categories
		CategoryNode[] nodeArray = new CategoryNode[categoryNodes.size()];

		for (int i = 0; i < categoryNodes.size(); i++) {
			nodeArray[i] = new CategoryNode((WorkbenchPreferenceNode) categoryNodes.get(i));
		}

		Collections.sort(Arrays.asList(nodeArray), comparer);
		return nodeArray;
	}

	/**
	 * Read the pages for the receiver from element.
	 * @param element
	 * @return Collection the ids of the children
	 */
	private Collection readPages(IConfigurationElement element) {
		IConfigurationElement[] pages = element.getChildren(TAG_PAGE);
		HashSet list = new HashSet();
		for (int i = 0; i < pages.length; i++) {
			IConfigurationElement page = pages[i];
			String id = page.getAttribute(ATT_ID);
			if (id != null)
				list.add(id);
		}

		return list;
	}

	/**
	 * Post process all of the groups.
	 * 
	 */
	private void processGroups() {

		Hashtable nodeToGroupMapping = new Hashtable();

		Iterator groupIterator = groups.values().iterator();

		while (groupIterator.hasNext()) {
			WorkbenchPreferenceGroup nextGroup = (WorkbenchPreferenceGroup) groupIterator.next();
			Iterator pages = nextGroup.getPageIds().iterator();
			while(pages.hasNext()){
				nodeToGroupMapping.put(pages.next(), nextGroup);
			}
		}

		Iterator nodeIterator = nodes.iterator();
		WorkbenchPreferenceGroup advanced = (WorkbenchPreferenceGroup) groups.get(ADVANCED_ID);

		while (nodeIterator.hasNext()) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodeIterator.next();
			if (nodeToGroupMapping.containsKey(node.getId())) {
				((WorkbenchPreferenceGroup) nodeToGroupMapping.get(node.getId())).addNode(node);
			}
			else if(advanced != null && topLevelNodes.contains(node))
					advanced.addNode(node);
				

		}

		organizeGroups();
	}

	/**
	 * Return the groupd in sorted and in tree order.
	 * 
	 * @return Collection of Group
	 */
	private void organizeGroups() {
		topGroups = new ArrayList();

		Iterator allGroups = groups.values().iterator();

		while (allGroups.hasNext()) {
			WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) allGroups.next();
			String parentId = group.getParent();
			if (parentId == null)
				topGroups.add(group);
			else {
				Object parent = groups.get(parentId);
				if (parent == null) {
					WorkbenchPlugin.log("Invalid category path: " + parentId); //$NON-NLS-1$
					topGroups.add(group);
				} else {
					((WorkbenchPreferenceGroup) parent).addChild(group);
				}
			}

		}

	}

	/**
	 * Return the top level groups.
	 * @return Collection of WorkbenchPreferenceGroup.
	 */
	public Collection getTopLevelGroups() {
		return topGroups;
	}

	/**
	 * Return the top level IPreferenceNodes.
	 * @return  Collection of IPreferenceNode.
	 */
	public Collection getTopLevelNodes() {
		return topLevelNodes;
	}
}