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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
	
	private List contexts;
	private IPluginRegistry pluginRegistry;
	private List unmodifiableContexts;
	
	RegistryReader(IPluginRegistry pluginRegistry) {
		super();	
		this.pluginRegistry = pluginRegistry;
		unmodifiableContexts = Collections.EMPTY_LIST;
	}

	List getContexts() {
		return unmodifiableContexts;
	}

	void load() {
		if (contexts == null)
			contexts = new ArrayList();
		else 
			contexts.clear();

		if (pluginRegistry != null)	
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);
			
		unmodifiableContexts = Collections.unmodifiableList(new ArrayList(contexts));
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();

		if (Persistence.TAG_CONTEXT.equals(name))
			return readContext(element);

		return false;
	}

	private String getPluginId(IConfigurationElement element) {
		String pluginId = null;	
	
		if (element != null) {	
			IExtension extension = element.getDeclaringExtension();
		
			if (extension != null) {
				IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
			
				if (pluginDescriptor != null) 
					pluginId = pluginDescriptor.getUniqueIdentifier();				
			}
		}

		return pluginId;
	}

	private boolean readContext(IConfigurationElement element) {
		IContext context = Persistence.readContext(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (context != null)
			contexts.add(context);	
		
		return true;
	}
}