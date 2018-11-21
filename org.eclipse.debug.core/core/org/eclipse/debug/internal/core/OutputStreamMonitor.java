/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;

/**
 * Monitors the output stream of a system process and notifies
 * listeners of additions to the stream.
 *
 * The output stream monitor reads system out (or err) via
 * and input stream.
 */
public class OutputStreamMonitor implements IFlushableStreamMonitor {

	/**
	 * VM property which indicates maximum wait time (in milliseconds) for
	 * reading to finish, when calling {@link #getContents()}.
	 */
	private static final String MAX_WAIT_TIME_FOR_STREAM_READING_PROPERTY = "org.eclipse.debug.core.msMaxWaitTimeForStreamMonitorReading"; //$NON-NLS-1$

	/**
	 * Wait max n milliseconds for the thread to finish reading when calling
	 * {@link #getContents()}.
	 */
	private static final long MAX_WAIT_TIME_FOR_STREAM_READING = maxWaitTimeForStreamReading();

	/**
	 * The stream being monitored (connected system out or err).
	 */
	private InputStream fStream;

	/**
	 * A collection of listeners
	 */
	private ListenerList<IStreamListener> fListeners = new ListenerList<>();

	/**
	 * Whether content is being buffered
	 */
	private boolean fBuffered = true;

	/**
	 * The local copy of the stream contents
	 */
	private StringBuilder fContents;

	/**
	 * The thread which reads from the stream
	 */
	private Thread fThread;

	/**
	 * The size of the read buffer
	 */
	private static final int BUFFER_SIZE= 8192;

	/**
	 * Whether or not this monitor has been killed.
	 * When the monitor is killed, it stops reading
	 * from the stream immediately.
	 */
	private boolean fKilled= false;

	/**
	 * Last time we've read something from the stream
	 */
	private long lastSleep;

	private String fEncoding;

	/**
	 * Indicates whether the monitor is done reading from the stream.
	 */
	private final AtomicBoolean fReadingDone;

	/**
	 * Creates an output stream monitor on the
	 * given stream (connected to system out or err).
	 *
	 * @param stream input stream to read from
	 * @param encoding stream encoding or <code>null</code> for system default
	 */
	public OutputStreamMonitor(InputStream stream, String encoding) {
        fStream = new BufferedInputStream(stream, 8192);
        fEncoding = encoding;
		fContents= new StringBuilder();
		fReadingDone = new AtomicBoolean(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public synchronized void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Causes the monitor to close all
	 * communications between it and the
	 * underlying stream by waiting for the thread to terminate.
	 */
	protected void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			try {
				thread.join();
			} catch (InterruptedException ie) {
			}
			fListeners = new ListenerList<>();
		}
	}

