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

import org.eclipse.ui.activities.IActivationService;
import org.eclipse.ui.activities.IActivationServiceEvent;


final class ActivationServiceEvent implements IActivationServiceEvent {

	private boolean activeActivityIdsChanged;
	private IActivationService activationService;
	private boolean disposed; 
	
	ActivationServiceEvent(IActivationService activationService, boolean activeActivityIdsChanged, boolean disposed) {
		if (activationService == null)
			throw new NullPointerException();
		
		this.activeActivityIdsChanged = activeActivityIdsChanged;
		this.activationService = activationService;
		this.disposed = disposed;
	}

	public IActivationService getActivationService() {
		return activationService;
	}
	
	public boolean haveActiveActivityIdsChanged() {
		return activeActivityIdsChanged;
	}
	
	public boolean isDisposed() {
		return disposed;
	}
}
