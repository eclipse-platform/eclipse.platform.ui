/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	private SortedMap configurationMap;
	private SortedMap scopeMap;
	private List definitions;
	
	private Registry() {
		super();
		configurationMap = new TreeMap();
		scopeMap = new TreeMap();
		definitions = new ArrayList();
		(new RegistryReader()).read(Platform.getPluginRegistry(), this);
	}

	public SortedMap getConfigurationMap() {
		return Collections.unmodifiableSortedMap(configurationMap);			
	}

	public SortedMap getScopeMap() {
		return Collections.unmodifiableSortedMap(scopeMap);			
	}

	public List getDefinitions() {
		return Collections.unmodifiableList(definitions);		
	}	

	void addConfiguration(Configuration configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
		
		configurationMap.put(configuration.getId(), configuration);	
	}

	void addScope(Scope scope)
		throws IllegalArgumentException {
		if (scope == null)
			throw new IllegalArgumentException();
		
		scopeMap.put(scope.getId(), scope);
	}

	void addDefinition(Definition definition)
		throws IllegalArgumentException {
		if (definition == null)
			throw new IllegalArgumentException();
		
		definitions.add(definition);
	}
}
