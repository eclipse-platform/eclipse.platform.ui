/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * Notified of viewer updates.
 * 
 * @since 3.3
 */
public interface IViewerUpdateListener {

	/**
	 * Notification that a sequence of viewer updates are starting.
	 */
	public void viewerUpdatesBegin();
	
	/**
	 * Notification that viewer updates are complete. Corresponds to
	 * a <code>viewerUpdatesBegin()</code> notification.
	 */
	public void viewerUpdatesComplete();
	
	/**
	 * Notification that a specific update has started within
	 * a sequence of updates.
	 * 
	 * @param update update
	 */
	public void updateStarted(IViewerUpdate update);
	
	/**
	 * Notification that a specific update has completed within a
	 * sequence of updates.
	 * 
	 * @param update update
	 */
	public void updateComplete(IViewerUpdate update);
}
