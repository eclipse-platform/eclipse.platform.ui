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
	private int length = 0; // number of remaining unread bytes, -1 if closed

	private long readTimeout;
	private long closeTimeout;
	private Thread thread = new Thread(new FillBufferRunnable(), "TimeoutInputStream");//$NON-NLS-1$
	private IOException ioe = null;
	private RuntimeException re = null;

	/**
	 * Creates a timeout wrapper for an input stream.
	 * @param in the underlying input stream
	 * @param bufferSize the buffer size in bytes; should be large enough to mitigate
	 *        Thread synchronization and context switching overhead
	 * @param readTimeout the number of milliseconds to block for a read() or skip() before
	 *        throwing an InterruptedIOException; 0 blocks indefinitely, -1 does not block
	 * @param closeTimeout the number of milliseconds to block for a close() before throwing
	 *        an InterruptedIOException; 0 blocks indefinitely, -1 does not block
	 */
	public TimeoutInputStream(InputStream in, int bufferSize, long readTimeout, long closeTimeout) {
		super(in);
		this.iobuffer = new byte[bufferSize];
		this.readTimeout = readTimeout;
		this.closeTimeout = closeTimeout;
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Wraps the underlying stream's method.
	 * @throws IOException if an i/o error occurs
	 */
	public void close() throws IOException {
		if (thread == null) return;
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
	
	/**
	 * Returns the number of unread bytes in the buffer.
	 */
	public synchronized int available() throws IOException {
		return length > 0 ? length : 0;
	}
	
	/**
	 * Reads a byte from the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was received,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized int read() throws IOException {
		syncfill();
		if (length == -1) return -1;
		int b = iobuffer[head++];
		if (head == iobuffer.length) head = 0;
		length--;
		asyncfill();
		return b;
	}
	
	/**
	 * Reads multiple bytes from the stream.
	 * @throws InterruptedIOException if the timeout expired and no data was received,
	 *         bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized int read(byte[] buffer, int off, int len) throws IOException {
		if (len == 0) return 0;
		syncfill();
		if (length == -1) return -1;
		int pos = off;
		if (len > length) len = length;
		while (len-- > 0) {
			buffer[pos++] = iobuffer[head++];
			if (head == iobuffer.length) head = 0;
			length--;
		}
		asyncfill();
		return pos - off;
	}

	/**
	 * Skips multiple bytes in the stream.
	 * @throws InterruptedIOException if the timeout expired before all of the
	 *         bytes specified have been skipped, bytesTransferred may be non-zero
	 * @throws IOException if an i/o error occurs
	 */
	public synchronized long skip(long count) throws IOException {
		long amount = 0;
		try {
			while (count != 0 && length != -1) {
				int skip = (count > length) ? length : (int) count;
				head = (head + skip) % iobuffer.length;
				length -= skip;
				amount += skip;
				count -= skip;
				syncfill();
			}
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

	private void syncfill() throws IOException {
		if (length == 0) {
			asyncfill();
			if (readTimeout != -1) {
				try {
					wait(readTimeout);
				} catch (InterruptedException e) {
				}
			}
			if (length == 0) {
				throw new InterruptedIOException();
			}
		}
	}

	private void asyncfill() throws IOException {
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
			if (length != -1 && length != iobuffer.length) {
				notify();
			}
		}
	}
	
	private class FillBufferRunnable implements Runnable {
		public void run() {
			final Object lock = TimeoutInputStream.this;
			try {
				boolean eof = false;
				for (;;) {
					int off, len;
					synchronized (lock) {
						try {
							while (thread != null && (length == iobuffer.length || eof || ioe != null || re != null)) {
								lock.wait();
							}
							if (thread == null) return; // quit signal
						} catch (InterruptedException e) {
							return; // alternative quit signal
						}
						off = (head + length) % iobuffer.length;
						len = ((head > off) ? head : iobuffer.length) - off;
					}
					try {
						// the i/o operation might block without releasing the lock,
						// so we do this outside of the synchronized block
						int count = in.read(iobuffer, off, len);
						if (count == -1) eof = true;
						synchronized (lock) {
							if (eof) {
								if (length == 0) length = -1;
							} else {
								length += count;
							}
							if (count != 0) lock.notify();
						}
					} catch (InterruptedIOException e) {
						int count = e.bytesTransferred; // keep partial transfer
						e.bytesTransferred = 0; // not relevant if rethrown
						synchronized (lock) {
							if (length != -1) length += count;
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
					in.close();
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
