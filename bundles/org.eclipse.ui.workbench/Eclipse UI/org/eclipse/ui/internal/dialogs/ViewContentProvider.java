/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Provides content for viewers that wish to show Views.
 */
public class ViewContentProvider implements ITreeContentProvider {

    /**
     * Child cache.  Map from Object->Object[].  Our hasChildren() method is 
     * expensive so it's better to cache the results of getChildren().
     */
    private Map childMap = new HashMap();

    /**
     * Create a new instance of the ViewContentProvider.
     */
    public ViewContentProvider() {
        //no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        childMap.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object element) {
        Object[] children = (Object[]) childMap.get(element);
        if (children == null) {
            children = createChildren(element);
            childMap.put(element, children);
        }
        return children;
    }

    /**
     * Does the actual work of getChildren.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    private Object[] createChildren(Object element) {
        if (element instanceof IViewRegistry) {
            IViewRegistry reg = (IViewRegistry) element;
            IViewCategory [] categories = reg.getCategories();

            ArrayList filtered = new ArrayList();
            for (int i = 0; i < categories.length; i++) {
                if (!hasChildren(categories[i])) {
					continue;
				}

                filtered.add(categories[i]);
            }
            categories = (IViewCategory[]) filtered.toArray(new IViewCategory[filtered
                    .size()]);

            // if there is only one category, return it's children directly
            if (categories.length == 1) {
                return getChildren(categories[0]);
            }
            return categories;
        } else if (element instanceof IViewCategory) {
            IViewDescriptor [] views = ((IViewCategory) element).getViews();
            if (views != null) {
                ArrayList filtered = new ArrayList();
                for (int i = 0; i < views.length; i++) {
                    Object o = views[i];
                    if (WorkbenchActivityHelper.filterItem(o)) {
						continue;
					}
                    filtered.add(o);
                }
                return removeIntroView(filtered).toArray();
            }
        }

        return new Object[0];
    }

    /**
     * Removes the temporary intro view from the list so that it cannot be activated except through
     * the introduction command.
     *  
     * @param list the list of view descriptors
     * @return the modified list.
     * @since 3.0
     */
    private ArrayList removeIntroView(ArrayList list) {
        for (Iterator i = list.iterator(); i.hasNext();) {
            IViewDescriptor view = (IViewDescriptor) i.next();
            if (view.getId().equals(IIntroConstants.INTRO_VIEW_ID)) {
                i.remove();
            }
        }
        return list;
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
        if (element instanceof IViewRegistry) {
			return true;
		} else if (element instanceof IViewCategory) {
            if (getChildren(element).length > 0) {
				return true;
			}
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        childMap.clear();
    }
}
