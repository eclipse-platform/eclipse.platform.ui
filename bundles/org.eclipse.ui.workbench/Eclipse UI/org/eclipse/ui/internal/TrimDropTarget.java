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
    	private int threshold = 100;  // How close to a caret the cursor has to be to be 'valid'
    	private List insertCarets = new ArrayList();
    	private InsertCaret curCaret;
    	private DragBorder border = null;
    	private boolean docked = true;
        
        // Holder for the position of trim that is 'floating' with the cursor
    	private int cursorAreaId;
        private int previousAreaId;
        private IWindowTrim previousInsertBefore;

        private ActualTrimDropTarget() {
            super();

            draggedTrim = null;
            docked = true;
            
            previousAreaId = SWT.NONE;
            previousInsertBefore = null;
        }
        
        public void setTrim(IWindowTrim trim) {
        	// Are we starting a new drag?
        	if (draggedTrim != trim) {
        		previousAreaId = SWT.NONE;
        		previousInsertBefore = null;
            	
            	// remember the dragged trim
            	draggedTrim = trim;
            	
            	// The dragged trim is always initially docked
            	docked = true;
        	}
        }
        
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
				insertionPoints[0] = Geometry.centerPoint(trimRect);//getInsertPoint(trimRect, areaId, false);
				return insertionPoints;
			}
			
			// The trim is not empty so place an insertion point 'before'
			// each trim
			TrimDescriptor trimDesc = null;
			int curIndex = 0;
			for (Iterator iter = trim.iterator(); iter.hasNext();) {
				trimDesc = (TrimDescriptor) iter.next();

				// The insertion point is dependent on the orientation
				Rectangle handleRect = trimDesc.getDockingCache().getControl().getBounds();
				insertionPoints[curIndex++] = getInsertPoint(handleRect, areaId, false);
			}
			
			// ... and one at the end of the last trim element
			Rectangle ctrlRect = trimDesc.getCache().getControl().getBounds();
			insertionPoints[curIndex] = getInsertPoint(ctrlRect, areaId, true);
			
			return insertionPoints;
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
        	
        	// Do we have to 'undock' ?
        	if (cursorAreaId == SWT.NONE && docked) {
        		undock();
        	}
        	
        	// Do we have to 'dock'?
        	if (cursorAreaId != SWT.NONE && !docked) {
        		IWindowTrim insertBefore = getInsertBefore(cursorAreaId, pos);
        		dock(cursorAreaId, insertBefore);
        	}

        	// Provide tracking for the appropriate 'mode'
        	if (docked)
        		trackDocked(pos);
        	else
        		trackUndocked(pos);
        }
        
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
        
        private void trackDocked(Point pos) {
        	// Where are we now?
        	int curArea = layout.getTrimAreaId(draggedTrim.getControl());
        	IWindowTrim curInsertBefore = getInsertBefore(curArea, draggedTrim);
        	
        	// Where should we be?
        	int posArea = getTrimArea(pos);
        	IWindowTrim posInsertBefore = getInsertBefore(posArea, pos);
        	
        	// Do we have to do anything?
        	if (curArea != posArea || curInsertBefore != posInsertBefore) {
        		// (Re)dock the trim in the new location
        		dock(posArea, posInsertBefore);
        	}
        }
        
        private void trackUndocked(Point pos) {
        	// Find the closest insertion caret
        	int minDist = Integer.MAX_VALUE;
        	InsertCaret closestCaret = null;
        	
        	for (Iterator iter = insertCarets.iterator(); iter.hasNext();) {
				InsertCaret caret = (InsertCaret) iter.next();
				Point caretPos = Geometry.centerPoint(caret.getBounds());
				int dist = Geometry.distanceSquared(pos, caretPos);
				if (dist < minDist) {
					minDist = dist;
					
					// If this is 'acceptable' then set the caret
					if (minDist <= (threshold*threshold))
							closestCaret = caret;
				}
			}
        	
        	// Update the various controls...
        	updateUndockedFeedback(pos, closestCaret);
        }
        
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
         * Provide the dragging feedback beased on the newly calculated drop info
         * 
         * @param pos The current cursor pos (in the LOCAL coordinate system)
         * @param newSide The new side that the trim will be dropped onto (<code>SWT.NONE</code>
         * indicates that the trim is not near -any- insertion point
         * @param newInsertBefore the trim that the dragged trim should be inserted before
         * (this may be <code>null</code>, indicating that it should be placed at the 'end')
         */
        private void updateUndockedFeedback(Point pos, InsertCaret closestCaret) {
        	// Handle 'undocked' tracking and transitions from the 'docked' to 'undocked' state 
        	if (closestCaret == null) {
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
		
        private void setCurCaret(InsertCaret newCaret) {
        	// un-highlight the 'old' caret (if any)
        	if (curCaret != null)
        		curCaret.setHighlight(false);
        	
        	curCaret = newCaret;
        	
        	// highlight the 'new' caret (if any)
        	if (curCaret != null)
        		curCaret.setHighlight(true);
        }
        
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
        
        private void createInsertPoints() {
        	// Find the closest insertion point. Only 'valid' areas are checked
        	int[] areaIds = layout.getAreaIds();
        	for (int i = 0; i < areaIds.length; i++) {
        		// Only check 'valid' sides
				if ((getValidSides() & areaIds[i]) != 0) {
					Point[] pnts = calculateInsertionPoints(areaIds[i]);
					for (int j = 0; j < pnts.length; j++) {
						insertCarets.add(new InsertCaret(windowComposite, pnts[j], areaIds[i], getInsertTrim(areaIds[i], j)));
					}
				}
			}
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
        		dock(previousAreaId, previousInsertBefore);
               	return;
        	}
        	
        	// Dock into the location specified by the 'curCaret'
        	dock(curCaret.getAreaId(), curCaret.getInsertTrim());
        }

        /**
         * Remove the trim frmo its current 'docked' location and attach it
         * to the cursor...
         */
        private void undock() {
        	// Remember the location that we -were- in...
        	previousAreaId = layout.getTrimAreaId(draggedTrim.getControl());
        	
        	// Determine who we were placed 'before' in the trim
        	List trimDescs = layout.getTrimArea(previousAreaId).getDescriptors();
        	for (Iterator iter = trimDescs.iterator(); iter.hasNext();) {
				TrimDescriptor tDesc = (TrimDescriptor) iter.next();
				if (tDesc.getTrim() == draggedTrim) {
					if (iter.hasNext())
						previousInsertBefore = ((TrimDescriptor)iter.next()).getTrim();
					else
						previousInsertBefore = null;
				}
			}
        	
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
        private void dock(int areaId, IWindowTrim insertBefore) {
			// dispose all the carets and the dragging border
			for (Iterator iter = insertCarets.iterator(); iter.hasNext();) {
				InsertCaret caret = (InsertCaret) iter.next();
				caret.dispose();
			}
			insertCarets.clear();
			curCaret = null;
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
        	if (!docked)
	            return DragCursors.getCursor(DragCursors
	                    .positionToDragCursor(curCaret == null ? SWT.NONE : curCaret.getAreaId()));
        	else
        		return DragCursors.getCursor(DragCursors.positionToDragCursor(cursorAreaId));
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
				dock(previousAreaId, previousInsertBefore);
			}
			
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
    	
    	// If this is the -first- drag then inform the drop target
    	IWindowTrim trim = (IWindowTrim) draggedObject;
    	if (dropTarget.draggedTrim == null)
    		dropTarget.setTrim(trim);
    	
    	dropTarget.track(position);
    	
    	// Spin the paint loop after every track
    	windowComposite.getDisplay().update();
    	
		return dropTarget;
    }
}
