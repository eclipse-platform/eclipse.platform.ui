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
 *   <li>Copy Stack action</li>
 *   <li>Empty Step group</li>
 *   <li>Step group</li>
 *   <li>Step Into action</li>
 *   <li>Step Over action</li>
 *   <li>Run to return action</li>
 *   <li>Empty thread group</li>
 *   <li>Thread group</li>
 *   <li>Suspend action</li>
 *   <li>Resume action</li>
 *   <li>Terminate action</li>
 *   <li>Empty launch group</li>
 *   <li>Launch group</li>
 *   <li>Terminate and Remove action</li>
 *   <li>Terminate All action</li>
 *   <li>Remove All Terminated action</li>
 *   <li>Relaunch action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 *   <li>Show qualified names action</li>
 *   <li>Property group</li>
 *   <li>Property dialog action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Process View Popup Menu</h3>
 * <ul>
 *   <li>Empty launch group</li>
 *   <li>Launch group</li>
 *   <li>Relaunch action</li>
 *   <li>Terminate action</li>
 *   <li>Terminate and Remove action</li>
 *   <li>Terminate All action</li>
 *   <li>Remove All Terminated action</li>
 *   <li>Property group</li>
 *   <li>Property dialog action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Variable View Popup Menu</h3>
 * <ul>
 *   <li>Empty variable group</li>
 *   <li>Variable group</li>
 *   <li>Add to Watch List action</li>
 *   <li>Change value action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 *   <li>Show qualified names action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Breakpoint View Popup Menu</h3>
 * <ul>
 *   <li>Empty Navigation group</li>
 *   <li>Navigation group</li>
 *   <li>Open action</li>
 *   <li>Empty Breakpoint goup</li>
 *   <li>Breakpoint group</li>
 *   <li>Enable/Disable action</li> 
 *   <li>Remove action</li>
 *   <li>Remove all action</li>
 *   <li>Empty render group</li>
 *   <li>Render group</li>
 *   <li>Show qualified names action</li>
 *   <li>Additions group</li>
 * </ul>
 * <h3>Inspector View Popup Menu</h3>
 * <ul>
 *   <li>Empty Expression group</li>
 *   <li>Expression group</li>
 *   <li>Remove action</li>
 *   <li>Remove all action</li>
 *   <li>Empty Render group</li>
 *   <li>Render group</li>
 *   <li>Show qualified names action</li>
 *   <li>Additions group</li>
 * </ul>
 * <p>
 * Constants only; not intended to be implemented or extended.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IDebugUIConstants {
	
	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.debug.ui";
	
	/**
	 * Debug perspective identifier (value <code>"org.eclipse.debug.ui.DebugPerspective"</code>).
	 */
	public static final String ID_DEBUG_PERSPECTIVE = PLUGIN_ID + ".DebugPerspective";
	
	/**
	 * Debug model presentation extension point identifier (value <code>"debugModelPresentations"</code>).
	 */
	public static final String ID_DEBUG_MODEL_PRESENTATION= "debugModelPresentations";
	
	// Preferences
	/**
	 * Boolean preference controlling automatic change to debug perspective when
	 * a debug session is launched, or when a debug session suspends
	 * (value <code>"org.eclipse.debug.ui.auto_show_debug_view"</code>). When this
	 * preference is <code>true</code> and a debug session is launched or suspends,
	 * and a debug view is not present in the current perspective, a debug perspective
	 * of the appropriate kind is created (or switched to if already created).
	 */
	public static final String PREF_AUTO_SHOW_DEBUG_VIEW= PLUGIN_ID + ".auto_show_debug_view";
	
	/**
	 * Boolean preference controlling automatic change to debug perspective when
	 * a program is launched in run mode (value <code>"org.eclipse.debug.ui.auto_show_process_view"</code>).
	 * When this preference is <code>true</code>
	 * and a program is launched, and a process view is not present in the current
	 * perspective, a debug perspective of the appropriate kind is created (or switched
	 * to if already created).
	 */
	public static final String PREF_AUTO_SHOW_PROCESS_VIEW= PLUGIN_ID + ".auto_show_process_view";
	
	// Debug views
	
	/**
	 * Debug view identifier (value <code>"org.eclipse.debug.ui.DebugView"</code>).
	 */
	public static final String ID_DEBUG_VIEW= "org.eclipse.debug.ui.DebugView";

	/**
	 * Process view identifier (value <code>"org.eclipse.debug.ui.ProcessView"</code>).
	 */
	public static final String ID_PROCESS_VIEW= "org.eclipse.debug.ui.ProcessView";
	
	/**
	 * Breakpoint view identifier (value <code>"org.eclipse.debug.ui.BreakpointView"</code>).
	 */
	public static final String ID_BREAKPOINT_VIEW= "org.eclipse.debug.ui.BreakpointView";
	
	/**
	 * Variable view identifier (value <code>"org.eclipse.debug.ui.VariableView"</code>).
	 */
	public static final String ID_VARIABLE_VIEW= "org.eclipse.debug.ui.VariableView";
	
	/**
	 * Inspector view identifier (value <code>"org.eclipse.debug.ui.InspectorView"</code>).
	 */
	public static final String ID_INSPECTOR_VIEW= "org.eclipse.debug.ui.InspectorView";
	
	/**
	 * Console view identifier (value <code>"org.eclipse.debug.ui.ConsoleView"</code>).
	 */
	public static final String ID_CONSOLE_VIEW= "org.eclipse.debug.ui.ConsoleView";

	// Debug Action images
	
	/**
	 * Debug action image identifier.
	 */
	public static final String IMG_ACT_DEBUG= "IMG_ACT_DEBUG";

	/**
	 * Run action image identifier.
	 */
	public static final String IMG_ACT_RUN= "IMG_ACT_RUN";
	
	/** Resume action image identifier. */
	public static final String IMG_LCL_RESUME= "IMG_LCL_RESUME";
	
	/** Suspend action image identifier. */
	public static final String IMG_LCL_SUSPEND= "IMG_LCL_SUSPEND";
	
	/** Terminate action image identifier. */
	public static final String IMG_LCL_TERMINATE= "IMG_LCL_TERMINATE";
	
	/** Disconnect action image identifier. */
	public static final String IMG_LCL_DISCONNECT= "IMG_LCL_DISCONNECT";
	
	/** Step into action image identifier. */
	public static final String IMG_LCL_STEPINTO= "IMG_LCL_STEPINTO";
	
	/** Step over action image identifier. */
	public static final String IMG_LCL_STEPOVER= "IMG_LCL_STEPOVER";
	
	/** Step return action image identifier. */
	public static final String IMG_LCL_STEPRETURN= "IMG_LCL_STEPRETURN";
	
	/** Clear action image identifier. */
	public static final String IMG_LCL_CLEAR= "IMG_LCL_CLEAR";
	
	/** Remove all terminated action image identifier. */
	public static final String IMG_LCL_REMOVE_TERMINATED= "IMG_LCL_REMOVE_TERMINATED";
	
	/** Display qualififed names action image identifier. */
	public static final String IMG_LCL_QUALIFIED_NAMES= "IMG_LCL_QUALIFIED_NAMES";
	
	/** Display variable type names action image identifier. */
	public static final String IMG_LCL_TYPE_NAMES= "IMG_LCL_TYPE_NAMES";
	
	/** Remove action image identifier. */
	public static final String IMG_LCL_REMOVE= "IMG_LCL_REMOVE";
	
	/** Remove all action image identifier. */
	public static final String IMG_LCL_REMOVE_ALL= "IMG_LCL_REMOVE_ALL";


	// Debug element images
	
	/** Debug mode launch image identifier. */
	public static final String IMG_OBJS_LAUNCH_DEBUG= "IMG_OBJS_LAUNCH_DEBUG";
	
	/** Run mode launch image identifier. */
	public static final String IMG_OBJS_LAUNCH_RUN= "IMG_OBJS_LAUNCH_RUN";
	
	/** Running debug target image identifier. */
	public static final String IMG_OBJS_DEBUG_TARGET= "IMG_OBJS_DEBUG_TARGET";
	
	/** Terminated debug target image identifier. */
	public static final String IMG_OBJS_DEBUG_TARGET_TERMINATED= "IMG_OBJS_DEBUG_TARGET_TERMINATED";
	
	/** Running thread image identifier. */
	public static final String IMG_OBJS_THREAD_RUNNING= "IMG_OBJS_THREAD_RUNNING";
	
	/** Suspended thread image identifier. */
	public static final String IMG_OBJS_THREAD_SUSPENDED= "IMG_OBJS_THREAD_SUSPENDED";
	
	/** Terminated thread image identifier. */
	public static final String IMG_OBJS_THREAD_TERMINATED= "IMG_OBJS_THREAD_TERMINATED";
	
	/** Stack frame image identifier. */
	public static final String IMG_OBJS_STACKFRAME= "IMG_OBJS_STACKFRAME";
	
	/** Enabled breakpoint image identifier. */
	public static final String IMG_OBJS_BREAKPOINT= "IMG_OBJS_BREAKPOINT";
	
	/** Disabled breakpoint image identifier. */
	public static final String IMG_OBJS_BREAKPOINT_DISABLED= "IMG_OBJS_BREAKPOINT_DISABLED";
		
	/** Running system process image identifier. */
	public static final String IMG_OBJS_OS_PROCESS= "IMG_OBJS_OS_PROCESS";
	
	/** Terminated system process image identifier. */
	public static final String IMG_OBJS_OS_PROCESS_TERMINATED= "IMG_OBJS_OS_PROCESS_TERMINATED";

	/** Expression image identifier. */
	public static final String IMG_OBJS_EXPRESSION= "IMG_OBJS_EXPRESSION";
	
	// wizard banners
	/** Debug wizard banner image identifier. */
	public static final String IMG_WIZBAN_DEBUG= "IMG_WIZBAN_DEBUG";
	
	/** Run wizard banner image identifier. */
	public static final String IMG_WIZBAN_RUN= "IMG_WIZBAN_RUN";
	
	/**
	 * Debug action set identifier (value <code>"org.eclipse.debug.ui.debugActionSet"</code>).
	 */
	public static final String DEBUG_ACTION_SET= PLUGIN_ID + ".debugActionSet";
	
	// menus 
	
	/** 
	 * Identifier for an empty group preceeding an
	 * edit group in a menu (value <code>"emptyEditGroup"</code>).
	 */
	public static final String EMPTY_EDIT_GROUP = "emptyEditGroup";
	
	/**
	 * Identifier for an edit group in a menu (value <code>"editGroup"</code>).
	 */
	public static final String EDIT_GROUP = "editGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * step group in a menu (value <code>"emptyStepGroup"</code>).
	 */
	public static final String EMPTY_STEP_GROUP = "emptyStepGroup";
	
	/**
	 * Identifier for a step group in a menu (value <code>"stepGroup"</code>).
	 */
	public static final String STEP_GROUP = "stepGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * thread group in a menu (value <code>"emptyThreadGroup"</code>).
	 */
	public static final String EMPTY_THREAD_GROUP = "emptyThreadGroup";
	
	/**
	 * Identifier for a thread group in a menu (value <code>"threadGroup"</code>).
	 */
	public static final String THREAD_GROUP = "threadGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * launch group in a menu (value <code>"emptyLaunchGroup"</code>).
	 */
	public static final String EMPTY_LAUNCH_GROUP = "emptyLaunchGroup";
	
	/**
	 * Identifier for a launch group in a menu (value <code>"launchGroup"</code>).
	 */
	public static final String LAUNCH_GROUP = "launchGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * variable group in a menu (value <code>"emptyVariableGroup"</code>).
	 */
	public static final String EMPTY_VARIABLE_GROUP = "emptyVariableGroup";
	
	/**
	 * Identifier for a variable group in a menu (value <code>"variableGroup"</code>).
	 */
	public static final String VARIABLE_GROUP = "variableGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * navigation group in a menu (value <code>"emptyNavigationGroup"</code>).
	 */
	public static final String EMPTY_NAVIGATION_GROUP = "emptyNavigationGroup";
	
	/**
	 * Identifier for a navigation group in a menu (value <code>"navigationGroup"</code>).
	 */
	public static final String NAVIGATION_GROUP = "navigationGroup";
	
	/** 
	 * Identifier for an empty group preceeding a
	 * breakpoint group in a menu (value <code>"emptyBreakpointGroup"</code>).
	 */
	public static final String EMPTY_BREAKPOINT_GROUP = "emptyBreakpointGroup";
	
	/**
	 * Identifier for a breakpoint group in a menu (value <code>"breakpointGroup"</code>).
	 */
	public static final String BREAKPOINT_GROUP = "breakpointGroup";
	
	/** 
	 * Identifier for an empty group preceeding an
	 * expression group in a menu (value <code>"emptyExpressionGroup"</code>).
	 */
	public static final String EMPTY_EXPRESSION_GROUP = "emptyExpressionGroup";
	
	/**
	 * Identifier for an expression group in a menu (value <code>"expressionGroup"</code>).
	 */

	public static final String EXPRESSION_GROUP = "expressionGroup";
	/** 
	 * Identifier for an empty group preceeding a
	 * render group in a menu (value <code>"emptyRenderGroup"</code>).
	 */
	public static final String EMPTY_RENDER_GROUP = "emptyRenderGroup";
	
	/**
	 * Identifier for a render group in a menu (value <code>"renderGroup"</code>).
	 */
	public static final String RENDER_GROUP = "renderGroup";
	
	/**
	 * Identifier for a property group in a menu (value <code>"propertyGroup"</code>).
	 */
	public static final String PROPERTY_GROUP = "propertyGroup";
}