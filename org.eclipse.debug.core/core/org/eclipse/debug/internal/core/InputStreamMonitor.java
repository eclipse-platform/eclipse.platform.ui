package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import java.io.IOException;
import java.io.InputStream;

/**
 * Monitors an input stream (connected to the output stream of
 * a system process). An notifies listeners of additions to the
 * stream.
 * 
 * @see IStreamMonitor
 */
public class InputStreamMonitor implements IStreamMonitor {
	/**
	 * The input stream being monitored.
	 */
	protected InputStream fStream;

	/**
	 * A collection of listeners
	 */
	protected ListenerList fListeners= new ListenerList(1);

	/**
	 * The local copy of the stream contents
	 */
	protected StringBuffer fContents;

	/**
	 * The thread which reads from the stream
	 */
	protected Thread fThread;

	/**
	 * The size of the read buffer
	 */
	protected static final int BUFFER_SIZE= 8192;

	/**
	 * The number of milliseconds to pause
	 * between reads.
	 */
	protected static final long DELAY= 50L;
	/**
	 * Creates an input stream monitor on the
	 * given input stream.
	 */
	public InputStreamMonitor(InputStream stream) {
		fStream= stream;
		fContents= new StringBuffer();
	}

	/**
	 * @see IStreamMonitor
	 */
	public void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Causes the monitor to close all
	 * communications between it and the
	 * underlying stream.
	 */
	public void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			try {
				thread.join();
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * Notifies the listeners that text has
	 * been appended to the stream.
	 */
	public void fireStreamAppended(String text) {
		if (text == null)
			return;
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			 ((IStreamListener) copiedListeners[i]).streamAppended(text, this);
		}
	}

	/**
	 * @see IStreamMonitor
	 */
	public String getContents() {
		return fContents.toString();
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code>
	 * method is used to allow <code>InputStreamMonitor</code>
	 * to implement <code>Runnable</code> without publicly
	 * exposing a <code>run</code> method.
	 */
	private void read() {
		byte[] bytes= new byte[BUFFER_SIZE];
		while (true) {
			try {
				if (fStream.available() == 0) {
					if (fThread == null)
						break;
				} else {
					int read= fStream.read(bytes);
					if (read > 0) {
						String text= new String(bytes, 0, read);
						fContents.append(text);
						fireStreamAppended(text);
					}
				}
			} catch (IOException ioe) {
			}
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * @see IStreamMonitor
	 */
	public void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Starts a <code>Thread</code> which reads the stream.
	 */
	public void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					read();
				}
			}, DebugCoreMessages.getString("InputStreamMonitor.label")); //$NON-NLS-1$
			fThread.start();
		}
	}
}
