/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.preference.*;
import java.text.Collator;
import java.util.*;
import org.eclipse.ui.internal.misc.Sorter;

/**
 *  Instances access the registry that is provided at creation time in order
 *  to determine the contributed preference pages
 */
public class PreferencePageRegistryReader extends RegistryReader {
	public static final String ATT_CATEGORY = "category";//$NON-NLS-1$
	public static final String ATT_CLASS = "class";//$NON-NLS-1$
	public static final String ATT_NAME = "name";//$NON-NLS-1$
	public static final String ATT_ID = "id";//$NON-NLS-1$
	public static final String TAG_PAGE = "page";//$NON-NLS-1$
	public static final String ATT_ICON = "icon";//$NON-NLS-1$
	public static final String PREFERENCE_SEPARATOR = "/";//$NON-NLS-1$
	private List nodes;
	private IWorkbench workbench;

	/*
	 * Internal class used to sort all the preference page nodes
	 * based on the category.
	 */
	class CategoryNode {
		private WorkbenchPreferenceNode node;
		private String flatCategory;
		/*
		 * Default constructor
		 */
		public CategoryNode(WorkbenchPreferenceNode node) {
			this.node = node;
		}
		/*
		 * Return the preference node this category represents
		 */
		public WorkbenchPreferenceNode getNode() {
			return node;
		}
		/*
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
	IPreferenceNode[] nodes = parent.getSubNodes();
	for (int i = 0; i < nodes.length; i++) {
		WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes[i];
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
public List getPreferenceContributions(IPluginRegistry registry) {
	loadNodesFromRegistry(registry); //all nodes keyed on category
	List contributions = new ArrayList(); //root nodes (which contain subnodes)

	//Add root nodes to the contributions vector	
	StringTokenizer tokenizer;
	String currentToken;
	IPreferenceNode workbenchNode;

	//Make the workbench preferences the first category
	workbenchNode = findNode(IWorkbenchConstants.WORKBENCH_PREFERENCE_CATEGORY_ID);
	if (workbenchNode == null) {
		//We must create a page for the workbench node (unlikely to occur but just in case)
		workbenchNode = new WorkbenchPreferenceNode(
			IWorkbenchConstants.WORKBENCH_PREFERENCE_CATEGORY_ID,
			"Workbench",//$NON-NLS-1$
			null,
			null,
			new EmptyPreferencePage());
		nodes.add(workbenchNode);
	}
	contributions.add(workbenchNode);

	// Sort nodes based on flattened display path composed of
	// actual labels of nodes referenced in category attribute.
	Object [] sortedNodes = sortByCategories(nodes);
	for (int i = 0; i < sortedNodes.length; i++) {
		//Iterate through all the nodes
		CategoryNode categoryNode = (CategoryNode)sortedNodes[i];
		WorkbenchPreferenceNode node = categoryNode.getNode();
		if (node == workbenchNode) continue;
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
			}
			else {
				parent = child;
			}
		}
		if (parent != null) {
			parent.add(node);
		}
		else {
			//Could not find the parent - log
			WorkbenchPlugin.log("Invalid preference page path: "+categoryNode.getFlatCategory());//$NON-NLS-1$
			contributions.add(node);
		}
	}
	return contributions;
}
/**
 * Get the preference nodes that are defined in the registry
 */
protected void loadNodesFromRegistry(IPluginRegistry registry) {
	nodes = new ArrayList();
	readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PREFERENCES);
}
/**
 * Read preference page element.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_PAGE) == false)
		return false;
	String name = element.getAttribute(ATT_NAME);
	String id = element.getAttribute(ATT_ID);
	String category = element.getAttribute(ATT_CATEGORY);
	String imageName = element.getAttribute(ATT_ICON);
	String className = element.getAttribute(ATT_CLASS);
	if (name==null) {
		logMissingAttribute(element, ATT_NAME);
	}
	if (id==null) {
		logMissingAttribute(element, ATT_ID);
	}
	if (className==null) {
		logMissingAttribute(element, ATT_CLASS);
	}
	if (name==null || id == null || className == null) {
		return true;
	}
	ImageDescriptor image = null;
	if (imageName != null) {
		image = WorkbenchImages.getImageDescriptorFromPlugin(element.getDeclaringExtension().getDeclaringPluginDescriptor(), imageName);
	}
	WorkbenchPreferenceNode node = new WorkbenchPreferenceNode(id, name, category, image, element, workbench);
	nodes.add(node);
	readElementChildren(element);
	return true;
}
/**
 * Sort the nodes based on full category + name. Category used for sorting
 * is created by substituting node IDs with labels of the referenced
 * nodes. workbench node is excluded from sorting because it always
 * appears first in the dialog.
 */
private Object[] sortByCategories(List nodes) {
	//sort by categories
	CategoryNode [] nodeArray = new CategoryNode [nodes.size()];

	for (int i=0; i<nodes.size(); i++) {
		nodeArray[i] = new CategoryNode((WorkbenchPreferenceNode)nodes.get(i));
	}

	Sorter sorter = new Sorter() {
		private Collator collator = Collator.getInstance();
		
		public boolean compare(Object o1, Object o2) {
			String s1 = ((CategoryNode)o1).getFlatCategory();
			String s2 = ((CategoryNode)o2).getFlatCategory();
			//Return true if elementTwo is 'greater than' elementOne
			return collator.compare(s2, s1) > 0;
		}
	};
	return sorter.sort(nodeArray);
}
}
