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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The default implementation of the interface <code>ICategory</code>.
 * 
 * @see ICategory
 */
public class Category extends WorkbenchAdapter implements ICategory {
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_PARENT = "parentCategory"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	
	private String id;
	private String name;
	private String[] parentPath;
	private String unparsedPath;
	private ArrayList elements;
	
	/**
	 * Creates an instance of <code>Category</code> as a
	 * miscellaneous category.
	 */
	public Category() {
		this.id = MISC_ID;
		this.name = MISC_NAME;
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
		name = configElement.getAttribute(ATT_NAME);
		unparsedPath = configElement.getAttribute(ATT_PARENT);
		
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
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public boolean hasElements() {
		if (elements != null)
			return !elements.isEmpty();
		else
			return false;
	}
}
