/*******************************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
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
 * @issue it's confusing to have both IWorkbenchPreferenceConstants and
 *   IWorkbenchPreferences (not to mention IPreferenceConstants)
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
	 * @deprecated whether a title is shown is controlled by the <code>SWT.TITLE</code> 
	 * shell style bit; see <code>IWorkbenchWindowConfigurer.setShellStyle(int)</code>
	 */
	public static String SHOULD_SHOW_TITLE_BAR = "SHOULD_SHOW_TITLE_BAR"; //$NON-NLS-1$
	
	/**
	 * Workbench preference id for whether workbench windows should have a menu
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a menu bar by default, and <code>false</code> if they
	 * should not have a menu bar by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has menu bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_MENU_BAR = "SHOULD_SHOW_MENU_BAR"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a cool
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a cool bar by default, and <code>false</code> if they
	 * should not have a cool bar by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has cool bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_COOL_BAR = "SHOULD_SHOW_COOL_BAR"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a status
	 * line by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a status line by default, and <code>false</code> if they
	 * should not have a status line by default.
	 * <p>
	 * The default value for this preference is: <code>true</code> (has status line)
	 * </p>
	 */
	public static String SHOULD_SHOW_STATUS_LINE = "SHOULD_SHOW_STATUS_LINE"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have fast view
	 * bars by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have fast view bars by default, and <code>false</code> if they
	 * should not have fast view bars by default.
	 * <p>
	 * The default value for this preference is: <code>false</code> (does not have fast view bars)
	 * </p>
	 */
	public static String SHOULD_SHOW_FAST_VIEW_BARS = "SHOULD_SHOW_FAST_VIEW_BARS"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a perspective
	 * bar by default. Boolean-valued: <code>true</code> if workbench windows
	 * should have a perspective bar by default, and <code>false</code> if they
	 * should not have a perspective bar by default.
	 * <p>
	 * The default value for this preference is: <code>false</code> (does not have perspective bar)
	 * </p>
	 */
	public static String SHOULD_SHOW_PERSPECTIVE_BAR = "SHOULD_SHOW_PERSPECTIVE_BAR"; //$NON-NLS-1$

	/**
	 * Workbench preference id for whether workbench windows should have a progress 
	 * indicator by default. Boolean-valued: <code>true</code> if workbench windows 
	 * should have a menu bar by default, and <code>false</code> if they
	 * should not have a menu bar by default.
	 * <p>
	 * The default value for this preference is: <code>false</code> (does not have progress indicator)
	 * </p>
	 */
	public static String SHOULD_SHOW_PROGRESS_INDICATOR = "SHOULD_SHOW_PROGRESS_INDICATOR"; //$NON-NLS-1$

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
	public static String SHOULD_SAVE_WORKBENCH_STATE = "SHOULD_SAVE_WORKBENCH_STATE"; //$NON-NLS-1$

    /**
     * Workbench preference id for whether the workbench should show the 
     * introduction component (if available) on startup.
     * 
	 * <p>
	 * The default value for this preference is: <code>true</code> (show intro)
	 * </p> 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#openIntro(IWorkbenchWindowConfigurer)
     */
    public static final String SHOULD_SHOW_INTRO = "SHOULD_SHOW_INTRO"; //$NON-NLS-1$
	
}
