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

package org.eclipse.ui.internal.commands;

import java.util.Collections;
import java.util.List;


public final class SimpleContextService extends AbstractContextService {
	
	private List contextIds;
	
	public SimpleContextService() {
		super();
		this.contextIds = Collections.EMPTY_LIST;
	}
	
	public List getContexts() {
		return contextIds;
	}
	
	public void setContexts(List contextIds)
		throws IllegalArgumentException {
		this.contextIds = Util.safeCopy(contextIds, String.class);    
		fireContextServiceChanged(); 
	}   
}
