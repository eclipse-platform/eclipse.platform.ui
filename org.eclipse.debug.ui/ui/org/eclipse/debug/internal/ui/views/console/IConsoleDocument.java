package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * 
 * <p>
 * Cients are <b>not</b> intended to implement this interface.
 * </p>
 * @since 2.1
 */
public interface IConsoleDocument {

	/**
	 * Connects this console document to the given streams proxy. This associates
	 * the standard in, out, and error streams with the console.
	 * 	 * @param streamsProxy	 */
	public void connect(IStreamsProxy streamsProxy);
	
	/**
	 * Connects this console document to the given stream monitor, uniquely identified
	 * by the given identifier. This allows for more than the stanard (in, out, error)
	 * streams to be connected to the console.
	 * 	 * @param streamMonitor	 * @param streamIdentifer	 */
	public void connect(IStreamMonitor streamMonitor, String streamIdentifer);
}
