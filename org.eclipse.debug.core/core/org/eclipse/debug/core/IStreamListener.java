package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IStreamMonitor;

/**
 * A stream listener is notified of changes
 * to a stream monitor.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IStreamMonitor
 */
public interface IStreamListener {
	/**
	 * Notifies this listener that text has been appended to
	 * the given stream monitor.
	 *
	 * @param text the appended text
	 * @param monitor the stream monitor to which text was appended
	 */
	public void streamAppended(String text, IStreamMonitor monitor);
}
