/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;

/**
 */
public class HelpBasePreferenceInitializer
		extends
			AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();

		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		boolean isWindows = os.indexOf("windows") != -1; //$NON-NLS-1$

		if (isWindows)
			prefs
					.setDefault("custom_browser_path", //$NON-NLS-1$
							"\"C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE\" %1"); //$NON-NLS-1$
		else
			prefs.setDefault("custom_browser_path", "mozilla %1"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
