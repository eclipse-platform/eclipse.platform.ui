/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * Notified of label updates.
 * 
 * @since 3.3
 */
public interface ILabelUpdateListener {

	/**
	 * Notification that a sequence of viewer updates are starting.
	 */
	public void labelUpdatesBegin();
	
	/**
	 * Notification that viewer updates are complete. Corresponds to
	 * a <code>viewerUpdatesBegin()</code> notification.
	 */
	public void labelUpdatesComplete();
	
	/**
	 * Notification that a specific update has started within
	 * a sequence of updates.
	 * 
	 * @param update update
	 */
	public void labelUpdateStarted(ILabelUpdate update);
	
	/**
	 * Notification that a specific update has completed within a
	 * sequence of updates.
	 * 
	 * @param update update
	 */
	public void labelUpdateComplete(ILabelUpdate update);
}
