/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.Platform;

public final class Registry {

	public static Registry instance;
	
	public static Registry getInstance() {
		if (instance == null)
			instance = new Registry();
	
		return instance;
	}
	
	private SortedMap actionMap;
	
	private Registry() {
		super();
		actionMap = new TreeMap();
		(new RegistryReader()).read(Platform.getPluginRegistry(), this);		
	}

	public SortedMap getActionMap() {
		return Collections.unmodifiableSortedMap(actionMap);			
	}
	
	void addAction(Action action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();
		
		actionMap.put(action.getId(), action);	
	}
}
