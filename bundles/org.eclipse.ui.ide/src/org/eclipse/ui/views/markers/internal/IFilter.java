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

package org.eclipse.ui.views.markers.internal;

/**
 * Generic filtering interface.
 */
public interface IFilter {

    /**
     * Filters the list of elements. Removes the elements that need to filtered out from the list.
     * 
     * @param elements
     */
    public Object[] filter(Object[] elements);

    /**
     * @param item
     * @return
     * <ul>
     * <li><code>true</code> if the item will make it through the filter.</li>
     * <li><code>false</code> if the item will not make it through the filter.</li>
     * </ul>
     */
    public boolean select(Object item);

}
