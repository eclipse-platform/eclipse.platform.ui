/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;

public final class Registry {
	
	public static Registry instance;
	
	public static Registry getInstance() {
		if (instance == null)
			instance = new Registry();
	
		return instance;
	}
	
	private SortedMap configurationMap;
	private SortedSet regionalBindingSet;
	private SortedMap scopeMap;
	
	private Registry() {
		super();
		configurationMap = new TreeMap();
		regionalBindingSet = new TreeSet();
		scopeMap = new TreeMap();
		(new RegistryReader()).read(Platform.getPluginRegistry(), this);
	}

	public SortedMap getConfigurationMap() {
		return Collections.unmodifiableSortedMap(configurationMap);			
	}

	public SortedSet getRegionalBindingSet() {
		return Collections.unmodifiableSortedSet(regionalBindingSet);		
	}	
	
	public SortedMap getScopeMap() {
		return Collections.unmodifiableSortedMap(scopeMap);			
	}

	void addConfiguration(Configuration configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
		
		configurationMap.put(configuration.getLabel().getId(), configuration);	
	}

	void addRegionalBinding(RegionalBinding regionalBinding)
		throws IllegalArgumentException {
		if (regionalBinding == null)
			throw new IllegalArgumentException();
		
		regionalBindingSet.add(regionalBinding);
	}

	void addScope(Scope scope)
		throws IllegalArgumentException {
		if (scope == null)
			throw new IllegalArgumentException();
		
		scopeMap.put(scope.getLabel().getId(), scope);
	}
}
