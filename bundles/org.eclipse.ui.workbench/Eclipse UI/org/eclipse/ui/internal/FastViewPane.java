/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *     Matthew Hatem Matthew_Hatem@notesdev.ibm.com Bug 189953
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuFastViewOrientation;
import org.eclipse.ui.internal.presentations.SystemMenuSizeFastView;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
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
 * @see org.eclipse.ui.internal.FastViewBar
 */
public class FastViewPane {
    private int side = SWT.LEFT;

    private PresentablePart currentPane;

    private Composite clientComposite;
    
    private int minSize = 10;

    private int size;

    private Sash sash;
    
    private AbstractPresentationFactory presFactory;

    // Traverse listener -- listens to ESC and closes the active fastview 
    private Listener escapeListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.character == SWT.ESC) {
                if (currentPane != null) {
                    currentPane.getPane().getPage().hideFastView();
                }
            }
        }
    };

    private DefaultStackPresentationSite site = new DefaultStackPresentationSite() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.skins.IPresentationSite#setState(int)
         */
        public void setState(int newState) {
            super.setState(newState);
            PartPane pane = currentPane.getPane();
            switch (newState) {
            case IStackPresentationSite.STATE_MINIMIZED:
                
                pane.getPage().hideFastView();
                break;
            case IStackPresentationSite.STATE_MAXIMIZED:
                pane.setZoomed(true);
                sash.setVisible(false);
                this.getPresentation().setBounds(getBounds());
                break;
            case IStackPresentationSite.STATE_RESTORED:
                pane.setZoomed(false);
                sash.setVisible(true);
                this.getPresentation().setBounds(getBounds());
                break;
            default:
            }
        }

        public void flushLayout() {
        	
        }
        
        public void close(IPresentablePart part) {
            if (!isCloseable(part)) {
                return;
            }
            IWorkbenchPartReference ref = currentPane.getPane().getPartReference();
            if (ref instanceof IViewReference) {
                currentPane.getPane().getPage().hideView((IViewReference)ref);
            }
        }

        public void close(IPresentablePart[] parts) {
            for (int idx = 0; idx < parts.length; idx++) {
                close(parts[idx]);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(org.eclipse.ui.internal.skins.IPresentablePart, boolean)
         */
        public void dragStart(IPresentablePart beingDragged,
                Point initialPosition, boolean keyboard) {
            dragStart(initialPosition, keyboard);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(boolean)
         */
        public void dragStart(Point initialPosition, boolean keyboard) {
            if (!isPartMoveable()) {
				return;
			}

            PartPane pane = currentPane.getPane();

            Control control = this.getPresentation().getControl();

            Rectangle bounds = Geometry.toDisplay(clientComposite, control
                    .getBounds());

            WorkbenchPage page = pane.getPage();

            page.hideFastView();
            if (page.isZoomed()) {
                page.zoomOut();
            }

            DragUtil.performDrag(pane, bounds, initialPosition, !keyboard);
        }

        public IPresentablePart getSelectedPart() {
            return currentPane;
        }

        public void addSystemActions(IMenuManager menuManager) {
        	ViewStackTrimToolBar vstt = getTrim();
        	
            appendToGroupIfPossible(menuManager,
                    "misc", new SystemMenuFastViewOrientation(currentPane.getPane(), vstt)); //$NON-NLS-1$

            // Only add the 'Fast View' menu entry if the
        	// pane is showing a 'legacy' fast view
        	if (vstt == null) {
	            appendToGroupIfPossible(menuManager,
	                    "misc", new UpdatingActionContributionItem(fastViewAction)); //$NON-NLS-1$
        	}
        	
            appendToGroupIfPossible(menuManager,
                    "size", new SystemMenuSizeFastView(FastViewPane.this)); //$NON-NLS-1$
        }

        /**
         * Returns the ViewStackTrimToolBar which has caused the FV to be shown. If
         * <code>null</code> then we can assume it was the legacy FastViewBar.
         */
        private ViewStackTrimToolBar getTrim() {
        	if (currentPane == null || currentPane.getPane() == null)
        		return null;

            ViewStackTrimToolBar trim = null;

        	PartPane pane = currentPane.getPane();
        	if (pane instanceof ViewPane) {
        		ViewPane vp = (ViewPane) pane;
                Perspective persp = vp.getPage().getActivePerspective();
                IViewReference viewRef = vp.getViewReference();
                FastViewManager fvm = persp.getFastViewManager();
                
                String trimId = null;
                if (fvm != null)
                	trimId = fvm.getIdForRef(viewRef);

                if (trimId != null && !trimId.equals(FastViewBar.FASTVIEWBAR_ID))
                	trim = fvm.getViewStackTrimToolbar(trimId);
        	}
        	
        	return trim;
        }
        
        public boolean isPartMoveable(IPresentablePart toMove) {
            return isPartMoveable();
        }

        public boolean isStackMoveable() {
            // a fast view stack is moveable iff its part is moveable
            return isPartMoveable();
        }

        private boolean isPartMoveable() {
            if (currentPane == null) {
				return false;
			}
            Perspective perspective = currentPane.getPane().getPage()
                    .getActivePerspective();
            if (perspective == null) {
                // Shouldn't happen -- can't have a FastViewPane without a perspective
                return false;
            }
            
            IWorkbenchPartReference ref = currentPane.getPane().getPartReference();
            
            if (ref instanceof IViewReference) {
                return perspective.isMoveable((IViewReference)ref);
            }
            return true;
        }

        public boolean supportsState(int newState) {
            if (currentPane == null) {
				return false;
			}
            if (currentPane.getPane().getPage().isFixedLayout()) {
				return false;
			}
            return true;
        }

        public IPresentablePart[] getPartList() {
            return new IPresentablePart[] {getSelectedPart()};
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.presentations.IStackPresentationSite#getProperty(java.lang.String)
         */
        public String getProperty(String id) {
            // fast views stacks do not get arbitrary user properties.
            return null;
        }
    };

    private SystemMenuFastView fastViewAction = new SystemMenuFastView(site);

    private static void appendToGroupIfPossible(IMenuManager m, String groupId,
            ContributionItem item) {
        try {
            m.appendToGroup(groupId, item);
        } catch (IllegalArgumentException e) {
            m.add(item);
        }
    }

    private Listener mouseDownListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.widget instanceof Control) {
                Control control = (Control) event.widget;

                if (control.getShell() != clientComposite.getShell()) {
                    return;
                }

                if (event.widget instanceof ToolBar) {
                    // Ignore mouse down on actual tool bar buttons
                    Point pt = new Point(event.x, event.y);
                    ToolBar toolBar = (ToolBar) event.widget;
                    if (toolBar.getItem(pt) != null) {
						return;
					}
                }

                Point loc = DragUtil.getEventLoc(event);

            	// 'Extrude' the rect -before- converting to Display coords
            	// to avoid Right-to-Left issues
                Rectangle bounds = clientComposite.getBounds();
                if (site.getState() != IStackPresentationSite.STATE_MAXIMIZED) {
                    bounds = Geometry.getExtrudedEdge(bounds, size + getSashSize(), side);
                }
                
                // Now map the bounds to display coords
                bounds = clientComposite.getDisplay().map(clientComposite, null, bounds);

                if (!bounds.contains(loc)) {
                    site.setState(IStackPresentationSite.STATE_MINIMIZED);
                }
            }
        }
    };

    public void moveSash() {
        final KeyListener listener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC || e.character == '\r') {
                    currentPane.setFocus();
                }
            }
        };
        sash.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                sash.setBackground(sash.getDisplay().getSystemColor(
                        SWT.COLOR_LIST_SELECTION));
                sash.addKeyListener(listener);
            }

            public void focusLost(FocusEvent e) {
                sash.setBackground(null);
                sash.removeKeyListener(listener);
            }
        });
        sash.setFocus();
    }

    private Listener resizeListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.type == SWT.Resize && currentPane != null) {
                setSize(size);
            }
        }
    };

    private SelectionAdapter selectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {

            if (currentPane != null) {
                Rectangle bounds = clientComposite.getClientArea();
                Point location = new Point(e.x, e.y);
                int distanceFromEdge = Geometry.getDistanceFromEdge(bounds,
                        location, side);

                if (!(side == SWT.TOP || side == SWT.LEFT)) {
                    distanceFromEdge -= getSashSize();
                }

                setSize(distanceFromEdge);

                if (e.detail != SWT.DRAG) {
                    updateFastViewSashBounds();
                    //					getPresentation().getControl().moveAbove(null);
                    //					currentPane.moveAbove(null); 
                    //					sash.moveAbove(null);
                    //currentPane.getControl().redraw();
                    sash.redraw();
                }
            }
        }
    };

    private void setSize(int size) {

        if (size < minSize) {
            size = minSize;
        }
        this.size = size;

        StackPresentation presentation = getPresentation();
        if (presentation == null || presentation.getControl().isDisposed()) {
            return;
        }
        getPresentation().setBounds(getBounds());
        updateFastViewSashBounds();
    }

    /**
     * Returns the current fastview size ratio. Returns 0.0 if there is no fastview visible.
     */
    public float getCurrentRatio() {
        if (currentPane == null) {
            return 0.0f;
        }

        boolean isVertical = !Geometry.isHorizontal(side);
        Rectangle clientArea = clientComposite.getClientArea();

        int clientSize = Geometry.getDimension(clientArea, isVertical);

        return (float) size / (float) clientSize;
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

        return Geometry.getExtrudedEdge(bounds, Math.min(
                FastViewPane.this.size, available), side);
    }

    /**
     * Displays the given view as a fastview. The view will be docked to the edge of the
     * given composite until it is subsequently hidden by a call to hideFastView. 
     * 
     * @param newClientComposite
     * @param pane
     * @param newSide
     */
    public void showView(Composite newClientComposite, ViewPane pane,
            int newSide, float sizeRatio) {
        side = newSide;

        if (currentPane != null) {
            hideView();
        }

        currentPane = new PresentablePart(pane, newClientComposite);
        
        fastViewAction.setPane(currentPane);
        clientComposite = newClientComposite;

        clientComposite.addListener(SWT.Resize, resizeListener);

        // Create the control first
        Control ctrl = pane.getControl();
        if (ctrl == null) {
            pane.createControl(clientComposite);
            ctrl = pane.getControl();
        }

        ctrl.addListener(SWT.Traverse, escapeListener);

        // Temporarily use the same appearance as docked views .. eventually, fastviews will
        // be independently pluggable.
        AbstractPresentationFactory factory = getPresentationFactory();
        StackPresentation presentation = factory.createViewPresentation(
                newClientComposite, site);

        site.setPresentation(presentation);
        site.setPresentationState(IStackPresentationSite.STATE_RESTORED);
        presentation.addPart(currentPane, null);
        presentation.selectPart(currentPane);
        presentation.setActive(StackPresentation.AS_ACTIVE_FOCUS);
        presentation.setVisible(true);

        boolean horizontalResize = Geometry.isHorizontal(side); 

        minSize = presentation.computePreferredSize(horizontalResize,
        		ISizeProvider.INFINITE,
        		Geometry.getDimension(getClientArea(), horizontalResize),
				0);
        
        // Show pane fast.
        ctrl.setEnabled(true); // Add focus support.
        Composite parent = ctrl.getParent();

        boolean horizontal = Geometry.isHorizontal(side);

        // Create a sash of the correct style using the factory
        int style = AbstractPresentationFactory.SASHTYPE_FLOATING;
    	if (horizontal)
    		style |= AbstractPresentationFactory.SASHORIENTATION_HORIZONTAL;
    	else
    		style |= AbstractPresentationFactory.SASHORIENTATION_VERTICAL;
        sash = factory.createSash(parent, style);
        
        sash.addSelectionListener(selectionListener);

        Rectangle clientArea = newClientComposite.getClientArea();

        getPresentation().getControl().moveAbove(null);
        currentPane.getPane().moveAbove(null);
        sash.moveAbove(null);

        setSize((int) (Geometry.getDimension(clientArea, !horizontal) * sizeRatio));

        Display display = sash.getDisplay();

        display.addFilter(SWT.MouseDown, mouseDownListener);

        pane.setFocus();
    }

    /**
     * Updates the position of the resize sash.
     */
    private void updateFastViewSashBounds() {
        Rectangle bounds = getBounds();

        int oppositeSide = Geometry.getOppositeSide(side);
        Rectangle newBounds = Geometry.getExtrudedEdge(bounds, -getSashSize(),
                oppositeSide);

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
        hideView();
    }
    
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

        if (clientComposite != null) {
            Display display = clientComposite.getDisplay();

            display.removeFilter(SWT.MouseDown, mouseDownListener);
        }

        if (currentPane == null) {
            return;
        }

        fastViewAction.setPane(null);
        
        //unzoom before hiding
        currentPane.getPane().setZoomed(false);

        if (sash != null) {
            sash.dispose();
            sash = null;
        }

        clientComposite.removeListener(SWT.Resize, resizeListener);

        // Get pane.
        // Hide the right side sash first
        //hideFastViewSash();
        Control ctrl = currentPane.getControl();

        ctrl.removeListener(SWT.Traverse, escapeListener);

        // Hide it completely.
        getPresentation().setVisible(false);
        site.dispose();
        //currentPane.setFastViewSash(null);
        ctrl.setEnabled(false); // Remove focus support.

        currentPane.dispose();
        currentPane = null;
    }

    /**
     * @return Returns the currently visible fastview or null if none
     */
    public ViewPane getCurrentPane() { 
        if (currentPane != null && currentPane.getPane() instanceof ViewPane) {
            return (ViewPane)currentPane.getPane();
        }
        
        return null;
    }

    public void setState(int newState) {
    	site.setState(newState);
    }
    
    public int getState() {
    	return site.getState();
    }
    
    /**
     * 
     */
    public void showSystemMenu() {
        getPresentation().showSystemMenu();
    }
    
    /**
     * 
     */
    public void showPaneMenu() {
        getPresentation().showPaneMenu();
    }
    
    private int getSashSize() {
    	AbstractPresentationFactory factory = getPresentationFactory();
    	
    	// Set up the correct 'style' bits
    	int style = AbstractPresentationFactory.SASHTYPE_FLOATING;
    	if (Geometry.isHorizontal(side))
    		style |= AbstractPresentationFactory.SASHORIENTATION_HORIZONTAL;
    	else
    		style |= AbstractPresentationFactory.SASHORIENTATION_VERTICAL;
    		
    	int size = factory.getSashSize(style);
    	
    	return size;
    }
    
    private AbstractPresentationFactory getPresentationFactory() {
    	if (presFactory == null) {
	    	presFactory = ((WorkbenchWindow) currentPane.getPane().getWorkbenchWindow())
					.getWindowConfigurer().getPresentationFactory();
    	}
        return presFactory;
    }
}
