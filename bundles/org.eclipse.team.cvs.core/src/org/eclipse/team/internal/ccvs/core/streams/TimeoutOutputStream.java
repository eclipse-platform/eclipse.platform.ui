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

	private long writeTimeout;
	private long closeTimeout;
	private boolean flushRequested = false;
	private Thread thread = new Thread(new CommitBufferRunnable(), "TimeoutInputStream");//$NON-NLS-1$
	private IOException ioe = null;
	private RuntimeException re = null;

	/**
	 * Creates a timeout wrapper for an output stream.
	 * @param out the underlying input stream
	 * @param bufferSize the buffer size in bytes; should be large enough to mitigate
	 *        Thread synchronization and context switching overhead
	 * @param writeTimeout the number of milliseconds to block for a write() or flush() before
	 *        throwing an InterruptedIOException; 0 blocks indefinitely, -1 does not block
	 * @param closeTimeout the number of milliseconds to block for a close() before throwing
	 *        an InterruptedIOException; 0 blocks indefinitely, -1 does not block
	 */
	public TimeoutOutputStream(OutputStream out, int bufferSize, long writeTimeout, long closeTimeout) {
		super(out);
		this.iobuffer = new byte[bufferSize];
		this.writeTimeout = writeTimeout;
		this.closeTimeout = closeTimeout;
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Wraps the underlying stream's method.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes flushed from the buffer
	 * @throws IOException if an i/o error occurs
	 */
	public void close() throws IOException {
		if (thread == null) return;
		try {
			flush();
		} finally {
			Thread oldThread = thread;
			thread = null;
			oldThread.interrupt();
			if (closeTimeout != -1) {
				try {
					oldThread.join(closeTimeout);
				} catch (InterruptedException e) {
				}
			}
			synchronized (this) {
				if (ioe != null) throw ioe;
				if (re != null) throw re;
			}
		}
	}

	/**
	 * Writes a byte to the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was sent,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void write(int b) throws IOException {
		if (length == iobuffer.length) synccommit();
		iobuffer[(head + length) % iobuffer.length] = (byte) b;
		length++;
		asynccommit();
	}
	
	/**
	 * Writes multiple bytes to the stream.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes sent
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void write(byte[] buffer, int off, int len) throws IOException {
		int amount = 0;
		try {
			while (len-- > 0) {
				if (length == iobuffer.length) synccommit();
				iobuffer[(head + length) % iobuffer.length] = buffer[off++];
				length++;
				amount++;
			}
			asynccommit();
		} catch (InterruptedIOException e) {
			e.bytesTransferred = amount;
			throw e;
		}
	}

	/**
	 * Flushes the stream.
	 * @throws InterruptedIOException if the timeout expired, bytesTransferred will
	 *         reflect the number of bytes flushed from the buffer
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized void flush() throws IOException {
		int oldLength = length;
		flushRequested = true;
		InterruptedIOException iioe = null;
		try {
			synccommit();
			if (length == 0) return;
			iioe = new InterruptedIOException();
		} catch (InterruptedIOException e) {
			iioe = e;
		}
		iioe.bytesTransferred = oldLength - length;
		throw iioe;
	}
	
	private void synccommit() throws IOException {
		asynccommit();
		if (length != 0) {
			int oldLength = length;
			if (writeTimeout != -1) {
				try {
					wait(writeTimeout);
				} catch (InterruptedException e) {
				}
			}
			if (length == oldLength) {
				throw new InterruptedIOException();
			}
		}
	}

	private void asynccommit() throws IOException {
		try {
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
		} finally {
			if (length != 0 || flushRequested) notify();
		}
	}
	
	private class CommitBufferRunnable implements Runnable {
		public void run() {
			final Object lock = TimeoutOutputStream.this;
			boolean running = true;
			try {
				for (;;) {
					int off, len;
					synchronized (lock) {
						try {
							while (running && thread != null && ((length == 0 && ! flushRequested) || ioe != null || re != null)) {
								lock.wait();
							}
							if (thread == null) running = false; // quit signal
							if (! running && length == 0) return;
						} catch (InterruptedException e) {
							running = false; // alternative quit signal
						}
						off = head;
						len = iobuffer.length - head;
						if (len > length) len = length;
					}
					try {
						// the i/o operation might block without releasing the lock,
						// so we do this outside of the synchronized block
						if (len != 0) out.write(iobuffer, off, len);
						if (flushRequested) out.flush();
						synchronized (lock) {
							flushRequested = false;
							head = (head + len) % iobuffer.length;
							length -= len;
							lock.notify();
						}
					} catch (InterruptedIOException e) {
						len = e.bytesTransferred; // keep partial transfer
						e.bytesTransferred = 0; // not relevant if rethrown
						synchronized (lock) {
							head = (head + len) % iobuffer.length;
							length -= len;
							ioe = e;
							lock.notify();
						}
					} catch (IOException e) {
						synchronized (lock) {
							ioe = e;
							lock.notify();
						}
					} catch (RuntimeException e) {
						synchronized (lock) {
							re = e;
							lock.notify();
						}
					}
				}
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					synchronized (lock) {
						ioe = e;
					} 
				} catch (RuntimeException e) {
					synchronized (lock) {
						re = e;
					}
				}
			}
		}
	}
}
