/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.util.*;
/**
 * Holds mapping of short plugin Id to PluginContext
 */
class PluginsContexts {
	private Map map = new HashMap();
	public void put(String pluginId, PluginContexts contexts) {
		map.put(pluginId, contexts);
	}
	public PluginContexts get(String pluginId) {
		return (PluginContexts) map.get(pluginId);
	}
	public void remove(String pluginId) {
		map.remove(pluginId);
	}
}
