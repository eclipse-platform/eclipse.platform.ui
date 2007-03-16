/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.SwtUtil;

/**
 * A presentation part is used to build the presentation for the
 * workbench.  Common subclasses are pane and folder.
 */
abstract public class LayoutPart implements ISizeProvider {
    protected ILayoutContainer container;

    protected String id;

    public static final String PROP_VISIBILITY = "PROP_VISIBILITY"; //$NON-NLS-1$
    
    /**
     * Number of times deferUpdates(true) has been called without a corresponding
     * deferUpdates(false)
     */
	private int deferCount = 0;

    /**
     * PresentationPart constructor comment.
     */
    public LayoutPart(String id) {
        super();
        this.id = id;
    }
    
    /**
     * When a layout part closes, focus will return to a previously active part.
     * This method determines whether this part should be considered for activation
     * when another part closes. If a group of parts are all closing at the same time,
     * they will all return false from this method while closing to ensure that the
     * parent does not activate a part that is in the process of closing. Parts will
     * also return false from this method if they are minimized, closed fast views,
     * obscured by zoom, etc.
     * 
     * @return true iff the parts in this container may be given focus when the active
     * part is closed
     */
    public boolean allowsAutoFocus() {
        if (container != null) {
            return container.allowsAutoFocus();
        }
        return true;
    }


    /**
     * Creates the SWT control
     */
    abstract public void createControl(Composite parent);

    /** 
     * Disposes the SWT control
     */
    public void dispose() {
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }

    /**
     * Gets the parent for this part.
     * <p>
     * In general, this is non-null if the object has been added to a container and the
     * container's widgetry exists. The exception to this rule is PartPlaceholders
     * created when restoring a ViewStack using restoreState, which point to the 
     * ViewStack even if its widgetry doesn't exist yet. Returns null in the remaining
     * cases.
     * </p> 
     * <p>
     * TODO: change the semantics of this method to always point to the parent container,
     * regardless of whether its widgetry exists. Locate and refactor code that is currently 
     * depending on the special cases.
     * </p>
     */
    public ILayoutContainer getContainer() {
        return container;
    }

    /**
     * Get the part control.  This method may return null.
     */
    abstract public Control getControl();

    /**
     * Gets the ID for this part.
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the compound ID for this part.
     * The compound ID is of the form: primaryId [':' + secondaryId]
     * 
     * @return the compound ID for this part.
     */
    public String getCompoundId() {
        return getID();
    }

    public boolean isCompressible() {
        return false;
    }

    /**
     * Gets the presentation size.
     */
    public Point getSize() {
        Rectangle r = getBounds();
        Point ptSize = new Point(r.width, r.height);
        return ptSize;
    }

    /**
     * @see org.eclipse.ui.presentations.StackPresentation#getSizeFlags(boolean)
     * 
     * @since 3.1
     */
    public int getSizeFlags(boolean horizontal) {
        return SWT.MIN;
    }
    
