/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A file buffer represents a file that can be edited by more than one client. Editing is
 * session oriented. This means that editing is a sequence of modification steps. The
 * start of the sequence and the end of the sequence are explicitly indicated. There are
 * no time constraints connected with the sequence of modification steps. A file buffer
 * reifies editing sessions and allows them to interleave.<p>
 * It is not sepcified whether simultaneous editing sessions can be owned by different
 * threads.
 * 
 * @since 3.0
 */
public interface IFileBuffer {
	
	/**
	 * Returns the underlying file of this file buffer.
	 * 
	 * @return the underlying file of this file buffer
	 */
	IFile getUnderlyingFile();

	/**
	 * Returns whether the underlying file of this file buffer has changed.
	 * 
	 * @return <code>true</code> if the underlying file of this file buffer has changed
	 */
	boolean hasUnderlyingFileChanged();
	
	/**
	 * Reverts the contents of this file buffer to the content of its underlying file. After 
	 * that call successfully returned, <code>isDirty</code> returns <code>false</code>.
	 * 
	 * @param monitor the progress monitor
	 * @throws CoreException  if reading or accessing the underlying file fails
	 */
	void revert(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Commits this file buffer by changing the contents of th underlying file to
	 * the contents of this file buffer. After that call, <code>isDirty</code> 
	 * returns <code>false</code>.
	 * 
	 * @param monitor the progress monitor
	 * @param overwrite indicates whether the underlying file should be overwritten if it is not uin synch with the file system
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
	 * Validates the state of this file buffer and tries to bring the buffer's underlying file into
	 * a state in which it can be modified.
	 * 
	 * @param monitor the progress monitor
	 * @param computationContext the context in which the validation is performed, e.g., a SWT shell
	 * @exception CoreException if the underlying file can not be accessed to it's state cannot be changed
	 */
	void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException;
	
	/**
	 * Returns whether the state of this file buffer has been validated.
	 * 
	 * @return <code>true</code> if the state has been validated, <code>false</code> otherwise
	 */
	boolean isStateValidated();
	
	/**
	 * Returns the status of this file buffer. This is the result of the last operation peformed on this file buffer or
	 * internally initiated by this file buffer.
	 * 
	 * @return the status of this file buffer
	 */
	IStatus getStatus();
}
