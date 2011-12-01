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


/**
 * A validator to be used with a VirtualTreeModelViewer to determine which viewer
 * items should be updated by the viewer.   
 * 
 * @see VirtualTreeModelViewer
 * @since 3.8
 */
public interface IVirtualItemValidator {
    
    /**
     * Allows the validator to determine whether the given item is to be deemed 
     * visible in the virtual tree.
     * 
     * @param item Item to be tested.
     * @return returns true if the item should be considered visible.
     */
    public boolean isItemVisible(VirtualItem item);
    
    /**
     * Indicates that the viewer requested to reveal the given item in viewer.
     * 
     * @param item Item to show.
     */
    public void showItem(VirtualItem item);
}
