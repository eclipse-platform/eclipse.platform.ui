package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A set of mappings between an accelerator key and an action id.
 */
public class AcceleratorSet {
	private String configurationId;
	private String scopeId;
	private String pluginId;
	private String pluginVersion;
	private HashSet accelerators;
	/**
	 * Create an instance of AcceleratorSet and initializes 
	 * it with its configuration id, scope id and plugin id.
	 */		
	public AcceleratorSet(String configurationId, String scopeId, String pluginId) {
		this.configurationId = configurationId;
		this.scopeId = scopeId;
		this.pluginId = pluginId;
		accelerators = new HashSet();
	}
	/**
	 * Returns this AcceleratorSet's configuration id.
	 */
	public String getConfigurationId() {
		return configurationId;
	}
	/**
	 * Returns this AcceleratorSet's scope id.
	 */
	public String getScopeId() {
		return scopeId;
	}
	/**
	 * Returns this AcceleratorSet's plugin id.
	 */
	public String getPluginId() {
		return pluginId;	
	}
	/**
	 * Returns an array with all accelerators defined in this set.
	 */
	public Accelerator[] getAccelerators() {
		Accelerator[] result = new Accelerator[accelerators.size()];
		accelerators.toArray(result);
		return result;	
	}
	/**
	 * Adds the given accelerator to the set.
	 */	
	public boolean add(Accelerator a) {
		return accelerators.add(a);
	}
	/**
	 * Find and return an accelerator with the specified id.
	 * 
	 * @return Accelerator or null.
	 */
	public Accelerator getAccelerator(String id) {
		for (Iterator iterator = accelerators.iterator(); iterator.hasNext();) {
			Accelerator acc = (Accelerator) iterator.next();
			if(acc.getId().equals(id))
				return acc;
		}
		return null;
	}
	/**
	 * Remove the specified Accelerator from this set.
	 */
	public void removeAccelerator(Accelerator acc) {
		accelerators.remove(acc);
	}
}
