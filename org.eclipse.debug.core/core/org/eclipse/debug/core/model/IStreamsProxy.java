package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;

/**
 * A streams proxy acts as proxy between the streams of a
 * process and interested clients. This abstraction allows
 * implementations of <code>IProcess</code> to handle I/O related
 * to the standard input, output, and error streams associated
 * with a process.
 * <p>
 * Clients implementing the <code>IProcess</code> interface must also
 * provide an implementation of this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IProcess
 */
public interface IStreamsProxy {
	/**
	 * Returns a monitor for the error stream of this proxy's process,
	 * or <code>null</code> if not supported.
	 * The monitor is connected to the error stream of the
	 * associated process.
	 *
	 * @return an error stream monitor, or <code>null</code> if none
	 */
	public IStreamMonitor getErrorStreamMonitor();
	/**
	 * Returns a monitor for the output stream of this proxy's process,
	 * or <code>null</code> if not supported.
	 * The monitor is connected to the output stream of the
	 * associated process.
	 *
	 * @return an output stream monitor, or <code>null</code> if none
	 */
	public IStreamMonitor getOutputStreamMonitor();
	/**
	 * Writes the given text to the output stream connected to the
	 * standard input stream of this proxy's process.
	 *
	 * @exception IOException when an error occurs writing to the 
	 *		underlying <code>OutputStream</code>.
	 *
	 */
	public void write(String input) throws IOException;
}
