package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Defines constants for the debug tools infrastructure plug-in.
 * <p>
 * Constants only; not intended to be implemented.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IDebugConstants {
	
	/**
	 * Debug infrastructure plug-in identifier
	 * (value <code>"org.eclipse.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.debug.core";
	
	/**
	 * Launcher extension point identifier
	 * (value <code>"launchers"</code>).
	 */
	public static final String EXTENSION_POINT_LAUNCHER= "launchers";
	
	/**
	 * Breakpoint extension point identifier
	 * (value <code>"breakpoints"</code>).
	 */
	public static final String EXTENSION_POINT_BREAKPOINTS= "breakpoints";	
	
	/**
	 * Root breakpoint marker type
	 * (value <code>"org.eclipse.debug.core.breakpoint"</code>).
	 */
	public static final String BREAKPOINT = "breakpointMarker";
	
	/**
	 * Line breakpoint type.
	 * (value <code>"org.eclipse.debug.core.lineBreakpoint"</code>).
	 */
	public static final String LINE_BREAKPOINT = "lineBreakpointMarker";	
	
	/**
	 * Root breakpoint marker type	
	 * (value <code>"org.eclipse.debug.core.breakpoint"</code>).
	 */
	public static final String BREAKPOINT_MARKER = PLUGIN_ID + "." + BREAKPOINT;
	
	/**
	 * Line breakpoint marker type.
	 * (value <code>"org.eclipse.debug.core.lineBreakpoint"</code>).
	 */
	public static final String LINE_BREAKPOINT_MARKER = PLUGIN_ID + "." + LINE_BREAKPOINT;
	
	/**
	 * Debug model identifier breakpoint marker attribute
	 * (value <code>"modelIdentifier"</code>).
	 * This attribute is a <code>String<code> corresponding to the identifier
	 * of a debug model plugin a breakpoint is associated with.
	 *
	 * @see org.eclipse.core.resources.IMarker#getAttribute(String, String)
	 */
	public static final String MODEL_IDENTIFIER = "modelIdentifier";
		
	/**
	 * Enabled breakpoint marker attribute (value <code>"enabled"</code>).
	 * The attribute is a <code>boolean</code> corresponding to the
	 * enabled state of a breakpoint.
	 *
	 * @see org.eclipse.core.resources.IMarker#getAttribute(String, boolean)
	 */
	public final static String ENABLED= "enabled";
	
	/**
	 * Breakpoint marker attribute (value <code>"id"</code>).
	 * The attribute is a <code>String</code> corresponding to the
	 * id of a breakpoint type
	 */
	public final static String ID= "id";
	
}
