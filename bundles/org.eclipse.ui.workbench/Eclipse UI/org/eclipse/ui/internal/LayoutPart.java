/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * A presentation part is used to build the presentation for the
 * workbench.  Common subclasses are pane and folder.
 */
abstract public class LayoutPart {
    protected ILayoutContainer container;

    protected String id;

    private ListenerList propertyListeners = new ListenerList(1);

    public static final String PROP_VISIBILITY = "PROP_VISIBILITY"; //$NON-NLS-1$

    /**
     * PresentationPart constructor comment.
     */
    public LayoutPart(String id) {
        super();
        this.id = id;
    }

    /**
     * Adds a property change listener to this action.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a property change listener
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        propertyListeners.add(listener);
    }

    /**
     * Removes the given listener from this action.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a property change listener
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyListeners.remove(listener);
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
     * Return the preference store for layout parts.
     */
    private IPreferenceStore getPreferenceStore() {
        return WorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Return whether the window's shell is activated
     */
    /* package */
    boolean getShellActivated() {
        Window window = getWindow();
        if (window instanceof WorkbenchWindow)
            return ((WorkbenchWindow) window).getShellActivated();
        else
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

    // getMinimumWidth() added by cagatayk@acm.org 
    /**
     * Returns the minimum width a part can have. Subclasses may
     * override as necessary.
     */
    public int getMinimumWidth() {
        return 0;
    }

    // getMinimumHeight() added by cagatayk@acm.org 
    /**
     * Returns the minimum height a part can have. Subclasses may 
     * override as necessary.
     */
    public int getMinimumHeight() {
        return 0;
    }

    /**
     * Returns the top level window for a part.
     */
    public Window getWindow() {
        Control ctrl = getControl();
        if (ctrl != null) {
            Object data = ctrl.getShell().getData();
            if (data instanceof Window)
                return (Window) data;
        }
        return null;
    }

    /**
     * Returns the workbench window window for a part.
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        Window parentWindow = getWindow();
        if (parentWindow instanceof IWorkbenchWindow)
            return (IWorkbenchWindow) parentWindow;
        if (parentWindow instanceof DetachedWindow)
            return ((DetachedWindow) parentWindow).getWorkbenchPage()
                    .getWorkbenchWindow();

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

        if (!control.isReparentable()) {
            // WARNING!!! The commented code here doesn't work... but something
            // similar will be needed to get undockable views working on all
            // platforms.
            //dispose();
            //createControl(newParent);
        } else {
            // make control small in case it is not resized with other controls
            control.setBounds(0, 0, 0, 0);
            // By setting the control to disabled before moving it,
            // we ensure that the focus goes away from the control and its children
            // and moves somewhere else
            boolean enabled = control.getEnabled();
            control.setEnabled(false);
            control.setParent(newParent);
            control.setEnabled(enabled);
        }
    }

    /**
     * Returns true if this part is visible.
     */
    public boolean isVisible() {
        Control ctrl = getControl();
        if (ctrl != null && !ctrl.isDisposed())
            return ctrl.isVisible();
        return false;
    }

    /**
     * Shows the receiver if <code>visible</code> is true otherwise hide it.
     */
    public void setVisible(boolean makeVisible) {
        Control ctrl = getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
            if (makeVisible == ctrl.getVisible())
                return;

            if (!makeVisible && isFocusAncestor(ctrl)) {
                // Workaround for Bug 60970 [EditorMgmt] setActive() called on an editor when it does not have focus.
                // Force focus on the shell so that when ctrl is hidden,
                // SWT does not try to send focus elsewhere, which may cause
                // some other part to be activated, which affects the part
                // activation order and can cause flicker.
                ctrl.getShell().forceFocus();
            }

            ctrl.setVisible(makeVisible);
            final Object[] listeners = propertyListeners.getListeners();
            if (listeners.length > 0) {
                Boolean oldValue = makeVisible ? Boolean.FALSE : Boolean.TRUE;
                Boolean newValue = makeVisible ? Boolean.TRUE : Boolean.FALSE;
                PropertyChangeEvent event = new PropertyChangeEvent(this,
                        PROP_VISIBILITY, oldValue, newValue);
                for (int i = 0; i < listeners.length; ++i)
                    ((IPropertyChangeListener) listeners[i])
                            .propertyChange(event);
            }
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
        if (ctrl != null)
            ctrl.setBounds(r);
    }

    /**
     * Sets the parent for this part.
     */
    public void setContainer(ILayoutContainer container) {
        //		if (this.container != null) {
        //			this.container.wasRemoved(this);
        //		}

        this.container = container;
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

    public IPresentablePart getPresentablePart() {
        return null;
    }

    public boolean resizesVertically() {
        return true;
    }

    public void setZoomed(boolean isZoomed) {
        ILayoutContainer container = getContainer();

        if (container != null) {
            container.setZoomed(isZoomed);
        }
    }

    /**
     * @return Returns the propertyListeners.
     */
    protected ListenerList getPropertyListeners() {
        return propertyListeners;
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

        IPresentablePart part = getPresentablePart();

        if (part != null) {
            buf.append(part.getName());
            return;
        }
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

    public void forceLayout() {
        ILayoutContainer container = getContainer();
        if (getContainer() != null) {
            container.resizeChild(this);
        }
    }

    /**
     * Tests the integrity of this object. Throws an exception if the object's state
     * is not internally consistent. For use in test suites.
     */
    public void testInvariants() {
    }
}