package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

/**
 * Common function for debug views. Provides access to underlying viewer and
 * debug model presentation being used by a viewer. This allows clients to do
 * such things as add and remove filters to a viewer, and configure a debug
 * model presentation.
 * <p>
 * Clients may implement this interface. Generally, clients should subclass
 * <code>AbstractDebugView</code> when creating a new debug view.
 * </p>
 * @see org.eclipse.core.runtime.IAdaptable
 * @see org.eclipse.debug.ui.IDebugModelPresentation
 * @see org.eclipse.debug.ui.AbstractDebugView
 * @since 2.0
 */

public interface IDebugView extends IViewPart {
	
	/**
	 * Returns the viewer contained in this debug view.
	 *
	 * @return viewer
	 */
	public Viewer getViewer();
	
	/**
	 * Returns the debug model presentation for this view specified
	 * by the debug model identifier.
	 *
	 * @param id the debug model identifier that corresponds to the <code>id</code>
	 *     attribute of a debug model presentation extension
	 * @return the debug model presentation, or <code>null</code> if no
	 *     presentation is registered for the specified id
	 */
	public IDebugModelPresentation getPresentation(String id);
	
	/**
	 * Installs the given action under the given action id.
	 *
	 * @param actionId the action id
	 * @param action the action, or <code>null</code> to clear it
	 * @see #getAction
	 */
	public void setAction(String actionID, IAction action);
	
	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction
	 */
	public IAction getAction(String actionID);
	
	/**
	 * Returns the context menu manager for this view.
	 *
	 * @return the context menu manager for this view, or <code>null</code> if none
	 */
	public IMenuManager getContextMenuManager();
}