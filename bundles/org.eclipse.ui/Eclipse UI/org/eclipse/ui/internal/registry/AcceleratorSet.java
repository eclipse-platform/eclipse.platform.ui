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
	
	public AcceleratorSet(String configurationId, String scopeId, String pluginId) {
		this.configurationId = configurationId;
		this.scopeId = scopeId;
		this.pluginId = pluginId;
		accelerators = new HashSet();
	}
	
	public String getConfigurationId() {
		return configurationId;
	}
	public String getScopeId() {
		return scopeId;
	}
	public String getPluginId() {
		return pluginId;	
	}
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
}
