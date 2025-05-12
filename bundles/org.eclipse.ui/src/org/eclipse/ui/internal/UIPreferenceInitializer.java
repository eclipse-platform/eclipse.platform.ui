/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kiryl Kazakevich, Intel - bug 88359
 *     Tonny Madsen, RCP Company - bug 201055
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 485313
 *     Christoph Läubrich - Bug 552773 - Simplify logging in platform code base
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
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

	@Override
	public void initializeDefaultPreferences() {


		IScopeContext context = DefaultScope.INSTANCE;
		IEclipsePreferences node = context.getNode(UIPlugin.getDefault().getBundle().getSymbolicName());
		// initialize preference node, see Bug 564662
		context.getNode(WorkbenchPlugin.getDefault().getBundle().getSymbolicName());
		node.put(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		// Although there is no longer any item on the preference pages
		// for setting the linking preference, since it is now a per-part
		// setting, it remains as a preference to allow product overrides of the
		// initial state of linking in the Navigator. By default, linking is
		// off.
		node.putBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR,
				false);

		node.putBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS, true);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
				false);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_TEXT_ON_QUICK_ACCESS, false);
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

		// Default for prompting for save when saveables are still held on to by
		// other parts
		node.putBoolean(
				IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN,
				true);

		// Default the min/max behaviour to the old (3.2) style
		node.putBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, true);

		// By default the Fast View Bar allows to select a new fast view from
		// the view list
		node.putBoolean(IWorkbenchPreferenceConstants.DISABLE_NEW_FAST_VIEW,
				false);

		// Default the sticky view close behaviour to the new style
		node.putBoolean(
				IWorkbenchPreferenceConstants.ENABLE_32_STICKY_CLOSE_BEHAVIOR,
				false);

		node.putInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION, SWT.TOP);
		node.putInt(IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);
		node.putBoolean(
				IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS, true);

		node.putInt(IWorkbenchPreferenceConstants.RECENTLY_USED_WORKINGSETS_SIZE, 5);

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
			UIPlugin.getDefault().getLog().error(e.getLocalizedMessage(), e);
		}

		rootNode
				.addNodeChangeListener(new IEclipsePreferences.INodeChangeListener() {

					@Override
					public void added(NodeChangeEvent event) {
						if (!event.getChild().name().equals(uiName)) {
							return;
						}
						((IEclipsePreferences) event.getChild())
								.addPreferenceChangeListener(PlatformUIPreferenceListener
										.getSingleton());

					}

					@Override
					public void removed(NodeChangeEvent event) {
						// Nothing to do here

					}

				});
	}

}
