package org.eclipse.debug.core.model;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/


/**
 * A stream monitor who's contents can be flushed. As well, a client may
 * turn buffering on/off in a flushable stream monitor.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 2.1
 */
public interface IFlushableStreamMonitor extends IStreamMonitor {

	/**
	 * Empties the contents of this stream monitor's underlying buffer.
	 */
	public void flushContents();
	
	/**
	 * Sets whether the contents of this monitor's underlying stream is
	 * buffered.
	 * 
	 * @param buffer
	 */
	public void setBuffered(boolean buffer);
	
	/**
	 * Returns whether the contents of this monitor's underlying stream is
	 * buffered.
	 * 
	 * @return whether the contents of this monitor's underlying stream is
	 * buffered
	 */
	public boolean isBuffered();
}
