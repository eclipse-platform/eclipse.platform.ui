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
import java.util.List;

public final class AcceleratorSet {
	
	private String acceleratorConfigurationId;
	private String acceleratorScopeId;
	private String pluginId;
	private List accelerators;

	AcceleratorSet(String acceleratorConfigurationId, String acceleratorScopeId, String pluginId) {
		super();
		this.acceleratorConfigurationId = acceleratorConfigurationId;
		this.acceleratorScopeId = acceleratorScopeId;
		this.pluginId = pluginId;
		accelerators = new ArrayList();
	}

	public String getAcceleratorConfigurationId() {
		return acceleratorConfigurationId;
	}

	public String getAcceleratorScopeId() {
		return acceleratorScopeId;
	}

	public String getPluginId() {
		return pluginId;	
	}

	void addAccelerator(Accelerator accelerator) {
		accelerators.add(accelerator);
	}

	public List getAccelerators() {
		return Collections.unmodifiableList(accelerators);
	}
}
