/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.ui.console.IConsoleConstants;

/**
 * Help context ids for the console plugin.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IConsoleHelpContextIds {
	
	public static final String PREFIX = IConsoleConstants.PLUGIN_ID + "."; //$NON-NLS-1$
	
	// Actions
	public static final String CLEAR_CONSOLE_ACTION = PREFIX + "clear_console_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_SCROLL_LOCK_ACTION = PREFIX + "console_scroll_lock_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_SELECT_ALL_ACTION = PREFIX + "console_select_all_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_COPY_ACTION = PREFIX + "copy_to_clipboard_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_CUT_ACTION = PREFIX + "console_cut_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_PASTE_ACTION = PREFIX + "console_paste_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_FIND_REPLACE_ACTION = PREFIX + "console_find_replace_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_OPEN_LINK_ACTION = PREFIX + "console_open_link_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_OPEN_CONSOLE_ACTION = PREFIX + "console_open_console_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_DISPLAY_CONSOLE_ACTION = PREFIX + "console_display_console_action"; //$NON-NLS-1$
	public static final String CONSOLE_PIN_CONSOLE_ACITON = PREFIX + "console_pin_console_action"; //$NON-NLS-1$
	
	// Views
	public static final String CONSOLE_VIEW = PREFIX + "console_view_context"; //$NON-NLS-1$

	// Preference pages
	public static final String CONSOLE_PREFERENCE_PAGE = PREFIX + "console_preference_page_context"; //$NON-NLS-1$
}

