/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;


/**
 * Listener for viewer state updates.
 * 
 * @since 3.6
 */
public interface IStateUpdateListener {

	/**
	 * Notification that a sequence of state saving updates are starting.
	 */
	public void stateSaveUpdatesBegin(Object input);
	
	/**
	 * Notification that viewer updates are complete. Corresponds to
	 * a <code>viewerUpdatesBegin()</code> notification.
	 */
	public void stateSaveUpdatesComplete(Object input);

	/**
     * Notification that a sequence of viewer updates are starting.
     */
    public void stateRestoreUpdatesBegin(Object input);
    
    /**
     * Notification that viewer updates are complete. Corresponds to
     * a <code>viewerUpdatesBegin()</code> notification.
     */
    public void stateRestoreUpdatesComplete(Object input);

	/**
	 * Notification that a specific update has started within
	 * a sequence of updates.
	 * 
	 * @param update update
	 */
	public void stateUpdateStarted(Object input, IViewerUpdate update);
	
	/**
	 * Notification that a specific update has completed within a
	 * sequence of updates.
	 * 
	 * @param update update
	 */
	public void stateUpdateComplete(Object input, IViewerUpdate update);
}
