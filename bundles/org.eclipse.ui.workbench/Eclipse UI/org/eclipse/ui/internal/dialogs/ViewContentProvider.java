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
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.activities.ws.FilterableObject;
import org.eclipse.ui.activities.ws.WorkbenchActivityHelper;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.IViewRegistry;

/**
 * Provides content for viewers that wish to show Views.
 */
public class ViewContentProvider
	extends FilterableObject
	implements ITreeContentProvider {

	/**
	 * Create a new instance of the ViewContentProvider.
	 * 
	 * @param filtering
	 *            the initial filtering state.
	 */
	public ViewContentProvider(boolean filtering) {
		super(filtering);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (element instanceof IViewRegistry) {
			IViewRegistry reg = (IViewRegistry) element;
			Category[] categories = reg.getCategories();

			if (getFiltering()) {
				ArrayList filtered = new ArrayList();
				for (int i = 0; i < categories.length; i++) {
                    if (WorkbenchActivityHelper.filterItem(categories[i]))
						continue;

					filtered.add(categories[i]);
				}
				categories =
					(Category[]) filtered.toArray(
						new Category[filtered.size()]);
			}
            
			// if there is only one category, return it's children directly
			if (categories.length == 1) {
				return getChildren(categories[0]);
			}
			return categories;
		} else if (element instanceof Category) {
			ArrayList list = ((Category) element).getElements();
			if (list != null) {
				if (getFiltering()) {
					ArrayList filtered = new ArrayList();
					for (Iterator i = list.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (WorkbenchActivityHelper.filterItem(o))
							continue;
						filtered.add(o);
					}
					return filtered.toArray();
				}
				return list.toArray();
			}

		}
        
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(java.lang.Object element) {
		if (element instanceof IViewRegistry)
			return true;
		else if (element instanceof Category)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
