/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

/**
 * 
 */
public class BreakpointContainerFactoryManager {
	
	private static BreakpointContainerFactoryManager fgManager;
	
	private Map fFactories= new HashMap();

	public static BreakpointContainerFactoryManager getDefault() {
		if (fgManager == null) {
			fgManager= new BreakpointContainerFactoryManager();
		}
		return fgManager;
	}
	
	public BreakpointContainerFactoryManager() {
		loadContainerFactories();
	}

	/**
	 * 
	 */
	private void loadContainerFactories() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_BREAKPOINT_CONTAINER_FACTORIES);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element= configurationElements[i];
			String id= element.getAttribute("id"); //$NON-NLS-1$
			String label= element.getAttribute("label"); //$NON-NLS-1$
			if (id != null && label != null) {
				try {
					IBreakpointContainerFactory factory = (IBreakpointContainerFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
					if (factory != null) {
						factory.setLabel(label);
						factory.setIdentifier(id);
						ImageDescriptor imageDescriptor = getImageDescriptor(element);
						if (imageDescriptor != null) {
							factory.setImageDescriptor(imageDescriptor);
						}
						fFactories.put(id, factory);
					}
				} catch (CoreException e) {
				}
			}
		}
	}
	
	/**
	 * Returns the image for this shortcut, or <code>null</code> if none
	 * 
	 * @return the image for this shortcut, or <code>null</code> if none
	 */
	public ImageDescriptor getImageDescriptor(IConfigurationElement element) {
		ImageDescriptor descriptor= null;
		String iconPath = element.getAttribute("icon"); //$NON-NLS-1$
		// iconPath may be null because icon is optional
		if (iconPath != null) {
			try {
				Bundle bundle = Platform.getBundle(element.getDeclaringExtension().getNamespace());
				URL iconURL = bundle.getEntry("/"); //$NON-NLS-1$
				iconURL = new URL(iconURL, iconPath);
				descriptor = ImageDescriptor.createFromURL(iconURL);
			} catch (MalformedURLException e) {
				DebugUIPlugin.log(e);
			}
		}
		return descriptor;
	}
	
	/**
	 * Returns the factory with the given identifier or <code>null</code>
	 * if none.
	 * @param identifier
	 * @return
	 */
	public IBreakpointContainerFactory getFactory(String identifier) {
		return (IBreakpointContainerFactory) fFactories.get(identifier);
	}
	
	/**
	 * Returns the available breakpoint container factories.
	 * @return the available breakpoint container factories
	 */
	public IBreakpointContainerFactory[] getFactories() {
		return (IBreakpointContainerFactory[]) fFactories.values().toArray(new IBreakpointContainerFactory[0]);
	}

}
