package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Writes to an output stream (connected to the input stream of
 * a system process), queueing output if the output stream is blocked.
 */
public class OutputStreamMonitor {
	
	/**
	 * The output stream which is being written to.
	 */
	private OutputStream fStream;
	/**
	 * The queue of output.
	 */
	private Vector fQueue;
	/**
	 * The thread which writes to the output stream.
	 */
	private Thread fThread;
	/**
	 * A lock for ensuring that writes to the queue are contiguous
	 */
	private Object fLock;
	
	/**
	 * Creates an output stream monitor on the
	 * given output stream.
	 */
	public OutputStreamMonitor(OutputStream stream) {
		fStream= stream;
		fQueue= new Vector();
		fLock= new Object();
	}
	
	/**
	 * Appends the given text to the stream, or
	 * queues the text to be written at a later time
	 * if the stream is blocked.
	 */
	public void write(String text) {
		synchronized(fLock) {
			fQueue.add(text);
			fLock.notifyAll();
		}
	}

	/**
	 * Starts a thread which writes the stream.
	 */
	public void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					write();
				}
			}, DebugCoreMessages.getString("OutputStreamMonitor.label")); //$NON-NLS-1$
			fThread.start();
		}
	}
	
	/**
	 * Close all communications between this
	 * monitor and the underlying stream.
	 */
	public void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			thread.interrupt(); 
		}
	}
	
	/**
	 * Continuously writes to the stream.
	 */
	protected void write() {
		while (fThread != null) {
			writeNext();
		}	
	}
	
	/**
	 * Write the text in the queue to the stream.
	 */
	protected void writeNext() {
		while (!fQueue.isEmpty()) {
			String text = (String)fQueue.firstElement();
			fQueue.removeElementAt(0);
			try {
				if (fStream != null) {
					fStream.write(text.getBytes());
					fStream.flush();
				}
			} catch (IOException e) {
				DebugCoreUtils.logError(e);
			}
		}
		try {
			synchronized(fLock) {
				fLock.wait();
			}
		} catch (InterruptedException e) {
		}
	}
}

