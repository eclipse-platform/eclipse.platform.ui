package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A debug event listener registers with the debug plug-in
 * to receive event notification from programs being debugged.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see DebugEvent
 * @deprecated implementors should implement the new interface
 *  <code>IDebugEventSetListener</code>
 */
public interface IDebugEventListener {
	/**
	 * Notifies this listener of the given debug event.
	 *
	 * @param event the debug event
	 * @deprecated impementors should implement the new interface
	 * 	<code>IDebugEventSetListener</code>
	 */
	public void handleDebugEvent(DebugEvent event);
}
