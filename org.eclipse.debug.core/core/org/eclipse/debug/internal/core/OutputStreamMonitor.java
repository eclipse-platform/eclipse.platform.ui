/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Paul Pazderski  - Bug 545769: fixed rare UTF-8 character corruption bug
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
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

	private long lastSleep;

	private Charset fCharset;

	private final AtomicBoolean fDone;

	/**
	 * Creates an output stream monitor on the given stream (connected to system
	 * out or err).
	 *
	 * @param stream input stream to read from
	 * @param charset stream charset or <code>null</code> for system default
	 */
	public OutputStreamMonitor(InputStream stream, Charset charset) {
		fStream = new BufferedInputStream(stream, 8192);
		fCharset = charset;
		fContents= new StringBuilder();
		fDone = new AtomicBoolean(false);
	}

	/**
	 * Creates an output stream monitor on the given stream (connected to system
	 * out or err).
	 *
	 * @param stream input stream to read from
	 * @param encoding stream encoding or <code>null</code> for system default
	 * @deprecated use {@link #OutputStreamMonitor(InputStream, Charset)}
	 *             instead
	 */
	@Deprecated
	public OutputStreamMonitor(InputStream stream, String encoding) {
		this(stream, Charset.forName(encoding));
	}

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

	@Override
	public synchronized String getContents() {
		return fContents.toString();
	}

	private void read() {
		try {
			internalRead();
		} finally {
			fDone.set(true);
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
	private void internalRead() {
		lastSleep = System.currentTimeMillis();
		long currentTime = lastSleep;
		char[] chars = new char[BUFFER_SIZE];
		int read = 0;
		try (InputStreamReader reader = (fCharset == null ? new InputStreamReader(fStream) : new InputStreamReader(fStream, fCharset))) {
			while (read >= 0) {
				try {
					if (fKilled) {
						break;
					}
					read = reader.read(chars);
					if (read > 0) {
						String text = new String(chars, 0, read);
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
						// just give up CPU to maintain UI responsiveness.
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (IOException e) {
			DebugPlugin.log(e);
		}
	}

	protected void kill() {
		fKilled= true;
	}

	@Override
	public synchronized void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Starts a thread which reads from the stream
	 */
	protected void startMonitoring() {
		if (fThread == null) {
			fDone.set(false);
			fThread = new Thread((Runnable) this::read, DebugCoreMessages.OutputStreamMonitor_label);
			fThread.setDaemon(true);
			fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}

	@Override
	public synchronized void setBuffered(boolean buffer) {
		fBuffered = buffer;
	}

	@Override
	public synchronized void flushContents() {
		fContents.setLength(0);
	}

	@Override
	public synchronized boolean isBuffered() {
		return fBuffered;
	}

	private ContentNotifier getNotifier() {
		return new ContentNotifier();
	}

	/**
	 * @return {@code true} if reading the underlying stream is done.
	 *         {@code false} if reading the stream has not started or is not done.
	 */
	public boolean isReadingDone() {
		return fDone.get();
	}

	class ContentNotifier implements ISafeRunnable {

		private IStreamListener fListener;
		private String fText;

		@Override
		public void handleException(Throwable exception) {
			DebugPlugin.log(exception);
		}

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
