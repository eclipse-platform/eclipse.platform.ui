package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * This class can be used to transfer a file from the CVS server to a local IFile
 */
public class FileInputStreamWrapper {
	
	// default file transfer buffer size (in bytes)
	private static int TRANSFER_BUFFER_SIZE = 8192;
	// update progress bar in increments of this size (in bytes)
	//   no incremental progress shown for files smaller than this size
	private static int TRANSFER_PROGRESS_INCREMENT = 32768;
	
	// the platform's line termination sequence
	private static final byte[] PLATFORM_NEWLINE_BYTES =
		System.getProperty("line.separator").getBytes();  //$NON-NLS-1$ // at least one byte long
	// the server's line termination sequence
	private static final int SERVER_NEWLINE_BYTE = 0x0a; // exactly one byte long
	private static final byte[] SERVER_NEWLINE_BYTES = new byte[] { SERVER_NEWLINE_BYTE };
	// true iff newlines must be converted between platform and server formats
	private static boolean DONT_CONVERT_NEWLINES = PLATFORM_NEWLINE_BYTES.length == 1
		&& PLATFORM_NEWLINE_BYTES[0] == SERVER_NEWLINE_BYTE;
		
	// VCM 1.0 comitted files using CR/LF as a delimiter
	private static final int CARRIAGE_RETURN_BYTE = 0x0d;
	
	private InputStream input;
	private long fileSize;
	private int totalRead;
	private boolean isBinary;
	private IProgressMonitor monitor;
	private byte[] buffer;
	private int nextProgressThresh;
	
	private static final byte[] BUFFER = new byte[TRANSFER_BUFFER_SIZE / 2];
	private static final byte[] EXPANSION_BUFFER = new byte[TRANSFER_BUFFER_SIZE];
	
	private int position;
	private int bufferLength;
	private String title;
	
	public FileInputStreamWrapper(InputStream input, long fileSize, boolean isBinary, String title, IProgressMonitor monitor) {
		this.input = input;
		this.fileSize = fileSize;
		this.totalRead = 0;
		this.isBinary = isBinary;
		this.monitor = monitor;
		this.buffer = BUFFER;
		this.nextProgressThresh = TRANSFER_PROGRESS_INCREMENT;
		this.title = title;
	}
	
	public class InputStreamFromServer extends InputStream {
		public int read() throws IOException {
			if (position >= bufferLength) {
				if (fill() == -1)
					return -1;
			}
			return buffer[position++];
		}
		public int read(byte[] bytes) throws IOException {
			return read(bytes, 0, bytes.length);
		}
		public int read(byte[] bytes, int offset, int length) throws IOException {
			if (position >= bufferLength) {
				if (fill() == -1)
					return -1;
			}
			length = Math.min(bufferLength - position, length);
			System.arraycopy(buffer, position, bytes, offset, length);
			position += length;
			return length;
		}
	}
	
	/**
	 * Return a stream that can be passed to IFile#setContent()
	 * After the call to setContent, the receiver's input stream will be at the byte
	 * after the received file.
	 */
	public InputStream getInputStream() {
		return new InputStreamFromServer();
	}
	
	/*
	 * Transfers a file to or from the remove CVS server, possibly expanding line delimiters.
	 * <p>
	 * Line termination sequences are only converted upon request by specifying an
	 * array containing the expected sequence of bytes representing an outbound newline,
	 * and a single byte representing an inbound newline.  If null is passed for the
	 * former, the file is assumed to have binary contents, hence no translation is
	 * performed.
	 * </p><p>
	 * Translation is performed on-the-fly, so the file need not fit in available memory.
	 * </p>
	 * @param in the input stream
	 * @param out the output stream
	 * @param size the source file size
	 * @param newlineIn the single byte for a received newline, ignored if binary
	 * @param newlineOut the sequence of bytes for sent newline, or null if binary
	 * @param monitor the progress monitor
	 * @param title the name of the file being received (as shown in the monitor)
	 */
	private int fill() throws IOException {
		
		// Check if we've read the entire file
		if (totalRead == fileSize) {
			return -1;
		} else if (position < bufferLength) {
			return bufferLength - position;
		}

		position = 0;

		// If we're not converting, use the big buffer to read
		if (isBinary || DONT_CONVERT_NEWLINES) {
			buffer = EXPANSION_BUFFER;
		} else {
			buffer = BUFFER;
		}
		
		bufferLength = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
		if (bufferLength == -1) {
			// Unexpected end of stream
			throw new IOException(Policy.bind("Session.readError")); //$NON-NLS-1$
		}
		totalRead += bufferLength;
		
		if (isBinary || DONT_CONVERT_NEWLINES) {
			return bufferLength;
		}
		
		bufferLength = convertNewLines(BUFFER, EXPANSION_BUFFER, bufferLength);
		buffer = EXPANSION_BUFFER;
		
		// update progress monitor
		if (totalRead > nextProgressThresh) {
			monitor.subTask(Policy.bind("Session.transfer", //$NON-NLS-1$
					new Object[] { title, new Long(totalRead / 1024), new Long(fileSize / 1024)}));
			nextProgressThresh = totalRead + TRANSFER_PROGRESS_INCREMENT;
		}
		
		return bufferLength;
	}
	
	/*
	 * Copy the bytes from the source to the target, converting any LF to the platform newline byte.
	 * 
	 * There is special handling that will skip incoming CRs that precede LF.
	 */
	private int convertNewLines(byte[] source, byte[] target, int length) {
		boolean seenCR = false;
		int targetPosition = 0;
		for (int sourcePosition = 0; sourcePosition < length; ++sourcePosition) {
			final byte b = source[sourcePosition];
			if (b == CARRIAGE_RETURN_BYTE) {
				// We keep track of CRs to perform autocorrection for improperly stored text files
				seenCR = true;
			} else {
				if (b == SERVER_NEWLINE_BYTE) {
					// if fixCRLF we ignore previous CR (if there was one)
					// replace newlineIn with newlineOut
					for (int x = 0; x < PLATFORM_NEWLINE_BYTES.length; ++x) target[targetPosition++] = PLATFORM_NEWLINE_BYTES[x];
				} else {
					if (seenCR) target[targetPosition++] = CARRIAGE_RETURN_BYTE; // preserve stray CR's
					target[targetPosition++] = b;
				}
				seenCR = false;
			}
		}
		if (seenCR) target[targetPosition++] = CARRIAGE_RETURN_BYTE;
		
		return targetPosition;
	}
}
