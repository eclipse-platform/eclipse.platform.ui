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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Category provides for hierarchical grouping of elements
 * registered in the registry. One extension normally defines
 * a category, and other reference it via its ID.
 * <p>
 * A category may specify its parent category in order to
 * achieve hierarchy.
 * </p>
 */
public class Category implements IWorkbenchAdapter, IPluginContribution {
	/**
	 * Name of the miscellaneous category
	 */
	public final static String MISC_NAME = WorkbenchMessages.getString("ICategory.other"); //$NON-NLS-1$
	
	/**
	 * Identifier of the miscellaneous category
	 */
	public final static String MISC_ID = "org.eclipse.ui.internal.otherCategory"; //$NON-NLS-1$
	
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_PARENT = "parentCategory"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	
	private String id;
    private String pluginId;    
	private String name;
	private String[] parentPath;
	private String unparsedPath;
	private ArrayList elements;
    private IConfigurationElement configurationElement;
	
	/**
	 * Creates an instance of <code>Category</code> as a
	 * miscellaneous category.
	 */
	public Category() {
		this.id = MISC_ID;
		this.name = MISC_NAME;
		this.pluginId = MISC_ID;  // TODO: remove hack for bug 55172
	}
	
	/**
	 * Creates an instance of <code>Category</code> with
	 * an ID and label.
	 * 
	 * @param id the unique identifier for the category
	 * @param label the presentation label for this category
	 */
	public Category(String id, String label) {
		this.id = id;
		this.name = label;
	}
	
	/**
	 * Creates an instance of <code>Category</code> using the
	 * information from the specified configuration element.
	 * 
	 * @param configElement the <code>IConfigurationElement<code> containing
	 * 		the ID, label, and optional parent category path.
	 * @throws a <code>WorkbenchException</code> if the ID or label is <code>null</code
	 */
	public Category(IConfigurationElement configElement) throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
        pluginId = configElement.getDeclaringExtension().getNamespace();
		name = configElement.getAttribute(ATT_NAME);
		unparsedPath = configElement.getAttribute(ATT_PARENT);
		configurationElement = configElement;
		if (id == null || name == null)
			throw new WorkbenchException("Invalid category: " + id); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public void addElement(Object element) {
		if (elements == null)
			elements = new ArrayList(5);
		elements.add(element);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAdaptable.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) 
			return this;
        else if (adapter == IConfigurationElement.class) 
            return configurationElement;
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public Object[] getChildren(Object o) {
		return getElements().toArray();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public String getLabel(Object o) {
		return getLabel();
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getLabel() {
		return name;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String[] getParentPath() {
		if (unparsedPath != null) {
			StringTokenizer stok = new StringTokenizer(unparsedPath, "/"); //$NON-NLS-1$
			parentPath = new String[stok.countTokens()];
			for (int i = 0; stok.hasMoreTokens(); i++) {
				parentPath[i] = stok.nextToken();
			}
			unparsedPath = null;
		}
		
		return parentPath;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getRootPath() {
		String[] path = getParentPath();
		if (path != null && path.length > 0)
			return path[0];
		else
			return id;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public ArrayList getElements() {
		return elements;
	}
	
	public boolean hasElement(Object o) {
		if (elements == null)
			return false;
		if (elements.isEmpty())
			return false;
		return elements.contains(o);
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public boolean hasElements() {
		if (elements != null)
			return !elements.isEmpty();
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#fromPlugin()
	 */
	public boolean fromPlugin() {
		return pluginId != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {		
		return pluginId;
	}
}
