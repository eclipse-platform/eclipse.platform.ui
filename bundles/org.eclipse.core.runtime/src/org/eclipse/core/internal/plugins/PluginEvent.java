/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginEvent;

public class PluginEvent implements IPluginEvent {
	private IPluginDescriptor pluginDescriptor;
	private int type;
	public PluginEvent(IPluginDescriptor pluginDescriptor, int type) {
		this.pluginDescriptor = pluginDescriptor;
		this.type = type;
	}
	public IPluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
	}
	public int getType() {
		return type;
	}
}
