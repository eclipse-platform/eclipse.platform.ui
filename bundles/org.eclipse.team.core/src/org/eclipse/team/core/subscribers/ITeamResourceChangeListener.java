/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.subscribers;


import java.util.EventListener;

/**
 * A resource state change listener is notified of changes to resources
 * regarding their team state. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see ITeamManager#addResourceStateChangeListener(IResourceStateChangeListener)
 */
public interface ITeamResourceChangeListener extends EventListener{
	
	/**
	 * Notifies this listener that some resources' team properties have
	 * changed. The changes have already happened. For example, a resource's 
	 * base revision may have changed. The resource tree is open for modification 
	 * when this method is invoked, so markers can be created, etc.
	 * <p>
	 * Note: This method is called by Team core; it is not intended to be
	 * called directly by clients.
	 * </p>
	 *
	 * @param deltas detailing the kinds of team changes
	 * 
	 * [Note: The changed state event is purposely vague. For now it is only
	 * a hint to listeners that they should query the provider to determine the
	 * resources new sync info.]
	 */
	public void teamResourceChanged(TeamDelta[] deltas);
}

