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
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read navigator extensions from the registry.
 */
public class NavigatorRegistryReader extends RegistryReader {
	private static final String TAG_VIEW="view";	//$NON-NLS-1$
	private NavigatorRegistry registry;
	
/**
 * Overrides method in RegistryReader.
 * 
 * @see RegistryReader#readElement(IConfigurationElement)
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_VIEW)) {
		try {
			NavigatorDescriptor desc = new NavigatorDescriptor(element);
			registry.add(desc);
		} catch (WorkbenchException e) {
			// log an error since its not safe to open a dialog here
			WorkbenchPlugin.log("Unable to create navigator descriptor.",e.getStatus());//$NON-NLS-1$
		}
		return true;
	}
	
	return false;
}
/**
 * 
 * @param in the plugin registry to read from
 * @param out 
 */
public void readRegistry(IPluginRegistry in, NavigatorRegistry out) {
	registry = out;
	readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_NAVIGATOR);
}
}
