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
 * An adapter that debug views implement.
 * Provides access to underlying viewer and debug model presentation being
 * used by a viewer. This allows clients to do such things as add and
 * remove filters to a viewer, and configure a debug model presentation.
 * The following debug views support this adapter:
 * <ul>
 * <li>Debug view</li>
 * <li>Breakpoint view</li>
 * <li>Variable view</li>
 * <li>Expression view</li>
 * <li>Console view</li>
 * </ul>
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.core.runtime.IAdaptable
 * @see IDebugModelPresentation
 */

public interface IDebugView extends IViewPart {
	
	/**
	 * Returns the viewer contained in this debug view.
	 *
	 * @return viewer
	 * @since 2.0
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
	 * @since 2.0
	 */
	public void setAction(String actionID, IAction action);
	
	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction
	 * @since 2.0
	 */
	public IAction getAction(String actionID);
	
	/**
	 * Returns the context menu manager for this view.
	 *
	 * @return the context menu manager for this view, or <code>null</code> if none
	 * @since 2.0
	 */
	public IMenuManager getContextMenuManager();
}