package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Platform;

/**
 * 
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
	
	public boolean addConfiguration(AcceleratorConfiguration a) {
		return acceleratorConfigurations.add(a);	
	}
	
	public boolean addScope(AcceleratorScope a) {
		return acceleratorScopes.add(a);	
	}
	
	public boolean addSet(AcceleratorSet a) {
		return acceleratorSets.add(a);
	}
	
	public void load() {
		AcceleratorRegistryReader reader = 
			new AcceleratorRegistryReader();
		reader.read(Platform.getPluginRegistry(), this);
	}
	
	public List getAcceleratorSets() {
		return acceleratorSets;	
	}
	
	public List getAvailableAcceleratorConfigurations() {
		ArrayList setConfigIds = new ArrayList();
		ArrayList availableConfigIds = new ArrayList();
		// make a list of all config ids referenced by accelerator sets
		for(int i=0; i<acceleratorSets.size(); i++) {
			if(acceleratorSets.get(i) instanceof AcceleratorSet) {
				String configId = ((AcceleratorSet)acceleratorSets.get(i)).getConfigurationId();
				if(!setConfigIds.contains(configId)) {
					setConfigIds.add(configId);
				}
			}
		}
		//for each id in seConfigIds, see if it is a registered configuration id
		// if so, add it to availableConfigIds
		for(int i=0; i<setConfigIds.size(); i++) {
			for(int j=0; j<acceleratorConfigurations.size(); j++) {
				if(setConfigIds.get(i).equals(acceleratorConfigurations.get(j)))
					availableConfigIds.add(setConfigIds.get(i));
					break;
			}		
		}
		return availableConfigIds;
	}
}
