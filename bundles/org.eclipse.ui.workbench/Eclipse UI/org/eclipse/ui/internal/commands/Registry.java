/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

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
	
	private SortedMap commandMap;
	private SortedMap groupMap;
	
	private Registry() {
		super();
		commandMap = new TreeMap();
		groupMap = new TreeMap();
		(new RegistryReader()).read(Platform.getPluginRegistry(), this);		
	}

	public SortedMap getCommandMap() {
		return Collections.unmodifiableSortedMap(commandMap);			
	}

	public SortedMap getGroupMap() {
		return Collections.unmodifiableSortedMap(groupMap);			
	}

	void addCommand(Item item)
		throws IllegalArgumentException {
		if (item == null)
			throw new IllegalArgumentException();
		
		commandMap.put(item.getId(), item);	
	}

	void addGroup(Item item)
		throws IllegalArgumentException {
		if (item == null)
			throw new IllegalArgumentException();
		
		groupMap.put(item.getId(), item);	
	}
}
