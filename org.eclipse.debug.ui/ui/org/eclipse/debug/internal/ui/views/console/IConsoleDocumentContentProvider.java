package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.graphics.Color;

/**
 * Provides content for a console document.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 2.1
 */

public interface IConsoleDocumentContentProvider {

	/**
	 * Returns whether the console associated with this content provider's
	 * process can accept keyboard input. This attribute may change over the life
	 * of a process/document.
	 * 	 * @return whether the console associated with this content provider's
	 * process can accept keyboard input	 */
	public boolean isReadOnly();
	
	/**
	 * Returns whether the output is still being produced by this content provider.
	 * Usually, this is associated with the termianted property of this content
	 * provider's process.
	 * 	 * @return whether the output is still being produced by this content provider	 */
	public boolean isTerminated();
	
	/**
	 * Returns the color to draw output associated with the given stream.
	 * 	 * @param streamIdentifer	 * @return Color	 */
	public Color getColor(String streamIdentifer);
	
	/**
	 * Connects this content provider to the given process and console document.
	 * This content provider should connect its streams to the given console
	 * document.
	 * 	 * @param process	 * @param partitioner	 */
	public void connect(IProcess process, IConsoleDocument partitioner);
	
	/**
	 * Disconnects this content provider.	 */
	public void disconnect();
}
