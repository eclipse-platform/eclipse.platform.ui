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
package org.eclipse.debug.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class DebugUIPreferenceInitializer extends AbstractPreferenceInitializer {

	public DebugUIPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
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
		
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR, new RGB(255, 0, 0));
		
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.MEMORY_VIEW_UNBUFFERED_LINE_COLOR, new RGB(114, 119, 129));
		
		//LaunchHistoryPreferencePage
		prefs.setDefault(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, 10);
		
		//VariableViewsPreferencePage
		prefs.setDefault(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CHANGED_VARIABLE_COLOR, new RGB(255, 0, 0));
		prefs.setDefault(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP, false);
		
		//Registers View
		prefs.setDefault(IDebugPreferenceConstants.REGISTERS_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		// Variable/Expression view default settings
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowDetailPaneAction", true); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false); //$NON-NLS-1$
		prefs.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + '+' + "org.eclipse.debug.ui.ShowTypeNamesAction", false);		 //$NON-NLS-1$
		
		// Step filter preferences
		prefs.setDefault(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS, false);
		
		// set default for column size preference
		prefs.setDefault(IDebugPreferenceConstants.PREF_COLUMN_SIZE, 
				IDebugPreferenceConstants.PREF_COLUMN_SIZE_DEFAULT);
	}
}