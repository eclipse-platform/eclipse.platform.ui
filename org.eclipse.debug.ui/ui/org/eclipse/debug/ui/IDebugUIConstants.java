package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Constant definitions for debug UI plug-in.
 * <p>
 * Popup menus in the debug UI support action contribution via the
 * <code>org.eclipse.ui.popupMenus</code>  extension. Actions may be
 * contributed to any group on the menu. To facilitate insertion of actions
 * inbetween existing groups, empty groups have been defined
 * in the menu. Each group prefixed by "empty" indicates an empty group.
 * </p>
 * <h3>Debug View Popup Menu</h3>
 * <ul>
 *   <li>Empty edit group</li>
 *   <li>Edit group</li>
 *   <li>Copy stack action</li>
 *   <li>Empty step group</li>
 *   <li>Step group</li>
 *   <li>Step into group</li>
 *   <li>Step into action</li>
 * 	 <li>Step over group</li>
 *   <li>Step over action</li>
 *   <li>Step return group</li> 
 *   <li>Step return action</li>
 *   <li>Empty thread group</li>
 *   <li>Thread group</li>
 *   <li>Resume action</li>
 *   <li>Suspend action</li>
 *   <li>Terminate action</li>
 *   <li>Disconnect action</li>
 *   <li>Empty launch group</li>
 *   <li>Launch group</li>
 *   <li>Remove all terminated action</li>
 *   <li>Terminate and Remove action</li>
 *   <li>Relaunch action</li>
 *   <li>Terminate all action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 *   <li>Property group</li>
 *   <li>Property dialog action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Variables View Popup Menus</h3>
 * <ul>
 *   <li>Empty variable group</li>
 *   <li>Variable group</li>
 *   <li>Select all action</li>
 *   <li>Copy to clipboard action</li>
 *   <li>Change value action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 *   <li>Show type names action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Breakpoints View Popup Menu</h3>
 * <ul>
 *   <li>Empty Navigation group</li>
 *   <li>Navigation group</li>
 *   <li>Open action</li>
 *   <li>Empty Breakpoint goup</li>
 *   <li>Breakpoint group</li>
 *   <li>Enable action</li> 
 *   <li>Disable action</li>
 *   <li>Remove action</li>
 *   <li>Remove all action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 * 	 <li>Show breakpoints for model action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Expressions View Popup Menu</h3>
 * <ul>
 *   <li>Empty Expression group</li>
 *   <li>Expression group</li>
 *   <li>Select all action</li>
 * 	 <li>Copy to clipboard action</li>	 
 *   <li>Remove action</li>
 *   <li>Remove all action</li>
 *   <li>Change variable value action</li>
 *   <li>Empty Render group</li>
 *   <li>Render group</li>
 * 	 <li>Show type names action</li>
 *   <li>Additions group</li>
 * </ul>
 * <p>
 * Constants only; not intended to be implemented or extended.
 * </p>
 */

public interface IDebugUIConstants {
	
	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.debug.ui"; //$NON-NLS-1$
	
	/**
	 * Debug perspective identifier (value <code>"org.eclipse.debug.ui.DebugPerspective"</code>).
	 */
	public static final String ID_DEBUG_PERSPECTIVE = PLUGIN_ID + ".DebugPerspective"; //$NON-NLS-1$
	
	/**
	 * Debug model presentation simple extension point identifier (value <code>"debugModelPresentations"</code>).
	 */
	public static final String ID_DEBUG_MODEL_PRESENTATION= "debugModelPresentations"; //$NON-NLS-1$
		
	/**
	 * Constant for referring to no perspective.
	 */
	public static final String PERSPECTIVE_NONE = "perspective_none"; //$NON-NLS-1$
	
	/**
	 * Constant for referring to a default perspective.
	 */
	public static final String PERSPECTIVE_DEFAULT = "perspective_default"; //$NON-NLS-1$

	// Preferences

	/**
	 * String preference that identifies the default 'switch to perspective id' when running a 
	 * launch configuration.  This default is used if a particular launch configuration does not
	 * override the 'switch to perspective when in run mode' attribute with something else.
	 */
	public static final String PREF_SHOW_RUN_PERSPECTIVE_DEFAULT= PLUGIN_ID + ".show_run_perspective_default";  //$NON-NLS-1$
	
	/**
	 * String preference that identifies the default 'switch to perspective id' when debugging a 
	 * launch configuration.  This default is used if a particular launch configuration does not
	 * override the 'switch to perspective when in debug mode' attribute with something else.
	 */
	public static final String PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT= PLUGIN_ID + ".show_debug_perspective_default";  //$NON-NLS-1$
	
