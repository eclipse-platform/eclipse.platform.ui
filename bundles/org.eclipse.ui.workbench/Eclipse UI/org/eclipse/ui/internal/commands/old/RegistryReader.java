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

package org.eclipse.ui.internal.commands.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {

	private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
	
	private List contextBindingElements;
	private List imageBindingElements;
	private IPluginRegistry pluginRegistry;
	private List unmodifiableContextBindingElements;
	private List unmodifiableImageBindingElements;
	
	RegistryReader(IPluginRegistry pluginRegistry) {
		super();	
		this.pluginRegistry = pluginRegistry;
		unmodifiableContextBindingElements = Collections.EMPTY_LIST;		
		unmodifiableImageBindingElements = Collections.EMPTY_LIST;
	}

	List getContextBindingElements() {
		return unmodifiableContextBindingElements;
	}
	
	List getImageBindingElements() {
		return unmodifiableImageBindingElements;
	}

	void load() {
		if (contextBindingElements == null)
			contextBindingElements = new ArrayList();
		else 
			contextBindingElements.clear();
			
		if (imageBindingElements == null)
			imageBindingElements = new ArrayList();
		else 
			imageBindingElements.clear();		

		if (pluginRegistry != null)	
			readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);
			
		unmodifiableContextBindingElements = Collections.unmodifiableList(new ArrayList(contextBindingElements));
		unmodifiableImageBindingElements = Collections.unmodifiableList(new ArrayList(imageBindingElements));
	}

	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();

		if (Persistence.TAG_CONTEXT_BINDING.equals(name))
			return readContextBinding(element);
			
		if (Persistence.TAG_IMAGE_BINDING.equals(name))
			return readImageBinding(element);			

		return true; // TODO return false once commands extension point is complete
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

	private boolean readContextBinding(IConfigurationElement element) {
		ContextBindingElement contextBindingElement = Persistence.readContextBindingElement(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (contextBindingElement != null)
			contextBindingElements.add(contextBindingElement);	
		
		return true;
	}
	
	private boolean readImageBinding(IConfigurationElement element) {
		ImageBindingElement imageBindingElement = Persistence.readImageBindingElement(new ConfigurationElementMemento(element), getPluginId(element));
	
		if (imageBindingElement != null)
			imageBindingElements.add(imageBindingElement);	
		
		return true;
	}
}