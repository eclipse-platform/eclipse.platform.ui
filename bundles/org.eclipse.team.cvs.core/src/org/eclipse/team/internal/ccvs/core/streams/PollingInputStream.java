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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Polls a progress monitor periodically and handles timeouts over extended durations.
 * For this class to be effective, a high numAttempts should be specified, and the
 * underlying stream should time out frequently on reads (every second or so).
 *
 * Supports resuming partially completed operations after an InterruptedIOException
 * if the underlying stream does.  Check the bytesTransferred field to determine how
 * much of the operation completed; conversely, at what point to resume.
 */
public class PollingInputStream extends FilterInputStream {
	private static final boolean DEBUG = Policy.DEBUG_STREAMS;
	private int numAttempts;
	private IProgressMonitor monitor;
	
	/**
	 * Creates a new polling input stream.
	 * @param in the underlying input stream
	 * @param numAttempts the number of attempts before issuing an InterruptedIOException,
	 *        if 0, retries indefinitely until canceled
	 * @param monitor the progress monitor to be polled for cancellation
	 */
	public PollingInputStream(InputStream in, int numAttempts, IProgressMonitor monitor) {
		super(in);
		this.numAttempts = numAttempts;
		this.monitor = monitor;
	}
	
	/**
	 * Wraps the underlying stream's method.
	 * It may be important to wait for an input stream to be closed because it
	 * holds an implicit lock on a system resoure (such as a file) while it is
	 * open.  Closing a stream may take time if the underlying stream is still
	 * servicing a previous request.
	 * @throws OperationCanceledException if the progress monitor is canceled
	 * @throws InterruptedIOException if the underlying operation times out numAttempts times
	 * @throws IOException if an i/o error occurs
	 */
	public void close() throws IOException {
		int attempts = 0;
		for (;;) {
			try {
				in.close();
				return;
			} catch (InterruptedIOException e) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				if (++attempts == numAttempts)
					throw new InterruptedIOException(Policy.bind("PollingInputStream.closeTimeout")); //$NON-NLS-1$
				if (DEBUG) System.out.println("close retry=" + attempts); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Wraps the underlying stream's method.
	 * @throws OperationCanceledException if the progress monitor is canceled
	 * @throws InterruptedIOException if the underlying operation times out numAttempts times
	 *         and no data was received, bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public int read() throws IOException {
		int attempts = 0;
		for (;;) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			try {
				return in.read();
			} catch (InterruptedIOException e) {
				if (++attempts == numAttempts)
					throw new InterruptedIOException(Policy.bind("PollingInputStream.readTimeout")); //$NON-NLS-1$
				if (DEBUG) System.out.println("read retry=" + attempts); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Wraps the underlying stream's method.
	 * @throws OperationCanceledException if the progress monitor is canceled
	 * @throws InterruptedIOException if the underlying operation times out numAttempts times
	 *         and no data was received, bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public int read(byte[] buffer, int off, int len) throws IOException {
		int attempts = 0;
		for (;;) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			try {
				return in.read(buffer, off, len);
			} catch (InterruptedIOException e) {
				if (e.bytesTransferred != 0) return e.bytesTransferred; // keep partial transfer
				if (++attempts == numAttempts)
					throw new InterruptedIOException(Policy.bind("PollingInputStream.readTimeout")); //$NON-NLS-1$
				if (DEBUG) System.out.println("read retry=" + attempts); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Wraps the underlying stream's method.
	 * @throws OperationCanceledException if the progress monitor is canceled
	 * @throws InterruptedIOException if the underlying operation times out numAttempts times
	 *         and no data was received, bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public long skip(long count) throws IOException {
		int attempts = 0;
		for (;;) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			try {
				return in.skip(count);
			} catch (InterruptedIOException e) {
				if (e.bytesTransferred != 0) return e.bytesTransferred; // keep partial transfer
				if (++attempts == numAttempts)
					throw new InterruptedIOException(Policy.bind("PollingInputStream.readTimeout")); //$NON-NLS-1$
				if (DEBUG) System.out.println("read retry=" + attempts); //$NON-NLS-1$
			}
		}
	}
}
