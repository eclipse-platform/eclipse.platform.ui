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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWindowTrim;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDropTarget2;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimArea;
import org.eclipse.ui.internal.layout.TrimDescriptor;
import org.eclipse.ui.internal.layout.TrimLayout;

/**
 */
/*package*/class TrimDropTarget implements IDragOverListener {

	private final class DragBorder {
		private Control dragControl = null;
		private Canvas border = null;
		
		public DragBorder(Control toDrag) {
			dragControl = toDrag;
			Point dragSize = toDrag.getSize();
			
			// Create a control large enough to 'contain' the dragged control
			border = new Canvas(dragControl.getParent(), SWT.NONE);
			border.setSize(dragSize.x+2, dragSize.y+2);
			
			// Ensure the border is vosible and the control is 'above' it...
			border.moveAbove(null);
			dragControl.moveAbove(null);
			
			border.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					e.gc.setBackground(new Color(border.getDisplay(), 0,0,0));
					e.gc.setBackground(new Color(border.getDisplay(), 0,0,0));
					Rectangle bb = border.getBounds();
					e.gc.fillRectangle(0,0,bb.width, bb.height);
				}
			});
		}
		
        
        public void setTrimLocation(Point newPos) {
    		// Move the border but ensure that it is still inside the Client area
        	border.setLocation(newPos.x, newPos.y);
    		Rectangle bb = border.getBounds();
    		Rectangle cr = windowComposite.getClientArea();
    		Geometry.moveInside(bb,cr);
    		
    		// OK, now move the drag control and the border to their new locations
    		dragControl.setLocation(bb.x+1, bb.y+1);
    		border.setBounds(bb);
        }


		public void dispose() {
			border.dispose();
		}


		public Rectangle getBounds() {
			return border.getBounds();
		}
	}
	
	private final class InsertCaret {
		private Canvas caretControl;
		private static final int size = 10;
		private Color baseColor;
		
		private int areaId;
		private IWindowTrim insertBefore;
		
		public InsertCaret(Composite parent, Point pos, int areaId, int insertIndex) {
			caretControl = new Canvas (parent, SWT.NONE);
			caretControl.setSize(size, size);
			caretControl.setVisible(false);

			// Remember the trim item that this insert is associated with
			this.areaId = areaId;
			this.insertBefore = getInsertTrim(insertIndex);

			// set up the painting vars
			//baseColor = new Color (parent.getDisplay(), 0,255,0);
			baseColor = caretControl.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
			
			caretControl.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					e.gc.setBackground(baseColor);
					e.gc.setForeground(baseColor);

					switch (getAreaId()) {
					case SWT.LEFT:
						{
							int[] points = { 0, size/2, size, 0, size, size };
							e.gc.fillPolygon(points);
						}
						break;
					case SWT.RIGHT:
						{
							int[] points = { size, size/2, 0, size, 0, 0 };
							e.gc.fillPolygon(points);
						}
						break;
					case SWT.TOP:
						{
							int[] points = { size/2, 0, 0, size-1, size, size-1 };
							e.gc.fillPolygon(points);
						}
						break;
					case SWT.BOTTOM:
						{
							int[] points = { size/2, size, 0, 0, size, 0 };
							e.gc.fillPolygon(points);
						}
						break;
					}
				}
			});
			
			showCaret(pos, areaId);
		}

		/**
		 * @return The area ID that this caret is 'on'
		 */
		public int getAreaId() {
			return areaId;
		}

		/**
		 * @return The 'beforeMe' trim for this insertion caret
		 */
		public IWindowTrim getInsertTrim() {
			return insertBefore;
		}
		
		private IWindowTrim getInsertTrim(int index) {
			TrimArea area = layout.getTrimArea(getAreaId());
			List descs = area.getDescriptors();

			// If the index is at the end then the insert 'trim' is null
			if (index >= descs.size())
				return null;
			
			// Get the trim from the description
			TrimDescriptor desc = (TrimDescriptor) descs.get(index);
			return desc.getTrim();
		}
		
		public void showCaret(Point pos, int side) {
			areaId = side;
			
			switch (side) {
			case SWT.LEFT:
				caretControl.setLocation(pos.x, pos.y - (size/2));
				break;
			case SWT.RIGHT:
				caretControl.setLocation(pos.x-size, pos.y - (size/2));
				break;
			case SWT.TOP:
				caretControl.setLocation(pos.x-(size/2), pos.y);
				break;
			case SWT.BOTTOM:
				caretControl.setLocation(pos.x-(size/2), pos.y - size);
				break;
			}
			
			// Force the control into the client rect
    		Rectangle bb = caretControl.getBounds();
    		Rectangle cr = windowComposite.getClientArea();
    		Geometry.moveInside(bb,cr);
    		caretControl.setBounds(bb);
    		
			caretControl.moveAbove(null);
			caretControl.setVisible(true);
			caretControl.redraw();
		}
		
		public void hideCaret() {
			caretControl.setVisible(false);
		}

		public Rectangle getBounds() {
			return caretControl.getBounds();
		}

		public void dispose() {
			//baseColor.dispose();
			caretControl.dispose();
		}
	}
	
    private final class ActualTrimDropTarget implements IDropTarget2 {
        public IWindowTrim draggedTrim;
        
        // tracking parameters
    	private int threshold = 100;  // How close to a caret the cursor has to be to be 'valid'
    	private List insertCarets = new ArrayList();
    	private InsertCaret curCaret;
    	private DragBorder border = null;
    	private boolean tracking = false;
        
        // Holder for the position of trim that is 'floating' with the cursor
        private int initialAreaId;
        private IWindowTrim initialInsertBefore;

        private ActualTrimDropTarget() {
            super();

            draggedTrim = null;
            tracking = false;
            
            initialAreaId = SWT.NONE;
            initialInsertBefore = null;
        }
        
        public void setTrim(IWindowTrim trim) {
        	// Are we starting a new drag?
        	if (draggedTrim != trim) {
        		initialAreaId = SWT.NONE;
        		initialInsertBefore = null;
            	
            	// remember the dragged trim
            	draggedTrim = trim;
            	
            	tracking = false;
        	}
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
	        	};
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
        	// First things first...'undock' the dragged trim
        	if (!tracking)
        		startTracking();
        	
        	// Convert the mouse positon into 'local' coords
        	Rectangle r = new Rectangle(pos.x, pos.y, 1,1);
        	r = Geometry.toControl(windowComposite, r);
        	pos.x = r.x;
        	pos.y = r.y;
        	        	
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
        	
        	updateFeedback(pos, closestCaret);
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
        private void updateFeedback(Point pos, InsertCaret closestCaret) {
        	// Handle 'undocked' tracking and transitions from the 'docked' to 'undocked' state 
        	if (closestCaret == null) {
        		border.setTrimLocation(pos);
        		
        		// update the state vars
        		curCaret = null;
        		return;
        	}
        	
        	// If nothing has changed then it's a no-op
        	if (curCaret == closestCaret)
        		return;

        	// We're over a -new- insertion point; update the feedback
        	// remember the new 'drop' location
        	curCaret = closestCaret;
			
			// 'snap' the control to the insertion point
			Rectangle ctrlRect = draggedTrim.getControl().getBounds();
			Rectangle caretRect = curCaret.getBounds();
			Point caretCenter = new Point(caretRect.x+(caretRect.width/2), caretRect.y+(caretRect.height/2));
        	switch (curCaret.getAreaId()) {
				case SWT.TOP:
					border.setTrimLocation(new Point(caretCenter.x-(ctrlRect.width/2), caretRect.y+caretRect.height+1));
					break;
				case SWT.LEFT:
					border.setTrimLocation(new Point(caretRect.x+caretRect.width+2, caretCenter.y-(ctrlRect.height/2)));
					break;
				case SWT.RIGHT:
					border.setTrimLocation(new Point((caretRect.x-(ctrlRect.width))-3, caretCenter.y-(ctrlRect.height/2)));
					break;
				case SWT.BOTTOM:
					border.setTrimLocation(new Point(caretCenter.x-(ctrlRect.width/2), (caretRect.y-(ctrlRect.height))-3));
					break;
        	};
        }
                
        private void startTracking() {
        	if (!tracking) {
	        	// Remember the location that we -were- in...
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
	        	
	        	// Remove the trim from the layout
	        	layout.removeTrim(draggedTrim);
	           	LayoutUtil.resize(draggedTrim.getControl());
	           	
	           	// Create a new dragging border onto the dragged trim
	           	border = new DragBorder(draggedTrim.getControl());

	           	// Create and show the insertion points
	           	createInsertPoints();
	           	curCaret = null;
	           	
	        	tracking = true;
        	}
        }
        
        private void createInsertPoints() {
        	// Find the closest insertion point. Only 'valid' areas are checked
        	int[] areaIds = layout.getAreaIds();
        	for (int i = 0; i < areaIds.length; i++) {
        		// Only check 'valid' sides
				if ((getValidSides() & areaIds[i]) != 0) {
					Point[] pnts = calculateInsertionPoints(areaIds[i]);
					for (int j = 0; j < pnts.length; j++) {
						insertCarets.add(new InsertCaret(windowComposite, pnts[j], areaIds[i], j));
					}
				}
			}
		}

		/* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
         */
        public void drop() {
        	// If we're already docked then there's nothing to do
        	if (!tracking)
        		return;
        	
        	// If we try to drop while in no-man's land then re-dock to the last known location
        	if (curCaret == null) {
        		redock();
               	return;
        	}
        	
        	// If we're changing orientation we have to get the IWindowTrim to re-create the control
        	boolean initialHorizontal = (initialAreaId == SWT.TOP) || 
			(initialAreaId == SWT.BOTTOM);
        	boolean curHorizontal = (curCaret.getAreaId() == SWT.TOP) || 
			(curCaret.getAreaId() == SWT.BOTTOM);
            if (curHorizontal != initialHorizontal) {
                draggedTrim.dock(curCaret.getAreaId());
            }

            // handle rearrangements within the trim
            layout.addTrim(curCaret.getAreaId(), draggedTrim, curCaret.getInsertTrim());
           	LayoutUtil.resize(draggedTrim.getControl());
        }

        /**
         * Return the 'undocked' trim to its previous location in the layout
         */
        private void redock() {        	
            layout.addTrim(initialAreaId, draggedTrim, initialInsertBefore);
           	LayoutUtil.resize(draggedTrim.getControl());
        }
        	
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
         */
        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors
                    .positionToDragCursor(curCaret == null ? SWT.NONE : curCaret.getAreaId()));
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
			if (!dropPerformed) {
				// Force the dragged trim back into its original position...				
				curCaret = null;
				
				drop();
			}
			
			// dispose all the carets
			for (Iterator iter = insertCarets.iterator(); iter.hasNext();) {
				InsertCaret caret = (InsertCaret) iter.next();
				caret.dispose();
			}
			insertCarets.clear();
			curCaret = null;
			border.dispose();
			
			draggedTrim = null; // indicates we're not dragging
			tracking = false;
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
    	
    	IWindowTrim trim = (IWindowTrim) draggedObject;
    	dropTarget.setTrim(trim);
    	
    	dropTarget.track(position);
    	
    	// Spin the paint loop after every track
    	windowComposite.getShell().update();
    	
		return dropTarget;
    }
}
