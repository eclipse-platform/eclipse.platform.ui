package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import java.util.Collection;

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
abstract public class LayoutPart implements IWorkbenchDropTarget {
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
//	/**
//	 * Gets root container for this part.
//	 */
//	public RootLayoutContainer getRootContainer() {
//		if (container != null)
//			return container.getRootContainer();
//		return null;
//	}
	/**
	 * Gets the parent for this part.
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
			return ((DetachedWindow) parentWindow).getWorkbenchPage().getWorkbenchWindow();

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

			ctrl.setVisible(makeVisible);
			final Object[] listeners = propertyListeners.getListeners();
			if (listeners.length > 0) {
				Boolean oldValue = makeVisible ? Boolean.FALSE : Boolean.TRUE;
				Boolean newValue = makeVisible ? Boolean.TRUE : Boolean.FALSE;
				PropertyChangeEvent event =
					new PropertyChangeEvent(this, PROP_VISIBILITY, oldValue, newValue);
				for (int i = 0; i < listeners.length; ++i)
					 ((IPropertyChangeListener) listeners[i]).propertyChange(event);
			}
		}
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#getType()
	 */
	public int getType() {
		return UNDEFINED;
	}

	public IPresentablePart getPresentablePart() {
		// TODO: write me
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#targetPartFor(org.eclipse.ui.internal.IWorkbenchDragSource)
	 */
	public LayoutPart targetPartFor(IWorkbenchDragSource dragSource) {
		return null;
	}
	
	protected void addDropTargets(Collection result, ILayoutContainer container){
		LayoutPart[] parts = container.getChildren();
		for (int i = 0; i < parts.length; i++) {
			parts[i].addDropTargets(result);
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#addDropTargets(java.util.Collection)
	 */
	public void addDropTargets(Collection result) {
		result.add(this);
	}

	public boolean resizesVertically() {
		return true;
	}
	
/**
 * @return Returns the propertyListeners.
 */
protected ListenerList getPropertyListeners() {
	return propertyListeners;
}
}
