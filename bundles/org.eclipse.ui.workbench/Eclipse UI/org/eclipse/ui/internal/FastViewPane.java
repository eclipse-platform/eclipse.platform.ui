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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.internal.presentations.PartTabFolderPresentation;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

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
	private ViewPane currentPane;
	private Composite clientComposite;
	private static final int SASH_SIZE = 3;
	private static final int MIN_FASTVIEW_SIZE = 10;
	private float ratio;
	private DefaultStackPresentationSite site = new DefaultStackPresentationSite() {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.skins.IPresentationSite#setState(int)
		 */
		public void setState(int newState) {
			ViewPane pane = currentPane;
			switch(newState) {
				case IPresentationSite.STATE_MINIMIZED: 
					currentPane.getPage().toggleFastView(currentPane.getViewReference());
					break;
				case IPresentationSite.STATE_MAXIMIZED:
					pane.getPage().removeFastView(pane.getViewReference());
				    pane.doZoom();
					break;
				case IPresentationSite.STATE_RESTORED:
					pane.getPage().removeFastView(pane.getViewReference());
					break;
				default:
			}
		}
		
		public void close(IPresentablePart part) {
			if (!isClosable(part)) {
				return;
			}
			currentPane.getPage().hideView(currentPane.getViewReference());
		}
	};
	
	private Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.Resize && currentPane != null) {
				setFastViewSizeUsingRatio();
				
				currentPane.moveAbove(null);
				updateFastViewSashBounds(getBounds());
			}
		}
	};
		
	private SelectionAdapter selectionListener = new SelectionAdapter () {
		public void widgetSelected(SelectionEvent e) {
			if (e.detail == SWT.DRAG && currentPane != null) {
				Rectangle bounds = getBounds();
				Point location = new Point(e.x, e.y);
				int distanceFromEdge = Geometry.getDistanceFromEdge(bounds, location, side);
				if (distanceFromEdge < MIN_FASTVIEW_SIZE) {
					distanceFromEdge = MIN_FASTVIEW_SIZE;
				}
				
				if (side == SWT.BOTTOM || side == SWT.RIGHT) {
					distanceFromEdge -= SASH_SIZE;
				}
				
				Rectangle newBounds = Geometry.getExtrudedEdge(bounds, distanceFromEdge, side);
				
				getPresentation().setBounds(newBounds);
				currentPane.moveAbove(null); 
				
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
		int currentSize = Geometry.getDimension(getBounds(), isVertical);
		
		return (float)currentSize / (float)clientSize;
	}

	private Rectangle getBounds() {
		return getPresentation().getControl().getBounds();
	}
	
	private void setFastViewSizeUsingRatio() {
		// Set initial bounds
		boolean isVertical = !Geometry.isHorizontal(side);
		Rectangle clientArea = clientComposite.getClientArea();

		int defaultSize = (int) (Geometry.getDimension(clientArea, isVertical) * ratio);
		Rectangle newBounds = Geometry.getExtrudedEdge(clientArea, defaultSize, side);
		getPresentation().setBounds(newBounds);
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
		
		StackPresentation presentation = new PartTabFolderPresentation(newClientComposite, site, SWT.MIN | SWT.MAX, pane.getPage().getTheme());
		
		site.setPresentation(presentation);
		site.setPresentationState(IPresentationSite.STATE_MAXIMIZED);
		presentation.addPart(pane.getPresentablePart(), null);
		presentation.selectPart(pane.getPresentablePart());
		presentation.setActive(true);
		presentation.setVisible(true);
		
		setFastViewSizeUsingRatio();
		
		// Show pane fast.
		ctrl.setEnabled(true); // Add focus support.
		Composite parent = ctrl.getParent();
		Rectangle bounds = getFastViewBounds();

		pane.setVisible(true);
		presentation.setBounds(bounds);
		presentation.getControl().moveAbove(null);
		pane.moveAbove(null);
		pane.setFocus();
		
		fastViewSash = new Sash(parent, Geometry.getSwtHorizontalOrVerticalConstant(Geometry.isHorizontal(side)));
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
		site.dispose();
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
		int viewSize = Math.min(Geometry.getDimension(getBounds(), isVertical),
				clientSize - MIN_FASTVIEW_SIZE);
		
		return Geometry.getExtrudedEdge(clientArea, viewSize, side);
	}
	
	/**
	 * @return
	 */
	private StackPresentation getPresentation() {
		return site.getPresentation();
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
		
		clientComposite.removeListener(SWT.Resize, resizeListener);
		
		// Get pane.
		// Hide the right side sash first
		hideFastViewSash();
		Control ctrl = currentPane.getControl();
		
		// Hide it completely.
		getPresentation().setVisible(false);
		site.dispose();
		currentPane.setFastViewSash(null);
		ctrl.setEnabled(false); // Remove focus support.
		
		currentPane = null;
	}
	
	/**
	 * @return Returns the currently visible fastview or null if none
	 */
	public ViewPane getCurrentPane() {
		return currentPane;
	}

}
