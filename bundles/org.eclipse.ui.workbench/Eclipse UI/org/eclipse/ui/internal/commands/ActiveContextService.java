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

import org.eclipse.ui.commands.IActiveContextService;

public final class ActiveContextService implements IActiveContextService {
	
	private String[] activeContextIds = new String[0];
	
	public ActiveContextService() {
		super();
	}
   
	/*
	 * @see IActiveContextService#getActiveContexts()
	 */
	public String[] getActiveContexts() {
		return (String[]) activeContextIds.clone();	
	}	
	
	/*
	 * @see IActiveContextService#setActiveContexts(String[] activeContexts)
	 */
	public void setActiveContexts(String[] activeContextIds)
		throws IllegalArgumentException {
		if (activeContextIds == null || activeContextIds.length < 1)
			throw new IllegalArgumentException();
			
		activeContextIds = (String[]) activeContextIds.clone();
    	
		for (int i = 0; i < activeContextIds.length; i++)
			if (activeContextIds[i] == null)
				throw new IllegalArgumentException(); 
				
		this.activeContextIds = activeContextIds;   	
	}    
}
