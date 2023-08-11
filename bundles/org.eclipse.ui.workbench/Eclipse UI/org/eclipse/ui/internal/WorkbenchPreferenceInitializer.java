/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 *     Tristan Hume - <trishume@gmail.com> -
 *     		Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 *     		Implemented workbench auto-save to correctly restore state in case of crash.
 *     Denis Zygann <d.zygann@web.de> - Bug 330453
 *     Axel Richard <axel.richard@obeo.fr> - Bug 486644
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 146205
 *     Patrik Suzzi <psuzzi@itemis.com> - Bug 529885
 *     Christian Georgi (SAP SE) - Bug 540440
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
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Implementation of the workbench plugin's preferences extension's
 * customization element. This is needed in order to force the workbench
 * plugin's preferences to be initialized properly when running without
 * org.eclipse.core.runtime.compatibility. For more details, see bug 58975 - New
 * preference mechanism does not properly initialize defaults.
 *
 * @since 3.0
 */
public class WorkbenchPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IScopeContext context = DefaultScope.INSTANCE;
		IEclipsePreferences node = context.getNode(WorkbenchPlugin.getDefault().getBundle().getSymbolicName());

		node.putBoolean(IPreferenceConstants.RUN_IN_BACKGROUND, true);
		node.putBoolean(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT, true);

		node.putBoolean(IPreferenceConstants.EDITORLIST_PULLDOWN_ACTIVE, false);
		node.putBoolean(IPreferenceConstants.EDITORLIST_DISPLAY_FULL_NAME, false);
		node.putBoolean(IPreferenceConstants.STICKY_CYCLE, false);
		node.putBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, true);
		node.putInt(IPreferenceConstants.REUSE_EDITORS, 99);
		node.putBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK, false);
		node.putBoolean(IPreferenceConstants.SELECT_ON_HOVER, false);
		node.putBoolean(IPreferenceConstants.OPEN_AFTER_DELAY, false);
		node.putInt(IPreferenceConstants.RECENT_FILES, 20);

		// FIXME this does not actually set the default since it is the wrong
		// node. It works because the default-default is false.
		node.putBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE, false);

		// 5 minute workbench save interval
		node.putInt(IPreferenceConstants.WORKBENCH_SAVE_INTERVAL, 5);

		node.putBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS, true);

		node.putBoolean(IPreferenceConstants.COOLBAR_VISIBLE, true);
		node.putBoolean(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE, true);

		node.putInt(IPreferenceConstants.EDITOR_TAB_WIDTH, 3); // high
		node.putInt(IPreferenceConstants.OPEN_PERSP_MODE, IPreferenceConstants.OPM_ACTIVE_PAGE);
		node.put(IPreferenceConstants.ENABLED_DECORATORS, ""); //$NON-NLS-1$
		node.putInt(IPreferenceConstants.EDITORLIST_SELECTION_SCOPE, IPreferenceConstants.EDITORLIST_SET_PAGE_SCOPE); // Current
																														// Window
		node.putInt(IPreferenceConstants.EDITORLIST_SORT_CRITERIA, IPreferenceConstants.EDITORLIST_NAME_SORT); // Name
																												// Sort
		node.putBoolean(IPreferenceConstants.COLOR_ICONS, true);
		node.putInt(IPreferenceConstants.KEYS_PREFERENCE_SELECTED_TAB, 0);
		node.putBoolean(IPreferenceConstants.MULTI_KEY_ASSIST, true);
		node.putInt(IPreferenceConstants.MULTI_KEY_ASSIST_TIME, 1000);

		// Temporary option to enable wizard for project capability
		node.putBoolean("ENABLE_CONFIGURABLE_PROJECT_WIZARD", false); //$NON-NLS-1$
		// Temporary option to enable single click
		node.putInt("SINGLE_CLICK_METHOD", OpenStrategy.DOUBLE_CLICK); //$NON-NLS-1$
		// Temporary option to enable cool bars
		node.putBoolean("ENABLE_COOL_BARS", true); //$NON-NLS-1$
		// Temporary option to enable new menu organization
		node.putBoolean("ENABLE_NEW_MENUS", true); //$NON-NLS-1$
		// Temporary option to turn off the dialog font
		node.putBoolean("DISABLE_DIALOG_FONT", false); //$NON-NLS-1$

		// Heap status preferences is stored in different node
		IEclipsePreferences heapNode = context.getNode("org.eclipse.ui"); //$NON-NLS-1$
		heapNode.putBoolean(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, false);
		node.putInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL, 500);
		node.putBoolean(IHeapStatusConstants.PREF_SHOW_MAX, false);
		node.putBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION, false);

		// Globalization preferences
		node.put(IPreferenceConstants.NL_EXTENSIONS, ""); //$NON-NLS-1$
		node.putInt(IPreferenceConstants.LAYOUT_DIRECTION, SWT.NONE);
		node.putBoolean(IPreferenceConstants.BIDI_SUPPORT, false);
		node.put(IPreferenceConstants.TEXT_DIRECTION, ""); //$NON-NLS-1$

		// Auto-save
		node.putBoolean(IPreferenceConstants.SAVE_AUTOMATICALLY, false);
		node.putInt(IPreferenceConstants.SAVE_AUTOMATICALLY_INTERVAL, 20);

		// Progress view
		node.putInt(IPreferenceConstants.MAX_PROGRESS_ENTRIES, 20);

		// Visualized command keys
		node.putBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD, false);
		node.putBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS, false);
		node.putInt(IPreferenceConstants.SHOW_KEYS_TIME_TO_CLOSE, 3000);

		node.putInt(IWorkbenchPreferenceConstants.DISPOSE_CLOSED_BROWSER_HOVER_TIMEOUT, -1);

		// Don't show more than 10.000 child elements per parent by default
		node.putInt(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT, 1000);

		node.put(IWorkbenchPreferenceConstants.RESOURCE_RENAME_MODE,
				IWorkbenchPreferenceConstants.RESOURCE_RENAME_MODE_INLINE);

		IEclipsePreferences rootNode = (IEclipsePreferences) Platform.getPreferencesService().getRootNode()
				.node(InstanceScope.SCOPE);

		final String workbenchName = WorkbenchPlugin.getDefault().getBundle().getSymbolicName();
		try {
			if (rootNode.nodeExists(workbenchName)) {
				((IEclipsePreferences) rootNode.node(workbenchName))
						.addPreferenceChangeListener(PlatformUIPreferenceListener.getSingleton());
			}
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.getDefault().getBundle().getSymbolicName(),
					IStatus.ERROR, e.getLocalizedMessage(), e);
			WorkbenchPlugin.getDefault().getLog().log(status);
		}

		LargeFileLimitsPreferenceHandler.setDefaults();
	}

}
