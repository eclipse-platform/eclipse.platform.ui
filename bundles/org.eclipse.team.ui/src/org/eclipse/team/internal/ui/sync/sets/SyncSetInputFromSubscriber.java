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
package org.eclipse.team.internal.ui.sync.sets;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;

/**
 * This class translates resource deltas and subscriber events into the effects 
 * on a sync set
 */
public class SyncSetInputFromSubscriber extends SyncSetInput  {

	private TeamSubscriber subscriber;

	public SyncSetInputFromSubscriber(TeamSubscriber subscriber) {
		this.subscriber = subscriber;
	}
		
	public void disconnect() {
	}
	
	public TeamSubscriber getSubscriber() {
		return subscriber;
	}
	
	protected IResource[] getRoots() {
		return getSubscriber().roots();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetInput#fetchInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fetchInput(IProgressMonitor monitor) throws TeamException {
		// don't calculate changes unless 
	}
}
