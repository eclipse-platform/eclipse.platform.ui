/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A list of adaptable objects.  This is a generic list that can
 * be used to display an arbitrary set of adaptable objects in the workbench.
 * Also implements the IWorkbenchAdapter interface for simple display
 * and navigation.
 */
public class AdaptableList implements IWorkbenchAdapter, IAdaptable {
    protected List children = new ArrayList();

    /**
     * Creates a new adaptable list with the given children.
     */
    public AdaptableList() {
        // do nothing
    }

    /**
     * Creates a new adaptable list with the given children.
     */
    public AdaptableList(IAdaptable[] newChildren) {
        for (int i = 0; i < newChildren.length; i++) {
            children.add(newChildren[i]);
        }
    }

    /**
     * Adds all the adaptable objects in the given enumeration to this list.
     * Returns this list.
     */
    public AdaptableList add(Iterator iterator) {
        while (iterator.hasNext()) {
            add((IAdaptable) iterator.next());
        }
        return this;
    }

    /**
     * Adds the given adaptable object to this list.
     * Returns this list.
     */
    public AdaptableList add(IAdaptable adaptable) {
        children.add(adaptable);
        return this;
    }

    /* (non-Javadoc)
     * Method declared on IAdaptable
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class)
            return this;
        return null;
    }

    /**
     * Returns the elements in this list.
     */
    public Object[] getChildren() {
        return children.toArray();
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public Object[] getChildren(Object o) {
        return children.toArray();
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public String getLabel(Object object) {
        return object == null ? "" : object.toString(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public Object getParent(Object object) {
        return null;
    }

    /**
     * Removes the given adaptable object from this list.
     */
    public void remove(IAdaptable adaptable) {
        children.remove(adaptable);
    }

    /**
     * Returns the number of items in the list
     */
    public int size() {
        return children.size();
    }
}