    /**
     * @see org.eclipse.ui.presentations.StackPresentation#computePreferredSize(boolean, int, int, int)
     * 
     * @since 3.1 
     */
    public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular, int preferredParallel) {
    	
    	return preferredParallel;    	
    }
    
    public IDropTarget getDropTarget(Object draggedObject, Point displayCoordinates) {
        return null;
    }
    
    public boolean isDocked() {
        Shell s = getShell();
        if (s == null) {
            return false;
        }
        
        return s.getData() instanceof IWorkbenchWindow;
    }
    
    public Shell getShell() {
        Control ctrl = getControl();
        if (!SwtUtil.isDisposed(ctrl)) {
            return ctrl.getShell();
        }
        return null;        
    }

    /**
	 * Returns the workbench window window for a part.
	 * 
	 * @return the workbench window, or <code>null</code> if there's no window
	 *         associated with this part.
	 */
    public IWorkbenchWindow getWorkbenchWindow() {
        Shell s = getShell();
        if (s==null) {
        	return null;
        }
        Object data = s.getData();
        if (data instanceof IWorkbenchWindow) {
            return (IWorkbenchWindow)data;
        } else if (data instanceof DetachedWindow) {
            return ((DetachedWindow) data).getWorkbenchPage()
                .getWorkbenchWindow();
        }
        
        return null;
        
    }

    /**
     * Move the control over another one.
     */
    public void moveAbove(Control refControl) {
    }

    /**
     * Reparent a part.
     */
    public void reparent(Composite newParent) {
        Control control = getControl();
        if ((control == null) || (control.getParent() == newParent)) {
            return;
        }

        if (control.isReparentable()) {
            // make control small in case it is not resized with other controls
            //control.setBounds(0, 0, 0, 0);
            // By setting the control to disabled before moving it,
            // we ensure that the focus goes away from the control and its children
            // and moves somewhere else
            boolean enabled = control.getEnabled();
            control.setEnabled(false);
            control.setParent(newParent);
            control.setEnabled(enabled);
            control.moveAbove(null);
        }
    }

    /**
     * Returns true if this part was set visible. This returns whatever was last passed into
     * setVisible, but does not necessarily indicate that the part can be seen (ie: one of its
     * ancestors may be invisible) 
     */
    public boolean getVisible() {
        Control ctrl = getControl();
        if (!SwtUtil.isDisposed(ctrl)) {
			return ctrl.getVisible();
		}
        return false;    
    }
    
    /**
     * Returns true if this part can be seen. Returns false if the part or any of its ancestors
     * are invisible.
     */
    public boolean isVisible() {
        Control ctrl = getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
			return ctrl.isVisible();
		}
        return false;
    }

    /**
     * Shows the receiver if <code>visible</code> is true otherwise hide it.
     */
    public void setVisible(boolean makeVisible) {
        Control ctrl = getControl();
        if (!SwtUtil.isDisposed(ctrl)) {
            if (makeVisible == ctrl.getVisible()) {
				return;
			}

            if (!makeVisible && isFocusAncestor(ctrl)) {
                // Workaround for Bug 60970 [EditorMgmt] setActive() called on an editor when it does not have focus.
                // Force focus on the shell so that when ctrl is hidden,
                // SWT does not try to send focus elsewhere, which may cause
                // some other part to be activated, which affects the part
                // activation order and can cause flicker.
                ctrl.getShell().forceFocus();
            }

            ctrl.setVisible(makeVisible);
        }
    }

    /**
     * Returns <code>true</code> if the given control or any of its descendents has focus.
     */
    private boolean isFocusAncestor(Control ctrl) {
        Control f = ctrl.getDisplay().getFocusControl();
        while (f != null && f != ctrl) {
            f = f.getParent();
        }
        return f == ctrl;
    }

    /**
     * Sets the presentation bounds.
     */
    public void setBounds(Rectangle r) {
        Control ctrl = getControl();
        if (!SwtUtil.isDisposed(ctrl)) {
			ctrl.setBounds(r);
		}
    }

    /**
     * Sets the parent for this part.
     */
    public void setContainer(ILayoutContainer container) {
        
        this.container = container;
        
        if (container != null) {
            setZoomed(container.childIsZoomed(this));
        }
    }

    /**
     * Sets focus to this part.
     */
    public void setFocus() {
    }

    /** 
     * Sets the part ID.
     */
    public void setID(String str) {
        id = str;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWorkbenchDragDropPart#getPart()
     */
    public LayoutPart getPart() {
        return this;
    }

    public void childRequestZoomIn(LayoutPart toZoom) {
        
    }
    
    public void childRequestZoomOut() {
        
    }
    
    public final void requestZoomOut() {
        ILayoutContainer container = getContainer();
        if (container != null) {
            container.childRequestZoomOut();
        }
    }
    
    public final void requestZoomIn() {
        ILayoutContainer container = getContainer();
        if (container != null) {
            container.childRequestZoomIn(this);
        }
    }
    
    public final boolean isObscuredByZoom() {
        ILayoutContainer container = getContainer();
        
        if (container != null) {
            return container.childObscuredByZoom(this);
        }
        
        return false;
    }
    
    public boolean childObscuredByZoom(LayoutPart toTest) {
        return false;
    }
    
    public boolean childIsZoomed(LayoutPart childToTest) {
        return false;
    }
    
    public void setZoomed(boolean isZoomed) {

    }
    
    /**
     * deferUpdates(true) disables widget updates until a corresponding call to
     * deferUpdates(false). Exactly what gets deferred is the decision
     * of each LayoutPart, however the part may only defer operations in a manner
     * that does not affect the final result. 
     * That is, the state of the receiver after the final call to deferUpdates(false)
     * must be exactly the same as it would have been if nothing had been deferred. 
     * 
     * @param shouldDefer true iff events should be deferred 
     */
    public final void deferUpdates(boolean shouldDefer) {
    	if (shouldDefer) {
    		if (deferCount == 0) {
    			startDeferringEvents();
    		}
    		deferCount++;
    	} else {
    		if (deferCount > 0) {
    			deferCount--;
    			if (deferCount == 0) {
    				handleDeferredEvents();
    			}
    		}
    	}
    }
    
    /**
     * This is called when deferUpdates(true) causes UI events for this
     * part to be deferred. Subclasses can overload to initialize any data
     * structures that they will use to collect deferred events.
     */
    protected void startDeferringEvents() {
    	
    }
    
    /**
     * Immediately processes all UI events which were deferred due to a call to
     * deferUpdates(true). This is called when the last call is made to 
     * deferUpdates(false). Subclasses should overload this method if they
     * defer some or all UI processing during deferUpdates.
     */
    protected void handleDeferredEvents() {
    	
    }
    
    /**
     * Subclasses can call this method to determine whether UI updates should
     * be deferred. Returns true iff there have been any calls to deferUpdates(true)
     * without a corresponding call to deferUpdates(false). Any operation which is
     * deferred based on the result of this method should be performed later within
     * handleDeferredEvents(). 
     * 
     * @return true iff updates should be deferred.
     */
    protected final boolean isDeferred() {
    	return deferCount > 0;
    }

    /**
     * Writes a description of the layout to the given string buffer.
     * This is used for drag-drop test suites to determine if two layouts are the
     * same. Like a hash code, the description should compare as equal iff the
     * layouts are the same. However, it should be user-readable in order to
     * help debug failed tests. Although these are english readable strings,
     * they do not need to be translated.
     * 
     * @param buf
     */
    public void describeLayout(StringBuffer buf) {

    }

    /**
     * Returns an id representing this part, suitable for use in a placeholder.
     * 
     * @since 3.0
     */
    public String getPlaceHolderId() {
        return getID();
    }

    public void resizeChild(LayoutPart childThatChanged) {

    }

    public void flushLayout() {
        ILayoutContainer container = getContainer();
        if (getContainer() != null) {
            container.resizeChild(this);
        }
    }

    /**
     * Returns true iff the given part can be added to this ILayoutContainer
     * @param toAdd
     * @return
     * @since 3.1
     */
    public boolean allowsAdd(LayoutPart toAdd) {
        return false;
    }
    
    /**
     * Tests the integrity of this object. Throws an exception if the object's state
     * is not internally consistent. For use in test suites.
     */
    public void testInvariants() {
    }
}