	/**
	 * Boolean preference controlling whether a build is done before
	 * launching a program (if one is needed).
	 */
	public static final String PREF_BUILD_BEFORE_LAUNCH= PLUGIN_ID + ".build_before_launch"; //$NON-NLS-1$
	/**
	 * Identifier for the radio button group that consists of the following three preferences.
	 */
	public static final String PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO= PLUGIN_ID + ".save_dirty_editors_before_launch_radio"; //$NON-NLS-1$
	/**
	 * Radio button preference specifying that dirty editors should be autosaved (no prompting)
	 * before launching.
	 */
	public static final String PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH= PLUGIN_ID + ".auto_save_dirty_editors_before_launch"; //$NON-NLS-1$
	/**
	 * Radio button preference specifying that dirty editors should cause a prompt to appear to the
	 * user asking if they wish to save.
	 */
	public static final String PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH= PLUGIN_ID + ".prompt_save_dirty_editors_before_launch"; //$NON-NLS-1$
	/**
	 * Radio button preference specifyig that dirty editors should never be saved before launching.
	 */
	public static final String PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH= PLUGIN_ID + ".never_save_dirty_editors_before_launch"; //$NON-NLS-1$
	/**
	 * Boolean preference controlling automatic removal of terminated launches
	 * when a new launch is registered.
	 * @since 2.0
	 */
	public static final String PREF_AUTO_REMOVE_OLD_LAUNCHES= PLUGIN_ID + ".auto_remove_old_launches"; //$NON-NLS-1$
		
	/**
	 * Boolean preference controlling whether the debugger re-uses non-dirty editors
	 * that it opens when displaying source. When <code>true</code> the debugger
	 * re-uses the same editor when showing source for a selected stack frame (unless
	 * the editor is dirty).
	 * 
	 * @since 2.0
	 */
	public static final String PREF_REUSE_EDITOR = PLUGIN_ID + ".reuse_editor"; //$NON-NLS-1$
	
	/**
	 * Boolean preference controlling whether the debugger shows types names
	 * in its variable view. When <code>true</code> the debugger
	 * will display type names in new variable views.
	 * 
	 * @since 2.0
	 */
	public static final String PREF_SHOW_TYPE_NAMES = PLUGIN_ID + ".show_type_names"; //$NON-NLS-1$	
	
	/**
	 * Boolean preference controlling whether the debugger shows the detail pane
	 * in its variable view. When <code>true</code> the debugger
	 * will show the detail panes in new variable views.
	 * 
	 * @since 2.0
	 */
	public static final String PREF_SHOW_DETAIL_PANE = PLUGIN_ID + ".show_detail_pane"; //$NON-NLS-1$	
		
	// Debug views
	
	/**
	 * Debug view identifier (value <code>"org.eclipse.debug.ui.DebugView"</code>).
	 */
	public static final String ID_DEBUG_VIEW= "org.eclipse.debug.ui.DebugView"; //$NON-NLS-1$
	
	/**
	 * Breakpoint view identifier (value <code>"org.eclipse.debug.ui.BreakpointView"</code>).
	 */
	public static final String ID_BREAKPOINT_VIEW= "org.eclipse.debug.ui.BreakpointView"; //$NON-NLS-1$
	
	/**
	 * Variable view identifier (value <code>"org.eclipse.debug.ui.VariableView"</code>).
	 */
	public static final String ID_VARIABLE_VIEW= "org.eclipse.debug.ui.VariableView"; //$NON-NLS-1$
	
	/**
	 * Expression view identifier (value <code>"org.eclipse.debug.ui.ExpressionView"</code>).
	 * @since 2.0
	 */
	public static final String ID_EXPRESSION_VIEW= "org.eclipse.debug.ui.ExpressionView"; //$NON-NLS-1$
		
	/**
	 * Console view identifier (value <code>"org.eclipse.debug.ui.ConsoleView"</code>).
	 */
	public static final String ID_CONSOLE_VIEW= "org.eclipse.debug.ui.ConsoleView"; //$NON-NLS-1$

	// Extension points
	
	/**
	 * Extension point for launch configuration type images.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPE_IMAGES = "launchConfigurationTypeImages"; //$NON-NLS-1$
	
	// Debug Action images
	
	/**
	 * Debug action image identifier.
	 */
	public static final String IMG_ACT_DEBUG= "IMG_ACT_DEBUG"; //$NON-NLS-1$

	/**
	 * Run action image identifier.
	 */
	public static final String IMG_ACT_RUN= "IMG_ACT_RUN"; //$NON-NLS-1$
		
