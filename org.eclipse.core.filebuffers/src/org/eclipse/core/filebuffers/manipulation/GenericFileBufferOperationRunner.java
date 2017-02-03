/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import java.util.ArrayList;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferManager;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;


/**
 * A <code>GenericFileBufferOperationRunner</code> executes
 * {@link org.eclipse.core.filebuffers.manipulation.IFileBufferOperation}.
 * The runner takes care of all aspects that are not operation specific.
 * <p>
 * This class is not intended to be subclassed. Clients instantiate this class.
 * </p>
 *
 * @see org.eclipse.core.filebuffers.manipulation.IFileBufferOperation
 * @since 3.3
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GenericFileBufferOperationRunner {

	/** The validation context */
	private final Object fValidationContext;
	/** The file buffer manager */
	private final IFileBufferManager fFileBufferManager;

	/** The lock for waiting for completion of computation in the UI thread. */
	private final Object fCompletionLock= new Object();
	/** The flag indicating completion of computation in the UI thread. */
	private transient boolean fIsCompleted;
	/** The exception thrown during the computation in the UI thread. */
	private transient Throwable fThrowable;


	/**
	 * Creates a new file buffer operation runner.
	 *
	 * @param fileBufferManager the file buffer manager
	 * @param validationContext the validationContext
	 */
	public GenericFileBufferOperationRunner(IFileBufferManager fileBufferManager, Object validationContext) {
		fFileBufferManager= fileBufferManager;
		fValidationContext= validationContext;
	}

	/**
	 * Executes the given operation for all file buffers specified by the given locations.
	 *
	 * @param locations the file buffer locations
	 * @param operation the operation to be performed
	 * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
	 * @throws CoreException in case of error
	 * @throws OperationCanceledException in case the execution get canceled
	 */
	public void execute(IPath[] locations, final IFileBufferOperation operation, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		final int size= locations.length;
		SubMonitor subMonitor= SubMonitor.convert(monitor, operation.getOperationName(), size * 200);
		try {
			IFileBuffer[] fileBuffers= createFileBuffers(locations, subMonitor.split(size * 10));

			IFileBuffer[] fileBuffers2Save= findFileBuffersToSave(fileBuffers);
			fFileBufferManager.validateState(fileBuffers2Save, subMonitor.split(size * 10), fValidationContext);
			if (!isCommitable(fileBuffers2Save))
			{
				throw new OperationCanceledException();
			}

			IFileBuffer[] unsynchronizedFileBuffers= findUnsynchronizedFileBuffers(fileBuffers);
			performOperation(unsynchronizedFileBuffers, operation, subMonitor.split(size * 40));

			final IFileBuffer[] synchronizedFileBuffers= findSynchronizedFileBuffers(fileBuffers);
			fIsCompleted= false;
			fThrowable= null;
			synchronized (fCompletionLock) {

				executeInContext(new Runnable() {
					@Override
					public void run() {
						synchronized(fCompletionLock) {
							try {
								SafeRunner.run(new ISafeRunnable() {
									@Override
									public void handleException(Throwable throwable) {
										fThrowable= throwable;
									}
									@Override
									public void run() throws Exception {
										performOperation(synchronizedFileBuffers, operation, subMonitor.split(50));
									}
								});
							} finally {
								fIsCompleted= true;
								fCompletionLock.notifyAll();
							}
						}
					}
				});

				while (!fIsCompleted) {
					try {
						fCompletionLock.wait(500);
					} catch (InterruptedException x) {
					}
				}
			}

			if (fThrowable != null) {
				if (fThrowable instanceof CoreException)
					throw (CoreException) fThrowable;
				throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, fThrowable.getLocalizedMessage(), fThrowable));
			}

			commit(fileBuffers2Save, subMonitor.split(size * 80));

		} finally {
			releaseFileBuffers(locations, subMonitor.split(size * 10));
		}
	}

	private void performOperation(IFileBuffer fileBuffer, IFileBufferOperation operation, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, 100);
		ISchedulingRule rule= fileBuffer.computeCommitRule();
		IJobManager manager= Job.getJobManager();
		manager.beginRule(rule, subMonitor.split(1));
		String name= fileBuffer.getLocation().lastSegment();
		subMonitor.setTaskName(name);
		operation.run(fileBuffer, subMonitor.split(99));
		manager.endRule(rule);
	}

	private void performOperation(IFileBuffer[] fileBuffers, IFileBufferOperation operation, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, fileBuffers.length);
		for (IFileBuffer fileBuffer : fileBuffers) {
			performOperation(fileBuffer, operation, subMonitor.split(1));
		}
	}

	private void executeInContext(Runnable runnable) {
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		fileBufferManager.execute(runnable);
	}

	private IFileBuffer[] findUnsynchronizedFileBuffers(IFileBuffer[] fileBuffers) {
		ArrayList<IFileBuffer> list= new ArrayList<>();
		for (int i= 0; i < fileBuffers.length; i++) {
			if (!fileBuffers[i].isSynchronizationContextRequested())
				list.add(fileBuffers[i]);
		}
		return list.toArray(new IFileBuffer[list.size()]);
	}

	private IFileBuffer[] findSynchronizedFileBuffers(IFileBuffer[] fileBuffers) {
		ArrayList<IFileBuffer> list= new ArrayList<>();
		for (IFileBuffer fileBuffer : fileBuffers) {
			if (fileBuffer.isSynchronizationContextRequested())
				list.add(fileBuffer);
		}
		return list.toArray(new IFileBuffer[list.size()]);
	}

	private IFileBuffer[] createFileBuffers(IPath[] locations, IProgressMonitor progressMonitor) throws CoreException {

		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, FileBuffersMessages.FileBufferOperationRunner_task_connecting, locations.length);
		try {
			IFileBuffer[] fileBuffers= new ITextFileBuffer[locations.length];
			for (int i= 0; i < locations.length; i++) {
				fFileBufferManager.connect(locations[i], LocationKind.NORMALIZE, subMonitor.split(1));
				fileBuffers[i]= fFileBufferManager.getFileBuffer(locations[i], LocationKind.NORMALIZE);
			}
			return fileBuffers;

		} catch (CoreException x) {
			try {
				releaseFileBuffers(locations, new NullProgressMonitor());
			} catch (CoreException e) {
			}
			throw x;
		}
	}

	private void releaseFileBuffers(IPath[] locations, IProgressMonitor progressMonitor) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, FileBuffersMessages.FileBufferOperationRunner_task_disconnecting, locations.length);
		final ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		for (IPath location : locations) {
			fileBufferManager.disconnect(location, LocationKind.NORMALIZE, subMonitor.split(1));
		}
	}

	private IFileBuffer[] findFileBuffersToSave(IFileBuffer[] fileBuffers) {
		ArrayList<IFileBuffer> list= new ArrayList<>();
		for (IFileBuffer fileBuffer : fileBuffers) {
			IFileBuffer buffer= fileBuffer;
			if (!buffer.isDirty())
				list.add(buffer);
		}
		return list.toArray(new IFileBuffer[list.size()]);
	}

	private boolean isCommitable(IFileBuffer[] fileBuffers) {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (!fileBuffers[i].isCommitable())
				return false;
		}
		return true;
	}

	protected ISchedulingRule computeCommitRule(IFileBuffer[] fileBuffers) {
		ArrayList<ISchedulingRule> list= new ArrayList<>();
		for (IFileBuffer fileBuffer : fileBuffers) {
			ISchedulingRule rule= fileBuffer.computeCommitRule();
			if (rule != null)
				list.add(rule);
		}
		ISchedulingRule[] rules= new ISchedulingRule[list.size()];
		list.toArray(rules);
		return new MultiRule(rules);
	}

	protected void commit(final IFileBuffer[] fileBuffers, final IProgressMonitor progressMonitor) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, 2);
		ISchedulingRule rule= computeCommitRule(fileBuffers);
		Job.getJobManager().beginRule(rule, subMonitor.split(1));
		try {
			doCommit(fileBuffers, subMonitor.split(1));
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}

	protected void doCommit(final IFileBuffer[] fileBuffers, IProgressMonitor progressMonitor) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, FileBuffersMessages.FileBufferOperationRunner_task_committing, fileBuffers.length);
		for (IFileBuffer fileBuffer : fileBuffers) {
			fileBuffer.commit(subMonitor.split(1), true);
		}
	}

}
