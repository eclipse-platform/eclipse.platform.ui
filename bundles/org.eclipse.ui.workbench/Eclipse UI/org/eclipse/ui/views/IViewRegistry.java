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
package org.eclipse.ui.views;


/**
 * The view registry maintains a list of views explicitly registered
 * against the view extension point..
 * <p>
 * The description of a given view is kept in a <code>IViewDescriptor</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.views.IViewDescriptor
 * @see org.eclipse.ui.views.IStickyViewDescriptor
 * @since 3.1
 */
public interface IViewRegistry {
    /**
     * Return a view descriptor with the given extension id.  If no view exists
     * with the id return <code>null</code>.
     * 
     * @param id the id to search for
     * @return the descriptor or <code>null</code>
     */
    public IViewDescriptor find(String id);

    /**
     * Returns an array of view categories.
     * 
     * @return the categories.  Never <code>null</code>.
     */
    public IViewCategory[] getCategories();

    /**
     * Return a list of views defined in the registry.
     * 
     * @return the views.  Never <code>null</code>.
     */
    public IViewDescriptor[] getViews();

    /**
     * Return a list of sticky views defined in the registry.
     * 
     * @return the sticky views.  Never <code>null</code>.
     */
    public IStickyViewDescriptor[] getStickyViews();
}
