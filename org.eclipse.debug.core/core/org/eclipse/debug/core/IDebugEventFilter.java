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
 * @since 2.0
 */
public interface IDebugEventFilter {

	/**
	 * Filters the given set of debug events, and returns the set of debug
	 * events that should be fired to registered listeners - <code>null</code>
	 * or an empty collection if no debug events should be fired.
	 * <p>
	 * When multiple event filters are registered, events are passed through
	 * all filters. That is, the events returned from the first filter are
	 * passed through the second filter, and so on.
	 * </p>
	 * 
	 * @return  the set of debug events to fire
	 */
	public DebugEvent[] filterDebugEvents(DebugEvent[] events);
}
