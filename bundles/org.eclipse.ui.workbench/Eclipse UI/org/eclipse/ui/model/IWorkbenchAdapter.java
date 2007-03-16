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

/**
 * This adapter interface provides visual presentation and hierarchical structure
 * for workbench elements, allowing them to be displayed in the UI
 * without having to know the concrete type of the element.
 * <p>
 * There is an associate label provider and content provider for showing
 * elements with a registered workbench adapter in JFace structured viewers.
 * </p>
 * @see WorkbenchLabelProvider
 * @see BaseWorkbenchContentProvider
 */
public interface IWorkbenchAdapter {
    /**
     * Returns the children of this object.  When this object
     * is displayed in a tree, the returned objects will be this
     * element's children.  Returns an empty array if this
     * object has no children.
     *
     * @param o The object to get the children for.
     * @return Object[]
     */
    public Object[] getChildren(Object o);

    /**
     * Returns an image descriptor to be used for displaying an object in the workbench.
     * Returns <code>null</code> if there is no appropriate image.
     *
     * @param object The object to get an image descriptor for.
     * @return ImageDescriptor
     */
    public ImageDescriptor getImageDescriptor(Object object);

    /**
     * Returns the label text for this element.  This is typically
     * used to assign a label to this object when displayed
     * in the UI.  Returns an empty string if there is no appropriate
     * label text for this object.
     *
     * @param o The object to get a label for.
     * @return String
     */
    public String getLabel(Object o);

    /**
     * Returns the logical parent of the given object in its tree.
     * Returns <code>null</code> if there is no parent, or if this object doesn't
     * belong to a tree.
     *
     * @param o The object to get the parent for.
     * @return Object
     */
    public Object getParent(Object o);
}