	/**
	 * Notifies the listeners that text has
	 * been appended to the stream.
	 * @param text the text that was appended to the stream
	 */
	private void fireStreamAppended(String text) {
		getNotifier().notifyAppend(text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	@Override
	public synchronized String getContents() {
		boolean doneReading = waitForReadingToFinish(MAX_WAIT_TIME_FOR_STREAM_READING);
		if (!doneReading) {
			logWarning("OutputStreamMonitor::startMonitoring() was unable to start reading stream after " + MAX_WAIT_TIME_FOR_STREAM_READING + " ms."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return fContents.toString();
	}

	private void read() {
		fReadingDone.set(false);
		try {
			readInternal();
		} finally {
			fReadingDone.set(true);
		}
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code>
	 * method is used to allow <code>OutputStreamMonitor</code>
	 * to implement <code>Runnable</code> without publicly
	 * exposing a <code>run</code> method.
	 */
	private void readInternal() {
        lastSleep = System.currentTimeMillis();
        long currentTime = lastSleep;
		byte[] bytes= new byte[BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (fKilled) {
					break;
				}
				read= fStream.read(bytes);
				if (read > 0) {
					String text;
					if (fEncoding != null) {
						text = new String(bytes, 0, read, fEncoding);
					} else {
						text = new String(bytes, 0, read);
					}
					synchronized (this) {
						if (isBuffered()) {
							fContents.append(text);
						}
						fireStreamAppended(text);
					}
				}
			} catch (IOException ioe) {
				if (!fKilled) {
					DebugPlugin.log(ioe);
				}
				return;
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					DebugPlugin.log(e);
				}
				return;
			}

            currentTime = System.currentTimeMillis();
            if (currentTime - lastSleep > 1000) {
                lastSleep = currentTime;
                try {
                    Thread.sleep(1); // just give up CPU to maintain UI responsiveness.
                } catch (InterruptedException e) {
                }
            }
		}
		try {
			fStream.close();
		} catch (IOException e) {
			DebugPlugin.log(e);
		}
	}

	protected void kill() {
		fKilled= true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public synchronized void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Starts a thread which reads from the stream
	 */
	protected void startMonitoring() {
		if (fThread == null) {
			fThread = new Thread((Runnable) () -> read(), DebugCoreMessages.OutputStreamMonitor_label);
            fThread.setDaemon(true);
            fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IFlushableStreamMonitor#setBuffered(boolean)
	 */
	@Override
	public synchronized void setBuffered(boolean buffer) {
		fBuffered = buffer;
	}

	/**
	 * @see org.eclipse.debug.core.model.IFlushableStreamMonitor#flushContents()
	 */
	@Override
	public synchronized void flushContents() {
		fContents.setLength(0);
	}

	/**
	 * @see IFlushableStreamMonitor#isBuffered()
	 */
	@Override
	public synchronized boolean isBuffered() {
		return fBuffered;
	}

	private ContentNotifier getNotifier() {
		return new ContentNotifier();
	}

	/**
	 * Blocking call to make sure the stream reading thread is done (and so
	 * {@link #readInternal()} was called).
	 *
	 * @param timeout max time to wait for reading the entire stream
	 *
	 * @return {@code true} if the stream reading is done
	 */
	protected boolean waitForReadingToFinish(long timeout) {
		final long start = System.currentTimeMillis();
		while (!fReadingDone.get() && System.currentTimeMillis() - start < timeout) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		return fReadingDone.get();
	}

	private static long maxWaitTimeForStreamReading() {
		long maxTime = 10_000;
		String systemPropertyValue = System.getProperty(MAX_WAIT_TIME_FOR_STREAM_READING_PROPERTY);
		if (systemPropertyValue != null) {
			try {
				maxTime = Long.parseLong(systemPropertyValue);
			} catch (NumberFormatException e) {
				logError("Failed to read default value for max stream monitor read time: " + MAX_WAIT_TIME_FOR_STREAM_READING_PROPERTY, e); //$NON-NLS-1$
			}
		}
		return maxTime;
	}

	private static void logWarning(String warningMessage) {
		log(IStatus.WARNING, warningMessage, null);
	}

	private static void logError(String errorMessage, Exception e) {
		log(IStatus.ERROR, errorMessage, e);
	}

	private static void log(int statusCode, String errorMessage, Exception e) {
		IStatus errorStatus = new Status(statusCode, DebugPlugin.getUniqueIdentifier(), errorMessage, e);
		DebugPlugin.log(errorStatus);
	}

	class ContentNotifier implements ISafeRunnable {

		private IStreamListener fListener;
		private String fText;

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		@Override
		public void handleException(Throwable exception) {
			DebugPlugin.log(exception);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			fListener.streamAppended(fText, OutputStreamMonitor.this);
		}

		public void notifyAppend(String text) {
			if (text == null) {
				return;
			}
			fText = text;
			for (IStreamListener iStreamListener : fListeners) {
				fListener = iStreamListener;
				SafeRunner.run(this);
			}
			fListener = null;
			fText = null;
		}
	}
}
