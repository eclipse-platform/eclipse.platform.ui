/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.filebuffers.IFileBuffer;

/**
 * @since 3.0
 */
public abstract class AbstractFileBuffer implements IFileBuffer {
	
	
	public abstract void create(IPath location, IProgressMonitor monitor) throws CoreException;
	
	public abstract void connect();
	
	public abstract void disconnect() throws CoreException;
	
	public abstract boolean isDisposed();

	public abstract void requestSynchronizationContext();
	
	public abstract void releaseSynchronizationContext();
}
