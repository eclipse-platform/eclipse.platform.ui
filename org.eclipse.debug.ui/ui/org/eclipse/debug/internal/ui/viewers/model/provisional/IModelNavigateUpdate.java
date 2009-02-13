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
package org.eclipse.debug.internal.ui.viewers.model.provisional;


/**
 * Update to retrieve the next element to navigate to in model.
 * 
 * @since 3.5
 */
public interface IModelNavigateUpdate extends IViewerUpdate {
    
    /**
     * True if navigation is in reverse.
     * @return
     */
    public boolean isReverse();
    
    /**
     * Sets the delta which will select the next element to navigate to.  The delta
     * should use the {@link IModelDelta.FORCE} flag in conjunction with the
     * {@link IModelDelta.SELECT} flag.  If the update element is the last element 
     * in the navigation order, this delta should be set to <code>null</code>
     *  
     * @param delta Delta which will cause next model element to be revealed.  Should
     * be set to <code>null</code> to indicate that there are no more elements to 
     * navigate to in the given traversal order.
     */
    public void setNextElementDelta(IModelDelta delta);
}
