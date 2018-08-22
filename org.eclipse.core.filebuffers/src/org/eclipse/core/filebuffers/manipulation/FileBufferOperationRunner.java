/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferManager;


/**
 * A <code>FileBufferOperationRunner</code> executes
 * {@link org.eclipse.core.filebuffers.manipulation.IFileBufferOperation}.
 * The runner takes care of all aspects that are not operation specific.
 * <p>
 * This class is not intended to be subclassed. Clients instantiate this class.
 * </p>
 *
 * @see org.eclipse.core.filebuffers.manipulation.IFileBufferOperation
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileBufferOperationRunner extends GenericFileBufferOperationRunner {

	/**
	 * Creates a new file buffer operation runner.
	 *
	 * @param fileBufferManager the file buffer manager
	 * @param validationContext the validationContext
	 */
	public FileBufferOperationRunner(IFileBufferManager fileBufferManager, Object validationContext) {
		super(fileBufferManager, validationContext);
	}

	@Override
	protected void commit(final IFileBuffer[] fileBuffers, final IProgressMonitor progressMonitor) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				doCommit(fileBuffers, progressMonitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, computeCommitRule(fileBuffers), IWorkspace.AVOID_UPDATE, progressMonitor);
	}
}
