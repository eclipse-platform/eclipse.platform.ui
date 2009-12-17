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
package org.eclipse.ui.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Abstract base class with basic implementations of the IWorkbenchAdapter
 * interface. Intended to be subclassed.
 * 
 * @since 3.0
 */
public abstract class WorkbenchAdapter implements IWorkbenchAdapter,
        IWorkbenchAdapter2 {
    /**
     * The empty list of children.
     */
    protected static final Object[] NO_CHILDREN = new Object[0];

    /**
     * The default implementation of this <code>IWorkbenchAdapter</code> method
     * returns the empty list. Subclasses may override.
     */
    public Object[] getChildren(Object object) {
        return NO_CHILDREN;
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter</code> method
     * returns <code>null</code>. Subclasses may override.
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter</code> method
     * returns the empty string if the object is <code>null</code>, and
     * the object's <code>toString</code> otherwise. Subclasses may override.
     */
    public String getLabel(Object object) {
        return object == null ? "" : object.toString(); //$NON-NLS-1$
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter</code> method
     * returns <code>null</code>. Subclasses may override.
     */
    public Object getParent(Object object) {
        return null;
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter2</code> method
     * returns <code>null</code>. Subclasses may override.
     */
    public RGB getBackground(Object element) {
        return null;
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter2</code> method
     * returns <code>null</code>. Subclasses may override.
     */
    public RGB getForeground(Object element) {
        return null;
    }

    /**
     * The default implementation of this <code>IWorkbenchAdapter2</code> method
     * returns <code>null</code>. Subclasses may override.
     */
    public FontData getFont(Object element) {
        return null;
    }
}
