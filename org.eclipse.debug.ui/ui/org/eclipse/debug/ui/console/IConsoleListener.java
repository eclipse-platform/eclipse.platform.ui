package org.eclipse.debug.ui.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.text.IRegion;

/**
 * Notified of lines appended to the console.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>This interface is still evolving</b>
 * </p>
 * @since 2.1
 */
public interface IConsoleListener {

	/**
	 * Notification that a line of text has been appended to the console. The
	 * given region describes the offset and length of the line appended to the
	 * console, excluding the line delimiter.
	 * 
	 * @param console console to which a line has been appeneded
	 * @param line region describing the offset and length of line appended to
	 * the console, excluding the line delimiter
	 */
	public void lineAppended(IConsole console, IRegion line);
	
	// TODO: do we need connect/disconnect?
}
