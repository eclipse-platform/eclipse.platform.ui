/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.dnd.DragBorder;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.IDropTarget2;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimDescriptor;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.layout.TrimToolBarBase;

/**
 */
/*package*/class TrimDropTarget implements IDragOverListener {
	
    private final class ActualTrimDropTarget implements IDropTarget2 {
        public IWindowTrim draggedTrim;
        
        // tracking parameters
    	private DragBorder border = null;
    	private int dockedArea;
        
        // Holder for the position of trim that is 'floating' with the cursor
    	private int cursorAreaId;
        private int initialAreaId;
        private IWindowTrim initialInsertBefore;        
		private Rectangle initialLocation;

        /**
         * Constructor
         */
        private ActualTrimDropTarget() {
            super();

            draggedTrim = null;
            dockedArea = SWT.NONE;
            
            initialAreaId = SWT.NONE;
            initialInsertBefore = null;
        }
        
        /**
         * This method is used to delineate separate trims dragging events. The -first- drag
         * event will set this and then it will remain constant until the drag gesture is done;
         * either by dropping or escaping. Once the gesture is finished the trim value is set
         * back to 'null'.
         * 
         * @param trim The trim item currently being dragged.
         */
        public void startDrag(IWindowTrim trim) {
        	// Are we starting a new drag?
        	if (draggedTrim != trim) {
            	// remember the dragged trim
            	draggedTrim = trim;
            	
            	// Remember the location that we were in initially so we
            	// can go back there on an cancel...
            	initialAreaId = layout.getTrimAreaId(draggedTrim.getControl());
            	
            	// Determine who we were placed 'before' in the trim
            	initialInsertBefore = getInsertBefore(initialAreaId, draggedTrim);
            	
            	// Remember the location that the control used to be at for animation purposes
            	initialLocation = DragUtil.getDisplayBounds(draggedTrim.getControl());
            	            	
            	// The dragged trim is always initially docked
            	dockedArea = initialAreaId;
        	}
        }
                
        /**
         * Determine the trim area from the point. To avoid clashing at the 'corners' due to extending the trim area's
         * rectangles we first ensure that the point is not actually -within- a trim area before we check the extended
         * rectangles.
         * 
         * @param pos The current cursor pos
         * @return the Trim area that the cursor is in or SWT.NONE if the point is not in an area
         */
        private int getTrimArea(Point pos) {
        	// First, check if we're actually -within- a trim area (i.e. no boundary extensions)
        	int areaId = getTrimArea(pos, 0);
        	
        	// If we are not inside a trim area...are we 'close' to one?
        	if (areaId == SWT.NONE) {
				areaId = getTrimArea(pos, TrimDragPreferences.getThreshold());
			}
        	
        	// not inside any trim area
        	return areaId;
        }
        
        /**
         * Checks the trims areas against the given position. Each trim area is 'extended' into
         * the workbench page by the value of <code>extendedBoundaryWidth</code> before the checking
         * takes place.
         * 
         * @param pos The point to check against
         * @param extendedBoundaryWidth The amount to extend the trim area's 'inner' edge by
         * 
         * @return The trim area or SWT.NONE if the point is not within any extended trim area's rect.
         */
        private int getTrimArea(Point pos, int extendedBoundaryWidth) {
        	int[] areaIds = layout.getAreaIds();
        	for (int i = 0; i < areaIds.length; i++) {
				Rectangle trimRect = layout.getTrimRect(windowComposite, areaIds[i]);
				trimRect = Geometry.toControl(windowComposite, trimRect);

				// Only check 'valid' sides
				if ( (areaIds[i] & getValidSides()) != SWT.NONE) {
					// TODO: more confusion binding 'areaIds' to SWT 'sides'
		        	switch (areaIds[i]) {
					case SWT.LEFT:
						trimRect.width += extendedBoundaryWidth;
						
						if (pos.y >= trimRect.y &&
							pos.y <= (trimRect.y+trimRect.height) &&
							pos.x <= (trimRect.x+trimRect.width)) {
							return areaIds[i];
						}
						break;
					case SWT.RIGHT:
						trimRect.x -= extendedBoundaryWidth;
						trimRect.width += extendedBoundaryWidth;
						
						if (pos.y >= trimRect.y &&
							pos.y <= (trimRect.y+trimRect.height) &&
							pos.x >= trimRect.x) {
							return areaIds[i];
						}
						break;
					case SWT.TOP:
						trimRect.height += extendedBoundaryWidth;
						
						if (pos.x >= trimRect.x &&
							pos.x <= (trimRect.x+trimRect.width) &&
							pos.y <= (trimRect.y+trimRect.height)) {
							return areaIds[i];
						}
						break;
					case SWT.BOTTOM:
						trimRect.y -= extendedBoundaryWidth;
						trimRect.height += extendedBoundaryWidth;
						
						if (pos.x >= trimRect.x &&
							pos.x <= (trimRect.x+trimRect.width) &&
							pos.y >= trimRect.y) {
							return areaIds[i];
						}
						break;
		        	}
				}
			}
        	
        	// not inside any trim area
        	return SWT.NONE;
        }
        
        /**
         * Determine the window trim that the currently dragged trim should be inserted
         * before.
         * @param areaId The area id that is being checked
         * @param pos The position used to determine the correct insertion trim
         * @return The trim to 'dock' the draggedTrim before
         */
        private IWindowTrim getInsertBefore(int areaId, Point pos) {
        	boolean isHorizontal = (areaId == SWT.TOP) || (areaId == SWT.BOTTOM);
        	
        	// Walk the trim area and return the first one that the positon
        	// is 'after'.
        	List tDescs = layout.getTrimArea(areaId).getDescriptors();
        	for (Iterator iter = tDescs.iterator(); iter.hasNext();) {
				TrimDescriptor desc = (TrimDescriptor) iter.next();
				
				// Skip ourselves
				if (desc.getTrim() == draggedTrim) {
					continue;
				}
				
				// Now, check
				Rectangle bb = desc.getCache().getControl().getBounds();
				Point center = Geometry.centerPoint(bb);
				if (isHorizontal) {
					if (pos.x < center.x) {
						return desc.getTrim();
					}
				}
				else {
					if (pos.y < center.y) {
						return desc.getTrim();
					}
				}
			}
        	
        	return null;
        }
        
        /**
         * Returns the trim that is 'before' the given trim in the given area
         * 
         * @param areaId The areaId of the trim
         * @param trim The trim to find the element after
         * 
         * @return The trim that the given trim is 'before'
         */
        private IWindowTrim getInsertBefore(int areaId, IWindowTrim trim) {
        	List tDescs = layout.getTrimArea(areaId).getDescriptors();
        	for (Iterator iter = tDescs.iterator(); iter.hasNext();) {
				TrimDescriptor desc = (TrimDescriptor) iter.next();
				if (desc.getTrim() == trim) {
					if (iter.hasNext()) {
						desc = (TrimDescriptor) iter.next();
						return desc.getTrim();
					}
					return null;
				}
			}
        	
        	return null;
        }
        
        /**
         * Recalculates the drop information based on the current cursor pos.
         * 
         * @param pos The cursor position
         */
        public void track(Point pos) {
        	// Convert the mouse positon into 'local' coords
        	Rectangle r = new Rectangle(pos.x, pos.y, 1,1);
        	r = Geometry.toControl(windowComposite, r);
        	pos.x = r.x;
        	pos.y = r.y;
        	        	
        	// Are we 'inside' a trim area ?
        	cursorAreaId = getTrimArea(pos);

        	// Provide tracking for the appropriate 'mode'
        	if (cursorAreaId != SWT.NONE) {
				trackInsideTrimArea(pos);
			} else {
				trackOutsideTrimArea(pos);
			}
        }
       
        /**
         * Perform the feedback used when the cursor is 'inside' a particular trim area.
         * The current implementation will place the dragged trim into the trim area at
         * the location determined by the supplied point.
         * 
         * @param pos The point to use to determine where in the trim area the dragged trim
         * should be located.
         */
        private void trackInsideTrimArea(Point pos) {
        	// Where should we be?
        	int newArea = getTrimArea(pos);
        	IWindowTrim newInsertBefore = getInsertBefore(newArea, pos);

        	// if we're currently undocked then we should dock
        	boolean shouldDock = dockedArea == SWT.NONE;
        	
        	// If we're already docked then only update if there's a change in area or position
        	if (dockedArea != SWT.NONE) {
	        	// Where are we now?
	        	IWindowTrim curInsertBefore = getInsertBefore(dockedArea, draggedTrim);
	        	
	        	// If we're already docked we should only update if there's a change
	        	shouldDock = dockedArea != newArea || curInsertBefore != newInsertBefore;
        	}
        	
        	// Do we have to do anything?
        	if (shouldDock) {
        		// (Re)dock the trim in the new location
        		dock(newArea, newInsertBefore);
        	}
        }
        
        /**
         * Provide the dragging feedback when the cursor is -not- explicitly inside
         * a particular trim area.
         * 
         */
        private void trackOutsideTrimArea(Point pos) {
        	// If we -were- docked then undock
        	if (dockedArea != SWT.NONE) {
				undock();
			}
    		
    		border.setLocation(pos, SWT.BOTTOM);
        }
                
        /**
         * Return the set of valid sides that a piece of trim can be docked on. We
         * arbitrarily extend this to include any areas that won't cause a change in orientation
         * 
         * @return The extended drop 'side' set
         */
        private int getValidSides() {
        	int result = draggedTrim.getValidSides();
        	if (result == SWT.NONE) {
				return result;
			}

        	// For now, if we can dock...we can dock -anywhere-
        	return SWT.TOP | SWT.BOTTOM | SWT.LEFT | SWT.RIGHT;
        }

        /**
         * The user either cancelled the drag or tried to drop the trim in an invalid
         * area...put the trim back in the last location it was in
         */
        private void redock() {
        	// Since the control might move 'far' we'll provide an animation
        	Rectangle startRect = DragUtil.getDisplayBounds(draggedTrim.getControl());
            RectangleAnimation animation = new RectangleAnimation(
                    windowComposite.getShell(), startRect, initialLocation, 300);
            animation.schedule();

            dock(initialAreaId, initialInsertBefore);
        }
        
		/* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
         */
        public void drop() {
        	// If we aren't docked then restore the initial location
        	if (dockedArea == SWT.NONE) {
				redock();
			}
        }

        /**
         * Remove the trim frmo its current 'docked' location and attach it
         * to the cursor...
         */
        private void undock() {
        	// Remove the trim from the layout
        	layout.removeTrim(draggedTrim);
           	LayoutUtil.resize(draggedTrim.getControl());
           	
           	// Re-orient the widget to its -original- side and size
        	draggedTrim.dock(initialAreaId);
        	draggedTrim.getControl().setSize(initialLocation.width, initialLocation.height);
           	
           	// Create a new dragging border onto the dragged trim
        	// Special check for TrimPart...should be generalized
        	boolean wantsFrame = !(draggedTrim instanceof TrimToolBarBase);
           	border = new DragBorder(windowComposite, draggedTrim.getControl(), wantsFrame);

           	dockedArea = SWT.NONE;
        }
        
        /**
         * Return the 'undocked' trim to its previous location in the layout
         */
        private void dock(int areaId, IWindowTrim insertBefore) {
        	// remove the drag 'border'
        	if (border != null) {
				border.dispose();
				border = null;
        	}
			
			// Update the trim's orientation if necessary
			draggedTrim.dock(areaId);

			// Add the trim into the layout
            layout.addTrim(areaId, draggedTrim, insertBefore);
           	LayoutUtil.resize(draggedTrim.getControl());
           	
           	// Remember the area that we're currently docked in
           	dockedArea = areaId;
        }
        	
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
         */
        public Cursor getCursor() {
        	// If the trim isn't docked then show the 'no smoking' sign
        	if (cursorAreaId == SWT.NONE) {
				return windowComposite.getDisplay().getSystemCursor(SWT.CURSOR_NO);
			}
        	
        	// It's docked; show the four-way arrow cursor
        	return windowComposite.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#getSnapRectangle()
         */
        public Rectangle getSnapRectangle() {
        	// TODO: KLUDGE!! We don't want to show -any- snap rect
        	// but Tracker won't allow that so place it where it won't be visible
        	return new Rectangle(100000, 0,0,0);
        }

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.dnd.IDropTarget2#dragFinished(boolean)
		 */
		public void dragFinished(boolean dropPerformed) {
			// If we didn't perform a drop then restore the original position
			if (!dropPerformed && dockedArea == SWT.NONE) {
				// Force the dragged trim back into its original position...				
				redock();
			}
			
			// Set the draggedTrim to null. This indicates that we're no longer
			// dragging the trim. The first call to the TrimDropTarget's 'drag' method
			// will reset this the next time a drag starts.
			draggedTrim = null;
		}
    }
    
    private ActualTrimDropTarget dropTarget;
    
    private TrimLayout layout;
    private Composite windowComposite;

    /**
     * Create a new drop target capable of accepting IWindowTrim items
     * 
     * @param someComposite The control owning the TrimLayout
     * @param theWindow the workbenchWindow
     */
    public TrimDropTarget(Composite someComposite, WorkbenchWindow theWindow) {
        layout = (TrimLayout) someComposite.getLayout();
        windowComposite = someComposite;

        // Create an instance of a drop target to use
        dropTarget = new ActualTrimDropTarget();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
     */
    public IDropTarget drag(Control currentControl, Object draggedObject,
            Point position, final Rectangle dragRectangle) {
    	
    	// Have to be dragging trim
    	if (!(draggedObject instanceof IWindowTrim)) {
			return null;
		}
    	
    	// OK, we're dragging trim. is it from -this- shell?
    	IWindowTrim trim = (IWindowTrim) draggedObject;
    	if (trim.getControl().getShell() != windowComposite.getShell()) {
			return null;
		}
    	
    	// If this is the -first- drag then inform the drop target
    	if (dropTarget.draggedTrim == null) {
    		dropTarget.startDrag(trim);
    	}

    	// Forward on to the 'actual' drop target for feedback
    	dropTarget.track(position);
    	
    	// Spin the paint loop after every track
    	windowComposite.getDisplay().update();
    	
		return dropTarget;
    }
}
