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
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.activities.IObjectActivityManager;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ICategory;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.roles.IDERoleManager;

public class ViewContentProvider implements ITreeContentProvider {
	/**
	 * Create a new instance of the ViewContentProvider.
	 */
	public ViewContentProvider() {
		super();
	}
	public void dispose() {
	}
	/**
	 * Returns the child elements of the given parent element.
	 */
	public Object[] getChildren(Object element) {
		if (element instanceof IViewRegistry) {
			IViewRegistry reg = (IViewRegistry) element;
			ICategory[] categories = reg.getCategories();

            IObjectActivityManager objectManager = 
            	WorkbenchPlugin.getDefault()
            		.getWorkbench()
            		.getActivityManager(
            			IWorkbenchConstants.PL_VIEWS, false);
            if (objectManager != null) {
                ArrayList filtered = new ArrayList();
                Collection activeObjects = objectManager.getActiveObjects();                
    			for (int i = 0; i < categories.length; i++) {
                    if (activeObjects.contains(IDERoleManager.createViewCategoryIdKey(categories[i].getId()))) {
                        filtered.add(categories[i]);
                    }
    			}
    			return filtered.toArray();
            }
            return categories;
		} else if (element instanceof ICategory) {
			ArrayList list = ((ICategory) element).getElements();            
			if (list != null) {

				IObjectActivityManager objectManager = 
					WorkbenchPlugin.getDefault()
						.getWorkbench()
						.getActivityManager(
							IWorkbenchConstants.PL_VIEWS, false);              
                if (objectManager != null) {
					Collection activeObjects = objectManager.getActiveObjects();
                    ArrayList filtered = new ArrayList();
                    for (Iterator i = list.iterator(); i.hasNext();) {
                        IViewDescriptor desc = (IViewDescriptor) i.next();
                        if (activeObjects.contains(desc.getId())) {
                            filtered.add(desc);
                        }
                    }
                    return filtered.toArray();                    
                }
                return list.toArray();                
			}

		} else {
			return new Object[0];
		}

		return new Object[0];
	}
	/**
	 * Return the children of an element.
	 */
	public Object[] getElements(Object element) {
		return getChildren(element);
	}
	/**
	 * Returns the parent for the given element, or <code>null</code> 
	 * indicating that the parent can't be computed. 
	 */
	public Object getParent(Object element) {
		return null;
	}
	/**
	 * Returns whether the given element has children.
	 */
	public boolean hasChildren(java.lang.Object element) {
		if (element instanceof IViewRegistry)
			return true;
		else if (element instanceof ICategory)
			return true;
		return false;
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	public boolean isDeleted(Object element) {
		return false;
	}
}
