package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
	 * @deprecated How a perspective is opened is dependent on user's current
	 * 		context instead of one global preference.  Callers should use
	 * 		IWorkbench.showPerspective methods.
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
	 * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
	 * OPEN_PERSPECTIVE_REPLACE and NO_NEW_PERSPECTIVE</code>.
	 * </p>
	 * @deprecated Opening a perspective on project creation is now handled
	 * 		by the New Project & Capabilities wizard.
	 */
	public static final String PROJECT_OPEN_NEW_PERSPECTIVE =
		"PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * A preference value indicating that an action should open a new 
	 * perspective in a new window.
	 * 
	 * @deprecated How a perspective is opened is dependent on user's current
	 * 		context instead of one global preference.  Callers should use
	 * 		IWorkbench.showPerspective methods.
	 */
	public static final String OPEN_PERSPECTIVE_WINDOW = "OPEN_PERSPECTIVE_WINDOW"; //$NON-NLS-1$
	
	/**
	 * A preference value indicating that an action should open a new 
	 * perspective in a new page.
	 * 
	 * @deprecated How a perspective is opened is dependent on user's current
	 * 		context instead of one global preference.  Callers should use
	 * 		IWorkbench.showPerspective methods.
	 */
	public static final String OPEN_PERSPECTIVE_PAGE = "OPEN_PERSPECTIVE_PAGE"; //$NON-NLS-1$
	
	/**
	 * A preference value indicating that an action should open a new 
	 * perspective by replacing the current perspective.
	 * 
	 * @deprecated How a perspective is opened is dependent on user's current
	 * 		context instead of one global preference.  Callers should use
	 * 		IWorkbench.showPerspective methods.
	 */
	public static final String OPEN_PERSPECTIVE_REPLACE =
		"OPEN_PERSPECTIVE_REPLACE"; //$NON-NLS-1$
		
	/**
	 * A preference value indicating that an action should not open a 
	 * new perspective.
	 * 
	 * @deprecated Opening a perspective on project creation is now handled
	 * 		by the New Project & Capabilities wizard.
	 */
	public static final String NO_NEW_PERSPECTIVE = "NO_NEW_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * A preference value indicating the default workbench perspective
	 */
	public static final String DEFAULT_PERSPECTIVE_ID = "defaultPerspectiveId"; //$NON-NLS-1$
}
