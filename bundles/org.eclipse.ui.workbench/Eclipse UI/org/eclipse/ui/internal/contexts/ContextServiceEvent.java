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

import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IContextServiceEvent;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class ContextServiceEvent 
	implements IContextServiceEvent {

	private IContextService contextService;

	/**
	 * TODO javadoc
	 * 
	 * @param contextService
	 * @throws IllegalArgumentException
	 */	
	public ContextServiceEvent(IContextService contextService)
		throws IllegalArgumentException {		
		super();
		
		if (contextService == null)
			throw new IllegalArgumentException();
		
		this.contextService = contextService;
	}

	/**
	 * TODO javadoc
	 * 
	 * @param object
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ContextServiceEvent))
			return false;

		ContextServiceEvent contextServiceEvent = (ContextServiceEvent) object;	
		return contextService.equals(contextServiceEvent.contextService);
	}

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */		
	public IContextService getContextService() {
		return contextService;
	}
}
