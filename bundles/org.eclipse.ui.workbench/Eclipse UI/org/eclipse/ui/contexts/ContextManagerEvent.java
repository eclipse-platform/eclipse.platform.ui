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

package org.eclipse.ui.contexts;

import org.eclipse.ui.internal.commands.util.Util;

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
public class ContextManagerEvent {

	private IContextManager contextManager;

	/**
	 * TODO javadoc
	 * 
	 * @param contextManager
	 * @throws IllegalArgumentException
	 */	
	public ContextManagerEvent(IContextManager contextManager)
		throws IllegalArgumentException {		
		super();
		
		if (contextManager == null)
			throw new IllegalArgumentException();
		
		this.contextManager = contextManager;
	}

	/**
	 * TODO javadoc
	 * 
	 * @param object
	 */		
	public boolean equals(Object object) {
		if (!(object instanceof ContextManagerEvent))
			return false;

		ContextManagerEvent contextManagerEvent = (ContextManagerEvent) object;	
		return Util.equals(contextManager, contextManagerEvent.contextManager);
	}
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */		
	public IContextManager getContextManager() {
		return contextManager;
	}
}