	/** Resume action image identifier. */
	public static final String IMG_LCL_RESUME= "IMG_LCL_RESUME"; //$NON-NLS-1$
	
	/** Suspend action image identifier. */
	public static final String IMG_LCL_SUSPEND= "IMG_LCL_SUSPEND"; //$NON-NLS-1$
	
	/** Terminate action image identifier. */
	public static final String IMG_LCL_TERMINATE= "IMG_LCL_TERMINATE"; //$NON-NLS-1$
	
	/** Terminate all action image identifier. */
	public static final String IMG_LCL_TERMINATE_ALL= "IMG_LCL_TERMINATE_ALL"; //$NON-NLS-1$
	
	/** Terminate and remove action image identifier. */
	public static final String IMG_LCL_TERMINATE_AND_REMOVE= "IMG_LCL_TERMINATE_AND_REMOVE"; //$NON-NLS-1$
	
	/** Disconnect action image identifier. */
	public static final String IMG_LCL_DISCONNECT= "IMG_LCL_DISCONNECT"; //$NON-NLS-1$
	
	/** Step into action image identifier. */
	public static final String IMG_LCL_STEPINTO= "IMG_LCL_STEPINTO"; //$NON-NLS-1$
	
	/** Step over action image identifier. */
	public static final String IMG_LCL_STEPOVER= "IMG_LCL_STEPOVER"; //$NON-NLS-1$
	
	/** Step return action image identifier. */
	public static final String IMG_LCL_STEPRETURN= "IMG_LCL_STEPRETURN"; //$NON-NLS-1$
	
	/** Clear action image identifier. */
	public static final String IMG_LCL_CLEAR= "IMG_LCL_CLEAR"; //$NON-NLS-1$
	
	/** Remove all terminated action image identifier. */
	public static final String IMG_LCL_REMOVE_TERMINATED= "IMG_LCL_REMOVE_TERMINATED"; //$NON-NLS-1$
	
	/** Display variable type names action image identifier. */
	public static final String IMG_LCL_TYPE_NAMES= "IMG_LCL_TYPE_NAMES"; //$NON-NLS-1$
	
	/** Remove action image identifier. */
	public static final String IMG_LCL_REMOVE= "IMG_LCL_REMOVE"; //$NON-NLS-1$
	
	/** Remove all action image identifier. */
	public static final String IMG_LCL_REMOVE_ALL= "IMG_LCL_REMOVE_ALL"; //$NON-NLS-1$
	
	/** Re-launch action image identifier.*/
	public static final String IMG_LCL_RELAUNCH= "IMG_LCL_RELAUNCH"; //$NON-NLS-1$

	/** Copy-to-clipboard action image identifier.*/
	public static final String IMG_LCL_COPY= "IMG_LCL_COPY"; //$NON-NLS-1$

	
	// Debug element images
	
	/** Debug mode launch image identifier. */
	public static final String IMG_OBJS_LAUNCH_DEBUG= "IMG_OBJS_LAUNCH_DEBUG"; //$NON-NLS-1$
	
	/** Run mode launch image identifier. */
	public static final String IMG_OBJS_LAUNCH_RUN= "IMG_OBJS_LAUNCH_RUN"; //$NON-NLS-1$
	
	/** Running debug target image identifier. */
	public static final String IMG_OBJS_DEBUG_TARGET= "IMG_OBJS_DEBUG_TARGET"; //$NON-NLS-1$
	
	/** Terminated debug target image identifier. */
	public static final String IMG_OBJS_DEBUG_TARGET_TERMINATED= "IMG_OBJS_DEBUG_TARGET_TERMINATED"; //$NON-NLS-1$
	
	/** Running thread image identifier. */
	public static final String IMG_OBJS_THREAD_RUNNING= "IMG_OBJS_THREAD_RUNNING"; //$NON-NLS-1$
	
	/** Suspended thread image identifier. */
	public static final String IMG_OBJS_THREAD_SUSPENDED= "IMG_OBJS_THREAD_SUSPENDED"; //$NON-NLS-1$
	
	/** Terminated thread image identifier. */
	public static final String IMG_OBJS_THREAD_TERMINATED= "IMG_OBJS_THREAD_TERMINATED"; //$NON-NLS-1$
	
	/** Stack frame (suspended) image identifier. */
	public static final String IMG_OBJS_STACKFRAME= "IMG_OBJS_STACKFRAME"; //$NON-NLS-1$
	
