package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import java.io.IOException;

public class StreamsProxy implements IStreamsProxy {
	/**
	 * The monitor for the output stream (connected to standard out of the process)
	 */
	private OutputStreamMonitor fOutputMonitor;
	/**
	 * The monitor for the error stream (connected to standard error of the process)
	 */
	private OutputStreamMonitor fErrorMonitor;
	/**
	 * The monitor for the input stream (connected to standard in of the process)
	 */
	private InputStreamMonitor fInputMonitor;
	/**
	 * Records the open/closed state of communications with
	 * the underlying streams.
	 */
	private boolean fClosed= false;
	/**
	 * Creates a <code>StreamsProxy</code> on the streams
	 * of the given <code>IProcess</code>.
	 */
	public StreamsProxy(RuntimeProcess process) {
		if (process == null) {
			return;
		}
		fOutputMonitor= new OutputStreamMonitor(process.getInputStream());
		fErrorMonitor= new OutputStreamMonitor(process.getErrorStream());
		fInputMonitor= new InputStreamMonitor(process.getOutputStream());
		fOutputMonitor.startMonitoring();
		fErrorMonitor.startMonitoring();
		fInputMonitor.startMonitoring();
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams after all remaining data
	 * in the streams is read.
	 */
	protected void close() {
		if (!fClosed) {
			fClosed= true;
			fOutputMonitor.close();
			fErrorMonitor.close();
			fInputMonitor.close();
		}
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams immediately.
	 * Data remaining in the streams is lost.
	 */	
	protected void kill() {
		fClosed= true;
		fOutputMonitor.kill();
		fErrorMonitor.kill();
		fInputMonitor.close();
	}

	/**
	 * @see IStreamsProxy#getErrorStreamMonitor()
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see IStreamsProxy#getOutputStreamMonitor()
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	/**
	 * @see IStreamsProxy#write(String)
	 */
	public void write(String input) throws IOException {
		if (!fClosed) {
			fInputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}

}
