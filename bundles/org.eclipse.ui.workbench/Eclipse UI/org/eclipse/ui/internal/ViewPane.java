/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;


import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Provides a wrapper for the view's widgetry.
 * 
 * TODO: Delete ViewPane and EditorPane, and make PartPane non-abstract.
 */
public class ViewPane extends PartPane {
    private boolean busy = false;

    // create initially toolbarless bar manager so that actions may be added in the 
    // init method of the view.
    private ToolBarManager isvToolBarMgr = new PaneToolBarManager(SWT.FLAT
            | SWT.WRAP);

    private MenuManager isvMenuMgr;

    boolean hasFocus;

    /**
     * Indicates whether a toolbar button is shown for the view local menu.
     */
    private boolean hadViewMenu = false;

    /**
     * Toolbar manager for the ISV toolbar.
     */
    class PaneToolBarManager extends ToolBarManager {
        public PaneToolBarManager(int style) {
            super(style);
        }

        protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
            toolBarResized(toolBar, newCount);

            toolBar.layout();
        }
    }

    /**
     * Menu manager for view local menu.
     */
    class PaneMenuManager extends MenuManager {
        public PaneMenuManager() {
            super("View Local Menu"); //$NON-NLS-1$
        }

        protected void update(boolean force, boolean recursive) {
            super.update(force, recursive);

            boolean hasMenu = !isEmpty();
            if (hasMenu != hadViewMenu) {
                hadViewMenu = hasMenu;
                firePropertyChange(IPresentablePart.PROP_PANE_MENU);
            }
        }
    }

    /**
     * Constructs a view pane for a view part.
     */
    public ViewPane(IViewReference ref, WorkbenchPage page) {
        super(ref, page);
    }

    /**
     * Create control. Add the title bar.
     */
    public void createControl(Composite parent) {
        // Only do this once.
        if (getControl() != null && !getControl().isDisposed())
            return;

        super.createControl(parent);
    }


    private void recreateToolbars() {
        // create new toolbars based on the locked vs !locked state
        createToolBars();
        // create new toolbars
        updateActionBars();

    }

    /**
     * Create a title bar for the pane.
     * 	- the view icon and title to the far left
     *	- the view toolbar appears in the middle.
     * 	- the view pulldown menu, pin button, and close button to the far right.
     */
    protected void createTitleBar() {
        // Only do this once.

        updateTitles();

        // Listen to title changes.
        getPartReference().addPropertyListener(this);

        createToolBars();

    }

    private void toolBarResized(ToolBar toolBar, int newSize) {
        
        Control toolbar = isvToolBarMgr.getControl();
        if (toolbar != null) {
            Control ctrl = getControl();

            boolean visible = ctrl != null && ctrl.isVisible()
                    && toolbarIsVisible();

            toolbar.setVisible(visible);
        }

        firePropertyChange(IPresentablePart.PROP_TOOLBAR);
    }

    /**
     * 
     */
    private void createToolBars() {
        Composite parentControl = control;
        
        // ISV toolbar.
        //			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
        final ToolBar isvToolBar = isvToolBarMgr.createControl(parentControl.getParent());
        
        isvToolBar.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                if (event.widget instanceof ToolBar) {
                
                    if (((ToolBar)event.widget).getItem(new Point(event.x, event.y)) == null)
                        doZoom();
                }
            }
        });

        
        isvToolBar.addListener(SWT.Activate, this);
        isvToolBar.moveAbove(control);
    }

    public void dispose() {
        super.dispose();

        /* Bug 42684.  The ViewPane instance has been disposed, but an attempt is
         * then made to remove focus from it.  This happens because the ViewPane is
         * still viewed as the active part.  In general, when disposed, the control
         * containing the titleLabel will also disappear (disposing of the 
         * titleLabel).  As a result, the reference to titleLabel should be dropped. 
         */
        if (isvMenuMgr != null) {
            isvMenuMgr.dispose();
        }
        if (isvToolBarMgr != null) {
            isvToolBarMgr.dispose();
        }
    }

    /**
     * @see PartPane#doHide
     */
    public void doHide() {
        getPage().hideView(getViewReference());
    }

    /*package*/Rectangle getParentBounds() {
        Control ctrl = getControl();

        if (getContainer() != null && getContainer() instanceof LayoutPart) {
            LayoutPart part = (LayoutPart) getContainer();

            if (part.getControl() != null) {
                ctrl = part.getControl();
            }
        }

        return DragUtil.getDisplayBounds(ctrl);
    }

    /**
     * Make this view pane a fast view
     */
    public void doMakeFast() {
        WorkbenchWindow window = (WorkbenchWindow) getPage()
                .getWorkbenchWindow();

        FastViewBar fastViewBar = window.getFastViewBar();
        if (fastViewBar == null) {
            return;
        }
        Shell shell = window.getShell();

        RectangleAnimation animation = new RectangleAnimation(shell,
                getParentBounds(), fastViewBar.getLocationOfNextIcon());

        animation.schedule();

        getPage().addFastView(getViewReference());
    }

    public void doRemoveFast() {

        Shell shell = getControl().getShell();

        Rectangle initialBounds = getParentBounds();

        getPage().removeFastView(getViewReference());

        IWorkbenchPart toActivate = getViewReference().getPart(true);
        if (toActivate != null) {
            getPage().activate(toActivate);
        }

        Rectangle finalBounds = getParentBounds();

        RectangleAnimation animation = new RectangleAnimation(shell,
                initialBounds, finalBounds);

        animation.schedule();
    }

    /**
     * Pin the view.
     */
    protected void doDock() {
        getPage().removeFastView(getViewReference());
    }
    
    public void doDetach() {
    	getPage().detachView(getViewReference());
    }
    		
    public void doAttach() {
    	getPage().attachView(getViewReference());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getCompoundId()
     */
    public String getCompoundId() {
        IViewReference ref = getViewReference();
        if (ref != null)
            return ViewFactory.getKey(ref);
        else
            return super.getCompoundId();
    }

    /**
     * Returns the drag control.
     */
    public Control getDragHandle() {
        return control;
    }

    /**
     * @see ViewActionBars
     */
    public MenuManager getMenuManager() {
        if (isvMenuMgr == null)
            isvMenuMgr = new PaneMenuManager();
        return isvMenuMgr;
    }

    /**
     * Returns the tab list to use when this part is active.
     * Includes the view and its tab (if applicable), in the appropriate order.
     */
    public Control[] getTabList() {
        Control c = getControl();
        if (getContainer() instanceof ViewStack) {
            ViewStack tf = (ViewStack) getContainer();
            return tf.getTabList(this);
        }
        return new Control[] { c };
    }

    /**
     * @see ViewActionBars
     */
    public ToolBarManager getToolBarManager() {
        return isvToolBarMgr;
    }

    /**
     * Answer the view part child.
     */
    public IViewReference getViewReference() {
        return (IViewReference) getPartReference();
    }

    /**
     * Indicates that a property has changed.
     *
     * @param source the object whose property has changed
     * @param propId the id of the property which has changed; property ids
     *   are generally defined as constants on the source class
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IWorkbenchPart.PROP_TITLE)
            updateTitles();
    }

    /**
     * Sets the fast view state.  If this view is a fast view then
     * various controls like pin and minimize are added to the
     * system bar.
     */
    public void setFast(boolean b) {
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */
    void shellActivated() {
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */
    void shellDeactivated() {
    }

    /**
     * Set the active border.
     * @param active
     */
    void setActive(boolean active) {
        hasFocus = active;

        if (getContainer() instanceof PartStack) {
            ((PartStack) getContainer())
                    .setActive(active ? StackPresentation.AS_ACTIVE_FOCUS
                            : StackPresentation.AS_INACTIVE);
        }
    }

    /**
     * Indicate focus in part.
     */
    public void showFocus(boolean inFocus) {
        setActive(inFocus);
    }

    /**
     * Return true if this view is a fast view.
     */
    private boolean isFastView() {
        return page.isFastView(getViewReference());
    }

    /**
     * Return true if the view may be moved.
     */
    boolean isMoveable() {
        return !page.isFixedLayout();
    }

    /**
     * Return if there should be a view menu at all.
     * There is no view menu if there is no menu manager,
     * no pull down button or if the receiver is an
     * inactive fast view.
     */
    public boolean hasViewMenu() {

        if (isvMenuMgr != null) {
            return !isvMenuMgr.isEmpty();
        }

        return false;
    }

    public void showViewMenu(Point location) {
        if (!hasViewMenu())
            return;

        // If this is a fast view, it may have been minimized. Do nothing in this case.
        if (isFastView() && (page.getActiveFastView() != getViewReference()))
            return;

        Menu aMenu = isvMenuMgr.createContextMenu(getControl().getParent());
        aMenu.setLocation(location.x, location.y);
        aMenu.setVisible(true);
    }

    public String toString() {

        return getClass().getName() + "@" + Integer.toHexString(hashCode()); //$NON-NLS-1$
    }

    /**
     * @see ViewActionBars
     */
    public void updateActionBars() {
        if (isvMenuMgr != null)
            isvMenuMgr.updateAll(false);
        if (isvToolBarMgr != null)
            isvToolBarMgr.update(false);

    }

    /**
     * Update the title attributes.
     */
    public void updateTitles() {
        firePropertyChange(IPresentablePart.PROP_TITLE);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#addSizeMenuItem(org.eclipse.swt.widgets.Menu)
     */
    public void addSizeMenuItem(Menu menu, int index) {
        if (isMoveable())
            super.addSizeMenuItem(menu, index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#doZoom()
     */
    protected void doZoom() {
        if (isMoveable())
            super.doZoom();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#setContainer(org.eclipse.ui.internal.ILayoutContainer)
     */
    public void setContainer(ILayoutContainer container) {
        ILayoutContainer oldContainer = getContainer();
        if (hasFocus) {
            if (oldContainer != null && oldContainer instanceof PartStack) {
                ((PartStack) oldContainer)
                        .setActive(StackPresentation.AS_INACTIVE);
            }

            if (container != null && container instanceof PartStack) {
                ((PartStack) container)
                        .setActive(StackPresentation.AS_ACTIVE_FOCUS);
            }
        }

        super.setContainer(container);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#reparent(org.eclipse.swt.widgets.Composite)
     */
    public void reparent(Composite newParent) {
        super.reparent(newParent);

        if (isvToolBarMgr != null) {
            ToolBar bar = isvToolBarMgr.getControl();
            if (bar != null) {
                bar.setParent(newParent);
                bar.moveAbove(control);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#moveAbove(org.eclipse.swt.widgets.Control)
     */
    public void moveAbove(Control refControl) {
        super.moveAbove(refControl);

        Control toolbar = internalGetToolbar();
        
        if (toolbar != null) {
            toolbar.moveAbove(control);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#setVisible(boolean)
     */
    public void setVisible(boolean makeVisible) {
        super.setVisible(makeVisible);

        Control toolbar = internalGetToolbar();
        
        if (toolbar != null) {
            boolean visible = makeVisible && toolbarIsVisible();
            toolbar.setVisible(visible);
        }
    }

    public boolean toolbarIsVisible() {
        ToolBarManager toolbarManager = getToolBarManager();

        if (toolbarManager == null) {
            return false;
        }

        ToolBar control = toolbarManager.getControl();

        if (control == null || control.isDisposed()) {
            return false;
        }

        return control.getItemCount() > 0;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#setBusy(boolean)
     */
    public void setBusy(boolean isBusy) {
        if (isBusy != busy) {
            busy = isBusy;
            firePropertyChange(IPresentablePart.PROP_BUSY);
        }
    }

    /**
     * Return the busy state of the receiver.
     * @return boolean
     */
    public boolean isBusy() {
        return busy;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#showHighlight()
     */
    public void showHighlight() {
        firePropertyChange(IPresentablePart.PROP_HIGHLIGHT_IF_BACK);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getPlaceHolderId()
     */
    public String getPlaceHolderId() {
        return ViewFactory.getKey(getViewReference());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#getToolBar()
     */
    public Control getToolBar() {

        if (!toolbarIsVisible()) {
            return null;
        }
        
        return internalGetToolbar();
    }
    
    private ToolBar internalGetToolbar() {
        if (isvToolBarMgr == null) {
            return null;
        }
        
        return isvToolBarMgr.getControl();        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#isCloseable()
     */
    public boolean isCloseable() {
        Perspective perspective = page.getActivePerspective();
        if (perspective == null) {
            // Shouldn't happen -- can't have a ViewStack without a
            // perspective
            return true;
        }
        return perspective.isCloseable(getViewReference());
    }
    
    public void showPaneMenu() {
		if (isFastView()) {
	        Perspective perspective = page.getActivePerspective();
	        
	        perspective.getFastViewPane().showSystemMenu();
		} else {
			super.showPaneMenu();	
		}
    } 
    
    public void removeContributions() {
    	super.removeContributions();
    	
        if (isvMenuMgr != null) {
            isvMenuMgr.removeAll();
        }
        if (isvToolBarMgr != null) {
            isvToolBarMgr.removeAll();
        }
    }
}
