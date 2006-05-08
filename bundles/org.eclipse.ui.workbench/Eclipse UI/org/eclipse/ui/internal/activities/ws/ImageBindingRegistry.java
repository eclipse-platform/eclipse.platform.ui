/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.1
 */
public class ImageBindingRegistry implements IExtensionChangeHandler {
	private String tag; 
	private ImageRegistry registry = new ImageRegistry();
	
	/**
	 * @param tag 
	 * 
	 */
	public ImageBindingRegistry(String tag) {
		super();
		this.tag = tag;
		IExtension [] extensions = getExtensionPointFilter().getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addExtension(PlatformUI.getWorkbench().getExtensionTracker(), extensions[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement [] elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals(tag)) {
				String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				String file = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (file == null || id == null) {
					continue; //ignore - malformed
				}
				if (registry.getDescriptor(id) == null) { // first come, first serve
					ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(element.getNamespace(), file);
					if (descriptor != null) {
						registry.put(id, descriptor);
						tracker.registerObject(extension, id, IExtensionTracker.REF_WEAK);
					}
				}
			}
		}
		
	}
    
    /**
     * Return the activity support extension point that this registry is interested in.
     * 
     * @return the extension point
     */
	public IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
                PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof String) {
				registry.remove((String) objects[i]);
			}
		}
	}
	
	/**
	 * Get the ImageDescriptor for the given id.
	 * 
	 * @param id the id
	 * @return the descriptor
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return registry.getDescriptor(id);
	}
	
	/**
	 * Dispose of this registry.
	 */
	void dispose() {
		registry.dispose();
	}

}
