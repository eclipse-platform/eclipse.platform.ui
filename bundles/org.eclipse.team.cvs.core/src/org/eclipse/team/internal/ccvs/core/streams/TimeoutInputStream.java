/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * Wraps an input stream that blocks indefinitely to simulate timeouts on read(),
 * skip(), and close().  The resulting input stream is buffered and supports
 * retrying operations that failed due to an InterruptedIOException.
 *
 * Supports resuming partially completed operations after an InterruptedIOException
 * REGARDLESS of whether the underlying stream does unless the underlying stream itself
 * generates InterruptedIOExceptions in which case it must also support resuming.
 * Check the bytesTransferred field to determine how much of the operation completed;
 * conversely, at what point to resume.
 */
public class TimeoutInputStream extends FilterInputStream {
	private byte[] iobuffer; // circular buffer
	private int head = 0; // points to first unread byte
	private int length = 0; // number of remaining unread bytes
	private boolean eof = false; // if true, EOF encountered
	private boolean closeRequested = false; // if true, close requested
	private IOException ioe = null;
	private RuntimeException re = null;

	private long readTimeout; // read() timeout in millis
	private long closeTimeout; // close() timeout in millis, or -1
	private Thread thread;

	/**
	 * Creates a timeout wrapper for an input stream.
	 * @param in the underlying input stream
	 * @param bufferSize the buffer size in bytes; should be large enough to mitigate
	 *        Thread synchronization and context switching overhead
	 * @param readTimeout the number of milliseconds to block for a read() or skip() before
	 *        throwing an InterruptedIOException; 0 blocks indefinitely
	 * @param closeTimeout the number of milliseconds to block for a close() before throwing
	 *        an InterruptedIOException; 0 blocks indefinitely, -1 closes the stream in the background
	 */
	public TimeoutInputStream(InputStream in, int bufferSize, long readTimeout, long closeTimeout) {
		super(in);
		this.iobuffer = new byte[bufferSize];
		this.readTimeout = readTimeout;
		this.closeTimeout = closeTimeout;
		thread = new Thread(new FillBufferRunnable(), "TimeoutInputStream");//$NON-NLS-1$
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Wraps the underlying stream's method.
	 * It may be important to wait for a stream to actually be closed because it
	 * holds an implicit lock on a system resoure (such as a file) while it is
	 * open.  Closing a stream may take time if the underlying stream is still
	 * servicing a previous request.
	 * @throws InterruptedIOException if the timeout expired
	 * @throws IOException if an i/o error occurs
	 */
	public void close() throws IOException {
		Thread oldThread;
		synchronized (this) {
			if (thread == null) return;
			oldThread = thread;
			closeRequested = true;
			thread.interrupt();
			checkError();
		}
		if (closeTimeout == -1) return;
		try {
			oldThread.join(closeTimeout);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // we weren't expecting to be interrupted
		}
		synchronized (this) {
			checkError();
			if (thread != null) throw new InterruptedIOException();
		}
	}
	
	/**
	 * Returns the number of unread bytes in the buffer.
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized int available() throws IOException {
		if (length == 0) checkError();
		return length > 0 ? length : 0;
	}
	
	/**
	 * Reads a byte from the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was received,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized int read() throws IOException {
		if (length == 0) checkError();
		syncFill();
		if (length == 0) {
			checkError();
			if (eof) return -1;
			throw new InterruptedIOException();
		}
		int b = iobuffer[head++] & 255;
		if (head == iobuffer.length) head = 0;
		length--;
		asyncFill();
		return b;
	}
	
	/**
	 * Reads multiple bytes from the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was received,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized int read(byte[] buffer, int off, int len) throws IOException {
		if (length == 0) checkError();
		syncFill();
		if (length == 0) {
			checkError();
			if (eof) return -1;
			throw new InterruptedIOException();
		}
		int pos = off;
		if (len > length) len = length;
		while (len-- > 0) {
			buffer[pos++] = iobuffer[head++];
			if (head == iobuffer.length) head = 0;
			length--;
		}
		asyncFill();
		return pos - off;
	}

	/**
	 * Skips multiple bytes in the stream.
	 * @throws InterruptedIOException if the timeout expired before all of the
	 *         bytes specified have been skipped, bytesTransferred may be non-zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized long skip(long count) throws IOException {
		if (length == 0) checkError();
		long amount = 0;
		try {
			while (count != 0) {
				int skip = (count > length) ? length : (int) count;
				head = (head + skip) % iobuffer.length;
				length -= skip;
				amount += skip;
				count -= skip;
				syncFill();
				if (length == 0) {
					checkError();
					if (eof) break;
					throw new InterruptedIOException();
				}
			}
			asyncFill();
			return amount;
		} catch (InterruptedIOException e) {
			e.bytesTransferred = (int) amount; // assumes amount < Integer.MAX_INT
			throw e;
		}
	}

	/**
	 * Mark is not supported by the wrapper even if the underlying stream does, returns false.
	 */
	public boolean markSupported() {
		return false;
	}

	/*
	 * Waits for the buffer to fill if it is empty and the stream has not reached EOF.
	 * The buffer might still be empty when this method returns if the operation timed out
	 * or EOF was encountered.
	 */
	private void syncFill() throws IOException {
		if (length != 0 || eof) return;
		notify();
		try {
			wait(readTimeout);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // we weren't expecting to be interrupted
		}
	}

	/*
	 * Notifies the background thread that some bytes were read so that it can fill the buffer
	 * asynchronously in the background.
	 */
	private void asyncFill() {
		if ((length != iobuffer.length && ! eof) || closeRequested) notify();
	}

	/*
	 * Checks if exceptions are pending and throws the next one, if any.
	 */
	private void checkError() throws IOException {
		if (ioe != null) {
			IOException e = ioe;
			ioe = null;
			throw e;
		}
		if (re != null) {
			RuntimeException e = re;
			re = null;
			throw e;
		}
	}
	
	private class FillBufferRunnable implements Runnable {
		private final Object lock = TimeoutInputStream.this;

		public void run() {
			try {
				readUntilDone();
				waitUntilClosed();
			} catch (IOException e) {
				synchronized (lock) { ioe = e; }
				waitUntilClosed();
			} catch (RuntimeException e) {
				synchronized (lock) { re = e; }
				waitUntilClosed();
			} finally {
				/*** Closes the stream and sets thread to null when done ***/
				try {
					in.close();
				} catch (IOException e) {
					synchronized (lock) { ioe = e; } 
				} catch (RuntimeException e) {
					synchronized (lock) { re = e; }
				} finally {
					synchronized (lock) {
						eof = true;
						thread = null;
						lock.notify();
					}
				}
			}
		}
		
		/**
		 * Reads bytes into the buffer until EOF, closed, or error.
		 */
		private void readUntilDone() throws IOException {
			boolean pause = false;
			for (;;) {
				int off, len;
				synchronized (lock) {
					for (;;) {
						if (closeRequested || eof) return; // quit signal
						if (length != iobuffer.length && ! pause) break;
						pause = false;
						try {
							lock.wait();
						} catch (InterruptedException e) {
							closeRequested = true; // alternate quit signal
						}
					}
					off = (head + length) % iobuffer.length;
					len = ((head > off) ? head : iobuffer.length) - off;
				}
				int count;
				try {
					// the i/o operation might block without releasing the lock,
					// so we do this outside of the synchronized block
					count = in.read(iobuffer, off, len);
				} catch (InterruptedIOException e) {
					// keep partial transfer
					count = e.bytesTransferred;
					e.bytesTransferred = 0;
					synchronized (lock) { ioe = e; }
				}
				synchronized (lock) {
					if (count == -1) return;
					if (count == 0) pause = true;
					length += count;
					lock.notify();
				}
			}				
		}
		
		/**
		 * Waits until we have been requested to close the stream.
		 */
		private void waitUntilClosed() {
			synchronized (lock) {
				eof = true;
				lock.notify();
				while (! closeRequested) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						closeRequested = true; // alternate quit signal
					}
				}
			}
		}
	}
}
