/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;



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
	 * Sets whether the contents of this monitor's underlying stream should be
	 * buffered. When <code>false</code>, contents appended to this stream monitor
	 * are not stored in a buffer, and are thus not available from
	 * <code>getContents()</code>. Registered listeners are notified of appended
	 * text, and must buffer the contents if desired.
	 * 
	 * @param buffer whether the contents of this monitor's underlying stream
	 * should be buffered
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