	/** Stack frame (running) image identifier. */
	public static final String IMG_OBJS_STACKFRAME_RUNNING= "IMG_OBJS_STACKFRAME_RUNNING"; //$NON-NLS-1$
	
	/** Enabled breakpoint image identifier. */
	public static final String IMG_OBJS_BREAKPOINT= "IMG_OBJS_BREAKPOINT"; //$NON-NLS-1$
	
	/** Disabled breakpoint image identifier. */
	public static final String IMG_OBJS_BREAKPOINT_DISABLED= "IMG_OBJS_BREAKPOINT_DISABLED"; //$NON-NLS-1$
		
	/** Running system process image identifier. */
	public static final String IMG_OBJS_OS_PROCESS= "IMG_OBJS_OS_PROCESS"; //$NON-NLS-1$
	
	/** Terminated system process image identifier. */
	public static final String IMG_OBJS_OS_PROCESS_TERMINATED= "IMG_OBJS_OS_PROCESS_TERMINATED"; //$NON-NLS-1$

	/**
	 * Expression image identifier.
	 * 
	 * @since 2.0
	 */
	public static final String IMG_OBJS_EXPRESSION= "IMG_OBJS_EXPRESSION"; //$NON-NLS-1$
		
	// wizard banners
	/** Debug wizard banner image identifier. */
	public static final String IMG_WIZBAN_DEBUG= "IMG_WIZBAN_DEBUG"; //$NON-NLS-1$
	
	/** Run wizard banner image identifier. */
	public static final String IMG_WIZBAN_RUN= "IMG_WIZBAN_RUN"; //$NON-NLS-1$
	
	/**
	 * Debug action set identifier (value <code>"org.eclipse.debug.ui.debugActionSet"</code>).
	 */
	public static final String DEBUG_ACTION_SET= PLUGIN_ID + ".debugActionSet"; //$NON-NLS-1$
	
	/**
	 * Launch action set identifier (value <code>"org.eclipse.debug.ui.LaunchActionSet"</code>).
	 */
	public static final String LAUNCH_ACTION_SET= PLUGIN_ID + ".launchActionSet"; //$NON-NLS-1$
	
	// menus 
	
	/** 
	 * Identifier for an empty group preceeding an
	 * edit group in a menu (value <code>"emptyEditGroup"</code>).
	 */
	public static final String EMPTY_EDIT_GROUP = "emptyEditGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for an edit group in a menu (value <code>"editGroup"</code>).
	 */
	public static final String EDIT_GROUP = "editGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * step group in a menu (value <code>"emptyStepGroup"</code>).
	 */
	public static final String EMPTY_STEP_GROUP = "emptyStepGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a step group in a menu or toolbar (value <code>"stepGroup"</code>).
	 */
	public static final String STEP_GROUP = "stepGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a step into group in a menu or toolbar (value <code>"stepIntoGroup"</code>).
	 */
	public static final String STEP_INTO_GROUP = "stepIntoGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a step over group in a menu or toolbar (value <code>"stepOverGroup"</code>).
	 */
	public static final String STEP_OVER_GROUP = "stepOverGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a step return group in a menu or toolbar (value <code>"stepReturnGroup"</code>).
	 */
	public static final String STEP_RETURN_GROUP = "stepReturnGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * thread group in a menu (value <code>"emptyThreadGroup"</code>).
	 */
	public static final String EMPTY_THREAD_GROUP = "emptyThreadGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a thread group in a menu or toolbar(value <code>"threadGroup"</code>).
	 */
	public static final String THREAD_GROUP = "threadGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * launch group in a menu (value <code>"emptyLaunchGroup"</code>).
	 */
	public static final String EMPTY_LAUNCH_GROUP = "emptyLaunchGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a launch group in a menu (value <code>"launchGroup"</code>).
	 */
	public static final String LAUNCH_GROUP = "launchGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * variable group in a menu (value <code>"emptyVariableGroup"</code>).
	 */
	public static final String EMPTY_VARIABLE_GROUP = "emptyVariableGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a variable group in a menu (value <code>"variableGroup"</code>).
	 */
	public static final String VARIABLE_GROUP = "variableGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * navigation group in a menu (value <code>"emptyNavigationGroup"</code>).
	 */
	public static final String EMPTY_NAVIGATION_GROUP = "emptyNavigationGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a navigation group in a menu (value <code>"navigationGroup"</code>).
	 */
	public static final String NAVIGATION_GROUP = "navigationGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding a
	 * breakpoint group in a menu (value <code>"emptyBreakpointGroup"</code>).
	 */
	public static final String EMPTY_BREAKPOINT_GROUP = "emptyBreakpointGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a breakpoint group in a menu (value <code>"breakpointGroup"</code>).
	 */
	public static final String BREAKPOINT_GROUP = "breakpointGroup"; //$NON-NLS-1$
	
