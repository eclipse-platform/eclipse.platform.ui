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

/**
 */
/*package*/ class TrimDropTarget implements IDragOverListener {

	private TrimLayout layout;
	private Composite windowComposite;
	private WorkbenchWindow window;
		
	private static final float edgeDockRatio = 0.20f;
	
	public TrimDropTarget(Composite someComposite, WorkbenchWindow theWindow) {
		layout = (TrimLayout)someComposite.getLayout();
		windowComposite = someComposite;
		window = theWindow;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
	 */
	public IDropTarget drag(Control currentControl, Object draggedObject, Point position, final Rectangle dragRectangle) {

		// Handle dropping window trim on the border of the workbench (for example,
		// the fast view bar)
		if (draggedObject instanceof IWindowTrim) {
			final IWindowTrim draggedTrim = (IWindowTrim)draggedObject;
			
			Control trimControl = draggedTrim.getControl();

			if (trimControl.getParent() == windowComposite) {
				Control targetTrim = getTrimControl(currentControl);
				
				if (targetTrim != null) {
					int side = layout.getTrimLocation(targetTrim);
					
					if (side == SWT.DEFAULT) {
						if (targetTrim == layout.getCenterControl()) {
							side = CompatibilityDragTarget.getRelativePosition(targetTrim, position);
							if (side == SWT.CENTER) {
								side = SWT.DEFAULT;
							}
							
							targetTrim = null;
						}
					}
					
					if (side != SWT.DEFAULT && (targetTrim != trimControl) 
							&& (targetTrim != null || side != layout.getTrimLocation(trimControl)) 
							&& ((side & draggedTrim.getValidSides()) != 0)) {
						final int dropSide = side; 
						final Control insertionPoint = targetTrim;
						
						return new AbstractDropTarget() {
							public void drop() {
								draggedTrim.dock(dropSide);
							}

							public Cursor getCursor() {
								return DragCursors.getCursor(DragCursors.positionToDragCursor(dropSide));
							}
							
							public Rectangle getSnapRectangle() {
								
								int smaller = Math.min(dragRectangle.width, dragRectangle.height);
								
								return Geometry.toDisplay(windowComposite, Geometry.getExtrudedEdge(windowComposite.getClientArea(), 
									smaller, dropSide));							
							}
						};
					}
				}
			}			
		}
		
		// Handle dropping a view on the border of the workbench (docks the view to the edge).
		if (draggedObject instanceof ViewPane
				|| draggedObject instanceof PartTabFolder) {
			final LayoutPart draggedPane = (LayoutPart)draggedObject;
			
			// We can't drag between workbench windows
			if (draggedPane.getWorkbenchWindow() != window) {
				return null;
			}
			
			if (draggedPane instanceof PartTabFolder) {
				PartTabFolder folder = (PartTabFolder)draggedPane;
				if (folder.getWindow() != window) {
					return null;
				}
			}
			
			// Determine which border we're dragging over
			final Rectangle bounds = Geometry.toDisplay(layout.getCenterControl().getParent(), 
					layout.getCenterControl().getBounds());
			
			// Stores the side as an SWT.* constant
			final int relativePosition = Geometry.getClosestSide(bounds, position);
			if (bounds.contains(position)) {
				return null;
			}
			
			final RootLayoutContainer sashContainer = window.getActiveWorkbenchPage().getPerspectivePresentation().getLayout();

			return new AbstractDropTarget() {
				public void drop() {
					// Drop the part on this border
					window.getActiveWorkbenchPage().getPerspectivePresentation().derefPart(draggedPane);
					
					PartTabFolder folder;
					if (draggedPane instanceof PartTabFolder) {
						folder = (PartTabFolder)draggedPane;
					} else {
						// Create a new folder and add both items
						folder = new PartTabFolder(window.getActiveWorkbenchPage());
						folder.add(draggedPane);
					}
					sashContainer.addEnhanced(
						folder,
						relativePosition,
						edgeDockRatio,
						null);
					draggedPane.setFocus();
				}

				public Cursor getCursor() {
					// Return a cursor that points in the opposite direction from the current side of
					// the layout
					return DragCursors.getCursor(
							DragCursors.positionToDragCursor(
							Geometry.getOppositeSide(relativePosition)));
				}
				
				public Rectangle getSnapRectangle() {
					// Compute the size of the view once docked
					int sz = (int) (Geometry.getDimension(bounds, !Geometry.isHorizontal(relativePosition)) 
						* edgeDockRatio);
					
					// Extrude the edge of the layout to the given size
					return Geometry.getExtrudedEdge(bounds, sz, relativePosition); 					
				}
			};

		}
		
		return null;
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
