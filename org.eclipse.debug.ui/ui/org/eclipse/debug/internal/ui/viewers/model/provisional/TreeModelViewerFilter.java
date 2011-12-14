/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter for the Tree Model Viewer which allows more efficient filtering
 * in the lazy viewer.
 * <p>
 * The standard {@link ViewerFilter} class must be applied to all elements in the 
 * tree, thus forcing the lazy viewer to retrieve all children of all elements and
 * defeating the lazy loading behavior.  This class adds an {@link #isApplicable(ITreeModelViewer, Object)}
 * method, which can be used by the filter to discern which parent elements the 
 * filter should apply to. 
 * </p> 
 * 
 * @since 3.8
 */
abstract public class TreeModelViewerFilter extends ViewerFilter {

    /**
     * Determines whether the filter applies to the given parent element.
     * @return Returns true if the viewer should use the given filter on the 
     * given element.
     * @param viewer The viewer that is using this filter to select elements. 
     * @param parentElement Parent element to check filter for.
     */
    abstract public boolean isApplicable(ITreeModelViewer viewer, Object parentElement);
}
