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
 *
 */
public interface IFileBuffer {
	
	IFile getUnderlyingFile();
	
	void addFileBufferListener(IFileBufferListener listener);
	
	void removeFileBufferListener(IFileBufferListener listener);
	
	void revert(IProgressMonitor monitor) throws CoreException;
	
	void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException;
		
	boolean isDirty();
	
	void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException;
	
	boolean isStateValidated();
	
	IStatus getStatus();
}
