package org.eclipse.ui.internal.registry;

/**
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import java.util.ArrayList;
import java.util.List;

public final class AcceleratorSet {
	
	private String configurationId;
	private String scopeId;
	private String pluginId;
	private List accelerators;

	AcceleratorSet(String configurationId, String scopeId, String pluginId) {
		super();
		this.configurationId = configurationId;
		this.scopeId = scopeId;
		this.pluginId = pluginId;
		accelerators = new ArrayList();
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

	public boolean add(Accelerator a) {
		return accelerators.add(a);
	}

	public Accelerator[] getAccelerators() {
		return (Accelerator[]) accelerators.toArray(new Accelerator[accelerators.size()]);
	}
}
