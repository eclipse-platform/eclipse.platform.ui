/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

/**
 * Defines names of workbench preferences that are available for the
 * application to configure. These preferences are not intended to be used by 
 * other plug-ins.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.0
 */
public interface IWorkbenchPreferences {
	
	/**
	 * Workbench preference id for whether workbench windows should have a window
	 * title by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a window title by default, and <code>false</code> if they
	 * should not have a window title by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has window title)
	 * </p>
	 */
	public static String SHOULD_SHOW_TITLE_BAR = "wb.show.title.bar"; //$NON-NLS-1$
	
	/**
	 * Workbench preference id for whether workbench windows should have a menu
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a menu bar by default, and <code>false</code> if they
	 * should not have a menu bar by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has menu bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_MENU_BAR = "wb.show.menu.bar"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a tool
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a tool bar by default, and <code>false</code> if they
	 * should not have a tool bar by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has tool bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_TOOL_BAR = "wb.show.tool.bar"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a shortcut
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a shortcut bar by default, and <code>false</code> if they
	 * should not have a shortcut bar by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has shortcut bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_SHORTCUT_BAR = "wb.show.shortcut.bar"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a status
	 * line by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a status line by default, and <code>false</code> if they
	 * should not have a status line by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has status line)
	 * </p>
	 */
	public static String SHOULD_SHOW_STATUS_LINE = "wb.show.status.line"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether the workbench should attempt to close
	 * all open editors when the workbench closes. 
	 * Boolean-valued: <code>true</code> if editors should be closed, and 
	 * <code>false</code> if editors should simply be discarded
	 * <p>
	 * The default value for this preference is: <code>false</code> (discard editors)
	 * </p>
	 */
	public static String SHOULD_CLOSE_EDITORS_ON_EXIT = "wb.code.editors.on.exit"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether the workbench should save the state
	 * of the workbench when it closes, and restore that state when it next
	 * re-opens. Boolean-valued: <code>true</code> if workbench state should be
	 * saved and restored, and <code>false</code> if workbench state should
	 * simply be discarded on exit, and recreated from scratch on reopen.
	 * <p>
	 * The default value for this preference is: <code>false</code> (discard
	 * workbench state)
	 * </p>
	 */
	public static String SHOULD_SAVE_WORKBENCH_STATE = "wb.save.workbench.state"; //$NON-NLS-1$
}
