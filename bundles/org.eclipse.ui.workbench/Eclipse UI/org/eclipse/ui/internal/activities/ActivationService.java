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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.activities.DisposedException;
import org.eclipse.ui.activities.IActivationService;
import org.eclipse.ui.activities.IActivationServiceEvent;
import org.eclipse.ui.activities.IActivationServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class ActivationService implements IActivationService {

	private boolean disposed;
	private Set activeActivityIds = new HashSet();
	private List activationServiceListeners;

	public ActivationService() {
	}

	public void addActivationServiceListener(IActivationServiceListener activationServiceListener) {
		if (disposed)
			return;
		
		if (activationServiceListener == null)
			throw new NullPointerException();
			
		if (activationServiceListeners == null)
			activationServiceListeners = new ArrayList();
		
		if (!activationServiceListeners.contains(activationServiceListener))
			activationServiceListeners.add(activationServiceListener);
	}

	public void dispose() {
		disposed = true;
		activeActivityIds = null;
		fireActivationServiceChanged(new ActivationServiceEvent(this, false, true));		
		activationServiceListeners = null;
	}
	
	public Set getActiveActivityIds()
		throws DisposedException {
		if (disposed)
			throw new DisposedException();

		return Collections.unmodifiableSet(activeActivityIds);
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	public void removeActivationServiceListener(IActivationServiceListener activationServiceListener) {
		if (disposed)
			return;
		
		if (activationServiceListener == null)
			throw new NullPointerException();
			
		if (activationServiceListeners != null)
			activationServiceListeners.remove(activationServiceListener);
	}
		
	public void setActiveActivityIds(Set activeActivityIds)
		throws DisposedException {
			
		if (disposed)
			throw new DisposedException();		
		
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean activationServiceChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			fireActivationServiceChanged(new ActivationServiceEvent(this, true, false));	
		}
	}

	private void fireActivationServiceChanged(IActivationServiceEvent activationServiceEvent) {
		if (activationServiceEvent == null)
			throw new NullPointerException();
		
		if (activationServiceListeners != null)
			for (int i = 0; i < activationServiceListeners.size(); i++)
				((IActivationServiceListener) activationServiceListeners.get(i)).activationServiceChanged(activationServiceEvent);
	}
}
