package org.eclipse.ui.internal.registry;

/**
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import java.util.*;

import org.eclipse.core.runtime.Platform;

public final class AcceleratorRegistry {
	
	private List configurations;
	private List scopes;
	private List sets;
	private HashMap idToScope;
	
	public AcceleratorRegistry() {
		super();
		configurations = new ArrayList();
		scopes = new ArrayList();
		sets = new ArrayList();
	}

	public void load() {
		(new AcceleratorRegistryReader()).read(Platform.getPluginRegistry(), this);
	}
	
	boolean addConfiguration(AcceleratorConfiguration a) {
		return configurations.add(a);	
	}

	public AcceleratorConfiguration getConfiguration(String id) {
		for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
			AcceleratorConfiguration element = (AcceleratorConfiguration) iterator.next();
			
			if (element.getId().equals(id))
				return element;
		}

		return null;
	}	

	public AcceleratorConfiguration[] getConfigurations() {
		AcceleratorConfiguration[] result = new AcceleratorConfiguration[configurations.size()];
		configurations.toArray(result);
		return result;
	}

	boolean addScope(AcceleratorScope a) {
		return scopes.add(a);	
	}

	AcceleratorScope getScope(String id) {
		if (idToScope == null) {
			idToScope = new HashMap();
			AcceleratorScope scopes[] = getScopes();
			
			for (int i = 0; i < scopes.length; i++) {
				AcceleratorScope s = scopes[i];
				idToScope.put(s.getId(), s);
			}
		}
		
		return (AcceleratorScope) idToScope.get(id);
	}

	public AcceleratorScope[] getScopes() {
		AcceleratorScope[] result = new AcceleratorScope[scopes.size()];
		scopes.toArray(result);
		return result;
	}

	boolean addSet(AcceleratorSet a) {
		return sets.add(a);
	}

	AcceleratorSet getSet(String configId, String scopeId,String pluginId) {
		for (Iterator iterator = sets.iterator(); iterator.hasNext();) {
			AcceleratorSet set = (AcceleratorSet) iterator.next();
			
			if (set.getConfigurationId().equals(configId) &&
				set.getScopeId().equals(scopeId) &&
				set.getPluginId().equals(pluginId))
				return set;
		}
		
		return null;
	}

	public List getAcceleratorSets() {
		return sets;
	}	

	public AcceleratorConfiguration[] getConfigsWithSets() {
		List list = new ArrayList();
		
		for (int i = 0; i < configurations.size(); i++) {
			AcceleratorConfiguration config = (AcceleratorConfiguration) configurations.get(i);
			String configId = config.getId();
			
			for (int j = 0; j < sets.size(); j++) {
				AcceleratorSet set = (AcceleratorSet) sets.get(j);
				
				if (configId.equals(set.getConfigurationId())) {
					list.add(config);
					break;
				}	
			}
		}
		
		return (AcceleratorConfiguration[]) list.toArray(new AcceleratorConfiguration[list.size()]);
	}
}
