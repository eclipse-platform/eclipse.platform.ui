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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.ui.IWorkbenchPreferenceConstants;

/**
 * Implementation of the UI plugin's preference extension's customization
 * element. This is needed in order to force the UI plugin's preferences to be
 * initialized properly when running without
 * org.eclipse.core.runtime.compatibility. For more details, see bug 58975 - New
 * preference mechanism does not properly initialize defaults.
 * 
 * @since 3.0
 */
public class UIPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {

		IEclipsePreferences node = new DefaultScope().getNode(UIPlugin.getDefault().getBundle()
				.getSymbolicName());
		node.put(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		//Deprecated but kept for backwards compatibility
		node.put(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		node.put(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		node.put(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		//Although there is no longer any item on the preference pages
		//for setting the linking preference, since it is now a per-part
		// setting, it remains as a preference to allow product overrides of the
		//initial state of linking in the Navigator. By default, linking is
		// off.
		node.putBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, false);

		//Appearance / Presentation preferences
		node.put(IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID,
				"org.eclipse.ui.presentations.default"); //$NON-NLS-1$
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, true);
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, true);
		node.put(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
				IWorkbenchPreferenceConstants.TOP_LEFT);
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, true);

		//the fast view bar should be on the bottom of a fresh workspace
		node.put(IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION,
				IWorkbenchPreferenceConstants.BOTTOM);

		//default to showing intro on startup
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_INTRO, true);

		//Default to the standard key configuration.
		node.put(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID,
				"org.eclipse.ui.defaultAcceleratorConfiguration"); //$NON-NLS-1$

		//The default character width is undefined (i.e., -1)
		node.putInt(IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS, -1);

		//Set the workspace selection dialog to open by default
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_WORKSPACE_SELECTION_DIALOG, true);

		new InstanceScope().getNode(UIPlugin.getDefault().getBundle().getSymbolicName())
				.addPreferenceChangeListener(PlatformUIPreferenceListener.getSingleton());
	}

}