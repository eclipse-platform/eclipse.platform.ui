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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

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
					factory.setLabel(label);
					factory.setIdentifier(id);
					if (factory != null) {
						fFactories.put(id, factory);
					}
				} catch (CoreException e) {
				}
			}
		}
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
