package org.eclipse.ui.console;

import java.io.IOException;

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
