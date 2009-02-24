/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;

/** 
 * {@link TreeModelViewer} content provider interface.
 *  
 * @since 3.5
 */
public interface ITreeModelContentProvider extends ILazyTreePathContentProvider {

    /**
     * Translates and returns the given child index from the viewer coordinate
     * space to the model coordinate space.
     *  
     * @param parentPath path to parent element
     * @param index index of child element in viewer (filtered) space
     * @return index of child element in model (raw) space
     */
    public int viewToModelIndex(TreePath parentPath, int index);
    
    /**
     * Translates and returns the given child count from the viewer coordinate
     * space to the model coordinate space.
     *  
     * @param parentPath path to parent element
     * @param count number of child elements in viewer (filtered) space
     * @return number of child elements in model (raw) space
     */
    public int viewToModelCount(TreePath parentPath, int count);
    
    /**
     * Translates and returns the given child index from the model coordinate
     * space to the viewer coordinate space.
     *  
     * @param parentPath path to parent element
     * @param index index of child element in model (raw) space
     * @return index of child element in viewer (filtered) space or -1 if filtered
     */
    public int modelToViewIndex(TreePath parentPath, int index);

    /**
     * Returns whether the given element is filtered.
     * 
     * @param parentElementOrTreePath
     *            the parent element or path
     * @param element
     *            the child element
     * @return whether to filter the element
     */
    public boolean shouldFilter(Object parentElementOrTreePath, Object element);   

    /**
     * Notification the given element is being unmapped.
     * 
     * @param path
     */
    public void unmapPath(TreePath path);

    /**
     * Turns on the mode which causes the model viewer to ignore SELECT, 
     * EXPAND, and COLLAPSE flags of {@link IModelDelta}.
     *  
     * @param suppress If <code>true</code> it turns on the suppress mode.
     */
    public void setSuppressModelControlDeltas(boolean suppress);
    
    /**
     * Returns true if the viewer is currently in the mode to ignore SELECT, 
     * REVEAL, EXPAND, and COLLAPSE flags of {@link IModelDelta}.
     *  
     * @return Returns <code>true</code> if in suppress mode.
     */
    public boolean isSuppressModelControlDeltas();
    
    /**
     * Translates and returns the given child count from the model coordinate
     * space to the viewer coordinate space.
     *  
     * @param parentPath path to parent element
     * @param count child count element in model (raw) space
     * @return child count in viewer (filtered) space
     */
    public int modelToViewChildCount(TreePath parentPath, int count);

    /**
     * Registers the specified listener for view update notifications.
     */
    public void addViewerUpdateListener(IViewerUpdateListener listener);
    
    /**
     * Removes the specified listener from update notifications.
     */
    public void removeViewerUpdateListener(IViewerUpdateListener listener);
    
    /**
     * Registers the given listener for model delta notification.
     * This listener is called immediately after the viewer processes
     * the delta.  
     */
    public void addModelChangedListener(IModelChangedListener listener);
    
    /**
     * Removes the given listener from model delta notification.
     */
    public void removeModelChangedListener(IModelChangedListener listener);
    
    /**
     * Instructs the content provider to process the given model delta.  This
     * mechanism can be used to control the view's layout (expanding, selecting
     * , etc.) 
     * 
     * @param delta The model delta to process.
     */
    public void updateModel(IModelDelta delta);
}
