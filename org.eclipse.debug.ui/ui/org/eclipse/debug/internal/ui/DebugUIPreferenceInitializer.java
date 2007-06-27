/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class DebugUIPreferenceInitializer extends AbstractPreferenceInitializer {

	public DebugUIPreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = DebugUIPlugin.getDefault().getPreferenceStore();
//		Debug PreferencePage
		prefs.setDefault(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IDebugUIConstants.PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT, IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		prefs.setDefault(IDebugUIConstants.PREF_SHOW_RUN_PERSPECTIVE_DEFAULT, IDebugUIConstants.PERSPECTIVE_NONE);
		prefs.setDefault(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, true);
		prefs.setDefault(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.NEVER);
		prefs.setDefault(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.ALWAYS);
		prefs.setDefault(IDebugUIConstants.PREF_REUSE_EDITOR, true);
		prefs.setDefault(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, false);
		prefs.setDefault(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.NEVER);
		prefs.setDefault(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.PROMPT);
		prefs.setDefault(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_ALL_BREAKPOINTS, true);
		prefs.setDefault(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_BREAKPOINTS_FROM_CONTAINER, true);
		
		/**
		 * Context launching preferences. Appear on the the Launching preference page
		 * 
		 * @since 3.3.0
		 */
		prefs.setDefault(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_LAUNCH_PARENT_PROJECT, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE, false);
		
		//View Management preference page
		prefs.setDefault(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		prefs.setDefault(IInternalDebugUIConstants.PREF_TRACK_VIEWS, true);
		
		//ConsolePreferencePage
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_WRAP, false);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_WIDTH, 80);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK, 80000);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK, 100000);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH, 8);
		
		// colors
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR, new RGB(255, 0, 0));
		// can be called in non-UI thread, so we must play safe
		Display display = DebugUIPlugin.getStandardDisplay();
		if (Thread.currentThread().equals(display.getThread())) {
			PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR, display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB());
		} else {
			display.asyncExec(new Runnable() {
				public void run() {
					PreferenceConverter.setDefault(
						DebugUIPlugin.getDefault().getPreferenceStore(),
						IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR,
						DebugUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB());
				}
			});
		}
		
		PreferenceConverter.setDefault(prefs, IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND, new RGB(255, 255, 0));
		PreferenceConverter.setDefault(prefs, IDebugUIConstants.PREF_MEMORY_HISTORY_UNKNOWN_COLOR, new RGB(114, 119, 129));
		PreferenceConverter.setDefault(prefs, IDebugUIConstants.PREF_MEMORY_HISTORY_KNOWN_COLOR, new RGB(0, 0, 0));

		//LaunchHistoryPreferencePage
		prefs.setDefault(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, 10);
		
		//VariableViewsPreferencePage
		prefs.setDefault(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		PreferenceConverter.setDefault(prefs, IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR, new RGB(255, 0, 0));
		prefs.setDefault(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP, false);
		prefs.setDefault(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH, 10000);
		
		//Registers View
		prefs.setDefault(IDebugPreferenceConstants.REGISTERS_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		// Variable/Expression view default settings
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false);		 //$NON-NLS-1$
		
		// set default for column size preference
		prefs.setDefault(IDebugPreferenceConstants.PREF_COLUMN_SIZE, 
				IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT);
		
		// set default for row size preference
		prefs.setDefault(IDebugPreferenceConstants.PREF_ROW_SIZE, 
				IDebugPreferenceConstants.PREF_ROW_SIZE_DEFAULT);
		
		// set default padded string
		prefs.setDefault(IDebugUIConstants.PREF_PADDED_STR, 
				IDebugPreferenceConstants.PREF_PADDED_STR_DEFAULT);
		
		// set default code page for ascii and ebcdic 
		prefs.setDefault(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE, 
				IDebugPreferenceConstants.DEFAULT_ASCII_CP);
		prefs.setDefault(IDebugUIConstants.PREF_DEFAULT_EBCDIC_CODE_PAGE, 
				IDebugPreferenceConstants.DEFAULT_EBCDIC_CP);
		
		if (MemoryViewUtil.isLinuxGTK()) {
			prefs.setDefault(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM, false);
		}
		else {
			prefs.setDefault(IDebugPreferenceConstants.PREF_DYNAMIC_LOAD_MEM, true);
		}
		
		prefs.setDefault(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PAGE_SIZE, IDebugPreferenceConstants.DEFAULT_PAGE_SIZE);
		prefs.setDefault(IDebugPreferenceConstants.PREF_RESET_MEMORY_BLOCK, IDebugPreferenceConstants.RESET_VISIBLE);
		prefs.setDefault(IDebugPreferenceConstants.PREF_TABLE_RENDERING_PRE_BUFFER_SIZE,IDebugPreferenceConstants.DEFAULT_PAGE_SIZE);
		prefs.setDefault(IDebugPreferenceConstants.PREF_TABLE_RENDERING_POST_BUFFER_SIZE,IDebugPreferenceConstants.DEFAULT_PAGE_SIZE);
		
		/**
		 * new launch configuration filtering options
		 * @since 3.2
		 */
		prefs.setDefault(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_FILTER_WORKING_SETS, true);
		prefs.setDefault(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES, false);
	}
}
