package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.OutputStream;

/**
 * ConsoleOutputStream is a buffered output stream that additionally
 * flushes the buffer when a carriage return is found.
 */
public class ConsoleOutputStream extends OutputStream {

	private static final int BUFFER_LENGTH = 256;
	private static byte[] buffer = new byte[BUFFER_LENGTH];
	private static int size = 0;
	
	/**
	 * @see OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		if ((((char)b) == '\n') || (size >= buffer.length))
			writeBufferToConsole();
		else
			buffer[size++] = (byte)b;
	}
	
	/**
	 * Flush the buffer
	 */
	private void writeBufferToConsole() {
		Console.appendAll(new String(buffer, 0, size));
		size = 0;
	}
}

