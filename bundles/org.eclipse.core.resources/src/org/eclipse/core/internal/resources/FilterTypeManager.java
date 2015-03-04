/*******************************************************************************
 * Copyright (c) 2008, 2015 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.HashMap;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.runtime.*;

/**
 *  This class collects all the registered {@link AbstractFileInfoMatcher} instances along 
 *  with their properties.
 */
class FilterTypeManager implements IManager {

	private static final String FILTER_ELEMENT = "filterMatcher"; //$NON-NLS-1$

	private HashMap<String, IFilterMatcherDescriptor> factories = new HashMap<String, IFilterMatcherDescriptor>();

	public FilterTypeManager() {
		IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILTER_MATCHERS);
		if (point != null) {
			IExtension[] ext = point.getExtensions();
			// initial population
			for (int i = 0; i < ext.length; i++) {
				IExtension extension = ext[i];
				processExtension(extension);
			}
			RegistryFactory.getRegistry().addListener(new IRegistryEventListener() {
				@Override
				public void added(IExtension[] extensions) {
					for (int i = 0; i < extensions.length; i++)
						processExtension(extensions[i]);
				}

				@Override
				public void added(IExtensionPoint[] extensionPoints) {
					// nothing to do
				}

				@Override
				public void removed(IExtension[] extensions) {
					for (int i = 0; i < extensions.length; i++)
						processRemovedExtension(extensions[i]);
				}

				@Override
				public void removed(IExtensionPoint[] extensionPoints) {
					// nothing to do
				}
			});
		}
	}

	public IFilterMatcherDescriptor getFilterDescriptor(String id) {
		return factories.get(id);
	}

	public IFilterMatcherDescriptor[] getFilterDescriptors() {
		return factories.values().toArray(new IFilterMatcherDescriptor[0]);
	}

	protected void processExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equalsIgnoreCase(FILTER_ELEMENT)) {
				IFilterMatcherDescriptor desc = new FilterDescriptor(element);
				factories.put(desc.getId(), desc);
			}
		}
	}

	protected void processRemovedExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equalsIgnoreCase(FILTER_ELEMENT)) {
				IFilterMatcherDescriptor desc = new FilterDescriptor(element, false);
				factories.remove(desc.getId());
			}
		}
	}

	@Override
	public void shutdown(IProgressMonitor monitor) {
		//nothing to do
	}

	@Override
	public void startup(IProgressMonitor monitor) {
		//nothing to do
	}
}
