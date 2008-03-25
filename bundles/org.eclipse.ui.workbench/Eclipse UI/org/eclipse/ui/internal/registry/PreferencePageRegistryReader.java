/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;

/**
 *  Instances access the registry that is provided at creation time in order
 *  to determine the contributed preference pages
 */
public class PreferencePageRegistryReader extends CategorizedPageRegistryReader {

	private static final String TAG_PAGE = "page"; //$NON-NLS-1$

	private List nodes;

	private IWorkbench workbench;
	

	class PreferencesCategoryNode extends CategoryNode {

		WorkbenchPreferenceNode node;

		/**
		 * Create a new instance of the receiver.
		 * @param reader
		 * @param nodeToCategorize
		 */
		public PreferencesCategoryNode(CategorizedPageRegistryReader reader,
				WorkbenchPreferenceNode nodeToCategorize) {
			super(reader);
			this.node = nodeToCategorize;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText()
		 */
		String getLabelText() {
			return node.getLabelText();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText(java.lang.Object)
		 */
		String getLabelText(Object element) {
			return ((WorkbenchPreferenceNode) element).getLabelText();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getNode()
		 */
		Object getNode() {
			return node;
		}
	}

	/**
	 * Create a new instance configured with the workbench
	 * 
	 * @param newWorkbench the workbench
	 */
	public PreferencePageRegistryReader(IWorkbench newWorkbench) {
		workbench = newWorkbench;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.String)
	 */
	Object findNode(String id) {
		for (int i = 0; i < nodes.size(); i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes.get(i);
			if (node.getId().equals(id)) {
				return node;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.Object, java.lang.String)
	 */
	Object findNode(Object parent, String currentToken) {
		IPreferenceNode[] subNodes = ((WorkbenchPreferenceNode) parent).getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) subNodes[i];
			if (node.getId().equals(currentToken)) {
				return node;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#add(java.lang.Object, java.lang.Object)
	 */
	void add(Object parent, Object node) {
		((IPreferenceNode) parent).add((IPreferenceNode) node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#createCategoryNode(org.eclipse.ui.internal.registry.CategorizedPageRegistryReader, java.lang.Object)
	 */
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object) {
		return new PreferencesCategoryNode(reader, (WorkbenchPreferenceNode) object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getCategory(java.lang.Object)
	 */
	String getCategory(Object node) {
		return ((WorkbenchPreferenceNode) node).getCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getNodes()
	 */
	Collection getNodes() {
		return nodes;
	}

	/**
	 * Load the preference page contirbutions from the registry and
	 * organize preference node contributions by category into hierarchies
	 * If there is no page for a given node in the hierarchy then a blank
	 * page will be created.
	 * If no category has been specified or category information
	 * is incorrect, page will appear at the root level. workbench
	 * log entry will be created for incorrect category information.
	 * 
	 * @param registry the extension registry
	 */
	public void loadFromRegistry(IExtensionRegistry registry) {
		nodes = new ArrayList();

		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_PREFERENCES);

		processNodes();

	}

	/**
	 * Read preference page element.
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_PAGE) == false) {
			return false;
		}
		WorkbenchPreferenceNode node = createNode(element);
		if (node != null) {
			if (node.getId().equals(((Workbench) workbench).getMainPreferencePageId()))
				node.setPriority(-1);
			nodes.add(node);
		}
		return true;
	}

	/**
	 * Create a workbench preference node.
	 * @param element
	 * @return WorkbenchPreferenceNode
	 */
	public static WorkbenchPreferenceNode createNode(IConfigurationElement element) {
		boolean nameMissing = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) == null;
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);		
		boolean classMissing = getClassValue(element, IWorkbenchRegistryConstants.ATT_CLASS) == null;

		if (nameMissing) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_NAME);
		}
		if (id == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_ID);
		}
		if (classMissing) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_CLASS);
		}
		
		if (nameMissing || id == null || classMissing) {
			return null;
		}

		WorkbenchPreferenceNode node = new WorkbenchPreferenceNode(id, element);
		return node;
	}

	/**
	 * Return the top level IPreferenceNodes, minus the one which fail the
	 * Expression check.
	 * @return  Collection of IPreferenceNode.
	 */
	public Collection getTopLevelNodes() {
		return WorkbenchActivityHelper.restrictCollection(topLevelNodes, new ArrayList());
	}
}
