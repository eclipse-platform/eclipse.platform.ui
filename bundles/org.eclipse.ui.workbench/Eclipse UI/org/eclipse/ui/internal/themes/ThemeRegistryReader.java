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
package org.eclipse.ui.internal.themes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * Registry reader for themes.
 *
 * @since 3.0
 */
public class ThemeRegistryReader extends RegistryReader {
	private static final String TAG_LOOKNFEEL="theme";//$NON-NLS-1$
	private ThemeRegistry themeRegistry;
	
	/**
	 * ThemeRegistryReader constructor comment.
	 */
	public ThemeRegistryReader() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_LOOKNFEEL)) {
			readTheme(element);
			readElementChildren(element);
		}	
		return true;
	}
	
	/**
	 * Read the theme extensions within a registry.
	 */
	public void readThemes(IPluginRegistry in, ThemeRegistry out)
		throws CoreException {
		// this does not seem to really ever be throwing an the exception
		themeRegistry = out;
		readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_THEMES);
	}
	
	/**
	 * Reads the theme element.
	 */
	protected void readTheme(IConfigurationElement element) {
		try {
			IThemeDescriptor desc = new ThemeDescriptor(element);
			themeRegistry.add(desc);
		} catch (CoreException e) {
			// log an error since its not safe to open a dialog here
			WorkbenchPlugin.log("Unable to create theme descriptor." , e.getStatus());//$NON-NLS-1$
		}
	}
	
}
