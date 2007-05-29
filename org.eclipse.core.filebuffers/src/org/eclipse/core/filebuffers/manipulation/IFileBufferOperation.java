/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.filebuffers.IFileBuffer;

/**
 * A file buffer operation performs changes of the contents of a file buffer.
 * <p>
 * File buffer operations can be executed by a
 * {@link org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner} or
 * a {@link org.eclipse.core.filebuffers.manipulation.GenericFileBufferOperationRunner}.
 * The operation runner takes care of all aspects that are common to file buffer
 * manipulation such as creating file buffers, state validation, committing file
 * buffers, etc. Thus, the purpose of <code>IFileBufferOperation</code> is
 * constrained to buffer content manipulation.
 *
 * @see org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner
 * @since 3.1
 */
public interface IFileBufferOperation {

	/**
	 * Returns the name of this file buffer operation. The operation name is
	 * used by the <code>FileBufferOperationRunner</code> while reporting
	 * progress.
	 *
	 * @return the operation name or <code>null</code>
	 */
	String getOperationName();

	/**
	 * Runs this operation, that is manipulates the content of the given file
	 * buffer.
	 *
	 * @param fileBuffer the file buffer
	 * @param monitor the progress monitor
	 * @throws CoreException in case the content manipulation failed
	 * @throws OperationCanceledException in case the monitor has been set to canceled
	 */
	void run(IFileBuffer fileBuffer, IProgressMonitor monitor) throws CoreException, OperationCanceledException;
}