	/** 
	 * Identifier for an empty group preceeding an
	 * expression group in a menu (value <code>"emptyExpressionGroup"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String EMPTY_EXPRESSION_GROUP = "emptyExpressionGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for an expression group in a menu (value <code>"expressionGroup"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String EXPRESSION_GROUP = "expressionGroup"; //$NON-NLS-1$
	/** 
	 * Identifier for an empty group preceeding a
	 * render group in a menu (value <code>"emptyRenderGroup"</code>).
	 */
	public static final String EMPTY_RENDER_GROUP = "emptyRenderGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a render group in a menu or toolbar(value <code>"renderGroup"</code>).
	 */
	public static final String RENDER_GROUP = "renderGroup"; //$NON-NLS-1$
	
	/**
	 * Identifier for a property group in a menu (value <code>"propertyGroup"</code>).
	 */
	public static final String PROPERTY_GROUP = "propertyGroup"; //$NON-NLS-1$
	
	/**
	 * Id for the popup menu associated with the variables (tree viewer) part of the VariableView
	 */
	public static final String VARIABLE_VIEW_VARIABLE_ID = "org.eclipse.debug.ui.VariableView.variables"; //$NON-NLS-1$
	
	/**
	 * Id for the popup menu associated with the detail (text viewer) part of the VariableView
	 */
	public static final String VARIABLE_VIEW_DETAIL_ID = "org.eclipse.debug.ui.VariableView.detail"; //$NON-NLS-1$
	
	// status codes
	/**
	 * Status indicating an invalid extension definition.
	 */
	public static final int STATUS_INVALID_EXTENSION_DEFINITION = 100;
	
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;		
	
	// launch configuration attribute keys
	/**
	 * Launch configuartion attribute - the perspective to
	 * switch to when a launch configuration is launched in
	 * run mode (value <code>org.eclipse.debug.ui.target_run_perspective</code>).
	 * Value is a string corresponding to a perspective identifier,
	 * or <code>null</code> indicating no perspective change.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_TARGET_RUN_PERSPECTIVE = PLUGIN_ID + ".target_run_perspective";	 //$NON-NLS-1$
	
	/**
	 * Launch configuartion attribute - the perspective to
	 * switch to when a launch configuration is launched in
	 * debug mode (value <code>org.eclipse.debug.ui.target_run_perspective</code>).
	 * Value is a string corresponding to a perspective identifier,
	 * or <code>null</code> indicating no perspective change.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_TARGET_DEBUG_PERSPECTIVE = PLUGIN_ID + ".target_debug_perspective";		 //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute - the container where the configuration file
	 * is stored.  The container is set via the 'setContainer()' call on ILaunchConfigurationWorkingCopy.
	 * This constant is only needed for persisting and reading the default value of the
	 * container value for individual resources.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_CONTAINER = PLUGIN_ID + ".container"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute - a boolean value that indicates if the launch configuration
	 * is 'private'.  A private configuration is one that does not appear to the user in the launch
	 * history or the launch configuration dialog.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_PRIVATE = PLUGIN_ID + ".private"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute - a boolean value that indicates if the launch configuration
	 * is displayed in the debug favorites menu. Default value is
	 * <code>false</code> if absent.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_DEBUG_FAVORITE = PLUGIN_ID + ".debugFavorite"; //$NON-NLS-1$	
	
	/**
	 * Launch configuration attribute - a boolean value that indicates if the launch configuration
	 * is displayed in the run favorites menu.Default value is
	 * <code>false</code> if absent.
	 * 
	 * @since 2.0
	 */
	public static final String ATTR_RUN_FAVORITE = PLUGIN_ID + ".runFavorite"; //$NON-NLS-1$		
	
	/**
	 * Debug action groups extension point identifier
	 * (value <code>"debugActionGroups"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_DEBUG_ACTION_GROUPS= "debugActionGroups";	 //$NON-NLS-1$
	
	/**
	 * Launch configuration tab groups extension point identifier
	 * (value <code>"launchConfigurationTabGroups"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS= "launchConfigurationTabGroups";	 //$NON-NLS-1$	

	/**
	 * Launch configuration shortcuts extension point identifier
	 * (value <code>"launchConfigurationShortcuts"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_LAUNCH_CONFIGURATION_SHORTCUTS= "launchConfigurationShortcuts";	 //$NON-NLS-1$	
}