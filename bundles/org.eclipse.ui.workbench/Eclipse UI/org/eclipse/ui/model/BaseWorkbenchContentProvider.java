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
package org.eclipse.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tree content provider for objects that can be adapted to the interface
 * {@link org.eclipse.ui.model.IWorkbenchAdapter IWorkbenchAdapter}.
 * <p>
 * This class may be instantiated, or subclassed. If subclassed, the following
 * </p>
 * 
 * @see IWorkbenchAdapter
 * @since 3.0
 */
public class BaseWorkbenchContentProvider implements ITreeContentProvider {

    /**
     * Creates a new workbench content provider.
     *
     */
    public BaseWorkbenchContentProvider() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void dispose() {
        // do nothing
    }

    /**
     * Returns the implementation of IWorkbenchAdapter for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     * <p>
     * </p>
     * 
     * @param element the element
     * @return the corresponding workbench adapter object
     */
    protected IWorkbenchAdapter getAdapter(Object element) {
        if (!(element instanceof IAdaptable)) {
            return null;
        }
        return (IWorkbenchAdapter) ((IAdaptable) element)
                .getAdapter(IWorkbenchAdapter.class);
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public Object[] getChildren(Object element) {
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter != null) {
            return adapter.getChildren(element);
        }
        return new Object[0];
    }

    /* (non-Javadoc)
     * Method declared on IStructuredContentProvider.
     */
    public Object[] getElements(Object element) {
        return getChildren(element);
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public Object getParent(Object element) {
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter != null) {
            return adapter.getParent(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
    }

}