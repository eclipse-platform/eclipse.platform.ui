/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
public class WorkbenchPreferenceInitializer extends
		AbstractPreferenceInitializer {
	
	

	public void initializeDefaultPreferences() {
		IScopeContext context = new DefaultScope();
		IEclipsePreferences node = context.getNode(WorkbenchPlugin
				.getDefault().getBundle().getSymbolicName());

		node
				.putBoolean(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT,
						true);

		node.putBoolean(IPreferenceConstants.EDITORLIST_PULLDOWN_ACTIVE, false);
		node.putBoolean(IPreferenceConstants.EDITORLIST_DISPLAY_FULL_NAME,
				false);
		node.putBoolean(IPreferenceConstants.STICKY_CYCLE, false);
		node.putBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, false);
		node.putBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS, true);
		node.putInt(IPreferenceConstants.REUSE_EDITORS, 8);
		node.putBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK, false);
		node.putBoolean(IPreferenceConstants.SELECT_ON_HOVER, false);
		node.putBoolean(IPreferenceConstants.OPEN_AFTER_DELAY, false);
		node.putInt(IPreferenceConstants.RECENT_FILES, 4);

		node.putInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION, SWT.TOP);
		node.putInt(IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);

		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS, true);
		node.putBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE, false);
		
		node.putBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS, true);

		node.putInt(IPreferenceConstants.EDITOR_TAB_WIDTH, 3); // high
		node.putInt(IPreferenceConstants.OPEN_VIEW_MODE,
				IPreferenceConstants.OVM_EMBED);
		node.putInt(IPreferenceConstants.OPEN_PERSP_MODE,
				IPreferenceConstants.OPM_ACTIVE_PAGE);
		node.put(IPreferenceConstants.ENABLED_DECORATORS, ""); //$NON-NLS-1$
		node.putInt(IPreferenceConstants.EDITORLIST_SELECTION_SCOPE,
				IPreferenceConstants.EDITORLIST_SET_PAGE_SCOPE); // Current
																 // Window
		node.putInt(IPreferenceConstants.EDITORLIST_SORT_CRITERIA,
				IPreferenceConstants.EDITORLIST_NAME_SORT); // Name Sort
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
		//Temporary option to turn off the dialog font
		node.putBoolean("DISABLE_DIALOG_FONT", false); //$NON-NLS-1$

		// Heap status preferences
		node.putBoolean(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, false);
		node.putInt(IHeapStatusConstants.PREF_UPDATE_INTERVAL, 500);
		node.putBoolean(IHeapStatusConstants.PREF_SHOW_MAX, false);
		node.putBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION, false);
		
		IEclipsePreferences rootNode = (IEclipsePreferences) Platform
				.getPreferencesService().getRootNode()
				.node(InstanceScope.SCOPE);

		final String workbenchName = WorkbenchPlugin.getDefault().getBundle()
				.getSymbolicName();
		try {
			if (rootNode.nodeExists(workbenchName)) {
				((IEclipsePreferences) rootNode.node(workbenchName))
						.addPreferenceChangeListener(PlatformUIPreferenceListener
								.getSingleton());
			}
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin
					.getDefault().getBundle().getSymbolicName(), IStatus.ERROR,
					e.getLocalizedMessage(), e);
			WorkbenchPlugin.getDefault().getLog().log(status);
		}
	
	}

}
