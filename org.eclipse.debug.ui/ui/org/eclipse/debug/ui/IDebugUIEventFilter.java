package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch; 

/**
 * An event filter allows clients to selectively suppress events that
 * cause the debug UI to automatically display the debug/process view when
 * a launch is registered or when a debug session suspends.
 * <p>
 * For example, a client that programatically launches a debug session may
 * not want the default behavior of switching to the debug perspective as
 * a result of registering a launch. In this case the client could add a filter
 * to the debug UI to selectively suppress switch to the debug perspective.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IDebugUIEventFilter {
	
	/**
	 * Returns whether the given launch should cause the debug UI to
	 * show the debug/process view. This method is only called when the
	 * <code>PREF_AUTO_SHOW_DEBUG_VIEW</code> preference is <code>true</code>.
	 *
	 * @param the launch that has just been registered
	 * @return whether to automatically display the launch
	 */
	public boolean showLaunch(ILaunch launch);
	
	/**
	 * Returns whether the given event should cause the debug UI to
	 * show the associated debug element. This method is only called when the
	 * <code>PREF_AUTO_SHOW_DEBUG_VIEW</code> preference is <code>true</code>,
	 * and the debug event is of kind <code>SUSPEND</code>.
	 *
	 * @param the launch that has just been registered
	 * @return whether to automatically display the launch
	 * @see DebugEvent
	 */
	public boolean showDebugEvent(DebugEvent event);

}