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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.SystemMenuClose;
import org.eclipse.ui.internal.presentations.SystemMenuMaximize;
import org.eclipse.ui.internal.presentations.SystemMenuMinimize;
import org.eclipse.ui.internal.presentations.SystemMenuMoveView;
import org.eclipse.ui.internal.presentations.SystemMenuRestore;
import org.eclipse.ui.internal.presentations.SystemMenuSizeFastView;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
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
public class FastViewPane {
	private int side = SWT.LEFT;

	//private Sash fastViewSash;
	private ViewPane currentPane;
	private Composite clientComposite;
	private static final int SASH_SIZE = 5;
	private static final int MIN_FASTVIEW_SIZE = 10;
	private int size;
	//private WidgetHider hider;
	private HiddenSash sash;
	
	// Counts how many times we've scheduled a redraw... use this to avoid resizing
	// the widgetry when we're getting resize requests faster than we can process them.
	// This is needed for GTK, which resizes slowly (bug 54517)
	private int redrawCounter = 0;
	
	private DefaultStackPresentationSite site = new DefaultStackPresentationSite() {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.skins.IPresentationSite#setState(int)
		 */
		public void setState(int newState) {
			super.setState(newState);
			ViewPane pane = currentPane;
			switch(newState) {
				case IStackPresentationSite.STATE_MINIMIZED: 
					currentPane.getPage().toggleFastView(currentPane.getViewReference());
					break;
				case IStackPresentationSite.STATE_MAXIMIZED:
					sash.setVisible(false);
					getPresentation().setBounds(getBounds());
					break;
				case IStackPresentationSite.STATE_RESTORED:
					sash.setVisible(true);
					getPresentation().setBounds(getBounds());
					break;
				default:
			}
		}
		
		public void close(IPresentablePart part) {
			if (!isCloseable(part)) {
				return;
			}
			currentPane.getPage().hideView(currentPane.getViewReference());
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(org.eclipse.ui.internal.skins.IPresentablePart, boolean)
		 */
		public void dragStart(IPresentablePart beingDragged, Point initialPosition, boolean keyboard) {
			dragStart(initialPosition, keyboard);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(boolean)
		 */
		public void dragStart(Point initialPosition, boolean keyboard) {
			ViewPane pane = currentPane;
			
			Control control = getPresentation().getControl(); 
			
			Rectangle bounds = Geometry.toDisplay(clientComposite,
                    control.getBounds());
			
			Perspective persp = currentPane.getPage().getActivePerspective();
			
			persp.setActiveFastView(null, 0);
			
            DragUtil.performDrag(pane, bounds,
                    initialPosition, !keyboard);
		}
		
	};
		
    private class SystemMenuContribution extends ContributionItem {
        
        private SystemMenuClose systemMenuClose;
        private SystemMenuMaximize systemMenuMaximize;
        private SystemMenuMinimize systemMenuMinimize;
        private SystemMenuMoveView systemMenuMoveView;
        private SystemMenuRestore systemMenuRestore;
        private SystemMenuSizeFastView systemMenuSizeFastView;
        
        SystemMenuContribution(IStackPresentationSite stackPresentationSite, FastViewPane fastViewPane) {
            systemMenuClose = new SystemMenuClose(fastViewPane.getCurrentPane().getPresentablePart(), stackPresentationSite);
            systemMenuMaximize = new SystemMenuMaximize(stackPresentationSite);
            systemMenuMinimize = new SystemMenuMinimize(stackPresentationSite);
            systemMenuMoveView = new SystemMenuMoveView(fastViewPane.getCurrentPane().getPresentablePart(), stackPresentationSite);
            systemMenuRestore = new SystemMenuRestore(stackPresentationSite);
            systemMenuSizeFastView = new SystemMenuSizeFastView(fastViewPane);            
        }
        
        public void fill(Menu menu, int index) {
            systemMenuRestore.fill(menu, index);
            systemMenuMoveView.fill(menu, index);
            systemMenuSizeFastView.fill(menu, index);
            systemMenuMinimize.fill(menu, index);
            systemMenuMaximize.fill(menu, index);
            new MenuItem(menu, SWT.SEPARATOR);
            systemMenuClose.fill(menu, index);
        }
        
        public void dispose() {
            systemMenuClose.dispose();
            systemMenuMaximize.dispose();
            systemMenuMinimize.dispose();
            systemMenuMoveView.dispose();
            systemMenuRestore.dispose();
            systemMenuSizeFastView.dispose();
        }
    }
    
    private IContributionItem systemMenuContribution;
    	
	public void moveSash() {
		sash.moveSash();
	}
	
	private Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.Resize && currentPane != null) {
				setSize(size);
			}
		}
	};
		
	private SelectionAdapter selectionListener = new SelectionAdapter () {
		public void widgetSelected(SelectionEvent e) {

			
			if (currentPane != null) {
				Rectangle bounds = clientComposite.getClientArea();
				Point location = new Point(e.x, e.y);
				int distanceFromEdge = Geometry.getDistanceFromEdge(bounds, location, side);
				if (distanceFromEdge < MIN_FASTVIEW_SIZE) {
					distanceFromEdge = MIN_FASTVIEW_SIZE;
				}
				
				if (side == SWT.TOP || side == SWT.LEFT) {
					distanceFromEdge += SASH_SIZE;
				}
				
				setSize(distanceFromEdge);
				
				if (e.detail != SWT.DRAG) {
					getPresentation().getControl().moveAbove(null);
					currentPane.moveAbove(null); 
					sash.moveAbove(null);
				}
			}
		}
	};

	private void setSize(int size) {		
		this.size = size; 
		
		// Do the rest of this method inside an asyncExec. This allows the method 
		// to return quickly (without resizing). This way, if we recieve a lot of 
		// resize requests in a row, we only need to process the last one.
		// This is needed for GTK, which resizes slowly (bug 54517)
		redrawCounter++;
		getPresentation().getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				--redrawCounter;
				StackPresentation presentation = getPresentation();
				if (presentation == null || presentation.getControl().isDisposed()) {
					return;
				}
				if (redrawCounter == 0) {
					getPresentation().setBounds(getBounds());

					updateFastViewSashBounds();
				}
			}
		});
	}
	
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
		
		return (float)size / (float)clientSize;
	}

	private Rectangle getClientArea() {
		return clientComposite.getClientArea();
	}
	
	private Rectangle getBounds() {
		Rectangle bounds = getClientArea();
		
		if (site.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
			return bounds;
		}
		
		boolean horizontal = Geometry.isHorizontal(side);
		
		int available = Geometry.getDimension(bounds, !horizontal);
		
		return Geometry.getExtrudedEdge(bounds, Math.min(FastViewPane.this.size, available), side);		
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
		
		clientComposite = newClientComposite;
	
		clientComposite.addListener(SWT.Resize, resizeListener);
		
		// Create the control first
		Control ctrl = pane.getControl();
		if (ctrl == null) {
			pane.createControl(clientComposite);
			ctrl = pane.getControl();			
		}

		// Temporarily use the same appearance as docked views .. eventually, fastviews will
		// be independently pluggable.
		AbstractPresentationFactory factory = ((WorkbenchWindow) pane.getWorkbenchWindow())
        	.getWindowConfigurer().getPresentationFactory();
		StackPresentation presentation = factory.createPresentation(newClientComposite,
				site, AbstractPresentationFactory.ROLE_DOCKED_VIEW,
				SWT.MIN | SWT.MAX, pane.getPage().getPerspective().getId(), pane.getID());

		//StackPresentation presentation = new PartTabFolderPresentation(newClientComposite, site, SWT.MIN | SWT.MAX);
		
		site.setPresentation(presentation);
		site.setPresentationState(IStackPresentationSite.STATE_RESTORED);
		presentation.addPart(pane.getPresentablePart(), null);
		presentation.selectPart(pane.getPresentablePart());
		presentation.setActive(true);
		presentation.setVisible(true);

		systemMenuContribution = new SystemMenuContribution(site, this);
		presentation.getSystemMenuManager().add(systemMenuContribution);

		// Show pane fast.
		ctrl.setEnabled(true); // Add focus support.
		Composite parent = ctrl.getParent();

		pane.setVisible(true);
		pane.setFocus();
		
		boolean horizontal = Geometry.isHorizontal(side);
		sash = new HiddenSash(parent, Geometry.getSwtHorizontalOrVerticalConstant(horizontal));

		sash.addSelectionListener(selectionListener);

		Rectangle clientArea = newClientComposite.getClientArea();
		
		getPresentation().getControl().moveAbove(null);
		currentPane.moveAbove(null); 
		sash.moveAbove(null);

		setSize((int)(Geometry.getDimension(clientArea, !horizontal) * sizeRatio));

	}
	
	/**
	 * Updates the position of the resize sash.
	 * 
	 * @param bounds
	 */
	private void updateFastViewSashBounds() {
		Rectangle bounds = getBounds();
		
		int oppositeSide = Geometry.getOppositeSide(side);
		Rectangle newBounds = Geometry.getExtrudedEdge(bounds, SASH_SIZE, oppositeSide);
		
		Rectangle oldBounds = sash.getBounds();
		
		if (!newBounds.equals(oldBounds)) {
			sash.setBounds(newBounds);
		}
	}
	
	/**
	 * Disposes of any active widgetry being used for the fast view pane. Does not dispose
	 * of the view itself.
	 */
	public void dispose() {
		if (sash != null) {
			sash.dispose();
		}
				
		StackPresentation presentation = getPresentation();
		
		if (presentation != null) {
			presentation.getSystemMenuManager().remove(systemMenuContribution);
			// TODO spec says not to call this directly
			systemMenuContribution.dispose();
			systemMenuContribution = null;
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
		if (sash != null) {
			sash.setVisible(false);
		}
	}
	
	/**
	 * Hides the currently visible fastview.
	 */
	public void hideView() {
		
		if (currentPane == null) {
			return;
		}

		sash.setVisible(false);
		
		clientComposite.removeListener(SWT.Resize, resizeListener);
		
		// Get pane.
		// Hide the right side sash first
		hideFastViewSash();
		Control ctrl = currentPane.getControl();
		
		// Hide it completely.
		getPresentation().setVisible(false);
		site.dispose();
		//currentPane.setFastViewSash(null);
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
