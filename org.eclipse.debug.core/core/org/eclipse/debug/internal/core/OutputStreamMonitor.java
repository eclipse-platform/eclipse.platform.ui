package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

/**
 * Monitors the output stream of a system process and notifies 
 * listeners of additions to the stream.
 * 
 * The output stream monitor reads system out (or err) via
 * and input stream.
 */
public class OutputStreamMonitor implements IStreamMonitor {
	/**
	 * The stream being monitored (connected system out or err).
	 */
	private InputStream fStream;

	/**
	 * A collection of listeners
	 */
	private ListenerList fListeners= new ListenerList(1);

	/**
	 * The local copy of the stream contents
	 */
	private StringBuffer fContents;

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
	 * Creates an output stream monitor on the
	 * given stream (connected to system out or err).
	 */
	public OutputStreamMonitor(InputStream stream) {
		fStream= stream;
		fContents= new StringBuffer();
	}

	/**
	 * @see IStreamMonitor#addListener(IStreamListener)
	 */
	public void addListener(IStreamListener listener) {
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
			fListeners.removeAll();
		}
	}

	/**
	 * Notifies the listeners that text has
	 * been appended to the stream.
	 */
	private void fireStreamAppended(String text) {
		if (text == null)
			return;
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			 ((IStreamListener) copiedListeners[i]).streamAppended(text, this);
		}
	}

	/**
	 * @see IStreamMonitor#getContents()
	 */
	public String getContents() {
		return fContents.toString();
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code>
	 * method is used to allow <code>OutputStreamMonitor</code>
	 * to implement <code>Runnable</code> without publicly
	 * exposing a <code>run</code> method.
	 */
	private void read() {
		byte[] bytes= new byte[BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (fKilled) {
					break;
				}
				read= fStream.read(bytes);
				if (read > 0) {
					String text= new String(bytes, 0, read);
					fContents.append(text);
					fireStreamAppended(text);
				}
			} catch (IOException ioe) {
				DebugPlugin.log(ioe);
				return;
			}
		}
	}
	
	protected void kill() {
		fKilled= true;
		if (fStream != null) {
			try {
				fStream.close();
			} catch(IOException e) {
				DebugPlugin.log(e);
			}
		}
	}

	/**
	 * @see IStreamMonitor#removeListener(IStreamListener)
	 */
	public void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Starts a thread which reads from the stream
	 */
	protected void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					read();
				}
			}, DebugCoreMessages.getString("OutputStreamMonitor.label")); //$NON-NLS-1$
			fThread.start();
		}
	}
}
