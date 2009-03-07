/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.views.framelist.FrameList;

/**
 * This interface defines the API for the resource navigator.
 * The action groups should restrict themselves to using this API.
 * <p>
 * This interface is not intended to be implemented by clients.
 * Subclass <code>org.eclipse.ui.views.ResourceNavigator</code> 
 * instead.   
 * </p>
 * 
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public interface IResourceNavigator extends IViewPart {

    /**
     * Returns the pattern filter.
     *
     * @return the pattern filter
     */
    ResourcePatternFilter getPatternFilter();

    /**
     * Returns the active working set, or <code>null<code> if none.
     *
     * @return the active working set, or <code>null<code> if none
     * @since 2.0
     */
    IWorkingSet getWorkingSet();

    /**
     * Returns the current sorter.
     * @return the resource navigator's sorter
     * 
     * @deprecated as of 3.3, use {@link IResourceNavigator#getComparator()} instead
     */
    ResourceSorter getSorter();

    /**
     * Sets the current sorter.
     * @param sorter the sorter to use
     * 
     * @deprecated as of 3.3, use {@link IResourceNavigator#setComparator(ResourceComparator)} instead
     */
    void setSorter(ResourceSorter sorter);

    /**
     * Returns the current comparator.
     * 
     * @return the resource navigator's comparator
     * @since 3.3
     */
    ResourceComparator getComparator();

    /**
     * Sets the current comparator.
     * 
     * @param comparator the comparator to use
     * @since 3.3
     */
    void setComparator(ResourceComparator comparator);
    
    /**
     * Sets the values of the filter preference to be the 
     * strings in preference values.
     * @param patterns filter patterns to use on contents of the resource navigator
     */
    void setFiltersPreference(String[] patterns);

    /**
     * Returns the viewer which shows the resource tree.
     * @return the resource navigator's tree viewer
     */
    TreeViewer getViewer();

    /**
     * Returns the frame list for this navigator.
     * @return the list of frames maintained by the resource navigator 
     */
    FrameList getFrameList();

    /**
     * Returns whether this navigator's selection automatically tracks the active editor.
     * 
     * @return <code>true</code> if linking is enabled, <code>false</code> if not
     * @since 2.1
     */
    boolean isLinkingEnabled();

    /**
     * Sets the working set for this view, or <code>null</code> to clear it.
     * 
     * @param workingSet the working set, or <code>null</code> to clear it
     * @since 2.0
     */
    void setWorkingSet(IWorkingSet workingSet);

    /**
     * Sets whether this navigator's selection automatically tracks the active editor.
     * 
     * @param enabled <code>true</code> to enable, <code>false</code> to disable
     * @since 2.1
     */
    void setLinkingEnabled(boolean enabled);
}
