package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An event filter allows clients to intercept debug events.
 * Event filters are registered with the debug plug-in.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see DebugPlugin
 */
public interface IDebugEventFilter {

	/**
	 * Returns whether the given debug event is filtered.
	 * When <code>true</code> is returned, the event is not
	 * sent to registered debug event listeners.
	 * 
	 * @return  whether the given debug event is filtered
	 */
	public boolean filterDebugEvent(DebugEvent event);
}
