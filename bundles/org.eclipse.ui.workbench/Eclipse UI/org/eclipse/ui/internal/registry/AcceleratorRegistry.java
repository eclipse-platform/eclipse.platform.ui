/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

public final class AcceleratorRegistry {
	
	private Map acceleratorConfigurations;
	private Map acceleratorScopes;
	private List acceleratorSets;
	
	public AcceleratorRegistry() {
		super();
		acceleratorConfigurations = new HashMap();
		acceleratorScopes = new HashMap();
		acceleratorSets = new ArrayList();
	}

	public void load() {
		(new AcceleratorRegistryReader()).read(Platform.getPluginRegistry(), this);
	}
	
	void addAcceleratorConfiguration(AcceleratorConfiguration acceleratorConfiguration)
		throws IllegalArgumentException {
		if (acceleratorConfiguration == null)
			throw new IllegalArgumentException();
		
		acceleratorConfigurations.put(acceleratorConfiguration.getId(), acceleratorConfiguration);	
	}

	public Map getAcceleratorConfigurations() {
		return Collections.unmodifiableMap(acceleratorConfigurations);			
	}

	void addAcceleratorScope(AcceleratorScope acceleratorScope)
		throws IllegalArgumentException {
		if (acceleratorScope == null)
			throw new IllegalArgumentException();
		
		acceleratorScopes.put(acceleratorScope.getId(), acceleratorScope);	
	}

	public Map getAcceleratorScopes() {
		return Collections.unmodifiableMap(acceleratorScopes);			
	}

	void addAcceleratorSet(AcceleratorSet acceleratorSet) {
		acceleratorSets.add(acceleratorSet);
	}

	public List getAcceleratorSets() {
		return Collections.unmodifiableList(acceleratorSets);		
	}	
}
