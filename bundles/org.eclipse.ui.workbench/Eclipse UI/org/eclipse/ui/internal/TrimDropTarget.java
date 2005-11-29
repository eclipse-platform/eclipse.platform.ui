/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.CompatibilityDragTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimLayout;

/**
 */
/*package*/class TrimDropTarget implements IDragOverListener {

    private final class ActualTrimDropTarget extends AbstractDropTarget {
        public Rectangle dragRectangle;
        public IWindowTrim draggedTrim;
        public int dropSide;
        public IWindowTrim insertBefore;

        private ActualTrimDropTarget(Rectangle dragRectangle, IWindowTrim draggedTrim, int dropSide, IWindowTrim insertBefore) {
            super();
            updateTarget(dragRectangle, draggedTrim, dropSide, insertBefore);
        }
        
        private void updateTarget(Rectangle dragRectangle, IWindowTrim draggedTrim, int dropSide,  IWindowTrim insertBefore) {
            this.dragRectangle = dragRectangle;
            this.draggedTrim = draggedTrim;
            this.dropSide = dropSide;
            this.insertBefore = insertBefore;
        }

        public void drop() {
        	// Skip if we're doing immedate drpping
        	if (!layout.isImmediate()) {
	        	// Trying to insert before ourselves is a NO-OP
	        	if (insertBefore == draggedTrim)
	        		return;
	        	
	            if (dropSide != layout.getTrimAreaId(draggedTrim.getControl())) {
	            	layout.removeTrim(draggedTrim);
	                draggedTrim.dock(dropSide);
	            }
	
	            // handle rearrangements within the trim
		       	layout.addTrim(draggedTrim, dropSide, insertBefore);
	           	LayoutUtil.resize(draggedTrim.getControl());
        	}
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors
                    .positionToDragCursor(dropSide));
        }

        public Rectangle getSnapRectangle() {
        	if (layout.isImmediate())
        		return new Rectangle(0,0,0,0);
        	
        	return dragRectangle;
        }
    }
    
    private ActualTrimDropTarget dropTarget;
    
    private TrimLayout layout;
    private Composite windowComposite;
    private WorkbenchWindow window;

    /**
     * Create a new drop target capable of accepting IWindowTrim items
     * 
     * @param someComposite The control owning the TrimLayout
     * @param theWindow the workbenchWindow
     */
    public TrimDropTarget(Composite someComposite, WorkbenchWindow theWindow) {
        layout = (TrimLayout) someComposite.getLayout();
        windowComposite = someComposite;
        window = theWindow;

        // Create an instance of a drop target to use
        dropTarget = new ActualTrimDropTarget(null, null, 0, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
     */
    public IDropTarget drag(Control currentControl, Object draggedObject,
            Point position, final Rectangle dragRectangle) {

        // Handle dropping window trim on the border of the workbench (for example,
        // the fast view bar)
        if (draggedObject instanceof IWindowTrim) {
            final IWindowTrim draggedTrim = (IWindowTrim) draggedObject;

            Control trimControl = draggedTrim.getControl();

            if (trimControl.getParent() == windowComposite) {
                Control targetTrim = getTrimControl(currentControl);

                int side = layout.getTrimAreaId(targetTrim);

                if (side == SWT.DEFAULT) {
                    if (targetTrim == layout.getCenterControl()) {
                        side = CompatibilityDragTarget.getRelativePosition(
                                targetTrim, position);
                        if (side == SWT.CENTER) {
                            side = SWT.DEFAULT;
                        }

                        targetTrim = null;
                    }
                }

                // Determine the side to drop the trim on...
                side = getDropSide(position);
                
                if (side != SWT.DEFAULT && ((side & draggedTrim.getValidSides()) != 0)) {
                	// get the side that the trim is currently on
                	int curSide = layout.getTrimAreaId(trimControl);
                    
                    // Determine the 'insertion' point for the trim based on the side and cursor pos
                    IWindowTrim insertBefore = layout.getInsertBefore(side, position);
                    
                    // If we've either changed sides and / or the insertion point then update the feedback
                    if (dropTarget == null || side != dropTarget.dropSide || insertBefore != dropTarget.insertBefore) {
                        Rectangle snapRect = new Rectangle(0,0,0,0);
                        
                        // If we're not providing 'immediate' feedback then calculate the correct
                        // 'snap rect' to use to provide the feedback
                    	if (!layout.isImmediate()) {
	                    	// Get the rect for the control being dragged
	                    	Rectangle ctrlRect = draggedTrim.getControl().getBounds();
	                        
	                        // If we're dragging to a new location then we might have to 'flip' the rect
	                        int curOrientation = (curSide == SWT.TOP || curSide == SWT.BOTTOM) ? SWT.HORIZONTAL : SWT.VERTICAL;
	                        int newOrientation = (side == SWT.TOP || side == SWT.BOTTOM) ? SWT.HORIZONTAL : SWT.VERTICAL;
	                        if (curOrientation != newOrientation)
	                        	Geometry.flipXY(ctrlRect);
	                    	
	                        // Get the snap rectangle
	                        if (insertBefore == draggedTrim.getControl())
	                        	snapRect = DragUtil.getDisplayBounds(draggedTrim.getControl());
	                        else
	                        	snapRect = layout.getSnapRectangle(windowComposite, side, ctrlRect, insertBefore);
                    	}
                    	
                    	if (layout.isImmediate())
                    		fakeDrop(draggedTrim, insertBefore, side);
                    		
                        // update the drop target based on the updated info
	                    updateDropTarget(snapRect, draggedTrim, side, insertBefore);
                    }
                    
                    // If there's no change then re-use the existing drop target
                    return dropTarget;
                }
            }
        }

        return null;
    }

    /**
     * Return the side that the currently dragged trim should be placed on. The
     * <code>getClosestSide</code> method will incorrectly (for us) report SWT.LEFT
     * when a drag start happens on trim located at SWT.BOTTOM because of the 
     * corner diagonals...so we'll check if we're actually -in- a trim area and
     * only use the more general method if we aren't.
     *  
     * @param position The positon to be checked
     * @return The trim area appropriate for the given position
     */
    private int getDropSide(Point position) {
        // First, check to see if we're IN a side
        Rectangle trimRect;

        // Top
        trimRect = layout.getTrimRect(windowComposite, SWT.TOP);
        if (trimRect.contains(position))
        		return SWT.TOP;
        
        // Bottom
        trimRect = layout.getTrimRect(windowComposite, SWT.BOTTOM);
        if (trimRect.contains(position))
        		return SWT.BOTTOM;
        
        // Left
        trimRect = layout.getTrimRect(windowComposite, SWT.LEFT);
        if (trimRect.contains(position))
        		return SWT.LEFT;
        
        // Right
        trimRect = layout.getTrimRect(windowComposite, SWT.RIGHT);
        if (trimRect.contains(position))
        		return SWT.RIGHT;

        // We're not in a trim rect so pick the closest side
        return Geometry.getClosestSide(window.getShell().getBounds(), position);
	}

	private void fakeDrop(IWindowTrim draggedTrim, IWindowTrim insertBefore, int dropSide) {
    	// Trying to insert before ourselves is a NO-OP
    	if (insertBefore == draggedTrim)
    		return;
    	
        if (dropSide != layout.getTrimAreaId(draggedTrim.getControl())) {
        	layout.removeTrim(draggedTrim);
            draggedTrim.dock(dropSide);
        }

        // handle rearrangements within the trim
       	layout.addTrim(draggedTrim, dropSide, insertBefore);
       	LayoutUtil.resize(draggedTrim.getControl());    	
    }
    
    /**
     * Returns a drop target with the given specifications. As an optimization, the result of this method is cached
     * and the object is reused in subsequent calls.
     * 
     * @param dragRectangle the trim control's rect (flipped in XY if the orientation has changed)
     * @param draggedTrim the IWindowTrim being dragged
     * @param dropSide the side that the trim would be dropeed on
     * @param insertBefore the trim to insert this trim before
     * 
     * @return the drop result based on the current context
     * 
     * @since 3.1
     */
    private void updateDropTarget(Rectangle dragRectangle, IWindowTrim draggedTrim, int dropSide, IWindowTrim insertBefore) {
    	dropTarget.updateTarget(dragRectangle, draggedTrim, dropSide, insertBefore);
    }

    /**
     * Walks up the given control's hierarchy until the owner of the 
     * trim layout is found.
     * @param searchSource a trim control
     * @return the Control associated with the TrimLayout
     */
    private Control getTrimControl(Control searchSource) {
        if (searchSource == null) {
            return null;
        }

        if (searchSource.getParent() == windowComposite) {
            return searchSource;
        }

        return getTrimControl(searchSource.getParent());
    }
}
