package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 */
public class AcceleratorSet {
	private String configurationId;
	private String scopeId;
	private HashSet accelerators;
	
	public AcceleratorSet(String configurationId, String scopeId) {
		this.configurationId = configurationId;
		this.scopeId = scopeId;
		accelerators = new HashSet();
	}
	
	public String getConfigurationId() {
		return configurationId;
	}
	public String getScopeId() {
		return scopeId;
	}
	public HashSet getAccelerators() {
		return accelerators;	
	}
	
	/**
	 * Returns the Accelerator with the given id.
	 * 
	 * @return the accelerator with the given id, or <code>null</code>
	 * if no accelerator with the given id is found
	 */
	public Accelerator getAcceleratorById(String id) {
		Iterator i = accelerators.iterator();
		while(i.hasNext()) {
			Accelerator a = (Accelerator)(i.next());
			if(a.getId()==id)
				return a;
		}
		return null;
	}
	
	public boolean add(Accelerator a) {
		return accelerators.add(a);
	}
}
