package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.IStreamListener;

/**
 * A stream monitor manages the contents of the stream a process
 * is writing to, and notifies registered listeners of changes in
 * the stream.
 * <p>
 * Clients may implement this interface. Generally, a client that
 * provides an implementation of the <code>IStreamsProxy</code>
 * interface must also provide an implementation of this interface.
 * </p>
 * @see IStreamsProxy
 */
public interface IStreamMonitor {
	/**
	 * Adds the given listener to this stream monitor's registered listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addListener(IStreamListener listener);
	/**
	 * Returns the entire current contents of the stream. An empty
	 * String is returned if the stream is empty.
	 *
	 * @return the stream contents as a <code>String</code>
	 */
	public String getContents();
	/**
	 * Removes the given listener from this stream monitor's registered listeners.
	 * Has no effect if the listener is not already registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeListener(IStreamListener listener);
}
