/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Callback interface for clients interested in being notified about the lifecycle of 
 * a {@link Subscriber#refresh(IResource[], int, IProgressMonitor)} operation.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IRefreshEvent 
 * @see Subscriber
 * @since 3.0
 */
public interface IRefreshSubscriberListener {
	/**
	 * Notification that a refresh is about to start. The event contains the resources
	 * that will be refreshed.
	 * 
	 * @param event an event describing the state of the refresh.
	 */
	public void refreshStarted(IRefreshEvent event);	
	
	/**
	 * Notification that a refresh has completed. The event contains the changes
	 * found during the refresh as well as the status of the refresh.
	 * 
	 * @param event the event describing the result of the refresh.
	 */
	public ActionFactory.IWorkbenchAction refreshDone(IRefreshEvent event);
}
