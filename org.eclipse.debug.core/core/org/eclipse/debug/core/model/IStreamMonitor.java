package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.IStreamListener;

/**
 * A stream monitor manages the contents of a stream a process
 * is writing to, and notifies registered listeners of changes in
 * the stream.
 * <p>
 * Clients may implement this interface. Generally, a client that
 * provides an implementation of the <code>IStreamsProxy</code>
 * interface must also provide an implementation of this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IStreamsProxy
 */
public interface IStreamMonitor {
	/**
	 * Adds the given listener to the registered listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param the listener to add
	 */
	public void addListener(IStreamListener listener);
	/**
	 * Returns the current contents of the stream. An empty
	 * String is returned if the stream is empty.
	 *
	 * @return the stream contents as a <code>String</code>
	 */
	public String getContents();
	/**
	 * Removes the given listener from the registered listeners.
	 * Has no effect if the listener is not already registered.
	 *
	 * @param the listener to remove
	 */
	public void removeListener(IStreamListener listener);
}
