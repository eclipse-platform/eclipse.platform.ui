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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

import org.eclipse.jface.util.Geometry;

/**
 * Handles the presentation of an active fastview. A fast view pane docks to one side of a
 * parent composite, and is capable of displaying a single view. The view may be resized.
 * Displaying a new view will hide any view currently being displayed in the pane. 
 * 
 * Currently, the fast view pane does not own or contain the view. It only controls the view's 
 * position and visibility.  
 * 
 * @see org.ecliplse.ui.internal.FastViewBar
 */
class FastViewPane {
	private int side = SWT.LEFT;

	private Sash fastViewSash;
	private Color borderColor1;
	private Color borderColor2;
	private Color borderColor3;
	// TODO these colors should (probably) not be hardcoded
	private static final RGB RGB_COLOR1 = new RGB(132, 130, 132);
	private static final RGB RGB_COLOR2 = new RGB(143, 141, 138);
	private static final RGB RGB_COLOR3 = new RGB(171, 168, 165);
	private ViewPane currentPane;
	private Composite clientComposite;
	private static final int SASH_SIZE = 3;
	private static final int MIN_FASTVIEW_SIZE = 10;
	private float ratio;
	
	private Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.Resize && currentPane != null) {
				ViewPane pane = currentPane;
				if (pane.isZoomed() == false) {
					setFastViewSizeUsingRatio();
					
					pane.moveAbove(null);
					updateFastViewSashBounds(pane.getBounds());
				}
			}
		}
	};
	
	private PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent event) {
			if (borderColor1 == null) borderColor1 = WorkbenchColors.getColor(RGB_COLOR1);
			if (borderColor2 == null) borderColor2 = WorkbenchColors.getColor(RGB_COLOR2);
			if (borderColor3 == null) borderColor3 = WorkbenchColors.getColor(RGB_COLOR3);
			
			Point size = fastViewSash.getSize();
			Rectangle d = new Rectangle(0, 0, size.x, size.y);
			GC gc = event.gc;
			
			int deltay = 0;
			int deltax = 0;

			if (Geometry.isHorizontal(side)) {
 				deltax = d.width;
			} else {
				deltay = d.height;
			}

			gc.setForeground(borderColor1);
			gc.drawLine(d.x, d.y, d.x + deltax, d.y + deltay);
			
			gc.setForeground(borderColor2);
			gc.drawLine(d.x + 1, d.y + 1, d.x + 1 + deltax, d.y + 1 + deltay);
			
			gc.setForeground(borderColor3);
			gc.drawLine(d.x + 2, d.y + 2, d.x + 2 + deltax, d.y + 2 + deltay);
		}
	};
	
	private SelectionAdapter selectionListener = new SelectionAdapter () {
		public void widgetSelected(SelectionEvent e) {
			if (e.detail == SWT.DRAG && currentPane != null) {
				ViewPane pane = currentPane;
				Rectangle bounds = pane.getBounds();
				Point location = new Point(e.x, e.y);
				int distanceFromEdge = Geometry.getDistanceFromEdge(bounds, location, side);
				if (distanceFromEdge < MIN_FASTVIEW_SIZE) {
					distanceFromEdge = MIN_FASTVIEW_SIZE;
				}
				Rectangle newBounds = Geometry.getExtrudedEdge(bounds, distanceFromEdge, side);
				
				pane.setBounds(newBounds);
				pane.moveAbove(null); 
				
				updateFastViewSashBounds(newBounds);
				
				ratio = getCurrentRatio();
			}
		}
	};

	/**
	 * Returns the current fastview size ratio. Returns 0.0 if there is no fastview visible.
	 * 
	 * @return
	 */
	public float getCurrentRatio() {
		if (currentPane == null) {
			return 0.0f;
		}
		
		boolean isVertical = !Geometry.isHorizontal(side);
		Rectangle clientArea = clientComposite.getClientArea();

		int clientSize = Geometry.getDimension(clientArea, isVertical);
		int currentSize = Geometry.getDimension(currentPane.getBounds(), isVertical);
		
		return (float)currentSize / (float)clientSize;
	}

	private void setFastViewSizeUsingRatio() {
		// Set initial bounds
		boolean isVertical = !Geometry.isHorizontal(side);
		Rectangle clientArea = clientComposite.getClientArea();

		int defaultSize = (int) (Geometry.getDimension(clientArea, isVertical) * ratio);
		Rectangle newBounds = Geometry.getExtrudedEdge(clientArea, defaultSize, side);
		currentPane.setBounds(newBounds);
	}
	
	/**
	 * Displays the given view as a fastview. The view will be docked to the edge of the
	 * given composite until it is subsequently hidden by a call to hideFastView. 
	 * 
	 * @param newClientComposite
	 * @param pane
	 * @param newSide
	 */
	public void showView(Composite newClientComposite, ViewPane pane, int newSide, float sizeRatio) {
		side = newSide;
		
		if (currentPane != null) {
			hideView();
		}
	
		currentPane = pane;
		ratio = sizeRatio;
		
		clientComposite = newClientComposite;
	
		clientComposite.addListener(SWT.Resize, resizeListener);

		if (fastViewSash != null) {
			fastViewSash.dispose();
			fastViewSash = null;
		}
		
		// Create the control first
		Control ctrl = pane.getControl();
		if (ctrl == null) {
			pane.createControl(clientComposite);
			ctrl = pane.getControl();			
		}
		
		setFastViewSizeUsingRatio();
		
		// Show pane fast.
		ctrl.setEnabled(true); // Add focus support.
		Composite parent = ctrl.getParent();
		Rectangle bounds = getFastViewBounds();

		pane.setVisible(true);
		pane.setBounds(bounds);
		pane.moveAbove(null);
		pane.setFocus();
		
		fastViewSash = new Sash(parent, Geometry.getSwtHorizontalOrVerticalConstant(Geometry.isHorizontal(side)));
		fastViewSash.addPaintListener(paintListener);
		fastViewSash.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				fastViewSash.removePaintListener(paintListener);
			}
			public void focusLost(FocusEvent e) {
				fastViewSash.addPaintListener(paintListener);
			}
		});
		fastViewSash.addSelectionListener(selectionListener);

		pane.setFastViewSash(fastViewSash);
		updateFastViewSashBounds(bounds);
	}
	
	/**
	 * Updates the position of the resize sash.
	 * 
	 * @param bounds
	 */
	private void updateFastViewSashBounds(Rectangle bounds) {
		int oppositeSide = Geometry.getOppositeSide(side);
		Rectangle newBounds = Geometry.getExtrudedEdge(bounds, -SASH_SIZE, oppositeSide);
		
		fastViewSash.setBounds(newBounds);
		fastViewSash.moveAbove(null);
	}
	
	/**
	 * Disposes of any active widgetry being used for the fast view pane. Does not dispose
	 * of the view itself.
	 */
	public void dispose() {
		// Dispose of the sash too...
		if (fastViewSash != null) {
			fastViewSash.dispose();
			fastViewSash = null;
		}
	}

	/**
	 * Returns the bounding rectangle for the currently visible fastview, given the rectangle
	 * in which the fastview can dock. 
	 * 
	 * @param clientArea
	 * @param ratio
	 * @param orientation
	 * @return
	 */
	private Rectangle getFastViewBounds() {
		Rectangle clientArea = clientComposite.getClientArea();

		boolean isVertical = !Geometry.isHorizontal(side);
		int clientSize = Geometry.getDimension(clientArea, isVertical);
		int viewSize = Math.min(Geometry.getDimension(currentPane.getControl().getBounds(), isVertical),
				clientSize - MIN_FASTVIEW_SIZE);
		
		return Geometry.getExtrudedEdge(clientArea, viewSize, side);
	}
	
	/**
	 * Hides the sash for the fastview if it is currently visible. This method may not be
	 * required anymore, and might be removed from the public interface.
	 */
	public void hideFastViewSash() {
		if (fastViewSash != null)
			fastViewSash.setBounds(0, 0, 0, 0);
	}
	
	/**
	 * Hides the currently visible fastview.
	 */
	public void hideView() {

		if (currentPane == null) {
			return;
		}
		
		// Get pane.
		// Hide the right side sash first
		hideFastViewSash();
		Control ctrl = currentPane.getControl();
		
		// Hide it completely.
		currentPane.setVisible(false);
		currentPane.setFastViewSash(null);
		ctrl.setEnabled(false); // Remove focus support.
		
		currentPane = null;

		clientComposite.removeListener(SWT.Resize, resizeListener);
	}
	
	/**
	 * @return Returns the currently visible fastview or null if none
	 */
	public ViewPane getCurrentPane() {
		return currentPane;
	}

}
