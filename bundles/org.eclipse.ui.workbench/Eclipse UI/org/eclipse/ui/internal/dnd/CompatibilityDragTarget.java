/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.DragCursors;
import org.eclipse.ui.internal.IPartDropListener;
import org.eclipse.ui.internal.IWorkbenchDragSource;
import org.eclipse.ui.internal.IWorkbenchDropTarget;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartDropEvent;
import org.eclipse.ui.internal.PerspectivePresentation;

/**
 * Compatibility layer for the old-style drag-and-drop. Adapts an old-style
 * IPartDropListener into an IDragTarget.
 * 
 */
public class CompatibilityDragTarget implements IDragOverListener {

	// Define width of part's "hot" border	
	private final static int MARGIN = 15;
	
	private IPartDropListener listener;
	private int type;
	private IWorkbenchWindow window;

	class DropTarget extends AbstractDropTarget {
		private PartDropEvent dropEvent;

		DropTarget(PartDropEvent dropEvent) {
			this.dropEvent = dropEvent;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
		 */
		public void drop() {
			listener.drop(dropEvent);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
		 */
		public Cursor getCursor() {
			return DragCursors.getCursor(dropEvent.relativePosition);
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.dnd.IDropTarget#getSnapRectangle()
		 */
		public Rectangle getSnapRectangle() {
			int direction = DragCursors.dragCursorToSwtConstant(dropEvent.relativePosition);
			
			if (direction == SWT.DEFAULT || dropEvent.dropTarget == null) {
				return null;
			}
			
			Rectangle targetBounds = DragUtil.getDisplayBounds(dropEvent.dropTarget.getControl()); 
			
			if (direction == SWT.CENTER) {
				return targetBounds;
			}
			
			int distance = Geometry.getDimension(targetBounds, !Geometry.isHorizontal(direction));
			
			return Geometry.getExtrudedEdge(targetBounds, (int) (distance
					* PerspectivePresentation.getDockingRatio(dropEvent.dragSource.getPart(),
							dropEvent.dropTarget.getPart())),
							direction);
		}
	}
	
	/**
	 * Returns a drag event representing the current state of dragging.
	 */
	private PartDropEvent createDropEvent(Control targetControl, Point position, IWorkbenchDragSource sourcePart, Point eventLoc) {

		PartDropEvent event = new PartDropEvent();

		event.x = eventLoc.x;
		event.y = eventLoc.y;
		event.dragSource = sourcePart;
		
		Control sourceControl = sourcePart.getControl();
		if (sourceControl == null) {
			event.cursorX = event.x;
			event.cursorY = event.y;
		} else {
			Point relativePosition = sourceControl.toControl(position);
			event.cursorX = relativePosition.x;
			event.cursorY = relativePosition.y;
		}

		if (targetControl == null) {
			// cursor is outside of the shell
			event.relativePosition = DragCursors.OFFSCREEN;
			return event;
		}
				
		LayoutPart targetPart = getTargetPart(targetControl, sourcePart);
		if (targetPart == null) {
			event.relativePosition = DragCursors.INVALID;
			return event;
		}

		event.dropTarget = targetPart;
		Control c = targetPart.getControl();
		if (c == null) {
			event.relativePosition = DragCursors.INVALID;
			return event;
		}

		event.relativePosition = DragCursors.positionToDragCursor(getRelativePosition(c, position));
		return event;
	}
	
	/**
	 * Returns the relative position of the given point (in display coordinates)
	 * with respect to the given control. Returns one of SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.TOP, 
	 * or SWT.BOTTOM if the point is on the control or SWT.DEFAULT if the point is not on the control. 
	 * 
	 * @param control control to perform hit detection on
	 * @param toTest point to test, in display coordinates
	 * @return
	 */
	public static int getRelativePosition(Control c, Point toTest) {
		Point p = c.toControl(toTest);
		Point e = c.getSize();
		
		if (p.x > e.x || p.y > e.y || p.x < 0 || p.y < 0) {
			return SWT.DEFAULT;
		}

		// first determine whether mouse position is in center of part
		int hmargin = Math.min(e.x / 3, MARGIN);
		int vmargin = Math.min(e.y / 3, MARGIN);

		Rectangle inner = new Rectangle(hmargin, vmargin, e.x - (hmargin * 2), e.y - (vmargin * 2));
		if (inner.contains(p)) {
			return SWT.CENTER;
		} else {
			// normalize to center
			p.x -= e.x / 2;
			p.y -= e.y / 2;

			// now determine quadrant
			double a = Math.atan2(p.y * e.x, p.x * e.y) * (180 / Math.PI);

			if (a >= -135 && a < -45)
				return SWT.TOP;
			else if (a > -45 && a < 45)
				return SWT.RIGHT;
			else if (a > 45 && a < 135)
				return SWT.BOTTOM;
			else
				return SWT.LEFT;
		}
	}
	
	/**
	 * Returns the target part containing a particular control.  If the
	 * target part is not in the same window as the source part return null.
	 */
	private LayoutPart getTargetPart(Control target, IWorkbenchDragSource sourcePart) {		
		while (target != null) {
			Object data = target.getData();
			if (data instanceof IWorkbenchDropTarget) {
				//Found the one who handles drops and then the target
				//for it
				IWorkbenchDropTarget dropTarget = (IWorkbenchDropTarget) data;

				return dropTarget.targetPartFor(sourcePart);
			}
			target = target.getParent();
		}
		return null;
	}
	
	/**
	 * Return whether or not there is a valid drag/drop between the
	 * sourcePart and the dropTarget.
	 * 
	 * @param source
	 * @param dropTarget
	 * @return boolean
	 */
	private boolean isValid(IWorkbenchDragSource source, IWorkbenchDropTarget dropTarget){
		return (source.getType() & dropTarget.getType()) > 0;
	}
	
	public CompatibilityDragTarget(IPartDropListener listener, int type, IWorkbenchWindow window) {
		this.listener = listener;
		this.type = type;
		this.window = window;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragTarget#dropDetect(java.lang.Object, org.eclipse.swt.graphics.Point)
	 */
	public IDropTarget drag(Control currentControl, Object draggedObject, Point position, Rectangle dragRectangle) {
		if (draggedObject instanceof IWorkbenchDragSource) {
			IWorkbenchDragSource source = (IWorkbenchDragSource)draggedObject;
			
			if ((source.getType() & type) == 0) {
				return null;
			}
			
			if (source.getWorkbenchWindow() != window) {
				return null;
			}
			
			PartDropEvent dropEvent = createDropEvent(currentControl, position, source, new Point(dragRectangle.x, dragRectangle.y));
			 
			listener.dragOver(dropEvent);
			
			if (dropEvent.relativePosition != DragCursors.INVALID) {
				return new DropTarget(dropEvent);
			}
		}
		
		return null;
	}

}
