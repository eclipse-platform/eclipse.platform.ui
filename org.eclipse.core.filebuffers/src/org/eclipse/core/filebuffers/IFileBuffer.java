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
import org.eclipse.core.runtime.IStatus;


/**
 * A file buffer represents a file that can be edited by more than one client.
 * Editing is session oriented. This means that editing is a sequence of
 * modification steps. The start of the sequence and the end of the sequence are
 * explicitly indicated. There are no time constraints connected with the
 * sequence of modification steps. A file buffer reifies editing sessions and
 * allows them to interleave.
 * <p>
 * It is not specified whether simultaneous editing sessions can be owned by
 * different threads.
 * <p>
 * Clients are not supposed to implement that interface. Instances of this type
 * are obtained from a {@link org.eclipse.core.filebuffers.IFileBufferManager}.
 * 
 * @since 3.0
 */
public interface IFileBuffer {
	
	/**
	 * Returns the location of this file buffer.
	 * <p>
	 * The location is either a full path of a workspace resource or an
	 * absolute path in the local file system.
	 * </p>
	 * 
	 * @return the location of this file buffer
	 */
	IPath getLocation();

	/**
	 * Returns whether this file buffer is synchronized with the file system. This is when
	 * the file buffer's underlying file is in synchronization with the file system and the file buffer
	 * has been initialized after the underlying files has been modified the last time.
	 * 
	 * @return <code>true</code> if the file buffer is synchronized with the file system
	 */
	boolean isSynchronized();
	
	/**
	 * Reverts the contents of this file buffer to the content of its underlying file. After 
	 * that call successfully returned, <code>isDirty</code> returns <code>false</code> and
	 * <code>isSynchronized</code> returns <code>true</code>.
	 * 
	 * @param monitor the progress monitor
	 * @throws CoreException  if reading or accessing the underlying file fails
	 */
	void revert(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Commits this file buffer by changing the contents of the underlying file to
	 * the contents of this file buffer. After that call, <code>isDirty</code> 
	 * returns <code>false</code> and <code>isSynchronized</code> returns
	 * <code>true</code>.
	 * 
	 * @param monitor the progress monitor
	 * @param overwrite indicates whether the underlying file should be overwritten if it is not synchronized with the file system
	 * @throws CoreException if writing or accessing the underlying file fails
	 */
	void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException;
	
	/**
	 * Returns whether changes have been applied to this file buffer since initialization, or the most
	 * recent <code>revert</code> or <code>commit</code> call.
	 * 
	 * @return <code>true</code> if changes have been applied to this buffer
	 */
	boolean isDirty();
	
	/**
	 * Returns whether this file buffer is shared by more than one client.
	 * 
	 * @return <code>true</code> if this file buffer is shared by more than one client
	 */
	boolean isShared();
	
	/**
	 * Validates the state of this file buffer and tries to bring the buffer's
	 * underlying file into a state in which it can be modified. If state
	 * validation is not supported this operation does nothing.
	 * 
	 * @param monitor the progress monitor
	 * @param computationContext the context in which the validation is
	 *               performed, e.g., a SWT shell
	 * @exception CoreException if the underlying file can not be accessed to
	 *                    it's state cannot be changed
	 */
	void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException;
	
	/**
	 * Returns whether the state of this file buffer has been validated. If
	 * state validation is not supported this method always returns <code>true</code>.
	 * 
	 * @return <code>true</code> if the state has been validated, <code>false</code>
	 *            otherwise
	 */
	boolean isStateValidated();
	
	/**
	 * Resets state validation. If state validation is supported, <code>isStateValidated</code>
	 * afterwards returns <code>false</code> until the state is revalidated.
	 */
	void resetStateValidation();
	
	/**
	 * Returns the status of this file buffer. This is the result of the last operation performed on this file buffer or
	 * internally initiated by this file buffer.
	 * 
	 * @return the status of this file buffer
	 */
	IStatus getStatus();
	
	/**
	 * Returns the modification stamp of the file underlying this file buffer.
	 * 
	 * @return the modification stamp of the file underlying this file buffer
	 */
	long getModificationStamp();
}
