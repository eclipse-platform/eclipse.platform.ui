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
package org.eclipse.ui;


/**
 * Preference ids exposed by the Eclipse Platform User Interface.
 */
public interface IWorkbenchPreferenceConstants {
	
	/**
	 * A named preference for whether to show an editor when its
	 * input file is selected in the Navigator (and vice versa).
	 * <p>
	 * Value is of type <code>boolean</code>.
	 * </p>
	 */
	public static final String LINK_NAVIGATOR_TO_EDITOR =
		"LINK_NAVIGATOR_TO_EDITOR"; //$NON-NLS-1$

	/**
	 * A named preference for how a new perspective is opened.
	 * <p>
	 * Value is of type <code>String</code>.  The possible values are defined 
	 * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
	 * OPEN_PERSPECTIVE_REPLACE</code>.
	 * </p>
	 * 
	 * @see #OPEN_PERSPECTIVE_WINDOW
	 * @see #OPEN_PERSPECTIVE_PAGE
	 * @see #OPEN_PERSPECTIVE_REPLACE
	 * @see #NO_NEW_PERSPECTIVE
	 */
	public static final String OPEN_NEW_PERSPECTIVE = "OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$
	
	/**
	 * A named preference for how a new perspective is opened
	 * when the alternate key modifiers are pressed.  The alternate key modifiers
	 * are platform dependent.
	 * <p>
	 * Value is of type <code>String</code>.  The possible values are defined 
	 * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
	 * OPEN_PERSPECTIVE_REPLACE</code>.
	 * </p>
	 * @deprecated Workbench no longer supports alternate key modifier to open
	 * 		a new perspective. Callers should use IWorkbench.showPerspective methods.
	 */
	public static final String ALTERNATE_OPEN_NEW_PERSPECTIVE =
		"ALTERNATE_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$
		
	/**
	 * A named preference for how a new perspective is opened
	 * when the shift key modifier is pressed.  
	 * <p>
	 * Value is of type <code>String</code>.  The possible values are defined 
	 * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
	 * OPEN_PERSPECTIVE_REPLACE</code>.
	 * </p>
	 * 
	 * @deprecated Workbench no longer supports shift key modifier to open
	 * 		a new perspective. Callers should use IWorkbench.showPerspective methods.
	 */
	public static final String SHIFT_OPEN_NEW_PERSPECTIVE =
		"SHIFT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * A named preference for how a new perspective should be opened
	 * when a new project is created.
	 * <p>
	 * Value is of type <code>String</code>.  The possible values are defined 
	 * by the constants <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
	 * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
	 * </p>
	 * 
	 * @see #OPEN_PERSPECTIVE_WINDOW
	 * @see #OPEN_PERSPECTIVE_PAGE
	 * @see #OPEN_PERSPECTIVE_REPLACE
	 * @see #NO_NEW_PERSPECTIVE
	 * @deprecated in 3.0. This preference is IDE-specific, and is therefore found
	 * only in IDE configurations. IDE-specific tools should use 
	 * <code>org.eclipse.ui.ide.IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE</code>
	 * instead.
	 */
	public static final String PROJECT_OPEN_NEW_PERSPECTIVE =
		"PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * A preference value indicating that an action should open a new 
	 * perspective in a new window.
	 * 
	 * @see #PROJECT_OPEN_NEW_PERSPECTIVE
	 */
	public static final String OPEN_PERSPECTIVE_WINDOW = "OPEN_PERSPECTIVE_WINDOW"; //$NON-NLS-1$
	
	/**
	 * A preference value indicating that an action should open a new 
	 * perspective in a new page.
	 * 
	 * @see #PROJECT_OPEN_NEW_PERSPECTIVE
	 * @deprecated Opening a Perspective in a new page is no longer
	 * supported functionality as of 2.0.
	 */
	public static final String OPEN_PERSPECTIVE_PAGE = "OPEN_PERSPECTIVE_PAGE"; //$NON-NLS-1$
	
	/**
	 * A preference value indicating that an action should open a new 
	 * perspective by replacing the current perspective.
	 * 
	 * @see #PROJECT_OPEN_NEW_PERSPECTIVE
	 */
	public static final String OPEN_PERSPECTIVE_REPLACE =
		"OPEN_PERSPECTIVE_REPLACE"; //$NON-NLS-1$
		
	/**
	 * A preference value indicating that an action should not open a 
	 * new perspective.
	 * 
	 * @see #PROJECT_OPEN_NEW_PERSPECTIVE
	 */
	public static final String NO_NEW_PERSPECTIVE = "NO_NEW_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * A named preference indicating the default workbench perspective.
	 */
	public static final String DEFAULT_PERSPECTIVE_ID = "defaultPerspectiveId"; //$NON-NLS-1$
	
	/**
	 * A named preference indicating the presentation factory
	 * to use for the workbench look and feel.
	 * 
	 * @since 3.0
	 */
	public static final String PRESENTATION_FACTORY_ID = "presentationFactoryId"; //$NON-NLS-1$

	/**
	 * A named preference indicating where the fast view bar should be docked in a
	 * fresh workspace.  This preference is meaningless after a workspace has been
	 * setup, since the fast view bar state is then persisted in the workbench.  This
	 * preference is intended for applications that want the initial docking location
	 * to be somewhere specific.  The default value (when this preference is not set)
	 * is the bottom.
	 * 
	 * @since 3.0
	 */ 
	public static final String INITIAL_FAST_VIEW_BAR_LOCATION = "initialFastViewBarLocation"; //$NON-NLS-1$

	/**
	 * Setting the INITIAL_FAST_VIEW_BAR_LOCATION preference to this value will cause
	 * the fast view bar to be docked on the bottom of a fresh workspace.
	 * 
	 * @see #INITIAL_FAST_VIEW_BAR_LOCATION
	 * @since 3.0
	 */
	public static final String BOTTOM = "bottom"; //$NON-NLS-1$

	/**
	 * Setting the INITIAL_FAST_VIEW_BAR_LOCATION preference to this value will cause
	 * the fast view bar to be docked on the left of a fresh workspace.
	 * 
	 * @see #INITIAL_FAST_VIEW_BAR_LOCATION
	 * @since 3.0
	 */
	public static final String LEFT = "left"; //$NON-NLS-1$

	/**
	 * Setting the INITIAL_FAST_VIEW_BAR_LOCATION preference to this value will cause
	 * the fast view bar to be docked on the right of a fresh workspace.
	 * @see #INITIAL_FAST_VIEW_BAR_LOCATION
	 * 
	 * @since 3.0
	 */
	public static final String RIGHT = "right"; //$NON-NLS-1$
	
    /**
     * A named preference indicating whether the workbench should show the 
     * introduction component (if available) on startup.
     * 
	 * <p>
	 * The default value for this preference is: <code>true</code> (show intro)
	 * </p> 
	 * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#openIntro(IWorkbenchWindowConfigurer)
     * @since 3.0
     */
    public static final String SHOW_INTRO = "showIntro"; //$NON-NLS-1$
	
}
