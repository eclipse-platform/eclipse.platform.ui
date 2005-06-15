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
package org.eclipse.ui.internal;

public interface ILayoutContainer {
    public boolean allowsAdd(LayoutPart toAdd);
    
    /**
     * Add a child to the container.
     */
    public void add(LayoutPart newPart);

    /**
     * Returns a list of layout children.
     */
    public LayoutPart[] getChildren();

    /**
     * Remove a child from the container.
     */
    public void remove(LayoutPart part);

    /**
     * Replace one child with another
     */
    public void replace(LayoutPart oldPart, LayoutPart newPart);
    
    public void findSashes(LayoutPart toFind, PartPane.Sashes result);

    /**
     * When a layout part closes, focus will return to the previously active part.
     * This method determines whether the parts in this container should participate
     * in this behavior. If this method returns true, its parts may automatically be
     * given focus when another part is closed. 
     * 
     * @return true iff the parts in this container may be given focus when the active
     * part is closed
     */
    public boolean allowsAutoFocus();

    /**
     * Called by child parts to request a zoom in, given an immediate child 
     * 
     * @param toZoom
     * @since 3.1
     */
    public void childRequestZoomIn(LayoutPart toZoom);
    
    /**
     * Called by child parts to request a zoom out
     * 
     * @since 3.1
     */
    public void childRequestZoomOut();
    
    /**
     * Returns true iff the given child is obscured due to the fact that the container is zoomed into
     * another part. 
     * 
     * @param toTest
     * @return
     * @since 3.1
     */
    public boolean childObscuredByZoom(LayoutPart toTest);
    
    /**
     * Returns true iff we are zoomed into the given part, given an immediate child of this container.
     * 
     * @param toTest
     * @return
     * @since 3.1
     */
    public boolean childIsZoomed(LayoutPart toTest);

    /**
     * Called when the preferred size of the given child has changed, requiring a
     * layout to be triggered.
     * 
     * @param childThatChanged the child that triggered the new layout
     */
    public void resizeChild(LayoutPart childThatChanged);

}
