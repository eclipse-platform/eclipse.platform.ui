/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

/** 
 * {@link TreeModelViewer} content provider interface.
 *  
 * @since 3.5
 */
public interface ITreeModelContentProvider extends ILazyTreePathContentProvider {

    /**
     * Bit-mask which allows all possible model delta flags.
     * 
     * @since 3.6
     * @see #setModelDeltaMask(int)
     */
    public static final int ALL_MODEL_DELTA_FLAGS = ~0; 

    /**
     * Bit-mask which allows only flags which control selection and expansion. 
     * 
     * @since 3.6
     * @see #setModelDeltaMask(int)
     */
    public static final int CONTROL_MODEL_DELTA_FLAGS = 
        IModelDelta.EXPAND | IModelDelta.COLLAPSE | IModelDelta.SELECT | IModelDelta.REVEAL | IModelDelta.FORCE;

    /**
     * Bit-mask which allows only flags which update viewer's information
     * about the model.
     * 
     * @since 3.6
     * @see #setModelDeltaMask(int)
     */
    public static final int UPDATE_MODEL_DELTA_FLAGS = 
        IModelDelta.ADDED | IModelDelta.CONTENT | IModelDelta.INSERTED | IModelDelta.INSTALL | IModelDelta.REMOVED |
        IModelDelta.REPLACED | IModelDelta.STATE | IModelDelta.UNINSTALL;

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
     * Sets the bit mask which will be used to filter the {@link IModelDelta}
     * coming from the model.  Any delta flags which are hidden by the mask
     * will be ignored.
     *  
     * @param the bit mask for <code>IModelDelta</code> flags
     * 
     * @since 3.6
     */
    public void setModelDeltaMask(int mask);
    
    /**
     * Returns the current model delta mask.
     * 
     * @return bit mask used to filter model delta events.
     * 
     * @see #setModelDeltaMask(int)
     * @since 3.6
     */
    public int getModelDeltaMask();
    
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
     * Registers the specified listener for state update notifications.
     * @since 3.6
     */
    public void addStateUpdateListener(IStateUpdateListener listener);

    /**
     * Removes the specified listener from state update notifications.
     * @since 3.6
     */
    public void removeStateUpdateListener(IStateUpdateListener listener);

    /**
     * Instructs the content provider to process the given model delta.  This
     * mechanism can be used to control the view's layout (expanding, selecting
     * , etc.) 
     * 
     * @param delta The model delta to process.
     * @param mask Mask that can be used to suppress processing of some of the 
     * delta flags
     * 
     * @since 3.6
     */
    public void updateModel(IModelDelta delta, int mask);

    /**
     * Instructs the content provider to cancel any pending state changes 
     * (i.e. SELECT, REVEAL, EXPAND, COLLAPSE) for the given path.  Pending
     * state changes are changes the the viewer plans to re-apply to the 
     * viewer following a refresh.  If the user changes viewer state while
     * the viewer is being refreshed, user's change should override the 
     * previous state. 
     * 
     * @param path Path of the element for which to cancel pending changes.
     * @param flags Flags indicating the changes to cancel.
     */
    public void cancelRestore(TreePath path, int flags);
    
    /**
     * Notifies the content provider that a client called {@link Viewer#setInput(Object)}, 
     * and the viewer input is about to change.  
     *  
     * @param viewer The viewer that uses this content provider.
     * @param oldInput Old input object.
     * @param newInput New input object.
     * 
     * @since 3.7
     */
    public void inputAboutToChange(ITreeModelContentProviderTarget viewer, Object oldInput, Object newInput);
}
