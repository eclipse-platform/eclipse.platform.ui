package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Writes to an output stream, queueing output if the
 * output stream is blocked.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class OutputStreamMonitor {
	
	private final static String PREFIX= "output_stream_monitor.";
	private final static String LABEL= PREFIX + "label";
	
	protected OutputStream fStream;
	protected Vector fQueue;
	protected Thread fThread; 
	protected Object fLock;
	
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
	 * Starts a <code>Thread</code> which writes the stream.
	 */
	public void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					write();
				}
			}, DebugCoreUtils.getResourceString(LABEL));
			fThread.start();
		}
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
			thread.interrupt(); 
		}
	}
	
	protected void write() {
		while (fThread != null) {
			writeNext();
		}	
	}
	
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

