/**********************************************************************
Copyright (c) 2000, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * A file buffer manager manages file buffers for files while the files are
 * connected to the file buffer manager. In order to connect a file to a file
 * buffer manager call <code>connect</code>. After that call has
 * successfully completed the file buffer can be obtained by <code>getFileBuffer</code>.
 * The file buffer is created on the first connect and diposed on the last
 * disconnect. I.e. the file buffer manager keeps track of how often a file is
 * connected and returns the same file buffer to each client as long as the
 * file is connected.
 * 
 * @since 3.0
 */
public interface IFileBufferManager {

	/**
	 * Connects the file at the given location to this manager. After that call
	 * successfully completed it is guaranteed that each call to <code>getFileBuffer</code>
	 * returns the same file buffer until <code>disconnect</code> is called.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system. The file buffer manager does
	 * not resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 * 
	 * @param location the location of the file to be connected
	 * @param monitor the progress monitor
	 * @throws CoreException if the file could not successfully be connected
	 */
	void connect(IPath location, IProgressMonitor monitor) throws CoreException;

	/**
	 * Disconnects the file at the given location from this manager. After that
	 * call successfully completed there is no guarantee that <code>getFileBuffer</code>
	 * will return a valid file buffer.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system. The file buffer manager does
	 * not resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 * 
	 * @param location the location of the file to be disconnected
	 * @param monitor the progress monitor
	 * @throws CoreException if the file could not successfully be disconnected
	 */
	void disconnect(IPath location, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the file buffer managed for the given location or <code>null</code>
	 * if there is no such file buffer.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system. The file buffer manager does
	 * not resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 * 
	 * @param location the location
	 * @return the file buffer managed for that location or <code>null</code>
	 */
	IFileBuffer getFileBuffer(IPath location);

	/**
	 * Sets the synchronization context for this file buffer manager, i.e., for
	 * all file buffers this manager manages.
	 * 
	 * @param context the synchronization context managed by this file buffer
	 *               manager
	 */
	void setSynchronizationContext(ISynchronizationContext context);

	/**
	 * The caller requests that the synchronization context is used to
	 * synchronize the given location with its file buffer. This call as no
	 * effect if there is no file buffer managed for the given location.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 * 
	 * @param location the location
	 */
	void requestSynchronizationContext(IPath location);

	/**
	 * The caller no longer requests the synchronization context for the file
	 * buffer managed for the given location. This method has no effect if there
	 * is no file buffer managed for this location.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 * 
	 * @param location the location
	 */
	void releaseSynchronizationContext(IPath location);

	/**
	 * Adds the given listener to the list of file buffer listeners. After that
	 * call the listener is informed about changes related to this file
	 * buffer manager. If the listener is already registered with the file buffer, this
	 * call has no effect.
	 * 
	 * @param listener the listener to be added
	 */
	void addFileBufferListener(IFileBufferListener listener);

	/**
	 * Removes the given listener from the list of file buffer listeners. If
	 * the listener is not registered with this file buffer, this call has no
	 * effect.
	 * 
	 * @param listener the listener to be removed
	 */
	void removeFileBufferListener(IFileBufferListener listener);
}
