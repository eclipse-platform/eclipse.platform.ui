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

package org.eclipse.ui.commands.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.ui.commands.IContextService;
import org.eclipse.ui.internal.commands.Util;

public final class ContextService implements IContextService {
	
	private List contextIds;
	private IContextServiceObserver contextServiceObserver;
	
	public ContextService(IContextServiceObserver contextServiceObserver)
		throws IllegalArgumentException {
		super();

		if (contextServiceObserver == null)
			throw new IllegalArgumentException();

		this.contextIds = Collections.EMPTY_LIST;
		this.contextServiceObserver = contextServiceObserver;
	}

	public List getContexts() {
		return contextIds;
	}
	
	public void setContexts(List contextIds)
		throws IllegalArgumentException {
		this.contextIds = Util.safeCopy(contextIds, String.class);    
		contextServiceObserver.contextServiceChanged(this); 
	}   
}
