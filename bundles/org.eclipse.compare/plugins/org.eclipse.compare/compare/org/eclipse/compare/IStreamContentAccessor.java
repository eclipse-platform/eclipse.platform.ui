/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * An <code>IStreamContentAccessor</code> object represents a set of bytes which can be
 * accessed by means of a stream.
 * <p>
 * Clients may implement this interface, or use the standard implementation,
 * <code>BufferedContent</code>.
 *
 * @see BufferedContent
 */
public interface IStreamContentAccessor {
	/**
	 * Returns an open <code>InputStream</code> for this object which can be used to retrieve the object's content.
	 * The client is responsible for closing the stream when finished.
	 * Returns <code>null</code> if this object has no streamable contents.
	 *
	 * @return an input stream containing the contents of this object
	 * @exception CoreException if the contents of this object could not be accessed
	 */
	InputStream getContents() throws CoreException;
}
