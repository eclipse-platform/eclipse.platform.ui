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

package org.eclipse.ui.internal.contexts;

import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceEvent;
import org.eclipse.ui.internal.util.Util;

final class ContextActivationServiceEvent implements IContextActivationServiceEvent {

	private IContextActivationService contextActivationService;

	ContextActivationServiceEvent(IContextActivationService contextActivationService) {
		super();
		
		if (contextActivationService == null)
			throw new NullPointerException();
		
		this.contextActivationService = contextActivationService;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextActivationServiceEvent))
			return false;

		ContextActivationServiceEvent contextActivationServiceEvent = (ContextActivationServiceEvent) object;	
		return Util.equals(contextActivationService, contextActivationServiceEvent.contextActivationService);
	}
	
	public IContextActivationService getContextActivationService() {
		return contextActivationService;
	}
}
