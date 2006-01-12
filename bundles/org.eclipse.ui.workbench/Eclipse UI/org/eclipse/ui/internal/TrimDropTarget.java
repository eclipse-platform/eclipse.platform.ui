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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWindowTrim;
import org.eclipse.ui.internal.dnd.DragBorder;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.IDropTarget2;
import org.eclipse.ui.internal.dnd.InsertCaret;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimArea;
import org.eclipse.ui.internal.layout.TrimDescriptor;
import org.eclipse.ui.internal.layout.TrimLayout;

/**
 */
/*package*/class TrimDropTarget implements IDragOverListener {
	
    private final class ActualTrimDropTarget implements IDropTarget2 {
        public IWindowTrim draggedTrim;
        
        // tracking parameters
    	private List insertCarets = new ArrayList();
    	private InsertCaret curCaret;
    	private DragBorder border = null;
    	private boolean docked = true;
        
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
            docked = true;
            
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
        public void setTrim(IWindowTrim trim) {
        	// Are we starting a new drag?
        	if (draggedTrim != trim) {
            	// remember the dragged trim
            	draggedTrim = trim;
            	
            	// Remember the location that we were in initially so we
            	// can go back there on an cancel...
            	initialAreaId = layout.getTrimAreaId(draggedTrim.getControl());
            	
            	// Determine who we were placed 'before' in the trim
            	List trimDescs = layout.getTrimArea(initialAreaId).getDescriptors();
            	for (Iterator iter = trimDescs.iterator(); iter.hasNext();) {
    				TrimDescriptor tDesc = (TrimDescriptor) iter.next();
    				if (tDesc.getTrim() == draggedTrim) {
    					if (iter.hasNext())
    						initialInsertBefore = ((TrimDescriptor)iter.next()).getTrim();
    					else
    						initialInsertBefore = null;
    				}
    			}
            	
            	// Remember the location that the controlused to be at for animation purposes
            	initialLocation = DragUtil.getDisplayBounds(draggedTrim.getControl());
            	            	
            	// The dragged trim is always initially docked
            	docked = true;
        	}
        }
        
        /**
         * Determine the trim area from the point.
         * 
         * @param pos The current cursor pos
         * @return the Trim area that the cursor is in or SWT.NONE if the point is not in an area
         */
        private int getTrimArea(Point pos) {
        	int[] areaIds = layout.getAreaIds();
        	for (int i = 0; i < areaIds.length; i++) {
				Rectangle trimRect = layout.getTrimRect(windowComposite, areaIds[i]);
				trimRect = Geometry.toControl(windowComposite, trimRect);

				// Only check 'valid' sides
				if ( (areaIds[i] & getValidSides()) != 0) {
					// TODO: more confusion binding 'areaIds' to SWT 'sides'
		        	switch (areaIds[i]) {
						case SWT.TOP:
							if (pos.x >= trimRect.x &&
								pos.x <= (trimRect.x+trimRect.width) &&
								pos.y <= (trimRect.y+trimRect.height))
									return areaIds[i];
							break;
						case SWT.LEFT:
							if (pos.y >= trimRect.y &&
								pos.y <= (trimRect.y+trimRect.height) &&
								pos.x <= (trimRect.x+trimRect.width))
									return areaIds[i];
							break;
						case SWT.RIGHT:
							if (pos.y >= trimRect.y &&
								pos.y <= (trimRect.y+trimRect.height) &&
								pos.x >= trimRect.x)
									return areaIds[i];
							break;
						case SWT.BOTTOM:
							if (pos.x >= trimRect.x &&
								pos.x <= (trimRect.x+trimRect.width) &&
								pos.y >= trimRect.y)
									return areaIds[i];
							break;
		        	}
				}
			}
        	
        	// not inside any trim area
        	return SWT.NONE;
        }
        
        /**
         * Returns a point based on the supplied rectangle that would be an appropriate
         * insertion point. This point is adjusted based on the 'side' that the point is
         * being calculated for.
         * 
         * @param rect The rectangle to generate the insertion point for
         * @param side The side that this rectangle is on
         * @param atTheEnd 'true' iff you want a point at the 'end' of the rect...
         * 
         * @return The adjusted insertion point
         */
        private Point getInsertPoint(Rectangle rect, int side, boolean atTheEnd) {
        	if (atTheEnd) {
	        	switch (side) {
					case SWT.TOP:
						return new Point(rect.x+rect.width, rect.y+rect.height);
					case SWT.LEFT:
						return new Point(rect.x+rect.width, rect.y+rect.height);
					case SWT.RIGHT:
						return new Point(rect.x, rect.y+rect.height);
					case SWT.BOTTOM:
						return new Point(rect.x+rect.width, rect.y);
	        	}
        	}
        	else {
	        	switch (side) {
					case SWT.TOP:
						return new Point(rect.x, rect.y+rect.height);
					case SWT.LEFT:
						return new Point(rect.x+rect.width, rect.y);
					case SWT.RIGHT:
					case SWT.BOTTOM:
						return new Point(rect.x, rect.y);
				};
        	}
			
			return null;
        }
        
        /**
         * Calculate the set of insertion points appropriate for the given trim area.
         * 
         * @param areaId The area to compute the points for
         * @return The array of insertion points for the given area
         */
        private Point[] calculateInsertionPoints(int areaId) {
			TrimArea area = layout.getTrimArea(areaId);
			List trim = area.getDescriptors();
			
			Point[] insertionPoints = new Point[trim.size()+1];
			
			// If the trim is empty then the only insertion point is at the 'middle'
			if (trim.size() == 0) {
				// Get the trim rect (converted to 'local' coords
				Rectangle trimRect = layout.getTrimRect(windowComposite, areaId);
				trimRect = Geometry.toControl(windowComposite, trimRect);
				
				// Place the insertion point at the 'start' of the trim rect
				if (TrimDragPreferences.useMiddleIfEmpty())
					insertionPoints[0] = Geometry.centerPoint(trimRect);
				else
					insertionPoints[0] = getInsertPoint(trimRect, areaId, false);
					
				return insertionPoints;
			}
			
			// The trim is not empty so place an insertion point 'before'
			// each trim
			TrimDescriptor trimDesc = null;
			int curIndex = 0;
			for (Iterator iter = trim.iterator(); iter.hasNext();) {
				trimDesc = (TrimDescriptor) iter.next();

				// The insertion point is dependent on the orientation
				Rectangle handleRect = null;
				
				// If there's a docking handle then it's to the left of it
				// Otherwise it's to the left of the actual trim control
				if (trimDesc.getDockingCache() != null)
					handleRect = trimDesc.getDockingCache().getControl().getBounds();
				else
					handleRect = trimDesc.getCache().getControl().getBounds();
				
				insertionPoints[curIndex++] = getInsertPoint(handleRect, areaId, false);
			}
			
			// ... and one at the end of the last trim element
			Rectangle ctrlRect = trimDesc.getCache().getControl().getBounds();
			insertionPoints[curIndex] = getInsertPoint(ctrlRect, areaId, true);
			
			return insertionPoints;
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
				if (desc.getTrim() == draggedTrim)
					continue;
				
				// Now, check
				Rectangle bb = desc.getCache().getControl().getBounds();
				Point center = Geometry.centerPoint(bb);
				if (isHorizontal) {
					if (pos.x < center.x)
						return desc.getTrim();
				}
				else {
					if (pos.y < center.y)
						return desc.getTrim();
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
        	if (cursorAreaId != SWT.NONE)
        		trackInsideTrimArea(pos);
        	else
        		trackOutsideTrimArea(pos);
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
        	int posArea = getTrimArea(pos);
        	IWindowTrim posInsertBefore = getInsertBefore(posArea, pos);

        	// if we're currently undocked then we should dock
        	boolean shouldDock = !docked;
        	
        	if (docked) {
	        	// Where are we now?
	        	int curArea = layout.getTrimAreaId(draggedTrim.getControl());
	        	IWindowTrim curInsertBefore = getInsertBefore(curArea, draggedTrim);
	        	
	        	// If we're already docked we should only update if there's a change
	        	shouldDock = curArea != posArea || curInsertBefore != posInsertBefore;
        	}
        	
        	// Do we have to do anything?
        	if (shouldDock) {
        		// (Re)dock the trim in the new location
        		dock(posArea, posInsertBefore, true);
        	}
        }
        
        /**
         * Provide the dragging feedback when the cursor is -not- explicitly inside
         * a particular trim area.
         * 
         */
        private void trackOutsideTrimArea(Point pos) {
        	// Determine if we're within the 'threshold' distance from an Insert Caret
        	InsertCaret closestCaret = getClosestCaret(pos);
        	
        	// Update the various controls...
        	// Handle 'undocked' tracking and transitions from the 'docked' to 'undocked' state 
        	if (closestCaret == null) {
        		// In 'auto-dock' mode the trim may be docked during drag ahndling outside a trim area
        		if (docked)
        			undock();
        		
        		border.setLocation(pos, true);
        		
        		// update the state vars
        		setCurCaret(null);
        		return;
        	}
        	
        	// If nothing has changed then it's a no-op
        	if (curCaret == closestCaret)
        		return;

        	// We're over a -new- insertion point; update the feedback
        	// remember the new 'drop' location
        	setCurCaret(closestCaret);
        	
        	if (TrimDragPreferences.autoDock()) {
        		dock(curCaret.getAreaId(), curCaret.getInsertTrim(), false);
        		return;
        	}
        	
			// 'snap' the control to the insertion point
			Rectangle ctrlRect = draggedTrim.getControl().getBounds();
			Rectangle caretRect = curCaret.getBounds();
			Point caretCenter = new Point(caretRect.x+(caretRect.width/2), caretRect.y+(caretRect.height/2));
        	switch (curCaret.getAreaId()) {
				case SWT.TOP:
					border.setLocation(new Point(caretCenter.x-(ctrlRect.width/2), caretRect.y+caretRect.height+1), false);
					break;
				case SWT.LEFT:
					border.setLocation(new Point(caretRect.x+caretRect.width+2, caretCenter.y-(ctrlRect.height/2)), false);
					break;
				case SWT.RIGHT:
					border.setLocation(new Point((caretRect.x-(ctrlRect.width))-3, caretCenter.y-(ctrlRect.height/2)), false);
					break;
				case SWT.BOTTOM:
					border.setLocation(new Point(caretCenter.x-(ctrlRect.width/2), (caretRect.y-(ctrlRect.height))-3), false);
					break;
        	};
        }
        
        /**
         * Deterime the caret closest to the given point. The distance must be less than the
         * threshold value in order to count as a 'hit'.
         * 
         * @param pos The position to check against
         * @return The closest caret whose distance is less than the threshold
         */
        private InsertCaret getClosestCaret(Point pos) {
        	// Find the closest insertion caret
        	int threshold = TrimDragPreferences.getThreshold();
        	int thresholdSquared = threshold*threshold;
        	int minDist = Integer.MAX_VALUE;
        	InsertCaret closestCaret = null;
        	
        	for (Iterator iter = insertCarets.iterator(); iter.hasNext();) {
        		InsertCaret caret = (InsertCaret) iter.next();
				Point caretPos = Geometry.centerPoint(caret.getBounds());
				int dist = Geometry.distanceSquared(pos, caretPos);
				if (dist < minDist) {
					minDist = dist;
					
					// If this is 'acceptable' then set the caret
					if (minDist <= thresholdSquared)
							closestCaret = caret;
				}
			}
        	
        	return closestCaret;
        }
        
        /**
         * Return the set of valid sides that a piece of trim can be docked on. We
         * arbitrarily extend this to include any areas that won't cause a change in orientation
         * 
         * @return The extended drop 'side' set
         */
        private int getValidSides() {
        	int result = draggedTrim.getValidSides();
        	
        	// Automatically allow dropping onto sides that won't change the orientation
        	if ((result & SWT.LEFT) != 0) result |= SWT.RIGHT;
        	if ((result & SWT.RIGHT) != 0) result |= SWT.LEFT;
        	if ((result & SWT.TOP) != 0) result |= SWT.BOTTOM;
        	if ((result & SWT.BOTTOM) != 0) result |= SWT.TOP;
        	
        	return result;
        }
        		
        /**
         * Set the current caret. This is determined by choosing the caret
         * closest to the cursor (as long as it's within the 'threshold' value.
         * 
         * @param newCaret The caret to set as the new 'current' caret
         */
        private void setCurCaret(InsertCaret newCaret) {
        	// un-highlight the 'old' caret (if any)
        	if (curCaret != null) {
        		curCaret.setHighlight(false);
        		border.setHighlight(false);
        	}
        	
        	curCaret = newCaret;
        	
        	// highlight the 'new' caret (if any)
        	if (curCaret != null) {
        		curCaret.setHighlight(true);
        		border.setHighlight(true);
        	}
        }
        
		/**
		 * Return the IWindowTrim to use as the insertion parameter given the trim's index.
		 * 
		 * @param areaId The trim area to check
		 * @param index The index of the insert trim within this area
		 * @return The IWindowTrim associated with the index.
		 */
		private IWindowTrim getInsertTrim(int areaId, int index) {
			TrimArea area = layout.getTrimArea(areaId);
			List descs = area.getDescriptors();

			// If the index is at the end then the insert 'trim' is null
			if (index >= descs.size())
				return null;
			
			// Get the trim from the description
			TrimDescriptor desc = (TrimDescriptor) descs.get(index);
			return desc.getTrim();
		}
        
        /**
         * Create the set of insertion point indicators. This is based on the set
         * of 'valid' sides as defined by the dragged trim.
         */
        private void createInsertPoints() {
        	// Find the closest insertion point. Only 'valid' areas are checked
        	int[] areaIds = layout.getAreaIds();
        	for (int i = 0; i < areaIds.length; i++) {
        		// Only check 'valid' sides
				if ((getValidSides() & areaIds[i]) != 0) {
					Point[] pnts = calculateInsertionPoints(areaIds[i]);
					for (int j = 0; j < pnts.length; j++) {
						insertCarets.add(new InsertCaret(windowComposite, pnts[j], areaIds[i], getInsertTrim(areaIds[i], j),
									layout, TrimDragPreferences.useBars()));
					}
				}
			}
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

            dock(initialAreaId, initialInsertBefore, true);
        }
        
		/* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
         */
        public void drop() {
        	// If we're already docked then there's nothing to do
        	if (docked)
        		return;
        	
        	// If we try to drop while in no-man's land then re-dock to the last known location
        	if (curCaret == null) {
        		redock();
               	return;
        	}
        	
        	// Dock into the location specified by the 'curCaret'
        	dock(curCaret.getAreaId(), curCaret.getInsertTrim(), true);
        }

        /**
         * Remove the trim frmo its current 'docked' location and attach it
         * to the cursor...
         */
        private void undock() {
        	// Remove the trim from the layout
        	layout.removeTrim(draggedTrim);
           	LayoutUtil.resize(draggedTrim.getControl());
           	
           	// Create a new dragging border onto the dragged trim
           	border = new DragBorder(windowComposite, draggedTrim.getControl());

           	// Create and show the insertion points
           	createInsertPoints();
           	curCaret = null;
           	
           	docked = false;
        }
        
        /**
         * Return the 'undocked' trim to its previous location in the layout
         */
        private void dock(int areaId, IWindowTrim insertBefore, boolean removeCarets) {
			if (removeCarets) {
	        	// dispose all the carets and the dragging border
				for (Iterator iter = insertCarets.iterator(); iter.hasNext();) {
					InsertCaret caret = (InsertCaret) iter.next();
					caret.dispose();
				}
				insertCarets.clear();
				curCaret = null;
			}
			
			if (border != null)
				border.dispose();
			border = null;
			
			// Update the trim's orientation if necessary
			draggedTrim.dock(areaId);

			// Add the trim into the layout
            layout.addTrim(areaId, draggedTrim, insertBefore);
           	LayoutUtil.resize(draggedTrim.getControl());
           	
           	docked = true;
        }
        	
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
         */
        public Cursor getCursor() {
        	if (TrimDragPreferences.inhibitCustomCursors()) {
	        	// Show the four-way arrow by default
				Cursor dragCursor = windowComposite.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL);
	
				// If we're 'floating' (i.e. in a non-droppable area) then show the 'no smoking' cursor
	        	if (!docked && curCaret == null)
	        		dragCursor = windowComposite.getDisplay().getSystemCursor(SWT.CURSOR_NO);
	        		
	        	return dragCursor;
        	}
        	else {
            	if (!docked)
    	            return DragCursors.getCursor(DragCursors
    	                    .positionToDragCursor(curCaret == null ? SWT.NONE : curCaret.getAreaId()));
            	else
            		return DragCursors.getCursor(DragCursors.positionToDragCursor(cursorAreaId));
        	}
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
			if (!dropPerformed && !docked) {
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
        
        // Add the trim layout as a 'default' drop target so that it
        // continues to get events even when the mouse is -outside- the shell
        DragUtil.addDragTarget(null, this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
     */
    public IDropTarget drag(Control currentControl, Object draggedObject,
            Point position, final Rectangle dragRectangle) {
    	
    	// Have to be dragging trim
    	if (!(draggedObject instanceof IWindowTrim))
    		return null;
    	
    	// OK, we're dragging trim. is it from -this- shell?
    	IWindowTrim trim = (IWindowTrim) draggedObject;
    	if (trim.getControl().getShell() != windowComposite.getShell())
    		return null;
    	
    	// If this is the -first- drag then inform the drop target
    	if (dropTarget.draggedTrim == null) {
    		dropTarget.setTrim(trim);
    	}

    	// Forward on to the 'actual' drop target for feedback
    	dropTarget.track(position);
    	
    	// Spin the paint loop after every track
    	windowComposite.getDisplay().update();
    	
		return dropTarget;
    }
}
