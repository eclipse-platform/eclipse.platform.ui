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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Manager which provides access to the breakpoint container factories
 * which are contributed via the org.eclipse.debug.ui.breakpointContainerFactories
 * extension point.
 */
public class BreakpointContainerFactoryManager {
	
	private static BreakpointContainerFactoryManager fgManager;
	
	private Map fFactories= new HashMap();

	/**
	 * Returns the singleton instance of the breakpoint container
	 * factory manager.
	 */
	public static BreakpointContainerFactoryManager getDefault() {
		if (fgManager == null) {
			fgManager= new BreakpointContainerFactoryManager();
		}
		return fgManager;
	}
	
	/**
	 * Creates and initializes a new breakpoint container factory.
	 */
	private BreakpointContainerFactoryManager() {
		loadContainerFactories();
	}

	/**
	 * Loads all contributed breakpoint container factories contributed via extension.
	 */
	private void loadContainerFactories() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_BREAKPOINT_CONTAINER_FACTORIES);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element= configurationElements[i];
			IBreakpointContainerFactory factory = new BreakpointContainerFactory(element);
			if (validateFactory(factory)) {
				fFactories.put(factory.getIdentifier(), factory);
			}
		}
	}
	
	/**
	 * Validates the given factory. Checks that certain required attributes
	 * are available.
	 * @param factory the factory to validate
	 * @return whether the given factory is valid
	 */
	protected static boolean validateFactory(IBreakpointContainerFactory factory) {
		String id = factory.getIdentifier();
		String label = factory.getLabel();
		return id != null && id.length() > 0 && label != null && label.length() > 0;
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
