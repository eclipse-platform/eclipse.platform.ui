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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Wraps an output stream that blocks indefinitely to simulate timeouts on write(),
 * flush(), and close().  The resulting output stream is buffered and supports
 * retrying operations that failed due to an InterruptedIOException.
 *
 * Supports resuming partially completed operations after an InterruptedIOException
 * REGARDLESS of whether the underlying stream does unless the underlying stream itself
 * generates InterruptedIOExceptions in which case it must also support resuming.
 * Check the bytesTransferred field to determine how much of the operation completed;
 * conversely, at what point to resume.
 */
public class TimeoutOutputStream extends FilterOutputStream {
	private byte[] iobuffer; // circular buffer
	private int head = 0; // points to first unwritten byte
	private int length = 0; // number of remaining unwritten bytes
	private boolean cannotWrite = false; // if true, writes will not be honoured
	private boolean closeRequested = false; // if true, close requested
	private boolean flushRequested = false; // if true, flush requested
	private IOException ioe = null;
	private RuntimeException re = null;

	private long writeTimeout; // write() timeout in millis
	private long closeTimeout; // close() timeout in millis, or -1
	private Thread thread;

	/**
	 * Creates a timeout wrapper for an output stream.
	 * @param out the underlying input stream
	 * @param bufferSize the buffer size in bytes; should be large enough to mitigate
	 *        Thread synchronization and context switching overhead
	 * @param writeTimeout the number of milliseconds to block for a write() or flush() before
	 *        throwing an InterruptedIOException; 0 blocks indefinitely
	 * @param closeTimeout the number of milliseconds to block for a close() before throwing
	 *        an InterruptedIOException; 0 blocks indefinitely, -1 closes the stream in the background
	 */
	public TimeoutOutputStream(OutputStream out, int bufferSize, long writeTimeout, long closeTimeout) {
		super(out);
		this.iobuffer = new byte[bufferSize];
		this.writeTimeout = writeTimeout;
		this.closeTimeout = closeTimeout;
		thread = new Thread(new CommitBufferRunnable(), "TimeoutOutputStream");//$NON-NLS-1$
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Wraps the underlying stream's method.
	 * It may be important to wait for a stream to actually be closed because it
	 * holds an implicit lock on a system resoure (such as a file) while it is
	 * open.  Closing a stream may take time if the underlying stream is still
	 * servicing a previous request.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes flushed from the buffer
	 * @throws IOException if an i/o error occurs
	 */
	public void close() throws IOException {
		Thread oldThread;
		synchronized (this) {
			if (thread == null) return;
			oldThread = thread;
			closeRequested = true;
			flushRequested = true;
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
	 * Writes a byte to the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was sent,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void write(int b) throws IOException {
		if (cannotWrite) throw new IOException(Policy.bind("TimeoutOutputStream.cannotWriteToStream"));
		checkError();
		if (length == iobuffer.length) {
			syncCommit();
			if (length == iobuffer.length) throw new InterruptedIOException();
		}
		iobuffer[(head + length) % iobuffer.length] = (byte) b;
		length++;
		asyncCommit();
	}
	
	/**
	 * Writes multiple bytes to the stream.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes sent
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void write(byte[] buffer, int off, int len) throws IOException {
		if (cannotWrite) throw new IOException(Policy.bind("TimeoutOutputStream.cannotWriteToStream"));
		checkError();
		int amount = 0;
		try {
			while (len-- > 0) {
				if (length == iobuffer.length) {
					syncCommit();
					if (length == iobuffer.length) throw new InterruptedIOException();
				}
				iobuffer[(head + length) % iobuffer.length] = buffer[off++];
				length++;
				amount++;
			}
		} catch (InterruptedIOException e) {
			e.bytesTransferred = amount;
			throw e;
		}
		asyncCommit();
	}

	/**
	 * Flushes the stream.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes flushed from the buffer
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void flush() throws IOException {
		if (cannotWrite) throw new IOException(Policy.bind("TimeoutOutputStream.cannotWriteToStream"));
		checkError();
		flushRequested = true;
		int amount = 0;
		try {
			while (flushRequested && length != 0) {
				int oldLength = length;
				syncCommit();
				amount += oldLength - length;
				if (length == oldLength) throw new InterruptedIOException();
			}
		} catch (InterruptedIOException e) {
			e.bytesTransferred = amount;
			throw e;
		}
		asyncCommit();
	}
	
	/*
	 * Waits for the buffer to drain.
	 * The buffer might still be at the same level when this method returns if the operation timed out.
	 */
	private void syncCommit() throws IOException {
		notify();
		try {
			wait(writeTimeout);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // we weren't expecting to be interrupted
		}
		checkError();
		if (cannotWrite) throw new IOException(Policy.bind("TimeoutOutputStream.cannotWriteToStream"));
	}

	/*
	 * Notifies the background thread that some bytes were written so that it can drain the buffer
	 * asynchronously in the background.
	 */
	private void asyncCommit() throws IOException {
		if (length != 0 || flushRequested || closeRequested) notify();
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
	
	private class CommitBufferRunnable implements Runnable {
		private final Object lock = TimeoutOutputStream.this;

		public void run() {
			try {
				writeUntilDone();
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
					out.close();
				} catch (IOException e) {
					synchronized (lock) { ioe = e; } 
				} catch (RuntimeException e) {
					synchronized (lock) { re = e; }
				} finally {
					synchronized (lock) {
						cannotWrite = true;
						thread = null;
						lock.notify();
					}
				}
			}
		}
		
		/**
		 * Writes bytes from the buffer until closed and buffer is empty
		 */
		private void writeUntilDone() throws IOException {
			boolean pause = false;
			boolean mustFlush = false;
			for (;;) {
				int off, len;
				synchronized (lock) {
					for (;;) {
						if (closeRequested && length == 0) return; // quit signal
						if ((mustFlush || flushRequested || length != 0) && ! pause) break;
						pause = false;
						try {
							lock.wait();
						} catch (InterruptedException e) {
							closeRequested = true; // alternate quit signal
						}
					}
					off = head;
					len = iobuffer.length - head;
					if (len > length) len = length;
					if (flushRequested) {
						mustFlush = true;
						flushRequested = false;
					}
				}
				if (len != 0) {
					try {
						// the i/o operation might block without releasing the lock,
						// so we do this outside of the synchronized block
						out.write(iobuffer, off, len);
					} catch (InterruptedIOException e) {
						len = e.bytesTransferred;
						e.bytesTransferred = 0;
						if (len == 0) pause = true;
						synchronized (lock) { ioe = e; }
					}
					synchronized (lock) {
						head = (head + len) % iobuffer.length;
						length -= len;
						lock.notify();
					}
				} else {
					try {
						out.flush();
						mustFlush = false;
					} catch (InterruptedIOException e) {
						if (e.bytesTransferred == 0) pause = true;
						e.bytesTransferred = 0;
						synchronized (lock) { ioe = e; }
					}
					synchronized (lock) {
						lock.notify();
					}
				}
			}
		}
		
		/**
		 * Waits until we have been requested to close the stream.
		 */
		private void waitUntilClosed() {
			synchronized (lock) {
				cannotWrite = true;
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
