package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @see IStreamsProxy
 */
public class StreamsProxy implements IStreamsProxy {
	/**
	 * The monitor for the input stream (connected to standard out of the process)
	 */
	protected InputStreamMonitor fInputMonitor;
	/**
	 * The monitor for the error stream.
	 */
	protected InputStreamMonitor fErrorMonitor;
	/**
	 * The <code>OutputStream</code> of the <code>IProcess</code>.
	 */
	protected OutputStream fOutputStream;
	/**
	 * Records the open/closed state of communications with
	 * the underlying streams.
	 */
	protected boolean fClosed= false;
	/**
	 * Creates a <code>StreamsProxy</code> on the streams
	 * of the given <code>IProcess</code>.
	 */
	public StreamsProxy(RuntimeProcess process) {
		if (process != null) {
			fInputMonitor= new InputStreamMonitor(process.getInputStream());
			fErrorMonitor= new InputStreamMonitor(process.getErrorStream());
			fOutputStream= process.getOutputStream();
			fInputMonitor.startMonitoring();
			fErrorMonitor.startMonitoring();
		}
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams.
	 */
	public void close() {
		fClosed= true;
		fInputMonitor.close();
		fErrorMonitor.close();
	}

	/**
	 * @see IStreamsProxy
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see IStreamsProxy
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		return fInputMonitor;
	}

	/**
	 * @see IStreamsProxy
	 */
	public void write(String input) throws IOException {
		if (!fClosed && fOutputStream != null) {
			fOutputStream.write(input.getBytes());
			fOutputStream.flush();
		} else {
			throw new IOException();
		}
	}

}
