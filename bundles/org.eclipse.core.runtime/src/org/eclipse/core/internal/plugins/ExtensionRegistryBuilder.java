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

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPluginEvent;
import org.eclipse.core.runtime.IPluginListener;

public class ExtensionRegistryBuilder implements IPluginListener {

private ExtensionRegistry registry;
public ExtensionRegistryBuilder(ExtensionRegistry registry, IPluginDescriptor[] existing) {
	this.registry = registry;
	PluginHost[] existingHosts = new PluginHost[existing.length];
	for (int i = 0; i < existingHosts.length; i++)
		existingHosts[i] = new PluginHost(existing[i]);
	registry.add(existingHosts);
}
public void pluginChanged(IPluginEvent[] events) {
	for (int i = 0; i < events.length; i++) {
		switch (events[i].getType()) {
			case IPluginEvent.RESOLVED:
				registry.add(new PluginHost(events[i].getPluginDescriptor()));
				break;
			case IPluginEvent.UNRESOLVED :
			case IPluginEvent.UPDATED :
				// this is bogus: in Eclipse, multiple versions are possible 
				registry.remove(events[i].getPluginDescriptor().getUniqueIdentifier());
				break;
		}
	}
	
}

}
