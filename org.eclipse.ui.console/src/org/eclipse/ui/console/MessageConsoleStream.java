/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import java.io.IOException;

/**
 * Used to write messages to a message console. A message console may have more
 * than one stream connected to it. Each stream may be displayed in a different
 * color.
 * 
 * @since 3.0
 */
public class MessageConsoleStream extends IOConsoleOutputStream {
	/**
	 * Constructs a new stream connected to the given console.
	 * 
	 * @param console the console to write messages to
	 */
	public MessageConsoleStream(MessageConsole console) {
	    super(console);
	}
	
	/**
	 * Appends the specified message to this stream.
	 * 
	 * @param message message to append
	 */
	public void print(String message) {
		try {
            write(message);
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
	}
	
	
	/**
	 * Appends a line separator string to this stream.
	 */
	public void println() {
		try {
            write("\n"); //$NON-NLS-1$
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
	}	
	
	/**
	 * Appends the specified message to this stream, followed by a line
	 * separator string.
	 * 
	 * @param message message to print
	 */
	public void println(String message) {
		print(message);
		println();
	}	
}
