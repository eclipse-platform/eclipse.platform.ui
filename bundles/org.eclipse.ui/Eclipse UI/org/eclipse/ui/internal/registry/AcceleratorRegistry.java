package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.Platform;

/**
 * Provides access to a list of accelerator configurations, a list
 * of accelerator scopes, and a list of accelerator sets.
 */
public class AcceleratorRegistry {
	private List acceleratorConfigurations;
	private List acceleratorScopes;
	private List acceleratorSets;
	
	public AcceleratorRegistry() {
		acceleratorConfigurations = new ArrayList();
		acceleratorScopes = new ArrayList();
		acceleratorSets = new ArrayList();		
	}

	/**
	 * Adds the given accelerator configuration to the registry.
	 */	
	public boolean addConfiguration(AcceleratorConfiguration a) {
		return acceleratorConfigurations.add(a);	
	}
	
	/**
	 * Adds the given accelerator scope to the registry.
	 */
	public boolean addScope(AcceleratorScope a) {
		return acceleratorScopes.add(a);	
	}

	/**
	 * Adds the given accelerator set to the registry.
	 */	
	public boolean addSet(AcceleratorSet a) {
		return acceleratorSets.add(a);
	}
	
	/**
	 * Loads the accelerator registry from the platform's
	 * plugin registry.
	 */
	public void load() {
		AcceleratorRegistryReader reader = 
			new AcceleratorRegistryReader();
		reader.read(Platform.getPluginRegistry(), this);
	}
	
	/**
	 * Returns a list of all the accelerator sets in the registry.
	 */
	public List getAcceleratorSets() {
		return acceleratorSets;	
	}
	
	/**
	 * Returns a list of accelerator sets which belong to the configuration
	 * with the given id
	 * 
	 * @param configId the id of the accelerator configuration to be queried
	 */
	public List getSetsOf(String configId) {
		List sets = new ArrayList();
		for(int i=0;i<acceleratorSets.size();i++) {
			AcceleratorSet set = (AcceleratorSet)(acceleratorSets.get(i));
			String setConfigId = set.getConfigurationId();
			if(setConfigId.equals(configId)) {
				sets.add(set);
			}
		}
		return sets;	
	}
	
	/**
	 * Returns a list of accelerator sets which belong to both the given
	 * accelerator configuration and scope.
	 * 
	 * @param configId the accelerator configuration to be queried 
	 * @param scopeId the accelerator scope to be queried
	 */	
	public List getSetsOf(String configId, String scopeId) {
		List sets = new ArrayList();
		sets = getSetsOf(configId);
		for(int i=0;i<sets.size();i++) {
			AcceleratorSet set = (AcceleratorSet)(sets.get(i));
			if(!set.getScopeId().equals(scopeId)) {
				sets.remove(i);	
			}
		}
		return sets;	
	}
	
	/**
	 * Queries the given accelerator configuration and scope to find accelerators
	 * which belong to both. Returns a mapping between action definition ids and
	 * accelerator keys representing these accelerators.
	 * 
	 * @param configId the accelerator configuration to be queried 
	 * @param scopeId the accelerator scope to be queried
	 */
	public HashMap getAcceleratorsOf(String configId, String scopeId) {
		HashMap map = new HashMap();
		List sets = getSetsOf(configId, scopeId);
		for(int i=0; i<sets.size(); i++) {
			AcceleratorSet set = (AcceleratorSet)(sets.get(i));
			if (set.getScopeId().equals(scopeId)) {
				HashSet accelerators = set.getAccelerators();
				Iterator iterator = accelerators.iterator();
				while(iterator.hasNext()) {
					Accelerator a = (Accelerator)(iterator.next());
					map.put(a.getId(), a.getKey());	
				}	
			}
		}
		return map;	
	}
}
