package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;

/**
 * A breakpoint is capable of suspending the execution of a
 * program at a specific location when a program is running
 * in debug mode. Each breakpoint has an associated marker which
 * stores and persists all attributes associated with each breakpoint.
 * <p>
 * A breakpoint is defined in two parts:
 * <ol>
 * <li>By an extension of kind <code>"org.eclipse.debug.core.breakpoints"</li>
 * <li>By a marker definition that corresponds to the above breakpoint extension</li>
 * </ol>
 * <p>
 * For example, following is a definition of corresponding breakpoint
 * and breakpoint marker definitions. Note that the <ocde>markerType</code>
 * attribute defined by the breakpoint extension corresponds to the 
 * type of the marker definition.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.breakpoints"&gt;
 *   &lt;breakpoint 
 *      id="com.example.Breakpoint"
 *      class="com.example.Breakpoint"
 *      markerType="com.example.BreakpointMarker"&gt;
 *   &lt;/breakpoint&gt;
 * &lt;/extension&gt;
 * &lt;extension point="org.eclipse.core.resources.markers"&gt;
 *   &lt;marker 
 *      id="com.example.BreakpointMarker"
 *      super type="org.eclipse.debug.core.breakpointMarker"
 *      attribute name ="exampleAttribute"&gt;
 *   &lt;/marker&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * The breakpoint manager instantiates persisted breakpoints by
 * traversing all markers that are a subtype of
 * <code>"org.eclipse.debug.core.breakpointMarker"</code>, and 
 * instantiating the class defined by the <code>class</code> attribute
 * on the associated breakpoint extension. The method <code>setMarker</code>
 * is then called to associated a marker with the breakpoint.
 */

public interface IBreakpoint extends IAdaptable {
	
	/**
	 * Root breakpoint marker type	
	 * (value <code>"org.eclipse.debug.core.breakpoint"</code>).
	 */
	public static final String BREAKPOINT_MARKER = DebugPlugin.PLUGIN_ID + ".breakpointMarker";
	
	/**
	 * Line breakpoint marker type.
	 * (value <code>"org.eclipse.debug.core.lineBreakpoint"</code>).
	 */
	public static final String LINE_BREAKPOINT_MARKER = DebugPlugin.PLUGIN_ID + ".lineBreakpointMarker";
			
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
	
	/**
	 * Attribute name for the <code>"markerType"</code> attribute of
	 * a breakpoint extension.
	 */
	public final static String MARKER_TYPE= "markerType";	

	/**
	 * Deletes this breakpoint's underlying marker, and removes
	 * this breakpoint from the breakpoint manager.
	 *
	 * @exception CoreException if deleting the underlying marker throws
	 * 	a <code>CoreException<code>.
	 */
	public void delete() throws CoreException;
	
	/**
	 * Returns the marker associated with this breakpoint, or
	 * <code>null</code> if no marker is associated with this breakpoint.
	 * 
	 * @return associated marker, or <code>null</code> if there is
	 * 	no associated marker.
	 */
	public IMarker getMarker();
	/**
	 * Sets the marker associated with this breakpoint. This method is
	 * only called at breakpoint creation, when restoring a persisted
	 * marker.
	 * 
	 * @param marker the marker to associate with this breakpoint
	 * @exception CoreException if an error occurs accessing the marker
	 */
	public void setMarker(IMarker marker) throws CoreException;
	/**
	 * Returns the identifier of the debug model this breakpoint is
	 * associated with.
	 * 
	 * @return the identifier of the debug model this breakpoint is
	 * 	associated with
	 */
	public String getModelIdentifier();
	/**
	 * Returns whether this breakpoint is enabled
	 * 
	 * @exception CoreException if a <code>CoreException</code> is 
	 * 	thrown when retrieving the enabled attribute from the underlying marker
	 * @return whether this breakpoint is enabled
	 */
	public boolean isEnabled() throws CoreException;
	/**
	 * Sets the enabled state of this breakpoint. This has no effect
	 * if the current enabled state is the same as specified by the
	 * enabled parameter.
	 * 
	 * @param enabled  whether this breakpoint should be enabled
	 * @exception CoreException if a <code>CoreException</code> is thrown
	 * 	when setting the attribute on the underlying marker.
	 */
	public void setEnabled(boolean enabled) throws CoreException;

}


