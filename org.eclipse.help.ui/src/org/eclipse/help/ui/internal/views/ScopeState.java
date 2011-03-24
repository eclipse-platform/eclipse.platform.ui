/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.views;

public class ScopeState  {
	
	private static ScopeState instance;
	private ScopeSetManager scopeSetManager;
	private EngineDescriptorManager engineManager;
	
	public static synchronized ScopeState getInstance() {		
		if ( instance == null )
		{
			instance = new ScopeState();
		}
		return instance;
	}
	
	private ScopeState() {	
		scopeSetManager = new ScopeSetManager();
	}

	public ScopeSetManager getScopeSetManager() {
		return scopeSetManager;
	}

	public EngineDescriptorManager getEngineManager() {
		return engineManager;
	}

	public void setEngineManager(EngineDescriptorManager engineManager) {
		this.engineManager = engineManager;
	}
	
}
