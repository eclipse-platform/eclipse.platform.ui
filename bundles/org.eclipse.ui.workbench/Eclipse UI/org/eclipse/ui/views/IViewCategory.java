/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views;

import org.eclipse.core.runtime.IPath;

/**
 * Represents a categorization of views.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.1
 */
public interface IViewCategory {

    /**
     * Return the views contained within this category. Never <code>null</code>,
     * but may be empty.
     * 
     * @return the views contained within this category
     */
    IViewDescriptor[] getViews();

    /**
     * Return the id of this category.  Never <code>null</code>.
     * 
     * @return the id
     */
    String getId();

    /**
     * Return the human readable name of this view category.  Never <code>null</code>.
     * 
     * @return the label
     */
    String getLabel();

    /**
     * Return this categories path. The segments of this path will correspond to
     * category ids.
     * 
     * @return the path
     */
    IPath getPath();
}
