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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.ui.application.IWorkbenchPreferences;

/**
 * Implementation of the workbench plugin's preferences extension's customization element.
 * This is needed in order to force the workbench plugin's preferences to be initialized
 * properly when running without org.eclipse.core.runtime.compatibility.
 * For more details, see bug 58975 - New preference mechanism does not properly initialize defaults.
 * 
 * @since 3.0
 */
public class WorkbenchPreferenceInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		
		JFacePreferences.setPreferenceStore(store);

		// new generic workbench preferences (for RCP APIs in org.eclipse.ui.application)
		store.setDefault(IWorkbenchPreferences.SHOULD_SAVE_WORKBENCH_STATE, false);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_TITLE_BAR, true);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_MENU_BAR, true);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_COOL_BAR, true);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_FAST_VIEW_BARS, false);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_PERSPECTIVE_BAR, false);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_STATUS_LINE, true);
		store.setDefault(IWorkbenchPreferences.SHOULD_SHOW_PROGRESS_INDICATOR, false);			

		// workbench preferences that are API (but non-RCP)
		// @issue these should probably be on org.eclipse.ui's preference store, 
		//    not org.eclipse.ui.workbench
		store.setDefault(IPreferenceConstants.CLOSE_EDITORS_ON_EXIT, false);		
		store.setDefault(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT, true);
		
		// @issue some of these may be IDE-specific
		store.setDefault(IPreferenceConstants.EDITORLIST_PULLDOWN_ACTIVE, false);
		store.setDefault(IPreferenceConstants.EDITORLIST_DISPLAY_FULL_NAME, false);
		store.setDefault(IPreferenceConstants.STICKY_CYCLE, false);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, false);
		store.setDefault(IPreferenceConstants.REUSE_DIRTY_EDITORS, true);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS, 8);
		store.setDefault(IPreferenceConstants.OPEN_ON_SINGLE_CLICK, false);
		store.setDefault(IPreferenceConstants.SELECT_ON_HOVER, false);
		store.setDefault(IPreferenceConstants.OPEN_AFTER_DELAY, false);
		store.setDefault(IPreferenceConstants.RECENT_FILES, 4);

		store.setDefault(IPreferenceConstants.VIEW_TAB_POSITION, SWT.TOP);
		store.setDefault(IPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);

		store.setDefault(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS, true);
		store.setDefault(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, true);
		store.setDefault(IPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, true);
		store.setDefault(IPreferenceConstants.DOCK_PERSPECTIVE_BAR, false);
		
		store.setDefault(IPreferenceConstants.EDITOR_TAB_WIDTH, 3); // high
		store.setDefault(IPreferenceConstants.OPEN_VIEW_MODE, IPreferenceConstants.OVM_EMBED);
		store.setDefault(IPreferenceConstants.OPEN_PERSP_MODE, IPreferenceConstants.OPM_ACTIVE_PAGE);
		store.setDefault(IPreferenceConstants.ENABLED_DECORATORS, ""); //$NON-NLS-1$
		store.setDefault(IPreferenceConstants.EDITORLIST_SELECTION_SCOPE, IPreferenceConstants.EDITORLIST_SET_PAGE_SCOPE); // Current Window
		store.setDefault(IPreferenceConstants.EDITORLIST_SORT_CRITERIA, IPreferenceConstants.EDITORLIST_NAME_SORT); // Name Sort
		store.setDefault(IPreferenceConstants.COLOR_ICONS, true);
		store.setDefault(IPreferenceConstants.SHOW_SHORTCUT_BAR, true);
		store.setDefault(IPreferenceConstants.SHOW_STATUS_LINE, true);
		store.setDefault(IPreferenceConstants.SHOW_TOOL_BAR, true);
		store.setDefault(IPreferenceConstants.MULTI_KEY_ASSIST, false);
		store.setDefault(IPreferenceConstants.MULTI_KEY_ASSIST_TIME, 1000);			
		
		//Option to show user jobs in a dialog
		store.setDefault(IPreferenceConstants.RUN_IN_BACKGROUND,false);
		
		// Temporary option to enable wizard for project capability
		store.setDefault("ENABLE_CONFIGURABLE_PROJECT_WIZARD", false); //$NON-NLS-1$
		// Temporary option to enable single click
		store.setDefault("SINGLE_CLICK_METHOD", OpenStrategy.DOUBLE_CLICK); //$NON-NLS-1$
		// Temporary option to enable cool bars
		store.setDefault("ENABLE_COOL_BARS", true); //$NON-NLS-1$
		// Temporary option to enable new menu organization
		store.setDefault("ENABLE_NEW_MENUS", true); //$NON-NLS-1$	
		//Temporary option to turn off the dialog font
		store.setDefault("DISABLE_DIALOG_FONT", false); //$NON-NLS-1$
		
		store.addPropertyChangeListener(new PlatformUIPreferenceListener());
    }

}
