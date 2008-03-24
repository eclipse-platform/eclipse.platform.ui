/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kiryl Kazakevich, Intel - bug 88359
 *     Tonny Madsen, RCP Company - bug 201055
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.keys.IBindingService;
import org.osgi.service.prefs.BackingStoreException;

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

		
		IScopeContext context = new DefaultScope();
		IEclipsePreferences node = context.getNode(UIPlugin.getDefault()
				.getBundle().getSymbolicName());
		node.put(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		// Deprecated but kept for backwards compatibility
		node.put(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		node.put(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		node.put(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		// Although there is no longer any item on the preference pages
		// for setting the linking preference, since it is now a per-part
		// setting, it remains as a preference to allow product overrides of the
		// initial state of linking in the Navigator. By default, linking is
		// off.
		node.putBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR,
				false);

		// Appearance / Presentation preferences
		node.put(IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID,
				IWorkbenchConstants.DEFAULT_PRESENTATION_ID);
		node
				.putBoolean(
						IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
						true);
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, true);
		node.putBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS, true);
		node.put(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
				IWorkbenchPreferenceConstants.TOP_LEFT);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
				true);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_OTHER_IN_PERSPECTIVE_MENU,
				true);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_OPEN_ON_PERSPECTIVE_BAR,
				true);

		// the fast view bar should be on the bottom of a fresh workspace
		node.put(IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION,
				IWorkbenchPreferenceConstants.BOTTOM);

		// default to showing intro on startup
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_INTRO, true);

		// Default to the standard key configuration.
		node.put(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID,
				IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID);

		// Preference for showing system jobs in the jobs view
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS, false);

		// The default minimum character width for editor tabs is undefined
		// (i.e., -1)
		node
				.putInt(
						IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS,
						-1);

		// The default minimum character width for view tabs is 1
		node.putInt(IWorkbenchPreferenceConstants.VIEW_MINIMUM_CHARACTERS, 1);

		// Default for closing editors on exit.
		node.putBoolean(IWorkbenchPreferenceConstants.CLOSE_EDITORS_ON_EXIT,
				false);

		// Default for using window working sets
		node
				.putBoolean(
						IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT,
						false);

		// Default for showing filter text widget that determines what is shown
		// in a FilteredTree
		node
				.putBoolean(IWorkbenchPreferenceConstants.SHOW_FILTERED_TEXTS,
						true);

		// Default for enabling detached views
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_DETACHED_VIEWS,
				true);

		// Default for prompting for save when saveables are still held on to by other parts
		node.putBoolean(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN,
				true);

		// Default the min/max behaviour to the old (3.2) style
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, true);
		
		// By default the Fast View Bar allows to select a new fast view from the view list
		node.putBoolean(IWorkbenchPreferenceConstants.DISABLE_NEW_FAST_VIEW, false);
		
		// Default the sticky view close behaviour to the new style
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_32_STICKY_CLOSE_BEHAVIOR, false);

		IEclipsePreferences rootNode = (IEclipsePreferences) Platform
				.getPreferencesService().getRootNode()
				.node(InstanceScope.SCOPE);

		final String uiName = UIPlugin.getDefault().getBundle()
				.getSymbolicName();
		try {
			if (rootNode.nodeExists(uiName)) {
				((IEclipsePreferences) rootNode.node(uiName))
						.addPreferenceChangeListener(PlatformUIPreferenceListener
								.getSingleton());
			}
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getDefault()
					.getBundle().getSymbolicName(), IStatus.ERROR, e
					.getLocalizedMessage(), e);
			UIPlugin.getDefault().getLog().log(status);
		}

		rootNode
				.addNodeChangeListener(new IEclipsePreferences.INodeChangeListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#added(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
					 */
					public void added(NodeChangeEvent event) {
						if (!event.getChild().name().equals(uiName)) {
							return;
						}
						((IEclipsePreferences) event.getChild())
								.addPreferenceChangeListener(PlatformUIPreferenceListener
										.getSingleton());

					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#removed(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
					 */
					public void removed(NodeChangeEvent event) {
						// Nothing to do here

					}

				});
	}

}
