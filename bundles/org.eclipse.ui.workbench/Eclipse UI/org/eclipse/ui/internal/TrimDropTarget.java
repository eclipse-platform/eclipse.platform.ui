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
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.TrimLayout;

/**
 */
/*package*/class TrimDropTarget implements IDragOverListener {

    private final class ActualTrimDropTarget extends AbstractDropTarget {
        private Rectangle dragRectangle;

        private IWindowTrim draggedTrim;

        private int dropSide;

        private ActualTrimDropTarget(Rectangle dragRectangle, IWindowTrim draggedTrim, int dropSide) {
            super();
            this.dragRectangle = dragRectangle;
            this.draggedTrim = draggedTrim;
            this.dropSide = dropSide;
        }
        
        private void setTarget(Rectangle dragRectangle, IWindowTrim draggedTrim, int dropSide) {
            this.dragRectangle = dragRectangle;
            this.draggedTrim = draggedTrim;
            this.dropSide = dropSide;
        }

        public void drop() {
            if (dropSide != layout.getTrimLocation(draggedTrim.getControl())) {
                draggedTrim.dock(dropSide);
            }
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors
                    .positionToDragCursor(dropSide));
        }

        public Rectangle getSnapRectangle() {
            int smaller = Math.min(dragRectangle.width,
                    dragRectangle.height);

            return Geometry.toDisplay(
                    windowComposite, Geometry.getExtrudedEdge(windowComposite.getClientArea(),
                            smaller, dropSide));
        }
    }
    
    private ActualTrimDropTarget dropTarget; 

    private TrimLayout layout;

    private Composite windowComposite;

    private WorkbenchWindow window;

    public TrimDropTarget(Composite someComposite, WorkbenchWindow theWindow) {
        layout = (TrimLayout) someComposite.getLayout();
        windowComposite = someComposite;
        window = theWindow;
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

                int side = layout.getTrimLocation(targetTrim);

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

                side = Geometry.getClosestSide(window.getShell().getBounds(), position);
                
                if (side != SWT.DEFAULT
                        && ((side & draggedTrim.getValidSides()) != 0)) {
                    final int dropSide = side;
                    
                    return createDropResult(dragRectangle, draggedTrim, dropSide);
                }
            }
        }

        return null;
    }

    /**
     * Returns a drop target with the given specifications. As an optimization, the result of this method is cached
     * and the object is reused in subsequent calls.
     * 
     * @param dragRectangle
     * @param draggedTrim
     * @param dropSide
     * @return
     * @since 3.1
     */
    private IDropTarget createDropResult(final Rectangle dragRectangle, final IWindowTrim draggedTrim, final int dropSide) {
        if (dropTarget == null) {
            dropTarget = new ActualTrimDropTarget(dragRectangle, draggedTrim, dropSide);
        } else {
            dropTarget.setTarget(dragRectangle, draggedTrim, dropSide);
        }
        return dropTarget;
    }

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
