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
	 * The monitor for the input stream (connected to standard out of the process)
	 */
	private InputStreamMonitor fInputMonitor;
	/**
	 * The monitor for the error stream (connected to standard error of the process)
	 */
	private InputStreamMonitor fErrorMonitor;
	/**
	 * The monitor for the output stream (connected to standard in of the process)
	 */
	private OutputStreamMonitor fOutputMonitor;
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
		fInputMonitor= new InputStreamMonitor(process.getInputStream());
		fErrorMonitor= new InputStreamMonitor(process.getErrorStream());
		fOutputMonitor= new OutputStreamMonitor(process.getOutputStream());
		fInputMonitor.startMonitoring();
		fErrorMonitor.startMonitoring();
		fOutputMonitor.startMonitoring();
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams.
	 */
	protected void close() {
		fClosed= true;
		fInputMonitor.close();
		fErrorMonitor.close();
		fOutputMonitor.close();
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
		return fInputMonitor;
	}

	/**
	 * @see IStreamsProxy#write(String)
	 */
	public void write(String input) throws IOException {
		if (!fClosed) {
			fOutputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}

}
